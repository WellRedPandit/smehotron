package wrp.smehotron.tests

import org.scalatest.funsuite.AnyFunSuite
import wrp.smehotron.Smehotron

import scala.xml.XML

class BasicGoTest extends AnyFunSuite {

  val localPath = "src/test/resources/basic"

  test("GO: basic-ok.xml should pass") {
    val cfg = XML.loadFile(s"$localPath/basic-ok.smehotron.config.xml")
    val res = <go>{Smehotron(".", cfg).processGoModules()}</go>
    val status = (res \ "test" \ "@status").text
    assertResult("success")(status)
  }

  test("GO: basic-report.xml should fail with a message") {
    val cfg = XML.loadFile(s"$localPath/basic-report.smehotron.config.xml")
    val res = <go>{Smehotron(".", cfg).processGoModules()}</go>
    val status = (res \ "test" \ "@status").text
    assertResult("failure")(status)
    val msg = (res \ "test" \ "reports" \ "successful-report" \ "text").text
    assertResult("Please inform Director of Publications about the retraction")(msg)
  }

  test("GO: basic-assert.xml should fail with a message") {
    val cfg = XML.loadFile(s"$localPath/basic-assert.smehotron.config.xml")
    val res = <go>{Smehotron(".", cfg).processGoModules()}</go>
    val status = (res \ "test" \ "@status").text
    assertResult("failure")(status)
    val msg = (res \ "test" \ "asserts" \ "failed-assert" \ "text").text
    assertResult("@article-type must always be present!")(msg)
  }

}
