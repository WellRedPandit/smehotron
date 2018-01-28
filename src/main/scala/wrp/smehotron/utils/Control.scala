package wrp.smehotron.utils

import scala.language.reflectiveCalls

object Control {
  def using[A <: { def close(): Unit }, B](resource: A)(f: A => B): B =
    try {
      f(resource)
    } finally {
      if (Option(resource).nonEmpty) {
        try {
          resource.close()
        } catch {
          case _: Throwable => /*ignore*/
        }
      }
    }

  def RTE(msg: String, coz: Option[Throwable] = None) = {
    coz match {
      case Some(coz) => new RuntimeException(msg, coz)
      case None => new RuntimeException(msg)
    }
  }

  def skip(op: => Unit) = {}
}
