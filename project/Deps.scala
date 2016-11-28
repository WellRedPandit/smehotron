import sbt._

object V {
  val config = "1.3.1"
  val logback = "1.1.7"
  val scala = "2.11.8"
  val scalaCheck = "1.13.4"
  val scalaLogging = "3.5.0"
  val scalaTest = "3.0.1"
  val slf4j = "1.7.21"
  val scopt = "3.5.0"
  val ssc = "1.0.0"
  val tron = "1.0.1"
  val commonsIo = "2.5"
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
  val ssc = "com.elderresearch" %% "ssc" % V.ssc

  def compile(deps: Seq[ModuleID]): Seq[ModuleID] = deps map (_ % "compile")

  def test(deps: Seq[ModuleID]): Seq[ModuleID] = deps map (_ % "test")

  implicit def mod2seq (m: ModuleID) = Seq(m)
}
