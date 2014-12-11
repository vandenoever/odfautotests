<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
	xmlns:anim="urn:oasis:names:tc:opendocument:xmlns:animation:1.0"
	xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0"
	xmlns:config="urn:oasis:names:tc:opendocument:xmlns:config:1.0"
	xmlns:db="urn:oasis:names:tc:opendocument:xmlns:database:1.0" xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0" xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
	xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
	xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0"
	xmlns:manifest="urn:oasis:names:tc:opendocument:xmlns:manifest:1.0"
	xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0"
	xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datas:1.0" xmlns:o="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
	xmlns:odf="http://docs.oasis-open.org/ns/office/1.2/meta/odf#"
	xmlns:of="urn:oasis:names:tc:opendocument:xmlns:of:1.2"
	xmlns:presentation="urn:oasis:names:tc:opendocument:xmlns:presentation:1.0"
	xmlns:s="urn:oasis:names:tc:opendocument:xmlns:style:1.0" xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
	xmlns:smil="urn:oasis:names:tc:opendocument:xmlns:smil-compatible:1.0"
	xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
	xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
	xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
	xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="http://www.example.org/documenttests" xmlns:t="http://www.example.org/documenttests">

	<xsl:import href="shared.xsl" />

	<xsl:param name="mode" select="'odt'" />

	<xsl:param name="props" select="'paragraph'" />

	<t:testtemplates>
		<t:testtemplate name="background-color">
			<fo:background-color value="#339999" />
		</t:testtemplate>
		<t:testtemplate name="border">
			<fo:border value="0.06pt solid #000000" />
		</t:testtemplate>
		<t:testtemplate name="border-bottom">
			<fo:border-bottom value="0.06pt solid #000000" />
		</t:testtemplate>
		<t:testtemplate name="border-left">
			<fo:border-left value="0.06pt solid #000000" />
		</t:testtemplate>
		<t:testtemplate name="border-right">
			<fo:border-right value="0.06pt solid #000000" />
		</t:testtemplate>
		<t:testtemplate name="border-top">
			<fo:border-top value="0.06pt solid #000000" />
		</t:testtemplate>
		<t:testtemplate name="break-after">
			<fo:break-after value="auto" />
		</t:testtemplate>
		<t:testtemplate name="break-before">
			<fo:break-before value="auto" />
		</t:testtemplate>
		<t:testtemplate name="hyphenation-keep">
			<fo:hyphenation-keep value="page" />
		</t:testtemplate>
		<t:testtemplate name="hyphenation-ladder-count">
			<fo:hyphenation-ladder-count value="no-limit" />
		</t:testtemplate>
		<t:testtemplate name="keep-together">
			<fo:keep-together value="auto" />
		</t:testtemplate>
		<t:testtemplate name="keep-with-next">
			<fo:keep-with-next value="auto" />
		</t:testtemplate>
		<t:testtemplate name="line-height">
			<fo:line-height value="120%" />
		</t:testtemplate>
		<t:testtemplate name="margin">
			<fo:margin value="0.0299in" />
		</t:testtemplate>
		<t:testtemplate name="margin-bottom">
			<fo:margin-bottom value="0.0299in" />
		</t:testtemplate>
		<t:testtemplate name="margin-left">
			<fo:margin-left value="0.0299in" />
		</t:testtemplate>
		<t:testtemplate name="margin-right">
			<fo:margin-right value="0.0299in" />
		</t:testtemplate>
		<t:testtemplate name="margin-top">
			<fo:margin-top value="0.0299in" />
		</t:testtemplate>
		<t:testtemplate name="orphans">
			<fo:orphans value="1" />
		</t:testtemplate>
		<t:testtemplate name="padding">
			<fo:padding value="0.0299in" />
		</t:testtemplate>
		<t:testtemplate name="padding-bottom">
			<fo:padding-bottom value="0.0299in" />
		</t:testtemplate>
		<t:testtemplate name="padding-left">
			<fo:padding-left value="0.0299in" />
		</t:testtemplate>
		<t:testtemplate name="padding-right">
			<fo:padding-right value="0.0299in" />
		</t:testtemplate>
		<t:testtemplate name="padding-top">
			<fo:padding-top value="0.0299in" />
		</t:testtemplate>
		<t:testtemplate name="text-align">
			<fo:text-align value="center" />
		</t:testtemplate>
		<t:testtemplate name="text-align-last">
			<fo:text-align-last value="center" />
		</t:testtemplate>
		<t:testtemplate name="text-indent">
			<fo:text-indent value="10mm" />
		</t:testtemplate>
		<t:testtemplate name="widows">
			<fo:widows value="1" />
		</t:testtemplate>
		<t:testtemplate name="auto-test-indent">
			<s:auto-text-indent value="true" />
		</t:testtemplate>
		<t:testtemplate name="background-transparency">
			<s:background-transparency value="50%" />
		</t:testtemplate>
		<t:testtemplate name="border-line-width">
			<fo:border value="5pt double #000000" />
			<s:border-line-width value="0.4mm 0.0299in 0.7mm" />
		</t:testtemplate>
		<t:testtemplate name="border-line-width-bottom">
			<fo:border value="5pt double #000000" />
			<s:border-line-width-bottom value="0.4mm 0.0299in 0.7mm" />
		</t:testtemplate>
		<t:testtemplate name="border-line-width-left">
			<fo:border value="5pt double #000000" />
			<s:border-line-width-left value="0.4mm 0.0299in 0.7mm" />
		</t:testtemplate>
		<t:testtemplate name="border-line-width-right">
			<fo:border value="5pt double #000000" />
			<s:border-line-width-right value="0.4mm 0.0299in 0.7mm" />
		</t:testtemplate>
		<t:testtemplate name="border-line-width-top">
			<fo:border value="5pt double #000000" />
			<s:border-line-width-top value="0.4mm 0.0299in 0.7mm" />
		</t:testtemplate>
		<t:testtemplate name="font-independent-line-spacing">
			<s:font-independent-line-spacing
				value="false" />
		</t:testtemplate>
		<t:testtemplate name="join-border">
			<s:join-border value="true" />
		</t:testtemplate>
		<t:testtemplate name="justify-single-word">
			<s:justify-single-word value="true" />
		</t:testtemplate>
		<t:testtemplate name="line-break">
			<s:line-break value="normal" />
		</t:testtemplate>
		<t:testtemplate name="line-height-at-least">
			<s:line-height-at-least value="10mm" />
		</t:testtemplate>
		<t:testtemplate name="line-spacing">
			<s:line-spacing value="10mm" />
		</t:testtemplate>
		<t:testtemplate name="page-number">
			<s:page-number value="10" />
		</t:testtemplate>
		<t:testtemplate name="punctuation-wrap">
			<s:punctuation-wrap value="simple" />
		</t:testtemplate>
		<t:testtemplate name="register-true">
			<s:register-true value="false" />
		</t:testtemplate>
		<t:testtemplate name="shadow">
			<s:shadow value="#808080 -0.0701in 0.0701in" />
		</t:testtemplate>
		<t:testtemplate name="snap-to-layout-grid">
			<s:snap-to-layout-grid value="false" />
		</t:testtemplate>
		<t:testtemplate name="tab-stop-distance">
			<s:tab-stop-distance value="50mm" />
		</t:testtemplate>
		<t:testtemplate name="text-autospace">
			<s:text-autospace value="none" />
		</t:testtemplate>
		<t:testtemplate name="vertical-align">
			<s:vertical-align value="auto" />
		</t:testtemplate>
		<t:testtemplate name="writing-mode">
			<s:writing-mode value="lr-tb" />
		</t:testtemplate>
		<t:testtemplate name="writing-mode-automatic">
			<s:writing-mode-automatic value="false" />
		</t:testtemplate>
		<t:testtemplate name="line-number">
			<text:number-lines value="true" />
			<text:line-number value="5" />
		</t:testtemplate>
		<t:testtemplate name="number-lines">
			<text:number-lines value="true" />
		</t:testtemplate>
	</t:testtemplates>

	<xsl:output encoding="utf-8" indent="no" method="xml"
		omit-xml-declaration="no" />

	<xsl:template match="t:testtemplate">
		<xsl:variable name="family">
			<xsl:choose>
				<xsl:when test="$mode='ods'">
					<xsl:value-of select="'table-cell'" />
				</xsl:when>
				<xsl:when test="$mode='odp'">
					<xsl:value-of select="'graphic'" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'paragraph'" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<test name="{$mode}-{@name}">
			<input type="{$mode}1.2">
				<o:styles>
					<s:style s:name="standard" s:family="paragraph">
						<s:text-properties fo:font-size="12pt"
							s:font-name="Helvetica" />
					</s:style>
					<s:style s:name="standard" s:family="graphic">
						<s:graphic-properties draw:stroke="none"
							draw:fill="none" />
						<s:text-properties fo:font-size="12pt"
							s:font-name="Helvetica" />
					</s:style>
					<s:style s:name="standard" s:family="table-cell">
						<s:text-properties fo:font-size="12pt"
							s:font-name="Helvetica" />
					</s:style>
					<s:style s:name="color" s:family="text" s:display-name="Text">
						<s:text-properties fo:color="#339999" />
					</s:style>
					<s:style s:name="style" s:family="{$family}"
						s:display-name="TestStyle" s:parent-style-name="standard">
						<s:paragraph-properties>
							<xsl:for-each select="*">
								<xsl:attribute namespace="{namespace-uri()}" name="{name()}"><xsl:value-of
									select="@value" /></xsl:attribute>
							</xsl:for-each>
						</s:paragraph-properties>
					</s:style>
					<s:style s:name="table" s:family="table"
						s:master-page-name="Standard">
						<s:table-properties table:display="true"
							s:writing-mode="lr-tb" />
					</s:style>
				</o:styles>
				<o:automatic-styles>
					<s:style s:family="table" s:master-page-name="Standard"
						s:name="table">
						<s:table-properties s:writing-mode="lr-tb"
							table:display="true" />
					</s:style>
				</o:automatic-styles>
				<xsl:call-template name="body" />
			</input>
			<output types="{$mode}1.0 {$mode}1.1 {$mode}1.2 {$mode}1.2ext">
				<file path="styles.xml">
					<xsl:for-each select="*">
						<xsl:variable name="selector"
							select="concat(&quot;//s:style[@s:display-name='TestStyle' or (not(@s:display-name) and @s:name='TestStyle')]/s:paragraph-properties/@&quot;,name())" />
						<xpath expr="boolean({$selector})" />
						<xsl:call-template name="xpaths">
							<xsl:with-param name="selector" select="$selector" />
							<xsl:with-param name="value" select="@value" />
							<xsl:with-param name="index" select="-1" />
						</xsl:call-template>
					</xsl:for-each>
				</file>
			</output>
			<pdf />
		</test>
	</xsl:template>

	<xsl:template match="/">
		<documenttests xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://www.example.org/documenttests ../documenttests.xsd">
			<xsl:variable name="tests" select="/xsl:stylesheet/t:testtemplates/*" />
			<xsl:apply-templates select="$tests" />
		</documenttests>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
