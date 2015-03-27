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


    <!--#### HIDE COPYRIGHT TEXT FIELD FOR COLLECTIONS ####-->
    <!-- Suppress the Copyright Text field label on the "Edit/Create a Collection" pages -->
    <xsl:template match="dri:label[i18n:text/text()='xmlui.administrative.collection.EditCollectionMetadataForm.label_copyright_text']">
    </xsl:template>
    <!-- Suppress the Copyright Text field on the "Edit a Collection" page -->
    <xsl:template match="dri:item[dri:field/@id='aspect.administrative.collection.EditCollectionMetadataForm.field.copyright_text']">
    </xsl:template>
    <!-- Suppress the Copyright Text field on the "Create a Collection" page -->
    <xsl:template match="dri:item[dri:field/@id='aspect.administrative.collection.CreateCollectionForm.field.copyright_text']">
    </xsl:template>


    <!--#### HIDE NEWS (HTML) FIELD FOR COLLECTIONS ####-->
    <!-- Suppress the Sidebar News Text field label on the "Edit/Create a Collection" pages -->
    <xsl:template match="dri:label[i18n:text/text()='xmlui.administrative.collection.EditCollectionMetadataForm.label_side_bar_text']">
    </xsl:template>
    <!-- Suppress the Sidebar News Text field on the "Edit a Collection" page -->
    <xsl:template match="dri:item[dri:field/@id='aspect.administrative.collection.EditCollectionMetadataForm.field.side_bar_text']">
    </xsl:template>
    <!-- Suppress the Sidebar News Text field on the "Create a Collection" page -->
    <xsl:template match="dri:item[dri:field/@id='aspect.administrative.collection.CreateCollectionForm.field.side_bar_text']">
    </xsl:template>


    <!--#### HIDE LICENSE FIELD FOR COLLECTIONS ####-->
    <!-- Suppress the License Text field label on the "Edit/Create a Collection" pages -->
    <xsl:template match="dri:label[i18n:text/text()='xmlui.administrative.collection.EditCollectionMetadataForm.label_license']">
    </xsl:template>
    <!-- Suppress the License Text field on the "Edit a Collection" page -->
    <xsl:template match="dri:item[dri:field/@id='aspect.administrative.collection.EditCollectionMetadataForm.field.license']">
    </xsl:template>
    <!-- Suppress the License Text field on the "Create a Collection" page -->
    <xsl:template match="dri:item[dri:field/@id='aspect.administrative.collection.CreateCollectionForm.field.license']">
    </xsl:template>


    <!--#### HIDE COPYRIGHT TEXT FIELD FOR COMMUNITIES ####-->
    <!-- Suppress the Copyright Text field label on the "Edit/Create a Community" pages -->
    <xsl:template match="dri:label[i18n:text/text()='xmlui.administrative.community.EditCommunityMetadataForm.label_copyright_text']">
    </xsl:template>
    <!-- Suppress the Copyright Text field on the "Edit a Community" page -->
    <xsl:template match="dri:item[dri:field/@id='aspect.administrative.community.EditCommunityMetadataForm.field.copyright_text']">
    </xsl:template>
    <!-- Suppress the Copyright Text field on the "Create a Community" page -->
    <xsl:template match="dri:item[dri:field/@id='aspect.administrative.community.CreateCommunityForm.field.copyright_text']">
    </xsl:template>

    <!--#### HIDE NEWS (HTML) FIELD FOR COMMUNITIES ####-->
    <!-- Suppress the Sidebar News Text field label on the "Edit/Create a Community" pages -->
    <xsl:template match="dri:label[i18n:text/text()='xmlui.administrative.community.EditCommunityMetadataForm.label_side_bar_text']">
    </xsl:template>
    <!-- Suppress the Sidebar News Text field on the "Edit a Community" page -->
    <xsl:template match="dri:item[dri:field/@id='aspect.administrative.community.EditCommunityMetadataForm.field.side_bar_text']">
    </xsl:template>
    <!-- Suppress the Sidebar News Text field on the "Create a Community" page -->
    <xsl:template match="dri:item[dri:field/@id='aspect.administrative.community.CreateCommunityForm.field.side_bar_text']">
    </xsl:template>


</xsl:stylesheet>