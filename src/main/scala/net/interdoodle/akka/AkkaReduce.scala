package net.interdoodle.akka

import akka.actor.ActorSystem
import akka.dispatch.Future


/** Non-blocking reduce
 * reduce is executed on the Thread that completes the last Future
 * Taken from Viktor Klang's "Future of Akka" presentation
 * See http://days2011.scala-lang.org/node/138/283 */

object AkkaReduce extends App {
  implicit val defaultDispatcher = ActorSystem("MySystem").dispatcher
  val map = (1 to 100) map (x => Future { expensiveCalc(x) })
  
  Future.reduce(map)(_ + _) onComplete { f => 
    f match {
      case Right(result) => println("Result: " + result)
      case Left(exception) => println("Exception: " + exception)
    }
    System.exit(0)
  }


  def expensiveCalc(x:Int) = { x * x }
}