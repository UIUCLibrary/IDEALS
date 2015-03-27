<?xml version="1.0" encoding="UTF-8"?>
<!--
	ip-ignore.xsl
	
	Version: $Revision$
	
	Date: $Date$
	
	Copyright (c) 2009, University of Illinois at Urbana-Champaign.  
	All rights reserved.
-->

<!-- 
	This XSL customizes the IDEALS Ignored IP List editor with
    JavaScript to enhance usability.
	
	It restructures the XHTML normally output DSpace, adding
	some additional JavaScript calls to it.
	
	It should be loaded via the following call in your
	sitemap.xmap:
	
    <map:match pattern="admin/ip-ignore">
      <map:transform src="xslt/xhtml2xhtml/ip-ignore.xsl"/>
    </map:match>
-->
<xsl:stylesheet xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
	xmlns:dri="http://di.tamu.edu/DRI/1.0/"
	xmlns:mets="http://www.loc.gov/METS/"
	xmlns:xlink="http://www.w3.org/TR/xlink/"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
	xmlns:xhtml="http://www.w3.org/1999/xhtml"
	xmlns:mods="http://www.loc.gov/mods/v3"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns="http://www.w3.org/1999/xhtml"
	exclude-result-prefixes="mets xlink xsl dim xhtml mods dc">
    
    <xsl:output indent="yes"/>
    
	<!-- By default, just copy all XHTML tags (and their attributes) to output-->	
    <xsl:template match="*">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

	<!-- IMPORTANT: disable output escaping for &lt; stuff in the header -->
	<xsl:template match="text()">
		<xsl:choose>
			<xsl:when test="count(ancestor::body) > 0 or count(ancestor::script) > 0">
				<xsl:value-of select="."  />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="." disable-output-escaping="yes" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- ##################################################-->
	<!-- ################# Ignored IP List ################-->
	<!-- ##################################################-->
	<xsl:template match="xhtml:body[.//xhtml:input[@id='aspect_administrative_statistics_IpIgnoreList_field_range_start']]">
		<xsl:copy>
			<xsl:attribute name="onload">
				<xsl:text>mirrorIpRange()</xsl:text>
			</xsl:attribute>
			
			<!--Copy any existing attributes and continue-->
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="xhtml:input[@id='aspect_administrative_statistics_IpIgnoreList_field_range_start']">
		<xsl:copy>
			<xsl:attribute name="onkeyup">
				<xsl:text>mirrorIpRange()</xsl:text>
			</xsl:attribute>
			
			<!--Copy any existing attributes and continue-->
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
