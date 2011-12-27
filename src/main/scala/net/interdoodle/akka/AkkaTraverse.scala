package net.interdoodle.akka

import akka.actor.ActorSystem
import akka.dispatch.{Dispatcher, MessageDispatcher, Future}


/** Transforms a Traversable[X] ⇒ Future[Traversable[Y]]
 * Use it as a parallel map that preserves order
 * Taken from Viktor Klang's "Future of Akka" presentation
 * See http://days2011.scala-lang.org/node/138/283 */

object AkkaTraverse extends App {
  val system = ActorSystem("MySystem")
  implicit val defaultDispatcher = system.dispatcher


  def expensiveCalc(x:Int) = { x * x }
  
  def main(args:Array[String]) {
    val urls = List (
      "http://akka.io/",
      "http://www.playframework.org/",
      "http://nbronson.github.com/scala-stm/"
    )
    val futureListOfPages = Future.traverse(urls)(url ⇒ Future { GET(url) })
  }
}