package wrp.smehotron

import java.io.File
import java.nio.file.Path

import scala.io.Source
import ch.qos.logback.classic.{Level, LoggerContext}
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import wrp.smehotron.utils.Cmd
import wrp.smehotron.utils.PathOps._

import scala.xml.{Elem, NodeSeq, XML}

//import scala.collection.JavaConverters._

class Smehotron(val theRoot: Option[Path], cfg: Elem = <smehotron/>) extends LazyLogging {
  val jarDir = theRoot.get
  lazy val tronDir = jarDir / "schematron"
  lazy val saxonDir = jarDir / "saxon"
  lazy val saxonClasspath = s"${saxonDir}${File.separator}saxon.he.9.7.0.7.jar${File.pathSeparator}${saxonDir}${File.separator}resolver.jar"
  lazy val cats = (cfg \ "catalogs" \ "catalog").map(_.text)

  def processModules() = {
    val go = processGoModules()
    val nogo = processNogoModules()
    <smehotron-results>{go}{nogo}</smehotron-results>
  }

  def processGoModules() = {
    val go = (cfg \ "go" \ "module").flatMap { m =>
      val mod = log((m \ "@name").head.text)
      val sch = log((m \ "sch-driver").head.text)
      compile(sch) match {
        case Some(step3) => {
          val icic = m \ "input-controls" \ "input-control"
          val tapt = icic.map { icz =>
            val ic = icz.text.trim
            validate(step3, ic) match {
              case Some(svrl) =>
                val rpt = XML.loadFile(svrl)
                val asserts = rpt \\ "failed-assert"
                val reports = rpt \\ "successful-report"
                if (asserts.isEmpty && reports.isEmpty)
                  tapOk(svrl, ic, sch, mod)
                else
                  tapAssertsReports(svrl, ic, sch, mod, asserts, reports)
              case None =>
                tapSvrlFailed(ic, sch, mod)
            }
          }
          tapt.toList
        }
        case None =>
          tapCompilationFailed(sch, mod)
      }
    }
    <go>{go}</go>
  }

  def processNogoModules() = {
    val nogo = (cfg \ "nogo" \ "module").flatMap { m =>
      val mod = log((m \ "@name").head.text)
      val sch = log((m \ "sch-driver").head.text)
      compile(sch) match {
        case Some(step3) => {
          val icic = m \ "input-controls" \ "input-control" \ "source"
          val golden = (m \ "input-controls" \ "input-control" \ "golden").text.trim
          val tapt = icic.map { icz =>
            val ic = icz.text.trim
            validate(step3, ic) match {
              case Some(svrl) =>
                val suspect = Source.fromFile(svrl).getLines().mkString("\n")
                val yardstick = Source.fromFile(golden).getLines().mkString("\n")
                tapNogoResult(svrl, ic, golden, sch, mod, suspect == yardstick)
              case None =>
                tapSvrlFailed(ic, sch, mod)
            }
          }
          tapt.toList
        }
        case None =>
          tapCompilationFailed(sch, mod)
      }
    }
    <nogo>{nogo}</nogo>
  }
  def generateNogoGold() =
    (cfg \ "nogo" \ "module").flatMap { m =>
      val mod = log((m \ "@name").head.text)
      val sch = log((m \ "sch-driver").head.text)
      compile(sch) match {
        case Some(step3) => {
          val icic = m \ "input-controls" \ "input-control" \ "source"
          val golden = (m \ "input-controls" \ "input-control" \ "golden").text.trim
          val tapt = icic.map { icz =>
            val ic = icz.text.trim
            validate(step3, ic) match {
              case Some(svrl) =>
                FileUtils.moveFile(FileUtils.getFile(svrl),FileUtils.getFile(golden))
              case None =>
                throw new RuntimeException(s"could not produce svrl: module: $mod; sch-driver: $sch; input-control: $ic")
            }
          }
          tapt.toList
        }
        case None =>
          throw new RuntimeException(s"could not produce svrl: module: $mod; sch-driver: $sch")
      }
    }


  def validate(step3: String, docFile: String) = doStep(4, docFile, step3, ".svrl")

  def compile(rulesFile: String) =
    for (step1 <- doStep(1, rulesFile, s"$tronDir${File.separator}iso_dsdl_include.xsl");
         step2 <- doStep(2, step1, s"$tronDir${File.separator}iso_abstract_expand.xsl");
         step3 <- doStep(3, step2, s"$tronDir${File.separator}iso_svrl_for_xslt2.xsl")
    ) yield step3

  def doStep(num: Int, in: String, xsl: String, suffix: String = "") = {
    val out = if (suffix.size > 0) in + suffix else in.replaceAll("\\.\\d+$", "") + "." + num
    if (Cmd.run(log(mkCmd(in, out, xsl))).succeeded)
      Option(out)
    else
      None
  }

  def mkCmd(xmlIn: String, xmlOut: String, xsl: String) = {
    val base = Vector("java",
      "-cp", saxonClasspath,
      "net.sf.saxon.Transform",
      "-versionmsg:off",
      "allow-foreign=true",
      s"-s:$xmlIn",
      s"-o:$xmlOut",
      s"-xsl:$xsl")
    if (cats.isEmpty)
      base
    else
      base ++ Vector(s"-catalog:${cats.mkString(";")}")
  }

  private def log[T](value: T, f: T => String = { x: T => x.toString }) = {
    logger.debug(f(value))
    value
  }

  private def tapOk(svrl: String, inputControl: String, rules: String, module: String) =
    <test status="success">
      <module>{module}</module>
      <sch-driver>{rules}</sch-driver>
      <input-control>{inputControl}</input-control>
      <svrl>{svrl}</svrl>
    </test>

