<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->
<!--
    Rendering specific to the navigation (options)

    Author: art.lowel at atmire.com
    Author: lieven.droogmans at atmire.com
    Author: ben at atmire.com
    Author: Alexey Maslov

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
	exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc">

    <xsl:output indent="yes"/>


    <xsl:template match="dri:options">
        <div id="ds-options-wrapper">
            <div id="ds-options">

                <xsl:apply-templates/>

                <!-- Custom code for IDEALs to supply INFORMATION block in left navigation -->
                <!--<h1 class="ds-option-set-head">Information</h1>-->
                <!--<div id="aspect_artifactbrowser_Navigation_list_ideals_information" class="ds-option-set">-->
                    <!--<div class="list-group ds-simple-list">-->
                        <!--&lt;!&ndash; Add Javascript to open up Help info in a separate window.-->
                             <!--This does the same thing as target="_blank", but is valid for XHTML 1.0 &ndash;&gt;-->
                        <!--<a class="list-group-item" onclick="window.open(this.href); return false;" onkeypress="window.open(this.href); return false;">-->
                            <!--<xsl:attribute name="href">-->
                                <!--<xsl:text>https://wiki.cites.illinois.edu/wiki/display/IDEALS/Getting+Started+with+IDEALS</xsl:text>-->
                            <!--</xsl:attribute>-->
                            <!--<i18n:text>xmlui.dri2xhtml.structural.help</i18n:text>-->
                        <!--</a>-->
                        <!--<a class="list-group-item">-->
                            <!--<xsl:attribute name="href">-->
                                <!--<xsl:text>https://wiki.cites.illinois.edu/wiki/display/IDEALS/IDEALS+Resources+and+Information</xsl:text>-->
                            <!--</xsl:attribute>-->
                            <!--<i18n:text>xmlui.dri2xhtml.structural.about</i18n:text>-->
                        <!--</a>-->
                        <!--<a class="list-group-item">-->
                            <!--<xsl:attribute name="href">-->
                                <!--<xsl:value-of select="/*/dri:meta/dri:pageMeta/dri:metadata[@qualifier='contactURL']" />-->
                            <!--</xsl:attribute>-->
                            <!--<i18n:text>xmlui.dri2xhtml.structural.contact-link</i18n:text>-->
                        <!--</a>-->
                    <!--</div>-->
                <!--</div>-->
                <!-- End custom navigation INFORMATION block -->

            </div>
        </div>
    </xsl:template>

    <!-- Add each RSS feed from meta to a list -->
    <xsl:template name="addRSSLinks">
        <xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='feed']">
            <li>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="."/>
                    </xsl:attribute>

                    <xsl:attribute name="style">
                        <xsl:text>background: url(</xsl:text>
                        <xsl:value-of select="$context-path"/>
                        <xsl:text>/static/icons/feed.png) no-repeat</xsl:text>
                    </xsl:attribute>

                    <xsl:choose>
                        <xsl:when test="contains(., 'rss_1.0')">
                            <xsl:text>RSS 1.0</xsl:text>
                        </xsl:when>
                        <xsl:when test="contains(., 'rss_2.0')">
                            <xsl:text>RSS 2.0</xsl:text>
                        </xsl:when>
                        <xsl:when test="contains(., 'atom_1.0')">
                            <xsl:text>Atom</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="@qualifier"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
            </li>
        </xsl:for-each>
    </xsl:template>


    <!-- The template that applies to lists directly under the options tag that have other lists underneath
        them. Each list underneath the matched one becomes an option-set and is handled by the appropriate
        list templates. -->
    <xsl:template match="dri:options/dri:list[dri:list]" priority="3">
        <xsl:apply-templates select="dri:head"/>
        <div>
            <xsl:call-template name="standardAttributes">
                <xsl:with-param name="class">ds-option-set</xsl:with-param>
            </xsl:call-template>
            <div class="ds-options-list">
                <xsl:apply-templates select="*[not(name()='head')]" mode="nested"/>
            </div>
        </div>
    </xsl:template>

    <xsl:template match="dri:options/dri:list[dri:item]" priority="3">
        <xsl:apply-templates select="dri:head"/>
        <div>
            <xsl:call-template name="standardAttributes">
                <xsl:with-param name="class">ds-option-set</xsl:with-param>
            </xsl:call-template>
            <div class="ds-options-list">
                <xsl:apply-templates select="*[not(name()='head')]" mode="nested"/>
            </div>
        </div>
    </xsl:template>

    <!-- Special case for nested options lists -->
    <xsl:template match="dri:options/dri:list/dri:list" priority="3" mode="nested">
        <div class="panel panel-default">
            <div class="panel-heading">
                <xsl:apply-templates select="dri:head" mode="nested"/>
            </div>
            <div class="ds-simple-list list-group">
                <xsl:apply-templates select="dri:item" mode="nested"/>
            </div>
        </div>
    </xsl:template>

    <!--IDEALS-->
    <!--Surpress global browse on item view-->
    <xsl:template match="dri:options[../dri:body[@n='item-view']]/dri:list/dri:list[@id='aspect.browseArtifacts.Navigation.list.global']" priority="3" mode="nested">

    </xsl:template>
    <!--Surpress global browse on collection view-->
    <xsl:template match="dri:options[../dri:body[@n='collection-home']]/dri:list/dri:list[@id='aspect.browseArtifacts.Navigation.list.global']" priority="3" mode="nested">

    </xsl:template>
    <!--Surpress global browse on community view-->
    <xsl:template match="dri:options[../dri:body[@n='community-home']]/dri:list/dri:list[@id='aspect.browseArtifacts.Navigation.list.global']" priority="3" mode="nested">

    </xsl:template>

    <xsl:template match="dri:options//dri:item" mode="nested" priority="3">
        <xsl:apply-templates />
    </xsl:template>


    <xsl:template match="dri:options//dri:item/dri:xref">
        <a>
            <xsl:if test="@target">
                <xsl:attribute name="href"><xsl:value-of select="@target"/></xsl:attribute>
            </xsl:if>

            <xsl:attribute name="class">
                <xsl:text>list-group-item </xsl:text>
                <xsl:if test="@rend"><xsl:value-of select="@rend"/></xsl:if>
            </xsl:attribute>

            <xsl:if test="@n">
                <xsl:attribute name="name"><xsl:value-of select="@n"/></xsl:attribute>
            </xsl:if>

            <xsl:apply-templates />
        </a>
    </xsl:template>

    <!-- Quick patch to remove empty lists from options -->
    <xsl:template match="dri:options//dri:list[count(child::*)=0]" priority="5" mode="nested">
    </xsl:template>
    <xsl:template match="dri:options//dri:list[count(child::*)=0]" priority="5">
    </xsl:template>

</xsl:stylesheet>
