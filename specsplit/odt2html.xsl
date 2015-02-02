<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0" xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0" xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0" xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0" xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:h="http://www.w3.org/1999/xhtml" xmlns:xsd="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xsl text office draw table style h math xlink xsd svg c" xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0" xmlns:c="urn:c" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:math="http://www.w3.org/1998/Math/MathML">
  <xsl:output encoding="utf-8" indent="yes" method="xhtml"/>

  <xsl:key name="automaticParagraphStyle" match="office:automatic-styles/style:style[@style:family='paragraph']" use="@style:name"/>

  <xsl:function name="c:automaticParagraphStyle" as="element()?">
    <xsl:param name="name" as="xsd:string"/>
    <xsl:param name="context" as="element()"/>
    <xsl:variable name="root" select="$context/ancestor::office:*[last()]"/>
    <xsl:copy-of select="key('automaticParagraphStyle', $name, $root)"/>
  </xsl:function>

  <xsl:function name="c:getDocumentName" as="xsd:string">
    <xsl:param name="doc" as="document-node()"/>
    <xsl:variable name="one" select="$doc/office:*/office:body/office:text/text:p[starts-with(.,'Open Document Format for Office Applications (OpenDocument)')][1]"/>
    <xsl:variable name="two" select="$one/following-sibling::*[1][starts-with(.,'Part')]"/>
    <xsl:value-of select="concat($one,if ($two) then ', ' else '',$two)"/>
  </xsl:function>

  <xsl:function name="c:getDocumentPath" as="xsd:string">
    <xsl:param name="context" as="element()"/>
    <xsl:variable name="root" select="$context/ancestor::office:*[last()]"/>
    <xsl:variable name="source" select="$root/office:body/office:text/text:p[.='This version:'][1]/following-sibling::text:p[1]/text:a[1]/@xlink:href"/>
    <xsl:choose>
      <xsl:when test="$source='http://docs.oasis-open.org/office/v1.1/errata01/os/OpenDocument-v1.1-errata01-os-complete.odt'">
        <xsl:value-of select="'v1.1'"/>
      </xsl:when>
      <xsl:when test="$source='http://docs.oasis-open.org/office/v1.2/os/OpenDocument-v1.2-os.odt'">
        <xsl:value-of select="'v1.2'"/>
      </xsl:when>
      <xsl:when test="$source='http://docs.oasis-open.org/office/v1.2/os/OpenDocument-v1.2-os-part1.odt'">
        <xsl:value-of select="'v1.2-part1'"/>
      </xsl:when>
      <xsl:when test="$source='http://docs.oasis-open.org/office/v1.2/os/OpenDocument-v1.2-os-part2.odt'">
        <xsl:value-of select="'v1.2-part2'"/>
      </xsl:when>
      <xsl:when test="$source='http://docs.oasis-open.org/office/v1.2/os/OpenDocument-v1.2-os-part3.odt'">
        <xsl:value-of select="'v1.2-part3'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          <xsl:value-of select="concat($source, 'is unknown.')"/>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:template match="text()">
    <xsl:copy/>
  </xsl:template>

  <xsl:template match="office:forms"/>
  <xsl:template match="text:sequence-decls"/>
  <xsl:template match="text:variable-decls"/>
  <xsl:template match="text:variable-set"/>
  <xsl:template match="text:user-field-decls"/>

  <xsl:template match="text:bookmark">
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="text:bookmark-start"/>
  <xsl:template match="text:bookmark-end"/>
  <xsl:template match="text:reference-mark">
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="text:reference-mark-start"/>
  <xsl:template match="text:reference-mark-end"/>
  <xsl:template match="text:alphabetical-index-mark">
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="text:alphabetical-index-mark-start"/>
  <xsl:template match="text:alphabetical-index-mark-end"/>
  <xsl:template match="text:toc-mark">
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="text:toc-mark-start"/>
  <xsl:template match="text:toc-mark-end"/>
  <xsl:template match="text:bibliography-mark">
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="text:bibliography-mark-start"/>
  <xsl:template match="text:bibliography-mark-end"/>

  <xsl:template match="text:table-of-content">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="text:table-of-content-source"/>

  <xsl:template match="text:bibliography">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="text:bibliography-source"/>

  <xsl:template match="text:index-body">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="text:user-field-get">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="text:user-field-input">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="text:sequence-ref">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="text:sequence">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="text:page-number">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="text:tab">
    <xsl:value-of select="'&#9;'"/>
  </xsl:template>

  <xsl:template match="draw:frame">
    <div>
      <xsl:if test="@text:anchor-type='as-char'">
        <xsl:attribute name="class">
          <xsl:value-of select="'inline'"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </div>
  </xsl:template>

  <xsl:template match="draw:image">
    <xsl:if test="not(preceding-sibling::*[1][self::draw:object])">
      <img src="{@xlink:href}" alt=""/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="draw:text-box">
    <div>
      <xsl:apply-templates/>
    </div>
  </xsl:template>

  <xsl:template match="svg:desc"/>

  <xsl:template match="draw:object">
    <xsl:variable name="path" select="c:getDocumentPath(.)"/>
    <xsl:variable name="math" select="document(concat('out/',$path,'/',@xlink:href,'/content.xml'))/math:math"/>
    <xsl:choose>
      <xsl:when test="$math">
        <xsl:copy-of select="$math"/>
      </xsl:when>
      <xsl:otherwise>
        <object data="{@xlink:href}/content.xml">
          <xsl:if test="following-sibling::*[1][self::draw:image]">
            <img src="{following-sibling::draw:image/@xlink:href}"/>
          </xsl:if>
          <xsl:if test="not(following-sibling::*[1][self::draw:image])">
            <iframe src="{@xlink:href}/content.xml"/>
          </xsl:if>
        </object>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="draw:circle">
    <!-- TODO -->
  </xsl:template>

  <xsl:template match="draw:g">
    <!-- TODO -->
  </xsl:template>

  <xsl:template match="table:table">
    <table>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="table:table-column">
    <col>
      <xsl:apply-templates/>
    </col>
  </xsl:template>

  <xsl:template match="table:table-header-rows">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="table:table-row">
    <tr>
      <xsl:apply-templates/>
    </tr>
  </xsl:template>

  <xsl:template match="table:table-cell">
    <xsl:variable name="name" select="if (..[self::table:table-header-rows]/..[self::table:table-header-rows]) then 'th' else 'td'"/> 
    <xsl:element name="{$name}">
      <xsl:if test="@table:number-columns-spanned > 1">
        <xsl:attribute name="colspan">
          <xsl:value-of select="@table:number-columns-spanned"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@table:number-rows-spanned > 1">
        <xsl:attribute name="rowspan">
          <xsl:value-of select="@table:number-rows-spanned"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="table:covered-table-cell"/>

  <xsl:template match="text:conditional-text">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="text:soft-page-break"/>

  <xsl:template match="*">
    <xsl:message terminate="yes">
      <xsl:value-of select="concat(namespace-uri(),' ',local-name())"/>
    </xsl:message>
  </xsl:template>

  <xsl:template match="text:span">
    <span class="{@text:style-name}">
     <xsl:apply-templates/>
    </span>
  </xsl:template>

  <xsl:template match="text:a">
    <xsl:variable name="h" select="@xlink:href"/>
    <xsl:variable name="prefix">
      <xsl:choose>
        <xsl:when test="contains($h, '-part1')">
          <xsl:value-of select="'../v1.2-part1/'"/>
        </xsl:when>
        <xsl:when test="contains($h, '-part2')">
          <xsl:value-of select="'../v1.2-part2/'"/>
        </xsl:when>
        <xsl:when test="contains($h, '-part3')">
          <xsl:value-of select="'../v1.2-part3/'"/>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="href">
      <xsl:choose>
        <xsl:when test="contains(@xlink:href,'.odt#__RefHeading__') or starts-with($h,'#__RefHeading__')">
          <xsl:variable name="id" select="replace(.,'(Appendix )?([A-Z0-9.]*[A-Z0-9])(\.)? .*','$2')"/>
          <xsl:value-of select="concat($prefix,$id,'.xhtml')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@xlink:href"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <a href="{$href}">
     <xsl:apply-templates/>
    </a>
  </xsl:template>

  <xsl:template match="text:line-break">
    <br>
     <xsl:apply-templates/>
    </br>
  </xsl:template>

  <xsl:template match="text:s">
    <!-- TODO @count -->
    <xsl:value-of select="' '"/>
  </xsl:template>

  <xsl:template match="text:reference-ref">
    <a href="{.}.xhtml">
     <xsl:apply-templates/>
    </a>
  </xsl:template>

  <xsl:template match="text:bookmark-ref">
    <a href="{.}.xhtml">
     <xsl:apply-templates/>
    </a>
  </xsl:template>

  <xsl:function name="c:isBox" as="xsd:boolean">
    <xsl:param name="a" as="element()"/>
    <xsl:variable name="s" select="$a/@text:style-name" as="xsd:string"/>
    <xsl:value-of select="$s = 'Parent_20_Element_20_List' or $s = 'Attribute_20_List' or $s = 'Child_20_Element_20_List' or $s = 'Attribute_20_Value_20_List'"/>
  </xsl:function>

  <xsl:function name="c:isCode" as="xsd:boolean">
    <xsl:param name="a" as="element()"/>
    <xsl:variable name="c" select="$a/@text:style-name" as="xsd:string"/>
    <xsl:variable name="p" select="c:automaticParagraphStyle($c,$a)/@style:parent-style-name"/>
    <xsl:variable name="s" select="if ($p) then $p else $c"/>
    <xsl:value-of select="$s = 'RelaxNG' or $s = 'RelaxNG_20_Manifest' or $s = 'Example' or $s = 'Example_20_end' or $s = 'Code'"/>
  </xsl:function>

  <xsl:template match="text:section">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="text:p">
    <xsl:variable name="class">
      <xsl:variable name="s" select="@text:style-name"/>
      <xsl:choose>
        <xsl:when test="c:isBox(.) and count(preceding-sibling::text:p[1][c:isBox(.)]) = 0">
          <xsl:value-of select="'boxStart'"/>
        </xsl:when>
        <xsl:when test="c:isBox(.) and count(following-sibling::text:p[1][c:isBox(.)]) = 0">
          <xsl:value-of select="'boxEnd'"/>
        </xsl:when>
        <xsl:when test="c:isBox(.)">
          <xsl:value-of select="'box'"/>
        </xsl:when>
        <xsl:when test="c:isCode(.) and count(preceding-sibling::text:p[1][c:isCode(.)]) = 0">
          <xsl:value-of select="concat($s, ' codeStart')"/>
        </xsl:when>
        <xsl:when test="c:isCode(.) and count(following-sibling::text:p[1][c:isCode(.)]) = 0">
          <xsl:value-of select="concat($s, ' codeEnd')"/>
        </xsl:when>
        <xsl:when test="c:isCode(.)">
          <xsl:value-of select="concat($s, ' code')"/>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <p>
      <xsl:if test="string-length($class)">
        <xsl:attribute name="class">
          <xsl:value-of select="$class"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <xsl:template match="text:list-item">
    <li>
      <xsl:apply-templates/>
    </li>
  </xsl:template>

  <xsl:template match="text:list-header">
    <p>
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <xsl:template match="text:list">
    <ul>
      <xsl:apply-templates/>
    </ul>
  </xsl:template>

  <xsl:template match="text:h">
    <xsl:param name="number"/>
    <xsl:variable name="level" select="@text:outline-level" as="xsd:integer"/>
    <xsl:element name="h{$level}">
      <xsl:if test="$number">
        <a href="{$number}.xhtml">
          <span class="number">
            <xsl:value-of select="$number"/>
          </span>
          <xsl:value-of select="' '"/>
          <xsl:apply-templates/>
        </a>
      </xsl:if>
      <xsl:if test="not($number)">
        <xsl:apply-templates/>
      </xsl:if>
    </xsl:element>
  </xsl:template>

  <xsl:template name="appendices3">
    <xsl:param name="group"/>
    <xsl:param name="numberPrefix"/>
    <xsl:variable name="idPrefix" select="c:getDocumentPath(.)"/>
    <xsl:variable name="offset" select="if ($group[1]/text:list-item/text:list/text:list-item/text:list/text:list-item/text:p) then 0 else 1"/>
    <xsl:for-each-group select="$group" group-starting-with="text:list[@text:style-name='Appendix'][text:list-item/text:list/text:list-item/text:list/text:list-item/text:p]">
      <xsl:if test="self::text:list[@text:style-name='Appendix' and text:list-item/text:list/text:list-item/text:list/text:list-item/text:p]">
        <xsl:variable name="number" select="concat($numberPrefix,position() - $offset)"/>
        <div id="{$idPrefix}_{$number}">
          <h2>
            <a href="{$number}.xhtml">
              <span class="number">
                <xsl:value-of select="$number"/>
              </span>
              <xsl:value-of select="'. '"/>
              <xsl:apply-templates select="text:list-item/text:list/text:list-item/text:list/text:list-item/text:p/node()"/>
            </a>
          </h2>
        </div>
      </xsl:if>
      <xsl:apply-templates select="current-group() except ."/>
    </xsl:for-each-group>
  </xsl:template>

  <xsl:template name="appendices2">
    <xsl:param name="group"/>
    <xsl:param name="numberPrefix"/>
    <xsl:variable name="idPrefix" select="c:getDocumentPath(.)"/>
    <xsl:variable name="offset" select="if ($group[1]/text:list-item/text:list/text:list-item/text:p) then 0 else 1"/>
    <xsl:for-each-group select="$group" group-starting-with="text:list[@text:style-name='Appendix'][text:list-item/text:list/text:list-item/text:p]">
      <xsl:choose>
        <xsl:when test="self::text:list[@text:style-name='Appendix' and text:list-item/text:list/text:list-item/text:p]">
          <xsl:variable name="number" select="concat($numberPrefix,position() - $offset)"/>
          <div id="{$idPrefix}_{$number}">
            <h2>
              <a href="{$number}.xhtml">
                <span class="number">
                  <xsl:value-of select="$number"/>
                </span>
                <xsl:value-of select="'. '"/>
                <xsl:apply-templates select="text:list-item/text:list/text:list-item/text:p/node()"/>
              </a>
            </h2>
            <xsl:call-template name="appendices3">
              <xsl:with-param name="group" select="current-group()"/>
              <xsl:with-param name="numberPrefix" select="concat($number,'.')"/>
            </xsl:call-template>
          </div>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="current-group() except ."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each-group>
  </xsl:template>

  <xsl:template name="appendices">
    <xsl:variable name="idPrefix" select="c:getDocumentPath(.)"/>
    <xsl:for-each-group select="node()" group-starting-with="text:list[@text:style-name='Appendix' and text:list-item/text:p]">
      <xsl:choose>
        <xsl:when test="self::text:list[@text:style-name='Appendix' and text:list-item/text:p]">
          <xsl:variable name="number" select="translate(string(position() - 1),'123456789','ABCDEFGHI')"/>
          <div id="{$idPrefix}_{$number}">
            <h1>
              <a href="{$number}.xhtml">
                <xsl:value-of select="'Appendix '"/>
                <span class="number">
                  <xsl:value-of select="$number"/>
                </span>
                <xsl:value-of select="'. '"/>
                <xsl:apply-templates select="text:list-item/text:p/node()"/>
              </a>
            </h1>
            <xsl:call-template name="appendices2">
              <xsl:with-param name="group" select="current-group()"/>
              <xsl:with-param name="numberPrefix" select="concat($number,'.')"/>
            </xsl:call-template>
          </div>
        </xsl:when>
        <xsl:when test="position() > 1">
          <xsl:apply-templates select="current-group() except ."/>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each-group>
  </xsl:template>

  <!-- Convert the content below a text:h of a given level to html.
       This function is called recursively for each possible header level. -->
  <xsl:template name="group">
    <xsl:param name="level" select="1"/>
    <xsl:param name="group" select="node()"/>
    <xsl:param name="numberPrefix" select="''"/>
    <xsl:variable name="first" select="$group[self::text:h and @text:outline-level=$level][1]"/>
    <xsl:variable name="head" select="$group[following-sibling::text:h[@text:outline-level=$level][1] is $first]"/>
    <xsl:variable name="body" select="$group except $head"/>
    <xsl:variable name="h" select="concat('h',$level)"/>
    <xsl:variable name="idPrefix" select="c:getDocumentPath(.)"/>
    <xsl:apply-templates select="$head"/>
    <xsl:for-each-group select="$body" group-starting-with="text:h[@text:outline-level=$level]">
      <xsl:choose>
        <xsl:when test="self::text:h[@text:outline-level=$level]">
          <xsl:variable name="number" select="concat($numberPrefix,position())"/>
          <div id="{$idPrefix}_{$number}">
            <xsl:apply-templates select=".">
              <xsl:with-param name="number" select="$number"/>
            </xsl:apply-templates>
            <xsl:call-template name="group">
              <xsl:with-param name="level" select="$level + 1"/>
              <xsl:with-param name="group" select="current-group() except ."/>
              <xsl:with-param name="numberPrefix" select="concat($number,'.')"/>
            </xsl:call-template>
          </div>
        </xsl:when>
        <xsl:when test="position() = last()">
          <xsl:variable name="firstAppendix" select="current-group()[self::text:list and @text:style-name='Appendix'][1]"/>
          <xsl:variable name="appendixNodes" select="$firstAppendix | current-group()[preceding-sibling::* = $firstAppendix]"/>
          <xsl:apply-templates select="current-group() except $appendixNodes"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="current-group()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each-group>
  </xsl:template>

  <!-- Convert the office:text element into an html tree -->
  <xsl:template match="office:text">
    <xsl:call-template name="group"/>
    <xsl:call-template name="appendices"/>
  </xsl:template>

  <xsl:template name="html">
    <xsl:param name="href" as="xsd:string" select="''"/>
    <xsl:param name="title" as="xsd:string"/>
    <xsl:param name="body" as="node()*"/>
    <xsl:result-document href="{$href}" method="xhtml" indent="yes" doctype-public="-//W3C//DTD XHTML 1.1 plus MathML 2.0 plus SVG 1.1//EN" doctype-system="http://www.w3.org/2002/04/xhtml-math-svg/xhtml-math-svg-flat.dtd">
      <html xml:lang="EN">
        <head>
          <title><xsl:copy-of select="$title"/></title>
          <link rel="stylesheet" type="text/css" href="../spec.css" />
        </head>
        <body>
          <xsl:copy-of select="$body"/>
        </body>
      </html>
    </xsl:result-document>
  </xsl:template>

  <xsl:template name="enhance">
    <xsl:param name="div" as="element()"/>
    <xsl:param name="documentName" as="xsd:string"/>
    <div id="{$div/@id}">
      <p>
        <a href="index.xhtml">
          <xsl:value-of select="$documentName"/>
        </a>
      </p>
      <xsl:for-each select="ancestor::h:div[position()>1]/*[h:a/h:span[@class='number']]">
        <p>
          <a href="{h:a/h:span[@class='number']}.xhtml#{$div/@id}">
            <xsl:value-of select="."/>
          </a>
        </p>
      </xsl:for-each>
      <xsl:copy-of select="$div/*"/>
    </div>
  </xsl:template>

  <!-- Write each section of the content.xml into a separate file.
       -->
  <xsl:template name="writeSections">
    <xsl:param name="tree"/>
    <xsl:param name="dir" as="xsd:string"/>
    <xsl:param name="version" as="xsd:string"/>
    <xsl:param name="documentName" as="xsd:string"/>
    <xsl:for-each select="$tree//h:span[@class='number']">
      <xsl:variable name="number" select="." as="xsd:string"/>
      <xsl:variable name="h" select=".." as="element()"/>
      <xsl:variable name="div" select="../../.." as="element()"/>
      <xsl:variable name="enhanced">
        <xsl:call-template name="enhance">
          <xsl:with-param name="div" select="$div"/>
          <xsl:with-param name="documentName" select="$documentName"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:call-template name="html">
        <xsl:with-param name="href" select="concat($dir,$version,'/',$number,'.xhtml')"/>
        <xsl:with-param name="title" select="$h"/>
        <xsl:with-param name="body" select="$enhanced"/>
      </xsl:call-template>
      <enumeration value="{$version}/{$number}"/>
    </xsl:for-each>
    <xsl:call-template name="html">
      <xsl:with-param name="href" select="concat($dir,$version,'/','all.xhtml')"/>
      <xsl:with-param name="title" select="'all'"/>
      <xsl:with-param name="body" select="$tree"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="index">
    <xsl:param name="tree"/>
    <xsl:for-each select="$tree//h:h1/h:a/h:span[@class='number']">
      <xsl:copy-of select="../.."/>
    </xsl:for-each>
    <xsl:copy-of select="$tree/node()[not(@id) and not(preceding-sibling::h:div[@id])]"/>
  </xsl:template>

  <!-- Convert one version of the specification.
       The file out/$v/content.xml is the input file. -->
  <xsl:template name="convert">
    <xsl:param name="v"/>
    <xsl:message>
      <xsl:value-of select="$v"/>
    </xsl:message>
    <xsl:variable name="dir" select="concat('out/', $v, '/')"/>
    <xsl:variable name="doc" select="document(concat($dir,'content.xml'))"/>
    <!-- convert the document into an HTML tree -->
    <xsl:variable name="tree">
      <xsl:apply-templates select="$doc/office:document-content/office:body/office:text"/>
    </xsl:variable>
    <!-- write each section of the HTML tree into a separate file -->
    <xsl:call-template name="writeSections">
      <xsl:with-param name="tree" select="$tree"/>
      <xsl:with-param name="dir" select="'out/'"/>
      <xsl:with-param name="version" select="$v"/>
      <xsl:with-param name="documentName" select="c:getDocumentName($doc)"/>
    </xsl:call-template>
    <!-- create an index and write it to index.xhtml -->
    <xsl:variable name="index">
      <xsl:call-template name="index">
        <xsl:with-param name="tree" select="$tree"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="html">
      <xsl:with-param name="href" select="concat($dir,'index.xhtml')"/>
      <xsl:with-param name="title"/>
      <xsl:with-param name="body" select="$index"/>
    </xsl:call-template>
  </xsl:template>

  <!-- convert each version of the specification -->
  <xsl:template match="/">
    <xsl:variable name="sectionlist">
      <xsl:call-template name="convert">
        <xsl:with-param name="v" select="'v1.1'"/>
      </xsl:call-template>
      <xsl:call-template name="convert">
        <xsl:with-param name="v" select="'v1.2'"/>
      </xsl:call-template>
      <xsl:call-template name="convert">
        <xsl:with-param name="v" select="'v1.2-part1'"/>
      </xsl:call-template>
      <xsl:call-template name="convert">
        <xsl:with-param name="v" select="'v1.2-part2'"/>
      </xsl:call-template>
      <xsl:call-template name="convert">
        <xsl:with-param name="v" select="'v1.2-part3'"/>
      </xsl:call-template>
    </xsl:variable>
    <!-- write the list of sections -->
    <xsl:result-document href="specreferences.xsd" method="xml" indent="yes">
      <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" targetNamespace="http://www.example.org/documenttests" xmlns:tns="http://www.example.org/documenttests" elementFormDefault="qualified">
        <xs:simpleType name="specRefType">
          <xs:restriction base="xs:token">
            <xsl:for-each select="$sectionlist/*">
              <xs:enumeration value="{@value}" />
            </xsl:for-each>
          </xs:restriction>
        </xs:simpleType>
        <xs:simpleType name='specRefsType'>
          <xs:restriction>
            <xs:simpleType>
              <xs:list itemType="tns:specRefType" />
            </xs:simpleType>
            <xs:minLength value='1' />
          </xs:restriction>
        </xs:simpleType>
      </xs:schema>
    </xsl:result-document>
  </xsl:template>
</xsl:stylesheet>
