<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output encoding="utf-8" indent="yes" method="xml"
        omit-xml-declaration="no"></xsl:output>
    <xsl:template match="@*|node()">
        <xsl:if test="namespace-uri(.)!='http://www.calligra.org/2005/'">
            <xsl:copy>
                <xsl:apply-templates select="@*|node()" />
            </xsl:copy>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
