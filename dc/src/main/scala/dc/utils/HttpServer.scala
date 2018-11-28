package dc.utils

import java.net.SocketAddress
import java.util.concurrent.TimeUnit

import com.twitter.finagle.http.{HttpTransport, HttpServerTracingFilter}
import com.twitter.finagle.http.codec.HttpServerDispatcher
import com.twitter.finagle.http.filter.DtabFilter
import com.twitter.finagle.server.DefaultServer
import com.twitter.finagle.{ServerCodecConfig, http}
import com.twitter.finagle.netty3.Netty3Listener
import com.twitter.util.Duration
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpRequest}
import com.twitter.conversions.time._

/**
  * Created by pt on 15-11-16.
  */
object HttpListener extends Netty3Listener[Any, Any](
  "http",
  http.Http().server(ServerCodecConfig("httpserver", new SocketAddress{})).pipelineFactory,
  channelReadTimeout = 5.seconds,
  channelWriteCompletionTimeout =  5.seconds
)

object HttpServer
  extends DefaultServer[HttpRequest, HttpResponse, Any, Any](
    "http", HttpListener,
    {
      val dtab = new DtabFilter[HttpRequest, HttpResponse]
      val tracingFilter = new HttpServerTracingFilter[HttpRequest, HttpResponse]("http")
      (t, s) => new HttpServerDispatcher(
        new HttpTransport(t),
        tracingFilter andThen dtab andThen s
      )
    }
  )
