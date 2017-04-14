package wrp.smehotron.tests

import java.io.File

import org.scalatest.FunSuite
import wrp.smehotron.Smehotron

import scala.xml.XML

class BasicNogoTest extends FunSuite {

  val localPath = "src/test/resources/basic_nogo"

  test("NOGO: basic-ok.xml should pass") {
    // first, clean up...
    new File("src/test/resources/basic_nogo/basic-ok-expected.svrl").delete()
    val cfg = XML.loadFile(s"$localPath/basic-ok.smehotron.config.xml")
    val resgen = Smehotron(".", cfg).generateNogoExpectedSvrls()
    val stgen = (resgen \ "outcome" \ "@type").text
    assertResult("success")(stgen)
    val resprc = <nogo>{Smehotron(".", cfg).processNogoModules()}</nogo>
    val stprc = (resprc \ "test" \ "@status").text
    assertResult("success")(stprc)
    // last, clean up...
    new File("src/test/resources/basic_nogo/basic-ok-expected.svrl").delete()
  }

}
