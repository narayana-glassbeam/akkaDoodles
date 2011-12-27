package net.interdoodle.akka

import akka.actor.{Actor, ActorSystem, Props}
import akka.dispatch.{Dispatcher, MessageDispatcher, Future}


/** Non-blocking reduce
 * Executed on the Thread that completes the last Future
 * Taken from Viktor Klang's "Future of Akka" presentation
 * See http://days2011.scala-lang.org/node/138/283 */

object AkkaReduce extends App with Actor {
  val system = ActorSystem("MySystem")
  implicit val defaultDispatcher = system.dispatcher


  def expensiveCalc(x:Int) = { x * x }
  

  def main(args:Array[String]) {
    val map = (1 to 100) map (x => Future { expensiveCalc(x) })
    Future.reduce(map)(_ + _) onComplete {
      _ match {
        case Left(exception) => println(exception)
        case Right(result) => println(result)
      }
    }
  }
}