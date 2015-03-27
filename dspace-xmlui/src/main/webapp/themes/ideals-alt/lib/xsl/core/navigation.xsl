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

    <!--
        The template to handle dri:options. Since it contains only dri:list tags (which carry the actual
        information), the only things than need to be done is creating the ds-options div and applying
        the templates inside it.

        In fact, the only bit of real work this template does is add the search box, which has to be
        handled specially in that it is not actually included in the options div, and is instead built
        from metadata available under pageMeta.
    -->
    <!-- TODO: figure out why i18n tags break the go button -->
    <!--<xsl:template match="dri:options">-->
        <!--<div id="ds-options-wrapper">-->
            <!--<div id="ds-options">-->
                <!--<xsl:if test="not(contains(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='URI'], 'discover'))">-->
                    <!--<h1 id="ds-search-option-head" class="ds-option-set-head">-->
                        <!--<i18n:text>xmlui.dri2xhtml.structural.search</i18n:text>-->
                    <!--</h1>-->
                    <!--<div id="ds-search-option" class="ds-option-set">-->
                        <!--&lt;!&ndash; The form, complete with a text box and a button, all built from attributes referenced-->
                     <!--from under pageMeta. &ndash;&gt;-->
                        <!--<form id="ds-search-form" method="post">-->
                            <!--<xsl:attribute name="action">-->
                                <!--<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath']"/>-->
                                <!--<xsl:value-of-->
                                        <!--select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='simpleURL']"/>-->
                            <!--</xsl:attribute>-->
                            <!--<fieldset>-->
                                <!--<input class="ds-text-field " type="text">-->
                                    <!--<xsl:attribute name="name">-->
                                        <!--<xsl:value-of-->
                                                <!--select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='queryField']"/>-->
                                    <!--</xsl:attribute>-->
                                <!--</input>-->
                                <!--<input class="ds-button-field " name="submit" type="submit" i18n:attr="value"-->
                                       <!--value="xmlui.general.go">-->
                                    <!--<xsl:attribute name="onclick">-->
                                    <!--<xsl:text>-->
                                        <!--var radio = document.getElementById(&quot;ds-search-form-scope-container&quot;);-->
                                        <!--if (radio != undefined &amp;&amp; radio.checked)-->
                                        <!--{-->
                                        <!--var form = document.getElementById(&quot;ds-search-form&quot;);-->
                                        <!--form.action=-->
                                    <!--</xsl:text>-->
                                        <!--<xsl:text>&quot;</xsl:text>-->
                                        <!--<xsl:value-of-->
                                                <!--select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath']"/>-->
                                        <!--<xsl:text>/handle/&quot; + radio.value + &quot;</xsl:text>-->
                                        <!--<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='simpleURL']"/>-->
                                        <!--<xsl:text>&quot; ; </xsl:text>-->
                                    <!--<xsl:text>-->
                                        <!--}-->
                                    <!--</xsl:text>-->
                                    <!--</xsl:attribute>-->
                                <!--</input>-->
                                <!--<xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='focus'][@qualifier='container']">-->
                                    <!--<label>-->
                                        <!--<input id="ds-search-form-scope-all" type="radio" name="scope" value=""-->
                                               <!--checked="checked"/>-->
                                        <!--<i18n:text>xmlui.dri2xhtml.structural.search</i18n:text>-->
                                    <!--</label>-->
                                    <!--<br/>-->
                                    <!--<label>-->
                                        <!--<input id="ds-search-form-scope-container" type="radio" name="scope">-->
                                            <!--<xsl:attribute name="value">-->
                                                <!--<xsl:value-of-->
                                                        <!--select="substring-after(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='focus'][@qualifier='container'],':')"/>-->
                                            <!--</xsl:attribute>-->
                                        <!--</input>-->
                                        <!--<xsl:choose>-->
                                            <!--<xsl:when-->
                                                    <!--test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='focus'][@qualifier='containerType']/text() = 'type:community'">-->
                                                <!--<i18n:text>xmlui.dri2xhtml.structural.search-in-community</i18n:text>-->
                                            <!--</xsl:when>-->
                                            <!--<xsl:otherwise>-->
                                                <!--<i18n:text>xmlui.dri2xhtml.structural.search-in-collection</i18n:text>-->
                                            <!--</xsl:otherwise>-->

                                        <!--</xsl:choose>-->
                                    <!--</label>-->
                                <!--</xsl:if>-->
                            <!--</fieldset>-->
                        <!--</form>-->
                        <!--&lt;!&ndash;Only add if the advanced search url is different from the simple search&ndash;&gt;-->
                        <!--<xsl:if test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='advancedURL'] != /dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='simpleURL']">-->
                            <!--&lt;!&ndash; The "Advanced search" link, to be perched underneath the search box &ndash;&gt;-->
                            <!--<a>-->
                                <!--<xsl:attribute name="href">-->
                                    <!--<xsl:value-of-->
                                            <!--select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='advancedURL']"/>-->
                                <!--</xsl:attribute>-->
                                <!--<i18n:text>xmlui.dri2xhtml.structural.search-advanced</i18n:text>-->
                            <!--</a>-->
                        <!--</xsl:if>-->
                    <!--</div>-->

                <!--</xsl:if>-->
                <!--&lt;!&ndash; Once the search box is built, the other parts of the options are added &ndash;&gt;-->
                <!--<xsl:apply-templates/>-->

                <!--&lt;!&ndash; DS-984 Add RSS Links to Options Box &ndash;&gt;-->
                <!--<xsl:if test="count(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='feed']) != 0">-->
                    <!--<h1 id="ds-feed-option-head" class="ds-option-set-head">-->
                        <!--<i18n:text>xmlui.feed.header</i18n:text>-->
                    <!--</h1>-->
                    <!--<div id="ds-feed-option" class="ds-option-set">-->
                        <!--<ul>-->
                            <!--<xsl:call-template name="addRSSLinks"/>-->
                        <!--</ul>-->
                    <!--</div>-->
                <!--</xsl:if>-->


            <!--</div>-->
        <!--</div>-->
    <!--</xsl:template>-->

    <xsl:template match="dri:options">
        <div id="ds-options-wrapper">
            <div id="ds-options">

                <!-- Removed search form as found in structural.xsl.  -->
                <!-- Removed  'Search Dspace' text found above search form in Options div. -->

                <xsl:apply-templates/>

                <!-- Custom code for IDEALs to supply INFORMATION block in left navigation -->
                <h1 class="ds-option-set-head">Information</h1>
                <div id="aspect_artifactbrowser_Navigation_list_ideals_information" class="ds-option-set">
                    <div class="list-group ds-simple-list">
                        <!-- Add Javascript to open up Help info in a separate window.
                             This does the same thing as target="_blank", but is valid for XHTML 1.0 -->
                        <a class="list-group-item" onclick="window.open(this.href); return false;" onkeypress="window.open(this.href); return false;">
                            <xsl:attribute name="href">
                                <xsl:text>https://wiki.cites.illinois.edu/wiki/display/IDEALS/Getting+Started+with+IDEALS</xsl:text>
                            </xsl:attribute>
                            <i18n:text>xmlui.dri2xhtml.structural.help</i18n:text>
                        </a>
                        <a class="list-group-item">
                            <xsl:attribute name="href">
                                <xsl:text>https://wiki.cites.illinois.edu/wiki/display/IDEALS/IDEALS+Resources+and+Information</xsl:text>
                            </xsl:attribute>
                            <i18n:text>xmlui.dri2xhtml.structural.about</i18n:text>
                        </a>
                        <a class="list-group-item">
                            <xsl:attribute name="href">
                                <xsl:value-of select="/*/dri:meta/dri:pageMeta/dri:metadata[@qualifier='contactURL']" />
                            </xsl:attribute>
                            <i18n:text>xmlui.dri2xhtml.structural.contact-link</i18n:text>
                        </a>
                    </div>
                </div>
                <!-- End custom navigation INFORMATION block -->

                <!-- Custom code for IDEALs to supply ACCESS RESTRICTIONS block in left navigation -->
                <h1 class="ds-option-set-head">Access Key</h1>
                <div id="aspect_artifactbrowser_Navigation_list_ideals_access_restrictions" class="ds-option-set">
                    <ul class="list-group ds-simple-list">
                        <li class="list-group-item">
                            <div>
                                <img src="{$theme-path}/images/lock.png" alt="Closed Access" title="Closed Access"/>
                            </div>
                            <a onclick="window.open(this.href); return false;"
                               onkeypress="window.open(this.href); return false;">
                                <xsl:attribute name="href">
                                    <xsl:text>https://wiki.cites.illinois.edu/wiki/display/IDEALS/Access+Restriction+Policy</xsl:text>
                                </xsl:attribute>
                                <i18n:text>xmlui.dri2xhtml.structural.access-closed</i18n:text>
                            </a>
                        </li>

                        <li class="list-group-item">
                            <div>
                                <img src="{$theme-path}/images/lock-illinois.png" alt="Campus Access" title="Campus Access"/>
                            </div>
                            <a onclick="window.open(this.href); return false;" onkeypress="window.open(this.href); return false;">
                                <xsl:attribute name="href">
                                    <xsl:text>https://wiki.cites.illinois.edu/wiki/display/IDEALS/Access+Restriction+Policy</xsl:text>
                                </xsl:attribute>
                                <i18n:text>xmlui.dri2xhtml.structural.access-campus</i18n:text>
                            </a>
                        </li>
                    </ul>
                </div>
                 <!--End custom navigation ACCESS RESTRICTIONS block -->

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
