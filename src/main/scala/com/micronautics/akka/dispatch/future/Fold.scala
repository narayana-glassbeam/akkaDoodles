package com.micronautics.akka.dispatch.future

import akka.actor.ActorSystem
import akka.dispatch.Future


/** '''Future.fold()''' {{{def fold [T, R] (futures: Traversable[Future[T]])(zero: R)(foldFun: (R, T)=> R)(implicit dispatcher: MessageDispatcher): Future[R]}}}
  * Non-blocking fold is executed on the thread of the last Future to be completed
  * Taken from Viktor Klang's "Future of Akka" presentation
  * See http://days2011.scala-lang.org/node/138/283 */
object Fold extends App {
  implicit val defaultDispatcher = ActorSystem("MySystem").dispatcher
  val futures = (1 to 10) map (x => Future { expensiveCalc(x) })
  
  Future.fold(futures)(0)(_ + _) onComplete { f => 
    f match {
      case Right(result)   => println("Result: " + result)
      case Left(exception) => println("Exception: " + exception)
    }
    System.exit(0)
  }

  /** Pretend that method consumes a lot of computational resource */
  def expensiveCalc(x:Int) = { x * x }
}