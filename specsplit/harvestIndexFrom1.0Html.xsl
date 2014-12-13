<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:h="http://www.w3.org/1999/xhtml"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output encoding="utf-8" indent="yes" method="xml"
    omit-xml-declaration="no"/>

  <xsl:template match="h:b">
    <xsl:variable name="text" select="text()"/>
    <xsl:variable name="number" select="substring-before($text,'&#160;')"/>
    <xsl:variable name="validNumber" select="string-length($number) > 0 and string-length(translate($number,'ABCDEF0123456789.','')) = 0"/>
    <xsl:choose>
      <xsl:when test="$validNumber">
        <!-- remove trailing '.' from number -->
        <xsl:variable name="l" select="string-length($number)"/>
        <xsl:variable name="last" select="translate(substring($number,$l),'.','')"/>
        <i number="{substring($number,1,$l - 1)}{$last}" text="{substring-after($text,'&#160;')}"/>
      </xsl:when>
      <xsl:when test="starts-with($text,'Appendix&#160;')">
        <i number="{substring($text,10,1)}" text="{substring($text,12)}"/>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/">
    <a>
      <!-- match <p/> that has only <b/> as child element -->
      <xsl:apply-templates select=".//h:p/h:b[not(preceding-sibling::h:*) and not(following-sibling::h:*)]"/>
    </a>
  </xsl:template>
</xsl:stylesheet>
