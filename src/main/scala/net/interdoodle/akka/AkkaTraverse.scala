package net.interdoodle.akka

import akka.dispatch.{Dispatcher, MessageDispatcher, Future}


/** Taken from Viktor Klang's "Future of Akka" presentation
 * See http://days2011.scala-lang.org/node/138/283 */

object AkkaTraverse extends App {

  def expensiveCalc(x:Int) = { x * x }
  
  def main(args:Array[String]) {
    val urls = List (
      "http://akka.io/",
      "http://www.playframework.org/",
      "http://nbronson.github.com/scala-stm/"
    )
    // Error: could not find implicit value for parameter dispatcher: akka.dispatch.MessageDispatcher
    val futureListOfPages = Future.traverse(urls)(url ⇒ Future { GET(url) })
  }
}