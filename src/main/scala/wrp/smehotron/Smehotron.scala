package wrp.smehotron

import java.io.File
import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import wrp.smehotron.utils.Cmd
import wrp.smehotron.utils.PathOps._

import scala.xml.{Elem, XML}

//import scala.collection.JavaConverters._

class Smehotron (val theRoot: Option[Path], cats: Seq[String]) extends LazyLogging
{
  val jarDir = theRoot.get
  lazy val tronDir = jarDir/"schematron"
  lazy val saxonDir = jarDir/"saxon"
  lazy val saxonClasspath = s"${saxonDir}${File.separator}saxon.he.9.7.0.7.jar${File.pathSeparator}${saxonDir}${File.separator}resolver.jar"

  // TODO: implement
  def validate(cfg: Elem) = ???

  def validate(rulesFile: String, docFile: String) =
    for (step1 <- doStep(1, rulesFile, s"$tronDir${File.separator}iso_dsdl_include.xsl");
         step2 <- doStep(2, step1, s"$tronDir${File.separator}iso_abstract_expand.xsl");
         step3 <- doStep(3, step2, s"$tronDir${File.separator}iso_svrl_for_xslt2.xsl");
         step4 <- doStep(4, docFile, step3, ".svrl")
    ) yield step4

  def doStep(num: Int, in: String, xsl: String, suffix: String = "") = {
    val out = if (suffix.size > 0 ) in + suffix
              else in.replaceAll("\\.\\d+$","") + "." + num
    // TODO: skip (and log) if out newer than in
    val cmd1 = log(mkCmd(in, out, xsl))
    if(Cmd.run(cmd1).succeeded)
      Option(out)
    else
      None
  }

  def mkCmd(xmlIn: String, xmlOut: String, xsl: String) =
    Vector("java",
      "-cp", saxonClasspath,
      "net.sf.saxon.Transform",
      "-versionmsg:off",
      "allow-foreign=true",
      s"-catalog:${cats.mkString(";")}",
      s"-s:$xmlIn",
      s"-o:$xmlOut",
      s"-xsl:$xsl")

  private def log[T](value: T, f: T=>String = { x: T => x.toString}) = {
    logger.debug(f(value))
    value
  }

}

case class MainArgs(rules: Option[File] = None, xml: Option[File] = None, cfg: Option[File] = None)

object Smehotron extends LazyLogging {
  val parser = new scopt.OptionParser[MainArgs]("smehotron") {
    head("smehotron", "1.0.1")

    opt[File]('c', "cfg").minOccurs(1).maxOccurs(1)
      .valueName("<config-file>")
      .action( (x, c) => c.copy(cfg = Option(x)) )
      .validate( x => if( x.exists() ) success else failure("config does not exist") )
      .text("config (required)")

    opt[File]('s', "sch").minOccurs(0).maxOccurs(1)
      .valueName("<schematron-rules>")
      .action( (x, c) => c.copy(rules = Option(x)) )
      .validate( x => if( x.exists() ) success else failure("schematron file does not exist") )
      .text("schematron file (optional)")

    opt[File]('x', "xml").minOccurs(0).maxOccurs(1)
      .valueName("<xml-file>")
      .action( (x, c) => c.copy(xml = Option(x)) )
      .validate( x => if( x.exists() ) success else failure("xml file does not exist") )
      .text("xml file (optional)")

    checkConfig( c =>
      if( (c.xml.isEmpty && c.rules.nonEmpty) || (c.xml.nonEmpty && c.rules.isEmpty) )
        failure("xml and sch should either be both defined or both omitted")
      else
        success
    )
  }

  def apply(root:String, cats: Seq[String]) =  new Smehotron(Option(abs(root)), cats)

  def main(args: Array[String]) {
    val jar = new File(getClass.getProtectionDomain.getCodeSource.getLocation.toURI)
    parser.parse(args, MainArgs()) match {
      case Some(opts) =>
        val cfg = XML.loadFile(opts.cfg.get)
        val cats = (cfg \\ "catalog").map(_.text)
        val tron = Smehotron(jar.getParent, cats)
        (opts.rules, opts.xml) match {
          case (Some(rules), Some(xml)) =>
            tron.validate(rules.getAbsolutePath, xml.getAbsolutePath)
          case (None, None) =>
            tron.validate(cfg)
          case _ => throw new RuntimeException("rules and xml – both or neither")
        }
      case None => /*ignore*/
    }
  }
}
