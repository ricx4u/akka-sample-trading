package org.samples.trading.actor

import org.samples.trading.common.OrderReceiver
import scala.actors._
import org.samples.trading.domain.Order
import org.samples.trading.domain.Orderbook
import org.samples.trading.domain.SupportedOrderbooksReq
import org.samples.trading.domain.Rsp
import org.samples.trading.common.MatchingEngineRouting

class ActorOrderReceiver
  extends Actor with OrderReceiver {
  type ME = ActorMatchingEngine

  //  override def scheduler = new SchedulerAdapter {
  //      def execute(block: => Unit) =
  //        threadPool.execute(new Runnable {
  //          def run() { block }
  //        })
  //    }

  def act() {
    loop {
      react {
        case routing@MatchingEngineRouting(mapping) ⇒
          refreshMatchingEnginePartitions(routing.asInstanceOf[MatchingEngineRouting[ActorMatchingEngine]])
        case order: Order ⇒ placeOrder(order)
        case "exit"       ⇒ exit
        case unknown      ⇒ println("Received unknown message: " + unknown)
      }
    }
  }

  protected def placeOrder(order: Order) = {
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
