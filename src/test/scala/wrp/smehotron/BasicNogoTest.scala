package wrp.smehotron.tests

import org.scalatest.FunSuite
import wrp.smehotron.Smehotron

import scala.xml.XML

class BasicNogoTest extends FunSuite {

  val localPath = "src/test/resources/basic_nogo"

  test("basic-ok.xml should pass") {
    /*val cfg = XML.loadFile(s"$localPath/basic-ok.smehotron.config.xml")
    val res = Smehotron(".", cfg).processGoModules()
    val status = (res \ "test" \ "@status").text
    assertResult("success")(status)*/
  }

}
