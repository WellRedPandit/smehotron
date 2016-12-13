package wrp.smehotron.tests

import org.scalatest.FunSuite
import wrp.smehotron.Smehotron

import scala.xml.XML

class BasicTest extends FunSuite {

  val localPath = "src/test/resources/basic"

  test("basic-ok.xml should pass") {
    val cfg = XML.loadFile(s"$localPath/basic-ok.smehotron.config.xml")
    val res = Smehotron(".", cfg).processGoModules()
    val status = (res \ "test" \ "@status").text
    assert(status == "success")
  }

  test("basic-report.xml should fail with a message") {
    val cfg = XML.loadFile(s"$localPath/basic-report.smehotron.config.xml")
    val res = Smehotron(".", cfg).processGoModules()
    val status = (res \ "test" \ "@status").text
    assert(status == "failure")
    val msg = (res \ "test" \ "reports" \ "successful-report" \ "text").text
    assert(msg == "Please inform Director of Publications about the retraction")
  }

  test("basic-assert.xml should fail with a message") {
    val cfg = XML.loadFile(s"$localPath/basic-assert.smehotron.config.xml")
    val res = Smehotron(".", cfg).processGoModules()
    val status = (res \ "test" \ "@status").text
    assert(status == "failure")
    val msg = (res \ "test" \ "asserts" \ "failed-assert" \ "text").text
    assert(msg == "@article-type must always be present!")
  }

}
