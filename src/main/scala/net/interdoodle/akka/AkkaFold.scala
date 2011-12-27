package net.interdoodle.akka

import akka.dispatch.{Dispatcher, MessageDispatcher, Future}


/** Non-blocking fold
 * Executed on the Thread of the last Future to be completed
 * Taken from Viktor Klang's "Future of Akka" presentation
 * See http://days2011.scala-lang.org/node/138/283 */

object AkkaFold extends App {

  def expensiveCalc(x:Int) = { x * x }
  
  def main(args:Array[String]) {
    // Need an implicit MessageDispatcher somehow. What is the simplest example possible?
    // What other options exist for providing a MessageDispatcher?
    val map = (1 to 100) map (x => Future { expensiveCalc(x) })
    Future.reduce(map)(_ + _) onComplete {
      _.value.get match {
        case Left(exception) => actor ! MRFailed(exception) // What is MRResult?
        case Right(result) => actor ! MRResult(result) // What is MRResult?
      }
    }
  }
}