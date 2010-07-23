package org.samples.trading.basic

import org.junit._
import Assert._

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import scala.actors._
import scala.actors.threadpool._

import org.samples.trading.domain._
import org.samples.trading.common._

class BasicPerformanceTest extends PerformanceTest {
  type TS = BasicTradingSystem
  type OR = BasicOrderReceiver
  
//  val threadPool: ExecutorService = Executors.newFixedThreadPool(4)
  val threadPool: ExecutorService = Executors.newCachedThreadPool()

  override def createTradingSystem: TS = new BasicTradingSystem

  override def placeOrder(orderReceiver: BasicOrderReceiver, order: Order): Rsp = {
    orderReceiver.asInstanceOf[BasicOrderReceiver].placeOrder(order)
  }


  // need this so that junit will detect this as a test case
  @Test
  def dummy {
    
  }

  override def runScenario(scenario: String, orders: List[Order], repeat: Int, numberOfClients: Int, delayMs: Int) = {
    val totalNumberOfRequests = orders.size * repeat * numberOfClients
    val latch = new CountDownLatch(numberOfClients)
    val receivers = tradingSystem.orderReceivers.toIndexedSeq
    val clients = (for (i <- 0 until numberOfClients) yield {
      val receiver = receivers(i % receivers.size)
      new Client(receiver, orders, latch, repeat, delayMs)
    }).toList

    val start = System.nanoTime
    clients.foreach(_.start)
    val ok = latch.await(5000 + (2 + delayMs) * totalNumberOfRequests, TimeUnit.MILLISECONDS)
    val durationNs = (System.nanoTime - start)

    assertTrue(ok)
    assertEquals(numberOfClients * (orders.size / 2) * repeat, TotalTradeCounter.counter.get)
    logMeasurement(scenario, numberOfClients, durationNs, repeat, totalNumberOfRequests)
  }

  class Client(orderReceiver: BasicOrderReceiver, orders: List[Order], latch: CountDownLatch, repeat: Int, delayMs: Int) extends Actor {

    def this(orderReceiver: BasicOrderReceiver, orders: List[Order], latch: CountDownLatch, repeat: Int) {
      this (orderReceiver, orders, latch, repeat, 0)
    }
    
    override def scheduler = new SchedulerAdapter {
      def execute(block: => Unit) =
        threadPool.execute(new Runnable {
          def run() { block }
        })
    }

    def act() {
      (1 to repeat).foreach(i =>
        {
          // println("Client repeat: " + i)
          for (o <- orders) {
            val t0 = System.nanoTime
            val rsp = placeOrder(orderReceiver, o)
            val duration = System.nanoTime - t0
            stat.addValue(duration)
            if (!rsp.status) {
              throw new IllegalStateException("Invalid rsp")
            }
            delay(delayMs)
          }
        }
        )
      latch.countDown()
      exit
    }
  }
}


