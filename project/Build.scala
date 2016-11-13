import sbt._
import sbt.Keys._
import sbtassembly.AssemblyPlugin.autoImport._

object TronBuild extends Build {

  import Deps._

  val repos = Seq(
    "local maven" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    "ERI OSS" at "http://dl.bintray.com/elderresearch/OSS")

    lazy val sharedScalacSettings = Seq(
    "-encoding", "UTF-8"
    , "-g:vars"
    , "-feature"
    , "-unchecked"
    , "-deprecation"
    , "-target:jvm-1.7"
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
      resolvers ++= repos,
      shellPrompt := ShellPrompt.prompt
    )

    object ShellPrompt {
      val prompt =
        (state: State) => "%s> ".format(Project.extract(state).currentProject.id)
    }

  lazy val tron = Project(id = "smehotron", base = file("."))
    .settings(scalacOptions := sharedScalacSettings)
    .settings(commonProjectSettings: _*)
    .settings(name := "smehotron",
      libraryDependencies ++=
        compile(ammoniteOps ++ config ++ slf4j ++ logback ++ scalaLogging ++ commonsIo ++ scopt ++ ssc) ++
        test(ammoniteRepl ++ scalaCheck ++ scalaTest))
    .settings(//test in assembly := {},
      assemblyJarName in assembly := "smehotron.jar",
      mainClass in assembly := Some("wrp.smehotron.Smehotron"),
      initialCommands in (Test, console) := """import wrp.smehotron._"""
//      ,initialCommands in (Test, console) := """ammonite.Main().run()"""
    )
    .settings(
      TaskKey[Unit]("dist") := {
        import scala.collection.JavaConversions._
        import java.nio.file._
        import java.nio.file.attribute.PosixFilePermission._
        sbt.IO.delete(new File("dist"))
        sbt.IO.copyFile(new File("target/scala-2.11/smehotron.jar"), new File("dist/smehotron.jar"), true)
        sbt.IO.copyFile(new File("scripts/smehotron.sh"), new File("dist/smehotron.sh"), true)
        Files.setPosixFilePermissions(Paths.get("dist/smehotron.sh"),
          Set(OWNER_EXECUTE, OWNER_READ, OWNER_WRITE))
        sbt.IO.copyDirectory(new File("saxon"),new File("dist/saxon"),false,true)
        sbt.IO.copyDirectory(new File("schematron"),new File("dist/schematron"),false,true)
      }
    )
}
