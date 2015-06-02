<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->
<!--
    Rendering specific to the item display page.

    Author: art.lowel at atmire.com
    Author: lieven.droogmans at atmire.com
    Author: ben at atmire.com
    Author: Alexey Maslov

-->

<xsl:stylesheet
        xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
        xmlns:dri="http://di.tamu.edu/DRI/1.0/"
        xmlns:mets="http://www.loc.gov/METS/"
        xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
        xmlns:xlink="http://www.w3.org/TR/xlink/"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
        xmlns:atom="http://www.w3.org/2005/Atom"
        xmlns:ore="http://www.openarchives.org/ore/terms/"
        xmlns:oreatom="http://www.openarchives.org/ore/atom/"
        xmlns="http://www.w3.org/1999/xhtml"
        xmlns:xalan="http://xml.apache.org/xalan"
        xmlns:encoder="xalan://java.net.URLEncoder"
        xmlns:util="org.dspace.app.xmlui.utils.XSLUtils"
        xmlns:jstring="java.lang.String"
        xmlns:rights="http://cosimo.stanford.edu/sdr/metsrights/"
        xmlns:confman="org.dspace.core.ConfigurationManager"
        exclude-result-prefixes="xalan encoder i18n dri mets dim xlink xsl util jstring rights confman">

    <xsl:output indent="yes"/>

    <xsl:template name="itemSummaryView-DIM">
        <!-- optional: Altmeric.com badge and PlumX widget -->
        <xsl:if test='confman:getProperty("altmetrics", "altmetric.enabled") and ($identifier_doi or $identifier_handle)'>
            <xsl:call-template name='impact-altmetric'/>
        </xsl:if>
        <xsl:if test='confman:getProperty("altmetrics", "plumx.enabled") and $identifier_doi'>
            <xsl:call-template name='impact-plumx'/>
        </xsl:if>

        <!-- Generate the info about the item from the metadata section -->
        <xsl:apply-templates select="./mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim"
                             mode="itemSummaryView-DIM"/>

        <xsl:copy-of select="$SFXLink" />

        <!-- Generate the bitstream information from the file section -->
        <xsl:choose>
            <xsl:when test="./mets:fileSec/mets:fileGrp[@USE='CONTENT' or @USE='ORIGINAL']/mets:file">
                <xsl:apply-templates select="./mets:fileSec/mets:fileGrp[@USE='CONTENT' or @USE='ORIGINAL']">
                    <xsl:with-param name="context" select="."/>
                    <xsl:with-param name="primaryBitstream" select="./mets:structMap[@TYPE='LOGICAL']/mets:div[@TYPE='DSpace Item']/mets:fptr/@FILEID"/>
                </xsl:apply-templates>
            </xsl:when>
            <!-- Special case for handling ORE resource maps stored as DSpace bitstreams -->
            <xsl:when test="./mets:fileSec/mets:fileGrp[@USE='ORE']">
                <xsl:apply-templates select="./mets:fileSec/mets:fileGrp[@USE='ORE']"/>
            </xsl:when>
            <xsl:otherwise>
                <h2><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-head</i18n:text></h2>
                <table class="table ds-table file-list">
                    <thead>
                        <tr class="ds-table-header-row">
                            <th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-file</i18n:text></th>
                            <th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-size</i18n:text></th>
                            <th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-format</i18n:text></th>
                            <th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-view</i18n:text></th>
                        </tr>
                    </thead>
                    <tr>
                        <td colspan="4">
                            <p><i18n:text>xmlui.dri2xhtml.METS-1.0.item-no-files</i18n:text></p>
                        </td>
                    </tr>
                </table>
            </xsl:otherwise>
        </xsl:choose>

        <!-- Output all of the metadata about the item from the metadata section -->
        <!-- For IDEALS, always just show the summary view!...we don't want to show dc.* field names -->
        <xsl:apply-templates select="mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim"
                             mode="itemDetailView-DIM"/>

        <!-- For IDEALS, don't allow License info to display -->
        <!-- Generate the Creative Commons license information from the file section (DSpace deposit license hidden by default)-->
        <!--<xsl:apply-templates select="./mets:fileSec/mets:fileGrp[@USE='CC-LICENSE']"/>-->

    </xsl:template>


    <xsl:template match="dim:dim" mode="itemSummaryView-DIM">
        <div class="item-summary-view-metadata">
            <xsl:call-template name="itemSummaryView-DIM-fields"/>
        </div>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-fields">
        <xsl:param name="clause" select="'1'"/>
        <xsl:param name="phase" select="'even'"/>
        <xsl:variable name="otherPhase">
            <xsl:choose>
                <xsl:when test="$phase = 'even'">
                    <xsl:text>odd</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>even</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:choose>
            <!-- Title row -->
            <xsl:when test="$clause = 1">

                <xsl:choose>
                    <xsl:when test="descendant::text() and (count(dim:field[@element='title'][not(@qualifier)]) &gt; 1)">
                        <!-- display first title as h1 -->
                        <h1>
                            <xsl:value-of select="dim:field[@element='title'][not(@qualifier)][1]/node()"/>
                        </h1>
                        <div class="simple-item-view-other">
                            <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-title</i18n:text>:</span>
                            <span>
                                <xsl:for-each select="dim:field[@element='title'][not(@qualifier)]">
                                    <xsl:value-of select="./node()"/>
                                    <xsl:if test="count(following-sibling::dim:field[@element='title'][not(@qualifier)]) != 0">
                                        <xsl:text>; </xsl:text>
                                        <br/>
                                    </xsl:if>
                                </xsl:for-each>
                            </span>
                        </div>
                    </xsl:when>
                    <xsl:when test="dim:field[@element='title'][descendant::text()] and count(dim:field[@element='title'][not(@qualifier)]) = 1">
                        <h1>
                            <xsl:value-of select="dim:field[@element='title'][not(@qualifier)][1]/node()"/>
                        </h1>
                    </xsl:when>
                    <xsl:otherwise>
                        <h1>
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.no-title</i18n:text>
                        </h1>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- Author(s) row -->
            <xsl:when test="$clause = 2 and (dim:field[@element='contributor'][@qualifier='author' and descendant::text()] or dim:field[@element='creator' and descendant::text()] or dim:field[@element='contributor' and descendant::text()])">
                <div class="simple-item-view-authors">
                    <xsl:choose>
                        <xsl:when test="dim:field[@element='contributor'][@qualifier='author']">
                            <xsl:for-each select="dim:field[@element='contributor'][@qualifier='author']">
                                <span>
                                    <xsl:if test="@authority">
                                        <xsl:attribute name="class"><xsl:text>ds-dc_contributor_author-authority</xsl:text></xsl:attribute>
                                    </xsl:if>
                                    <xsl:copy-of select="node()"/>
                                </span>
                                <xsl:if test="count(following-sibling::dim:field[@element='contributor'][@qualifier='author']) != 0">
                                    <xsl:text>; </xsl:text>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:when test="dim:field[@element='creator']">
                            <xsl:for-each select="dim:field[@element='creator']">
                                <xsl:copy-of select="node()"/>
                                <xsl:if test="count(following-sibling::dim:field[@element='creator']) != 0">
                                    <xsl:text>; </xsl:text>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:when test="dim:field[@element='contributor']">
                            <xsl:for-each select="dim:field[@element='contributor']">
                                <xsl:copy-of select="node()"/>
                                <xsl:if test="count(following-sibling::dim:field[@element='contributor']) != 0">
                                    <xsl:text>; </xsl:text>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.no-author</i18n:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </div>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- identifier.uri row -->
            <xsl:when test="$clause = 3 and (dim:field[@element='identifier' and @qualifier='uri' and descendant::text()])">
                <div class="simple-item-view-other">

                    <!--URI is the unique identifier (e.g. hdl:2142/2)-->
                    <xsl:variable name="uri" select="/mets:METS/@ID"/>
                    <!--URL is the actual hdl.handle.net URL (e.g. http://hdl.handle.net/2142/2)-->
                    <xsl:variable name="hdl">
                        <xsl:if test="contains($uri, 'hdl:')">
                            <xsl:text>http://hdl.handle.net/</xsl:text>
                            <xsl:value-of select="substring-after($uri, 'hdl:')"/>
                        </xsl:if>
                    </xsl:variable>

                    <!--HDL badge-->
                    <a data-toggle="tooltip" data-placement="bottom" title="Persistent Handle: Use this URI when linking to or citing this item">
                        <xsl:attribute name="href">
                            <xsl:value-of select="$hdl"/>
                        </xsl:attribute>
                        <svg xmlns="http://www.w3.org/2000/svg" width="240" height="20">
                            <linearGradient id="b" x2="0" y2="100%">
                                <stop offset="0" stop-color="#bbb" stop-opacity=".1" />
                                <stop offset="1" stop-opacity=".1" />
                            </linearGradient>
                            <mask id="a">
                                <rect width="234" height="20" rx="3" fill="#fff" />
                            </mask>
                            <g mask="url(#a)">
                                <path fill="#587498" d="M0 0h33v20H0z" />
                                <path fill="#F3843E" d="M33 0h201v20H33z" />
                                <path fill="url(#b)" d="M0 0h234v20H0z" />
                            </g>
                            <g fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,Geneva,sans-serif"
                                    font-size="11">
                                <text x="16.5" y="15" fill="#010101" fill-opacity=".3">HDL</text>
                                <text x="16.5" y="14">HDL</text>
                                <text x="132.5" y="15" fill="#010101" fill-opacity=".3">
                                    <xsl:value-of select="$hdl" />
                                </text>
                                <text x="132.5" y="14">
                                    <xsl:value-of select="$hdl" />
                                </text>
                            </g>
                        </svg>
                    </a>

                    <xsl:call-template name="DisplayScopusCitationCount"/>

                    <!--IDEALS: put URI in a box-->
                    <!--URI is the unique identifier (e.g. hdl:2142/2)-->
                    <!--<xsl:variable name="uri" select="/mets:METS/@ID"/>-->
                    <!--&lt;!&ndash;URL is the actual hdl.handle.net URL (e.g. http://hdl.handle.net/2142/2)&ndash;&gt;-->
                    <!--<xsl:variable name="url">-->
                        <!--<xsl:if test="contains($uri, 'hdl:')">-->
                            <!--<xsl:text>http://hdl.handle.net/</xsl:text>-->
                            <!--<xsl:value-of select="substring-after($uri, 'hdl:')"/>-->
                        <!--</xsl:if>-->
                    <!--</xsl:variable>-->

                    <!--<div class="panel-heading text-center">Use <strong>this</strong> link to cite this item:</div>-->
                    <!--<div class="panel-body text-center">-->
                        <!--<a>-->
                            <!--<xsl:attribute name="href">-->
                                <!--<xsl:value-of select="$url"/>-->
                            <!--</xsl:attribute>-->
                            <!--<xsl:value-of select="$url"/>-->
                        <!--</a>-->
                    <!--</div>-->

                    <!--<span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-uri</i18n:text>:</span>-->
                    <!--<span>-->
                    <!--<xsl:for-each select="dim:field[@element='identifier' and @qualifier='uri']">-->
                    <!--<a>-->
                    <!--<xsl:attribute name="href">-->
                    <!--<xsl:copy-of select="./node()"/>-->
                    <!--</xsl:attribute>-->
                    <!--<xsl:copy-of select="./node()"/>-->
                    <!--</a>-->
                    <!--<xsl:if test="count(following-sibling::dim:field[@element='identifier' and @qualifier='uri']) != 0">-->
                    <!--<br/>-->
                    <!--</xsl:if>-->
                    <!--</xsl:for-each>-->
                    <!--</span>-->
                </div>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- date.issued row -->
            <!--<xsl:when test="$clause = 4 and (dim:field[@element='date' and @qualifier='issued' and descendant::text()])">-->
            <!--<div class="simple-item-view-other">-->
            <!--<span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-date</i18n:text>:</span>-->
            <!--<span>-->
            <!--<xsl:for-each select="dim:field[@element='date' and @qualifier='issued']">-->
            <!--<xsl:copy-of select="substring(./node(),1,10)"/>-->
            <!--<xsl:if test="count(following-sibling::dim:field[@element='date' and @qualifier='issued']) != 0">-->
            <!--<br/>-->
            <!--</xsl:if>-->
            <!--</xsl:for-each>-->
            <!--</span>-->
            <!--</div>-->
            <!--<xsl:call-template name="itemSummaryView-DIM-fields">-->
            <!--<xsl:with-param name="clause" select="($clause + 1)"/>-->
            <!--<xsl:with-param name="phase" select="$otherPhase"/>-->
            <!--</xsl:call-template>-->
            <!--</xsl:when>-->

            <!-- Abstract row -->
            <!--<xsl:when test="$clause = 5 and (dim:field[@element='description' and @qualifier='abstract' and descendant::text()])">-->
            <!--<div class="simple-item-view-description">-->
            <!--<h3><i18n:text>xmlui.dri2xhtml.METS-1.0.item-abstract</i18n:text>:</h3>-->
            <!--<div>-->
            <!--<xsl:if test="count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1">-->
            <!--<div class="spacer">&#160;</div>-->
            <!--</xsl:if>-->
            <!--<xsl:for-each select="dim:field[@element='description' and @qualifier='abstract']">-->
            <!--<xsl:choose>-->
            <!--<xsl:when test="node()">-->
            <!--<xsl:copy-of select="node()"/>-->
            <!--</xsl:when>-->
            <!--<xsl:otherwise>-->
            <!--<xsl:text>&#160;</xsl:text>-->
            <!--</xsl:otherwise>-->
            <!--</xsl:choose>-->
            <!--<xsl:if test="count(following-sibling::dim:field[@element='description' and @qualifier='abstract']) != 0">-->
            <!--<div class="spacer">&#160;</div>-->
            <!--</xsl:if>-->
            <!--</xsl:for-each>-->
            <!--<xsl:if test="count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1">-->
            <!--<div class="spacer">&#160;</div>-->
            <!--</xsl:if>-->
            <!--</div>-->
            <!--</div>-->
            <!--<xsl:call-template name="itemSummaryView-DIM-fields">-->
            <!--<xsl:with-param name="clause" select="($clause + 1)"/>-->
            <!--<xsl:with-param name="phase" select="$otherPhase"/>-->
            <!--</xsl:call-template>-->
            <!--</xsl:when>-->

            <!-- Description row -->
            <!--<xsl:when test="$clause = 6 and (dim:field[@element='description' and not(@qualifier) and descendant::text()])">-->
            <!--<div class="simple-item-view-description">-->
            <!--<h3 class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-description</i18n:text>:</h3>-->
            <!--<div>-->
            <!--<xsl:if test="count(dim:field[@element='description' and not(@qualifier)]) &gt; 1 and not(count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1)">-->
            <!--<div class="spacer">&#160;</div>-->
            <!--</xsl:if>-->
            <!--<xsl:for-each select="dim:field[@element='description' and not(@qualifier)]">-->
            <!--<xsl:copy-of select="./node()"/>-->
            <!--<xsl:if test="count(following-sibling::dim:field[@element='description' and not(@qualifier)]) != 0">-->
            <!--<div class="spacer">&#160;</div>-->
            <!--</xsl:if>-->
            <!--</xsl:for-each>-->
            <!--<xsl:if test="count(dim:field[@element='description' and not(@qualifier)]) &gt; 1">-->
            <!--<div class="spacer">&#160;</div>-->
            <!--</xsl:if>-->
            <!--</div>-->
            <!--</div>-->
            <!--<xsl:call-template name="itemSummaryView-DIM-fields">-->
            <!--<xsl:with-param name="clause" select="($clause + 1)"/>-->
            <!--<xsl:with-param name="phase" select="$otherPhase"/>-->
            <!--</xsl:call-template>-->
            <!--</xsl:when>-->

            <!--<xsl:when test="$clause = 7 and $ds_item_view_toggle_url != ''">-->
            <!--<p class="ds-paragraph item-view-toggle item-view-toggle-bottom">-->
            <!--<a>-->
            <!--<xsl:attribute name="href"><xsl:value-of select="$ds_item_view_toggle_url"/></xsl:attribute>-->
            <!--<i18n:text>xmlui.ArtifactBrowser.ItemViewer.show_full</i18n:text>-->
            <!--</a>-->
            <!--</p>-->
            <!--</xsl:when>-->

            <!-- recurse without changing phase if we didn't output anything -->
            <xsl:otherwise>
                <!-- IMPORTANT: This test should be updated if clauses are added! -->
                <xsl:if test="$clause &lt; 5">
                    <xsl:call-template name="itemSummaryView-DIM-fields">
                        <xsl:with-param name="clause" select="($clause + 1)"/>
                        <xsl:with-param name="phase" select="$phase"/>
                    </xsl:call-template>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>

        <!-- Generate the Creative Commons license information from the file section (DSpace deposit license hidden by default) -->
        <xsl:apply-templates select="mets:fileSec/mets:fileGrp[@USE='CC-LICENSE']"/>
    </xsl:template>


    <xsl:template match="dim:dim" mode="itemDetailView-DIM">
        <!--<table class="ds-includeSet-table detailtable">-->
        <!--<xsl:apply-templates mode="itemDetailView-DIM"/>-->
        <!--</table>-->


        <h2><i18n:text>xmlui.dri2xhtml.METS-1.0.item-metadata-head</i18n:text></h2>
        <table class="table table-striped ds-includeSet-table">

            <!-- Check if this item has metadata -->
            <xsl:choose>

                <!--If there's no metadata, display no metadata message-->
                <xsl:when test="not(dim:field)">
                    <tr class="ds-table-row">
                        <!--Display field label-->
                        <td>Item has no metadata.</td>
                    </tr>
                </xsl:when>
                <xsl:otherwise> <!--Otherwise, the item has metadata to display-->

                    <!-- Title = dc.title-->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='title' and not(@qualifier)]"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-title</i18n:text></xsl:with-param>
                        <xsl:with-param name="value-separator">;<br/></xsl:with-param>
                    </xsl:call-template>

                    <!-- Alternative Title = dc.title.alternative -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='title' and @qualifier='alternative']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-alternative</i18n:text></xsl:with-param>
                        <xsl:with-param name="value-separator">;<br/></xsl:with-param>
                    </xsl:call-template>

                    <!-- Author = dc.creator or dc.contributor.author -->
                    <!-- Special Case:  author names are clickable! -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and ((@element='contributor' and @qualifier='author') or @element='creator')]"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-author</i18n:text></xsl:with-param>
                        <xsl:with-param name="link_url"
                                        select="concat($context-path,'/browse?type=author&amp;value=##VALUE##')"/>
                        <xsl:with-param name="value-separator"> </xsl:with-param>
                    </xsl:call-template>


                    <xsl:choose>
                        <xsl:when test="dim:field[@mdschema='thesis']">
                            <!-- THESIS-SPECIFIC METADATA -->
                            <xsl:call-template name="thesis-metadata"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <!-- For NON-THESIS CONTENT, all our Contributors are grouped together -->

                            <!-- Advisors = dc.contributor.advisor -->
                            <!-- Special Case:  contributor names are clickable! -->
                            <xsl:call-template name="metadata-field">
                                <xsl:with-param name="fields"
                                                select="dim:field[@mdschema='dc' and @element='contributor' and @qualifier='advisor']"/>
                                <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-advisor</i18n:text></xsl:with-param>
                                <xsl:with-param name="link_url"
                                                select="concat($context-path,'/browse?type=contributor&amp;value=##VALUE##')"/>
                                <xsl:with-param name="value-separator"> </xsl:with-param>
                            </xsl:call-template>

                            <!-- Other Contributors = dc.contributor.* -->
                            <!-- Special Case:  contributor names are clickable! -->
                            <xsl:call-template name="metadata-field">
                                <xsl:with-param name="fields"
                                                select="dim:field[@mdschema='dc' and @element='contributor' and not(@qualifier='author' or @qualifier='advisor')]"/>
                                <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-contributor</i18n:text></xsl:with-param>
                                <xsl:with-param name="link_url"
                                                select="concat($context-path,'/browse?type=contributor&amp;value=##VALUE##')"/>
                                <xsl:with-param name="value-separator"> </xsl:with-param>
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>


                    <!-- Subject = dc.subject.* -->
                    <!-- Special Case:  keywords are clickable! -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields" select="dim:field[@mdschema='dc' and @element='subject']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-subject</i18n:text></xsl:with-param>
                        <xsl:with-param name="link_url"
                                        select="concat($context-path,'/browse?type=subject&amp;value=##VALUE##')"/>
                        <xsl:with-param name="value-separator"> </xsl:with-param>
                    </xsl:call-template>

                    <!-- Geographic Coverage = dc.coverage.spatial -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='coverage' and @qualifier='spatial']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-spatial</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Temporal Coverage = dc.coverage.temporal -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='coverage' and @qualifier='temporal']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-temporal</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Abstract = dc.description.abstract -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='description' and @qualifier='abstract']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-abstract</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Issue Date = dc.date.issued -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='date' and @qualifier='issued']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-date</i18n:text></xsl:with-param>
                        <xsl:with-param name="value-separator"><br/></xsl:with-param>
                        <xsl:with-param name="type" select="'date'"/>
                    </xsl:call-template>

                    <!-- Date Updated = dc.date.updated -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='date' and @qualifier='updated']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-date-updated</i18n:text></xsl:with-param>
                        <xsl:with-param name="value-separator"><br/></xsl:with-param>
                        <xsl:with-param name="type" select="'date'"/>
                    </xsl:call-template>

                    <!-- Publisher = dc.publisher -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields" select="dim:field[@mdschema='dc' and @element='publisher']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-publisher</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Citation Info = dc.identifier.bibliographicCitiation -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='identifier' and @qualifier='bibliographicCitation']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-bibliographicCitation</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Series / Report = dc.relation.ispartof -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='relation' and @qualifier='ispartof']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-ispartof</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Genre = dc.type.genre -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='type' and @qualifier='genre']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-genre</i18n:text></xsl:with-param>
                        <xsl:with-param name="type" select="'genre-lookup'"/>
                    </xsl:call-template>

                    <!-- Type = dc.type -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='type' and not(@qualifier)]"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-type</i18n:text></xsl:with-param>
                        <xsl:with-param name="type" select="'type-lookup'"/>
                    </xsl:call-template>

                    <!-- Language = dc.language -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='language' and not(@qualifier)]"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-language</i18n:text></xsl:with-param>
                        <xsl:with-param name="type" select="'language-lookup'"/>
                    </xsl:call-template>

                    <!-- Description = dc.description -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='description' and not(@qualifier)]"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-description</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Table of Contents = dc.description.tableofcontents -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='description' and @qualifier='tableofcontents']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-tableofcontents</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- URI = dc.identifier.uri -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='identifier' and @qualifier='uri']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-uri</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- ISBN = dc.identifier.issn -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='identifier' and @qualifier='isbn']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-isbn</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- ISSN = dc.identifier.issn -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='identifier' and @qualifier='issn']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-issn</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- DOI  = dc.identifier.doi -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='identifier' and @qualifier='doi']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-doi</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Other Identifier = dc.identifier -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='identifier' and not(@qualifier)]"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-otherIdentifier</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Publication Status = dc.description.status -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='description' and @qualifier='status']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-status</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Peer-Reviewed = dc.description.peerReview -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='description' and @qualifier='peerReview']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-peerReviewed</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Sponsor / Grant = dc.description.sponsorship -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='description' and @qualifier='sponsorship']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-sponsorship</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Rights Information = dc.rights -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='rights' and not(@qualifier)]"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-rights</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Date Available = dc.date.available -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='date' and @qualifier='available']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-date-available</i18n:text></xsl:with-param>
                        <xsl:with-param name="value-separator"><br/></xsl:with-param>
                        <xsl:with-param name="type" select="'date'"/>
                    </xsl:call-template>

                    <!-- Date Submitted = dc.date.submitted -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='date' and @qualifier='submitted']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-date-submitted</i18n:text></xsl:with-param>
                        <xsl:with-param name="value-separator"><br/></xsl:with-param>
                        <xsl:with-param name="type" select="'date'"/>
                    </xsl:call-template>

                    <!-- Has Version = dc.relation.hasversion -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='relation' and @qualifier='hasversion']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-hasversion</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Is Version Of = dc.relation.isversionof -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='relation' and @qualifier='isversionof']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-isversionof</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Has Part(s) = dc.relation.haspart -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='relation' and @qualifier='haspart']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-haspart</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Replaces = dc.relation.replaces -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='relation' and @qualifier='replaces']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-replaces</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Replaced by = dc.relation.isreplacedby  -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='relation' and @qualifier='isreplacedby']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-isreplacedby</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Relation = dc.relation -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='relation' and not(@qualifier)]"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-relation</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- Identifier in Online Catalog = dc.identifier.localBib -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='identifier' and @qualifier='localBib']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-localBib</i18n:text></xsl:with-param>
                    </xsl:call-template>

                    <!-- OCLC Number = dc.identifier.oclc -->
                    <xsl:call-template name="metadata-field">
                        <xsl:with-param name="fields"
                                        select="dim:field[@mdschema='dc' and @element='identifier' and @qualifier='oclc']"/>
                        <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-oclc</i18n:text></xsl:with-param>
                    </xsl:call-template>

                </xsl:otherwise>
            </xsl:choose>

        </table>


        <span class="Z3988">
            <xsl:attribute name="title">
                <xsl:call-template name="renderCOinS"/>
            </xsl:attribute>
            &#xFEFF; <!-- non-breaking space to force separating the end tag -->
        </span>
        <xsl:copy-of select="$SFXLink" />
    </xsl:template>

    <!--
     Metadata Field Template
     Displays a single metadata field (with its values),
     based on the given inputs.
