<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
    xmlns:manifest="urn:oasis:names:tc:opendocument:xmlns:manifest:1.0"
    xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0"
    xmlns:o="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
    xmlns:odf="http://docs.oasis-open.org/ns/office/1.2/meta/odf#"
    xmlns:s="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
    xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:t="http://www.example.org/documenttests"
    xmlns="http://www.example.org/documenttests">

    <t:testtemplates>
        <t:testtemplate name="singleinsertiontext" location="office_text">
            <t:input>
                <t:bodyinput>
                    <t:singleinsertiontexttrackedchanges id="otit"/>
                </t:bodyinput>
            </t:input>
            <t:output>
                <t:singleinsertiontexttrackedchangesxpaths filename="content.xml" elementname="o:text"/>
            </t:output>
        </t:testtemplate>

        <t:testtemplate name="singledeletiontext" location="office_text">
            <t:input>
                <t:bodyinput>
                    <t:singledeletiontexttrackedchanges id="otdt"/>
                </t:bodyinput>
            </t:input>
            <t:output>
                <t:singledeletiontexttrackedchangesxpaths filename="content.xml" elementname="o:text"/>
            </t:output>
        </t:testtemplate>

        <t:testtemplate name="singleformatchange" location="office_text">
            <t:input>
                <t:autostylesinput>
                    <t:singleformatchangeautostyles/>
                </t:autostylesinput>
                <t:bodyinput>
                    <t:singleformatchangetrackedchanges id="otfc"/>
                </t:bodyinput>
            </t:input>
            <t:output>
                <t:singleformatchangetrackedchangesxpaths filename="content.xml" elementname="o:text"/>
            </t:output>
        </t:testtemplate>

        <t:testtemplate name="singleinsertiontext" location="style_footer">
            <t:input>
                <t:footerinput stylefooterelementname="s:footer">
                    <t:singleinsertiontexttrackedchanges id="sfit"/>
                </t:footerinput>
            </t:input>
            <t:output>
                <t:singleinsertiontexttrackedchangesxpaths filename="styles.xml" elementname="s:footer"/>
            </t:output>
        </t:testtemplate>

        <t:testtemplate name="singledeletiontext" location="style_footer">
            <t:input>
                <t:footerinput stylefooterelementname="s:footer">
                    <t:singledeletiontexttrackedchanges id="sfdt"/>
                </t:footerinput>
            </t:input>
            <t:output>
                <t:singledeletiontexttrackedchangesxpaths filename="styles.xml" elementname="s:footer"/>
            </t:output>
        </t:testtemplate>

        <t:testtemplate name="singleformatchange" location="style_footer">
            <t:input>
                <t:footerinput stylefooterelementname="s:footer">
                    <t:footerautostyles>
                        <t:singleformatchangeautostyles/>
                    </t:footerautostyles>
                    <t:singleformatchangetrackedchanges id="sffc"/>
                </t:footerinput>
            </t:input>
            <t:output>
                <t:singleformatchangetrackedchangesxpaths filename="styles.xml" elementname="s:footer"/>
            </t:output>
        </t:testtemplate>

        <t:testtemplate name="singleinsertiontext" location="style_footer_left">
            <t:input>
                <t:footerinput stylefooterelementname="s:footer-left">
                    <t:singleinsertiontexttrackedchanges id="sflit"/>
                </t:footerinput>
            </t:input>
            <t:output>
                <t:singleinsertiontexttrackedchangesxpaths filename="styles.xml" elementname="s:footer-left"/>
            </t:output>
        </t:testtemplate>

        <t:testtemplate name="singledeletiontext" location="style_footer_left">
            <t:input>
                <t:footerinput stylefooterelementname="s:footer-left">
                    <t:singledeletiontexttrackedchanges id="sfldt"/>
                </t:footerinput>
            </t:input>
            <t:output>
                <t:singledeletiontexttrackedchangesxpaths filename="styles.xml" elementname="s:footer-left"/>
            </t:output>
        </t:testtemplate>

        <t:testtemplate name="singleformatchange" location="style_footer_left">
            <t:input>
                <t:footerinput stylefooterelementname="s:footer-left">
                    <t:footerautostyles>
                        <t:singleformatchangeautostyles/>
                    </t:footerautostyles>
                    <t:singleformatchangetrackedchanges id="sflfc"/>
                </t:footerinput>
            </t:input>
            <t:output>
                <t:singleformatchangetrackedchangesxpaths filename="styles.xml" elementname="s:footer-left"/>
            </t:output>
        </t:testtemplate>
    </t:testtemplates>

    <xsl:output encoding="utf-8" indent="no" method="xml"
        omit-xml-declaration="no" />

    <xsl:template match="t:singleinsertiontexttrackedchanges">
        <text:tracked-changes text:track-changes="false">
            <text:changed-region text:id="{@id}">
                <xsl:attribute name="xml:id"><xsl:value-of select="@id"/></xsl:attribute>
                <text:insertion>
                    <o:change-info>
                        <dc:creator>Test</dc:creator>
                        <dc:date>2014-12-09T12:33:00</dc:date>
                        <text:p>Comment 1</text:p>
                        <text:p>Comment 2</text:p>
                    </o:change-info>
                </text:insertion>
            </text:changed-region>
        </text:tracked-changes>
        <text:p><text:change-start text:change-id="{@id}"/>Hello<text:change-end text:change-id="{@id}"/></text:p>
    </xsl:template>

    <xsl:template match="t:singledeletiontexttrackedchanges">
        <text:tracked-changes text:track-changes="false">
            <text:changed-region text:id="{@id}">
                <xsl:attribute name="xml:id"><xsl:value-of select="@id"/></xsl:attribute>
                <text:deletion>
                    <o:change-info>
                        <dc:creator>Test</dc:creator>
                        <dc:date>2014-12-09T12:33:00</dc:date>
                        <text:p>Comment 1</text:p>
                        <text:p>Comment 2</text:p>
                    </o:change-info>
                </text:deletion>
            </text:changed-region>
        </text:tracked-changes>
        <text:p><text:change-start text:change-id="{@id}"/>Hello<text:change-end text:change-id="{@id}"/></text:p>
    </xsl:template>

    <xsl:template match="t:singleformatchangetrackedchanges">
        <text:tracked-changes text:track-changes="false">
            <text:changed-region text:id="{@id}">
                <xsl:attribute name="xml:id"><xsl:value-of select="@id"/></xsl:attribute>
                <text:format-change>
                    <o:change-info>
                        <dc:creator>Test</dc:creator>
                        <dc:date>2014-12-09T12:33:00</dc:date>
                        <text:p>Comment 1</text:p>
                        <text:p>Comment 2</text:p>
                    </o:change-info>
                </text:format-change>
            </text:changed-region>
        </text:tracked-changes>
        <text:p>Hello <text:change-start text:change-id="{@id}"/><text:span text:style-name="T1">World</text:span><text:change-end text:change-id="{@id}"/>!</text:p>
    </xsl:template>

    <xsl:template match="t:singleformatchangeautostyles">
        <s:style s:name="T1" s:family="text">
            <s:text-properties fo:font-weight="bold" s:font-weight-asian="bold" s:font-weight-complex="bold"/>
        </s:style>
    </xsl:template>

    <xsl:template match="t:autostylesinput">
        <o:automatic-styles>
            <xsl:apply-templates select="*" />
        </o:automatic-styles>
    </xsl:template>

    <xsl:template match="t:bodyinput">
        <o:text>
            <xsl:apply-templates select="*" />
        </o:text>
    </xsl:template>

    <xsl:template match="t:footerinput">
        <o:document-styles>
            <o:automatic-styles>
                <xsl:apply-templates select="t:footerautostyles/*" />
                <s:page-layout s:name="cols"/>
            </o:automatic-styles>
            <o:master-styles>
                <s:master-page s:name="Test" s:page-layout-name="cols">
                    <!-- TODO: seems at least LO ignores style:header-left without any style:header. Does that make sense, so should we add one here in case of style:header-left? -->
                    <xsl:if test="@stylefooterelementname='s:footer-left'">
                        <s:footer><text:p></text:p></s:footer>
                    </xsl:if>
                    <xsl:element name="{@stylefooterelementname}">
                        <xsl:apply-templates select="*" />
                    </xsl:element>
                </s:master-page>
            </o:master-styles>
        </o:document-styles>
    </xsl:template>

    <xsl:template match="t:singleinsertiontexttrackedchangesxpaths">
        <xsl:variable name="basePath">
            <xsl:choose>
                <xsl:when test="@elementname='o:text'">//o:text</xsl:when>
                <xsl:otherwise>//o:document-styles/o:master-styles/s:master-page[@s:name='Test']/<xsl:value-of select="@elementname" /></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <file path="{@filename}">
            <!-- id not defined to be the same after roundtrip, at least LO does not do it -->
            <xpath expr="count({$basePath}/text:tracked-changes/text:changed-region[@text:id or @id]/text:insertion)=1" />
            <!-- test change info -->
            <xpath expr="{$basePath}/text:tracked-changes/text:changed-region/text:insertion/o:change-info/dc:creator='Test'" />
            <xpath expr="{$basePath}/text:tracked-changes/text:changed-region/text:insertion/o:change-info/dc:date='2014-12-09T12:33:00'" />
            <!-- test change info comments -->
            <xpath expr="count({$basePath}/text:tracked-changes/text:changed-region/text:insertion/o:change-info/text:p)=2" />
            <xpath expr="{$basePath}/text:tracked-changes/text:changed-region/text:insertion/o:change-info/text:p[1]='Comment 1'" />
            <xpath expr="{$basePath}/text:tracked-changes/text:changed-region/text:insertion/o:change-info/text:p[2]='Comment 2'" />
            <!-- test text:change-start and text:change-end -->
            <xpath expr="count({$basePath}/text:p)=1" />
            <xpath expr="count({$basePath}/text:p/text:change-start/@text:change-id)=1" />
            <xpath expr="({$basePath}/text:p/text:change-start/@text:change-id = {$basePath}/text:p/text:change-end/@text:change-id)" />
            <xpath expr="count({$basePath}/text:p/text:change-end[preceding::text:change-start])=1" />
            <!-- TODO: if both xml:id and text:id are set, they need to be equal perhaps -->
            <xpath expr="({$basePath}/text:tracked-changes/text:changed-region/@text:id = {$basePath}/text:p/text:change-start//@text:change-id) or ({$basePath}/text:tracked-changes/text:changed-region/@id = {$basePath}/text:p/text:change-start//@text:change-id)" />
        </file>
    </xsl:template>

    <xsl:template match="t:singledeletiontexttrackedchangesxpaths">
        <xsl:variable name="basePath">
            <xsl:choose>
                <xsl:when test="@elementname='o:text'">//o:text</xsl:when>
                <xsl:otherwise>//o:document-styles/o:master-styles/s:master-page[@s:name='Test']/<xsl:value-of select="@elementname" /></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <file path="{@filename}">
            <!-- id not defined to be the same after roundtrip, at least LO does not do it -->
            <xpath expr="count({$basePath}/text:tracked-changes/text:changed-region[@text:id or @id]/text:deletion)=1" />
            <!-- test change info -->
            <xpath expr="{$basePath}/text:tracked-changes/text:changed-region/text:deletion/o:change-info/dc:creator='Test'" />
            <xpath expr="{$basePath}/text:tracked-changes/text:changed-region/text:deletion/o:change-info/dc:date='2014-12-09T12:33:00'" />
            <!-- test change info comments -->
            <xpath expr="count({$basePath}/text:tracked-changes/text:changed-region/text:deletion/o:change-info/text:p)=2" />
            <xpath expr="{$basePath}/text:tracked-changes/text:changed-region/text:deletion/o:change-info/text:p[1]='Comment 1'" />
            <xpath expr="{$basePath}/text:tracked-changes/text:changed-region/text:deletion/o:change-info/text:p[2]='Comment 2'" />
            <!-- test text:change-start and text:change-end -->
            <xpath expr="count({$basePath}/text:p)=1" />
