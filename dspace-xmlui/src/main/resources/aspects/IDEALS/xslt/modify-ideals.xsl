<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:dri="http://di.tamu.edu/DRI/1.0/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">

    <!--
         This stylesheet modifies and restructures the DRI for
         the following pages:
           * Homepage (recent submissions RSS feeds & top downloads listing)
           * Community View (recent submissions RSS feeds)
           * Collection View (recent submissions RSS feeds)
     -->

    <xsl:template match="*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>


    <xsl:template match="dri:document">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <!--Make sure template for options is loaded *first*-->
            <xsl:apply-templates select="dri:options"/>
            <!--Then apply all other templates in normal order-->
            <xsl:apply-templates select="*[not(self::dri:options)]"/>
        </xsl:copy>
    </xsl:template>


    <!--#### SIDEBAR NAVIGATION MODIFICATIONS ####-->
    <!-- Reorder the "Browse By" navigation options in sidebar.
         This reorders both the *global* navigation, as well as
         Community/Collection specific browsing.
         NEW ORDER: Title, Author, Subject, Date, Community/Collections (global only) -->
    <xsl:template name="modifySidebar">
        <!--Display Heading-->
        <xsl:apply-templates select="dri:head"/>
        <!--Display Browse by Title-->
        <xsl:apply-templates select="dri:item[dri:xref/i18n:text/text()='xmlui.ArtifactBrowser.Navigation.browse_title']"/>
        <!--Display Browse by Author-->
        <xsl:apply-templates select="dri:item[dri:xref/i18n:text/text()='xmlui.ArtifactBrowser.Navigation.browse_author']"/>
        <!--Display Browse by Contributor-->
        <xsl:apply-templates select="dri:item[dri:xref/i18n:text/text()='xmlui.ArtifactBrowser.Navigation.browse_contributor']"/>
        <!--Display Browse by Subject-->
        <xsl:apply-templates select="dri:item[dri:xref/i18n:text/text()='xmlui.ArtifactBrowser.Navigation.browse_subject']"/>
        <!--Display Browse by Date-->
        <xsl:apply-templates select="dri:item[dri:xref/i18n:text/text()='xmlui.ArtifactBrowser.Navigation.browse_dateissued']"/>
        <!--Display Browse Community/Collection-->
        <xsl:apply-templates select="dri:item[dri:xref/i18n:text/text()='xmlui.ArtifactBrowser.Navigation.communities_and_collections']"/>
    </xsl:template>

    <xsl:template
            match="dri:list[@id='aspect.viewArtifacts.Navigation.list.browse']/dri:list[@id='aspect.browseArtifacts.Navigation.list.global']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:call-template name="modifySidebar"/>
        </xsl:copy>
    </xsl:template>

    <!-- For collections which include periodical information,
         add an "Issue" or 'Series' navigation option. -->
    <xsl:template
            match="dri:list[@id='aspect.viewArtifacts.Navigation.list.browse']/dri:list[@id='aspect.browseArtifacts.Navigation.list.context']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:call-template name="modifySidebar"/>
            <xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='collectionIsPeriodical']">
                <!--Display Browse by Issue/Periodical -->
                <xsl:apply-templates select="dri:item[dri:xref/i18n:text/text()='xmlui.ArtifactBrowser.Navigation.browse_periodical']"/>
            </xsl:if>
            <xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='collectionHasSeriesMetadata']">
                <!--Display Browse by Series/Report -->
                <xsl:apply-templates select="dri:item[dri:xref/i18n:text/text()='xmlui.ArtifactBrowser.Navigation.browse_series']"/>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <!-- Reorder the global "My Account" navigation options in sidebar-->
    <xsl:template match="dri:list[@id='aspect.viewArtifacts.Navigation.list.account']">
        <xsl:choose>
            <!--Only reorder options if a user is logged into the system-->
            <xsl:when test="/dri:document/dri:meta/dri:userMeta[@authenticated='yes']">
                <!--Re-Order: My IDEALS, Submit new item, My Profile, Logout -->
                <xsl:copy>
                    <xsl:copy-of select="@*"/>
                    <!--Display Heading-->
                    <xsl:apply-templates select="dri:head"/>
                    <!--Display My IDEALS (Submissions)-->
                    <xsl:apply-templates select="dri:item[dri:xref/i18n:text/text()='xmlui.Submission.Navigation.submissions']"/>
                    <!--Display Submit new Item -->
                    <xsl:apply-templates select="dri:item[dri:xref/i18n:text/text()='xmlui.Submission.Navigation.startNewSubmission']"/>
                    <!--Display My Profile-->
                    <xsl:apply-templates select="dri:item[dri:xref/i18n:translate/i18n:text/text()='xmlui.EPerson.Navigation.profile']"/>
                    <!--Display Logout-->
                    <xsl:apply-templates select="dri:item[dri:xref/i18n:text/text()='xmlui.EPerson.Navigation.logout']"/>
                </xsl:copy>
            </xsl:when>
            <!--Otherwise, just display options in normal order-->
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:copy-of select="@*"/>
                    <xsl:apply-templates/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <!--#### RECENT SUBMISSIONS MODIFICATIONS ####-->
    <!-- Add RSS feed button(s) to appear next to Recent Submissions listings on Community Homepage-->
    <xsl:template match="dri:div[@id='aspect.artifactbrowser.CommunityRecentSubmissions.div.community-recent-submission']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <!--Add RSS Image to the Heading-->
            <xsl:apply-templates select="dri:head" mode="rss-image"/>
            <xsl:apply-templates select="*[not(name()='head')]"/>
        </xsl:copy>
    </xsl:template>

    <!-- Add RSS feed button(s) to appear next to Recent Submissions listings on Collection Homepage-->
    <xsl:template match="dri:div[@id='aspect.artifactbrowser.CollectionRecentSubmissions.div.collection-recent-submission']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <!--Add RSS Image to the Heading-->
            <xsl:apply-templates select="dri:head" mode="rss-image"/>
            <xsl:apply-templates select="*[not(name()='head')]"/>
        </xsl:copy>
    </xsl:template>

    <!-- Add RSS feed button(s) to appear next to Recent Submissions listings on Homepage-->
    <xsl:template match="dri:div[@id='aspect.rochesterStatistics.StatisticsViewer.div.site-recent-submission']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <!--Add RSS Image to the Heading-->
            <xsl:apply-templates select="dri:head" mode="rss-image"/>
            <xsl:apply-templates select="*[not(name()='head')]"/>
        </xsl:copy>
    </xsl:template>

    <!-- Insert an 'ideals-rss' <div> directly within the Recent Submissions <head>-->
    <xsl:template match="dri:head" mode="rss-image">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
            <dri:div id="ideals-rss"/>
        </xsl:copy>
    </xsl:template>
    <!--#### END RECENT SUBMISSIONS MODIFICATIONS ####-->


    <!--#### HOME PAGE / TOP 10 DOWNLOADS MODIFICATIONS ####-->
    <xsl:template match="dri:body[//dri:div/@id='file.news.div.news']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="dri:div[@id='file.news.div.news']"/>
            <xsl:apply-templates select="dri:div[@id='aspect.rochesterStatistics.StatisticsViewer.div.download-statistics']"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="dri:div[@id='file.news.div.news']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
            <xsl:apply-templates select="../dri:div[@id='aspect.rochesterStatistics.StatisticsViewer.div.top-download-list']"/>
            <xsl:apply-templates select="../dri:div[@id='aspect.rochesterStatistics.StatisticsViewer.div.site-recent-submission']"/>
        </xsl:copy>
    </xsl:template>
    <!--#### END HOME PAGE / TOP 10 DOWNLOADS MODIFICATIONS ####-->

    <!--#### TOP 10 DOWNLOADS MODIFICATIONS ####-->
     <!--Make sure Top 10 downloads 'div' is listed *first* in <dri:body>! -->
     <!--This ensures we can float the Top 10 Downloads to the right of everything-->
    <!--<xsl:template match="dri:body">-->
        <!--<xsl:copy>-->
            <!--<xsl:copy-of select="@*"/>-->
            <!--&lt;!&ndash;Make sure template for Top Downloads is loaded *first*&ndash;&gt;-->
            <!--<xsl:apply-templates select="dri:div[@id='aspect.rochesterStatistics.StatisticsViewer.div.top-download-list']"/>-->
            <!--&lt;!&ndash;Then apply all other templates in normal order&ndash;&gt;-->
            <!--<xsl:apply-templates select="dri:div[@id!='aspect.rochesterStatistics.StatisticsViewer.div.top-download-list']"/>-->
        <!--</xsl:copy>-->
    <!--</xsl:template>-->
    <!--#### END TOP 10 DOWNLOADS MODIFICATIONS ####-->


    <!--&lt;!&ndash;#### COMM/COLL RIGHT SIDEBAR MODIFICATIONS ####&ndash;&gt;-->
    <!--<xsl:template match="dri:div[@id='aspect.artifactbrowser.CommunityViewer.div.community-home']">-->
        <!--<xsl:copy>-->
            <!--<xsl:copy-of select="@*"/>-->
            <!--<xsl:apply-templates select="dri:head"/>-->
            <!--&lt;!&ndash;Make sure template for comm/coll view is loaded *first*&ndash;&gt;-->
            <!--<xsl:apply-templates select="dri:div[@id='aspect.artifactbrowser.CommunityViewer.div.community-view']"/>-->
            <!--&lt;!&ndash;Then apply all other templates in normal order&ndash;&gt;-->
            <!--<xsl:apply-templates select="dri:div[@id!='aspect.artifactbrowser.CommunityViewer.div.community-view']"/>-->
        <!--</xsl:copy>-->
    <!--</xsl:template>-->

    <!--<xsl:template match="dri:div[@id='aspect.artifactbrowser.CollectionViewer.div.collection-home']">-->
        <!--<xsl:copy>-->
            <!--<xsl:copy-of select="@*"/>-->
            <!--<xsl:apply-templates select="dri:head"/>-->
            <!--&lt;!&ndash;Make sure template for comm/coll view is loaded *first*&ndash;&gt;-->
            <!--<xsl:apply-templates select="dri:div[@id='aspect.artifactbrowser.CollectionViewer.div.collection-view']"/>-->
            <!--&lt;!&ndash;Then apply all other templates in normal order&ndash;&gt;-->
            <!--<xsl:apply-templates select="dri:div[@id!='aspect.artifactbrowser.CollectionViewer.div.collection-view']"/>-->
        <!--</xsl:copy>-->
    <!--</xsl:template>-->

    <!--<xsl:template match="dri:body[dri:div/@id='aspect.artifactbrowser.CommunityViewer.div.community-home']">-->
        <!--<xsl:copy>-->
            <!--<xsl:copy-of select="@*"/>-->
            <!--<xsl:apply-templates select="dri:head"/>-->
            <!--<xsl:apply-templates select="dri:div[@id='aspect.artifactbrowser.CommunityViewer.div.community-home']"/>-->
            <!--<xsl:apply-templates select="dri:div/dri:div[@id='aspect.artifactbrowser.CommunityViewer.div.community-search-browse']"/>-->
            <!--<xsl:apply-templates select="dri:div/dri:div[@id='aspect.artifactbrowser.CommunityRecentSubmissions.div.community-recent-submission']"/>-->
            <!--<xsl:apply-templates select="dri:div[@id='aspect.rochesterStatistics.StatisticsViewer.div.download-statistics']"/>-->
        <!--</xsl:copy>-->
    <!--</xsl:template>-->

    <!--<xsl:template match="dri:body[dri:div/@id='aspect.artifactbrowser.CollectionViewer.div.collection-home']">-->
        <!--<xsl:copy>-->
            <!--<xsl:copy-of select="@*"/>-->
            <!--<xsl:apply-templates select="dri:head"/>-->
            <!--<xsl:apply-templates select="dri:div[@id='aspect.artifactbrowser.CollectionViewer.div.collection-home']"/>-->
            <!--<xsl:apply-templates select="dri:div/dri:div[@id='aspect.artifactbrowser.CollectionViewer.div.collection-search-browse']"/>-->
            <!--<xsl:apply-templates select="dri:div/dri:div[@id='aspect.artifactbrowser.CollectionRecentSubmissions.div.collection-recent-submission']"/>-->
            <!--<xsl:apply-templates select="dri:div[@id='aspect.rochesterStatistics.StatisticsViewer.div.download-statistics']"/>-->
        <!--</xsl:copy>-->
    <!--</xsl:template>-->

    <!--<xsl:template match="dri:div[@id='aspect.artifactbrowser.CommunityViewer.div.community-home']">-->
        <!--<xsl:copy>-->
            <!--<xsl:copy-of select="@*"/>-->
            <!--<xsl:apply-templates select="dri:head"/>-->
            <!--<xsl:apply-templates select="dri:div[@id='aspect.artifactbrowser.CommunityViewer.div.community-view']"/>-->
        <!--</xsl:copy>-->
    <!--</xsl:template>-->

    <!--<xsl:template match="dri:div[@id='aspect.artifactbrowser.CollectionViewer.div.collection-home']">-->
        <!--<xsl:copy>-->
            <!--<xsl:copy-of select="@*"/>-->
            <!--<xsl:apply-templates select="dri:head"/>-->
            <!--<xsl:apply-templates select="dri:div[@id='aspect.artifactbrowser.CollectionViewer.div.collection-view']"/>-->
        <!--</xsl:copy>-->
    <!--</xsl:template>-->
    <!--#### END COMM/COLL RIGHT SIDEBAR MODIFICATIONS ####-->


</xsl:stylesheet>