
/*
 * BASIC INFORMATION
 ********************************************************/

name := "ubirch-mongo-utils"
version := "0.9.5"
description := "MongoDB related utils"
organization := "com.ubirch.util"
homepage := Some(url("http://ubirch.com"))
scalaVersion := "2.11.12"
scalacOptions ++= Seq(
  "-feature"
)
scmInfo := Some(ScmInfo(
  url("https://github.com/ubirch/ubirch-mongo-utils"),
  "https://github.com/ubirch/ubirch-mongo-utils.git"
))

/*
 * CREDENTIALS
 ********************************************************/

(sys.env.get("CLOUDREPO_USER"), sys.env.get("CLOUDREPO_PW")) match {
  case (Some(username), Some(password)) =>
    println("USERNAME and/or PASSWORD found.")
    credentials += Credentials("ubirch.mycloudrepo.io", "ubirch.mycloudrepo.io", username, password)
  case _ =>
    println("USERNAME and/or PASSWORD is taken from /.sbt/.credentials")
    credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
}


/*
 * RESOLVER
 ********************************************************/

val resolverUbirchUtils = "cloudrepo.io" at "https://ubirch.mycloudrepo.io/repositories/ubirch-utils-mvn"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  resolverUbirchUtils)


/*
 * PUBLISHING
 ********************************************************/


publishTo := Some(resolverUbirchUtils)
publishMavenStyle := true


/*
 * DEPENDENCIES
 ********************************************************/

//version
val akkaV = "2.5.11"

//groups
val ubirchUtilGroup = "com.ubirch.util"
val akkaG = "com.typesafe.akka"

// Ubirch dependencies
lazy val ubirchUtilConfig = ubirchUtilGroup %% "ubirch-config-utils" % "0.2.4"
lazy val ubirchUtilDeepCheckModel = ubirchUtilGroup %% "ubirch-deep-check-utils" % "0.4.1"

// External dependencies
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
val akkaActor = akkaG %% "akka-actor" % akkaV
val akkaSlf4j = akkaG %% "akka-slf4j" % akkaV
val reactiveMongo = "org.reactivemongo" %% "reactivemongo" % "0.18.8" excludeAll ExclusionRule(organization = s"${akkaActor.organization}", name = s"${akkaActor.name}")
val jodaTime = "joda-time" % "joda-time" % "2.10"
val jodaConvert = "org.joda" % "joda-convert" % "2.1.1"
val mockito = "org.mockito" % "mockito-core" % "2.23.4"

val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
val slf4j = "org.slf4j" % "slf4j-api" % "1.7.21"
val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.1.7"

lazy val logging = Seq(
  scalaLogging,
  slf4j,
  logbackClassic
)


libraryDependencies ++= Seq(
  ubirchUtilConfig,
  ubirchUtilDeepCheckModel,
  akkaSlf4j,
  reactiveMongo,
  jodaTime,
  jodaConvert,
  scalaTest % "test",
  mockito
) ++ logging


