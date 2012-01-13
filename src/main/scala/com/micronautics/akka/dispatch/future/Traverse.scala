package com.micronautics.akka.dispatch.future

import java.net.URL
import scalax.io._
import scalax.io.JavaConverters._

import akka.actor.ActorSystem
import akka.dispatch.Future


/** '''Future.traverse()''' Transforms a {{{Traversable[X] => Future[Traversable[Y]]}}}
 * Use as a parallel map that preserves order.
 * 
 * {{{def traverse [A, B, M[_] <: Traversable[_]] (in: M[A])(fn: (A) => Future[B])(implicit cbf:CanBuildFrom[M[A], B, M[B]], dispatcher:MessageDispatcher):Future[M[B]]}}}
 * 
 * Taken from Viktor Klang's "Future of Akka" presentation
 * @see http://days2011.scala-lang.org/node/138/283 */
object Traverse extends App {
  implicit val defaultDispatcher = ActorSystem("MySystem").dispatcher
  val urls = List (
    "http://akka.io/",
    "http://www.playframework.org/",
    "http://nbronson.github.com/scala-stm/"
  )

  Future.traverse(urls)(url => Future { httpGet(url).length() }) onComplete { f => 
    f match {
      case Right(result)   => println("Web page lengths: " + result)
      case Left(exception) => println("Exception: " + exception)
    }
    System.exit(0)
  }

  /** Fetches contents of web page pointed to by urlStr */
  def httpGet(urlStr:String) = {
    new URL(urlStr).asInput.slurpString(Codec.UTF8)
  }
}