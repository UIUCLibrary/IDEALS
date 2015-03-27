<?xml version="1.0" encoding="UTF-8"?>

<!--
    index-suppress.xsl
    
    Version: $Revision: 1.7 $
    
    Date: $Date: 2006/07/27 22:54:52 $
    
    Copyright (c) 2008, University of Illinois at Urbana-Champaign.  
	All rights reserved.
-->
    
<!-- 
    This XSL handles the suppression of information from the IDEALS
	Homepage, which is unnecessary for the IDEALS theme.  Specifically,
	it suppresses the following on IDEALS Homepage:
	  * Community listing
-->
  
<xsl:stylesheet xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
  xmlns:dri="http://di.tamu.edu/DRI/1.0/" xmlns:mets="http://www.loc.gov/METS/"
  xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
  xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:mods="http://www.loc.gov/mods/v3" 
  xmlns:xlink="http://www.w3.org/TR/xlink/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0">

  <xsl:output indent="yes"/>


    <!--By default: anything that doesn't match one of the below templates -->
	<!-- is just copied to the output, so a later XSL can deal with it. -->
    <xsl:template match="*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

	<!-- INDEX PAGE customizations -->
    <!-- Suppress the 'div' that contains list of communities in DSpace. -->
	<!-- We don't want this displayed on the main index page. -->
	<xsl:template match="dri:div[@id='aspect.artifactbrowser.CommunityBrowser.div.comunity-browser']" />
	
	<!-- Suppress the 'div' that contains the search box for the index page. -->
 	<!-- IDEALS doesn't need this, as we have a search box in the header!-->
 	<xsl:template match="dri:div[@id='aspect.artifactbrowser.FrontPageSearch.div.front-page-search']" />
	
</xsl:stylesheet>
