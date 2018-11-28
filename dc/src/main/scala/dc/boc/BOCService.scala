package dc.boc

import com.twitter.conversions.time._
import com.twitter.finagle.Service
import com.twitter.finagle.util.DefaultTimer
import com.twitter.util.{Await, Future, Stopwatch}
import dc.boc.json.GameInfo.GameRequest
import dc.boc.json.QueryLocation.QueryLocation
import dc.utils.GetLocation
import dc.DataCollectionServer

import dc.cache.{handleBocData}
import com.alibaba.fastjson.{JSON, JSONException}


import dc.utils._
import dc.boc.BocContext
import org.jboss.netty.buffer.CompositeChannelBuffer
import org.jboss.netty.handler.codec.base64.{Base64, Base64Dialect}
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse, HttpResponseStatus}
import scala.collection.Seq

import  org.apache.commons.codec.binary.{Base64 => apacheBase64}


object BOCService extends Service[HttpRequest, HttpResponse] with Logging {
   val bidderReceiver = DataCollectionServer.statsReceiver.scope("chinapex")
//   val currentUser = DCServer.statsReceiver.scope("")
//
   var customerMap:Map[String,String] = Map()


  def apply(httpRequest: HttpRequest): Future[HttpResponse] = {
    val elapsed = Stopwatch.start()
    logDebug("BOCService ... ")
    val bidParam = BOCParam(httpRequest)

//    logDebug("Get Exchange OK ! ")

    val body = httpRequest.getContent
    val length = body.readableBytes
    val contentEncoding = httpRequest.getHeaders("Content-Encoding")
    val contentLength = httpRequest.getHeaders("Content-Length")
    val request = {
      //chanelbuff:BigEndiannHeapChannelBuffer
      if (body.hasArray) {
          new String(body.array(), body.arrayOffset(), length, "UTF-8")
      }else{
        val compositechanelbuf :CompositeChannelBuffer = body.asInstanceOf[CompositeChannelBuffer]
        val bufferList = compositechanelbuf.decompose(0,compositechanelbuf.capacity())
        var compressbytes: Array[scala.Byte] = new Array[scala.Byte](body.capacity())

        var content = new String()
//        bufferList.foreach(
//          subBuffer => {
//            content += new String(subBuffer.array(), subBuffer.arrayOffset(), subBuffer.capacity(), "UTF-8")
//          }
//        )
        content
      }
    }
    try {

      if(DataCollectionServer.eventQueue.size() <  200000){

        val ctx = BocContext(bidParam, request)
        val json = try {
          JSON.parseObject(request, classOf[GameRequest])
        } catch {
          case ex : JSONException => throw BOCException.BOC_REQUEST_JSON_DECODE_ERROR
        }
        DataCollectionServer.eventQueue.offer(handleBocData(json,ctx))
      }else{
         logDebug("the System overload!!")
      }
      val statusCode =  HttpResponseStatus.OK
      val resp = HttpUtils.newHttpResponse("ok".getBytes, statusCode)
      return Future.value(resp)
    } catch {
      case ex: Throwable =>

        val statusCode =  HttpResponseStatus.OK
        val resp = HttpUtils.newHttpResponse("oops".getBytes, statusCode)
        return Future.value(resp)
    }
  }


  def process_data(ctx :BocContext,json :GameRequest)  = {

    val add = if(json.getEvent.getLatitude != null & json.getEvent.getLongitude != null )
                      GetLocation.getAdd(json.getEvent.getLatitude,json.getEvent.getLongitude,Config.proxyenable,Config.proxyserver,Config.proxyport)
              else
                   "other"
//    logDebug(s"address:${add}")
    val district = if(add.equals("other"))
                        "other"
                   else
                      getDistrict(add)
//    logDebug(s"district:${district}")
    val status = getStatus(json)
     updateCounter(district,status)
    insertData(district,status,json)

    BocLogger(ctx,json,district)
  }

  def insertData(district:String,status:String,json :GameRequest) ={

    if(customerMap.size < 200000){

      if( customerMap.contains( json.getEvent.getOperat_id)) {

       if(status.equals("offline")){
           customerMap = customerMap.-(json.getEvent.getOperat_id)
       }else{
         logDebug(s"update ${json.getEvent.getOperat_id} time ${json.getEvent.getEvent_time}")
         customerMap.-(json.getUser.getUser_id)
         customerMap = customerMap ++ Map (json.getEvent.getOperat_id -> (json.getEvent.getEvent_time + "," + district))
       }
     }else
      {
//        DataCollectionServer.total = DataCollectionServer.total + 1
        if(status.equals("offline")){
          logDebug(s"the offline user operator ${json.getEvent.getOperat_id} don't exsit!")
        }else{
          logDebug(s"insert ${json.getEvent.getOperat_id} time ${json.getEvent.getEvent_time}")
          customerMap = customerMap ++ Map (json.getEvent.getOperat_id -> (json.getEvent.getEvent_time + "," + district))
        }

      }
    }
  }

   def cleanData() = {
     logDebug("beofore clean customer data")
     logDebug(s"customerMap size:${customerMap.size}")
     customerMap.foreach(f => logDebug(s"data:${f._1}"))
     customerMap.foreach( f => {
       val  tmp = (f._2).split(",")
//       logDebug(s"tmp :${tmp}")
       val time = tmp(0)
//       logDebug(s"action time :${time}")
       val district = tmp(1)
//       logDebug(s"district :${district}")
       val  userid  = f._1
       val currentTime = System.currentTimeMillis()
//       logDebug(s"currentTime :${currentTime}")
       val sub = currentTime - time.toLong
       logDebug(s"sub :${sub}")
      if( sub > 300000) {
        customerMap = customerMap.-(f._1)
        logDebug("After clean customer data")
        logDebug(s"customerMap size:${customerMap.size}")
//        customerMap.foreach(f => logDebug(s"data:${f._1}"))
      }
     }
     )
  }

  def  updateCounter(district:String,status:String) {

    bidderReceiver.counter(district+ "/" + status).incr()
    bidderReceiver.counter("total" + status).incr()

  }

  def getStatus(json :GameRequest) :String ={

    val widget_no = json.getEvent.getWidget_no

    if(widget_no.equals("visitor") || widget_no.equals("player") || widget_no.equals("app_start") || widget_no.equals("wx_init") ){

      return "online"
    }

    if(widget_no.equals("leave_app") ){
      return "offline"

    }
    widget_no
  }

  def  getDistrict (add :String ) :String = {
    val json = try {
      JSON.parseObject(add, classOf[QueryLocation])
    } catch {
      case ex : JSONException => throw BOCException.BOC_REQUEST_JSON_DECODE_ERROR
    }
   val admName =  json.getAddrList.get(0).getAdmName

    if(admName.split(",").length == 3){
      admName.split(",")(2) match {
        case "浦东新区"   => "pudong";
        case "长宁区"   =>  "changning";
        case "黄浦区"   => "huangpu";
        case "卢湾区"   => "luwan";
        case "徐汇区"   => "xuhui";
        case "静安区"   => "jingan";
        case "普陀区"   => "putuo";
        case "虹口区"   => "hongkou";
        case "杨浦区"   => "yangpu";
        case "闵行区"   => "minhang";
        case "宝山区"   => "baoshan";
        case "嘉定区"   => "jiading";
        case "金山区"   =>  "jinshan";
        case "松江区"   => "songjiang";
        case "青浦区"   => "qingpu";
        case "南汇区"   => "nanhui";
        case "奉贤区"   => "fengxian";
        case "崇明县"   => "chongming";
        case  _           =>  "other";

      }
    }else
      "other"
  }

}
