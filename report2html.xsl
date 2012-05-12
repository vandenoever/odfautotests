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
			<pre>
				<xsl:for-each select="r:fragment">
					<xsl:call-template name="formatxml" />
				</xsl:for-each>
			</pre>
			<a>
				<xsl:attribute name="href">
				<xsl:value-of select="@path" />
			</xsl:attribute>
				<xsl:value-of select='local-name(.)' />
			</a>
			size:
			<xsl:value-of select="@size" />
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
			<xsl:for-each select="$outputs">
				<td>
					<xsl:value-of
						select="concat('width: ', r:pdfinfo/r:page[position()=$pageNumber]/@width, ' ')" />
					<xsl:value-of
						select="concat('height: ', r:pdfinfo/r:page[position()=$pageNumber]/@height, ' ')" />
					<img class="thumb" src="{r:pdfinfo/r:page[position()=$pageNumber]/@png}" />
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
		<div class="testreport">
			<h1>
				<xsl:value-of select="@name" />
			</h1>
			<table>
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
					<xsl:for-each select="r:target[r:output[@type='pdf']]">
						<th>
							<xsl:value-of select="@name" />
							<xsl:apply-templates select="r:output" />
						</th>
					</xsl:for-each>
				</tr>
				<tr>
					<th>success</th>
					<xsl:for-each select="r:target[r:output[@type='pdf']]">
						<td>
							<xsl:value-of select="count(r:output/r:validation/r:error)=0" />
							<xsl:apply-templates select="r:output/r:validation" />
						</td>
					</xsl:for-each>
				</tr>
				<tr>
					<th>pages</th>
					<xsl:for-each select="r:target[r:output[@type='pdf']]">
						<td>
							<xsl:value-of select="r:output/r:pdfinfo/@pages" />
						</td>
					</xsl:for-each>
				</tr>
				<tr>
					<th>width</th>
					<xsl:for-each select="r:target[r:output[@type='pdf']]">
						<td>
							<xsl:value-of select="r:output/r:pdfinfo/r:page/@width" />
						</td>
					</xsl:for-each>
				</tr>
				<tr>
					<th>height</th>
					<xsl:for-each select="r:target[r:output[@type='pdf']]">
						<td>
							<xsl:value-of select="r:output/r:pdfinfo/r:page/@height" />
						</td>
					</xsl:for-each>
				</tr>
				<xsl:call-template name="pdfpages" />
			</table>
		</div>
	</xsl:template>
	<xsl:template match="/r:documenttestsreport">
		<html>
			<head>
				<title>ODF Automatic tests report</title>
				<style type="text/css">
					.popup {
					display: none;
					}
					th:hover .popup {
					display:
					block;
					position: absolute;
					background: white;
					border: 1px solid black;
					}
					th { font-weight: normal;
					background-color: #cccccc;
					text-align:
					left; }
					img.thumb {max-width: 10em; border: 1px solid black;}
				</style>
			</head>
			<body>
				<xsl:apply-templates select="r:testreport" />
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
