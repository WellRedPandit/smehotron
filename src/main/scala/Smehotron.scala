package wrp.smehotron

import utils.PathOps._
import java.io.File
import java.nio.file.Path

import ammonite.ops.Shellout._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import eri.commons.config.SSConfig

//import scala.collection.JavaConversions._

class Smehotron (val theRoot: Option[Path], cats: Seq[String]) extends LazyLogging
{
  val jarDir = theRoot.get
  lazy val tronDir = jarDir/"schematron"
  lazy val saxonDir = jarDir/"saxon"
  lazy val saxonClasspath = s"${saxonDir}\\saxon.he.9.7.0.7.jar;${saxonDir}\\resolver.jar"

  def validate(rulesFile: String, docFile: String) =
    for (step1 <- doStep(1, rulesFile, s"$tronDir\\iso_dsdl_include.xsl");
         step2 <- doStep(2, step1, s"$tronDir\\iso_abstract_expand.xsl");
         step3 <- doStep(3, step2, s"$tronDir\\iso_svrl_for_xslt2.xsl");
         step4 <- doStep(4, docFile, step3, ".svrl")
    ) yield step4

  def doStep(num: Int, in: String, xsl: String, suffix: String = "") = {
    val out = if (suffix.size > 0 ) in + suffix
              else in.replaceAll("\\.\\d+$","") + "." + num
    val cmd1 = log(mkCmd(in, out, xsl))
    if(log(%%(cmd1)(ammonite.ops.Path(pwd))).exitCode == 0)
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
  val parser = new scopt.OptionParser[MainArgs]("scopt") {
    head("smehotron", "1.0.1")

    opt[File]('c', "cfg").required()
      .valueName("<config-file>")
      .action( (x, c) => c.copy(cfg = Option(x)) )
      .validate( x => if( x.exists() ) success else failure("config does not exist") )
      .text("config is required")

    opt[File]('s', "sch").required()
      .valueName("<schematron-rules>")
      .action( (x, c) => c.copy(rules = Option(x)) )
      .validate( x => if( x.exists() ) success else failure("schematron file does not exist") )
      .text("schematron file is required")

    opt[File]('x', "xml").required()
      .valueName("<xml-file>")
      .action( (x, c) => c.copy(xml = Option(x)) )
      .validate( x => if( x.exists() ) success else failure("xml file does not exist") )
      .text("xml file is required")
  }

  def apply(root:String, cats: Seq[String]) =  new Smehotron(Option(abs(root)), cats)

  def main(args: Array[String]) {
    val jar = new File(getClass.getProtectionDomain.getCodeSource.getLocation.toURI)
    parser.parse(args, MainArgs()) match {
      case Some(opts) =>
        val cfg = new SSConfig(ConfigFactory.parseFile(opts.cfg.get).resolve())
        val cats = cfg.catalogs.as[Seq[String]]
        val tron = Smehotron(jar.getParent, cats)
        tron.validate(opts.rules.get.getAbsolutePath, opts.xml.get.getAbsolutePath)
      case None => /*ignore*/
    }
  }
}
