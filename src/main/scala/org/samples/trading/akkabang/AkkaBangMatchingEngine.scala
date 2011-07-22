package org.samples.trading.akkabang

import akka.actor._
import akka.dispatch.MessageDispatcher
import org.samples.trading.akka._
import org.samples.trading.domain.Order
import org.samples.trading.domain.Orderbook
import akka.event.EventHandler

class AkkaBangMatchingEngine(meId: String, orderbooks: List[Orderbook], disp: Option[MessageDispatcher])
  extends AkkaMatchingEngine(meId, orderbooks, disp) {

  override def handleOrder(order: Order) {
    orderbooksMap.get(order.orderbookSymbol) match {
      case Some(orderbook) ⇒
        // println(meId + " " + order)

        standby.foreach(_ ! order)

        txLog.storeTx(order)
        orderbook.addOrder(order)
        orderbook.matchOrders()

      case None ⇒
        EventHandler.warning(this, "Orderbook not handled by this MatchingEngine: " + order.orderbookSymbol)
    }
  }

}
