package dc.utils

import java.net.{InetAddress, Inet4Address, NetworkInterface}
import org.joda.time.DateTime
import scala.collection.JavaConversions._

/**
 * Created by kevin on 14年8月29日.
 *
 */
object ServerInfoUtils {
  private val ips = NetworkInterface.getNetworkInterfaces().filter(net => !net.getName.startsWith("lo")).map(
    net => net.getInetAddresses.filter(ip => ip.isInstanceOf[Inet4Address] && ip.getHostAddress != "127.0.0.1")
  ).flatten.toList
  private val pubips = ips.filter(ip => isPublicIp(ip))
  private val localips = ips.filter(ip => !isPublicIp(ip))

  private def isPublicIp(ip : InetAddress) = {
    if (ip.getAddress.length == 4) {
      val public = List(0x0A, 0x7F, 0xA9, 0xAC, 0xC0).map(bt => bt.toByte)
      !public.contains(ip.getAddress.apply(0))
    } else
      true
  }

  /*
  * get Local Host's IP
   */
  lazy val getLocalHost: String = {
    try{
      val address = java.net.InetAddress.getLocalHost
      val hostip = address.getHostAddress
      if (hostip != "127.0.0.1") hostip
      else {
        if (localips.nonEmpty) localips.head.getHostAddress
        else if (pubips.nonEmpty) pubips.head.getHostAddress
        else "Unknown"
      }
    } catch {
      case e:java.net.UnknownHostException => "Unknown"
    }
  }
  /*
  * get Thread id
   */
  def getThreadId: Long = {
    Thread.currentThread().getId
  }
  /*
  * get Process id, if failure  return -1
   */
  lazy val getProcessId: Long = {
    val process = java.lang.management.ManagementFactory.getRuntimeMXBean.getName
    try {
      process.substring(0,process.indexOf("@")).toLong
    }catch {
      case e: Exception => -1
    }
  }

  /**
   *  get the currentTime
   */
  def getCurrentTime = DateTime.now(Config.timezone)
}
