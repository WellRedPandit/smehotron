import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

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
    , "-Xlint"
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
      assemblyJarName in assembly := "smehotron.jar",
      mainClass in assembly := Some("wrp.smehotron.Smehotron"),
      initialCommands in(Test, console) := """import wrp.smehotron._""",
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
      dist <<= dist.dependsOn(assembly),
      initialCommands in console :=
        """
          |import wrp.smehotron._
          |import scala.concurrent.ExecutionContext.Implicits.global
          |import scala.util._
          | """.stripMargin
    )
}
