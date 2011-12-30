package com.micronautics.akka.dispatch.future

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{BasicResponseHandler, DefaultHttpClient}

import akka.actor.ActorSystem
import akka.dispatch.Future


/** '''Future.traverse()''' Transforms a {{{Traversable[X] ⇒ Future[Traversable[Y]]}}}
 * Use as a parallel map that preserves order.
 * Taken from Viktor Klang's "Future of Akka" presentation
 * See http://days2011.scala-lang.org/node/138/283 */
object Traverse extends App {
  implicit val defaultDispatcher = ActorSystem("MySystem").dispatcher
  val httpclient = new DefaultHttpClient
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
    val httpget = new HttpGet(urlStr)
    val brh = new BasicResponseHandler
    httpclient.execute (httpget, brh)
  }
}