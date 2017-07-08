package wrp.smehotron

import scala.xml.NodeSeq

object Tap {

  def tapOk(svrl: String, inputControl: String, rules: String, module: String) =
  //@formatter:off
    <test status="success">
      <module>{module}</module>
      <sch-driver>{rules}</sch-driver>
      <input-control>{inputControl}</input-control>
      <svrl>{svrl}</svrl>
    </test>
  //@formatter:on

  def tapNogoResult(svrl: String,
                    inputControl: String,
                    expected: String,
                    rules: String,
                    module: String,
                    success: Boolean) = {
    val outcome = if (success) "success" else "failure"
    //@formatter:off
    <test status={outcome}>
      <module>{module}</module>
      <sch-driver>{rules}</sch-driver>
      <input-control>{inputControl}</input-control>
      <expected-svrl>{expected}</expected-svrl>
      <actual-svrl>{svrl}</actual-svrl>
    </test>
  //@formatter:on
  }

  def tapNotFound(svrl: String,
                  inputControl: String,
                  expected: String,
                  rules: String,
                  module: String,
                  what: Seq[String]) = {
    //@formatter:off
    <test status="failure">
      <module>{module}</module>
      <sch-driver>{rules}</sch-driver>
      <input-control>{inputControl}</input-control>
      <expected-svrl>{expected}</expected-svrl>
      <actual-svrl>{svrl}</actual-svrl>
      <reason>{what.mkString("\n")}</reason>
    </test>
    //@formatter:on
  }

  def tapAssertsReports(svrl: String,
                        inputControl: String,
                        rules: String,
                        module: String,
                        asserts: NodeSeq,
                        reports: NodeSeq) =
  //@formatter:off
    <test status="failure">
      <module>{module}</module>
      <sch-driver>{rules}</sch-driver>
      <input-control>{inputControl}</input-control>
      <svrl>{svrl}</svrl>
      <asserts>{asserts}</asserts>
      <reports>{reports}</reports>
    </test>
  //@formatter:on

  def tapSvrlFailed(inputControl: String, rules: String, module: String) =
  //@formatter:off
    <test status="failure">
      <module>{module}</module>
      <sch-driver>{rules}</sch-driver>
      <input-control>{inputControl}</input-control>
      <reason>could not produce svrl</reason>
    </test>
  //@formatter:on


  def tapCompilationFailed(rules: String, module: String) =
  //@formatter:off
    <test status="failure">
      <module>{module}</module>
      <sch-driver>{rules}</sch-driver>
      <reason>could not compile</reason>
    </test>
  //@formatter:on

}
