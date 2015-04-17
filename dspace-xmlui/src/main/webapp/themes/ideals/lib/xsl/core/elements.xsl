<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->
<!--
    Templates to cover the common dri elements.

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

    <!--Removed the automatic font sizing for headers, because while I liked the idea,
     in practice it's too unpredictable.
     Also made all head's follow the same rule: count the number of ancestors that have
     a head, that's the number after the 'h' in the tagname-->
    <xsl:template name="renderHead">
        <xsl:param name="class"/>
        <xsl:variable name="head_count" select="count(ancestor::dri:*[dri:head])"/>
        <xsl:element name="h{$head_count}">
            <xsl:call-template name="standardAttributes">
                <xsl:with-param name="class" select="$class"/>
            </xsl:call-template>
            <xsl:apply-templates />
        </xsl:element>
    </xsl:template>


    <xsl:template match="dri:div/dri:head" priority="3">
        <xsl:call-template name="renderHead">
            <xsl:with-param name="class">ds-div-head</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- The second case is the header on tables, which always creates an HTML h3 element -->
    <xsl:template match="dri:table/dri:head" priority="2">
        <xsl:call-template name="renderHead">
            <xsl:with-param name="class">ds-table-head</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- The third case is the header on lists, which creates an HTML h3 element for top level lists and
        and h4 elements for all sublists. -->
    <xsl:template match="dri:list/dri:head" priority="2" mode="nested">
        <xsl:call-template name="renderHead">
            <xsl:with-param name="class">ds-list-head</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dri:list/dri:list/dri:head" priority="3" mode="nested">
        <xsl:call-template name="renderHead">
            <xsl:with-param name="class">ds-sublist-head</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dri:referenceSet/dri:head" priority="2">
        <xsl:call-template name="renderHead">
            <xsl:with-param name="class">ds-list-head</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dri:options/dri:list/dri:head" priority="3">
        <xsl:call-template name="renderHead">
            <xsl:with-param name="class">ds-option-set-head</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dri:head" priority="1">
        <xsl:call-template name="renderHead">
            <xsl:with-param name="class">ds-head</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--IDEALS: change "Submit to this collection" to a button -->
    <xsl:template match="dri:div[@id='aspect.artifactbrowser.CollectionViewer.div.collection-search-browse']/dri:p/dri:xref" priority="3">
        <a>
            <xsl:if test="@target">
                <xsl:attribute name="href"><xsl:value-of select="@target"/></xsl:attribute>
            </xsl:if>

            <xsl:attribute name="class">
                <xsl:text>btn btn-link </xsl:text>
                <xsl:if test="@rend"><xsl:value-of select="@rend"/></xsl:if>
            </xsl:attribute>

            <xsl:if test="@n">
                <xsl:attribute name="name"><xsl:value-of select="@n"/></xsl:attribute>
            </xsl:if>

            <xsl:apply-templates />
        </a>
    </xsl:template>

    <!--IDEALS: hide "Submit to this collection" in the main div -->
    <xsl:template match="dri:div[@id='aspect.artifactbrowser.CollectionViewer.div.collection-view']/dri:p[dri:xref]" priority="3">
        <xsl:element name="p">
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">hidden </xsl:attribute>
            <xsl:apply-templates />
        </xsl:element>
    </xsl:template>


    <!--IDEALS: keep head inside the div-->
    <xsl:template match="dri:div[@id='aspect.artifactbrowser.CommunityRecentSubmissions.div.community-recent-submission' or
                                 @id='aspect.artifactbrowser.CollectionRecentSubmissions.div.collection-recent-submission' or
                                 @id='aspect.rochesterStatistics.StatisticsViewer.div.site-recent-submission' or
                                 @id='aspect.discovery.RelatedItems.div.item-related' or
                                 @id='file.news.div.news']">
        <xsl:apply-templates select="@pagination">
            <xsl:with-param name="position">top</xsl:with-param>
        </xsl:apply-templates>
        <div>
            <xsl:call-template name="standardAttributes">
                <xsl:with-param name="class">ds-static-div</xsl:with-param>
            </xsl:call-template>
            <xsl:apply-templates/>
        </div>
        <xsl:variable name="itemDivision">
            <xsl:value-of select="@n"/>
        </xsl:variable>
        <xsl:variable name="xrefTarget">
            <xsl:value-of select="./dri:p/dri:xref/@target"/>
        </xsl:variable>
        <xsl:if test="$itemDivision='item-view'">
            <xsl:call-template name="cc-license">
                <xsl:with-param name="metadataURL" select="./dri:referenceSet/dri:reference/@url"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:apply-templates select="@pagination">
            <xsl:with-param name="position">bottom</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>


    <!--IDEALS: Filter Search -->
    <xsl:template match="dri:div[@id='edu.uiuc.dspace.filtersearch.FilterSearch.div.filter-search']/dri:list[not(@type)]" priority="2">
        <xsl:attribute name="class"><xsl:text>form-horizontal</xsl:text></xsl:attribute>
        <xsl:attribute name="role"><xsl:text>form</xsl:text></xsl:attribute>
        <h2 class="filter-search-title">
            Search Theses and Dissertations
            <!--a onclick="javascript:toggleFilterSearch(); return false;"
               href="#">Search Theses and Dissertations</a-->
        </h2>
        <xsl:copy>
            <xsl:apply-templates select="dri:item" mode="filter-search"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="dri:item" mode="filter-search">
        <div class="form-group">
            <xsl:if test="name(preceding-sibling::*[position()=1]) = 'label'">
                <xsl:apply-templates select="preceding-sibling::*[position()=1]" mode="filter-search">
                    <xsl:with-param name="input_id" select="dri:field/@id"/>
                </xsl:apply-templates>
            </xsl:if>
            <div class="col-sm-10">
                <xsl:apply-templates mode="filter-search" />
            </div>
        </div>
    </xsl:template>

    <xsl:template match="dri:label" mode="filter-search">
        <xsl:param name="input_id"/>
        <xsl:if test="count(./node())>0">
            <label>
                <xsl:attribute name="class">control-label col-sm-2</xsl:attribute>
                <xsl:attribute name="for"><xsl:value-of select="$input_id"/></xsl:attribute>
                <xsl:apply-templates/>
                <xsl:text>:</xsl:text>
            </label>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dri:field[@type='text']" mode="filter-search">
        <input>
            <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
            <xsl:attribute name="name"><xsl:value-of select="@n"/></xsl:attribute>
            <xsl:attribute name="type"><xsl:value-of select="@type"/></xsl:attribute>
            <xsl:attribute name="class">form-control</xsl:attribute>
            <xsl:copy>
                <xsl:apply-templates/>
            </xsl:copy>
        </input>
    </xsl:template>

    <xsl:template match="dri:field[@type='checkbox' or @type='radio']/dri:option" mode="filter-search">
        <div>
            <xsl:attribute name="class"><xsl:value-of select="../@type"/></xsl:attribute>
            <label>
                <input>
                    <xsl:attribute name="name"><xsl:value-of select="../@n"/></xsl:attribute>
                    <xsl:attribute name="type"><xsl:value-of select="../@type"/></xsl:attribute>
                    <xsl:attribute name="value"><xsl:value-of select="@returnValue"/></xsl:attribute>
                    <xsl:if test="../dri:value[@type='option'][@option = current()/@returnValue]">
                        <xsl:attribute name="checked">checked</xsl:attribute>
                    </xsl:if>
                    <xsl:if test="../@disabled='yes'">
                        <xsl:attribute name="disabled">disabled</xsl:attribute>
                    </xsl:if>
                </input>
                <xsl:apply-templates />
            </label>
        </div>
    </xsl:template>


    <xsl:template match="dri:field[@type='select']" mode="filter-search">
        <select class="form-control">
            <xsl:apply-templates mode="filter-search" />
        </select>
    </xsl:template>


    <xsl:template match="dri:field[@type='select']/dri:option" mode="filter-search">
        <option>
            <xsl:attribute name="value"><xsl:value-of select="@returnValue"/></xsl:attribute>
            <xsl:if test="../dri:value[@type='option'][@option = current()/@returnValue]">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates />
        </option>
    </xsl:template>


    <xsl:template match="dri:field[@type='button']" mode="filter-search">
        <input>
            <xsl:attribute name="class">btn btn-primary</xsl:attribute>
            <xsl:if test="@type='button'">
                <xsl:attribute name="type">submit</xsl:attribute>
            </xsl:if>
            <xsl:attribute name="value">
                <xsl:choose>
                    <xsl:when test="./dri:value[@type='raw']">
                        <xsl:value-of select="./dri:value[@type='raw']"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="./dri:value[@type='default']"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:if test="dri:value/i18n:text">
                <xsl:attribute name="i18n:attr">value</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates />
        </input>
    </xsl:template>

</xsl:stylesheet>
