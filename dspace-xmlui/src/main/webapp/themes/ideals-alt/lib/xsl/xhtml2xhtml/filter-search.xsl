<?xml version="1.0" encoding="UTF-8"?>
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

    <!--<xsl:template match="xhtml:form[contains(concat(' ',normalize-space(@class),' '),' filter-search ')]">-->
        <!--<h2 class="filter-search-title">-->
            <!--Search Theses and Dissertations-->
            <!--&lt;!&ndash;a onclick="javascript:toggleFilterSearch(); return false;"-->
               <!--href="#">Search Theses and Dissertations</a&ndash;&gt;-->
        <!--</h2>-->
        <!--<xsl:copy>-->
            <!--&lt;!&ndash;xsl:attribute name="style">display: none;</xsl:attribute&ndash;&gt;-->
            <!--&lt;!&ndash;<xsl:copy-of select="@*"/>&ndash;&gt;-->
            <!--<xsl:apply-templates/>-->
        <!--</xsl:copy>-->
    <!--</xsl:template>-->

    <!--<xsl:template match="xhtml:table[@id='edu_uiuc_dspace_filtersearch_FilterSearch_list_filter-search']">-->
        <!--<xsl:copy>-->
            <!--<xsl:copy-of select="@*"/>-->
            <!--<xsl:attribute name="class">-->
                <!--<xsl:text>table table-striped</xsl:text>-->
            <!--</xsl:attribute>-->
            <!--<xsl:apply-templates/>-->
        <!--</xsl:copy>-->
    <!--</xsl:template>-->

    <xsl:template match="xhtml:input[@id='edu_uiuc_dspace_filtersearch_FilterSearch_field_submit']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>btn btn-primary </xsl:text>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>


    <!--<xsl:template match="xhtml:form[contains(concat(' ',normalize-space(@class),' '),' filter-search ')]"></xsl:template>-->

    <!--<xsl:template match="xhtml:td/xhtml:span[@class='ds-gloss-list-label']">-->
        <!---->
    <!--</xsl:template>-->

    <!--&lt;!&ndash; Construct path for 'autocomplete' servlet and assign to variable.  &ndash;&gt;-->
    <!--&lt;!&ndash; NOTE: Since this stylesheet, filter-search.xsl, is being processed after &ndash;&gt;-->
    <!--&lt;!&ndash; the DRI document is transformed to XHTML, we do not have any access &ndash;&gt;-->
    <!--&lt;!&ndash; to the DRI document nor its meta section.  Therefore, the contextpath  &ndash;&gt;-->
    <!--&lt;!&ndash; value grabbed from //p[@class='contextpath'] was added to this XHTHL document  &ndash;&gt;-->
    <!--&lt;!&ndash; when the 'buildFooter' template is executed in ideals-structure.xsl  &ndash;&gt;-->
    <!--<xsl:variable name="servlet">-->
        <!--<xsl:value-of select = "concat(//xhtml:p[@class='contextpath'],'/autocomplete')" />-->
    <!--</xsl:variable>-->

    <!--&lt;!&ndash; Autocomplete Template for Department (in ETD Filter Search) &ndash;&gt;-->
    <!--<xsl:template match="xhtml:input[@xhtml:id='edu_uiuc_dspace_filtersearch_FilterSearch_field_etddepartment']">-->
        <!--<xsl:copy>-->
            <!--<xsl:copy-of select="@*"/>-->
            <!--<xsl:apply-templates/>-->
        <!--</xsl:copy>-->
        <!--&lt;!&ndash; add div to display returned autocomplete results &ndash;&gt;-->
        <!--<div id="autocomplete_department_choices" class="autocomplete">&#160;</div>-->
        <!--&lt;!&ndash; Get handle to identify community or collection.  &ndash;&gt;-->
        <!--<xsl:variable name="hdl">-->
            <!--<xsl:value-of select="//*[@id='ds-search-form-scope-container']/@value"/>-->
        <!--</xsl:variable>-->
        <!--<script type="text/javascript">-->
            <!--new Ajax.Autocompleter("edu_uiuc_dspace_filtersearch_FilterSearch_field_etddepartment",-->
            <!--"autocomplete_department_choices",-->
            <!--"<xsl:value-of select='$servlet' />",-->
            <!--{paramName: 'query', parameters: {type: 'department', handle: '<xsl:value-of select="$hdl" />'}, minChars: 2});-->
        <!--</script>-->
    <!--</xsl:template>-->

</xsl:stylesheet>
