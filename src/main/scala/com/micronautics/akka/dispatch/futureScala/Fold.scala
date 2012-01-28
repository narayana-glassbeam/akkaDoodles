package com.micronautics.akka.dispatch.futureScala

import java.util.concurrent.Executors

import akka.dispatch.{ExecutionContext, Future}


/** '''Future.fold()''' 
  * 
  * {{{def fold [T, R] (futures: Traversable[Future[T]])(zero: R)(foldFun: (R, T)=> R)(implicit dispatcher: MessageDispatcher): Future[R]}}}
  * 
  * Non-blocking fold is executed on the thread of the last Future to be completed.
  * 
  * Taken from Viktor Klang's "Future of Akka" presentation
  * @see http://days2011.scala-lang.org/node/138/283 */
object Fold extends App {
  val executorService = Executors.newFixedThreadPool(10)
  implicit val context = ExecutionContext.fromExecutor(executorService)
  val futures = (1 to 10) map (x => Future { expensiveCalc(x) })
  
  Future.fold(futures)(0)(_ + _) onComplete { f => 
    f match {
      case Right(result)   => println("Fold Scala result: " + result)
      case Left(exception) => println("Fold Scala exception: " + exception)
    }
    executorService.shutdown(); // terminates program
  }

  /** Pretend this method consumes a lot of computational resource */
  def expensiveCalc(x:Int) = { x * x }
}