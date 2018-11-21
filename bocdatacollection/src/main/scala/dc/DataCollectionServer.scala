package dc

import java.net.InetSocketAddress
import java.util.concurrent.{LinkedBlockingQueue, Executors}

import com.twitter.app.App
import com.twitter.finagle.channel.{OpenConnectionsThresholds, IdleConnectionFilter}
import com.twitter.finagle.filter.MaskCancelFilter
import com.twitter.finagle.http.HttpMuxer
import com.twitter.finagle.service.TimeoutFilter
import com.twitter.finagle.util.DefaultTimer
import com.twitter.finagle._
import com.twitter.server.{Admin, AdminHttpServer, Lifecycle, Stats}
import com.twitter.util.{Future, Await, Duration}
import dc.boc.{BOCService}

import dc.cache.{BOCEvent, BOCEventLoader}
import dc.debug.{DebugHandler, Debug}
import dc.utils.{Config, Logging}
import de.utils._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpHeaders, HttpResponseStatus, HttpRequest, HttpResponse}

trait AdvancedServer extends App
with AdminHttpServer
with Admin
with Lifecycle
with Stats
with Debug

object DataCollectionServer extends AdvancedServer with Logging {
  private val bidderReceiver = statsReceiver.scope("chinapex")

  val eventQueue = new LinkedBlockingQueue[BOCEvent](1000000)

  def main() {

    val dbLoader = BOCEventLoader.registerModules().start()

    val bidService = BOCService

    val muxService = new HttpMuxer()
      .withHandler("getdata", bidService)

   // val service =  muxService
    val server = de.utils.HttpServer.serve(new InetSocketAddress(Config.serverPort), muxService)
    logInfo(s"DataCollectionServer running...proxy:${Config.proxyenable},Server:${Config.proxyserver},proxyport:${Config.proxyport}")
    Await.result(server)
  }





}
