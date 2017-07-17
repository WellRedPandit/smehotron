package wrp.smehotron.utils

import java.security.SecureRandom
import java.util.UUID

object Uid {
  val random = new SecureRandom

  def uid(): String = UUID.randomUUID().toString().replace("-", "")

  def suid(strlen: Int = 26): String = {
    val uid = new java.math.BigInteger(130, random).toString(32)
    uid.substring(0, math.min(strlen, uid.size))
  }

}
