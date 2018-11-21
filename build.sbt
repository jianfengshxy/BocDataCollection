name := "bocdatacollection"

organization := "com.chinapex"

version := "0.1"

scalaVersion := "2.10.3"

resolvers += "twitter-repo" at "http://maven.twttr.com"

addCommandAlias("cc", ";clean;compile;test:compile")