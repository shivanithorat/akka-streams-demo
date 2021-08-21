name := "akka-streams-gk"

version := "0.1"

scalaVersion := "2.13.6"
val AkkaHttpVersion = "10.2.4"
val akkaVersion = "2.6.15"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed"  % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.1.4",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.slf4j"        % "slf4j-api"    % "1.7.30",
  "io.bullet" %% "borer-core" % "1.7.1",
  "io.bullet" %% "borer-derivation" % "1.7.1",
  "io.bullet" %% "borer-compat-akka" % "1.7.1",
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
)