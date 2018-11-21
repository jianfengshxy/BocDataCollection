
package dc.boc

import dc.boc.NoBidLevel.NoBidLevel

object NoBidLevel extends Enumeration {
  type NoBidLevel = Value
  val Error, Warning, Info, Fatal,  Normal = Value
}


case class BOCException(message: String, level: NoBidLevel = NoBidLevel.Warning) extends RuntimeException(message)

object BOCException {

  val BOC_REQUEST_JSON_DECODE_ERROR = BOCException("boc json syntax error")

}
