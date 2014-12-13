<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:h="http://www.w3.org/1999/xhtml"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0">
  <xsl:output encoding="utf-8" indent="yes" method="xml"
    omit-xml-declaration="no"></xsl:output>

  <xsl:template name="pos">
    <xsl:param name="poslevel"/>
    <xsl:variable name="level" select="@text:outline-level"/>
    <xsl:variable name="pos">
      <xsl:value-of select="0"/>
      <xsl:for-each select="preceding::text:h[1]">
        <xsl:variable name="prevLevel" select="@text:outline-level"/>
        <xsl:choose>
          <xsl:when test="$prevLevel >= $poslevel">
            <xsl:call-template name="pos">
              <xsl:with-param name="poslevel" select="$poslevel"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="0"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:variable>
    <xsl:value-of select="$pos + number($poslevel = $level)"/>
  </xsl:template>

  <xsl:template name="getNumber">
    <xsl:variable name="level" select="@text:outline-level"/>
    <xsl:variable name="pos">
      <xsl:call-template name="pos">
        <xsl:with-param name="poslevel" select="$level"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:if test="$level > 1">
      <xsl:for-each select="preceding::text:h[@text:outline-level=$level - 1][1]">
        <xsl:call-template name="getNumber"/>
      </xsl:for-each>
      <xsl:value-of select="'.'"/>
    </xsl:if>
    <xsl:value-of select="$pos"/>
  </xsl:template>

  <xsl:template match="text:h">
    <xsl:variable name="number">
      <xsl:call-template name="getNumber"/>
    </xsl:variable>
    <xsl:variable name="ref" select="text:bookmark/@text:name"/>
    <xsl:variable name="text" select="text()"/>

    <xsl:if test="not($ref) or not($number) or not($text)">
      <xsl:message>
        <xsl:value-of select="concat('Incomplete reference: ref=',$ref,' number=',$number,' text=',$text,'.')"/>
      </xsl:message>
    </xsl:if>
 
    <i ref="{$ref}" number="{$number}" text="{$text}"/>
  </xsl:template>
  <xsl:template match="*"/>
  <xsl:template match="/">
    <a>
      <xsl:apply-templates select=".//text:h"/>
    </a>
  </xsl:template>
</xsl:stylesheet>
