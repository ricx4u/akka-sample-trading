package org.samples.trading.common

import org.samples.trading.domain.Orderbook

trait OrderReceiver {
  type ME
  var matchingEngineForOrderbook: Map[String, ME] = Map()

  def refreshMatchingEnginePartitions(routing: MatchingEngineRouting[ME]) {

    val matchingEngines: List[ME] = routing.mapping.keys.toList
    def supportedOrderbooks(me: ME): List[String] = routing.mapping(me)

    val m = Map() ++
      (for {
        me ← matchingEngines
        orderbookSymbol ← supportedOrderbooks(me)
      } yield (orderbookSymbol, me))

    matchingEngineForOrderbook = m
  }

}

case class MatchingEngineRouting[ME](mapping: Map[ME, List[String]])