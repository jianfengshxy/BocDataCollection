import sbt._
import sbt.Keys._

import sbtassembly.Plugin._
import AssemblyKeys._
import com.twitter.scrooge.ScroogeSBT
import sbtprotobuf.{ProtobufPlugin=>PB}
import de.johoop.jacoco4sbt.JacocoPlugin._

object Dependencies {
  val finagleVersion = "6.13.1"

  val finalgeserversets = "com.twitter" % "finagle-serversets_2.10" % finagleVersion

  val finaglestats = "com.twitter" % "finagle-stats_2.10" % finagleVersion

  val finagleredis = "com.twitter" % "finagle-redis_2.10" % finagleVersion

  val twitterserver = "com.twitter" %% "twitter-server" % "1.6.1"

  val scroogecore = "com.twitter" % "scrooge-core_2.10" % "3.13.1"
  val thrift = "org.apache.thrift" % "libthrift" % "0.5.0"
  val protobuf = "com.google.protobuf" % "protobuf-java" % "2.4.1"

  val fastjson = "com.alibaba" % "fastjson" % "1.1.15"

  val scalatest = "org.scalatest" % "scalatest_2.10" % "2.1.0" % "test"
  val mockito = "org.mockito" % "mockito-core" % "1.9.5" % "test"

  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.7"
  val log4j2slf4j = "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.0.2"
  val log4japi = "org.apache.logging.log4j" % "log4j-api" % "2.0.2"
  val log4jcore = "org.apache.logging.log4j" % "log4j-core" % "2.0.2"
  val disruptor = "com.lmax" % "disruptor" % "3.2.0"
  val log4j12api = "org.apache.logging.log4j" % "log4j-1.2-api" % "2.0-rc1"

  val sqlite = "org.xerial" % "sqlite-jdbc" % "3.7.2"
  val configproperties = "com.typesafe" % "config" % "1.2.1"
  val kafkaclient = "org.apache.kafka" % "kafka_2.10" % "0.8.1.1"

  val ini4j = "org.ini4j" % "ini4j" % "0.5.2"

  val jodatime = "joda-time" % "joda-time" % "2.5"
  val jodaConvert = "org.joda" % "joda-convert" % "1.2"

  val skydbClient = "com.allyes.skydb" % "skydb-client" % "2.3.1.2"
  val pool2 = "org.apache.commons" % "commons-pool2" % "2.3"
  val doubleclick = "com.google.doubleclick" % "doubleclick-core" % "0.8.6"
  val protobuftojson = "com.googlecode.protobuf-java-format" % "protobuf-java-format" % "1.2"
  val jcommander = "com.beust" % "jcommander" % "1.72"
}

object FinagleDE extends Build {

  import Dependencies._

  val libraryDeps = Seq(
    scroogecore,
    thrift,
    protobuf,
    fastjson,

    slf4j,
    log4j2slf4j,
    log4jcore,
    log4japi,
    disruptor,

    scalatest,
    mockito

    , sqlite
    , configproperties
    , kafkaclient
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri")

    ,skydbClient
      exclude("org.apache.thrift", "libfb303")
    ,pool2
    ,ini4j
    ,jodatime
    ,jodaConvert
    ,doubleclick
    ,protobuftojson
    ,jcommander
  )

  val twitterSrvr = Seq(
    twitterserver,
 //   finagleredis,
    finalgeserversets
      exclude("org.slf4j", "slf4j-jdk14"),
    finaglestats
  )

  val pluginSettings = Seq(
    ScroogeSBT.newSettings ++ Seq(
      ScroogeSBT.scroogeBuildOptions := Seq("--finagle")
    ),
    PB.protobufSettings,
    assemblySettings ++ Seq(
      mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
      {
        case "com/twitter/common/args/apt/cmdline.arg.info.txt.1" => MergeStrategy.first
        case PathList("org", "slf4j", xs @ _*) => MergeStrategy.first
        case PathList(ps @ _*) if ps.last endsWith "Log4j2Plugins.dat" => MergeStrategy.first
        case "log4j2.xml" => MergeStrategy.first
        case x => old(x)
      }}
    ),
    net.virtualvoid.sbt.graph.Plugin.graphSettings,
    jacoco.settings
  ).flatten

  val repoSettings = Seq(
    resolvers ++= Seq(

    )
  )

  val buildSettings = Project.defaultSettings ++ pluginSettings ++ repoSettings

  lazy val root = Project(
    id = "root",
    base = file("."))
    .aggregate(dc)


  lazy val dc = Project(
    id = "dc",
    base = file("dc"),
    settings = buildSettings
  ) settings (
    libraryDependencies ++= libraryDeps ++ twitterSrvr,
    libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-jdk14")) },
    test in assembly := {},  // skip test during assembly
    mainClass in assembly := Some("dc.DataCollectionServer"),
    jarName in assembly := "de-assembly.jar"
    )
}
