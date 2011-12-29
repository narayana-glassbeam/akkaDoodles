package net.interdoodle.akka

import akka.actor.ActorSystem
import akka.dispatch.{Dispatcher, MessageDispatcher, Future}


/** Non-blocking fold
 * Executed on the Thread of the last Future to be completed
 * Taken from Viktor Klang's "Future of Akka" presentation
 * See http://days2011.scala-lang.org/node/138/283 */

object AkkaFold extends App {
  implicit val defaultDispatcher = ActorSystem("MySystem").dispatcher
  val futures = (1 to 10) map (x => Future { expensiveCalc(x) })
  Future.fold(futures)(0)(_ + _) onComplete { f =>
	if (f.isRight)
	  println("Result: " + f.right.get)
    else
	  println("Exception: " + f.left.get)
    System.exit(0)
  }


  def expensiveCalc(x:Int) = { x * x }
}