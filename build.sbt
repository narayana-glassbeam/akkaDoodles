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
  "org.scalatest"                 %% "scalatest"     % "latest.integration" % "test"    withSources(),
  "com.typesafe.akka"             %  "akka-actor"    % "latest.milestone"   % "compile" withSources(),
  "com.github.scala-incubator.io" %% "scala-io-core" % "latest.integration" % "compile" withSources(),
  "com.github.scala-incubator.io" %% "scala-io-file" % "latest.integration" % "compile" withSources(),
  "org.scala-tools"               %% "scala-stm"     % "0.5-SNAPSHOT"       % "compile" withSources()
)