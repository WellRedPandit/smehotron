package wrp.smehotron

import java.io.File
import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FileUtils
import wrp.smehotron.Tap._
import wrp.smehotron.utils.Cmd
import wrp.smehotron.utils.PathOps._

import scala.io.Source
import scala.util.{Failure, Try}
import scala.xml.{Elem, NodeSeq, XML}

object Smehotron {
  def apply(root: String, cfg: Elem = <smehotron/>, keep: Boolean = false) = new Smehotron(Option(abs(root)), cfg, keep)
}

class Smehotron(val theRoot: Option[Path], cfg: Elem = <smehotron/>, keep: Boolean = false) extends LazyLogging {
  val jarDir = theRoot.get
  lazy val tronDir = jarDir / "schematron"
  lazy val saxonDir = jarDir / "saxon"
  lazy val saxonClasspath = s"${saxonDir}${File.separator}saxon.he.9.7.0.7.jar${File.pathSeparator}${saxonDir}${File.separator}resolver.jar"
  lazy val cats = (cfg \ "catalogs" \ "catalog").map(_.text)
  val gashish = new org.hashids.Hashids("smehotron")

  def processModules() = {
    val goms = processGoModules()
    val nogoms = processNogoModules()
    val go = if (goms.nonEmpty) tapGo(goms) else NodeSeq.Empty
    val nogo = if (nogoms.nonEmpty) tapNogo(nogoms) else NodeSeq.Empty
    tapResults({
      go ++ nogo
    })
  }

  def processGoModules() =
    (cfg \ "go" \ "module").flatMap { m =>
      val mod = log((m \ "@name").head.text)
      val drvs = log((m \ "sch-drivers" \ "sch-driver") ++ (m \ "sch-driver"))
      drvs.map { drv =>
        val sch = drv.text
        val chain = Chain(keep)
        val res = compile(chain, sch) match {
          case Some(compiled) => {
            val icic = m \ "input-controls" \ "input-control" \ "source"
            val tapt = icic.map { icz =>
              val ic = icz.text.trim
              validate(chain, compiled, ic) match {
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
        chain.clean()
        res
      }
    }

  def processNogoModules() =
    (cfg \ "nogo" \ "module").flatMap { m =>
      val mod = log((m \ "@name").head.text)
      val drvs = log((m \ "sch-drivers" \ "sch-driver") ++ (m \ "sch-driver"))
      drvs.map { drv =>
        val sch = drv.text
        val chain = Chain(keep)
        val res = compile(chain, sch) match {
          case Some(compiled) => {
            val icic = m \ "input-controls" \ "input-control"
            val tapt = icic.map { icz =>
              val src = (icz \ "source").text.trim
              val expected = (icz \ "expected-svrl").text.trim
              validate(chain, compiled, src) match {
                case Some(svrl) =>
                  val suspect = Try(Source.fromFile(svrl).getLines().mkString("\n"))
                  val yardstick = Try(Source.fromFile(expected).getLines().mkString("\n"))
                  if (suspect.isSuccess && yardstick.isSuccess)
                    tapNogoResult(svrl, src, expected, sch, mod, suspect.get == yardstick.get)
                  else
                    tapNotFound(svrl, src, expected, sch, mod, List(suspect, yardstick).collect { case Failure(x) => x.getMessage })
                case None =>
                  tapSvrlFailed(src, sch, mod)
              }
            }
            tapt.toList
          }
          case None =>
            tapCompilationFailed(sch, mod)
        }
        chain.clean()
        res
      }
    }

  def generateNogoExpectedSvrls() = {
    val outcomes = (cfg \ "nogo" \ "module").flatMap { m =>
      val mod = log((m \ "@name").head.text)
      val drvs = log((m \ "sch-drivers" \ "sch-driver") ++ (m \ "sch-driver"))
      drvs.map { drv =>
        val sch = drv.text
        val chain = Chain(keep)
        val res = compile(chain, sch) match {
          case Some(compiled) => {
            val icic = m \ "input-controls" \ "input-control"
            val tapt = icic.map { icz =>
              val ic = (icz \ "source").text.trim
              val expected = (icz \ "expected-svrl").text.trim
              validate(chain, compiled, ic) match {
                case Some(svrl) =>
                  try {
                    FileUtils.moveFile(FileUtils.getFile(svrl), FileUtils.getFile(expected))
                    tapOutcomeSuccess(mod, sch, ic, expected)
                  } catch {
                    case x: Throwable =>
                      tapOutcomeMoveFailure(mod, sch, ic, expected, x.getMessage)
                  }
                case None =>
                  tapOutcomeSvlGenFailure(mod, sch, ic)
              }
            }
            tapt.toList
          }
          case None =>
            tapOutcomeCompileFailure(mod, sch)
        }
        chain.clean()
        res
      }
    }
    tapOutcomes(outcomes)
  }

  def validate(chain: Chain, compiled: String, docFile: String) = {
    val step = doStep(chain, docFile, compiled)
    if (step.nonEmpty) chain.addResult(step.get)
    step
  }

  def compile(chain: Chain, rulesFile: String) = {
    val step = for (step1 <- doStep(chain, rulesFile, s"$tronDir${File.separator}iso_dsdl_include.xsl");
         step2 <- doStep(chain, step1, s"$tronDir${File.separator}iso_abstract_expand.xsl");
         step3 <- doStep(chain, step2, s"$tronDir${File.separator}iso_svrl_for_xslt2.xsl")
    ) yield Tuple3(step1, step2, step3)
    if( step.nonEmpty) {
      chain.addResult(step.get._1)
      chain.addResult(step.get._2)
      chain.addResult(step.get._3)
      Some(step.get._3)
    } else {
      None
    }
  }

  def doStep(chain: Chain, in: String, xsl: String) = {
    val suffix = chain.incStepAndGet()
    val out = "RMVBL".r.findFirstIn(in) match {
      case Some(_) => in.replaceAll("\\.RMVBL.*$", ".RMVBL." + suffix)
      case None => in + ".RMVBL." + suffix
    }
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


}
