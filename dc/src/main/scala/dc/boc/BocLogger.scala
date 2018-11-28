package dc.boc


import java.util.Random
import dc.boc.json.GameInfo.GameRequest
import dc.boc.BocContext

import dc.utils.{ServerInfoUtils, Config}
import org.apache.logging.log4j.ThreadContext



object BocLogger extends dc.utils.Logging{
  protected override def logName = "bid.log"
  private val Separator = "\t"
  val NO_BID_DATA = (1, "", "", "", "0", "", "")
  val random = new Random();
  val postLogPre = 100;

  def apply(ctx: BocContext,
            json:GameRequest,
            district:String,
            errorCode: String = "",
            errorInfo: String = ""): String = {
    // no bid
    val dbName = "chinapex"
    val dayhour = ctx.requestTime.requestDayHour
    val logStr = buildLog(ctx,json,district, errorCode, errorInfo)
    log(logStr, dbName, dayhour, isBid=false)
    logStr
  }

  private def buildLog(ctx: BocContext,
                       json:GameRequest,
                       district:String,
                       errorCode: String = "",
                       errorInfo: String = ""): String = {

    val event_time = json.getEvent.getEvent_time
    val cus_type = json.getUser.getCus_type
    val cus_channel = json.getUser.getCus_channel
    val dev_brand = json.getUser.getDev_brand
    val dev_name = json.getUser.getDev_name
    val dev_version = json.getUser.getDev_version
    val open_id = json.getUser.getOpen_id
    val user_tel = json.getUser.getUser_tel
    val user_id = json.getUser.getUser_id
    val event_type = json.getEvent.getEvent_type
    val page_no = json.getEvent.getPage_no
    val widget_no = json.getEvent.getWidget_no

    val object_type = json.getEvent.getObject_type

    val plate_id = json.getEvent.getObject_property.getPlate_id
    val sev_ip_addr = json.getOs.getSev_ip_addr
    val sev_mac_addr = json.getOs.getSev_mac_addr
    val api_version = json.getApi.getApi_version
    val operat_id = json.getEvent.getOperat_id

    val object_val = json.getEvent.getObject_val
    val module_title = json.getEvent.getObject_property.getModule_title
    val gift_type = json.getEvent.getObject_property.getGift_type
    val prod_name = json.getEvent.getObject_property.getProd_name
    val url = json.getEvent.getObject_property.getUrl
    val game_name = json.getEvent.getObject_property.getGame_name
    val skin_id = json.getEvent.getObject_property.getSkin_id
    val cus_id = json.getUser.getCus_id


    val sb = new StringBuilder
     sb.append(event_time)                                          // 1:event time
      .append(Separator).append(cus_type)                         // 2:  //客户类别 1:注册用户 2:游客用户
      .append(Separator).append(cus_channel)                      // 3:  //客户渠道  1:微信渠道 2:APP渠道
      .append(Separator).append(dev_brand)                        // 4: //登录游戏的设备厂商
      .append(Separator).append(dev_name)                         // 5:  //登录游戏的设备名称
      .append(Separator).append(dev_version)                      // 6:  登录游戏的设备版本号
      .append(Separator).append(open_id)                          // 7: 微信用户openid不能缺失
      .append(Separator).append(user_tel)                         //8: 注册登录时手机号码
      .append(Separator).append(user_id)                         //9: 中银客户统一标识
      .append(Separator).append(event_type)                       //10: 事件类型   2：点击  1：加载
      .append(Separator).append(page_no)                         //11: 页面代码
      .append(Separator).append(widget_no)                         //12: 控件代码
      .append(Separator).append(object_type)                       //13: 对象类型  描述该事件操作的对象类型（1：萌宠 2：好友 3：软文 4：产品）
      .append(Separator).append(plate_id)                         //14:象的属性
      .append(Separator).append(sev_ip_addr)                         //15: ip_addr
      .append(Separator).append(sev_mac_addr)                         //16: mac_addr
      .append(Separator).append(api_version)                         //17: 当前版本号
      .append(Separator).append(operat_id)                         //18: 当前版本号
      .append(Separator).append(district)                         //19: 用户所在地区
      .append(Separator).append(object_val)                         //20: 当前版本号
      .append(Separator).append(module_title)                         //21: 当前版本号
      .append(Separator).append(gift_type)                         //22: 当前版本号
      .append(Separator).append(prod_name)                         //23: 当前版本号
      .append(Separator).append(url)                         //24: 当前版本号
      .append(Separator).append(game_name)                         //25: 当前
      .append(Separator).append(skin_id)                         //26: 当前
      .append(Separator).append(cus_id)                         //27: 当前
    sb.toString()
  }


  private def log(logStr: String, dbName : String, dayhour : String, isBid: Boolean): Unit = {
    /*
    if getLocalHost always returns 127.0.0.1
      To fix:
        Find your host name. Type: hostname. For example, you find your hostname is mycomputer.xzy.com
      Put your host name in your hosts file. /etc/hosts . Such as 10.50.16.136 mycomputer.xzy.com
    */
    ThreadContext.put("LocalIP", ServerInfoUtils.getLocalHost)
    ThreadContext.put("DbName", dbName)
    ThreadContext.put("ProductName", "chinapex")
    ThreadContext.put("BidLogExt", "a")
    ThreadContext.put("DayHourStr", dayhour)
    logInfo(logStr)
    ThreadContext.clearAll
  }
}
