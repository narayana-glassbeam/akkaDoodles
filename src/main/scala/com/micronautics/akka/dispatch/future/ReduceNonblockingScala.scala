package com.micronautics.akka.dispatch.future

import akka.actor.ActorSystem
import akka.dispatch.Future
import java.util.concurrent.Executors
import akka.dispatch.ExecutionContext


/** '''Future.reduce()'''  Non-blocking reduce, executed on the [[java.lang.Thread]] that completes the last [[akka.dispatch.Future]].
  * 
  * {{{def reduce [T, R >: T] (futures: Traversable[Future[T]])(op: (R, T) => T)(implicit dispatcher: MessageDispatcher): Future[R]}}}
  * 
  * This example is based on the code fragment shown in Viktor Klang's '''Future of Akka''' presentation.
  * @see http://days2011.scala-lang.org/node/138/283 */
object ReduceNonblockingScala extends App {
  val executorService = Executors.newFixedThreadPool(10)
  implicit val context = ExecutionContext.fromExecutor(executorService)

  val expensiveFutures = (1 to 100) map (x => Future { expensiveCalc(x) })
  
  Future.reduce(expensiveFutures)(_ + _) onComplete { f => 
    f match {
      case Right(result)   => println("Nonblocking Scala reduce result: " + result)
      case Left(exception) => println("Nonblocking Scala reduce exception: " + exception)
    }
    executorService.shutdown(); // terminates program
  }

  /** Pretend this method consumes a lot of computational resource */
  def expensiveCalc(x:Int) = { x * x }
}