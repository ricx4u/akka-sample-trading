package org.samples.trading.basic

import org.samples.trading.common.OrderReceiver
import org.samples.trading.domain.Order
import org.samples.trading.domain.Rsp
import org.samples.trading.domain.Orderbook
import org.samples.trading.common.MatchingEngineRouting

class BasicOrderReceiver() extends OrderReceiver {
  type ME = BasicMatchingEngine

  def placeOrder(order: Order): Rsp = {
    matchingEngineForOrderbook.get(order.orderbookSymbol) match {
      case Some(matchingEngine) ⇒
        matchingEngine.matchOrder(order)
      case None ⇒
        throw new IllegalArgumentException("Unknown orderbook: " + order.orderbookSymbol)
    }
  }

  def updateRouting(routing: MatchingEngineRouting[BasicMatchingEngine]) {
    refreshMatchingEnginePartitions(routing)
  }

}
