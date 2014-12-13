<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:h="http://www.w3.org/1999/xhtml"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output encoding="utf-8" indent="yes" method="xml"
    omit-xml-declaration="no"></xsl:output>
  <xsl:template match="h:h1|h:h2|h:h3|h:h4|h:h5|h:h6">
    <xsl:variable name="number" select="h:a[1]"/>
    <xsl:variable name="ref1" select="h:a[2]/@id"/>
    <xsl:variable name="ref">
       <xsl:if test="not($ref1)">
         <xsl:value-of select="h:a[1]/@id"/>
       </xsl:if>
       <xsl:value-of select="$ref1"/>
    </xsl:variable>
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
      <xsl:apply-templates select="document('v1.2/OpenDocument-v1.2-part1.html')//*"/>
      <xsl:apply-templates select="document('v1.2/OpenDocument-v1.2-part2.html')//*"/>
      <xsl:apply-templates select="document('v1.2/OpenDocument-v1.2-part3.html')//*"/>
    </a>
  </xsl:template>
</xsl:stylesheet>
