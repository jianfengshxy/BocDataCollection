package dc.boc

import dc.boc.BOCLevel.BOCLevel

object BOCLevel extends Enumeration {
  type BOCLevel = Value
  val Error, Warning, Info, Fatal,  Normal = Value
}


case class BOCException(message: String, level: BOCLevel = BOCLevel.Warning) extends RuntimeException(message)

object BOCException {

  val BOC_REQUEST_JSON_DECODE_ERROR = BOCException("boc json syntax error")

}
