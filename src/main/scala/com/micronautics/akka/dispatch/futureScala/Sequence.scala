package com.micronautics.akka.dispatch.futureScala

import akka.actor.ActorSystem
import java.util.concurrent.Executors
import akka.dispatch.{ExecutionContext, Future}


/** '''Future.sequence()''' {{{Traversable[Future[T]] => Future[Traversable[T]]}}}
  * Simple way of working with Futures.
  *
  * {{{def sequence [A, M[_] <: Traversable[_]] (in: M[Future[A]])(implicit cbf: CanBuildFrom[M[Future[A]], A, M[A]], dispatcher: MessageDispatcher): Future[M[A]]}}}
  *
  * Taken from Viktor Klang's "Future of Akka" presentation
  * @see http://days2011.scala-lang.org/node/138/283 */
object Sequence extends App {
  val executorService = Executors.newFixedThreadPool(10)
  implicit val context = ExecutionContext.fromExecutor(executorService)
  val stringFutures = for (i <- 1 to 10) yield Future { i.toString }

  Future sequence stringFutures onComplete { f =>
    f match {
      case Right(result)   => println("Sequence Scala result: " + result)
      case Left(exception) => println("Sequence Scala exception: " + exception)
    }
    executorService.shutdown(); // terminates program
  }
}