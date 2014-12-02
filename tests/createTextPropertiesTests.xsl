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
	xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
	xmlns:o="urn:oasis:names:tc:opendocument:xmlns:office:1.0" xmlns:odf="http://docs.oasis-open.org/ns/office/1.2/meta/odf#"
	xmlns:of="urn:oasis:names:tc:opendocument:xmlns:of:1.2"
	xmlns:presentation="urn:oasis:names:tc:opendocument:xmlns:presentation:1.0"
	xmlns:s="urn:oasis:names:tc:opendocument:xmlns:style:1.0" xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
	xmlns:smil="urn:oasis:names:tc:opendocument:xmlns:smil-compatible:1.0"
	xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
	xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
	xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
	xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="http://www.example.org/documenttests" xmlns:t="http://www.example.org/documenttests">

	<xsl:param name="mode" select="'odt'" />

	<t:testtemplates>
		<t:testtemplate name="color">
			<fo:color value="#339999" />
		</t:testtemplate>
		<t:testtemplate name="background-color">
			<fo:background-color value="#339999" />
		</t:testtemplate>
		<t:testtemplate name="background-color-transparent">
			<fo:background-color value="transparent" />
		</t:testtemplate>
		<t:testtemplate name="country-language">
			<fo:country value="NL" />
			<fo:language value="nl" />
		</t:testtemplate>
		<t:testtemplate name="font-family">
			<fo:font-family value="Helvetica" />
		</t:testtemplate>
		<t:testtemplate name="font-size">
			<fo:font-size value="6pt" />
		</t:testtemplate>
		<t:testtemplate name="font-style-oblique">
			<fo:font-style value="oblique" />
		</t:testtemplate>
		<t:testtemplate name="font-style-normal">
			<fo:font-style value="normal" />
		</t:testtemplate>
		<t:testtemplate name="font-variant-small-caps">
			<fo:font-variant value="small-caps" />
		</t:testtemplate>
		<t:testtemplate name="font-variant-normal">
			<fo:font-variant value="normal" />
		</t:testtemplate>
		<t:testtemplate name="font-weight-bold">
			<fo:font-weight value="bold" />
		</t:testtemplate>
		<t:testtemplate name="font-weight-100">
			<fo:font-weight value="100" />
		</t:testtemplate>
		<t:testtemplate name="font-weight-900">
			<fo:font-weight value="900" />
		</t:testtemplate>
		<t:testtemplate name="hyphenate-true">
			<fo:hyphenate value="true" />
		</t:testtemplate>
		<t:testtemplate name="hyphenate-false">
			<fo:hyphenate value="false" />
		</t:testtemplate>
		<t:testtemplate name="hyphenation-push-char-count-10">
			<fo:hyphenation-push-char-count value="10" />
		</t:testtemplate>
		<t:testtemplate name="hyphenation-remain-char-count-10">
			<fo:hyphenation-remain-char-count
				value="10" />
		</t:testtemplate>
		<t:testtemplate name="letter-spacing-normal">
			<fo:letter-spacing value="normal" />
		</t:testtemplate>
		<t:testtemplate name="letter-spacing-1mm">
			<fo:letter-spacing value="1mm" />
		</t:testtemplate>
		<t:testtemplate name="letter-spacing--1mm">
			<fo:letter-spacing value="-1mm" />
		</t:testtemplate>
		<t:testtemplate name="script">
			<fo:script value="arab" />
			<fo:language value="ar" />
		</t:testtemplate>
		<t:testtemplate name="script-225">
			<fo:script value="225" />
			<fo:language value="ar" />
		</t:testtemplate>
		<t:testtemplate name="script-es">
			<fo:script value="es" />
			<fo:language value="es" />
		</t:testtemplate>
		<t:testtemplate name="text-shadow">
			<fo:text-shadow value="3pt 3pt" />
		</t:testtemplate>
		<t:testtemplate name="text-transform-none">
			<fo:text-transform value="none" />
		</t:testtemplate>
		<t:testtemplate name="text-transform-lowercase">
			<fo:text-transform value="lowercase" />
		</t:testtemplate>
		<t:testtemplate name="text-transform-uppercase">
			<fo:text-transform value="uppercase" />
		</t:testtemplate>
		<t:testtemplate name="text-transform-capitalize">
			<fo:text-transform value="capitalize" />
		</t:testtemplate>
		<t:testtemplate name="country-asian">
			<s:country-asian value="none" />
		</t:testtemplate>
		<t:testtemplate name="country-complex">
			<s:country-complex value="SA" />
		</t:testtemplate>
		<t:testtemplate name="font-charset-x-symbol">
			<s:font-charset value="x-symbol" />
		</t:testtemplate>
		<t:testtemplate name="font-charset-utf8">
			<s:font-charset value="utf8" />
		</t:testtemplate>
		<t:testtemplate name="font-charset-utf-8">
			<s:font-charset value="utf-8" />
		</t:testtemplate>
		<t:testtemplate name="font-charset-asian">
			<s:font-charset-asian value="x-symbol" />
		</t:testtemplate>
		<t:testtemplate name="font-charset-complex">
			<s:font-charset-complex value="x-symbol" />
		</t:testtemplate>
		<t:testtemplate name="font-family-asian">
			<s:font-family-asian value="Helvetica" />
		</t:testtemplate>
		<t:testtemplate name="font-family-complex">
			<s:font-family-complex value="Helvetica" />
		</t:testtemplate>
		<t:testtemplate name="font-family-generic">
			<s:font-family-generic value="modern" />
		</t:testtemplate>
		<t:testtemplate name="font-family-generic-asian">
			<s:font-family-generic-asian value="modern" />
		</t:testtemplate>
		<t:testtemplate name="font-family-generic-complex">
			<s:font-family-generic-complex value="modern" />
		</t:testtemplate>
		<t:testtemplate name="font-name">
			<s:font-name value="Helvetica" />
		</t:testtemplate>
		<t:testtemplate name="font-name-asian">
			<s:font-name-asian value="Helvetica" />
		</t:testtemplate>
		<t:testtemplate name="font-name-complex">
			<s:font-name-complex value="Helvetica" />
		</t:testtemplate>
		<t:testtemplate name="font-pitch-fixed">
			<s:font-pitch value="fixed" />
		</t:testtemplate>
		<t:testtemplate name="font-pitch-variable">
			<s:font-pitch value="variable" />
		</t:testtemplate>
		<t:testtemplate name="font-pitch-asian">
			<s:font-pitch-asian value="variable" />
		</t:testtemplate>
		<t:testtemplate name="font-pitch-complex">
			<s:font-pitch-complex value="variable" />
		</t:testtemplate>
		<t:testtemplate name="font-relief-none">
			<s:font-relief value="none" />
		</t:testtemplate>
		<t:testtemplate name="font-relief-embossed">
			<s:font-relief value="embossed" />
		</t:testtemplate>
		<t:testtemplate name="font-relief-engraved">
			<s:font-relief value="engraved" />
		</t:testtemplate>
		<t:testtemplate name="font-size-asian">
			<s:font-size-asian value="3pt" />
		</t:testtemplate>
		<t:testtemplate name="font-size-complex">
			<s:font-size-complex value="3pt" />
		</t:testtemplate>
		<t:testtemplate name="font-size-rel">
			<s:font-size-rel value="16pt" />
		</t:testtemplate>
		<t:testtemplate name="font-size-rel-asian">
			<s:font-size-rel-asian value="16pt" />
		</t:testtemplate>
		<t:testtemplate name="font-size-rel-complex">
			<s:font-size-rel-complex value="16pt" />
		</t:testtemplate>
		<t:testtemplate name="font-style-asian">
			<s:font-style-asian value="italic" />
		</t:testtemplate>
		<t:testtemplate name="font-style-complex">
			<s:font-style-complex value="italic" />
		</t:testtemplate>
		<t:testtemplate name="font-style-name">
			<s:font-style-name value="Bold" />
			<fo:font-family value="Helvetica" />
		</t:testtemplate>
		<t:testtemplate name="font-style-name-asian">
			<s:font-style-name-asian value="Bold" />
			<fo:font-family value="Helvetica" />
		</t:testtemplate>
		<t:testtemplate name="font-style-name-complex">
			<s:font-style-name-complex value="Bold" />
			<fo:font-family value="Helvetica" />
		</t:testtemplate>
		<t:testtemplate name="font-weight-asian">
			<s:font-weight-asian value="bold" />
		</t:testtemplate>
		<t:testtemplate name="font-weight-complex">
			<s:font-weight-complex value="bold" />
		</t:testtemplate>
		<t:testtemplate name="language-asian">
			<s:language-asian value="zh" />
		</t:testtemplate>
		<t:testtemplate name="language-complex">
			<s:language-complex value="ar" />
			<s:country-complex value="SA" />
		</t:testtemplate>
		<t:testtemplate name="letter-kerning-false">
			<s:letter-kerning value="false" />
		</t:testtemplate>
		<t:testtemplate name="letter-kerning-true">
			<s:letter-kerning value="true" />
		</t:testtemplate>
		<t:testtemplate name="rfc-language-tag">
			<s:rfc-language-tag value="mn" />
		</t:testtemplate>
		<t:testtemplate name="rfc-language-tag-asian">
			<s:rfc-language-tag-asian value="mn" />
		</t:testtemplate>
		<t:testtemplate name="rfc-language-tag-complex">
			<s:rfc-language-tag-complex value="mn" />
		</t:testtemplate>
		<t:testtemplate name="script-asian">
			<s:script-asian value="en" />
		</t:testtemplate>
		<t:testtemplate name="script-complex">
			<s:script-complex value="en" />
		</t:testtemplate>
		<t:testtemplate name="script-type-latin">
			<s:script-type value="latin" />
		</t:testtemplate>
		<t:testtemplate name="script-type-asian">
			<s:script-type value="latin" />
		</t:testtemplate>
		<t:testtemplate name="text-blinking">
			<s:text-blinking value="true" />
		</t:testtemplate>
		<t:testtemplate name="text-combine-none">
			<s:text-combine value="none" />
		</t:testtemplate>
		<t:testtemplate name="text-combine-letters">
			<s:text-combine value="letters" />
		</t:testtemplate>
		<t:testtemplate name="text-combine-lines">
			<s:text-combine value="lines" />
		</t:testtemplate>
		<t:testtemplate name="text-combine-end-char">
			<s:text-combine-end-char value="Z" />
			<s:text-combine value="lines" />
		</t:testtemplate>
		<t:testtemplate name="text-combine-start-char">
			<s:text-combine-start-char value="A" />
			<s:text-combine-end-char value="Z" />
			<s:text-combine value="lines" />
		</t:testtemplate>
		<t:testtemplate name="text-emphasize">
			<s:text-emphasize value="disc below" />
		</t:testtemplate>
		<t:testtemplate name="text-line-through-color">
			<s:text-line-through-color value="#339999" />
			<s:text-line-through-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-line-through-color-font">
			<s:text-line-through-color value="font-color" />
			<s:text-line-through-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-line-through-mode-continuous">
			<s:text-line-through-mode value="continuous" />
			<s:text-line-through-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-line-through-mode-skip-white-space">
			<s:text-line-through-mode value="skip-white-space" />
			<s:text-line-through-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-line-through-style-solid">
			<s:text-line-through-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-line-through-style-wave">
			<s:text-line-through-style value="wave" />
		</t:testtemplate>
		<t:testtemplate name="text-line-through-text">
			<s:text-line-through-style value="solid" />
			<s:text-line-through-text value="X" />
		</t:testtemplate>
		<t:testtemplate name="text-line-through-text-style">
			<s:text-line-through-text-style value="color" />
			<s:text-line-through-text value="X" />
		</t:testtemplate>
		<t:testtemplate name="text-line-through-type-none">
			<s:text-line-through-type value="none" />
		</t:testtemplate>
		<t:testtemplate name="text-line-through-type-single">
			<s:text-line-through-type value="single" />
		</t:testtemplate>
		<t:testtemplate name="text-line-through-type-double">
			<s:text-line-through-type value="double" />
		</t:testtemplate>
		<t:testtemplate name="text-line-through-width-auto">
			<s:text-line-through-width value="auto" />
			<s:text-line-through-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-line-through-width-bold">
			<s:text-line-through-width value="bold" />
			<s:text-line-through-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-line-through-width-thin">
			<s:text-line-through-width value="thin" />
			<s:text-line-through-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-outline-true">
			<s:text-outline value="true" />
		</t:testtemplate>
		<t:testtemplate name="text-outline-false">
			<s:text-outline value="false" />
		</t:testtemplate>
		<t:testtemplate name="text-overline-color">
			<s:text-overline-color value="#339999" />
			<s:text-overline-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-overline-mode">
			<s:text-overline-mode value="continuous" />
			<s:text-overline-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-overline-style">
			<s:text-overline-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-overline-type">
			<s:text-overline-type value="single" />
		</t:testtemplate>
		<t:testtemplate name="text-overline-width">
			<s:text-overline-width value="1pt" />
			<s:text-overline-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-position-sub">
			<s:text-position value="sub" />
		</t:testtemplate>
		<t:testtemplate name="text-position-sub-50">
			<s:text-position value="sub 50%" />
		</t:testtemplate>
		<t:testtemplate name="text-position-super">
			<s:text-position value="super" />
		</t:testtemplate>
		<t:testtemplate name="text-position-super-50">
			<s:text-position value="super 50%" />
		</t:testtemplate>
		<t:testtemplate name="text-rotation-angle">
			<s:text-rotation-angle value="90" />
		</t:testtemplate>
		<t:testtemplate name="text-rotation-scale-fixed">
			<s:text-rotation-scale value="fixed" />
			<s:text-rotation-angle value="90" />
		</t:testtemplate>
		<t:testtemplate name="text-rotation-scale-line-height">
			<s:text-rotation-scale value="line-height" />
			<s:text-rotation-angle value="90" />
		</t:testtemplate>
		<t:testtemplate name="text-scale">
			<s:text-scale value="50%" />
		</t:testtemplate>
		<t:testtemplate name="text-scale-wide">
			<s:text-scale value="200%" />
		</t:testtemplate>
		<t:testtemplate name="text-underline-color-font-color">
			<s:text-underline-color value="font-color" />
			<s:text-underline-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-underline-color">
			<s:text-underline-color value="#339999" />
			<s:text-underline-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-underline-mode-continuous">
			<s:text-underline-mode value="continuous" />
			<s:text-underline-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-underline-mode-skip-white-space">
			<s:text-underline-mode value="skip-white-space" />
			<s:text-underline-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-underline-style-none">
			<s:text-underline-style value="none" />
		</t:testtemplate>
		<t:testtemplate name="text-underline-style-dash">
			<s:text-underline-style value="dash" />
		</t:testtemplate>
		<t:testtemplate name="text-underline-style-solid">
			<s:text-underline-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="text-underline-style-wave">
			<s:text-underline-style value="wave" />
		</t:testtemplate>
		<t:testtemplate name="text-underline-type-none">
			<s:text-underline-type value="none" />
		</t:testtemplate>
		<t:testtemplate name="text-underline-type-single">
			<s:text-underline-type value="single" />
		</t:testtemplate>
		<t:testtemplate name="text-underline-type-double">
			<s:text-underline-type value="double" />
		</t:testtemplate>
		<t:testtemplate name="text-underline-width">
			<s:text-underline-width value="1pt" />
			<s:text-underline-style value="solid" />
		</t:testtemplate>
		<t:testtemplate name="use-window-font-color-true">
			<s:use-window-font-color value="true" />
		</t:testtemplate>
		<t:testtemplate name="use-window-font-color-false">
			<s:use-window-font-color value="false" />
		</t:testtemplate>
		<t:testtemplate name="display-true">
			<text:display value="true" />
		</t:testtemplate>
		<t:testtemplate name="display-none">
			<text:display value="none" />
		</t:testtemplate>
		<t:testtemplate name="display-condition">
			<text:display value="condition" />
			<text:condition value="none" />
		</t:testtemplate>
	</t:testtemplates>

	<xsl:output encoding="utf-8" indent="no" method="xml"
		omit-xml-declaration="no" />

	<xsl:template name="body">
		<xsl:choose>
			<xsl:when test="$mode='ods'">
				<o:spreadsheet>
					<table:table table:name="TestTable" table:style-name="table"
						table:print="true">
						<table:table-column
							table:default-cell-style-name="style" />
						<table:table-row>
							<table:table-cell o:value-type="string">
								<text:p>hello world</text:p>
							</table:table-cell>
						</table:table-row>
					</table:table>
				</o:spreadsheet>
			</xsl:when>
			<xsl:when test="$mode='odp'">
				<o:presentation>
					<draw:page draw:master-page-name="Page">
						<draw:frame draw:style-name="style" svg:width="10cm"
							svg:height="10cm" svg:x="1cm" svg:y="1cm">
							<draw:text-box>
								<text:p>hello world</text:p>
							</draw:text-box>
						</draw:frame>
					</draw:page>
				</o:presentation>
			</xsl:when>
			<xsl:otherwise>
				<o:text>
					<text:p text:style-name="style">hello world</text:p>
				</o:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

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
						<s:text-properties>
							<xsl:for-each select="*">
								<xsl:attribute namespace="{namespace-uri()}" name="{name()}"><xsl:value-of
									select="@value" /></xsl:attribute>
							</xsl:for-each>
						</s:text-properties>
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
						<xpath
							expr="boolean(//s:style[@s:display-name='TestStyle' or (not(@s:display-name) and @s:name='TestStyle')]/s:text-properties/@{name()})" />
						<xpath
							expr="//s:style[@s:display-name='TestStyle' or (not(@s:display-name) and @s:name='TestStyle')]/s:text-properties/@{name()}='{@value}'" />
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
