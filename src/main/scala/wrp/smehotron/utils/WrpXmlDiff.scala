package wrp.smehotron.utils

import com.github.andyglow.xml.diff.XmlOps
import software.purpledragon.xml.compare.XmlCompare

object WrpXmlDiff {

  //https://github.com/stringbean/scala-xml-compare
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

  // https://github.com/andyglow/scala-xml-diff
  def diff(): Unit = {
    val res = <foo/> =?= <foo/>
    println(res)
  }
}
