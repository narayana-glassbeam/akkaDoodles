package net.interdoodle.akka

import akka.dispatch.{Dispatcher, MessageDispatcher, Future}


/** Non-blocking reduce
 * Executed on the Thread that completes the last Future
 * Taken from Viktor Klang's "Future of Akka" presentation
 * See http://days2011.scala-lang.org/node/138/283 */

object AkkaReduce extends App {

  def expensiveCalc(x:Int) = { x * x }
  
  def main(args:Array[String]) {
    // Need an implicit MessageDispatcher somehow. What is the simplest example possible?
    // What other options exist for providing a MessageDispatcher?
    val futures = (1 to 100) map (x ⇒ Future { expensiveCalc(x) })
    val sum = Future.fold(futures)(_ + _)(0)
  }
}