-->
    <xsl:template name="metadata-field">
        <!--Parameters and their default values-->
        <xsl:param name="fields"/>
        <xsl:param name="label"/>
        <!--<xsl:param name="value-separator"><hr class="metadata-seperator"/></xsl:param>-->
        <xsl:param name="value-separator"><br /></xsl:param>
        <xsl:param name="type" select="'text'"/>
        <xsl:param name="link_url"></xsl:param>
        <!--Only specified for special links-->

        <!--Only display the fields if they exist for this item and have a text node (i.e. value)-->
        <xsl:if test="$fields and $fields/text()">
            <tr class="ds-table-row">
                <!--Display field label-->
                <td class="field-label"><xsl:copy-of select="$label"/>:</td>
                <td>
                    <xsl:for-each select="$fields">
                        <!--Print out value-->
                        <xsl:call-template name="metadata-value">
                            <xsl:with-param name="type" select="$type"/>
                            <xsl:with-param name="link_url" select="$link_url"/>
                        </xsl:call-template>

                        <!--Get info about field, so we can check if there are more values below-->
                        <xsl:variable name="schema" select="./@mdschema"/>
                        <xsl:variable name="element" select="./@element"/>
                        <xsl:variable name="qualifier" select="./@qualifier"/>

                        <!--As long as there are more values for this field, show value separator-->
                        <xsl:if test="position() != last()">
                            <xsl:copy-of select="$value-separator"/>
                            <xsl:text> </xsl:text>
                        </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <!--
         Metadata Values Template
         Prints out a single metadata value, based on the type of field.
         Called by 'metadata-field' template above.

         Various Types:
         'link'  -> display as an <a href>
         'date'  -> only display first 10 chars (year, month, day)
         '*-lookup' -> Special cases, where we need to lookup value elsewhere
         default -> just display value
    -->
    <xsl:template name="metadata-value">
        <xsl:param name="type"/>
        <xsl:param name="link_url"/>
        <!--Only specified for 'link' type-->

        <xsl:choose>
            <!-- Special Links (where Link URL specified...normal links handled below)-->
            <xsl:when test="$link_url">
                <!-- Add value to Link URL and make a clickable link, by replacing
                     the text ##VALUE## with this metadata field's value -->
                <span style="display: inline-block; font-size: larger;"><a class="label label-default">
                    <xsl:attribute name="href">
                        <xsl:value-of select="substring-before($link_url,'##VALUE##')"/>
                        <xsl:value-of select="./node()"/>
                        <xsl:value-of select="substring-after($link_url,'##VALUE##')"/>
                    </xsl:attribute>
                    <xsl:value-of select="./node()"/>
                </a></span>
            </xsl:when>
            <!-- Dates -->
            <xsl:when test="$type='date'">
                <!-- Print out just the Year-Month-Day -->
                <xsl:copy-of select="substring(./node(),1,10)"/>
            </xsl:when>
            <!-- Type field -->
            <xsl:when test="$type='type-lookup'">
                <!--Lookup Type Description (this template is in 'ideals-utils.xsl')-->
                <xsl:call-template name="getTypeDesc">
                    <xsl:with-param name="type">
                        <xsl:value-of select="./node()"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <!-- Genre field -->
            <xsl:when test="$type='genre-lookup'">
                <!--Lookup Genre Description (this template is in 'ideals-utils.xsl')-->
                <xsl:call-template name="getGenreDesc">
                    <xsl:with-param name="genre">
                        <xsl:value-of select="./node()"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <!-- Language field -->
            <xsl:when test="$type='language-lookup'">
                <!--Lookup Language Description (this template is in 'ideals-utils.xsl')-->
                <xsl:call-template name="getLanguageDesc">
                    <xsl:with-param name="lang">
                        <xsl:value-of select="./node()"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <!--Default text type (also handles normal Links)-->
            <xsl:otherwise>
                <xsl:choose>
                    <!--Do a quick test if this text value is a link.
                        If so, we should display it as a link.-->
                    <xsl:when test="starts-with(./node(), 'http://') or starts-with(./node(), 'https://')">
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="./node()"/>
                            </xsl:attribute>
                            <xsl:value-of select="./node()"/>
                        </a>
                    </xsl:when>
                    <xsl:otherwise>
                        <!--Just print out the text value in the field-->
                        <xsl:copy-of select="./node()"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--
     Metadata Fields to display specifically for Theses/Dissertations
