package dc.utils

import java.lang.Long
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

import com.twitter.conversions.time._
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTimeZone

/**
 * Created by pt on 14-11-18.
 */
object Config extends Logging {
  val timezone = DateTimeZone.forID("Asia/Shanghai")

  val conf = ConfigFactory.load().getConfig("de")
  val productName = "datacollection"
  val dbname = "ifc"
  val serverPort = conf.getInt("port")
  val proxyenable = conf.getBoolean("proxyenable")
  val proxyserver = conf.getString("proxyserver")
  val proxyport  = conf.getInt("proxyport")

  //val http_server = conf.getString("http_server")
  val http_server = ""
//  val http_server_port = conf.getInt("http_server_port")
//  val message = ""
//  val bidLogExt = conf.getString("log.ext.bid")
//  val noBidLogExt = conf.getString("log.ext.nobid")
//  val debugHand = conf.getString("debugHand")
//  val DSCallTimeoutInt = conf.getInt("service.dataserver.timeout")
//  val DSCallTimeout = DSCallTimeoutInt.milliseconds
//  val RSCallTimeout = conf.getInt("service.rankingserver.timeout").milliseconds
//  val BidOverloadNoBidTimeout = conf.getInt("bidoverload.timeout.nobid").milliseconds
//  val BidOverloadErrorTimeout = conf.getInt("bidoverload.timeout.error").milliseconds
//  val bidOverloadErrorTimeoutLong = BidOverloadErrorTimeout + 50.milliseconds
//  val WinOverloadErrorTimeout = conf.getInt("win.timeout").seconds
//  val MonitorLoggerAppName = conf.getString("monitor_app")
//  val MonitorAliveTime = conf.getInt("monitor_time").seconds
//  val zookeeperConnect = service.zookeeperUrl
//  val cookieName = conf.getString("cookieName")
//  val cookieDomain = conf.getString("cookieDomain")
//  val maxConnection = if (conf.hasPath("max_connection")) conf.getInt("max_connection") else 8000

//  private val _dirRead = conf.getString("dir_read")
   private val _dirRead = ""
   val dirRead = if (_dirRead.endsWith("/")) _dirRead.substring(0, _dirRead.length-1) else _dirRead

//  val baiduBiddingFlag = conf.getBoolean("baidu_bidding_flag")
//  val cpcSpendingFlag = if (conf.hasPath("cpc_spending_flag")) conf.getBoolean("cpc_spending_flag") else false
  val cpcSpendingFlag = false

//  object framework {
//    private val confframe = conf.getConfig("framework")
//    val log4j1file = confframe.getString("log4j1file")
//  }

//  object inmobi {
//    private val inmobibase = conf.getConfig("inmobi")
//    val seat = inmobibase.getString("advertiserId")
//  }
//
//  object oppo {
//    private val oppobase = conf.getConfig("oppo")
//    val seat = oppobase.getString("advertiserId")
//  }
//
//  object huawei {
//    private val huaweibase = conf.getConfig("huawei")
//    val seat = huaweibase.getString("advertiserId")
//  }
//
//  object debug {
//    val base = conf.getConfig("debug")
//    val isDebug = base.getBoolean("enable")
//    val mockDS = isDebug && base.getBoolean("mockDS")
//    val mockRS = isDebug && base.getBoolean("mockRS")
//    val mockDC = isDebug && base.getBoolean("mockDC")
//    val keepAliveHttpConnection = isDebug && base.getBoolean("keepAliveHttpConnection")
//    val keepAliveTimeOut = base.getInt("keepAliveTimeOut")
//    val keepAliveTimeOutDuration = keepAliveTimeOut.seconds
//    val idleLowFilter = base.getInt("idle_low")
//    val idleHighFilter = base.getInt("idle_high")
//    val enableAutoCleanIdleConn = if (base.hasPath("enable_auto_clean_idle_conn"))
//                                    base.getBoolean("enable_auto_clean_idle_conn")
//                                  else false
//    val autoCleanIdleConnInterval = if (base.hasPath("auto_clean_idle_conn_interval"))
//                                    base.getInt("auto_clean_idle_conn_interval").minutes
//                                  else 5.minutes
//    val channelReadTimeout = if(base.hasPath("channel_read_timeout"))
//                                base.getInt("channel_read_timeout").seconds
//                              else
//                                5.seconds
//    val channelWriteCompletionTimeout = if(base.hasPath("channel_write_timeout"))
//                                          base.getInt("channel_write_timeout").seconds
//                                        else
//                                          5.seconds
//
//    val resetGroupId = sys.props.get("newGroupId") match {
//      case Some(x) => true
//      case None => false
//    }
//    val newConsumerOffset = sys.props.get("newConsumerOffset") match {
//      case Some(offset) => try{
//          (new Long(offset)).toLong
//        } catch {
//          case e: Exception =>
//            logDebug("the args newConsumerOffset can not parse long")
//            -1L
//        }
//      case None => -1L //doNothing
//    }
//
//    val postDataPrecent = {
//      val p = if (base.hasPath("post_data_precent")) base.getInt("post_data_precent") else 100
//      if (p > 100) 100 else if (p < 0) 0 else p
//    }
//    var dcIsNewFlag = base.getBoolean("dcIsNew")
//    val debugMainFlag = if (base.hasPath("mainFlag")) base.getBoolean("mainFlag") else false
//  }
//
//  object google {
//    val base = conf.getConfig("google")
//    val enableFilter = base.getBoolean("enable_filter")
//    val allowedVendorType = base.getInt("allow_vendor_type")
//    val ipfile = base.getString("ipfile")
//
//  }
//
//  object gdt {
//    val base = conf.getConfig("gdt")
//    val ipfile = base.getString("ipfile")
//  }

//  object cache {
//    private val base = conf.getConfig("cache")
//    val kafka = base.getConfig("kafka")
//    val topic = base.getString("topic")
//
//
//    val kafkaHost = base.getString("kafka_host").split(",")
//    val kafkaPort = base.getInt("kafka_port")
//    val reloadDuration = base.getInt("reload_time").seconds
//    val zookeeperUrl = base.getString("kafka.zookeeper.connect")
//    val sqliteFile: String = base.getString("sqlite.file")
//    val product: String = base.getString("product")
//  }
//
//  object retargetingkey {
//    private val base = conf.getConfig("retargetingKey")
//    val file = base.getString("file")
//    val reload = base.getInt("reload")
//  }
//
//  object cookieQuery {
//    private val base = conf.getConfig("cookieQuery")
//    val file = base.getString("file")
//    val reload = base.getInt("reload")
//  }
//
//  object region {
//    private val base = conf.getConfig("region.ipfile")
//    val IPFile = base.getString("thirdpart")
//  }

