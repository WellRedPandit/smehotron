package wrp.smehotron.utils


object Control {
  def RTE(msg: String, coz: Option[Throwable] = None) = {
    coz match {
      case Some(coz) => new RuntimeException(msg, coz)
      case None => new RuntimeException(msg)
    }
  }
}
