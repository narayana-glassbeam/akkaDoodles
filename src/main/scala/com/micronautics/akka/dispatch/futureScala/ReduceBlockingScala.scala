package com.micronautics.akka.dispatch.futureScala

import akka.actor.ActorSystem
import akka.dispatch.Future
import akka.dispatch.Await
import akka.dispatch.ExecutionContext
import akka.util.duration._
import scala.MatchError
import com.micronautics.concurrent.DaemonExecutors


/** '''Future.reduce()'''  Non-blocking reduce, executed on the [[java.lang.Thread]] that completes the last [[akka.dispatch.Future]].
  * 
  * {{{def reduce [T, R >: T] (futures: Traversable[Future[T]])(op: (R, T) => T)(implicit dispatcher: MessageDispatcher): Future[R]}}}
  * 
  * This example is based on the code fragment shown in Viktor Klang's '''Future of Akka''' presentation.
  * @see http://days2011.scala-lang.org/node/138/283 */
object ReduceBlockingScala extends App {
  val daemonExecutorService = DaemonExecutors.newFixedThreadPool(10)
  implicit val daemonContext = ExecutionContext.fromExecutor(daemonExecutorService)

  val expensiveFutures = (1 to 100) map (x => Future { expensiveCalc(x) })
  val resultFuture = Future.reduce(expensiveFutures)(_ + _)
  val result = Await.result(resultFuture, 1 second) 
  println("Blocking Scala reduce result: " + result)

  /** Pretend this method consumes a lot of computational resource */
  def expensiveCalc(x:Int) = { x * x }
}