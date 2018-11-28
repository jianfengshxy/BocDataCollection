package dc.utils

import java.io.FileInputStream
import java.net.{InetSocketAddress, SocketAddress}
import java.security.{KeyStore, SecureRandom}
import javax.net.ssl._

import com.twitter.finagle.http.codec.HttpServerDispatcher
import com.twitter.finagle.http.filter.DtabFilter
import com.twitter.finagle.http.{Response, Request, HttpServerTracingFilter, HttpTransport}
import com.twitter.finagle.netty3.{Netty3Listener, Netty3ListenerTLSConfig}
import com.twitter.finagle.server.DefaultServer
import com.twitter.finagle.ssl.Engine
import com.twitter.finagle.{Service, ServerCodecConfig, http}
import com.twitter.util.{Future, Await}
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}
import com.twitter.conversions.time._
/**
  * Created by pt on 16-1-8.
  */

class HttpsServer[Req <: HttpRequest, Rep <: HttpResponse] extends DefaultServer[Req, Rep, Any, Any] (
  "https", HttpsListener, {
    val dtab = new DtabFilter[Req, Rep]
    val tracingFilter = new HttpServerTracingFilter[Req, Rep]("http")
    (t, s) => new HttpServerDispatcher(
      new HttpTransport(t),
      tracingFilter andThen dtab andThen s
    )
  }
)
//
//object HttpsServer extends HttpsServer[HttpRequest, HttpResponse] {
//  def main(args: Array[String]) {
//    Config.https foreach { https =>
//      val httpsServer = HttpsServer.serve(new InetSocketAddress(https.port), new Service[HttpRequest, HttpResponse] {
//        override def apply(request: HttpRequest): Future[HttpResponse] = {
//          Future.value(HttpUtils.newHttpResponse("Hello World!"))
//        }
//      })
//      println("start server...")
//      Await.result(httpsServer)
//    }
//  }
//}

object HttpsListener extends Netty3Listener[Any, Any](
  "https",
  http.Http().server(ServerCodecConfig("httpserver", new SocketAddress {})).pipelineFactory,
  //tlsConfig = Some(Netty3ListenerTLSConfig(() => Engine(SSLEngineFactory.create()))),
  channelReadTimeout = 100.seconds,
  channelWriteCompletionTimeout = 5.seconds
)

//object SSLEngineFactory {
//  val config = Config.https.getOrElse(throw new IllegalArgumentException("No https config found!"))
//  val context = SSLContext.getInstance("TLS")
//  val keyManagers = createKeyManagers(config.keystorePath, config.keystorePassword, config.keyPassword)
//  val trustManagers = createTrustManagers(config.trustPath, config.trustKeystorePassword)
//  context.init(keyManagers, trustManagers, new SecureRandom())
//
//  def create(): SSLEngine = {
//    context.createSSLEngine()
//  }
//
//  def createKeyManagers(filePath: String, keystorePassword: String, keyPassword: String): Array[KeyManager] = {
//    val keyStore = KeyStore.getInstance("JKS")
//    val keyStoreIS = new FileInputStream(filePath)
//    try {
//      keyStore.load(keyStoreIS, keystorePassword.toCharArray)
//    } finally {
//      if (keyStoreIS != null) {
//        keyStoreIS.close()
//      }
//    }
//    val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
//    kmf.init(keyStore, keyPassword.toCharArray)
//    kmf.getKeyManagers
//  }
//
//  def createTrustManagers(filepath: String, keystorePassword: String): Array[TrustManager] = {
//    val trustStore = KeyStore.getInstance("JKS")
//    val trustStoreIS = new FileInputStream(filepath)
//    try {
//      trustStore.load(trustStoreIS, keystorePassword.toCharArray)
//    } finally {
//      if (trustStoreIS != null) {
//        trustStoreIS.close()
//      }
//    }
//    val trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
//    trustFactory.init(trustStore)
//    trustFactory.getTrustManagers
//  }
//
//}