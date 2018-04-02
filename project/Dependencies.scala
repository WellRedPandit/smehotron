import sbt._

object V {
  val betterFiles = "3.1.0"
  val cats = "0.9.0"
  val commonsIo = "2.6"
  val typesafeConfig = "1.3.3"
  val hashids = "1.0.3"
  val jdom = "2.0.6"
  val logback = "1.2.3"
  val scala = "2.12.5"
  val scalaCheck = "1.13.5"
  val scalaLogging = "3.8.0"
  val scalaTest = "3.0.5"
  val scalaXml = "1.1.0"
  val scopt = "3.7.0"
  val slf4j = "1.7.25"
  val tron = "1.11.0"
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
  val cats = "org.typelevel" %% "cats" % V.cats
  val jdom = "org.jdom" % "jdom2" % V.jdom
  val betterFiles ="com.github.pathikrit" %% "better-files" % V.betterFiles
  val hashids = "org.hashids" % "hashids" % V.hashids

  def depsCompile(deps: Seq[ModuleID]): Seq[ModuleID] = deps map (_ % "compile")

  def depsTest(deps: Seq[ModuleID]): Seq[ModuleID] = deps map (_ % "test")

  implicit def mod2seq (m: ModuleID) = Seq(m)
}
