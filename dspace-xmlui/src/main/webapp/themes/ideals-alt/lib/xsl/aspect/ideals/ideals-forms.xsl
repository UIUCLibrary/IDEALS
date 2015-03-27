<?xml version="1.0" encoding="UTF-8"?>

<!--
    ideals-forms.xsl

    Version: $Revision: 1.7 $

    Date: $Date: 2006/07/27 22:54:52 $

    Copyright (c) 2008, University of Illinois at Urbana-Champaign.
    All rights reserved.
-->

<!--
  This is the Forms XSL for the IDEALS Theme.  Mostly, it overrides
	the default DRI2XHTML theme's structural.xsl

	It handles customizations for the following:
    * IDEALS Submission Process Forms
	  * Other IDEALS Forms (e.g. in Administrative areas)
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
                exclude-result-prefixes="mets xlink xsl dim xhtml mods dc">

  <xsl:output indent="yes"/>

    <!--
        Theme path represents the full path back to theme. This is useful for
        accessing static resources such as images or javascript files. Simply
        prepend this variable and then add the file name, thus
        {$theme-path}/images/myimage.jpg will result in the full path from the
        HTTP root down to myimage.jpg. The theme path is composed of the
        "[context-path]/themes/[theme-dir]/".
    -->
    <xsl:variable name="theme-path" select="concat($context-path,'/themes/',/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path'])"/>



    <!-- ##################################################-->
  <!-- ########### Submission Form Help Link ############-->
  <!-- ##################################################-->

  <!-- Select A Collection Page: Add Help icon & link to Help -->
  <xsl:template match="dri:list[@id='aspect.submission.submit.SelectCollectionStep.list.select-collection']/dri:head">
        <legend>
        	<xsl:apply-templates />
            <span class="fieldset-help label">
                <a onclick="window.open(this.href); return false;" onkeypress="window.open(this.href); return false;">
                    <xsl:attribute name="href">
                        <xsl:text>https://wiki.cites.illinois.edu/wiki/display/IDEALS/Getting+Started+with+IDEALS#Submit_Select_a_Collection</xsl:text>
                    </xsl:attribute>
                    <img src="{$theme-path}/images/help.png" alt="Help" title="Help" />
                </a>
            </span>
        </legend>
  </xsl:template>

  <!-- Access Settings Page: Add Help icon & link to Help -->
  <xsl:template match="dri:list[@id='aspect.submission.StepTransformer.list.submit-access-settings']/dri:head">
        <legend>
        	<xsl:apply-templates />
            <span class="fieldset-help label">
                <a onclick="window.open(this.href); return false;" onkeypress="window.open(this.href); return false;">
                    <xsl:attribute name="href">
                        <xsl:text>https://wiki.cites.illinois.edu/wiki/display/IDEALS/Getting+Started+with+IDEALS</xsl:text>
                    </xsl:attribute>
                    <img src="{$theme-path}/images/help.png" alt="Help" title="Help" />
                </a>
            </span>
        </legend>
  </xsl:template>

  <!-- Describe Page: Add Help icon & link to Help -->
  <xsl:template match="dri:list[@id='aspect.submission.StepTransformer.list.submit-describe']/dri:head">
        <legend>
        	<xsl:apply-templates />
            <span class="fieldset-help label">
                <a onclick="window.open(this.href); return false;" onkeypress="window.open(this.href); return false;">
                    <xsl:attribute name="href">
                        <xsl:text>https://wiki.cites.illinois.edu/wiki/display/IDEALS/Getting+Started+with+IDEALS</xsl:text>
                    </xsl:attribute>
                    <img src="{$theme-path}/images/help.png" alt="Help" title="Help" />
                </a>
            </span>
        </legend>
  </xsl:template>

  <!-- Upload Page: Add Help icon & link to Help -->
  <xsl:template match="dri:list[@id='aspect.submission.StepTransformer.list.submit-upload-new']/dri:head">
        <legend>
        	<xsl:apply-templates />
            <span class="fieldset-help label">
                <a onclick="window.open(this.href); return false;" onkeypress="window.open(this.href); return false;">
                    <xsl:attribute name="href">
                        <xsl:text>https://wiki.cites.illinois.edu/wiki/display/IDEALS/Getting+Started+with+IDEALS</xsl:text>
                    </xsl:attribute>
                    <img src="{$theme-path}/images/help.png" alt="Help" title="Help" />
                </a>
            </span>
        </legend>
  </xsl:template>

  <!-- Review Page: Add Help icon & link to Help -->
  <xsl:template match="dri:list[@id='aspect.submission.StepTransformer.list.submit-review']/dri:head">
        <legend>
        	<xsl:apply-templates />
            <span class="fieldset-help label">
                <a onclick="window.open(this.href); return false;" onkeypress="window.open(this.href); return false;">
                    <xsl:attribute name="href">
                        <xsl:text>https://wiki.cites.illinois.edu/wiki/display/IDEALS/Getting+Started+with+IDEALS</xsl:text>
                    </xsl:attribute>
                    <img src="{$theme-path}/images/help.png" alt="Help" title="Help" />
                </a>
            </span>
        </legend>
  </xsl:template>

  <!-- ##################################################-->
  <!-- ########### Form Label customizations ############-->
  <!-- ##################################################-->

  <!-- Override the default "pick-label" to add (Required) after required fields.
       The original version of this template is available in structural.xsl -->
  <xsl:template name="pick-label">
    <xsl:choose>
      <xsl:when test="dri:field/dri:label">
        <label class="ds-form-label">
          <xsl:choose>
            <xsl:when test="./dri:field/@id">
              <xsl:attribute name="for">
                <xsl:value-of select="translate(./dri:field/@id,'.','_')"/>
              </xsl:attribute>
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
          </xsl:choose>
          <xsl:apply-templates select="dri:field/dri:label" mode="formComposite"/>
          <xsl:text>:</xsl:text>
          <xsl:if test="ancestor::dri:body[@id='aspect.submission.StepTransformer.div.submit-describe']">
            <xsl:if test="./dri:field/@required">
              <span class="required"><xsl:text> (Required)</xsl:text></span>
            </xsl:if>
          </xsl:if>
        </label>
      </xsl:when>
      <xsl:when test="string-length(string(preceding-sibling::*[1][local-name()='label'])) > 0">
        <xsl:choose>
          <xsl:when test="./dri:field/@id">
            <label>
              <xsl:apply-templates select="preceding-sibling::*[1][local-name()='label']"/>
              <xsl:text>:</xsl:text>
            </label>
          </xsl:when>
          <xsl:otherwise>
            <span>
              <xsl:apply-templates select="preceding-sibling::*[1][local-name()='label']"/>
              <xsl:text>:</xsl:text>
            </span>
          </xsl:otherwise>
        </xsl:choose>

      </xsl:when>
      <xsl:when test="dri:field">
        <xsl:choose>
          <xsl:when test="preceding-sibling::*[1][local-name()='label']">
            <label class="ds-form-label">
              <xsl:choose>
                <xsl:when test="./dri:field/@id">
                  <xsl:attribute name="for">
                    <xsl:value-of select="translate(./dri:field/@id,'.','_')"/>
                  </xsl:attribute>
                </xsl:when>
                <xsl:otherwise></xsl:otherwise>
              </xsl:choose>
              <xsl:apply-templates select="preceding-sibling::*[1][local-name()='label']"/>&#160;
            </label>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="preceding-sibling::*[1][local-name()='label']"/>&#160;
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <!-- If the label is empty and the item contains no field, omit the label. This is to
          make the text inside the item (since what else but text can be there?) stretch across
          both columns of the list. -->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- ##################################################-->
  <!-- ########### Form Field customizations ############-->
  <!-- ##################################################-->
  <!-- Override the default "textArea" templates to change the default size of <textarea> fields
       The original version of this template is available in structural.xsl -->
  <!-- templates for required textarea attributes used if not found in DRI document -->
  <xsl:template name="textAreaCols">
    <xsl:attribute name="cols">45</xsl:attribute>
  </xsl:template>
  <xsl:template name="textAreaRows">
    <xsl:attribute name="rows">4</xsl:attribute>
  </xsl:template>


  <!-- Display Type field in two columns -->
  <xsl:template match="dri:field[@id='aspect.submission.StepTransformer.field.dc_type' and @type='checkbox']">
    <xsl:apply-templates select="." mode="two-column"/>
  </xsl:template>
  <!-- Display Genre field in two columns -->
  <xsl:template match="dri:field[@id='aspect.submission.StepTransformer.field.dc_type_genre' and @type='checkbox']">
    <xsl:apply-templates select="." mode="two-column"/>
  </xsl:template>

  <!-- Custom template for checkbox/radiobutton fields which will display them in
       two columns. Template must be called specifically with mode="two-column". 
       NOTE: Some of this logic was copied from the "dri:field" (mode="normalField")
       template in structural.xsl-->
  <xsl:template match="dri:field[@type='checkbox' or @type= 'radio']" mode="two-column">
  
      <!--Get a count of the total number of checkboxes/radiobuttons we will display-->
      <xsl:variable name="total-options" select="count(./dri:option)"/>

      <!--Calculate first half of total options:
          If there is an odd number of options, the left column should be larger.-->
      <xsl:variable name="first-half" select="($total-options div 2) + ($total-options mod 2)"/>

      <!--Create a fieldset to group together all possible values for this field-->
      <fieldset>
          <xsl:call-template name="standardAttributes">
            <xsl:with-param name="class">
                <xsl:text>ds-</xsl:text><xsl:value-of select="@type"/><xsl:text>-field </xsl:text>
                <xsl:if test="dri:error">
                    <xsl:text>error </xsl:text>
                </xsl:if>
            </xsl:with-param>
          </xsl:call-template>
          <xsl:attribute name="id"><xsl:value-of select="generate-id()"/></xsl:attribute>
          <xsl:if test="dri:label">
            <legend><xsl:apply-templates select="dri:label" mode="compositeComponent" /></legend>
          </xsl:if>

          <!--Split values into a left and right column. Fill out the left column
              with the first half of the values.  The right column gets the second half. -->
          <div class="right-column">
            <xsl:apply-templates select="./dri:option[position() &gt; $first-half]"/>
          </div>
          <div class="left-column">
            <xsl:apply-templates select="./dri:option[position() &lt;= $first-half]"/>
          </div>
          <xsl:apply-templates select="dri:help" mode="help"/>
          <xsl:apply-templates select="dri:error" mode="error">
             <xsl:with-param name="field_id" select="translate(@id,'.','_')"/>
          </xsl:apply-templates>
      </fieldset>


  </xsl:template>


  <!-- Override Composite form fields (i.e. ones with multiple text boxes or a selectbox & textbox),
       in order to add a remove button next to each previously entered value.
       This override is ONLY for the Submission process (hence "primary submission")
       The original version of this template is available in structural.xsl -->
  <xsl:template match="dri:div[@rend='primary submission']//dri:field[@type='composite'][dri:field/dri:instance | dri:params/@operations]" mode="formComposite" priority="2">
      <div class="ds-form-content">
          <xsl:apply-templates select="dri:field" mode="compositeComponent"/>
          <xsl:if test="contains(dri:params/@operations,'add')">
              <!-- Add buttons should be named "submit_[field]_add" so that we can ignore errors from required fields when simply adding new values-->
              <input type="submit" value="add more" name="{concat('submit_',@n,'_add')}" class="ds-button-field ds-add-button btn btn-primary" />
          </xsl:if>
          <div class="spacer">&#160;</div>
          <xsl:apply-templates select="dri:field/dri:error" mode="compositeComponent"/>
          <xsl:apply-templates select="dri:error" mode="compositeComponent"/>
          <xsl:apply-templates select="dri:help" mode="compositeComponent"/>
          <xsl:if test="dri:instance or dri:field/dri:instance">
              <div class="ds-previous-values">
                  <xsl:call-template name="fieldIteratorWithDelete">
                      <xsl:with-param name="position">1</xsl:with-param>
                  </xsl:call-template>
                  <xsl:for-each select="dri:field">
                      <xsl:apply-templates select="dri:instance" mode="hiddenInterpreter"/>
                  </xsl:for-each>
              </div>
          </xsl:if>
      </div>
  </xsl:template>

  <!-- Override multi-valued form fields, in order
       to add a remove button next to each previously entered value.
       This override is ONLY for the Submission process (hence "primary submission")
       The original version of this template is available in structural.xsl -->
  <!-- This template is fornon-composite fields (i.e. single textbox fields)-->
  <xsl:template match="dri:div[@rend='primary submission']//dri:field[dri:field/dri:instance | dri:params/@operations]" priority="2">
      <!-- Create the first field normally -->
      <xsl:apply-templates select="." mode="normalField"/>
      <!-- Follow it up with an ADD button if the add operation is specified. This allows
          entering more than one value for this field. -->
      <xsl:if test="contains(dri:params/@operations,'add')">
          <!-- Add buttons should be named "submit_[field]_add" so that we can ignore errors from required fields when simply adding new values-->
          <input type="submit" value="add more" name="{concat('submit_',@n,'_add')}" class="ds-button-field ds-add-button btn btn-primary" />
      </xsl:if>
      <br/>
      <xsl:apply-templates select="dri:help" mode="help"/>
      <xsl:apply-templates select="dri:error" mode="error">
         <xsl:with-param name="field_id" select="translate(@id,'.','_')"/>
      </xsl:apply-templates>
      <xsl:if test="dri:instance">
          <div class="ds-previous-values">
              <!-- Iterate over the dri:instance elements contained in this field. The instances contain
                  stored values as either "interpreted", "raw", or "default" values. -->
              <xsl:call-template name="simpleFieldIteratorWithDelete">
                  <xsl:with-param name="position">1</xsl:with-param>
              </xsl:call-template>
          </div>
      </xsl:if>
  </xsl:template>


  <!-- Iterates through previous field values, displaying a delete icon next to each value.
       Based on usability testing, we've decided the multiple-remove functionality is a bit too confusing.
       This template is only called for single textboxes.  The below 'fieldDeleteIterator' template
       is for composite fields (multiple textboxes or selectbox + textbox in same field). -->
  <!-- Based on the 'simpleFieldIterator' template available in structural.xsl -->
  <xsl:template name="simpleFieldIteratorWithDelete">
      <xsl:param name="position"/>
      <xsl:if test="dri:instance[position()=$position]">
        <div class="previous-value">
          <!-- Add a hidden field which can be converted into an editable field. -->
          <input type="hidden" class="ds-text-field ds-previous-value-field">
            <xsl:attribute name="name"><xsl:value-of select="concat(@n,'_',$position)"/></xsl:attribute>
            <xsl:attribute name="value">
              <xsl:value-of select="dri:instance[position()=$position]/dri:value[@type='raw']"/>
            </xsl:attribute>
          </input>
          <xsl:apply-templates select="dri:instance[position()=$position]" mode="interpreted"/>
          <!-- Add a delete button which looks a trashcan -->
          <xsl:if test="contains(dri:params/@operations,'delete') and dri:instance">
            <!-- Delete buttons should be named "submit_[field]_remove_[index]" so that we can ignore errors from required fields when simply removing values-->
            <input type="submit" value="" name="{concat('submit_',@n,'_remove_',$position - 1)}" class="ds-button-field ds-delete-button" >
               <xsl:choose> <!--Figure out an appropriate 'title' for hover over text-->
                 <xsl:when test="dri:instance[position()=$position]/dri:value/text()">
                    <xsl:attribute name="title"><xsl:value-of select="concat('Remove: ', dri:instance[position()=$position]/dri:value)"/></xsl:attribute>
                 </xsl:when>
                 <xsl:otherwise>
                    <xsl:attribute name="title">Remove</xsl:attribute>
                 </xsl:otherwise>
               </xsl:choose>
            </input>
          </xsl:if>
          <!-- Add an edit button (pencil icon) -->
          <xsl:if test="contains(dri:params/@operations,'edit') and dri:instance">
            <input type="button" value="" name="{concat('submit_',@n,'_edit_',$position - 1)}" class="ds-button-field ds-edit-button" onclick="editPreviousValue(this)">
               <xsl:choose> <!--Figure out an appropriate 'title' for hover over text-->
                 <xsl:when test="dri:instance[position()=$position]/dri:value/text()">
                    <xsl:attribute name="title"><xsl:value-of select="concat('Edit: ', dri:instance[position()=$position]/dri:value)"/></xsl:attribute>
                 </xsl:when>
                 <xsl:otherwise>
                    <xsl:attribute name="title">Edit</xsl:attribute>
                 </xsl:otherwise>
               </xsl:choose>
            </input>
          </xsl:if>
        </div>
        
        <xsl:call-template name="simpleFieldIteratorWithDelete">
            <xsl:with-param name="position"><xsl:value-of select="$position + 1"/></xsl:with-param>
        </xsl:call-template>
      </xsl:if>
  </xsl:template>


  <!-- Iterates through previous field values, displaying a delete icon next to each value.
       Based on usability testing, we've decided the multiple-remove functionality is a bit too confusing.
       This template is for composite fields (multiple textboxes or selectbox + textbox in same field). -->
  <!-- Based on the 'fieldIterator' template available in structural.xsl -->
  <xsl:template name="fieldIteratorWithDelete">
      <xsl:param name="position"/>
      <xsl:choose>
          <!-- First check to see if the composite itself has a non-empty instance value in that
              position. In that case there is no need to go into the individual fields. -->
          <xsl:when test="count(dri:instance[position()=$position]/*)">
              <div class="previous-value">
                <xsl:apply-templates select="dri:instance[position()=$position]" mode="interpreted"/>
                <!-- Add a delete button which looks a trashcan -->
                <xsl:if test="contains(dri:params/@operations,'delete') and dri:instance">
                  <!-- Delete buttons should be named "submit_[field]_remove_[index]" so that we can ignore errors from required fields when simply removing values-->
                  <input type="submit" value="" title="{concat('Remove: ', dri:instance[position()=$position]/dri:value)}" name="{concat('submit_',@n,'_remove_',$position - 1)}" class="ds-button-field ds-delete-button"  />
                </xsl:if>
              </div>
              <xsl:call-template name="fieldIteratorWithDelete">
                  <xsl:with-param name="position"><xsl:value-of select="$position + 1"/></xsl:with-param>
              </xsl:call-template>
          </xsl:when>
          <!-- Otherwise, build the string from the component fields -->
          <xsl:when test="dri:field/dri:instance[position()=$position]">
              <div class="previous-value">
                <xsl:apply-templates select="dri:field" mode="compositeField">
                  <xsl:with-param name="position" select="$position"/>
                </xsl:apply-templates>
                <!-- Add a delete button which looks a trashcan -->
                <xsl:if test="contains(dri:params/@operations,'delete') and dri:instance">
                 <!-- Delete buttons should be named "submit_[field]_remove_[index]" so that we can ignore errors from required fields when simply removing values-->
                  <input type="submit" value="" name="{concat('submit_',@n,'_remove_',$position - 1)}" class="ds-button-field ds-delete-button" >
                     <xsl:choose> <!--Figure out an appropriate 'title' for hover over text-->
                       <xsl:when test="dri:instance[position()=$position]/dri:value/text()">
                          <xsl:attribute name="title"><xsl:value-of select="concat('Remove: ', dri:instance[position()=$position]/dri:value)"/></xsl:attribute>
                       </xsl:when>
                       <xsl:otherwise>
                          <xsl:attribute name="title">Remove</xsl:attribute>
                       </xsl:otherwise>
                     </xsl:choose>
                  </input>
                </xsl:if>
              </div>
              <xsl:call-template name="fieldIteratorWithDelete">
                  <xsl:with-param name="position"><xsl:value-of select="$position + 1"/></xsl:with-param>
              </xsl:call-template>
          </xsl:when>
      </xsl:choose>
  </xsl:template>


  <!-- Override the main error message field to automatically focus the
       first field that errored out.  This template was changed so that
       it is called from dri:field template above with the ID of the field.-->
  <!-- The original version of this template is available in structural.xsl -->
  <xsl:template match="dri:error" mode="error">
      <xsl:param name="field_id"/>

      <!--This is the first error if no other previous fields also had a dri:error tag-->
      <xsl:variable name="first-error" select="not(ancestor::dri:item/preceding-sibling::dri:item/dri:field/dri:error)"/>

      <!--If this is the FIRST error in the form,
          write out Javascript to place focus on this field-->
      <xsl:if test="$first-error and $field_id">
         <script type="text/javascript">
           document.getElementById('<xsl:value-of select="$field_id"/>').focus();
         </script>
      </xsl:if>

      <span class="error">* <xsl:apply-templates/></span>
  </xsl:template>

  <!-- Custom template for Remove (ds-delete-button) or Edit (ds-edit-button) buttons.
       For these buttons, we will use icons.  So, we want to remove any value.
       NOTE: Some of this logic was copied from the "dri:field" (mode="normalField")
       template in structural.xsl-->
  <xsl:template match="dri:field[@type='button' and (@rend='ds-delete-button' or @rend='ds-edit-button')]">
      <input>
        <xsl:call-template name="fieldAttributes"/>
		    <xsl:attribute name="type">submit</xsl:attribute>
        <!--We want an empty value, since we will be styling this button as an icon-->
        <xsl:attribute name="value"></xsl:attribute>
        <xsl:if test="@rend='ds-delete-button'">
          <xsl:attribute name="title">Delete</xsl:attribute>
        </xsl:if>
        <xsl:if test="@rend='ds-edit-button'">
          <xsl:attribute name="title">Edit</xsl:attribute>
        </xsl:if>
		    <xsl:apply-templates />
		  </input>
  </xsl:template>


  <!-- Fix bug in dri:help template so that it ONLY creates a <span> if
       the dri:help tag has contents-->
  <!-- The original version of this template is available in structural.xsl -->
  <xsl:template match="dri:help" mode="help">
        <!--Only create the <span> if there is content in the <dri:help> node-->
        <xsl:if test="./text() or ./node()">
          <span class="field-help">
              <xsl:apply-templates />
          </span>
        </xsl:if>
  </xsl:template>


</xsl:stylesheet>
