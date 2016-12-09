<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" queryBinding="xslt2" xmlns:m="http://www.osa.org/Schematron/util" xmlns:f="http://www.osa.org/Schematron/util-fund" xmlns:d="http://www.osa.org/Schematron/docs" id="basic">
  <rule context="article">
    <assert test="exists(@article-type)">@article-type must always be present!</assert>
    <report test="@article-type='retraction'">Please inform Director of Publications about the retraction</report>
  </rule>
</schema>