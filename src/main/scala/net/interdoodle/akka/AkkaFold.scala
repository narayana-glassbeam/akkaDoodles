package net.interdoodle.akka

import akka.actor.ActorSystem
import akka.dispatch.{Dispatcher, MessageDispatcher, Future}


/** Non-blocking fold
 * Executed on the Thread of the last Future to be completed
 * Taken from Viktor Klang's "Future of Akka" presentation
 * See http://days2011.scala-lang.org/node/138/283 */

object AkkaFold extends App {
  val system = ActorSystem("MySystem")
  implicit val defaultDispatcher = system.dispatcher


  def expensiveCalc(x:Int) = { x * x }

  override def main(args:Array[String]) {
    val futures = (1 to 100) map (x ⇒ Future { expensiveCalc(x) })
    val sum = Future.fold(futures)(0)(_ + _)
  }
}