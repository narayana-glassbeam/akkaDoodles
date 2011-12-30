package com.micronautics.akka.dispatch.future

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{BasicResponseHandler, DefaultHttpClient}
import akka.actor.ActorSystem
import akka.dispatch.Future
import scala.MatchError

/** '''Future.filter()''' {{{def filter (pred: (T) => Boolean): Future[T]}}}
  * There is no way for a {{{Future}}} to represent no results, so {{{Future}}}s that fail a filter will contain a {{{MatchError}}} exception.
  * {{{scala.MatchError}}} cannot be imported so the test for filter match failure uses Java reflection.
  * 
  * This example uses future filters to return a list of Future[String] containing the URLs of web pages that contain the string {{{Simpler Concurrency}}}.
  * Filters are applied after the future completes, so onComplete() is silently called. */
object Filter extends App {
  implicit val defaultDispatcher = ActorSystem("MySystem").dispatcher
  val httpclient = new DefaultHttpClient
  List (
    "http://akka.io/",
    "http://www.playframework.org/",
    "http://nbronson.github.com/scala-stm/"
  ) map (url => 
    Future{httpGet(url)}.filter(pageContents => 
      pageContents.indexOf("Simpler Concurrency")>=0
    ) onComplete{f => 
	  f match {
	    case Right(result)   => println("Result: " + url)
	    case Left(exception) => 
	      if (exception.getClass().getName()!="scala.MatchError") {
  	        val msg = exception.getMessage()
	        println("Exception: " + exception.getClass().getName() + " " + msg.substring(msg.lastIndexOf("(")) + " for " + url)
	      }
	  }
    } 
  )

  /** Fetches contents of web page pointed to by urlStr */
  def httpGet(urlStr:String):String = {
    val httpget = new HttpGet(urlStr)
    val brh = new BasicResponseHandler
    httpclient.execute(httpget, brh)
  }
}