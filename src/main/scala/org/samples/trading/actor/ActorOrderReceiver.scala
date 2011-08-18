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

  var orderCount = 0L
  // possibility to yield, due to starvation in some jvm/os
  val yieldCount = System.getProperty("benchmark.yieldCount", "0").toInt;

  def act() {
    loop {
      react {
        case routing@MatchingEngineRouting(mapping) ⇒
          refreshMatchingEnginePartitions(routing.asInstanceOf[MatchingEngineRouting[ActorMatchingEngine]])
        case order: Order ⇒
          placeOrder(order)
          if (yieldCount > 0 && orderCount % yieldCount == 0) {
            Thread.`yield`()
          }
          orderCount += 1
        case "exit"  ⇒ exit
        case unknown ⇒ println("Received unknown message: " + unknown)
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
