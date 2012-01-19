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
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.3.0"        withSources(),
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.3.0"        withSources(),
  "org.apache.httpcomponents"     %  "httpclient"    % "4.1.2"        withSources(),
  "org.scala-tools"               %% "scala-stm"     % "0.5-SNAPSHOT" withSources()
)

seq(assemblySettings: _*)
