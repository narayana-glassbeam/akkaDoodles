package com.micronautics.akka.dispatch.futureScala

import java.net.URL
import java.util.concurrent.Executors

import akka.dispatch.ExecutionContext
import akka.dispatch.Future
import scalax.io.JavaConverters.asInputConverter
import scalax.io.Codec


/** '''Future.traverse()''' Transforms a {{{Traversable[X] => Future[Traversable[Y]]}}}
 * Use as a parallel map that preserves order.
 * 
 * {{{def traverse [A, B, M[_] <: Traversable[_]] (in: M[A])(fn: (A) => Future[B])(implicit cbf:CanBuildFrom[M[A], B, M[B]], dispatcher:MessageDispatcher):Future[M[B]]}}}
 * 
 * Taken from Viktor Klang's "Future of Akka" presentation
 * @see http://days2011.scala-lang.org/node/138/283 */
object Traverse extends App {
  val executorService = Executors.newFixedThreadPool(10)
  implicit val context = ExecutionContext.fromExecutor(executorService)
  val urls = List (
    "http://akka.io/",
    "http://www.playframework.org/",
    "http://nbronson.github.com/scala-stm/"
  )

  Future.traverse(urls)(url => Future { httpGet(url).length() }) onComplete { f => 
    f match {
      case Right(result)   => println("Traverse Scala web page lengths: " + result)
      case Left(exception) => println("Traverse Scala exception: " + exception)
    }
    System.exit(0)
  }

  /** Fetches contents of web page pointed to by urlStr */
  def httpGet(urlStr:String) = {
    new URL(urlStr).asInput.slurpString(Codec.UTF8)
  }
}