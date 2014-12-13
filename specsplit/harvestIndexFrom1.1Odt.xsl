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

  <!-- top level appendices -->
  <xsl:template match="text:list[@text:style-name='Appendix'][text:list-item/text:p]">
    <xsl:variable name="number" select="count(preceding-sibling::text:list[@text:style-name='Appendix'][text:list-item/text:p])+1"/>
    <xsl:variable name="text" select="."/>
    <i number="{translate($number,'123456789','ABCDEFGHI')}" text="{$text}"/>
  </xsl:template>

  <xsl:template name="getAppendixNumber">
    <xsl:variable name="prec" select="preceding-sibling::text:list[@text:style-name='Appendix'][1]"/>
    <xsl:variable name="p">
      <xsl:choose>
        <xsl:when test="$prec/text:list-item/text:p">
          <xsl:value-of select="0"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="$prec">
            <xsl:call-template name="getAppendixNumber"/>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="$p + 1"/>
  </xsl:template>

  <xsl:template match="text:list[@text:style-name='Appendix']/text:list-item/text:list">
    <xsl:variable name="number" select="count(../../preceding-sibling::text:list[@text:style-name='Appendix'][text:list-item/text:p]|../text:p)"/>
    <xsl:variable name="n">
      <xsl:for-each select="../..">
        <xsl:call-template name="getAppendixNumber"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="m" select="number(not(../text:p)) + $n"/>
    <xsl:variable name="text" select="."/>
    <i number="{translate($number,'123456789','ABCDEFGHI')}.{$m}" text="{$text}"/>
  </xsl:template>

  <xsl:template match="text:h">
    <xsl:variable name="number">
      <xsl:call-template name="getNumber"/>
    </xsl:variable>
    <xsl:variable name="ref" select="text:bookmark-start/@text:name|text-bookmark/@text:name"/>
    <xsl:variable name="text" select="."/>

    <xsl:if test="not($number) or not($text) or string-length($text) = 0">
      <xsl:message>
        <xsl:value-of select="concat('Incomplete reference: ref=',$ref,' number=',$number,' text=',$text,'.')"/>
      </xsl:message>
    </xsl:if>
 
    <i number="{$number}" text="{$text}">
      <xsl:if test="string-length($ref) > 0">
        <xsl:attribute name="ref">
          <xsl:value-of select="$ref"/>
        </xsl:attribute>
      </xsl:if>
    </i>
  </xsl:template>
  <xsl:template match="*"/>
  <xsl:template match="/">
    <a>
      <xsl:apply-templates select=".//*"/>
    </a>
  </xsl:template>
</xsl:stylesheet>
