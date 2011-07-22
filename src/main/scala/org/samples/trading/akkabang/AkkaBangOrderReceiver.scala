package org.samples.trading.akkabang

import akka.actor._
import akka.dispatch.MessageDispatcher
import org.samples.trading.akka._
import org.samples.trading.domain._
import akka.event.EventHandler

class AkkaBangOrderReceiver(disp: Option[MessageDispatcher])
  extends AkkaOrderReceiver(disp) {

  override def placeOrder(order: Order) = {
    val matchingEngine = matchingEngineForOrderbook.get(order.orderbookSymbol)
    matchingEngine match {
      case Some(m) ⇒
        m ! order
      case None ⇒
        EventHandler.warning(this, "Unknown orderbook: " + order.orderbookSymbol)
    }
  }
}
