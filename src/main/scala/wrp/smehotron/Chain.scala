package wrp.smehotron

import java.util.concurrent.atomic.AtomicLong

import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.io.FileUtils
import wrp.smehotron.utils.Uid

object Chain {
  val runId = Uid.uid()
  val chainId = new AtomicLong(0)
}

case class Chain(keep: Boolean = false) extends StrictLogging {
  val id = Chain.runId + "." + Chain.chainId.incrementAndGet()
  val stepId = new AtomicLong(0)
  var results = Vector.empty[String]

  def getIds() = s"$id.$stepId"

  def incStepAndGet() = {
    stepId.incrementAndGet()
    getIds()
  }

  def addResult(r: String) = results :+= r

  def clean() =
    if (!keep)
      for (r <- results) {
        val res = FileUtils.deleteQuietly(new java.io.File(r))
        if (res)
          logger.debug("successfully removed intermediate: " + r)
        else
          logger.warn("failed to remove intermediate: " + r)
      }
}
