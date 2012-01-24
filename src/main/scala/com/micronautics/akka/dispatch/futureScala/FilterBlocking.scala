package com.micronautics.akka.dispatch.futureScala

import java.net.URL
import java.util.concurrent.Executors
import akka.dispatch.ExecutionContext
import akka.dispatch.Future
import scalax.io.JavaConverters.asInputConverter
import scalax.io.Codec
import com.micronautics.concurrent.DaemonExecutors
import akka.dispatch.Await
import akka.util.duration._

/**
  * '''Future.filter()''' {{{def filter (pred: (T) => Boolean): Future[T]}}}
  *
  * This example uses future filters to return a list of Future[String] containing the URLs of web pages that contain the string {{{Simpler Concurrency}}}.
  *
  * Filters are applied after the future completes, so onComplete() is called implicitly by the filter. If you provide Future.onComplete(), the filter will be evaluated before your onComplete()
  *
  * There is no way for a {{{Future}}} to represent the lack of results, so {{{Future}}}s that fail a filter contain a {{{MatchError}}} exception (on the Left).
  */
object FilterBlocking extends App {
  val daemonExecutorService = DaemonExecutors.newFixedThreadPool(10)
  implicit val context = ExecutionContext.fromExecutor(daemonExecutorService)
  val urlStrs = List (
    "http://akka.io/",
    "http://www.playframework.org/",
    "http://nbronson.github.com/scala-stm/"
  )

  // print contents of pages containing the string "Simpler Concurrency"
  val futures = urlStrs map (urlStr =>
    Future(httpGet(urlStr)) filter (pageContents =>
      pageContents.indexOf("Simpler Concurrency")>=0))
  val futureList = Future.sequence(futures)
  /*Await.result(futureList.map { items => map { contents =>
      println("Scala Filter blocking result: " + contents.substring(0,20) + "...") }
    }, 1 second
  )*/

  /** Fetches contents of web page pointed to by urlStr */
  def httpGet(urlStr:String):String = {
    new URL(urlStr).asInput.slurpString(Codec.UTF8)
  }
}