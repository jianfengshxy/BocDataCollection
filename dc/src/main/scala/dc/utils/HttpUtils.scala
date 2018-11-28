package dc.utils

import java.io.File
import java.util.concurrent.atomic.{AtomicReference, AtomicLong}
import com.twitter.util.Future
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http.HttpVersion._
import org.jboss.netty.handler.codec.http._
import scala.collection.mutable.Map
import scala.io.Source
import com.twitter.conversions.time._
/**
 * Created by pt on 14-12-18.
 */
object HttpUtils extends Logging {
  def newHttpResponse(
                       body: Array[Byte],
                       statusCode: HttpResponseStatus = HttpResponseStatus.OK,
                       exid: Int = 0
                       ): DefaultHttpResponse = {
    val response = new DefaultHttpResponse(HTTP_1_1, statusCode)
    response.setContent(ChannelBuffers.wrappedBuffer(body))
      response.headers.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE)
      response.headers.set("Keep-Alive", "timeout=" + 5.seconds + ", max=-1")
      response.headers()
      .set(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
      .set(HttpHeaders.Names.ACCEPT,"*")
      .set(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
      .set(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_HEADERS,"Origin, X-Requested-With, Content-Type, Accept")
      .set(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_METHODS,"GET, POST, PUT,DELETE")
      .set(HttpHeaders.Names.EXPIRES, "-1")
      .set(HttpHeaders.Names.SERVER, System.getProperty("java.vm.name"))
      .set(HttpHeaders.Names.CONTENT_LENGTH, response.getContent.readableBytes())
    response
  }

  def newHttpResponse(body: String): DefaultHttpResponse = {
    newHttpResponse(body.getBytes)
  }

  def mkHtml(body: String, statusCode: HttpResponseStatus = HttpResponseStatus.OK): DefaultHttpResponse = {
    val content =
      s"""
       <!DOCTYPE html>
      <html>
      <body style="width:90%">
      $body
      </body>
      </html>
       """.stripMargin
    newHttpResponse(content.getBytes, statusCode)
  }

  def winHttpResponse(statusCode: HttpResponseStatus = HttpResponseStatus.OK) = {
    val response = new DefaultHttpResponse(HTTP_1_1, statusCode)
    response.headers()
      .set(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
      .set("P3P","CP=\"OTI PSA OUR\"")
      .set(HttpHeaders.Names.EXPIRES, "-1")
      .set(HttpHeaders.Names.SERVER, System.getProperty("java.vm.name"))
      .set(HttpHeaders.Names.CONTENT_LENGTH, 0)
      .set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE)
    if (statusCode != HttpResponseStatus.FOUND)
      response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html")
    response

  }

  private val fileCacheMap : Map[String, FileCache] = Map()
  private val contentType = Map(
    "xml" -> "text/xml",
    "html" -> "text/html",
    "htm" -> "text/html",
    "txt" -> "text/plain"
  )

  case class FileCache(file: File) {
    private val bytes = new AtomicReference(Source.fromFile(file).mkString.getBytes())
    private val lastUpdate = new AtomicLong(ServerInfoUtils.getCurrentTime.getMillis)

    val contentTYPE = contentType.getOrElse(getFileExtension(file), "text/plain")

    private def getFileExtension(file: File): String = {
      val fileName = file.getName()
      if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase;
      } else {
        return "";
      }
    }

    def getBytes() : Array[Byte] = {
      val nowMillis = ServerInfoUtils.getCurrentTime.getMillis
      if (nowMillis - lastUpdate.get() > 60*1000L) {
        lastUpdate.set(nowMillis)
        //read future
        Future.apply({
          val tmp = Source.fromFile(file).mkString.getBytes()
          bytes.set(tmp)
        })
      }
      return bytes.get()
    }
  }

  private val rootPath = (new File(Config.dirRead)).getCanonicalPath
  lazy val indexFile = {
    val file1 = new File(rootPath + "/index.htm")
    val file2 = new File(rootPath + "/index.html")
    if (file1.exists() && file1.isFile && file1.canRead)
      Some(file1)
    else if (file2.exists() && file2.isFile && file2.canRead)
      Some(file2)
    else
      None
  }

  def fileReadResponse(request: HttpRequest, resp: HttpResponse) = {
    val rHand = request.getUri.split("\\?", 2)(0)
    val _hand_ = if (rHand.startsWith("/")) rHand.substring(1) else rHand

    val file =  if (_hand_.isEmpty)
      indexFile match {
        case Some(idxfile) => idxfile
        case None => new File(rootPath + "/")
      }
    else
      new File(rootPath + "/" + _hand_)

    logDebug(s" file : ${file.getCanonicalPath} exist[ ${file.exists()} ] read[ ${file.canRead()} ] dirReadFlag[ ${file.getCanonicalFile.getParent.startsWith(rootPath)} ] ")
    if (file.exists() && file.isFile && file.canRead && file.getCanonicalFile.getParent.startsWith(rootPath)) {
      val fileRealName = file.getCanonicalPath
      val fileContent = fileCacheMap.get(fileRealName) match {
        case Some(filecache) => filecache
        case None =>
          val filecache = FileCache(file)
          fileCacheMap.put(fileRealName, filecache)
          filecache
      }
      val bufferBytes = fileContent.getBytes()

      resp.setContent(ChannelBuffers.wrappedBuffer(bufferBytes))
      resp.setStatus(HttpResponseStatus.OK)
      resp.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE)
        .set(HttpHeaders.Names.CONTENT_TYPE, fileContent.contentTYPE)
        .set(HttpHeaders.Names.CONTENT_LENGTH, bufferBytes.length)
    } else {
      resp.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE)
      resp.headers().remove(HttpHeaders.Names.CONTENT_LENGTH)
    }
  }
}
