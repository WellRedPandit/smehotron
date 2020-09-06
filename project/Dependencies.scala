import sbt._

object V {
  val betterFiles = "3.1.0"
  val commonsIo = "2.7"
  val typesafeConfig = "1.4.0"
  val hashids = "1.0.3"
  val jdom = "2.0.6"
  val logback = "1.2.3"
  val scala = "2.13.3"
  val scalaCheck = "1.14.3"
  val scalaLogging = "3.9.2"
  val scalaTest = "3.2.2"
  val scalaXml = "1.3.0"
  val scopt = "3.7.1"
  val slf4j = "1.7.30"

  val tron = "1.13.0"
}

object Dependencies {
  val commonsIo = "commons-io" % "commons-io" % V.commonsIo
  val typesafeConfig = "com.typesafe" % "config" % V.typesafeConfig
  val slf4j = "org.slf4j" % "slf4j-api" % V.slf4j
  val logback = Seq(
    "ch.qos.logback" % "logback-classic" % V.logback,
    "ch.qos.logback" % "logback-core" % V.logback)
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % V.scalaLogging
  val scalaCheck = "org.scalacheck" %% "scalacheck" % V.scalaCheck
  val scalaTest = "org.scalatest" %% "scalatest" % V.scalaTest
  val scopt = "com.github.scopt" %% "scopt" % V.scopt
  val scalaXml = "org.scala-lang.modules" %% "scala-xml" % V.scalaXml
  val jdom = "org.jdom" % "jdom2" % V.jdom
  val betterFiles ="com.github.pathikrit" %% "better-files" % V.betterFiles
  val hashids = "org.hashids" % "hashids" % V.hashids

  def depsCompile(deps: Seq[ModuleID]): Seq[ModuleID] = deps map (_ % "compile")

  def depsTest(deps: Seq[ModuleID]): Seq[ModuleID] = deps map (_ % "test")

  implicit def mod2seq (m: ModuleID) = Seq(m)
}
