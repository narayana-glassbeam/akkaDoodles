package net.interdoodle.akka

import akka.actor.ActorSystem
import akka.dispatch.Future


/** Traversable[Future[T]] => Future[Traversable[T]]
 * Taken from Viktor Klang's "Future of Akka" presentation
 * See http://days2011.scala-lang.org/node/138/283 */

object AkkaSequence extends App {
  implicit val defaultDispatcher = ActorSystem("MySystem").dispatcher
  val stringFutures = for (i <- 1 to 10) yield Future { i.toString }
  Future sequence stringFutures onComplete { f => 
    f match {
      case Right(result) => println("Result: " + result)
      case Left(exception) => println("Exception: " + exception)
    }
    System.exit(0)
  }
}