package net.interdoodle.akka

import akka.actor.ActorSystem
import akka.dispatch.Future


/** Non-blocking fold
 * Fold is executed on the thread of the last Future to be completed
 * Taken from Viktor Klang's "Future of Akka" presentation
 * See http://days2011.scala-lang.org/node/138/283 */

object AkkaFold extends App {
  implicit val defaultDispatcher = ActorSystem("MySystem").dispatcher
  val futures = (1 to 10) map (x => Future { expensiveCalc(x) })
  Future.fold(futures)(0)(_ + _) onComplete { f => 
    f match {
      case Right(result) => println("Result: " + result)
      case Left(exception) => println("Exception: " + exception)
    }
    System.exit(0)
  }


  def expensiveCalc(x:Int) = { x * x }
}