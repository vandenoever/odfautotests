<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml"
	exclude-result-prefixes="r" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:r="http://www.example.org/documenttests">
	<xsl:output encoding="utf-8" indent="yes" method="xml"
		omit-xml-declaration="no"></xsl:output>
	<xsl:strip-space elements="*"></xsl:strip-space>
	<xsl:template match="r:error">
		<dt>
			<xsl:value-of select="@type" />
		</dt>
		<dd>
			<xsl:value-of select="@message" />
		</dd>
	</xsl:template>
	<xsl:template match="r:validation">
		<xsl:if test="count(r:error)&gt;0">
			<div class="popup">
				<h3>Validation errors:</h3>
				<dl>
					<xsl:apply-templates select="r:error" />
				</dl>
			</div>
		</xsl:if>
	</xsl:template>
	<xsl:template match="r:xpath">
		<tr>
			<td>
				<xsl:value-of select="@result" />
			</td>
			<td>
				<xsl:value-of select="@expr" />
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="r:file">
		<div>
			<h3>
				<xsl:value-of select="@path" />
			</h3>
			<table>
				<xsl:apply-templates select="r:xpath" />
			</table>
		</div>
	</xsl:template>
	<xsl:template name="openelement">
		<xsl:param name="indent" />
		<xsl:variable name="s"
			select="'                                         '" />
		<xsl:value-of
			select="concat(substring($s, 1, $indent), '&lt;', local-name(.))" />
		<xsl:for-each select="@*">
			<br />
			<xsl:value-of
				select="concat(substring($s, 1, $indent + 2), local-name(.), '=&quot;', ., '&quot;')" />
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="formatxml">
		<xsl:param name="indent">
			0
		</xsl:param>
		<xsl:variable name="s"
			select="'                                         '" />
		<xsl:variable name="i" select="number($indent)" />
		<xsl:for-each select="*|text()">
			<xsl:choose>
				<xsl:when test="count(*)">
					<xsl:call-template name="openelement">
						<xsl:with-param name="indent" select="$i" />
					</xsl:call-template>
					<xsl:value-of select="'>'" />
					<br />
					<xsl:call-template name="formatxml">
						<xsl:with-param name="indent" select="$i + 1" />
					</xsl:call-template>
					<xsl:value-of
						select="concat(substring($s, 1, $i), '&lt;/', local-name(.), '>')" />
					<br />
				</xsl:when>
				<xsl:when test="text()">
					<xsl:call-template name="openelement">
						<xsl:with-param name="indent" select="$i" />
					</xsl:call-template>
					<xsl:value-of select="'>'" />
					<xsl:value-of select="concat(text(), '&lt;', local-name(.), '/>')" />
					<br />
				</xsl:when>
				<xsl:when test="local-name(.)">
					<xsl:call-template name="openelement">
						<xsl:with-param name="indent" select="$i" />
					</xsl:call-template>
					<xsl:value-of select="'/>'" />
					<br />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="concat(substring($s, 1, $i), .)" />
					<br />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="r:input|r:output">
		<div class="popup">
			<xsl:if test="r:fragment">
				<pre>
					<xsl:for-each select="r:fragment">
						<xsl:call-template name="formatxml" />
					</xsl:for-each>
				</pre>
			</xsl:if>
			<a>
				<xsl:attribute name="href">
				<xsl:value-of select="@path" />
			</xsl:attribute>
				<xsl:value-of select='local-name(.)' />
			</a>
			size:
			<xsl:value-of select="@size" />
			<xsl:if test="../r:commands">
				<span>
					<xsl:value-of
						select="concat(', duration: ', sum(../r:commands/@durationMs), ' ms ')" />
				</span>
				<xsl:if test="../r:commands/@stderr">
					<span>
						stderr
						<pre class="output">
							<xsl:value-of select="../r:commands/@stderr" />
						</pre>
					</span>
				</xsl:if>
				<xsl:if test="../r:commands/@stdout">
					<span>
						stdout
						<pre class="output">
							<xsl:value-of select="../r:commands/@stdout" />
						</pre>
					</span>
				</xsl:if>
			</xsl:if>
		</div>
	</xsl:template>
	<xsl:template name="xpathresults">
		<xsl:param name="xpath" />
		<xsl:for-each
			select="../../../../r:target/r:output/r:file/r:xpath[@expr=$xpath]">
			<td>
				<xsl:value-of select="@result" />
			</td>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="pdfpage">
		<xsl:param name="pageNumber" />
		<xsl:param name="outputs" />
		<tr>
			<th>
				<xsl:value-of select="concat('page ', $pageNumber)" />
			</th>
			<td></td>
			<xsl:for-each select="$outputs">
				<td>
					<xsl:if test="r:pdfinfo/r:page[position()=$pageNumber]">
						<xsl:value-of
							select="concat('width: ', r:pdfinfo/r:page[position()=$pageNumber]/@width, ' ')" />
						<br />
						<xsl:value-of
							select="concat('height: ', r:pdfinfo/r:page[position()=$pageNumber]/@height, ' ')" />
						<br />
						<a href="{r:pdfinfo/r:page[position()=$pageNumber]/@png}">
							<img class="thumb"
								src="{r:pdfinfo/r:page[position()=$pageNumber]/@pngthumb}" />
						</a>
					</xsl:if>
				</td>
			</xsl:for-each>
		</tr>
	</xsl:template>
	<xsl:template name="pdfpages">
		<xsl:for-each select="r:target/r:output[@type='pdf']">
			<xsl:sort select="r:pdfinfo/@pages" data-type="number"
				order="descending" />
			<xsl:if test="position() = 1">
				<xsl:call-template name="pdfpage">
					<xsl:with-param name="pageNumber" select="position()" />
					<xsl:with-param name="outputs"
						select="../../r:target/r:output[@type='pdf']" />
				</xsl:call-template>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="r:testreport">
		<tr>
			<th class="testreport">
				<h1>
					<xsl:value-of select="@name" />
				</h1>
			</th>
		</tr>
		<tr>
			<td></td>
			<th>
				input
				<xsl:apply-templates select="r:input" />
			</th>
			<xsl:for-each select="r:target[r:output[@type='zip']]">
				<th>
					<xsl:value-of select="@name" />
					<xsl:apply-templates select="r:output" />
				</th>
			</xsl:for-each>
		</tr>
		<tr>
			<th>valid ODF</th>
			<td>
				<xsl:value-of select="count(r:input/r:validation/r:error)=0" />
				<xsl:apply-templates select="r:input/r:validation" />
			</td>
			<xsl:for-each select="r:target[r:output[@type='zip']]">
				<td>
					<xsl:value-of select="count(r:output/r:validation/r:error)=0" />
					<xsl:apply-templates select="r:output/r:validation" />
				</td>
			</xsl:for-each>
		</tr>
		<xsl:for-each
			select="r:target[position()=1]/r:output[@type='zip']/r:file/r:xpath">
			<tr>
				<th>
					<xsl:value-of select="@expr" />
				</th>
				<td />
				<xsl:call-template name="xpathresults">
					<xsl:with-param name="xpath" select="@expr" />
				</xsl:call-template>
			</tr>
		</xsl:for-each>
		<tr>
			<td></td>
			<th>
				input
				<xsl:apply-templates select="r:input" />
			</th>
			<xsl:for-each select="r:target[r:output[@type='pdf']]">
				<th>
					<xsl:value-of select="@name" />
					<xsl:apply-templates select="r:output" />
				</th>
			</xsl:for-each>
		</tr>
		<tr>
			<th>success</th>
			<td></td>
			<xsl:for-each select="r:target[r:output[@type='pdf']]">
				<td>
					<xsl:value-of select="count(r:output/r:validation/r:error)=0" />
					<xsl:apply-templates select="r:output/r:validation" />
				</td>
			</xsl:for-each>
		</tr>
		<xsl:call-template name="pdfpages" />
	</xsl:template>
	<xsl:template match="/r:documenttestsreport">
		<html>
			<head>
				<title>ODF Automatic tests report</title>
				<style type="text/css">
					.popup, .output {
					display: none;
					}
					th:hover .popup,
					td:hover
					.popup, span:hover .output {
					display:
					block;
					position:
					absolute;
					background: white;
					border:
					1px solid black;
					}
					th {
					vertical-align: top;
					font-weight: normal;
					background-color:
					#cccccc;
					text-align:
					left; }
					img.thumb { border: 1px
					solid black; }
					th:nth-child(1) { text-align: right; }
				</style>
			</head>
			<body>
				<table>
					<xsl:apply-templates select="r:testreport" />
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
