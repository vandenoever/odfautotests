<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:h="http://www.w3.org/1999/xhtml"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output encoding="utf-8" indent="yes" method="xml"
    omit-xml-declaration="no"/>
  <xsl:template match="h:h1|h:h2|h:h3|h:h4|h:h5|h:h6">
    <xsl:variable name="text" select="normalize-space(text())"/>
    <xsl:variable name="number" select="substring-before($text,' ')"/>
    <xsl:variable name="validNumber1" select="string-length($number) > 0 and string-length(substring(translate($number,'ABCDEF0123456789.',''),1,1)) = 0"/>
    <xsl:variable name="validNumber2" select="string-length($number) > 0 and string-length(substring(translate($number,'0123456789.',''),2)) = 0"/>
    <xsl:variable name="validNumber" select="$validNumber1 and $validNumber2"/>
    <xsl:choose>
      <xsl:when test="$validNumber">
        <!-- remove trailing '.' from number -->
        <xsl:variable name="l" select="string-length($number)"/>
        <xsl:variable name="last" select="translate(substring($number,$l),'.','')"/>
        <i number="{substring($number,1,$l - 1)}{$last}" text="{substring-after($text,' ')}"/>
      </xsl:when>
      <xsl:when test="starts-with($text,'Appendix ')">
        <i number="{substring($text,10,1)}" text="{substring($text,12)}"/>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
    <xsl:message>
      <xsl:value-of select="concat($number,' ',$text)"/>
    </xsl:message>
  </xsl:template>
  <xsl:template match="*"/>
  <xsl:template match="/">
    <a>
      <xsl:apply-templates select=".//*"/>
    </a>
  </xsl:template>
</xsl:stylesheet>
