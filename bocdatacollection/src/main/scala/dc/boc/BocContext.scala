package de.boc

import com.twitter.util.Stopwatch
import dc.boc.BOCParam
import dc.utils.ServerInfoUtils
import dc.cache._
import dc.utils.{ServerInfoUtils}
import org.joda.time.DateTime
import java.net.URLDecoder
import scala.collection.mutable.StringBuilder
import scala.collection.mutable._

/**
 * Created by pt on 12/8/14.
 */
case class BocContext(bidParam: BOCParam,request: String) {
  val requestTime = RequestTime(ServerInfoUtils.getCurrentTime)

}

case class RequestTime(time: DateTime){
  val getMillis = time.getMillis
  val timeSeconds = time.getMillis / 1000
  val dayOfWeek = if (time.getDayOfWeek == 7 ) 0 else time.getDayOfWeek
  val dayOfYear = time.getDayOfYear
  val daySecond = time.getSecondOfDay

  lazy val requestDayHour = time.toString("yyyy-MM-dd.HH")
  lazy val elapsedTime: Int = (System.currentTimeMillis() - getMillis).toInt
  val elapsed = Stopwatch.start()
}
