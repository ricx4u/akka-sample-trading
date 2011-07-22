package org.samples.trading.akka

import akka.actor._
import akka.dispatch.Future
import akka.dispatch.FutureTimeoutException
import akka.dispatch.MessageDispatcher
import org.samples.trading.common.MatchingEngine
import org.samples.trading.domain._
import org.samples.trading.domain.SupportedOrderbooksReq
import akka.dispatch.MessageDispatcher
import akka.actor.ActorRef
import akka.event.EventHandler

class AkkaMatchingEngine(val meId: String, val orderbooks: List[Orderbook], disp: Option[MessageDispatcher])
  extends Actor with MatchingEngine {

  for (d ← disp) {
    self.dispatcher = d
  }

  var standby: Option[ActorRef] = None

  def receive = {
    case standbyRef: ActorRef ⇒
      standby = Some(standbyRef)
    case order: Order ⇒
      handleOrder(order)
    case unknown ⇒
      EventHandler.warning(this, "Received unknown message: " + unknown)
  }

  def handleOrder(order: Order) {
    orderbooksMap.get(order.orderbookSymbol) match {
      case Some(orderbook) ⇒
        val pendingStandbyReply: Option[Future[_]] =
          for (s ← standby) yield { s !!! order }

        txLog.storeTx(order)
        orderbook.addOrder(order)
        orderbook.matchOrders()
        // wait for standby reply
        pendingStandbyReply.foreach(waitForStandby(_))
        done(true)
      case None ⇒
        EventHandler.warning(this, "Orderbook not handled by this MatchingEngine: " + order.orderbookSymbol)
        done(false)
    }
  }

  def done(status: Boolean) {
    self.channel ! new Rsp(status)
  }

  override def postStop {
    txLog.close()
  }

  def waitForStandby(pendingStandbyFuture: Future[_]) {
    try {
      pendingStandbyFuture.await
    } catch {
      case e: FutureTimeoutException ⇒
        EventHandler.error(this, "Standby timeout: " + e)
    }
  }

}