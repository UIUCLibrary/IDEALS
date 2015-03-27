<?xml version="1.0" encoding="UTF-8"?>

<!--
    ideals-stats.xsl
    
    Version: $Revision: 1.7 $
    
    Date: $Date: 2006/07/27 22:54:52 $
    
    Copyright (c) 2008, University of Illinois at Urbana-Champaign.  
	All rights reserved.
-->
    
<!-- 
    This is the Statistics XSL for the IDEALS Theme.  Mostly, it overrides
    the default DRI2XHTML theme's structural.xsl
    
    It handles customizations for the following:
      * Statistics information on Homepage
	  * Statistics information on Community/Collection/Item Views   
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

    <!-- ##################################################-->
    <!-- ##########  STATISTICS customizations ############-->
    <!-- ##################################################-->
    <!-- INDEX PAGE customizations -->
    <!-- Set up containers and get list info for top ten downloads -->
    <xsl:template match="dri:div[@id='aspect.rochesterStatistics.StatisticsViewer.div.ideals-topTen-download']">
    	<div id="ideals-downloads">
    	<div class="top_downloads_box">
			<div class="top_downloads_head">
			    <p><i18n:text>xmlui.ideals.topDownloadList_Statistics</i18n:text></p>
    		</div>
    		<div class="top_downloads_body">
    			<xsl:apply-templates select="dri:list" />
    		</div>
		</div>
		</div>
    </xsl:template>

    <!-- INDEX, COMMUNITY, COLLECTION, and ITEM page customizations -->
    <!-- Add statistic info to a page, using common 'ideals-stats-template' below. -->
	<xsl:template match="dri:div[@id='aspect.rochesterStatistics.StatisticsViewer.div.ideals-statistics']">
		<xsl:call-template name="ideals-stats-template">
        	<xsl:with-param name="stats-div" select="."/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="dri:div[@id='aspect.rochesterStatistics.StatisticsViewer.div.ideals-community-statistics']">
        <xsl:call-template name="ideals-stats-template">
            <xsl:with-param name="stats-div" select="."/>
        </xsl:call-template>
	</xsl:template>
	<xsl:template match="dri:div[@id='aspect.rochesterStatistics.StatisticsViewer.div.ideals-Collection-statistics']">
        <xsl:call-template name="ideals-stats-template">
            <xsl:with-param name="stats-div" select="."/>
        </xsl:call-template>
	</xsl:template>
	<xsl:template match="dri:div[@id='aspect.rochesterStatistics.StatisticsViewer.div.ideals-item-statistics']">
        <xsl:call-template name="ideals-stats-template">
            <xsl:with-param name="stats-div" select="."/>
        </xsl:call-template>
	</xsl:template>
	
	<!-- INDEX, COMMUNITY, COLLECTION, and ITEM page customizations -->
	<!-- Generic template to handle download statistics in IDEALS -->
    <xsl:template name="ideals-stats-template">
    	<xsl:param name="stats-div"/>
    	<div id="ideals-stats">
    	<div class="stats_box">
    		<div class="stats_box_body"> 
     			<ul>
     			  <xsl:for-each select="$stats-div/dri:p">
                            <li>
                                <xsl:apply-templates select="i18n:text" />
                                <xsl:value-of select="text()" />
                            </li>
                  </xsl:for-each>
     			</ul>  
    		</div> 
		</div>
		</div> 
    </xsl:template>
    
</xsl:stylesheet>
