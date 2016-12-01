package wrp.smehotron

import java.io.File
import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FileUtils
import wrp.smehotron.utils.Cmd
import wrp.smehotron.utils.PathOps._

import scala.xml.{Elem, XML}

//import scala.collection.JavaConverters._

class Smehotron(val theRoot: Option[Path], cfg: Elem, force: Boolean = false) extends LazyLogging {
  val jarDir = theRoot.get
  lazy val tronDir = jarDir / "schematron"
  lazy val saxonDir = jarDir / "saxon"
  lazy val saxonClasspath = s"${saxonDir}${File.separator}saxon.he.9.7.0.7.jar${File.pathSeparator}${saxonDir}${File.separator}resolver.jar"
  lazy val cats = (cfg \ "catalogs" \ "catalog").map(_.text)

  def processGoModules() =
    (cfg \ "go" \ "module").flatMap { m =>
      val mod = log((m \ "@name").head.text)
      val sch = log((m \ "sch-driver").head.text)
      val icic = m \ "input-controls" \ "input-control"
      val tapt = icic.zipWithIndex.map { icz =>
        validate(sch, icz._1.text) match {
          case Some(svrl) =>
            val rpt = XML.loadFile(svrl)
            val asserts = rpt \\ "failed-assert"
            val reports = rpt \\ "successful-report" // TODO ???: .filter(_ \ "@role" == "error")
            if (asserts.isEmpty && reports.isEmpty)
              tapOk(icz._2 + 1, s"svrl $svrl for ${icz._1.text} with $sch in $mod")
            else
              tapNotOk(icz._2 + 1, s"svrl $svrl for ${icz._1.text} with $sch in $mod has asserts and/or reports") // TODO ???: append asserts/reports
          case None =>
            tapNotOk(icz._2 + 1, s"could not produce svrl for ${icz._1.text} with $sch in $mod")
        }
      }
      tapMeta(s"module: $mod") :: tapHead(icic.size) :: tapt.toList
    }

  def validate(rulesFile: String, docFile: String) =
    for (step1 <- doStep(1, rulesFile, s"$tronDir${File.separator}iso_dsdl_include.xsl");
         step2 <- doStep(2, step1, s"$tronDir${File.separator}iso_abstract_expand.xsl");
         step3 <- doStep(3, step2, s"$tronDir${File.separator}iso_svrl_for_xslt2.xsl");
         step4 <- doStep(4, docFile, step3, ".svrl")
    ) yield step4

  def doStep(num: Int, in: String, xsl: String, suffix: String = "") = {
    val out = if (suffix.size > 0) in + suffix else in.replaceAll("\\.\\d+$", "") + "." + num
    val outf = new File(out)
    // TODO: skip (and log) if out newer than in
    if (outf.exists() && FileUtils.isFileNewer(outf, new File(in)) && !force) {
      logger.debug(s"$out is newer than $in, skipping regeneration...")
      Option(out)
    } else if (Cmd.run(log(mkCmd(in, out, xsl))).succeeded)
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

  private def tapHead(count: Int) = s"1..$count"

  private def tapOk(num: Int, msg: String) = s"ok $num - $msg"

  private def tapNotOk(num: Int, msg: String) = s"not ok $num - $msg"

  private def tapMeta(msg: String) = s"# $msg"

}

case class MainArgs(rules: Option[File] = None,
                    xml: Option[File] = None,
                    cfg: Option[File] = None,
                    root: Option[File] = None,
                    force: Boolean = false)

object Smehotron extends LazyLogging {
  val parser = new scopt.OptionParser[MainArgs]("smehotron") {
    head("smehotron", "1.0.1")

    opt[File]('c', "cfg").minOccurs(1).maxOccurs(1)
      .valueName("<config-file>")
      .action((x, c) => c.copy(cfg = Option(x)))
      .validate(x => if (x.exists() && x.isFile) success else failure("config either does not exist or not a file"))
      .text("config (required)")

    opt[File]('s', "sch").minOccurs(0).maxOccurs(1)
      .valueName("<schematron-rules>")
      .action((x, c) => c.copy(rules = Option(x)))
      .validate(x => if (x.exists() && x.isFile) success else failure("schematron file either does not exist or not a file"))
      .text("schematron file (optional)")

    opt[File]('x', "xml").minOccurs(0).maxOccurs(1)
      .valueName("<xml-file>")
      .action((x, c) => c.copy(xml = Option(x)))
      .validate(x => if (x.exists() && x.isFile) success else failure("xml file either does not exist or not a file"))
      .text("xml file (optional)")

    opt[File]('r', "root").minOccurs(0).maxOccurs(1)
      .valueName("<path-to-file>")
      .action((x, c) => c.copy(root = Option(x)))
      .validate(x => if (x.exists() && x.isDirectory) success else failure("root either does not exist or not a directory"))
      .text("path to root (optional)")

    opt[Unit]('f', "force").minOccurs(0).maxOccurs(1)
      .valueName("<force-compile>")
      .action((x, c) => c.copy(force = true))
      .text("force re–compilation of sch and re-generation of svrl even if they are newer than their sources")

    checkConfig(c =>
      if ((c.xml.isEmpty && c.rules.nonEmpty) || (c.xml.nonEmpty && c.rules.isEmpty))
        failure("xml and sch should either be both defined or both omitted")
      else
        success
    )
  }

  def apply(root: String, cfg: Elem, force: Boolean = false) = new Smehotron(Option(abs(root)), cfg, force)

  def main(args: Array[String]) {
    parser.parse(args, MainArgs()) match {
      case Some(opts) =>
        val root = opts.root match {
          case Some(r) => r.getAbsolutePath
          case None => new File(getClass.getProtectionDomain.getCodeSource.getLocation.toURI).getAbsoluteFile.getParent
        }
        val tron = Smehotron(root, XML.loadFile(opts.cfg.get), opts.force)
        (opts.rules, opts.xml) match {
          case (Some(rules), Some(xml)) =>
            tron.validate(rules.getAbsolutePath, xml.getAbsolutePath)
          case (None, None) =>
            tron.processGoModules().foreach(println)
          case _ => throw new RuntimeException("rules and xml – both or neither")
        }
      case None => /*ignore*/
    }
  }
}
