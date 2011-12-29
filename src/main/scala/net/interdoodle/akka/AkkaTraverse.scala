package net.interdoodle.akka

import akka.actor.ActorSystem
import akka.dispatch.Future
import org.apache.http.client._
import org.apache.http.client.methods._
import org.apache.http.impl.client._


/** Transforms a Traversable[X] ⇒ Future[Traversable[Y]]
 * Use it as a parallel map that preserves order
 * Taken from Viktor Klang's "Future of Akka" presentation
 * See http://days2011.scala-lang.org/node/138/283 */

object AkkaTraverse extends App {
  implicit val defaultDispatcher = ActorSystem("MySystem").dispatcher
  val httpclient = new DefaultHttpClient
  val urls = List (
    "http://akka.io/",
    "http://www.playframework.org/",
    "http://nbronson.github.com/scala-stm/"
  )

  Future.traverse(urls)(url => Future { httpGet(url) }) onComplete { f => 
    f match {
      case Right(result) => println("Result: " + result)
      case Left(exception) => println("Exception: " + exception)
    }
    System.exit(0)
  }


  def httpGet(urlStr:String) = {
    val httpget = new HttpGet(urlStr)
    val brh = new BasicResponseHandler
    httpclient.execute (httpget, brh)
  }
}