<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
	xmlns:dri="http://di.tamu.edu/DRI/1.0/" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
	
	<xsl:template match="*">
      <xsl:copy>
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates/>
      </xsl:copy>
    </xsl:template>
    
    <!-- Copy all author, browse, date, and subject context browse items found in dri:options
         and substitute them in the template that displays browse by author, title, and date items in dri:body
         of both the Collection and Community homepages.
     -->
    <xsl:template match="dri:list[@id='aspect.artifactbrowser.CommunityViewer.list.community-browse' or @id='aspect.artifactbrowser.CollectionViewer.list.collection-browse']">
      <xsl:copy>
          <xsl:copy-of select="@*"/>
          <!--Display Normal Heading-->
          <xsl:apply-templates select="dri:head"/>

          <!--Copy the "Browse By" options that appear in the "context" menu in the sidebar.
              This will ensure they are in the same order as the sidebar,
              and add the "Subject" option (which is missing on the Community/Collection homepages by default)-->
          <xsl:copy-of select="//dri:list[@id='aspect.viewArtifacts.Navigation.list.browse']/dri:list[@id='aspect.browseArtifacts.Navigation.list.context']/dri:item" />
      </xsl:copy>
    </xsl:template>

    <xsl:template match="dri:div[@id='aspect.artifactbrowser.CommunityViewer.div.community-search-browse' or @id='aspect.artifactbrowser.CollectionViewer.div.collection-search-browse']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates />

            <!--Copy the "Submit to this collection" button here -->
            <xsl:apply-templates select="//dri:div[@id='aspect.artifactbrowser.CollectionViewer.div.collection-view']/dri:p[dri:xref]" />
        </xsl:copy>
    </xsl:template>

    <!--Suppress community and collection search-->
    <xsl:template match="dri:div[@id='aspect.artifactbrowser.CollectionSearch.div.collection-search' or @id='aspect.artifactbrowser.CommunitySearch.div.community-search']">
        <xsl:comment>Suppressed community and collection search</xsl:comment>
    </xsl:template>

</xsl:stylesheet>