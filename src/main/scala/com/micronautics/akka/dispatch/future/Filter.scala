package com.micronautics.akka.dispatch.future

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{BasicResponseHandler, DefaultHttpClient}
import akka.actor.ActorSystem
import akka.dispatch.Future
import scala.MatchError

/** '''Future.filter()''' {{{def filter (pred: (T) => Boolean): Future[T]}}} */
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