import sbt._

object V {
  val cats = "0.9.0"
  val commonsIo = "2.5"
  val config = "1.3.1"
  val jdom = "2.0.6"
  val hashids = "1.0.1"
  val logback = "1.2.3"
  val scala = "2.12.1"
  val scalaCheck = "1.13.5"
  val scalaLogging = "3.5.0"
  val scalaTest = "3.0.1"
  val scalaXml = "1.0.6"
  val scopt = "3.5.0"
  val slf4j = "1.7.25"
  val tron = "1.0.8"
}

object Deps {
  val commonsIo = "commons-io" % "commons-io" % V.commonsIo
  val config = "com.typesafe" % "config" % V.config
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
  val hashids = "org.hashids" % "hashids" % V.hashids

  def compile(deps: Seq[ModuleID]): Seq[ModuleID] = deps map (_ % "compile")

  def test(deps: Seq[ModuleID]): Seq[ModuleID] = deps map (_ % "test")

  implicit def mod2seq (m: ModuleID) = Seq(m)
}
