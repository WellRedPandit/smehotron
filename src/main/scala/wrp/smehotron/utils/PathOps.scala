package wrp.smehotron.utils

import java.nio.file.{Files, Path, Paths}

import org.apache.commons.io.FileUtils

object PathOps {
  implicit class ImplicitPathOps(path: Path) {

    def /(p: String) = Paths.get(path.toString, p).toAbsolutePath

    def /(p: Path) = Paths.get(path.toString, p.toString).toAbsolutePath

    def up = Paths.get(path.toString, "..").toAbsolutePath

    def +(ext: String) = Paths.get(path.toString + ext).toAbsolutePath

    def -(ext: String) = Paths.get(path.toString.replaceAll(ext, "")).toAbsolutePath

    def |>(x1: String, x2: String) = Paths.get(path.toString.replaceAll(x1, x2)).toAbsolutePath

    def mkd() = if(!Files.exists(path)) FileUtils.forceMkdir(path.toFile)

    def ends(s: String) = path.toString.endsWith(s)

    def name = {
      val full = path.toAbsolutePath.toString
      full.substring(full.lastIndexOf('/')+1)
    }
  }

  def abs(p: Path): String = p.toAbsolutePath.toString
  def abs(path: String) = Paths.get(path).toAbsolutePath
  def name(p: Path): String = p.getFileName.toString

  // current working directory of this process
  lazy val pwd = Paths.get(new java.io.File("").getCanonicalPath)
}
