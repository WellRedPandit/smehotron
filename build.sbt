import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._
import Dependencies._

lazy val sharedScalacSettings = Seq(
  "-encoding", "UTF-8"
  , "-feature"
  , "-unchecked"
  , "-deprecation"
)

val prompt = (state: State) => "%s> ".format(Project.extract(state).currentProject.id)

val commonProjectSettings =
  Defaults.coreDefaultSettings ++
    Seq(
      organization := "wrp",
      version := V.tron,
      scalaVersion := V.scala,
      scalacOptions := sharedScalacSettings,
      shellPrompt := prompt
    )


lazy val dist = TaskKey[Unit]("dist")

lazy val tron = Project(id = "smehotron", base = file("."))
  .settings(scalacOptions := sharedScalacSettings)
  .settings(commonProjectSettings: _*)
  .settings(name := "smehotron",
    libraryDependencies ++=
      depsCompile(typesafeConfig ++ slf4j ++ logback ++ scalaLogging ++ commonsIo ++ scopt ++ scalaXml ++ jdom ++ hashids) ++
        depsTest(scalaCheck ++ scalaTest),
    Test / console / initialCommands := """import wrp.smehotron._""",
    console / initialCommands :=
      """
        |import wrp.smehotron._
        |import scala.concurrent.ExecutionContext.Implicits.global
        |import scala.util._
      """.stripMargin)
  // assembly, dist
  .settings(assembly / assemblyJarName := "smehotron.jar",
    assembly / mainClass := Some("wrp.smehotron.Main"),
    assembly / sbt.Keys.test := {},
    dist := {
      import java.nio.file._
      import java.nio.file.attribute.PosixFilePermission._
      import scala.jdk.CollectionConverters._
      import sys.process._

      val releaseDir = s"smehotron-${V.tron}"
      sbt.IO.delete(new File(releaseDir))
      sbt.IO.delete(new File(s"$releaseDir.zip"))
      sbt.IO.copyFile(new File("target/scala-2.13/smehotron.jar"), new File(s"$releaseDir/smehotron.jar"), true)
      sbt.IO.copyFile(new File("scripts/smehotron"), new File(s"$releaseDir/smehotron"), true)
      sbt.IO.copyFile(new File("scripts/smehotron.bat"), new File(s"$releaseDir/smehotron.bat"), true)
      scala.util.Try(Files.setPosixFilePermissions(Paths.get(s"$releaseDir/smehotron"), Set(OWNER_EXECUTE, OWNER_READ, OWNER_WRITE).asJava))
      sbt.IO.copyDirectory(new File("saxon"), new File(s"$releaseDir/saxon"), false, true)
      sbt.IO.copyDirectory(new File("schematron"), new File(s"$releaseDir/schematron"), false, true)
      s"zip -r $releaseDir.zip $releaseDir" !
    },
    dist := dist.dependsOn(assembly).value)