  private def tapNogoResult(svrl: String,
                            inputControl: String,
                            golden: String,
                            rules: String,
                            module: String,
                            success: Boolean) = {
    val outcome = if(success) "success" else "failure"
    <test status={outcome}>
      <module>{module}</module>
      <sch-driver>{rules}</sch-driver>
      <input-control>{inputControl}</input-control>
      <golden>{golden}</golden>
      <svrl>{svrl}</svrl>
    </test>
  }


  private def tapAssertsReports(svrl: String,
                               inputControl: String,
                               rules: String,
                               module: String,
                               asserts: NodeSeq,
                               reports: NodeSeq) =
    <test status="failure">
      <module>{module}</module>
      <sch-driver>{rules}</sch-driver>
      <input-control>{inputControl}</input-control>
      <svrl>{svrl}</svrl>
      <asserts>{asserts}</asserts>
      <reports>{reports}</reports>
    </test>

  private def tapSvrlFailed(inputControl: String, rules: String, module: String) =
    <test status="failure">
      <module>{module}</module>
      <sch-driver>{rules}</sch-driver>
      <input-control>{inputControl}</input-control>
      <reason>could not produce svrl</reason>
    </test>


  private def tapCompilationFailed(rules: String, module: String) =
    <test status="failure">
      <module>{module}</module>
      <sch-driver>{rules}</sch-driver>
      <reason>could not compile</reason>
    </test>

}

case class MainArgs(rules: Option[File] = None,
                    xml: Option[File] = None,
                    cfg: Option[File] = None,
                    root: Option[File] = None,
                    logLevel: Level = Level.ERROR,
                    generate: Boolean = false)

object Smehotron extends LazyLogging {
  val parser = new scopt.OptionParser[MainArgs]("smehotron") {
    head("smehotron", "1.0.4")

    opt[File]('c', "cfg").minOccurs(0).maxOccurs(1)
      .valueName("<config-file>")
      .action((x, c) => c.copy(cfg = Option(x)))
      .validate(x => if (x.exists() && x.isFile) success else failure("config either does not exist or not a file"))
      .text("config (optional)")

    opt[File]('s', "sch").minOccurs(0).maxOccurs(1)
      .valueName("<sch-driver>")
      .action((x, c) => c.copy(rules = Option(x)))
      .validate(x => if (x.exists() && x.isFile) success else failure("schematron file either does not exist or not a file"))
      .text("schematron file (optional)")

    opt[File]('x', "xml").minOccurs(0).maxOccurs(1)
      .valueName("<xml-file>")
      .action((x, c) => c.copy(xml = Option(x)))
      .validate(x => if (x.exists() && x.isFile) success else failure("xml file either does not exist or not a file"))
      .text("xml file (optional)")

    opt[File]('r', "root").minOccurs(0).maxOccurs(1)
      .valueName("<path/to/dir>")
      .action((x, c) => c.copy(root = Option(x)))
      .validate(x => if (x.exists() && x.isDirectory) success else failure("root either does not exist or not a directory"))
      .text("path to a root dir (optional)")

    opt[String]('g', "generate").minOccurs(0).maxOccurs(1)
      .valueName("<generate>")
      .validate(x =>
        if (Set("YES", "NO").contains(x.toUpperCase)) success else failure("generate accepts yes or no"))
      .action((x, c) => c.copy(generate = if(x.toUpperCase == "YES") true else false))
      .text("generate godlen SVRLs")

    opt[String]('l', "loglevel").minOccurs(0).maxOccurs(1)
      .valueName("<log level>")
      .validate(x =>
        if (Set("OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "ALL").contains(x.toUpperCase)) success else failure("bad log level"))
      .action((x, c) => c.copy(logLevel = Level.toLevel(x.toUpperCase)))
      .text("log level (case insensitive): OFF, ERROR (default), WARN, INFO, DEBUG, TRACE, ALL")

    checkConfig(c =>
      if ((c.xml.isEmpty && c.rules.nonEmpty) || (c.xml.nonEmpty && c.rules.isEmpty))
        failure("xml and sch should either be both defined or both omitted")
      else if (c.cfg.isEmpty && c.rules.isEmpty && c.xml.isEmpty)
        failure("no parameters supplied")
      else
        success
    )
  }

  def apply(root: String, cfg: Elem = <smehotron/>) = new Smehotron(Option(abs(root)), cfg)

  private def mkConfigForRulesXmlPair(rules: File, xml: File) = {
    val r = rules.getAbsolutePath
    val x = xml.getAbsolutePath
    <smehotron>
      <go>
        <module name="_phony_">
          <sch-driver>{r}</sch-driver>
          <input-controls>
            <input-control>{x}</input-control>
          </input-controls>
        </module>
      </go>
    </smehotron>
  }

  def main(args: Array[String]) {
    parser.parse(args, MainArgs()) match {
      case Some(opts) =>
        LoggerFactory.getILoggerFactory().asInstanceOf[LoggerContext].getLogger("wrp.smehotron").setLevel(opts.logLevel)
        val root = opts.root match {
          case Some(r) => r.getAbsolutePath
          case None => new File(getClass.getProtectionDomain.getCodeSource.getLocation.toURI).getAbsoluteFile.getParent
        }
        val conf = opts.cfg match {
          case Some(c) => XML.loadFile(c)
          case None =>
            (opts.rules, opts.xml) match {
              case (Some(rules), Some(xml)) => mkConfigForRulesXmlPair(rules, xml)
              case _ => throw new RuntimeException("sch-driver and xml – both or neither")
            }
        }
        if(opts.generate)
          Smehotron(root, conf).generateNogoGold()
        else
          Smehotron(root, conf).processModules().foreach(println)
      case None => /*ignore*/
    }
  }
}
