// see https://github.com/sbt/sbt-assembly
import AssemblyKeys._ // put this at the top of the file

organization := "Micronautics Research"

name := "AkkaDoodles"

version := "0.1"

scalaVersion := "2.9.1"

scalacOptions ++= Seq("-deprecation")

resolvers ++= Seq(
  "Typesafe Snapshots"    at "http://repo.typesafe.com/typesafe/snapshots",
  "Typesafe Releases"     at "http://repo.typesafe.com/typesafe/releases",
  "Scala-Tools Snapshots" at "http://scala-tools.org/repo-snapshots",
  "Scala Tools Releases"  at "http://scala-tools.org/repo-releases"
)

libraryDependencies ++= Seq(
  "org.scalatest"                 %% "scalatest"     % "1.6.1"        % "test" withSources(),
  "com.typesafe.akka"             %  "akka-actor"    % "latest.integration" withSources(),
  "com.github.scala-incubator.io" %% "scala-io-core" % "latest.integration" withSources(),
  "com.github.scala-incubator.io" %% "scala-io-file" % "latest.integration" withSources(),
  "org.apache.httpcomponents"     %  "httpclient"    % "4.1.2"        withSources(),
  "org.scala-tools"               %% "scala-stm"     % "0.5-SNAPSHOT" withSources()
)

seq(assemblySettings: _*)


logLevel := Level.Error

// Optional settings from https://github.com/harrah/xsbt/wiki/Quick-Configuration-Examples follow

// define the statements initially evaluated when entering 'console', 'console-quick', or 'console-project'
initialCommands := """
  import akka.dispatch.{Await,ExecutionContext,Future}
  import akka.util.duration._
  import java.net.URL
  import java.util.concurrent.Executors
  import scala.MatchError
  import scalax.io.JavaConverters.asInputConverter
  import scalax.io.Codec
  import com.micronautics.akka.dispatch.futureScala._
  import com.micronautics.concurrent._
  import com.micronautics.util._
  //
  val executorService = Executors.newFixedThreadPool(10)
  implicit val context = ExecutionContext.fromExecutor(executorService)
  //
  val daemonExecutorService = DaemonExecutors.newFixedThreadPool(10)
  //implicit val daemonContext = ExecutionContext.fromExecutor(daemonExecutorService)
  //
  def expensiveCalc(x:Int) = { x * x }
  //
  def httpGet(urlStr:String):String = {
    new URL(urlStr).asInput.slurpString(Codec.UTF8)
  }
  //
  //val f = Future { println("Evaluating future"); httpGet("http://akka.io") }
  //
  val urlStrs = List (
    "http://akka.io",
    "http://www.playframework.org",
    "http://nbronson.github.com/scala-stm"
  )
"""

// Only show warnings and errors on the screen for compilations.
// This applies to both test:compile and compile and is Info by default
logLevel in compile := Level.Warn

