<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:d="http://www.osa.org/Schematron/docs"
	xmlns:m="http://www.osa.org/Schematron/util" queryBinding="xslt2">

	<ns prefix="m" uri="http://www.osa.org/Schematron/util"/>

	<let name="module-code" value="'config'"/>

	<pattern>

		<rule id="CNFG110" context="source">
			<let name="ruleID" value="'CNFG110'"/>

			<assert test="matches(., '(.xml$|.XML$)', 'i')">[<value-of select="$ruleID"
				/>]: &lt;<name/>> does not end with '.xml' or '.XML'</assert>

			<report test="matches(descendant::text()[1], '^\s')">[<value-of
				select="$ruleID"/>]: &lt;<name/>> starts with whitespace</report>
		</rule>


		<rule id="CNFG120" context="expected-svrl">
			<let name="ruleID" value="'CNFG120'"/>

			<assert test="ends-with(., '.svrl')">[<value-of select="$ruleID"/>]:
				&lt;<name/>> does not end with '.svrl'</assert>

			<assert test="starts-with(., 'expected-svrl\')">[<value-of select="$ruleID"
				/>]: expected &lt;<name/>> does not start with 'expected-svrl\'</assert>

			<report test="matches(descendant::text()[1], '^\s')">[<value-of
				select="$ruleID"/>]: &lt;<name/>> starts with whitespace</report>
		</rule>


		<rule id="CNFG130" context="go//sch-driver">
			<let name="ruleID" value="'CNFG130'"/>

			<assert test="ends-with(., '.sch')">[<value-of select="$ruleID"/>]:
				&lt;<name/>> does not end with '.sch'</assert>

			<report test="matches(descendant::text()[1], '^\s')">[<value-of
				select="$ruleID"/>]: &lt;<name/>> starts with whitespace</report>

			<!--<report test="matches(descendant::text()[last()], '\s$')">[<value-of
				select="$ruleID"/>]: &lt;<name/>> ends with whitespace</report>-->

			<report
				test="matches(parent::module/@name, '^[A-Z]{3}$') and not(starts-with(., '..\..\..\'))"
				> [<value-of select="$ruleID"/>]: &lt;<name/>> does not start with
				'..\..\..\'</report>

			<report
				test="not(starts-with(., '..\..\sch-drivers\')) and not(matches(parent::module/@name, '^[A-Z]{3}$'))"
				>[<value-of select="$ruleID"/>]: &lt;<name/>> does not start with
				'..\..\sch-drivers\'</report>

			<report
				test="
					not(ends-with(., 'testing.sch')) and not(matches(parent::module/@name, '^[A-Z]{3}$'))
					and not(ends-with(parent::module/@name, '_TOP'))"
				>[<value-of select="$ruleID"/>]: &lt;<name/>> does not end with
				'testing.sch'</report>
		</rule>


		<rule id="CNFG140" context="nogo//sch-driver">
			<let name="ruleID" value="'CNFG140'"/>

			<assert
				test="ends-with(., 'testing.sch') or ends-with(parent::module/@name, '_TOP')"
				>[<value-of select="$ruleID"/>]: expected &lt;<name/>> to end with
				testing.sch</assert>

			<report test="matches(descendant::text()[1], '^\s')">[<value-of
				select="$ruleID"/>]: &lt;<name/>> starts with whitespace</report>

			<report test="not(starts-with(., '..\..\..\'))"> [<value-of select="$ruleID"
				/>]: &lt;<name/>> does not start with '..\..\..\'</report>
		</rule>


		<rule id="CNFG150" context="catalogs">
			<let name="ruleID" value="'CNFG150'"/>

			<assert test="ends-with(./@base, 'MEGA\sasha-pasha\smehotron')">[<value-of
				select="$ruleID"/>]: expected <name/>/@base to end with
				'MEGA\sasha-pasha\smehotron'</assert>

			<assert
				test="catalog = 'MEGA-OSA-JATS-BITS-DEV\dtd\catalog-OSA-JATS-BITS.xml'"
				>[<value-of select="$ruleID"/>]: expected
				catalog="MEGA-OSA-JATS-BITS-DEV\dtd\catalog-OSA-JATS-BITS.xml"</assert>
		</rule>


		<rule id="CNFG160" context="go">
			<let name="ruleID" value="'CNFG160'"/>

			<assert
				test="ends-with(./@base, 'MEGA\sasha-pasha\smehotron\MEGA-OSA-Schematron-v1.1-DEV\_test-suite\input-controls\Go')"
				>[<value-of select="$ruleID"/>]: expected <name/>/@base to end with
				'MEGA\sasha-pasha\smehotron\MEGA-OSA-Schematron-v1.1-DEV\_test-suite\input-controls\Go'</assert>

		</rule>


		<rule id="CNFG170" context="nogo">
			<let name="ruleID" value="'CNFG170'"/>

			<assert
				test="
					ends-with(./@base, 'MEGA\sasha-pasha\smehotron\MEGA-OSA-Schematron-v1.1-DEV\_test-suite\input-controls\NoGo\ALERT') or
					ends-with(./@base, 'MEGA\sasha-pasha\smehotron\MEGA-OSA-Schematron-v1.1-DEV\_test-suite\input-controls\NoGo\ERROR')"
				> [<value-of select="$ruleID"/>]: expected <name/>/@base to end with
				'MEGA\sasha-pasha\smehotron\MEGA-OSA-Schematron-v1.1-DEV\_test-suite\input-controls\NoGo\ALERT'
				or
				MEGA\sasha-pasha\smehotron\MEGA-OSA-Schematron-v1.1-DEV\_test-suite\input-controls\NoGo\ERROR</assert>
		</rule>


		<rule id="CNFG180" context="nogo//input-control">
			<let name="ruleID" value="'CNFG180'"/>

			<let name="source-name" value="substring-before(source, '.xml')"/>
			<let name="svrl-name"
				value="substring-before(substring-after(expected-svrl, 'expected-svrl\'), '.svrl')"/>

			<assert test="$source-name = $svrl-name">[<value-of select="$ruleID"/>]:
				[<value-of select="$source-name"/>] in &lt;source> does not match
				[<value-of select="$svrl-name"/>] in &lt;expected-svrl></assert>

		</rule>
	</pattern>
</schema>
