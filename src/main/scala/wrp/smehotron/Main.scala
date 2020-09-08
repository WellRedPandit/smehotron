package wrp.smehotron

import java.io.File

import ch.qos.logback.classic.{Level, LoggerContext}
import com.typesafe.scalalogging.LazyLogging
import org.jdom2.Element
import org.jdom2.input.SAXBuilder
import org.jdom2.output.{Format, XMLOutputter}
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.xml.XML


case class MainArgs(cfg: Option[File] = None,
                    root: Option[File] = None,
                    logLevel: Level = Level.ERROR,
                    generate: Boolean = false,
                    keep: Boolean = false)

object Main extends LazyLogging {

  val parser = new scopt.OptionParser[MainArgs]("smehotron") {
    head("smehotron", this.getClass().getPackage().getImplementationVersion())

    opt[File]('c', "cfg").minOccurs(1).maxOccurs(1)
      .valueName("<config-file>")
      .action((x, c) => c.copy(cfg = Option(x)))
      .validate(x => if (x.exists() && x.isFile) success else failure("config either does not exist or not a file"))
      .text("config (optional)")

    opt[File]('r', "root").minOccurs(0).maxOccurs(1)
      .valueName("<path/to/dir>")
      .action((x, c) => c.copy(root = Option(x)))
      .validate(x => if (x.exists() && x.isDirectory) success else failure("root either does not exist or not a directory"))
      .text("path to a root dir (optional)")

    opt[Unit]('g', "generate").minOccurs(0).maxOccurs(1)
      .valueName("<generate>")
      .action((_, c) => c.copy(generate = true))
      .text("generate expected SVRLs")

    opt[Unit]('k', "keep").minOccurs(0).maxOccurs(1)
      .valueName("<keep>")
      .action((_, c) => c.copy(keep = true))
      .text("keep intermediate files")

    opt[String]('l', "loglevel").minOccurs(0).maxOccurs(1)
      .valueName("<log level>")
      .validate(x =>
        if (Set("OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "ALL").contains(x.toUpperCase)) success else failure("bad log level"))
      .action((x, c) => c.copy(logLevel = Level.toLevel(x.toUpperCase)))
      .text("log level (case insensitive): OFF, ERROR (default), WARN, INFO, DEBUG, TRACE, ALL")
  }

  private def mkConfigForRulesXmlPair(rules: File, xml: File) = {
    val r = rules.getAbsolutePath
    val x = xml.getAbsolutePath
    <smehotron>
      <go>
        <module name="_phony_">
          <sch-driver>
            {r}
          </sch-driver>
          <input-controls>
            <input-control>
              <source>
                {x}
              </source>
            </input-control>
          </input-controls>
        </module>
      </go>
    </smehotron>
  }

  @tailrec
  def findBase(e: Element): Option[String] = {
    val base = Option(e.getAttributeValue("base"))
    if (base.nonEmpty) base
    else {
      if (e.isRootElement) None
      else findBase(e.getParentElement)
    }
  }

  def resolveBase(f: File, sep: String = File.separator): String = {
    val resolvables = Set("catalog", "sch-driver", "source", "expected-svrl")
    val sax = new SAXBuilder()
    val doc = sax.build(f)
    // http://stackoverflow.com/a/23870306
    val rx = if (sep == "/") "\\\\+" else "/+"
    val sub = if (sep == "/") "/" else "\\\\"

    def _resolveBase(e: Element): Unit = {
      for (e <- e.getChildren.asScala) {
        if (resolvables.contains(e.getName)) {
          val base = findBase(e)
          if (base.nonEmpty) {
            val newPath = (base.get + sep + e.getText).replaceAll(rx, sub)
            e.setText(newPath)
          } else {
            e.setText(e.getText.replaceAll(rx, sub))
          }
        }
        _resolveBase(e)
      }
    }

    _resolveBase(doc.getRootElement)
    val xout = new XMLOutputter(Format.getPrettyFormat())
    xout.outputString(doc)
  }

  def main(args: Array[String]): Unit = {
    wrp.smehotron.utils.WrpXmlDiff.compare()
    wrp.smehotron.utils.WrpXmlDiff.diff()
    parser.parse(args, MainArgs()) match {
      case Some(opts) =>
        LoggerFactory.getILoggerFactory().asInstanceOf[LoggerContext].getLogger("wrp.smehotron").setLevel(opts.logLevel)
        val root = opts.root match {
          case Some(r) => r.getAbsolutePath
          case None => new File(getClass.getProtectionDomain.getCodeSource.getLocation.toURI).getAbsoluteFile.getParent
        }
        val conf = XML.loadString(resolveBase(opts.cfg.get))
        if (opts.generate)
          Smehotron(root, conf, opts.keep).generateNogoExpectedSvrls().foreach(println)
        else
          Smehotron(root, conf, opts.keep).processModules().foreach(println)
      case None => /*ignore*/
    }
  }
}
