package org.samples.trading.common

import org.samples.trading.domain.Orderbook
import org.samples.trading.domain.OrderbookRepository

trait TradingSystem {
  type ME
  type OR

  val allOrderbookSymbols: List[String] = OrderbookRepository.allOrderbookSymbols

  val orderbooksGroupedByMatchingEngine: List[List[Orderbook]] =
    for (groupOfSymbols: List[String] ← OrderbookRepository.orderbookSymbolsGroupedByMatchingEngine)
      yield groupOfSymbols map (s ⇒ Orderbook(s, false))

  def useStandByEngines: Boolean = true

  lazy val matchingEngines: List[MatchingEngineInfo] = createMatchingEngines

  def matchingEngineRouting: MatchingEngineRouting[ME] = {
    val rules =
      for {
        info ← matchingEngines
        orderbookSymbols = info.orderbooks.map(_.symbol)
      } yield {
        (info.primary, orderbookSymbols)
      }

    MatchingEngineRouting(Map() ++ rules)
  }

  def createMatchingEngines: List[MatchingEngineInfo]

  lazy val orderReceivers: List[OR] = createOrderReceivers

  def createOrderReceivers: List[OR]

  def start()

  def shutdown()

  case class MatchingEngineInfo(primary: ME, standby: Option[ME], orderbooks: List[Orderbook])
}
