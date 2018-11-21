import sbt._
import sbt.Keys._

import sbtassembly.Plugin._
import AssemblyKeys._

object Dependencies {
  val finagleVersion = "6.13.1"

  val finalgeserversets = "com.twitter" % "finagle-serversets_2.10" % finagleVersion

  val finaglestats = "com.twitter" % "finagle-stats_2.10" % finagleVersion

  val finagleredis = "com.twitter" % "finagle-redis_2.10" % finagleVersion

  val twitterserver = "com.twitter" %% "twitter-server" % "1.6.1"

  val fastjson = "com.alibaba" % "fastjson" % "1.1.15"

  val scalatest = "org.scalatest" % "scalatest_2.10" % "2.1.0" % "test"
  val mockito = "org.mockito" % "mockito-core" % "1.9.5" % "test"

  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.7"
  val log4j2slf4j = "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.0.2"
  val log4japi = "org.apache.logging.log4j" % "log4j-api" % "2.0.2"
  val log4jcore = "org.apache.logging.log4j" % "log4j-core" % "2.0.2"
  val disruptor = "com.lmax" % "disruptor" % "3.2.0"
  val log4j12api = "org.apache.logging.log4j" % "log4j-1.2-api" % "2.0-rc1"


  val configproperties = "com.typesafe" % "config" % "1.2.1"

  val ini4j = "org.ini4j" % "ini4j" % "0.5.2"

  val jodatime = "joda-time" % "joda-time" % "2.5"
  val jodaConvert = "org.joda" % "joda-convert" % "1.2"


  val pool2 = "org.apache.commons" % "commons-pool2" % "2.3"
  val protobuftojson = "com.googlecode.protobuf-java-format" % "protobuf-java-format" % "1.2"
  val jcommander = "com.beust" % "jcommander" % "1.72"
}

object FinagleDE extends Build {

  import Dependencies._

  val libraryDeps = Seq(
    fastjson,

    slf4j,
    log4j2slf4j,
    log4jcore,
    log4japi,
    disruptor,

    scalatest,
    mockito

    , configproperties
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri")

    ,pool2
    ,ini4j
    ,jodatime
    ,jodaConvert
    ,jcommander
  )

  val twitterSrvr = Seq(
    twitterserver,
    finalgeserversets
      exclude("org.slf4j", "slf4j-jdk14"),
    finaglestats
  )

     val buildSettings = Project.defaultSettings

  lazy val root = Project(
    id = "root",
    base = file("."))
    .aggregate(bocdatacollection)


  lazy val bocdatacollection = Project(
    id = "bocdatacollection",
    base = file("bocdatacollection"),
    settings = buildSettings
  ) settings (
    libraryDependencies ++= libraryDeps ++ twitterSrvr,
    libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-jdk14")) },
    test in assembly := {},  // skip test during assembly
    mainClass in assembly := Some("dc.DataCollectionServer"),
    jarName in assembly := "de-assembly.jar"
    )
}
