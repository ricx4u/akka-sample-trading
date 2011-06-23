name := "akka-sample-trading"

version := "1.1.2-SNAPSHOT"

scalaVersion := "2.9.0-1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases"


libraryDependencies += "se.scalablesolutions.akka" % "akka-actor" % "1.1.2"

libraryDependencies += "se.scalablesolutions.akka" % "akka-dispatcher-extras" % "1.1.2"

libraryDependencies += "org.apache.commons" % "commons-math" % "2.1"

libraryDependencies += "junit" % "junit" % "4.8.2" % "test"

libraryDependencies += "org.scala-tools.testing" % "specs" % "1.6.1" % "test"

libraryDependencies += "org.mockito" % "mockito-all" % "1.8.0" % "test"

