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

    <!-- center checkbox for granting license -->
    <xsl:template
            match="xhtml:fieldset[@id='aspect_submission_StepTransformer_list_submit-review']//xhtml:div[@class='ds-form-content']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:value-of select="@class"/>
                <xsl:text> col-sm-offset-4</xsl:text>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- remove this label for the distribution license -->
    <xsl:template match="xhtml:label[@for='aspect_submission_StepTransformer_field_decision']">
    </xsl:template>


</xsl:stylesheet>
