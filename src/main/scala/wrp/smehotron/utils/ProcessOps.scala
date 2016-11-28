package wrp.smehotron.utils

import com.typesafe.scalalogging.StrictLogging

import scala.sys.process.ProcessLogger
import scala.sys.process.stringSeqToProcess

case class CmdResult(stdout: Vector[String] = Vector.empty, stderr: Vector[String] = Vector.empty, exitStatus: Int = 0) {
  def succeeded = exitStatus == 0

  def failed = exitStatus != 0
  def fail = if(failed) throw ProcessException(this) // TODO?: else succeeded
}

case class ProcessException(result: CmdResult) extends Exception(result.toString)

case class OutErrCollector(tag: String = "") extends StrictLogging {
  private var _stdout: Vector[String] = Vector.empty
  private var _stderr: Vector[String] = Vector.empty

  def fout(s: String) = {
    logger.debug("<_>" + tag + s)
    _stdout :+= s
  }

  def ferr(s: String) = {
    logger.debug("<!>" + tag + s)
    _stderr :+= s
  }

  def stdout = _stdout

  def stderr = _stderr
}

object Cmd extends StrictLogging {
  def run(cmd: Seq[String]) = {
    assert(cmd.size > 0, "command must have a head")
    logger.debug(cmd.map("\"" + _ + "\"").mkString(" "))
    val cltr = OutErrCollector(s"<${cmd.head}>")
    val plogger = ProcessLogger(cltr.fout, cltr.ferr)
    val exc = cmd ! plogger
    logger.debug(s"exit code: $exc")
    CmdResult(cltr.stdout, cltr.stderr, exc)
  }
}

