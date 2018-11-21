package dc.cache


import dc.boc.json.GameInfo.GameRequest
import de.boc.BocContext


/**
 * Created by pt on 15-2-26.
 */
trait BOCEvent {  }


case class handleBocData(json: GameRequest,ctx :BocContext) extends BOCEvent

case class cleanCustomerData(minEndTimeSec: Long) extends BOCEvent