  object rs {
//    private val base = conf.getConfig("service.rankingserver")
//    val path = base.getString("path").format(zookeeperConnect)
  }

  object devicespath{
    private val base = conf.getConfig("service.devicespath")
    val path = base.getString("path")
    val timeout = base.getInt("timeout")
  }

  object macroConf {
    private val base = conf.getConfig("macro")
    val isEncodePrice = base.getBoolean("encodePrice")
    val ADFHOST = base.getString("ADFHOST")
    val ADFHOST_SSL = base.getString("ADFHOST_SSL")
    val ADFCLICKOBJ = base.getString("ADFCLICKOBJ")
    val WINNOTICE_HOST = base.getString("WINNOTICE_HOST")
    val WINNOTICE_HOST_SSL = if (base.hasPath("WINNOTICE_HOST_SSL"))
        base.getString("WINNOTICE_HOST_SSL")
      else
        base.getString("WINNOTICE_HOST").replace("http", "https")
//    val CLICK_HOST = base.getString("CLICK_HOST")
    val AdPriceCurrency = base.getString("AdPriceCurrency")
    val CURL = base.getString("CURL")

    def getProtocol(secure : Boolean) : String = {
      if (secure) WINNOTICE_HOST_SSL else WINNOTICE_HOST
    }

    def getAdfHost(secure : Boolean) : String = {
      if (secure) ADFHOST_SSL else ADFHOST
    }
  }

  object service {
//    private val base = conf.getConfig("service")
//    val zookeeperUrl = base.getString("zookeeper.connect")
//    val DSBatchSize = base.getInt("dataserver.batch_size")
//    val DSCallTimeout = base.getInt("dataserver.timeout").milliseconds
//    val DSZKHost = base.getString("dataserver.zookeeper.host")
//    val DSKeyProductName = base.getString("dataserver.key.productName")
  }

  object sina {
    private val base = conf.getConfig("sina")
    val sianclickEncrypKey = base.getString("click_encryp_key")
    val sinaCurlEscSwitch = base.getBoolean("CURL_SINA_ESC_SWITCH")
    val sinaUnencCurl = base.getString("CURL_SINA_UNESC")
    val sinaEncCurl = base.getString("CURL_SINA_ESC")
  }

  object solution{
    private val base = conf.getConfig("solution")
    val cpcToCpmRatio = base.getInt("cpcToCpmRatio")
    val maxRetargetingNum = base.getInt("maxRetargetingNum")
    val maxNoRetargetingNum = base.getInt("maxNoRetargetingNum")
    val maxCandidateSize = maxRetargetingNum + maxNoRetargetingNum
  }

//  object dcclient {
//    private val base = conf.getConfig("dcclient")
//    val dcDbName      = base.getString("dbName")
//    val limitPercent  = base.getDouble("limitPercent") //quota消耗上限
//    val sqlitePath    = base.getString("sqlitePath")
//    val ignoreFlag    = debug.mockDC
//    val objDataFields = List("action", "bidden", "biddenCount", "impression", "click",
//      "quota", "spending", "totalBiddenCount", "totalimpression", "totalclick", "totalspending")
//    val socketDebugShow    = if (base.hasPath("socketDebugShow")) base.getBoolean("socketDebugShow") else false
//    var dcThriftServers = base.getString("dcThriftServers").split(",")
//
//    val controlSpending = 900 * 1000000
//  }

//  object dsclient{
//    private val base = conf.getConfig("dsclient")
//    val asyncThreadPoolSize = Runtime.getRuntime.availableProcessors() * 2
//    val asyncQueueSize = base.getInt("asyncQueueSize")
//  }
//
//  object win{
//    private val notice = conf.getConfig("win.notice")
//    val dePriceKey = notice.getConfig("decryptPrice.key")
//    private val click = conf.getConfig("win.click")
//    val clickErrUrl = click.getString("errorUrl")
//
//    val GEO_PATTERN = """^[A-Za-z0-9]{0,10}$"""
//  }
//
//  val https = if(conf.hasPath("https")){
//    val base = conf.getConfig("https")
//    Some(HttpsConfig(
//      port = base.getInt("port"),
//      keystorePath = base.getString("keystorePath"),
//      keystorePassword = base.getString("keystorePassword"),
//      keyPassword = base.getString("keyPassword"),
//      trustPath = base.getString("trustPath"),
//      trustKeystorePassword = base.getString("trustKeystorePassword")
//    ))
//  } else None
//
//  case class HttpsConfig(port: Int, keystorePath: String,
//                         keystorePassword: String, keyPassword: String,
//                         trustPath: String, trustKeystorePassword: String)
}
