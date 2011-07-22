package org.samples.trading.basic

import org.samples.trading.common._
import org.samples.trading.domain.Orderbook

class BasicTradingSystem extends TradingSystem {
  type ME = BasicMatchingEngine
  type OR = BasicOrderReceiver

  override def createMatchingEngines: List[MatchingEngineInfo] = {
    for {
      (orderbooks, i) ← orderbooksGroupedByMatchingEngine.zipWithIndex
      n = i + 1
    } yield {
      val me = new BasicMatchingEngine("ME" + i, orderbooks)
      val orderbooksCopy = orderbooks map (o ⇒ Orderbook(o.symbol, true))
      val standbyOption =
        if (useStandByEngines) {
          val meStandby = new BasicMatchingEngine("ME" + i + "s", orderbooksCopy)
          Some(meStandby)
        } else {
          None
        }

      MatchingEngineInfo(me, standbyOption, orderbooks)
    }
  }

  override def createOrderReceivers: List[BasicOrderReceiver] = {
    (1 to 10).toList map (i ⇒ new BasicOrderReceiver())
  }

  override def start() {
    for (MatchingEngineInfo(p, s, o) ← matchingEngines) {
      p.standby = s
    }
    val routing = matchingEngineRouting
    for (or ← orderReceivers) {
      or.updateRouting(routing)
    }
  }

  override def shutdown() {
    for (MatchingEngineInfo(p, s, o) ← matchingEngines) {
      p.exit()
      // standby is optional
      s.foreach(_.exit())
    }
  }

}