-->
    <xsl:template name="thesis-metadata">

        <xsl:choose>
            <xsl:when test="dim:field[@mdschema='thesis' and @element='degree' and @qualifier='level']='Dissertation'">
                <!--FOR DISSERTATIONS, display Committee Members & Research Director-->

                <!-- Research Director = dc.contributor.advisor -->
                <!-- Special Case:  contributor names are clickable! -->
                <xsl:call-template name="metadata-field">
                    <xsl:with-param name="fields"
                                    select="dim:field[@mdschema='dc' and @element='contributor' and @qualifier='advisor']"/>
                    <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-thesis-researchDirector</i18n:text></xsl:with-param>
                    <xsl:with-param name="link_url"
                                    select="concat($context-path,'/browse?type=contributor&amp;value=##VALUE##')"/>
                    <xsl:with-param name="value-separator">; </xsl:with-param>
                </xsl:call-template>

                <!-- Research Director = dc.contributor.researchDirector -->
                <!-- Special Case:  contributor names are clickable! -->
                <xsl:call-template name="metadata-field">
                    <xsl:with-param name="fields"
                                    select="dim:field[@mdschema='dc' and @element='contributor' and @qualifier='researchDirector']"/>
                    <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-thesis-researchDirector</i18n:text></xsl:with-param>
                    <xsl:with-param name="link_url"
                                    select="concat($context-path,'/browse?type=contributor&amp;value=##VALUE##')"/>
                    <xsl:with-param name="value-separator">; </xsl:with-param>
                </xsl:call-template>

                <!-- Committee Chairs = dc.contributor.committeeChair -->
                <!-- Special Case:  contributor names are clickable! -->
                <xsl:call-template name="metadata-field">
                    <xsl:with-param name="fields"
                                    select="dim:field[@mdschema='dc' and @element='contributor' and @qualifier='committeeChair']"/>
                    <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-thesis-committeeChair</i18n:text></xsl:with-param>
                    <xsl:with-param name="link_url"
                                    select="concat($context-path,'/browse?type=contributor&amp;value=##VALUE##')"/>
                    <xsl:with-param name="value-separator">; </xsl:with-param>
                </xsl:call-template>

                <!-- Committee Members = dc.contributor.committeeMember -->
                <!-- Special Case:  contributor names are clickable! -->
                <xsl:call-template name="metadata-field">
                    <xsl:with-param name="fields"
                                    select="dim:field[@mdschema='dc' and @element='contributor' and @qualifier='committeeMember']"/>
                    <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-thesis-committeeMember</i18n:text></xsl:with-param>
                    <xsl:with-param name="link_url"
                                    select="concat($context-path,'/browse?type=contributor&amp;value=##VALUE##')"/>
                    <xsl:with-param name="value-separator">; </xsl:with-param>
                </xsl:call-template>

                <!-- Other Contributors = dc.contributor.* -->
                <!-- Special Case:  contributor names are clickable! -->
                <xsl:call-template name="metadata-field">
                    <xsl:with-param name="fields"
                                    select="dim:field[@mdschema='dc' and @element='contributor' and not(@qualifier='author' or @qualifier='advisor' or @qualifier='committeeChair' or @qualifier='committeeMember')]"/>
                    <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-contributor</i18n:text></xsl:with-param>
                    <xsl:with-param name="link_url"
                                    select="concat($context-path,'/browse?type=contributor&amp;value=##VALUE##')"/>
                    <xsl:with-param name="value-separator">; </xsl:with-param>
                </xsl:call-template>

            </xsl:when>
            <xsl:otherwise>
                <!--FOR THESIS, just display Advisor-->

                <!-- Advisors = dc.contributor.advisor -->
                <!-- Special Case:  contributor names are clickable! -->
                <xsl:call-template name="metadata-field">
                    <xsl:with-param name="fields"
                                    select="dim:field[@mdschema='dc' and @element='contributor' and @qualifier='advisor']"/>
                    <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-advisor</i18n:text></xsl:with-param>
                    <xsl:with-param name="link_url"
                                    select="concat($context-path,'/browse?type=contributor&amp;value=##VALUE##')"/>
                    <xsl:with-param name="value-separator">; </xsl:with-param>
                </xsl:call-template>

                <!-- Other Contributors = dc.contributor.* -->
                <!-- Special Case:  contributor names are clickable! -->
                <xsl:call-template name="metadata-field">
                    <xsl:with-param name="fields"
                                    select="dim:field[@mdschema='dc' and @element='contributor' and not(@qualifier='author' or @qualifier='advisor')]"/>
                    <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-contributor</i18n:text></xsl:with-param>
                    <xsl:with-param name="link_url"
                                    select="concat($context-path,'/browse?type=contributor&amp;value=##VALUE##')"/>
                    <xsl:with-param name="value-separator">; </xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>

        <!-- Thesis Department = thesis.degree.department -->
        <xsl:call-template name="metadata-field">
            <xsl:with-param name="fields"
                            select="dim:field[@mdschema='thesis' and @element='degree' and @qualifier='department']"/>
            <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-thesis-degreeDepartment</i18n:text></xsl:with-param>
        </xsl:call-template>

        <!-- Thesis Discipline = thesis.degree.discipline -->
        <xsl:call-template name="metadata-field">
            <xsl:with-param name="fields"
                            select="dim:field[@mdschema='thesis' and @element='degree' and @qualifier='discipline']"/>
            <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-thesis-degreeDiscipline</i18n:text></xsl:with-param>
        </xsl:call-template>

        <!-- Thesis Grantor = thesis.degree.grantor -->
        <xsl:call-template name="metadata-field">
            <xsl:with-param name="fields"
                            select="dim:field[@mdschema='thesis' and @element='degree' and @qualifier='grantor']"/>
            <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-thesis-degreeGrantor</i18n:text></xsl:with-param>
        </xsl:call-template>

        <!-- Thesis Degree = thesis.degree.name -->
        <xsl:call-template name="metadata-field">
            <xsl:with-param name="fields"
                            select="dim:field[@mdschema='thesis' and @element='degree' and @qualifier='name']"/>
            <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-thesis-degreeName</i18n:text></xsl:with-param>
        </xsl:call-template>

        <!-- Thesis Level = thesis.degree.level -->
        <xsl:call-template name="metadata-field">
            <xsl:with-param name="fields"
                            select="dim:field[@mdschema='thesis' and @element='degree' and @qualifier='level']"/>
            <xsl:with-param name="label"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-thesis-degreeLevel</i18n:text></xsl:with-param>
        </xsl:call-template>

    </xsl:template>


    <!--
     This template maps IDEALS 'type' values into user-friendly descriptions.
     Essentially, it looks for a corresponding 'key' in your messages.xml.
       xmlui.dri2xhtml.type.{Type DB value}
       (e.g.) xmlui.dri2xhtml.type.text = Text

     If a key is found, the translated value is displayed as the Type (e.g. 'Type')
     If a key is NOT found, the Type DB value is displayed (e.g. 'text')
   -->
    <xsl:template name="getTypeDesc">
        <xsl:param name="type"/>

        <!--build full key name-->
        <xsl:variable name="type-key">xmlui.dri2xhtml.type.<xsl:value-of select='$type'/></xsl:variable>

        <!--Lookup the key in messages.xml language file.  If not found, just display DB value-->
        <i18n:text i18n:key="{$type-key}"><xsl:value-of select="$type"/></i18n:text>
    </xsl:template>

    <!--
      This template maps IDEALS 'genre' values into user-friendly descriptions.
      Essentially, it looks for a corresponding 'key' in your messages.xml.
        xmlui.dri2xhtml.genre.{Genre DB value}
        (e.g.) xmlui.dri2xhtml.genre.video = Video

      If a key is found, the translated value is displayed as the Genre (e.g. 'Video')
      If a key is NOT found, the Genre DB value is displayed (e.g. 'video')
    -->
    <xsl:template name="getGenreDesc">
        <xsl:param name="genre"/>

        <!--build full key name-->
        <xsl:variable name="genre-key">xmlui.dri2xhtml.genre.<xsl:value-of select='$genre'/></xsl:variable>

        <!--Lookup the key in messages.xml language file.  If not found, just display DB value-->
        <i18n:text i18n:key="{$genre-key}"><xsl:value-of select="$genre"/></i18n:text>
    </xsl:template>


    <!--
      This template maps common ISO Language Codes (e.g. 'en') to user-friendly
      language descriptions (e.g. "English").
      Essentially, it looks for a corresponding 'key' in your messages.xml.
        xmlui.dri2xhtml.language.{ISO Code}
        (e.g.) xmlui.dri2xhtml.language.en = English

      If a key is found, the translated value is displayed as the Language (e.g. 'English')
      If a key is NOT found, the ISO code is displayed (e.g. 'en')
    -->
    <xsl:template name="getLanguageDesc">
        <xsl:param name="lang"/> <!--Pass in the Language ISO code (e.g. "en" for English)-->

        <!--build full key name for Language-->
        <xsl:variable name="lang-key">xmlui.dri2xhtml.language.<xsl:value-of select='$lang'/></xsl:variable>

        <!--Lookup the Language's key in messages.xml language file.  If not found, just display Language code-->
        <i18n:text i18n:key="{$lang-key}"><xsl:value-of select="$lang"/></i18n:text>
    </xsl:template>

    <!--<xsl:template match="dim:field" mode="itemDetailView-DIM">-->
    <!--<tr>-->
    <!--<xsl:attribute name="class">-->
    <!--<xsl:text>ds-table-row </xsl:text>-->
    <!--<xsl:if test="(position() div 2 mod 2 = 0)">even </xsl:if>-->
    <!--<xsl:if test="(position() div 2 mod 2 = 1)">odd </xsl:if>-->
    <!--</xsl:attribute>-->
    <!--<td class="label-cell">-->
    <!--<xsl:value-of select="./@mdschema"/>-->
    <!--<xsl:text>.</xsl:text>-->
    <!--<xsl:value-of select="./@element"/>-->
    <!--<xsl:if test="./@qualifier">-->
    <!--<xsl:text>.</xsl:text>-->
    <!--<xsl:value-of select="./@qualifier"/>-->
    <!--</xsl:if>-->
    <!--</td>-->
    <!--<td>-->
    <!--<xsl:copy-of select="./node()"/>-->
    <!--<xsl:if test="./@authority and ./@confidence">-->
    <!--<xsl:call-template name="authorityConfidenceIcon">-->
    <!--<xsl:with-param name="confidence" select="./@confidence"/>-->
    <!--</xsl:call-template>-->
    <!--</xsl:if>-->
    <!--</td>-->
    <!--<td><xsl:value-of select="./@language"/></td>-->
    <!--</tr>-->
    <!--</xsl:template>-->

    <!-- don't render the item-view-toggle automatically in the summary view, only when it gets called -->
    <xsl:template match="dri:p[contains(@rend , 'item-view-toggle') and
        (preceding-sibling::dri:referenceSet[@type = 'summaryView'] or following-sibling::dri:referenceSet[@type = 'summaryView'])]">
    </xsl:template>

    <!-- don't render the head on the item view page -->
    <xsl:template match="dri:div[@n='item-view']/dri:head" priority="5">
    </xsl:template>

    <!--
      It displays the table associated with 'Files in this Item' found on the item view page.
      For IDEALS, we removed the columns for 'size' and the 'view/open' link.  Instead,
      the filesize is displayed next to the name.  We also added a 'file description' column.
    -->
    <!-- Generate the bitstream information from the file section -->
    <xsl:template match="mets:fileGrp[@USE='CONTENT']">
        <xsl:param name="context"/>
        <xsl:param name="primaryBitstream" select="-1"/>

        <h2><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-head</i18n:text></h2>
        <table class="table table-condensed ds-table file-list">
            <thead>
                <tr class="ds-table-header-row">
                    <th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-file</i18n:text></th>
                    <th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-description</i18n:text></th>
                    <th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-format</i18n:text></th>
                </tr>
            </thead>
            <!-- In IDEALS,when item have more than one formats, we list all formats under 'View Item' table.
              The order of the formats is :
              1, when there is a primaryBitstream which is also HTML, ONLY display that bitstream
              2, when there is a primaryBitstream (not HTML), the primaryBitstream is displayed first
              3, If there are pdf files, the pdf files are displayed first
              4, otherwise, just display in their default order-->
            <xsl:choose>

                <!-- (1) If primary bitstream is text/html MIME type, only display the primary bitstream -->
                <xsl:when test="mets:file[@ID=$primaryBitstream]/@MIMETYPE='text/html'">
                    <xsl:apply-templates select="mets:file[@ID=$primaryBitstream]">
                        <xsl:with-param name="context" select="$context"/>
                    </xsl:apply-templates>
                </xsl:when>

                <!-- (2) if there's a primaryBitstream, primary is the fist -->
                <xsl:when test="mets:file[@ID=$primaryBitstream]">
                    <xsl:apply-templates select="mets:file[@ID=$primaryBitstream]"/>
                    <xsl:apply-templates select="mets:file[@ID!=$primaryBitstream]"/>
                </xsl:when>

                <!-- (3) if there are pdfs, pdfs are the fist -->
                <xsl:when test="mets:file[@MIMETYPE='application/pdf']">
                    <xsl:apply-templates select="mets:file[@MIMETYPE='application/pdf']"/>
                    <xsl:apply-templates select="mets:file[@MIMETYPE!='application/pdf']"/>
                </xsl:when>

                <!-- Otherwise, just display in their default order -->
                <xsl:otherwise>
                    <xsl:apply-templates select="mets:file"/>
                </xsl:otherwise>
            </xsl:choose>

            <!-- If we have auto-converted additional formats for any files,
                 offer them in the other formats -->
            <xsl:if test="../mets:fileGrp[@USE='CONVERSION']">
                <tr class="ds-table-subheader-row">
                    <td colspan="3">Other Available Formats</td>
                </tr>
                <xsl:apply-templates select="../mets:fileGrp[@USE='CONVERSION']/mets:file"/>
            </xsl:if>
        </table>
    </xsl:template>

    <!--<xsl:template match="mets:fileGrp[@USE='CONTENT']">-->
    <!--<xsl:param name="context"/>-->
    <!--<xsl:param name="primaryBitstream" select="-1"/>-->

    <!--<h2><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-head</i18n:text></h2>-->
    <!--<div class="file-list">-->
    <!--<xsl:choose>-->
    <!--&lt;!&ndash; If one exists and it's of text/html MIME type, only display the primary bitstream &ndash;&gt;-->
    <!--<xsl:when test="mets:file[@ID=$primaryBitstream]/@MIMETYPE='text/html'">-->
    <!--<xsl:apply-templates select="mets:file[@ID=$primaryBitstream]">-->
    <!--<xsl:with-param name="context" select="$context"/>-->
    <!--</xsl:apply-templates>-->
    <!--</xsl:when>-->
    <!--&lt;!&ndash; Otherwise, iterate over and display all of them &ndash;&gt;-->
    <!--<xsl:otherwise>-->
    <!--<xsl:apply-templates select="mets:file">-->
    <!--&lt;!&ndash;Do not sort any more bitstream order can be changed&ndash;&gt;-->
    <!--&lt;!&ndash;<xsl:sort data-type="number" select="boolean(./@ID=$primaryBitstream)" order="descending" />&ndash;&gt;-->
    <!--&lt;!&ndash;<xsl:sort select="mets:FLocat[@LOCTYPE='URL']/@xlink:title"/>&ndash;&gt;-->
    <!--<xsl:with-param name="context" select="$context"/>-->
    <!--</xsl:apply-templates>-->
    <!--</xsl:otherwise>-->
    <!--</xsl:choose>-->
    <!--</div>-->
    <!--</xsl:template>-->

    <!-- Build a single row in the bitsreams table of the item view page -->
    <xsl:template match="mets:file">
        <xsl:param name="context" select="."/>
        <tr>
            <xsl:attribute name="class">
                <xsl:text>ds-table-row </xsl:text>
                <xsl:if test="(position() mod 2 = 0)">even </xsl:if>
                <xsl:if test="(position() mod 2 = 1)">odd </xsl:if>
            </xsl:attribute>
            <td>
                <!-- Add File Format icon, based on MIME Type -->
                <!-- This template is in 'ideals-utils.xsl' -->
                <xsl:call-template name="getFileFormatIcon">
                    <xsl:with-param name="mimetype">
                        <xsl:value-of select="substring-before(@MIMETYPE,'/')"/>
                        <xsl:text>/</xsl:text>
                        <xsl:value-of select="substring-after(@MIMETYPE,'/')"/>
                    </xsl:with-param>
                </xsl:call-template>

                <!-- Add Javascript to open up file downloads in a separate window.
                     This does the same thing as target="_blank", but is valid for XHTML 1.0.
                     Necessary so restricted files don't leave user at a login screen after download. -->
                <!--  Also adds a "trackPageview" call, so that Google Analytics can track file downloads. -->
                <a onclick="javascript:pageTracker._trackPageview(this.href); window.open(this.href); return false;" onkeypress="window.open(this.href); return false;">
                    <xsl:attribute name="href">
                        <xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:title"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="string-length(mets:FLocat[@LOCTYPE='URL']/@xlink:title) > 50">
                            <xsl:variable name="title_length" select="string-length(mets:FLocat[@LOCTYPE='URL']/@xlink:title)"/>
                            <xsl:value-of select="substring(mets:FLocat[@LOCTYPE='URL']/@xlink:title,1,15)"/>
                            <xsl:text> ... </xsl:text>
                            <xsl:value-of select="substring(mets:FLocat[@LOCTYPE='URL']/@xlink:title,$title_length - 25,$title_length)"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:title"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
                <!-- Display File size immediately after title (in parens)-->
                <!-- File size always comes in bytes and thus needs conversion -->
                <span class="file-size">
                    <xsl:text> (</xsl:text>
                    <xsl:call-template name="getFileSize">
                        <xsl:with-param name="file" select="."/>
                    </xsl:call-template>
                    <xsl:text>) </xsl:text>
                </span>

                <!--Check if this file has access restritions, and if so, display a lock icon-->
                <!-- This template is in 'ideals-utils.xsl' -->
                <xsl:call-template name="getFileRestrictionIcon">
                    <xsl:with-param name="file" select="."/>
                </xsl:call-template>
            </td>
            <!-- File Description -->
            <td>
                <xsl:choose>
                    <xsl:when test="mets:FLocat[@LOCTYPE='URL']/@xlink:label">
                        <xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:label"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <em><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-no-description</i18n:text></em>
                    </xsl:otherwise>
                </xsl:choose>

            </td>
            <!-- Hacked based on Dorothea Salo's work, to display more useful file
                 descriptions:
                 http://cavlec.yarinareth.net/2008/10/27/fugly-manakin-hack-user-friendly-file-descriptions/ -->
            <td>
                <!-- Note to self: This method is in our 'ideals-utils.xsl'-->
                <xsl:call-template name="getFileTypeDesc">
                    <xsl:with-param name="mimetype">
                        <xsl:value-of select="substring-before(@MIMETYPE,'/')"/>
                        <xsl:text>/</xsl:text>
                        <xsl:value-of select="substring-after(@MIMETYPE,'/')"/>
                    </xsl:with-param>
                </xsl:call-template>
            </td>
        </tr>
    </xsl:template>

    <!--
    File Format Icon:
    This template determines the appropriate icon to display next to
    a file of a given MIME Type.  An HTML <img> tag is returned, with
    the 'src' set to the location of the appropriate format icon.

    If the MIME Type is unknown a generic file icon is returned.
  -->
    <xsl:template name="getFileFormatIcon">
        <xsl:param name="mimetype"/>

        <!--Get values before and after the slash in a MIMETYPE (e.g. application/pdf)-->
        <xsl:variable name="before-slash" select="substring-before($mimetype,'/')"/>
        <xsl:variable name="after-slash" select="substring-after($mimetype,'/')"/>

        <xsl:variable name="icon-path">
            <!--Add the path of the icon folder in our Theme-->
            <xsl:value-of select="$theme-path"/>
            <xsl:text>/images/file-format/</xsl:text>

            <!--Now, narrow down a valid icon, based on what's before slash in MIMETYPE-->
            <xsl:choose>
                <!-- Application MIME types-->
                <xsl:when test="$before-slash='application'">
                    <xsl:choose>
                        <!--PDF Format-->
                        <xsl:when test="$after-slash='pdf'"><xsl:text>pdf.png</xsl:text></xsl:when>
                        <!--Word Format-->
                        <xsl:when test="$after-slash='msword'"><xsl:text>word.png</xsl:text></xsl:when>
                        <xsl:when test="$after-slash='vnd.openxmlformats-officedocument.wordprocessingml.document'"><xsl:text>word.png</xsl:text></xsl:when>
                        <!--Powerpoint Format-->
                        <xsl:when test="$after-slash='vnd.ms-powerpoint'"><xsl:text>powerpoint.png</xsl:text></xsl:when>
                        <xsl:when test="$after-slash='vnd.openxmlformats-officedocument.presentationml.presentation'"><xsl:text>powerpoint.png</xsl:text></xsl:when>
                        <!--Excel Format-->
                        <xsl:when test="$after-slash='vnd.ms-excel'"><xsl:text>excel.png</xsl:text></xsl:when>
                        <xsl:when test="$after-slash='vnd.openxmlformats-officedocument.spreadsheetml.sheet'"><xsl:text>excel.png</xsl:text></xsl:when>
                        <!--OpenOffice.org Writer Format-->
                        <xsl:when test="$after-slash='vnd.oasis.opendocument.text'"><xsl:text>ooo-writer.png</xsl:text></xsl:when>
                        <xsl:when test="$after-slash='vnd.sun.xml.writer.global'"><xsl:text>ooo-writer.png</xsl:text></xsl:when>
                        <!--OpenOffice.org Impress Format-->
                        <xsl:when test="$after-slash='vnd.oasis.opendocument.presentation'"><xsl:text>ooo-impress.png</xsl:text></xsl:when>
                        <xsl:when test="$after-slash='vnd.sun.xml.impress'"><xsl:text>ooo-impress.png</xsl:text></xsl:when>
                        <!--OpenOffice.org Calc Format-->
                        <xsl:when test="$after-slash='vnd.oasis.opendocument.spreadsheet'"><xsl:text>ooo-calc.png</xsl:text></xsl:when>
                        <xsl:when test="$after-slash='vnd.sun.xml.calc'"><xsl:text>ooo-calc.png</xsl:text></xsl:when>
                        <!--OpenOffice.org Draw Format-->
                        <xsl:when test="$after-slash='vnd.oasis.opendocument.graphics'"><xsl:text>ooo-draw.png</xsl:text></xsl:when>
                        <xsl:when test="$after-slash='vnd.sun.xml.draw'"><xsl:text>ooo-draw.png</xsl:text></xsl:when>
                        <!--OpenOffice.org Base Format-->
                        <xsl:when test="$after-slash='vnd.oasis.opendocument.database'"><xsl:text>ooo-base.png</xsl:text></xsl:when>
                        <!--OpenOffice.org Math Format-->
                        <xsl:when test="$after-slash='vnd.oasis.opendocument.formula'"><xsl:text>ooo-math.png</xsl:text></xsl:when>
                        <!--OpenOffice.org Math Format-->
                        <xsl:when test="$after-slash='zip'"><xsl:text>page_white_compressed.png</xsl:text></xsl:when>
                        <!--All other application-based formats-->
                        <xsl:otherwise><xsl:text>page_white.png</xsl:text></xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <!-- Audio MIME types-->
                <xsl:when test="$before-slash='audio'">
                    <xsl:text>audio.png</xsl:text>
                </xsl:when>
                <!-- Image MIME types-->
                <xsl:when test="$before-slash='image'">
                    <xsl:text>picture.png</xsl:text>
                </xsl:when>
                <!-- Text MIME types-->
                <xsl:when test="$before-slash='text'">
                    <xsl:choose>
                        <!--HTML Format-->
                        <xsl:when test="$after-slash='html'"><xsl:text>html.png</xsl:text></xsl:when>
                        <!--CSV Format-->
                        <xsl:when test="$after-slash='csv'"><xsl:text>csv.png</xsl:text></xsl:when>
                        <!--XML Format-->
                        <xsl:when test="$after-slash='xml'"><xsl:text>xml.png</xsl:text></xsl:when>
                        <!--All other text-based formats-->
                        <xsl:otherwise><xsl:text>text.png</xsl:text></xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <!-- Video MIME types-->
                <xsl:when test="$before-slash='video'">
                    <xsl:text>video.png</xsl:text>
                </xsl:when>
                <!-- Unknown MIME Type-->
                <xsl:otherwise><xsl:text>page_white.png</xsl:text></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <!--<p><xsl:value-of select="$mimetype"/></p>-->
        <!--Return an 'img' tag which references this file format icon-->
        <xsl:variable name="file_type">
            <xsl:call-template name="getFileTypeDesc">
                <xsl:with-param name="mimetype" select="$mimetype"/>
                <xsl:with-param name="translate-key" select="'false'"/>
            </xsl:call-template>
        </xsl:variable>
        <img class="format-icon">
            <xsl:attribute name="src">
                <xsl:value-of select="$icon-path"/>
            </xsl:attribute>

            <!--Display the file type description as the 'alt' and 'title' attributes-->
            <xsl:attribute name="alt"><xsl:value-of select="$file_type"/></xsl:attribute>
            <xsl:attribute name="title"><xsl:value-of select="$file_type"/></xsl:attribute>
        </img>


    </xsl:template>

    <!--
      Get File Size:
      This template returns the size of a given file,
      with the appropriate scale (bytes, KB, MB)

      Required Param: the <mets:file> tag in METS document
      -->
    <xsl:template name="getFileSize">
        <xsl:param name="file"/> <!--The <mets:file> tag in METS document-->

        <xsl:choose>
            <xsl:when test="@SIZE &lt; 1000">
                <xsl:value-of select="@SIZE"/>
                <i18n:text>xmlui.dri2xhtml.METS-1.0.size-bytes</i18n:text>
            </xsl:when>
            <xsl:when test="@SIZE &lt; 1000000">
                <xsl:value-of select="string(round(@SIZE div 1000))"/>
                <i18n:text>xmlui.dri2xhtml.METS-1.0.size-kilobytes</i18n:text>
            </xsl:when>
            <xsl:when test="@SIZE &lt; 1000000000">
                <xsl:value-of select="string(round(@SIZE div 1000000))"/>
                <i18n:text>xmlui.dri2xhtml.METS-1.0.size-megabytes</i18n:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="string(round(@SIZE div 1000000000))"/>
                <i18n:text>xmlui.dri2xhtml.METS-1.0.size-gigabytes</i18n:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <!--
      File Access Restriction Icon:
      This template determines the appropriate icon to display next to
      a file, based on whether it has access restrictions or NOT.
      If the file has restrictions, then an <img> tag is returned
      which references the appropriate restriction (lock) icon.  If the file
      has no restrictions, then nothing is returned.

      NOTE: This template only works if the METSRights section is enabled
      within METS documents.  To enable it, you need the following:
      (1) Enable METRightsDisseminationCrosswalk in dspace.cfg, by adding
          the following to the Crosswalk Plugin settings:

          plugin.named.org.dspace.content.crosswalk.DisseminationCrosswalk = \
          ... \
          ... \
          org.dspace.content.crosswalk.METSRightsDisseminationCrosswalk = METSRights

      (2) Append "?rightsMDTypes=METSRights" to the METS URL for Files, likely
          in the following template from 'structural.xsl':

          <xsl:template match="dri:reference" mode="summaryView">

      Required Param: the <mets:file> tag in METS document
    -->
    <xsl:template name="getFileRestrictionIcon">
        <xsl:param name="file"/> <!--The <mets:file> tag in METS document-->

        <!--Name of the DSpace Group for Illinois faculty/staff/students ONLY -->
        <xsl:variable name="Illinois_Users_Group" select="'UIUC Users [automated]'"/>

        <!--Get all Administrative Metadata IDs for this current file-->
        <xsl:variable name="file_ADMIDs" select="$file/@ADMID"/>

        <!--Look for corresponding "METSRights" section in METS Document for this file.
            It *MUST* be there for us to tell if there are access restrictions-->
        <xsl:variable name="file_restrictions" select="/mets:METS/mets:amdSec/mets:rightsMD[@ID=$file_ADMIDs]/mets:mdWrap[@OTHERMDTYPE='METSRIGHTS']"/>

        <!--<p><xsl:copy-of select="$file_restrictions//rights:Context"/></p>-->
        <!--Attempt to determine the name of the appropriate access restriction icon-->
        <!--<xsl:variable name="restriction_icon">-->
            <!--<xsl:if test="$file_restrictions">-->
                <!--&lt;!&ndash;Assuming we have file restrictions, we need to determine the *type*-->
                    <!--of access restrictions in place. This type is defined by within-->
                    <!--the <Context> tag of METSRights section &ndash;&gt;-->
                <!--<xsl:choose>-->
                    <!--&lt;!&ndash;First, check if General Public is given access (this overrides all other settings)&ndash;&gt;-->
                    <!--<xsl:when test="$file_restrictions//rights:Context[@in-effect='true' and @CONTEXTCLASS='GENERAL PUBLIC']"></xsl:when>-->
                    <!--&lt;!&ndash;Next, check for restrictions to Illinois users ONLY&ndash;&gt;-->
                    <!--<xsl:when test="$file_restrictions//rights:Context[@in-effect='true' and @CONTEXTCLASS='MANAGED GRP']/rights:UserName[@USERTYPE='GROUP']/text()=$Illinois_Users_Group">lock-illinois.png</xsl:when>-->
                    <!--&lt;!&ndash;Otherwise, there are tighter restrictions, so display general lock icon&ndash;&gt;-->
                    <!--<xsl:otherwise>lock.png</xsl:otherwise>-->
                <!--</xsl:choose>-->
            <!--</xsl:if>-->
        <!--</xsl:variable>-->

        <!--The file only has access restrictions
            if the $restriction_icon has a value-->
        <!--<xsl:if test="string-length($restriction_icon)>0">-->

            <!--&lt;!&ndash;Determine the access restriction text to display as img 'alt' attribute&ndash;&gt;-->
            <!--<xsl:variable name="restriction_text">-->
                <!--<xsl:if test="$restriction_icon='lock.png'">Restricted Access</xsl:if>-->
                <!--<xsl:if test="$restriction_icon='lock-illinois.png'">Restricted to U of Illinois</xsl:if>-->
            <!--</xsl:variable>-->

            <!--&lt;!&ndash;Build an <img> tag to display the restriction icon&ndash;&gt;-->
            <!--<img alt="Access Restrictions Icon" class="restrict-icon">-->
                <!--<xsl:attribute name="src">-->
                    <!--<xsl:value-of select="$theme-path"/>-->
                    <!--<xsl:text>/images/</xsl:text>-->
                    <!--<xsl:value-of select="$restriction_icon"/>-->
                <!--</xsl:attribute>-->
                <!--<xsl:attribute name="alt"><xsl:value-of select="$restriction_text"/></xsl:attribute>-->
                <!--<xsl:attribute name="title"><xsl:value-of select="$restriction_text"/></xsl:attribute>-->
            <!--</img>-->
        <!--</xsl:if>-->

      <xsl:variable name="restriction_text">
        <xsl:if test="$file_restrictions">
          <!--Assuming we have file restrictions, we need to determine the *type*
              of access restrictions in place. This type is defined by within
              the <Context> tag of METSRights section -->
          <xsl:choose>
            <!--First, check if General Public is given access (this overrides all other settings)-->
            <xsl:when test="$file_restrictions//rights:Context[@in-effect='true' and @CONTEXTCLASS='GENERAL PUBLIC']"></xsl:when>
            <!--Next, check for restrictions to Illinois users ONLY-->
            <xsl:when
              test="$file_restrictions//rights:Context[@in-effect='true' and @CONTEXTCLASS='MANAGED GRP']/rights:UserName[@USERTYPE='GROUP']/text()=$Illinois_Users_Group">UofI Only</xsl:when>
            <!--Otherwise, there are tighter restrictions, so display general lock icon-->
            <xsl:otherwise>Restricted</xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:variable>

      <xsl:if test="string-length($restriction_text)>0">

        <!--Determine the access restriction text to display as img 'alt' attribute-->
        <xsl:variable name="label-type">
          <xsl:if test="$restriction_text='Open Access'">label-success</xsl:if>
          <xsl:if test="$restriction_text='Restricted'">label-danger</xsl:if>
          <xsl:if test="$restriction_text='UofI Only'">label-warning</xsl:if>
        </xsl:variable>

        <span>
          <xsl:attribute name="class">label <xsl:value-of select="$label-type"/></xsl:attribute>
          <xsl:value-of select="$restriction_text"/>
        </span>

      </xsl:if>

    </xsl:template>



    <!--<xsl:template match="mets:file">-->
    <!--<xsl:param name="context" select="."/>-->
    <!--<div class="file-wrapper clearfix">-->
    <!--<div class="thumbnail-wrapper">-->
    <!--<a class="image-link">-->
    <!--<xsl:attribute name="href">-->
    <!--<xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>-->
    <!--</xsl:attribute>-->
    <!--<xsl:choose>-->
    <!--<xsl:when test="$context/mets:fileSec/mets:fileGrp[@USE='THUMBNAIL']/-->
    <!--mets:file[@GROUPID=current()/@GROUPID]">-->
    <!--<img alt="Thumbnail">-->
    <!--<xsl:attribute name="src">-->
    <!--<xsl:value-of select="$context/mets:fileSec/mets:fileGrp[@USE='THUMBNAIL']/-->
    <!--mets:file[@GROUPID=current()/@GROUPID]/mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>-->
    <!--</xsl:attribute>-->
    <!--</img>-->
    <!--</xsl:when>-->
    <!--<xsl:otherwise>-->
    <!--<img alt="Icon" src="{concat($theme-path, '/images/mime.png')}" style="height: {$thumbnail.maxheight}px;"/>-->
    <!--</xsl:otherwise>-->
    <!--</xsl:choose>-->
    <!--</a>-->
    <!--</div>-->
    <!--<div class="file-metadata" style="height: {$thumbnail.maxheight}px;">-->
    <!--<div>-->
    <!--<span class="bold">-->
    <!--<i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-name</i18n:text>-->
    <!--<xsl:text>:</xsl:text>-->
    <!--</span>-->
    <!--<span>-->
    <!--<xsl:attribute name="title"><xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:title"/></xsl:attribute>-->
    <!--<xsl:value-of select="util:shortenString(mets:FLocat[@LOCTYPE='URL']/@xlink:title, 17, 5)"/>-->
    <!--</span>-->
    <!--</div>-->
    <!--&lt;!&ndash; File size always comes in bytes and thus needs conversion &ndash;&gt;-->
    <!--<div>-->
    <!--<span class="bold">-->
    <!--<i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-size</i18n:text>-->
    <!--<xsl:text>:</xsl:text>-->
    <!--</span>-->
    <!--<span>-->
    <!--<xsl:choose>-->
    <!--<xsl:when test="@SIZE &lt; 1024">-->
    <!--<xsl:value-of select="@SIZE"/>-->
    <!--<i18n:text>xmlui.dri2xhtml.METS-1.0.size-bytes</i18n:text>-->
    <!--</xsl:when>-->
    <!--<xsl:when test="@SIZE &lt; 1024 * 1024">-->
    <!--<xsl:value-of select="substring(string(@SIZE div 1024),1,5)"/>-->
    <!--<i18n:text>xmlui.dri2xhtml.METS-1.0.size-kilobytes</i18n:text>-->
    <!--</xsl:when>-->
    <!--<xsl:when test="@SIZE &lt; 1024 * 1024 * 1024">-->
    <!--<xsl:value-of select="substring(string(@SIZE div (1024 * 1024)),1,5)"/>-->
    <!--<i18n:text>xmlui.dri2xhtml.METS-1.0.size-megabytes</i18n:text>-->
    <!--</xsl:when>-->
    <!--<xsl:otherwise>-->
    <!--<xsl:value-of select="substring(string(@SIZE div (1024 * 1024 * 1024)),1,5)"/>-->
    <!--<i18n:text>xmlui.dri2xhtml.METS-1.0.size-gigabytes</i18n:text>-->
    <!--</xsl:otherwise>-->
    <!--</xsl:choose>-->
    <!--</span>-->
    <!--</div>-->
    <!--&lt;!&ndash; Lookup File Type description in local messages.xml based on MIME Type.-->
    <!--In the original DSpace, this would get resolved to an application via-->
    <!--the Bitstream Registry, but we are constrained by the capabilities of METS-->
    <!--and can't really pass that info through. &ndash;&gt;-->
    <!--<div>-->
    <!--<span class="bold">-->
    <!--<i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-format</i18n:text>-->
    <!--<xsl:text>:</xsl:text>-->
    <!--</span>-->
    <!--<span>-->
    <!--<xsl:call-template name="getFileTypeDesc">-->
    <!--<xsl:with-param name="mimetype">-->
    <!--<xsl:value-of select="substring-before(@MIMETYPE,'/')"/>-->
    <!--<xsl:text>/</xsl:text>-->
    <!--<xsl:value-of select="substring-after(@MIMETYPE,'/')"/>-->
    <!--</xsl:with-param>-->
    <!--</xsl:call-template>-->
    <!--</span>-->
    <!--</div>-->
    <!--&lt;!&ndash;&ndash;&gt;-->
    <!--&lt;!&ndash; Display the contents of 'Description' only if bitstream contains a description &ndash;&gt;-->
    <!--<xsl:if test="mets:FLocat[@LOCTYPE='URL']/@xlink:label != ''">-->
    <!--<div>-->
    <!--<span class="bold">-->
    <!--<i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-description</i18n:text>-->
    <!--<xsl:text>:</xsl:text>-->
    <!--</span>-->
    <!--<span>-->
    <!--<xsl:attribute name="title"><xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:label"/></xsl:attribute>-->
    <!--&lt;!&ndash;<xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:label"/>&ndash;&gt;-->
    <!--<xsl:value-of select="util:shortenString(mets:FLocat[@LOCTYPE='URL']/@xlink:label, 17, 5)"/>-->
    <!--</span>-->
    <!--</div>-->
    <!--</xsl:if>-->
    <!--</div>-->
    <!--<div class="file-link" style="height: {$thumbnail.maxheight}px;">-->
    <!--<xsl:choose>-->
    <!--<xsl:when test="@ADMID">-->
    <!--<xsl:call-template name="display-rights"/>-->
    <!--</xsl:when>-->
    <!--<xsl:otherwise>-->
    <!--<xsl:call-template name="view-open"/>-->
    <!--</xsl:otherwise>-->
    <!--</xsl:choose>-->
    <!--</div>-->
    <!--</div>-->
    <!--</xsl:template>-->

    <xsl:template name="view-open">
        <a>
            <xsl:attribute name="href">
                <xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
            </xsl:attribute>
            <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-viewOpen</i18n:text>
        </a>
    </xsl:template>

    <xsl:template name="display-rights">
        <xsl:variable name="file_id" select="jstring:replaceAll(jstring:replaceAll(string(@ADMID), '_METSRIGHTS', ''), 'rightsMD_', '')"/>
        <xsl:variable name="rights_declaration" select="../../../mets:amdSec/mets:rightsMD[@ID = concat('rightsMD_', $file_id, '_METSRIGHTS')]/mets:mdWrap/mets:xmlData/rights:RightsDeclarationMD"/>
        <xsl:variable name="rights_context" select="$rights_declaration/rights:Context"/>
        <xsl:variable name="users">
            <xsl:for-each select="$rights_declaration/*">
                <xsl:value-of select="rights:UserName"/>
                <xsl:choose>
                    <xsl:when test="rights:UserName/@USERTYPE = 'GROUP'">
                        <xsl:text> (group)</xsl:text>
                    </xsl:when>
                    <xsl:when test="rights:UserName/@USERTYPE = 'INDIVIDUAL'">
                        <xsl:text> (individual)</xsl:text>
                    </xsl:when>
                </xsl:choose>
                <xsl:if test="position() != last()">, </xsl:if>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="alt-text"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-access-rights</i18n:text> <xsl:value-of select="$users"/></xsl:variable>

        <xsl:choose>
            <xsl:when test="(not ($rights_context/@CONTEXTCLASS = 'GENERAL PUBLIC') and ($rights_context/rights:Permissions/@DISPLAY = 'true')) or not ($rights_context)">
                <a href="{mets:FLocat[@LOCTYPE='URL']/@xlink:href}">
                    <img width="64" height="64" src="{concat($theme-path,'/images/Crystal_Clear_action_lock3_64px.png')}">
                        <xsl:attribute name="title"><xsl:value-of select="$alt-text"/></xsl:attribute>
                        <xsl:attribute name="alt"><xsl:value-of select="$alt-text"/></xsl:attribute>
                    </img>
                    <!-- icon source: http://commons.wikimedia.org/wiki/File:Crystal_Clear_action_lock3.png -->
                </a>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="view-open"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name='impact-altmetric'>
        <div id='impact-altmetric'>
            <!-- Altmetric.com -->
            <script type="text/javascript" src="{concat($scheme, 'd1bxh8uas1mnw7.cloudfront.net/assets/embed.js')}">&#xFEFF;
            </script>
            <div id='altmetric'
                 class='altmetric-embed'>
                <xsl:variable name='badge_type' select='confman:getProperty("altmetrics", "altmetric.badgeType")'/>
                <xsl:if test='boolean($badge_type)'>
                    <xsl:attribute name='data-badge-type'><xsl:value-of select='$badge_type'/></xsl:attribute>
                </xsl:if>

                <xsl:variable name='badge_popover' select='confman:getProperty("altmetrics", "altmetric.popover")'/>
                <xsl:if test='$badge_popover'>
                    <xsl:attribute name='data-badge-popover'><xsl:value-of select='$badge_popover'/></xsl:attribute>
                </xsl:if>

                <xsl:variable name='badge_details' select='confman:getProperty("altmetrics", "altmetric.details")'/>
                <xsl:if test='$badge_details'>
                    <xsl:attribute name='data-badge-details'><xsl:value-of select='$badge_details'/></xsl:attribute>
                </xsl:if>

                <xsl:variable name='no_score' select='confman:getProperty("altmetrics", "altmetric.noScore")'/>
                <xsl:if test='$no_score'>
                    <xsl:attribute name='data-no-score'><xsl:value-of select='$no_score'/></xsl:attribute>
                </xsl:if>

                <xsl:if test='confman:getProperty("altmetrics", "altmetric.hideNoMentions")'>
                    <xsl:attribute name='data-hide-no-mentions'>true</xsl:attribute>
                </xsl:if>

                <xsl:variable name='link_target' select='confman:getProperty("altmetrics", "altmetric.linkTarget")'/>
                <xsl:if test='$link_target'>
                    <xsl:attribute name='data-link-target'><xsl:value-of select='$link_target'/></xsl:attribute>
                </xsl:if>

                <xsl:choose>    <!-- data-doi data-handle data-arxiv-id data-pmid -->
                    <xsl:when test='$identifier_doi'>
                        <xsl:attribute name='data-doi'><xsl:value-of select='$identifier_doi'/></xsl:attribute>
                    </xsl:when>
                    <xsl:when test='$identifier_handle'>
                        <xsl:attribute name='data-handle'><xsl:value-of select='$identifier_handle'/></xsl:attribute>
                    </xsl:when>
                </xsl:choose>
                &#xFEFF;
            </div>
        </div>
    </xsl:template>

    <xsl:template name="impact-plumx">
        <div id="impact-plumx" style="clear:right">
            <!-- PlumX <http://plu.mx> -->
            <xsl:variable name="plumx_type" select="confman:getProperty('altmetrics', 'plumx.widget-type')"/>
            <xsl:variable name="plumx-script-url">
                <xsl:choose>
                    <xsl:when test="boolean($plumx_type)">
                        <xsl:value-of select="concat($scheme, 'd39af2mgp1pqhg.cloudfront.net/widget-', $plumx_type, '.js')"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat($scheme, 'd39af2mgp1pqhg.cloudfront.net/widget-popup.js')"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <script type="text/javascript" src="{$plumx-script-url}">&#xFEFF;
            </script>

            <xsl:variable name="plumx-class">
                <xsl:choose>
                    <xsl:when test="boolean($plumx_type) and ($plumx_type != 'popup')">
                        <xsl:value-of select="concat('plumx-', $plumx_type)"/>
                    </xsl:when>
                    <xsl:otherwise>plumx-plum-print-popup</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <a>
                <xsl:attribute name="id">plumx</xsl:attribute>
                <xsl:attribute name="class"><xsl:value-of select="$plumx-class"/></xsl:attribute>
                <xsl:attribute name="href">https://plu.mx/pitt/a/?doi=<xsl:value-of select="$identifier_doi"/></xsl:attribute>

                <xsl:variable name="plumx_data-popup" select="confman:getProperty('altmetrics', 'plumx.data-popup')"/>
                <xsl:if test="$plumx_data-popup">
                    <xsl:attribute name="data-popup"><xsl:value-of select="$plumx_data-popup"/></xsl:attribute>
                </xsl:if>

                <xsl:if test="confman:getProperty('altmetrics', 'plumx.data-hide-when-empty')">
                    <xsl:attribute name="data-hide-when-empty">true</xsl:attribute>
                </xsl:if>

                <xsl:if test="confman:getProperty('altmetrics', 'plumx.data-hide-print')">
                    <xsl:attribute name="data-hide-print">true</xsl:attribute>
                </xsl:if>

                <xsl:variable name="plumx_data-orientation" select="confman:getProperty('altmetrics', 'plumx.data-orientation')"/>
                <xsl:if test="$plumx_data-orientation">
                    <xsl:attribute name="data-orientation"><xsl:value-of select="$plumx_data-orientation"/></xsl:attribute>
                </xsl:if>

                <xsl:variable name="plumx_data-width" select="confman:getProperty('altmetrics', 'plumx.data-width')"/>
                <xsl:if test="$plumx_data-width">
                    <xsl:attribute name="data-width"><xsl:value-of select="$plumx_data-width"/></xsl:attribute>
                </xsl:if>

                <xsl:if test="confman:getProperty('altmetrics', 'plumx.data-border')">
                    <xsl:attribute name="data-border">true</xsl:attribute>
                </xsl:if>
                &#xFEFF;
            </a>

        </div>
    </xsl:template>


    <!-- For IDEALS, also add a Scopus citation count.  This functionality is modeled on the JSPUI equivalent:
         <http://blog.stuartlewis.com/2009/10/30/displaying-citation-counts-in-dspace/>  -->
    <xsl:template name="DisplayScopusCitationCount" mode="itemSummaryView-DIM">
        <xsl:variable name="doi" select="//dim:field[@mdschema='dc' and @element='identifier' and @qualifier='doi']"/>

        <!-- Only include if we have found a DOI -->
        <!--<xsl:if test="$doi">-->
            <!--<div id="scopus_citedbybox">-->
                <!--<span class="bold">-->
                    <!--<xsl:text>This item has been cited </xsl:text>-->
                    <!--<a id="scopus_citedbycount">-->
                        <!--<xsl:attribute name="doi">-->
                            <!--<xsl:value-of select="$doi"/>-->
                        <!--</xsl:attribute>-->
                        <!--<xsl:text>...</xsl:text>-->
                    <!--</a>-->
                    <!--times-->
                <!--</span>-->
                <!--(data provided by <a href="http://www.scopus.com">Scopus</a>)-->
            <!--</div>-->
            <!--&lt;!&ndash; <div id="scapiDebugArea">Debug:</div> &ndash;&gt;-->
        <!--</xsl:if>-->
    </xsl:template>

</xsl:stylesheet>
