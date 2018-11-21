package de.utils

import java.util.concurrent.atomic.AtomicInteger

import com.twitter.collection.BucketGenerationalQueue
import com.twitter.finagle.service.FailedService
import com.twitter.finagle._
import com.twitter.finagle.channel.OpenConnectionsThresholds
import com.twitter.finagle.stats.{NullStatsReceiver, StatsReceiver}
import com.twitter.util.{Duration, Future}

/**
 * Created by pt on 15-4-29.
 */
class IdleConnectionFilter[Req, Rep](
                                      self: ServiceFactory[Req, Rep],
                                      threshold: OpenConnectionsThresholds,
                                      statsReceiver: StatsReceiver = NullStatsReceiver
                                      ) extends ServiceFactoryProxy[Req, Rep](self) {
  private[this] val queue = new BucketGenerationalQueue[ClientConnection](threshold.idleTimeout)
  private[this] val connectionCounter = new AtomicInteger(0)
  private[this] val idle = statsReceiver.addGauge("idle") {
    queue.collectAll(threshold.idleTimeout).size
  }
  private[this] val refused = statsReceiver.counter("refused")
  private[this] val closed = statsReceiver.counter("closed")

  def openConnections = connectionCounter.get()

  override def apply(c: ClientConnection) = {
    c.onClose ensure { connectionCounter.decrementAndGet() }
    if (accept(c)) {
      queue.add(c)
      c.onClose ensure {
        queue.remove(c)
      }
      self(c) map { filterFactory(c) andThen _ }
    } else {
      refused.incr()
      val address = c.remoteAddress
      c.close()
      Future.value(new FailedService(new ConnectionRefusedException(address)))
    }
  }

  // This filter is responsible for adding/removing a connection to/from the idle tracking
  // system during the phase when the server is computing the result.
  // So if a request take a long time to be processed, we will never detect it as idle
  // NB: private[channel] for testing purpose only

  // TODO: this should be connection (service acquire/release) based, not request based.
  private[this] def filterFactory(c: ClientConnection) = new SimpleFilter[Req, Rep] {
    def apply(request: Req, service: Service[Req, Rep]) = {
      queue.touch(c)
      service(request)
    }
  }

  private[this] def closeIdleConnections() =
    queue.collect(threshold.idleTimeout) match {
      case Some(conn) =>
        conn.close()
        closed.incr()
        true

      case None =>
        false
    }

  def closeAllIdleConnections(idleTimeout: Duration = threshold.idleTimeout): Unit = {
    queue.collectAll(idleTimeout) foreach( _.close())
  }

  private[this] def accept(c: ClientConnection): Boolean = {
    val connectionCount = connectionCounter.incrementAndGet()
    if (connectionCount <= threshold.lowWaterMark)
      true
    else if (connectionCount <= threshold.highWaterMark) {
      closeIdleConnections() // whatever the result of this, we accept the connection
      true
    } else {
      // Try to close idle connections, if we don't find any, then we refuse the connection
      closeIdleConnections()
    }
  }
}
