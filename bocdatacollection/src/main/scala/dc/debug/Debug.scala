package dc.debug

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import com.twitter.app.App
import com.twitter.finagle.Service
import com.twitter.finagle.http.{HttpMuxer}
import com.twitter.util.Future
import dc.boc.json.GetStatics.GetStatics
import dc.boc.BOCService
import dc.utils.{HttpUtils}
import org.jboss.netty.handler.codec.http.{HttpResponseStatus, HttpMethod, HttpRequest, HttpResponse}

/**
  * Created by pt on 14-12-18.
  */
trait Debug {
  self: App =>

  DebugHandler.handlers.foreach(handler => {
    HttpMuxer.addHandler(handler.url, handler)
  })

}

trait DebugHandler extends Service[HttpRequest, HttpResponse] {
  def url: String

  def clickable: Boolean = false
}

object DebugHandler extends DebugHandler {
  val handlers = Seq[DebugHandler](this)
  lazy val handlerSeq = handlers.map(hd => (hd.url,hd))

  override val url = "/stats"
  override val clickable = true

  override def apply(request: HttpRequest): Future[HttpResponse] = {

    val getStatics = new GetStatics()
    getStatics.setTotal(getNumber("pudong") +
      getNumber("changning") +
      getNumber("huangpu") +
      getNumber("luwan") +
      getNumber("xuhui") +
      getNumber("jingan") +
      getNumber("putuo") +
      getNumber("hongkou") +
      getNumber("yangpu") +
      getNumber("minhang") +
      getNumber("baoshan") +
      getNumber("jiading") +
      getNumber("jinshan") +
      getNumber("songjiang") +
      getNumber("qingpu") +
      getNumber("nanhui") +
      getNumber("fengxian") +
      getNumber("chongming") +
      getNumber("other")
    )
    getStatics.setPudong(getNumber("pudong"))
    getStatics.setChangning(getNumber("changning"))
    getStatics.setHuangpu(getNumber("huangpu"))
    getStatics.setLuwan(getNumber("luwan"))
    getStatics.setXuhui(getNumber("xuhui"))
    getStatics.setJingan(getNumber("jingan"))
    getStatics.setPutuo(getNumber("putuo"))
    getStatics.setHongkou(getNumber("hongkou"))
    getStatics.setYangpu(getNumber("yangpu"))
    getStatics.setMinhang(getNumber("minhang"))
    getStatics.setBaoshan(getNumber("baoshan"))
    getStatics.setJiading(getNumber("jiading"))
    getStatics.setJinshan(getNumber("jinshan"))
    getStatics.setSongjiang(getNumber("songjiang"))
    getStatics.setQingpu(getNumber("qingpu"))
    getStatics.setNanhui(getNumber("nanhui"))
    getStatics.setFengxian(getNumber("fengxian"))
    getStatics.setChongming(getNumber("chongming"))
    getStatics.setOther(getNumber("other"))
    val response = HttpUtils.newHttpResponse(JSON.toJSONBytes(getStatics, SerializerFeature.WriteTabAsSpecial), HttpResponseStatus.OK, 1)
    Future.value(response)
  }

  def getNumber(district:String): Long=  {
    BOCService.customerMap.count( f => {
      val  tmp = (f._2).split(",")
      val district_tmp = tmp(1)
      district.equals(district_tmp)
    }).toLong
  }
}

