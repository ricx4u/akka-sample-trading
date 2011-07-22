package org.samples.trading.actorbang

import org.samples.trading.actor._
import org.samples.trading.domain.Order
import org.samples.trading.domain.Rsp

class ActorBangOrderReceiver
  extends ActorOrderReceiver {

  override protected def placeOrder(order: Order) = {
    val matchingEngine = matchingEngineForOrderbook.get(order.orderbookSymbol)
    matchingEngine match {
      case Some(m) ⇒
        // println("receiver " + order)
        m.forward(order)
      case None ⇒
        println("Unknown orderbook: " + order.orderbookSymbol)
        reply(new Rsp(false))
    }
  }

}
