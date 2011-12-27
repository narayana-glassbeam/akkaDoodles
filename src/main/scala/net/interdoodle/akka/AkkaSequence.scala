package net.interdoodle.akka

import akka.actor.ActorSystem
import akka.dispatch.{Dispatcher, MessageDispatcher, Future}


/** Traversable[Future[T]] => Future[Traversable[T]]
 * Taken from Viktor Klang's "Future of Akka" presentation
 * See http://days2011.scala-lang.org/node/138/283 */

object AkkaSequence extends App {
  val system = ActorSystem("MySystem")
  implicit val defaultDispatcher = system.dispatcher


  def main(args:Array[String]) {
    val stringFutures = for (i <- 1 to 10) yield Future { i.toString }
    val futureStrings = Future sequence stringFutures
  }
}