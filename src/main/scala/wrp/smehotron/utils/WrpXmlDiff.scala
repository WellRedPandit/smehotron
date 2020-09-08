package wrp.smehotron.utils

import com.github.andyglow.xml.diff.XmlOps
import software.purpledragon.xml.compare.XmlCompare

object WrpXmlDiff {
  def compare(): Unit = {
    val doc1 = <person>
      <name>John Smith</name>
    </person>
    val doc2 = <person>
      <name>Peter Smith</name>
    </person>

    val result = XmlCompare.compare(doc1, doc2)
    println(result)
    // result1 = XmlDiffers("different text", "John Smith", "Peter Smith")
  }

  def diff(): Unit = {
    val res = <foo/> =?= <foo/>
    println(res)
  }
}
