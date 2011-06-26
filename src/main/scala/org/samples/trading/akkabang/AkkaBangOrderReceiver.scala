package org.samples.trading.akkabang

import akka.actor._
import akka.dispatch.MessageDispatcher

import org.samples.trading.akka._
import org.samples.trading.domain._

class AkkaBangOrderReceiver(matchingEngines: List[ActorRef], disp: Option[MessageDispatcher])
  extends AkkaOrderReceiver(matchingEngines, disp) {

  override def placeOrder(order: Order) = {
    if (matchingEnginePartitionsIsStale) refreshMatchingEnginePartitions()
    val matchingEngine = matchingEngineForOrderbook.get(order.orderbookSymbol)
    matchingEngine match {
      case Some(m) =>
        // println("receiver " + order)
        m ! order
      case None =>
        println("Unknown orderbook: " + order.orderbookSymbol)
    }
  }
}
