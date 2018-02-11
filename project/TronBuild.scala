import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

object V {
  val betterFiles = "3.1.0"
  val cats = "0.9.0"
  val commonsIo = "2.6"
  val config = "1.3.2"
  val hashids = "1.0.3"
  val jdom = "2.0.6"
  val logback = "1.2.3"
  val scala = "2.12.4"
  val scalaCheck = "1.13.5"
  val scalaLogging = "3.7.2"
  val scalaTest = "3.0.5"
  val scalaXml = "1.0.6"
  val scopt = "3.7.0"
  val slf4j = "1.7.25"
  val tron = "1.11.0"
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
  val betterFiles ="com.github.pathikrit" %% "better-files" % V.betterFiles
  val hashids = "org.hashids" % "hashids" % V.hashids

  def compile(deps: Seq[ModuleID]): Seq[ModuleID] = deps map (_ % "compile")

  def test(deps: Seq[ModuleID]): Seq[ModuleID] = deps map (_ % "test")

  implicit def mod2seq (m: ModuleID) = Seq(m)
}

object TronBuild extends Build {

  import Deps._

  lazy val sharedScalacSettings = Seq(
    "-encoding", "UTF-8"
    , "-g:vars"
    , "-feature"
    , "-unchecked"
    , "-deprecation"
    , "-target:jvm-1.8"
    , "-Xlog-reflective-calls"
    //, "-Xlint"
    , "-Ywarn-unused:-imports"
    , "-Yno-adapted-args"
    , "-Ywarn-value-discard"
    //"-Xfatal-warnings" // be good!
  )

  val commonProjectSettings =
    Defaults.coreDefaultSettings ++
    Seq(
      organization := "wrp",
      version := V.tron,
      scalaVersion := V.scala,
      scalacOptions := sharedScalacSettings,
      shellPrompt := ShellPrompt.prompt
    )

  object ShellPrompt {
    val prompt =
      (state: State) => "%s> ".format(Project.extract(state).currentProject.id)
  }

  lazy val dist = TaskKey[Unit]("dist")

  lazy val tron = Project(id = "smehotron", base = file("."))
    .settings(scalacOptions := sharedScalacSettings)
    .settings(commonProjectSettings: _*)
    .settings(name := "smehotron",
      libraryDependencies ++=
        compile(config ++ slf4j ++ logback ++ cats ++ scalaLogging ++ commonsIo ++ scopt ++ scalaXml ++ jdom ++ hashids) ++
        test(scalaCheck ++ scalaTest),
      initialCommands in(Test, console) := """import wrp.smehotron._""",
      initialCommands in console :=
        """
          |import wrp.smehotron._
          |import scala.concurrent.ExecutionContext.Implicits.global
          |import scala.util._
        """.stripMargin)
      // assembly, dist
    .settings(assemblyJarName in assembly := "smehotron.jar",
      mainClass in assembly := Some("wrp.smehotron.Main"),
      sbt.Keys.test in assembly := {},
      dist := {
        import java.nio.file._
        import java.nio.file.attribute.PosixFilePermission._
        import scala.collection.JavaConversions._
        val releaseDir = s"smehotron-${V.tron}"
        sbt.IO.delete(new File(releaseDir))
        sbt.IO.delete(new File(s"$releaseDir.zip"))
        sbt.IO.copyFile(new File("target/scala-2.12/smehotron.jar"), new File(s"$releaseDir/smehotron.jar"), true)
        sbt.IO.copyFile(new File("scripts/smehotron"), new File(s"$releaseDir/smehotron"), true)
        sbt.IO.copyFile(new File("scripts/smehotron.bat"), new File(s"$releaseDir/smehotron.bat"), true)
        scala.util.Try(Files.setPosixFilePermissions(Paths.get(s"$releaseDir/smehotron"), Set(OWNER_EXECUTE, OWNER_READ, OWNER_WRITE)))
        sbt.IO.copyDirectory(new File("saxon"), new File(s"$releaseDir/saxon"), false, true)
        sbt.IO.copyDirectory(new File("schematron"), new File(s"$releaseDir/schematron"), false, true)
        s"zip -r $releaseDir.zip $releaseDir" !
      },
      dist <<= dist.dependsOn(assembly)
    )
}
