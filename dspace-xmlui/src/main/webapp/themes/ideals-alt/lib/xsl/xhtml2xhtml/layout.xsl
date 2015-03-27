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

    <!--<xsl:output indent="yes"/>-->
    <xsl:output method="xml" version="1.0" encoding="utf-8"/>

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

    <!-- Set the left sidebar options column width -->
    <xsl:template match="xhtml:div[@id='ds-options-wrapper']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>col-xs-12 col-sm-3 collapse </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:attribute name="role">
                <xsl:text>navigation</xsl:text>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- Set the ds-body column width -->
    <xsl:template match="xhtml:div[@id='ds-body']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>col-xs-12 col-sm-9 </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- Put "row" class on the ds-trail <ul> -->
    <xsl:template match="xhtml:ul[@id='ds-trail']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- Change the order of the front-page main divs -->
    <xsl:template match="xhtml:div[@id='file_news_div_news']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="xhtml:h1"/>
            <div class="row">
                <div class="col-xs-12 col-sm-8">
                    <xsl:apply-templates select="xhtml:div[@id='ds-home']"/>
                    <xsl:apply-templates select="xhtml:div[@id='aspect_rochesterStatistics_StatisticsViewer_div_site-recent-submission']"/>
                </div>
                <div class="col-xs-12 col-sm-4">
                    <xsl:apply-templates select="xhtml:div[@id='aspect_rochesterStatistics_StatisticsViewer_div_top-download-list']"/>
                </div>

            </div>
        </xsl:copy>
    </xsl:template>


    <!-- Change the order of the community/collection main divs -->
    <xsl:template match="xhtml:div[@id='aspect_artifactbrowser_CommunityViewer_div_community-home' or
                                   @id='aspect_artifactbrowser_CollectionViewer_div_collection-home']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <div class="row">
                <div class="col-xs-12 col-sm-9">
                    <xsl:apply-templates select="xhtml:div[@id='aspect_artifactbrowser_CommunityViewer_div_community-view' or
                                                   @id='aspect_artifactbrowser_CollectionViewer_div_collection-view']"/>
                </div>
                <div class="col-xs-12 col-sm-3">
                    <xsl:apply-templates select="xhtml:div[@id='aspect_artifactbrowser_CommunityViewer_div_community-search-browse' or
                                                   @id='aspect_artifactbrowser_CollectionViewer_div_collection-search-browse']"/>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-12">
                    <xsl:apply-templates select="xhtml:div[@id='aspect_artifactbrowser_CommunityRecentSubmissions_div_community-recent-submission' or
                                                   @id='aspect_artifactbrowser_CollectionRecentSubmissions_div_collection-recent-submission']"/>
                </div>
            </div>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:div[@id='aspect_rochesterStatistics_StatisticsViewer_div_download-statistics']">
        <div class="row">
            <div class="col-xs-12 col-sm-12">
                <xsl:copy>
                    <xsl:copy-of select="@*"/>
                    <xsl:apply-templates/>
                </xsl:copy>
            </div>
        </div>
    </xsl:template>

    <!--put the ETD search *before* the description-->
    <xsl:template match="xhtml:div[@id='aspect_artifactbrowser_CollectionViewer_div_collection-view']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="xhtml:div[@id='edu_uiuc_dspace_filtersearch_FilterSearch_div_collection-filter-search']"/>
            <xsl:apply-templates select="*[not(self::xhtml:div[@id='edu_uiuc_dspace_filtersearch_FilterSearch_div_collection-filter-search'])]"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:div[@id='aspect_artifactbrowser_CommunityViewer_div_community-view']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="xhtml:div[@id='edu_uiuc_dspace_filtersearch_FilterSearch_div_community-filter-search']"/>
            <xsl:apply-templates select="*[not(self::xhtml:div[@id='edu_uiuc_dspace_filtersearch_FilterSearch_div_community-filter-search'])]"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:div[@id='edu_uiuc_dspace_filtersearch_FilterSearch_div_community-filter-search' or
                                   @id='edu_uiuc_dspace_filtersearch_FilterSearch_div_collection-filter-search']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>clearfix </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!--checkboxes-->
    <xsl:template match="xhtml:fieldset[@class='ds-checkbox-field' or @class='ds-checkbox-field error']//xhtml:label">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>checkbox inline </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!--radios-->
    <xsl:template match="xhtml:fieldset[@class='ds-radio-field' or @class='ds-radio-field error']//xhtml:label">
        <div class="radio">
            <xsl:copy>
                <xsl:copy-of select="@*"/>
                <xsl:apply-templates/>

            </xsl:copy>
        </div>
    </xsl:template>

    <xsl:template match="xhtml:fieldset[@class='ds-radio-field ideals_access_selection' or @class='ds-radio-field ideals_access_selection error']//xhtml:label">
        <xsl:choose>
            <xsl:when test="position() = last()">
                <div id="limited_group">
                    <div id="limited_radio" class="radio">
                        <xsl:copy>
                            <xsl:copy-of select="@*"/>
                            <xsl:apply-templates/>

                        </xsl:copy>
                    </div>
                    <div id="limited_checkbox" class="checkbox">
                        <xsl:apply-templates select="//xhtml:li[@id='aspect_administrative_item_RestrictItemForm_item_access-settings']//xhtml:fieldset[@class='ds-checkbox-field full_embargo_checkbox']/*"/>
                        <xsl:apply-templates select="//xhtml:li[@id='aspect_submission_StepTransformer_item_access-settings']//xhtml:fieldset[@class='ds-checkbox-field full_embargo_checkbox']/*"/>
                    </div>
                </div>
            </xsl:when>
            <xsl:otherwise>
                <div class="radio">
                    <xsl:copy>
                        <xsl:copy-of select="@*"/>
                        <xsl:apply-templates/>

                    </xsl:copy>
                </div>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <!--ancestor::xhtml:li[@id='aspect_submission_StepTransformer_item_access-settings']/-->
    <xsl:template match="xhtml:table[@id='aspect_submission_StepTransformer_table_submit-upload-summary']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>table </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>

    </xsl:template>


    <xsl:template match="xhtml:form">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="role">
                <xsl:text>form</xsl:text>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:form//xhtml:li[xhtml:label]">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <div class="form-group">
                <xsl:apply-templates/>
            </div>
        </xsl:copy>
    </xsl:template>
    <!--[starts-with(@id, 'aspect_submission_StepTransformer_field_thesis_')]-->
    <xsl:template match="xhtml:form//xhtml:input[@type='text']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>form-control </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:form//xhtml:textarea">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>form-control </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:form//xhtml:select">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>form-control </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:form[@id='aspect_submission_StepTransformer_div_submit-upload']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <!--<xsl:text>form-horizontal </xsl:text>-->
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:fieldset[@id='aspect_submission_StepTransformer_list_submit-edit-file']//xhtml:input[starts-with(./@id, 'aspect_submission_StepTransformer_field_restrict_until_date_')]">
        <div class="form-group">
            <xsl:variable name="restrictField" select="./@id"/>

            <xsl:if test="starts-with(./@id, 'aspect_submission_StepTransformer_field_restrict_until_date_')">
                <xhtml:label class="col-sm-1 control-label ds-form-label" for=" {$restrictField}">Lift Date</xhtml:label>

                <!--<xsl:attribute name="disabled">-->
                <!--<xsl:text>disabled </xsl:text>-->
                <!--</xsl:attribute>-->
                <div class="form-content col-sm-4">
                    <xsl:copy>
                        <xsl:copy-of select="@*"/>
                        <xsl:attribute name="class">
                            <xsl:text>form-control </xsl:text>
                            <xsl:value-of select="@class"/>
                        </xsl:attribute>
                        <xsl:attribute name="id">
                            <!--overwrite the id. The old id had a different value for every bitstream, making it hard to select with the highest precedence in CSS-->
                            <xsl:text>restriction-date</xsl:text>
                        </xsl:attribute>
                        <xsl:apply-templates/>
                    </xsl:copy>
                </div>

            </xsl:if>
        </div>
    </xsl:template>

    <xsl:template match="xhtml:fieldset[@id='aspect_submission_StepTransformer_list_submit-edit-file']//xhtml:input[starts-with(./@id, 'aspect_submission_StepTransformer_field_reason_')]">
        <div class="form-group">
            <xsl:variable name="restrictField" select="./@id"/>
            <xhtml:label class="col-sm-1 control-label ds-form-label" for="{$restrictField}">Reason</xhtml:label>


                <!--<xsl:attribute name="disabled">-->
                <!--<xsl:text>disabled </xsl:text>-->
                <!--</xsl:attribute>-->
            <div class="form-content col-sm-4">
                <xsl:copy>
                    <xsl:copy-of select="@*"/>
                    <xsl:attribute name="class">
                        <xsl:text>form-control </xsl:text>
                        <xsl:value-of select="@class"/>
                    </xsl:attribute>
                    <xsl:attribute name="id">
                    <!--overwrite the id. The old id had a different value for every bitstream, making it hard to select with the highest precedence in CSS-->
                        <xsl:text>restriction-reason</xsl:text>
                    </xsl:attribute>
                    <xsl:apply-templates/>
                </xsl:copy>
            </div>
        </div>

    </xsl:template>

    <xsl:template match="xhtml:fieldset[@id='aspect_submission_StepTransformer_list_submit-edit-file']//xhtml:select[starts-with(./@id, 'aspect_submission_StepTransformer_field_groups_')]">
        <div class="form-group">
            <xsl:variable name="restrictField" select="./@id"/>
                <div class="form-content col-sm-5">
                    <xsl:copy>
                        <xsl:copy-of select="@*"/>
                        <xsl:attribute name="class">
                            <xsl:text>form-control </xsl:text>
                            <xsl:value-of select="@class"/>
                        </xsl:attribute>
                        <xsl:apply-templates/>
                    </xsl:copy>
                </div>
        </div>

                <!--<xsl:attribute name="disabled">-->
                <!--<xsl:text>disabled </xsl:text>-->
                <!--</xsl:attribute>-->
    </xsl:template>

    <xsl:template match="xhtml:fieldset[@id='aspect_submission_StepTransformer_list_submit-describe']//xhtml:input[@type='text' and @name!='dc_date_issued_year' and @name!='dc_date_issued_day']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>col-md-6 form-control </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:form[@id='aspect_submission_StepTransformer_div_submit-upload']//xhtml:li[xhtml:label]">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <div class="form-group">
                <xsl:apply-templates/>
            </div>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:form[@id='aspect_submission_StepTransformer_div_submit-upload']//xhtml:label[not(xhtml:input)]">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>col-sm-3 control-label </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:form[@id='aspect_submission_StepTransformer_div_submit-upload']//xhtml:div[@class='ds-form-content']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:choose>
                    <xsl:when test="preceding-sibling::xhtml:label">
                        <xsl:text>col-sm-9 </xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>col-sm-12 </xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!--make all buttons bootstrap buttons-->
    <xsl:template match="xhtml:input[@class='ds-button-field']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>btn btn-default </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:input[@class='ds-button-field' and @type='submit']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>btn btn-primary </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!--<xsl:template match="xhtml:input[@class='ds-button-field' and @type='submit' and @name='submit_save']">-->
    <!--<xsl:copy>-->
    <!--<xsl:copy-of select="@*"/>-->
    <!--<xsl:attribute name="class">-->
    <!--<xsl:text>btn btn-success </xsl:text>-->
    <!--<xsl:value-of select="@class"/>-->
    <!--</xsl:attribute>-->
    <!--<xsl:apply-templates/>-->
    <!--</xsl:copy>-->
    <!--</xsl:template>-->

    <!--<xsl:template match="xhtml:input[@class='ds-button-field' and @type='submit' and @name='submit_cancel']">-->
    <!--<xsl:copy>-->
    <!--<xsl:copy-of select="@*"/>-->
    <!--<xsl:attribute name="class">-->
    <!--<xsl:text>btn btn-warning </xsl:text>-->
    <!--<xsl:value-of select="@class"/>-->
    <!--</xsl:attribute>-->
    <!--<xsl:apply-templates/>-->
    <!--</xsl:copy>-->
    <!--</xsl:template>-->

    <!--Choose a Login Method -->
    <xsl:template match="xhtml:ul[@id='aspect_eperson_LoginChooser_list_login-options']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>horizontal </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:ul[@id='aspect_eperson_LoginChooser_list_login-options']/xhtml:li[1]/xhtml:a">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>btn btn-lg btn-primary </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:ul[@id='aspect_eperson_LoginChooser_list_login-options']/xhtml:li[2]/xhtml:a">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>btn btn-lg btn-warning </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!--Login form-->
    <xsl:template match="xhtml:form[@id='aspect_eperson_PasswordLogin_div_login']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>form-horizontal </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:form[@id='aspect_eperson_PasswordLogin_div_login']//xhtml:li[xhtml:label]">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <div class="form-group">
                <xsl:apply-templates/>
            </div>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:form[@id='aspect_eperson_PasswordLogin_div_login']//xhtml:label">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>col-sm-3 control-label </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:form[@id='aspect_eperson_PasswordLogin_div_login']//xhtml:div[@class='ds-form-content']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>col-sm-9 </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!--Configurable Browse-->
    <xsl:template match="xhtml:form[@id='aspect_artifactbrowser_ConfigurableBrowse_div_browse-navigation']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>form-horizontal </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:form[@id='aspect_artifactbrowser_ConfigurableBrowse_div_browse-controls']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>form-horizontal </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:form[@id='aspect_artifactbrowser_ConfigurableBrowse_div_browse-navigation']/xhtml:p[xhtml:input/@type='text']">
        <div class="form-group ">
            <xsl:apply-templates select="xhtml:input[@type='text']" />
            <xsl:apply-templates select="xhtml:input[@type='submit']" />
        </div>
    </xsl:template>

    <xsl:template match="xhtml:ul[@id='aspect_artifactbrowser_ConfigurableBrowse_list_jump-list']">
        <div class="form-group ">
            <xsl:copy>
                <xsl:copy-of select="@*"/>
                <xsl:attribute name="class">
                    <xsl:text>pagination pagination-sm </xsl:text>
                    <xsl:value-of select="@class"/>
                </xsl:attribute>
                <xsl:apply-templates/>
            </xsl:copy>
        </div>
    </xsl:template>

    <xsl:template match="xhtml:ul[@class='pagination-links']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>pager </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xhtml:form[@id='aspect_artifactbrowser_ConfigurableBrowse_div_browse-controls']/xhtml:p[xhtml:select]">
        <div class="form-group">
            <xsl:apply-templates select="xhtml:select"/>
            <xsl:apply-templates select="xhtml:input[@type='submit']"/>
        </div>
    </xsl:template>


    <xsl:template match="xhtml:input[@id='aspect_artifactbrowser_ConfigurableBrowse_field_starts_with']">
        <label for="{@id}" class="col-sm-3 col-xs-6 control-label"><xsl:apply-templates select="preceding-sibling::i18n:text[1]"/></label>
        <div class="col-sm-4 col-xs-3 ">
            <xsl:copy>
                <xsl:copy-of select="@*"/>
                <xsl:attribute name="class">
                    <xsl:text>form-control </xsl:text>
                    <xsl:value-of select="@class"/>
                </xsl:attribute>
                <xsl:apply-templates/>
            </xsl:copy>
        </div>
    </xsl:template>

    <xsl:template match="xhtml:select[@id='aspect_artifactbrowser_ConfigurableBrowse_field_order']">
        <label for="{@id}" class="col-sm-1 hidden-xs control-label"><xsl:apply-templates select="preceding-sibling::i18n:text[1]"/></label>
        <div class="col-sm-3 col-xs-5 ">
            <xsl:copy>
                <xsl:copy-of select="@*"/>
                <xsl:attribute name="class">
                    <xsl:text>form-control </xsl:text>
                    <xsl:value-of select="@class"/>
                </xsl:attribute>
                <xsl:apply-templates/>
            </xsl:copy>
        </div>
    </xsl:template>

    <xsl:template match="xhtml:select[@id='aspect_artifactbrowser_ConfigurableBrowse_field_rpp']">
        <label for="{@id}" class="col-sm-1 hidden-xs control-label"><xsl:apply-templates select="preceding-sibling::i18n:text[1]"/></label>
        <div class="col-sm-2 col-xs-4 ">
            <xsl:copy>
                <xsl:copy-of select="@*"/>
                <xsl:attribute name="class">
                    <xsl:text>form-control </xsl:text>
                    <xsl:value-of select="@class"/>
                </xsl:attribute>
                <xsl:apply-templates/>
            </xsl:copy>
        </div>
    </xsl:template>



    <!--suppress these-->
    <xsl:template match="xhtml:select[@id='aspect_artifactbrowser_ConfigurableBrowse_field_sort_by']"></xsl:template>
    <xsl:template match="xhtml:select[@id='aspect_artifactbrowser_ConfigurableBrowse_field_month']"></xsl:template>
    <xsl:template match="xhtml:select[@id='aspect_artifactbrowser_ConfigurableBrowse_field_year']"></xsl:template>
    <xsl:template match="xhtml:p[xhtml:select/@id='aspect_artifactbrowser_ConfigurableBrowse_field_year']"></xsl:template>
    <xsl:template match="xhtml:fieldset[@class='ds-checkbox-field full_embargo_checkbox']"></xsl:template>

    <xsl:template match="xhtml:input[@type='submit']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="class">
                <xsl:text>btn btn-primary </xsl:text>
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>