<!-- TODO: there can be both inline and out-of-line placement of the deleted text, that needs proper testing -->
<!--             <xpath expr="count({$basePath}/text:p/text:change-start/@text:change-id)=1" /> -->
<!--             <xpath expr="({$basePath}/text:p/text:change-start/@text:change-id = {$basePath}/text:p/text:change-end/@text:change-id)" />  -->
<!--             <xpath expr="count({$basePath}/text:p/text:change-end[preceding::t:change-start])=1" /> -->
                <!-- TODO: if both xml:id and text:id are set, they need to be equal perhaps -->
<!--             <xpath expr="({$basePath}/text:tracked-changes/text:changed-region/@t:id = {$basePath}/text:p/text:change-start//@text:change-id) or ({$basePath}/text:tracked-changes/text:changed-region/@id = {$basePath}/text:p/text:change-start//@text:change-id)" /> -->
        </file>
    </xsl:template>

    <xsl:template match="t:singleformatchangetrackedchangesxpaths">
        <xsl:variable name="basePath">
            <xsl:choose>
                <xsl:when test="@elementname='o:text'">//o:text</xsl:when>
                <xsl:otherwise>//o:document-styles/o:master-styles/s:master-page[@s:name='Test']/<xsl:value-of select="@elementname" /></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <file path="{@filename}">
            <!-- id not defined to be the same after roundtrip, at least LO does not do it -->
            <xpath expr="count({$basePath}/text:tracked-changes/text:changed-region[@text:id or @id]/text:format-change)=1" />
            <!-- test change info -->
            <xpath expr="{$basePath}/text:tracked-changes/text:changed-region/text:format-change/o:change-info/dc:creator='Test'" />
            <xpath expr="{$basePath}/text:tracked-changes/text:changed-region/text:format-change/o:change-info/dc:date='2014-12-09T12:33:00'" />
            <!-- test change info comments -->
            <xpath expr="count({$basePath}/text:tracked-changes/text:changed-region/text:format-change/o:change-info/text:p)=2" />
            <xpath expr="{$basePath}/text:tracked-changes/text:changed-region/text:format-change/o:change-info/text:p[1]='Comment 1'" />
            <xpath expr="{$basePath}/text:tracked-changes/text:changed-region/text:format-change/o:change-info/text:p[2]='Comment 2'" />
            <!-- test text:change-start and text:change-end -->
            <xpath expr="count({$basePath}/text:p)=1" />
            <xpath expr="count({$basePath}/text:p/text:change-start/@text:change-id)=1" />
            <xpath expr="({$basePath}/text:p/text:change-start/@text:change-id = {$basePath}/text:p/text:change-end/@text:change-id)" />
            <xpath expr="count({$basePath}/text:p/text:change-end[preceding::text:change-start])=1" />
            <!-- TODO: if both xml:id and text:id are set, they need to be equal perhaps -->
            <xpath expr="({$basePath}/text:tracked-changes/text:changed-region/@text:id = {$basePath}/text:p/text:change-start//@text:change-id) or ({$basePath}/text:tracked-changes/text:changed-region/@id = {$basePath}/text:p/text:change-start//@text:change-id)" />
        </file>
    </xsl:template>

    <xsl:template match="t:input|t:output">
        <xsl:apply-templates select="*" />
    </xsl:template>

    <xsl:template match="t:testtemplate">
        <test name="{@name}_{@location}">
            <input type="odt1.2">
                <xsl:apply-templates select="t:input" />
            </input>
            <output types="odt1.2 odt1.2ext"><!-- TODO: other types as well? -->
                <xsl:apply-templates select="t:output" />
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
