package dc.boc

import java.net.URLDecoder
import com.twitter.finagle.http.Request
import dc.utils.{Logging, Config}
import org.jboss.netty.handler.codec.http.{HttpHeaders, HttpRequest}

case class BOCParam(requestUrl: String = "",
                      channelId: String = "",
                      dbName: String = "",
                      exchangeId: Int,
                      postDataType: Int,
                      extParamEncode : String = "",
                      isDebug: Boolean = false) {
  val extParam : String = URLDecoder.decode(extParamEncode, "UTF-8")
}

object BOCParam extends Logging{

  def apply(request: HttpRequest): BOCParam = {

    val paramMaps = Request(request.getUri).params
//    logDebug("Request Params: start... " + "http://" + request.headers.get(HttpHeaders.Names.HOST) + request.getUri)
    val params = BOCParam(
      requestUrl = "http://" + request.headers.get(HttpHeaders.Names.HOST) + request.getUri,
      channelId = paramMaps.getOrElse("c", ""),
      dbName = paramMaps.getOrElse("d", Config.dbname),
      exchangeId = paramMaps.getIntOrElse("exid", 1),
      postDataType = paramMaps.getIntOrElse("pdt", 0),
      extParamEncode = paramMaps.getOrElse("extinfo", ""),
      isDebug = request.headers.get("IS_DEBUG") == "1"
    )
//    logDebug("Request Params: OK!")
    params
  }
}
