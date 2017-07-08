package wrp.smehotron

import scala.xml.{Node, NodeSeq}

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

  //@formatter:off
  def tapGo(m: Seq[Seq[Node]]) = <go>{m}</go>
  def tapNogo(m: Seq[Seq[Node]]) = <nogo>{m}</nogo>
  def tapResults(m: NodeSeq) = <smehotron-results>{m}</smehotron-results>
  //@formatter:on

  //@formatter:off
  def tapOutcomeSuccess(mod: String, sch: String, ic: String, exp: String) =
    <outcome type="success" module={mod} sch-driver={sch} input-control={ic}>generated svrl: {exp}</outcome>
  //@formatter:on

  //@formatter:off
  def tapOutcomeMoveFailure(mod: String, sch: String, ic: String, exp: String, msg: String) =
    <outcome type="failure" module={mod} sch-driver={sch} input-control={ic}>could not move svrl {exp} due to: {msg}</outcome>
  //@formatter:on

  //@formatter:off
  def tapOutcomeSvlGenFailure(mod: String, sch: String, ic: String) =
    <outcome type="failure" module={mod} sch-driver={sch} input-control={ic}>could not generate svrl</outcome>
  //@formatter:on

  //@formatter:off
  def tapOutcomeCompileFailure(mod: String, sch: String) =
    <outcome type="failure" module={mod} sch-driver={sch}>compilation failed</outcome>
  //@formatter:on

  //@formatter:off
  def tapOutcomes(m: Seq[Seq[Node]]) = <outcomes>{m}</outcomes>
  //@formatter:on

}
