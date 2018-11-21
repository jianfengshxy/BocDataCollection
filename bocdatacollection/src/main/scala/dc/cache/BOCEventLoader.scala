package dc.cache

import java.io.Closeable
import java.sql.{Connection, DriverManager, Statement}


import com.twitter.conversions.time._
import com.twitter.util.{CountDownLatch, ScheduledThreadPoolTimer}
import dc.boc.BOCService
import dc.utils.{Logging, ServerInfoUtils}
import dc.DataCollectionServer
import org.joda.time.DateTime
import sun.misc.{BASE64Decoder, BASE64Encoder}

import scala.collection.JavaConversions._

/**
 * Created by pt on 14-12-18.
 */

object BOCEventLoader extends Logging {
  val ONE_DAY_SECS = 24 * 3600
  val MAX_CACHE_LAG_NUM = 200
  val timer = new ScheduledThreadPoolTimer()

  val eventQueue = DataCollectionServer.eventQueue
  val countDownLatch = new CountDownLatch(1)
  var isDBReady = false
//  val maxCacheOffset = CacheConsumer.getCurrentMaxOffset(2,4,true)
//  @volatile var currentOffset = 0l
  @volatile var lastUpdateTime = ServerInfoUtils.getCurrentTime
  private val runner = new Thread("BOCEventLoader") {
    override def run(): Unit = BOCEventLoader.run()
  }

  sys.addShutdownHook(shutdown())

  def start(): this.type = {
    runner.start()
    this
  }

  def shutdown() = {
    timer.stop()
    runner.interrupt()
  }

  def await() = countDownLatch.await()

  def run() = {

    startDBEventHandler()
  }

  def registerModules(modules: String*): this.type = {
    this
  }

  def startDBCleaner() = {
    val time = new DateTime
   // val waitTime = ONE_DAY_SECS - time.getSecondOfDay + 5
    val waitTime =  300.seconds
    logInfo(s"dbCleaner will start in $waitTime ")
    timer.schedule(300.seconds, waitTime) {
//      eventQueue.offer(DBCleanEvent(System.currentTimeMillis() / 1000))
       eventQueue.offer(cleanCustomerData(System.currentTimeMillis() / 1000))
    }
  }


  def startDBEventHandler() = {
    while (!Thread.currentThread().isInterrupted) {
      var event:BOCEvent = null
      try {
        event = eventQueue.take()
        handle(event)
      } catch {
        case ex: InterruptedException =>
          logInfo(s"${Thread.currentThread().getName} was interrupted")
          Thread.currentThread().interrupt()
        case ex: Throwable =>
          logWarning(s"Error happened while handle event $event", ex)
      }
    }
  }

  def handle(event:BOCEvent): Unit = event match {

   case handleBocData(json,ctx) =>

         BOCService.process_data(ctx,json)
    case  cleanCustomerData(cleanTime) =>
          logInfo(s"start cleaner at $cleanTime")
          BOCService.cleanData()
    case _ =>
      logWarning(s"unhandled event $event")
  }

}
