package net.interdoodle.akka

import akka.dispatch.{Dispatcher, MessageDispatcher, Future}


/** Traversable[Future[T]] => Future[Traversable[T]]
 * Taken from Viktor Klang's "Future of Akka" presentation
 * See http://days2011.scala-lang.org/node/138/283 */

object AkkaSequence extends App {

  def main(args:Array[String]) {
    val stringFutures = for (i <- 1 to 10) yield Future { i.toString }
    val futureStrings = Future sequence stringFutures
  }
}