package org.samples.trading.actor

import org.samples.trading.common._
import org.samples.trading.domain.Orderbook

class ActorTradingSystem extends TradingSystem {
  type ME = ActorMatchingEngine
  type OR = ActorOrderReceiver

  override def createMatchingEngines: List[MatchingEngineInfo] = {
    for {
      (orderbooks, i) ← orderbooksGroupedByMatchingEngine.zipWithIndex
      n = i + 1
    } yield {
      val me = createMatchingEngine("ME" + n, orderbooks)
      val orderbooksCopy = orderbooks map (o ⇒ Orderbook(o.symbol, true))
      val standbyOption =
        if (useStandByEngines) {
          val meStandby = createMatchingEngine("ME" + n + "s", orderbooksCopy)
          Some(meStandby)
        } else {
          None
        }

      MatchingEngineInfo(me, standbyOption, orderbooks)
    }
  }

  def createMatchingEngine(meId: String, orderbooks: List[Orderbook]) =
    new ActorMatchingEngine(meId, orderbooks)

  override def createOrderReceivers: List[ActorOrderReceiver] = {
    (1 to 10).toList map (i ⇒ createOrderReceiver())
  }

  def createOrderReceiver() = new ActorOrderReceiver

  override def start() {
    for (MatchingEngineInfo(p, s, o) ← matchingEngines) {
      p.start()
      // standby is optional
      s.foreach(_.start())
      p.standby = s
    }
    val routing = matchingEngineRouting
    for (or ← orderReceivers) {
      or.start()
      or ! routing
    }
  }

  override def shutdown() {
    orderReceivers.foreach(_ ! "exit")
    for (MatchingEngineInfo(p, s, o) ← matchingEngines) {
      p ! "exit"
      // standby is optional
      s.foreach(_ ! "exit")
    }
  }
}
