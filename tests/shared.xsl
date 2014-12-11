<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
	xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0" xmlns:o="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
	xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
	xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
	xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="http://www.example.org/documenttests" xmlns:t="http://www.example.org/documenttests">

	<xsl:template name="isLength">
		<xsl:param name="value" />
		<xsl:variable name="len" select="string-length($value)" />
		<xsl:variable name="suf" select="substring($value,$len - 1,$len)" />
		<xsl:variable name="num" select="substring($value,1,$len - 2)" />
		<xsl:variable name="hasLengthUnit"
			select="$suf = 'cm' or $suf = 'mm' or $suf = 'in' or $suf = 'pt' or $suf = 'pc' or $suf = 'px'" />
		<xsl:variable name="hasValidNumber"
			select="string-length(translate($num,'1234567890.-',''))=0" />
		<xsl:value-of select="$hasLengthUnit and $hasValidNumber" />
	</xsl:template>

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

	<xsl:template name="xpath">
		<xsl:param name="selector" />
		<xsl:param name="value" />
		<xsl:param name="index" />
		<xsl:variable name="sel">
			<xsl:choose>
				<xsl:when test="$index = -1">
					<xsl:value-of select="$selector" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of
						select="concat('t:item(',$selector,',',$index,',&quot; &quot;)')" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="isLength">
			<xsl:call-template name="isLength">
				<xsl:with-param name="value" select="$value" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$isLength = 'true'">
				<xpath expr="t:compareLength({$sel},'{$value}')" />
			</xsl:when>
			<xsl:otherwise>
				<xpath expr="{$sel}='{$value}'" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="xpaths">
		<xsl:param name="selector" />
		<xsl:param name="value" />
		<xsl:param name="index" />
		<xsl:variable name="first" select="substring-before($value,' ')" />
		<xsl:variable name="current">
			<xsl:choose>
				<xsl:when test="string-length($first) = 0">
					<xsl:value-of select='$value' />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select='$first' />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="i">
			<xsl:choose>
				<xsl:when test="string-length($first) != 0 and $index = -1">
					<xsl:value-of select="'0'" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select='$index' />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:call-template name="xpath">
			<xsl:with-param name="selector" select="$selector" />
			<xsl:with-param name="value" select="$current" />
			<xsl:with-param name="index" select="$i" />
		</xsl:call-template>
		<xsl:if test="string-length($first)!=0">
			<xsl:call-template name="xpaths">
				<xsl:with-param name="selector" select="$selector" />
				<xsl:with-param name="value" select="substring-after($value,' ')" />
				<xsl:with-param name="index" select="$i + 1" />
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>
