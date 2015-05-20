<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->
<!--
    Main structure of the page, determines where
    header, footer, body, navigation are structurally rendered.
    Rendering of the header, footer, trail and alerts

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
        xmlns:confman="org.dspace.core.ConfigurationManager"
        xmlns="http://www.w3.org/1999/xhtml"
        exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc confman">

    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

    <!--
        Requested Page URI. Some functions may alter behavior of processing depending if URI matches a pattern.
        Specifically, adding a static page will need to override the DRI, to directly add content.
    -->
    <xsl:variable name="request-uri"
            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='URI']"/>

    <!--
        The starting point of any XSL processing is matching the root element. In DRI the root element is document,
        which contains a version attribute and three top level elements: body, options, meta (in that order).

        This template creates the html document, giving it a head and body. A title and the CSS style reference
        are placed in the html head, while the body is further split into several divs. The top-level div
        directly under html body is called "ds-main". It is further subdivided into:
        "ds-header"  - the header div containing title, subtitle, trail and other front matter
        "ds-body"    - the div containing all the content of the page; built from the contents of dri:body
        "ds-options" - the div with all the navigation and actions; built from the contents of dri:options
        "ds-footer"  - optional footer div, containing misc information

        The order in which the top level divisions appear may have some impact on the design of CSS and the
        final appearance of the DSpace page. While the layout of the DRI schema does favor the above div
        arrangement, nothing is preventing the designer from changing them around or adding new ones by
        overriding the dri:document template.
    -->
    <xsl:template match="dri:document">


        <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;
        </xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;!--[if lt IE 7]&gt; &lt;html class=&quot;no-js lt-ie9 lt-ie8 lt-ie7&quot; lang=&quot;en&quot;&gt; &lt;![endif]--&gt;
            &lt;!--[if IE 7]&gt;    &lt;html class=&quot;no-js lt-ie9 lt-ie8&quot; lang=&quot;en&quot;&gt; &lt;![endif]--&gt;
            &lt;!--[if IE 8]&gt;    &lt;html class=&quot;no-js lt-ie9&quot; lang=&quot;en&quot;&gt; &lt;![endif]--&gt;
            &lt;!--[if gt IE 8]&gt;&lt;!--&gt; &lt;html class=&quot;no-js&quot; lang=&quot;en&quot;&gt; &lt;!--&lt;![endif]--&gt;
        </xsl:text>

        <!-- First of all, build the HTML head element -->

        <xsl:call-template name="buildHead"/>

        <!-- Then proceed to the body -->
        <body>
            <!-- Prompt IE 6 users to install Chrome Frame. Remove this if you support IE 6.
                chromium.org/developers/how-tos/chrome-frame-getting-started -->
            <!--[if lt IE 7]><p class=chromeframe>Your browser is <em>ancient!</em> <a href="http://browsehappy.com/">Upgrade to a different browser</a> or <a href="http://www.google.com/chromeframe/?redirect=true">install Google Chrome Frame</a> to experience this site.</p><![endif]-->
            <xsl:choose>
                <xsl:when
                        test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='framing'][@qualifier='popup']">
                    <xsl:apply-templates select="dri:body/*"/>
                </xsl:when>
                <xsl:otherwise>
                    <div id="ds-main" role="document">
                        <!--The header div, complete with title, subtitle and other junk-->
                        <xsl:call-template name="buildHeader"/>

                        <!--The trail is built by applying a template over pageMeta's trail children. -->
                        <!--<xsl:call-template name="buildTrail"/>-->

                        <!--javascript-disabled warning, will be invisible if javascript is enabled-->
                        <div id="no-js-warning-wrapper" class="hidden">
                            <div id="no-js-warning" class="container">
                                <div class="notice failure">
                                    <xsl:text>JavaScript is disabled for your browser. Some features of this site may not work without it.</xsl:text>
                                </div>
                            </div>
                        </div>


                        <!--ds-content is a groups ds-body and the navigation together and used to put the clearfix on, center, etc.
                            ds-content-wrapper is necessary for IE6 to allow it to center the page content-->
                        <div id="ds-content-wrapper">
                            <div id="ds-content" class="container">
                                <div class="row">
                                    <!--
                                        Goes over the document tag's children elements: body, options, meta. The body template
                                        generates the ds-body div that contains all the content. The options template generates
                                        the ds-options div that contains the navigation and action options available to the
                                        user. The meta element is ignored since its contents are not processed directly, but
                                        instead referenced from the different points in the document.
                                    -->
                                    <xsl:apply-templates/>
                                </div>

                            </div>
                        </div>

                        <!--
                            The footer div, dropping whatever extra information is needed on the page. It will
                            most likely be something similar in structure to the currently given example. -->
                        <xsl:call-template name="buildFooter"/>

                    </div>

                </xsl:otherwise>
            </xsl:choose>
            <!-- Javascript at the bottom for fast page loading -->
            <xsl:call-template name="addJavascript"/>
        </body>
        <xsl:text disable-output-escaping="yes">&lt;/html&gt;</xsl:text>
    </xsl:template>

    <!-- The HTML head element contains references to CSS as well as embedded JavaScript code. Most of this
        information is either user-provided bits of post-processing (as in the case of the JavaScript), or
        references to stylesheets pulled directly from the pageMeta element. -->
    <xsl:template name="buildHead">
        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

            <!-- Always force latest IE rendering engine (even in intranet) & Chrome Frame -->
            <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>

            <!--  Mobile Viewport Fix
                j.mp/mobileviewport & davidbcalhoun.com/2010/viewport-metatag
                device-width : Occupy full width of the screen in its current orientation
                initial-scale = 1.0 retains dimensions instead of zooming out if page height > device height
                maximum-scale = 1.0 retains dimensions instead of zooming in if page width < device width
            -->
            <meta name="viewport"
                    content="width=device-width; initial-scale=1.0; maximum-scale=1.0;"/>

            <link rel="shortcut icon">
                <xsl:attribute name="href">
                    <xsl:value-of
                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                    <xsl:text>/themes/</xsl:text>
                    <xsl:value-of
                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                    <xsl:text>/images/favicon.ico</xsl:text>
                </xsl:attribute>
            </link>

            <link rel="apple-touch-icon">
                <xsl:attribute name="href">
                    <xsl:value-of
                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                    <xsl:text>/themes/</xsl:text>
                    <xsl:value-of
                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                    <xsl:text>/images/apple-touch-icon.png</xsl:text>
                </xsl:attribute>
            </link>

            <meta name="Generator">
                <xsl:attribute name="content">
                    <xsl:text>DSpace</xsl:text>
                    <xsl:if
                            test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='dspace'][@qualifier='version']">
                        <xsl:text> </xsl:text>
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='dspace'][@qualifier='version']"
                                />
                    </xsl:if>
                </xsl:attribute>
            </meta>

            <!-- Add stylsheets -->
            <xsl:for-each
                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='stylesheet']">
                <link rel="stylesheet" type="text/css">
                    <xsl:attribute name="media">
                        <xsl:value-of select="@qualifier"/>
                    </xsl:attribute>
                    <xsl:attribute name="href">
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                        <xsl:text>/themes/</xsl:text>
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                        <xsl:text>/</xsl:text>
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                </link>
            </xsl:for-each>

            <!-- Add syndication feeds -->
            <xsl:for-each select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='feed']">
                <link rel="alternate" type="application">
                    <xsl:attribute name="type">
                        <xsl:text>application/</xsl:text>
                        <xsl:value-of select="@qualifier"/>
                    </xsl:attribute>
                    <xsl:attribute name="href">
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                </link>
            </xsl:for-each>

            <!--  Add OpenSearch auto-discovery link -->
            <xsl:if
                    test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='opensearch'][@qualifier='shortName']">
                <link rel="search" type="application/opensearchdescription+xml">
                    <xsl:attribute name="href">
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='scheme']"/>
                        <xsl:text>://</xsl:text>
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='serverName']"/>
                        <xsl:text>:</xsl:text>
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='serverPort']"/>
                        <xsl:value-of select="$context-path"/>
                        <xsl:text>/</xsl:text>
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='opensearch'][@qualifier='autolink']"
                                />
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='opensearch'][@qualifier='shortName']"
                                />
                    </xsl:attribute>
                </link>
            </xsl:if>

            <script type="text/javascript" src="/themes/ideals/lib/js/jquery-1.10.2.min.js">&#160;</script>
            <!-- The following sets up some variables for the next javascript to use -->
            <script type="text/javascript">
                var page_structure_js_data = {
                default_textarea_value: '<i18n:text>xmlui.dri2xhtml.default.textarea.value</i18n:text>'
                };
            </script>

            <!-- The following javascript removes the default text of empty text areas when they are focused on or submitted -->
            <!-- There is also javascript to disable submitting a form when the 'enter' key is pressed. -->
            <script type="text/javascript" src="/themes/ideals/lib/js/page-structure.js">&#160;</script>

            <!-- Modernizr enables HTML5 elements & feature detects -->
            <script type="text/javascript">
                <xsl:attribute name="src">
                    <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                    <xsl:text>/themes/</xsl:text>
                    <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                    <xsl:text>/lib/js/modernizr-1.7.min.js</xsl:text>
                </xsl:attribute>&#160;</script>

            <!-- Modernizr enables HTML5 elements & feature detects -->
            <!--<script src="{concat($theme-path, '/vendor/modernizr/modernizr.js')}">&#160;</script>-->

            <!-- Add the title in -->
            <xsl:variable name="page_title"
                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='title']"/>
            <title>
                <!-- UIUC Modified: Always display "IDEALS @ Illinois" in title tag-->
                <xsl:text>IDEALS @ Illinois: </xsl:text>
                <xsl:choose>
                    <xsl:when test="not($page_title)">
                        <xsl:text>  </xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="$page_title/node()"/>
                    </xsl:otherwise>
                </xsl:choose>
            </title>

            <!-- Head metadata in item pages -->
            <xsl:if
                    test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='xhtml_head_item']">
                <xsl:value-of
                        select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='xhtml_head_item']"
                        disable-output-escaping="yes"/>
            </xsl:if>

            <!-- Add all Google Scholar Metadata values -->
            <xsl:for-each
                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[substring(@element, 1, 9) = 'citation_']">
                <meta name="{@element}" content="{.}"/>
            </xsl:for-each>

            <!-- Add MathJAX CDN, can do a local install, or possibly get SSL enabled-->
            <xsl:if
                    test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='render'][@qualifier='scientificFormulas'] = 'true'">
                <script type="text/x-mathjax-config">
                    MathJax.Hub.Config({
                        tex2jax: {
                            inlineMath: [['$','$'], ['\\(','\\)']],
                            ignoreClass: "detail-field-data"
                        },
                        TeX: {
                            Macros: {
                                AA: '{\\mathring A}'
                            }
                        }
                    });
                </script>

                <script type="text/javascript">
                    <xsl:attribute name="src">
                        <xsl:choose>
                            <xsl:when test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='scheme']='https'">
                                <xsl:text>https://cdn.mathjax.org</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>http://cdn.mathjax.org</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:text>/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML</xsl:text>
                    </xsl:attribute>
                    <xsl:text> </xsl:text>
                </script>
            </xsl:if>

        </head>
    </xsl:template>


    <!-- The header (distinct from the HTML head element) contains the title, subtitle, login box and various
        placeholders for header images -->
    <xsl:template name="buildHeader">

        <!-- IDEALS displays a different logo for the 34610 community -->
        <xsl:variable name="com_hdl"
                select="/dri:document/dri:meta/dri:pageMeta/dri:trail[2]/@target"/>
        <xsl:variable name="com" select="substring-after($com_hdl,'2142/')"/>
        <xsl:variable name="request_handle"
                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request' and @qualifier='URI']/text()"/>
        <xsl:variable name="request_subhandle" select="substring-after($request_handle, '2142/')"/>

        <!--header for small devices-->
        <div id="ds-header-sm" class="visible-xs container" role="banner">

            <!--IDEALS Home link-->
            <div id="header-logo">
                <a>
                    <xsl:attribute name="href">
                        <xsl:choose>
                            <xsl:when
                                    test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]/text()">
                                <xsl:value-of
                                        select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"
                                        />
                            </xsl:when>
                            <!--If no contextpath, send to "/" which will be the homepage of the site-->
                            <xsl:otherwise>
                                <xsl:text>/</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>

                    <img id="ds-header-logo-sm" src="{$theme-path}/images/IDEALS_logo_tablet.png"
                            alt="IDEALS Logo"/>
                </a>
            </div>


            <!-- User links - login button, menu and search form for mobile viewport -->
            <div id="user-links">
                <div id="ds-user-login-sm">
                    <xsl:choose>
                        <xsl:when test="/dri:document/dri:meta/dri:userMeta/@authenticated = 'yes'">
                            <a class="btn btn-sm btn-default blue-tooltip" data-toggle="tooltip"
                                    data-placement="top" title="My Profile">
                                <xsl:attribute name="href">
                                    <xsl:value-of
                                            select="/dri:document/dri:meta/dri:userMeta/dri:metadata[@element='identifier' and @qualifier='url']"
                                            />
                                </xsl:attribute>
                                <span class="glyphicon glyphicon-user"/>
                                <!--<i18n:text>xmlui.dri2xhtml.structural.profile</i18n:text>-->
                            </a>
                            <a class="btn btn-sm btn-danger blue-tooltip" data-toggle="tooltip"
                                    data-placement="top" title="Logout">
                                <xsl:attribute name="href">
                                    <xsl:value-of
                                            select="/dri:document/dri:meta/dri:userMeta/dri:metadata[@element='identifier' and @qualifier='logoutURL']"
                                            />
                                </xsl:attribute>
                                <span class="glyphicon glyphicon-log-out"/>
                                <!--<i18n:text>xmlui.dri2xhtml.structural.logout</i18n:text>-->
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <a class="btn btn-sm btn-default blue-tooltip" data-toggle="tooltip"
                                    data-placement="top" title="Log on">
                                <xsl:attribute name="href">
                                    <xsl:value-of
                                            select="/dri:document/dri:meta/dri:userMeta/dri:metadata[@element='identifier' and @qualifier='loginURL']"
                                            />
                                </xsl:attribute>
                                <span class="glyphicon glyphicon-log-in"/>
                                <!--<i18n:text>xmlui.dri2xhtml.structural.login</i18n:text>-->
                            </a>
                        </xsl:otherwise>
                    </xsl:choose>
                </div>


                <div class="container" id="toggled-menu">
                    <button class="collapsed btn btn-primary btn-sm" type="button"
                            data-toggle="collapse" data-target="#ds-options-wrapper">
                        <span class="glyphicon glyphicon-th-list"/>
                        <span class="sr-only">Toggle navigation</span>
                    </button>
                </div>

                <!-- Search form with dropdown menu.
                    Search scopes with radio button is included with the main div "ideals-search-scope" -->
                <div id="search-space-sm">
                    <form id="top_search_form_sm" accept-charset="UTF-8" action="/search"
                            class="form-inline" method="post">
                        <xsl:attribute name="action">
                            <xsl:value-of
                                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='simpleURL']"
                                    />
                        </xsl:attribute>
                        <div class="input-group">
                            <label class="sr-only" for="input-search-sm">
                                <xsl:value-of
                                        select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='queryField']"
                                        />
                            </label>
                            <input id="input-search-sm" class="form-control input-sm" type="text">
                                <xsl:attribute name="name">
                                    <xsl:value-of
                                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='queryField']"
                                            />
                                </xsl:attribute>
                                <xsl:attribute name="placeholder">Search</xsl:attribute>
                            </input>


                            <div class="input-group-btn">
                                <button class="btn btn-sm btn-default dropdown-toggle"
                                        data-toggle="dropdown" aria-expanded="false">
                                    <span class="caret"/>
                                    <span class="sr-only">Toggle Dropdown</span>
                                </button>

                                <ul class="dropdown-menu pull-right" role="menu">
                                    <li class="dropdown">
                                        <input class="btn btn-sm btn-default btn-block"
                                                name="submit" type="submit" i18n:attr="value"
                                                value="xmlui.general.go">
                                            <xsl:attribute name="onclick">
                                                <xsl:text>
                                                    var radio = document.getElementById(&quot;ds-search-form-scope-container-sm&quot;);
                                                    if (radio != undefined &amp;&amp; radio.checked)
                                                    {
                                                    var form = document.getElementById(&quot;top_search_form_sm&quot;);
                                                    form.action=
                                                </xsl:text>
                                                <xsl:text>&quot;</xsl:text>
                                                <xsl:value-of
                                                        select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath']"/>
                                                <xsl:text>/handle/&quot; + radio.value + &quot;/search&quot; ;</xsl:text>
                                                <xsl:text>
                                                    }
                                                </xsl:text>
                                            </xsl:attribute>
                                        </input>
                                    </li>

                                    <li>
                                        <a class="btn btn-sm btn-block btn-default">
                                            <xsl:attribute name="href">
                                                <xsl:value-of
                                                        select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='advancedURL']"
                                                        />
                                            </xsl:attribute>
                                            <i18n:text>xmlui.dri2xhtml.structural.search-advanced</i18n:text>
                                        </a>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>


        <!--hide the header on small devices-->
        <div id="ds-header-wrapper">
            <div id="ds-header" class="clearfix container" role="banner">

                <xsl:attribute name="class">
                    <xsl:choose>
                        <xsl:when test="$com='34610' or $request_subhandle='34610'">
                            <xsl:text>c2c</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>ideals</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:text>clearfix container</xsl:text>
                </xsl:attribute>

                <xsl:choose>
                    <xsl:when test="$com='34610' or $request_subhandle='34610'">
                        <img id="ds-header-logo" src="{$theme-path}/images/c2c-logo_606x80.gif"
                                alt="C2C Logo" usemap="#logomap"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <div class="row">
                            <div id="ds-image-group" class="col-sm-7 col-md-8 col-lg-8 hidden-xs">
                                <img id="ds-header-logo"
                                        src="{$theme-path}/images/IDEALS_logo_2x.png" alt="IDEALS Logo"
                                        usemap="#logomap"/>

                                <!--Image Map for IDEALS Logo-->
                                <map id="logomap" name="logomap">
                                    <!--IDEALS Home link-->
                                    <area shape="rect" coords="0,0,299,70" alt="IDEALS Home">
                                        <xsl:attribute name="href">
                                            <xsl:choose>
                                                <xsl:when
                                                        test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]/text()">
                                                    <xsl:value-of
                                                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"
                                                            />
                                                </xsl:when>
                                                <!--If no contextpath, send to "/" which will be the homepage of the site-->
                                                <xsl:otherwise>
                                                    <xsl:text>/</xsl:text>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:attribute>
                                    </area>
                                    <!--Block I link-->
                                    <area shape="rect" coords="299,10,335,56"
                                            href="http://illinois.edu/"
                                            alt="University of Illinois at Urbana-Champaign logo"/>
                                </map>

                                <div id="images-sm" class="hidden-sm">
                                    <img class="ds-header-campus-image"
                                            src="{$theme-path}/images/alma_scaled120x80.png"
                                            alt="The Alma Mater" title="The Alma Mater"/>
                                    <img class="ds-header-campus-image"
                                            src="{$theme-path}/images/barn_scaled53X80.png"
                                            alt="Round Barn" title="Round Barn"/>
                                </div>
                                <div id="images-md" class="hidden-md hidden-sm">
                                    <img class="ds-header-campus-image"
                                            src="{$theme-path}/images/Grainger_scaled120x80.png"
                                            alt="Grainger Engineering Library"
                                            title="Grainger Engineering Library"/>
                                </div>
                            </div>



                            <!--Test Start-->
                            <div class="row">
                                <div id="ds-user-links" class="hidden-xs">
                                    <div id="search-place" class="col-sm-3 col-md-3 col-lg-3">
                                        <form accept-charset="UTF-8" action="/search"
                                                class="form-inline" id="top_search_form" method="post">
                                            <xsl:attribute name="action">
                                                <xsl:value-of
                                                        select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='simpleURL']"
                                                        />
                                            </xsl:attribute>

                                            <div class="input-group">
                                                <label class="sr-only" for="input-search">
                                                    <xsl:value-of
                                                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='queryField']"
                                                            />
                                                </label>
                                                <input class="form-control input-sm" type="text"
                                                        id="input-search">
                                                    <xsl:attribute name="name">
                                                        <xsl:value-of
                                                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='queryField']"
                                                                />
                                                    </xsl:attribute>
                                                    <xsl:attribute name="placeholder"
                                                            >Search</xsl:attribute>
                                                </input>


                                                <div class="input-group-btn">
                                                    <button type="button"
                                                            class="btn btn-sm btn-default dropdown-toggle"
                                                            data-toggle="dropdown" aria-expanded="false">
                                                        <span class="caret"/>
                                                        <span class="sr-only">Toggle Dropdown</span>
                                                    </button>
                                                    <ul class="dropdown-menu pull-right" role="menu">
                                                        <li class="dropdown">
                                                            <input class="btn btn-sm btn-default btn-block"
                                                                    name="submit" type="submit" i18n:attr="value"
                                                                    value="xmlui.general.go">
                                                                <xsl:attribute name="onclick">
                                                                    <xsl:text>
                                                                        var radio = document.getElementById(&quot;ds-search-form-scope-container&quot;);
                                                                        if (radio != undefined &amp;&amp; radio.checked)
                                                                        {
                                                                        var form = document.getElementById(&quot;top_search_form&quot;);
                                                                        form.action=
                                                                    </xsl:text>
                                                                    <xsl:text>&quot;</xsl:text>
                                                                    <xsl:value-of
                                                                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath']"/>
                                                                    <xsl:text>/handle/&quot; + radio.value + &quot;/search&quot; ;</xsl:text>
                                                                    <xsl:text>
                                                                        }
                                                                    </xsl:text>
                                                                </xsl:attribute>
                                                            </input>
                                                        </li>

                                                        <li>
                                                            <a class="btn btn-sm btn-block btn-default">
                                                                <xsl:attribute name="href">
                                                                    <xsl:value-of
                                                                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='search'][@qualifier='advancedURL']"
                                                                            />
                                                                </xsl:attribute>
                                                                <i18n:text>xmlui.dri2xhtml.structural.search-advanced</i18n:text>
                                                            </a>
                                                        </li>
                                                    </ul>

                                                </div>
                                            </div>
                                        </form>
                                    </div>

                                    <div id="ds-user-login">
                                        <xsl:choose>
                                            <xsl:when
                                                    test="/dri:document/dri:meta/dri:userMeta/@authenticated = 'yes'">
                                                <a class="btn btn-sm btn-default blue-tooltip"
                                                        data-toggle="tooltip" data-placement="bottom"
                                                        title="My Profile">
                                                    <xsl:attribute name="href">
                                                        <xsl:value-of
                                                                select="/dri:document/dri:meta/dri:userMeta/dri:metadata[@element='identifier' and @qualifier='url']"
                                                                />
                                                    </xsl:attribute>
                                                    <span class="glyphicon glyphicon-user"/>
                                                    <!--<i18n:text>xmlui.dri2xhtml.structural.profile</i18n:text>-->
                                                    <!--<xsl:value-of select="/dri:document/dri:meta/dri:userMeta/dri:metadata[@element='identifier' and @qualifier='firstName']"/>-->
                                                    <!--<xsl:text> </xsl:text>-->
                                                    <!--<xsl:value-of select="/dri:document/dri:meta/dri:userMeta/dri:metadata[@element='identifier' and @qualifier='lastName']"/>-->
                                                </a>
                                                <a class="btn btn-sm btn-danger blue-tooltip"
                                                        data-toggle="tooltip" data-placement="bottom"
                                                        title="Logout">
                                                    <xsl:attribute name="href">
                                                        <xsl:value-of
                                                                select="/dri:document/dri:meta/dri:userMeta/dri:metadata[@element='identifier' and @qualifier='logoutURL']"
                                                                />
                                                    </xsl:attribute>
                                                    <span class="glyphicon glyphicon-log-out"/>
                                                    <!--<i18n:text>xmlui.dri2xhtml.structural.logout</i18n:text>-->
                                                </a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <a class="btn btn-sm btn-default">
                                                    <xsl:attribute name="href">
                                                        <xsl:value-of
                                                                select="/dri:document/dri:meta/dri:userMeta/dri:metadata[@element='identifier' and @qualifier='loginURL']"
                                                                />
                                                    </xsl:attribute>
                                                    <span class="glyphicon glyphicon-log-in"/> Log on
                                                    <!--<i18n:text>xmlui.dri2xhtml.structural.login</i18n:text>-->
                                                </a>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </div>
                                </div>

                                <!-- customized div to produce radio buttons and 'advanced search'
                                    link found underneath search box -->
                                <div class="col-xs-8 col-sm-5 col-md-4 col-lg-4"
                                        id="ideals-search-scope">
                                    <!-- Determine possible search scopes for main search box -->
                                    <xsl:choose>
                                        <xsl:when
                                                test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='focus'][@qualifier='container']">
                                            <!-- Search IDEALS -->
                                            <label class="radio-inline" id="ideals_scope_all"
                                                    for="ds-search-form-scope-all">
                                                <input id="ds-search-form-scope-all" type="radio"
                                                        name="scope" value="">
                                                    <xsl:attribute name="checked">
                                                        <xsl:choose>
                                                            <xsl:when test="$com='34610'">
                                                                <xsl:text/>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                                <xsl:text>checked</xsl:text>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                    </xsl:attribute>
                                                </input>
                                                <i18n:text>xmlui.dri2xhtml.structural.search</i18n:text>
                                            </label>

                                            <!-- Search This Community/Collection -->
                                            <label class="radio-inline" id="ideals_scope_container"
                                                    for="ds-search-form-scope-container">
                                                <input id="ds-search-form-scope-container"
                                                        type="radio" name="scope">
                                                    <xsl:attribute name="value">
                                                        <xsl:value-of
                                                                select="substring-after(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='focus'][@qualifier='container'],':')"
                                                                />
                                                    </xsl:attribute>
                                                    <xsl:attribute name="checked">
                                                        <xsl:choose>
                                                            <xsl:when test="$com='34610'">
                                                                <xsl:text>checked</xsl:text>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                                <xsl:text/>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                    </xsl:attribute>
                                                </input>
                                                <xsl:choose>
                                                    <xsl:when
                                                            test="/dri:document/dri:body//dri:div/dri:referenceSet[@type='detailView' and @n='community-view']">
                                                        <i18n:text>xmlui.dri2xhtml.structural.search-in-community</i18n:text>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <i18n:text>xmlui.dri2xhtml.structural.search-in-collection</i18n:text>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </label>
                                        </xsl:when>
                                    </xsl:choose>
                                </div>
                            </div>
                        </div>
                        <!--Test End-->


                        <h1 class="pagetitle visuallyhidden">
                            <xsl:choose>
                                <!-- protection against an empty page title -->
                                <xsl:when
                                        test="not(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='title'])">
                                    <xsl:text> </xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:copy-of
                                            select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='title']/node()"
                                            />
                                </xsl:otherwise>
                            </xsl:choose>

                        </h1>
                        <h2 class="static-pagetitle visuallyhidden">
                            <i18n:text>xmlui.dri2xhtml.structural.head-subtitle</i18n:text>
                        </h2>

                    </xsl:otherwise>
                </xsl:choose>

            </div>
        </div>
    </xsl:template>


    <xsl:template match="dri:trail">
        <!--put an arrow between the parts of the trail-->
        <xsl:if test="position()>1">
            <li class="ds-trail-arrow">
                <xsl:text>&#8594;</xsl:text>
            </li>
        </xsl:if>
        <li>
            <xsl:attribute name="class">
                <xsl:text>ds-trail-link </xsl:text>
                <xsl:if test="position()=1">
                    <xsl:text>first-link </xsl:text>
                </xsl:if>
                <xsl:if test="position()=last()">
                    <xsl:text>last-link</xsl:text>
                </xsl:if>
            </xsl:attribute>
            <!-- Determine whether we are dealing with a link or plain text trail link -->
            <xsl:choose>
                <xsl:when test="./@target">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="./@target"/>
                        </xsl:attribute>
                        <xsl:apply-templates/>
                    </a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </li>
    </xsl:template>

    <xsl:template name="cc-license">
        <xsl:param name="metadataURL"/>
        <xsl:variable name="externalMetadataURL">
            <xsl:text>cocoon:/</xsl:text>
            <xsl:value-of select="$metadataURL"/>
            <xsl:text>?sections=dmdSec,fileSec&amp;fileGrpTypes=THUMBNAIL</xsl:text>
        </xsl:variable>

        <xsl:variable name="ccLicenseName"
                select="document($externalMetadataURL)//dim:field[@element='rights']"/>
        <xsl:variable name="ccLicenseUri"
                select="document($externalMetadataURL)//dim:field[@element='rights'][@qualifier='uri']"/>
        <xsl:variable name="handleUri">
            <xsl:for-each
                    select="document($externalMetadataURL)//dim:field[@element='identifier' and @qualifier='uri']">
                <a>
                    <xsl:attribute name="href">
                        <xsl:copy-of select="./node()"/>
                    </xsl:attribute>
                    <xsl:copy-of select="./node()"/>
                </a>
                <xsl:if
                        test="count(following-sibling::dim:field[@element='identifier' and @qualifier='uri']) != 0">
                    <xsl:text>, </xsl:text>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:if
                test="$ccLicenseName and $ccLicenseUri and contains($ccLicenseUri, 'creativecommons')">
            <div about="{$handleUri}" class="clearfix">
                <xsl:attribute name="style">
                    <xsl:text>margin:0em 2em 0em 2em; padding-bottom:0em;</xsl:text>
                </xsl:attribute>
                <a rel="license" href="{$ccLicenseUri}" alt="{$ccLicenseName}"
                        title="{$ccLicenseName}">
                    <img>
                        <xsl:attribute name="src">
                            <xsl:value-of select="concat($theme-path,'/images/cc-ship.gif')"/>
                        </xsl:attribute>
                        <xsl:attribute name="alt">
                            <xsl:value-of select="$ccLicenseName"/>
                        </xsl:attribute>
                        <xsl:attribute name="style">
                            <xsl:text>float:left; margin:0em 1em 0em 0em; border:none;</xsl:text>
                        </xsl:attribute>
                    </img>
                </a>
                <span>
                    <xsl:attribute name="style">
                        <xsl:text>vertical-align:middle; text-indent:0 !important;</xsl:text>
                    </xsl:attribute>
                    <i18n:text>xmlui.dri2xhtml.METS-1.0.cc-license-text</i18n:text>
                    <xsl:value-of select="$ccLicenseName"/>
                </span>
            </div>
        </xsl:if>
    </xsl:template>

    <!-- Like the header, the footer contains various miscellaneous text, links, and image placeholders -->
    <xsl:template name="buildFooter">
        <footer class="ds-footer" role="contentinfo">

            <div id="ds-footer" class="container">

                <div class="row">

                    <div id="ds-footer-links" class="col-xs-12 col-sm-7">
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of
                                        select="/*/dri:meta/dri:pageMeta/dri:metadata[@qualifier='contactURL']"
                                        />
                            </xsl:attribute>
                            <i18n:text>xmlui.dri2xhtml.structural.contact-link</i18n:text>
                        </a>
                        <xsl:text> | </xsl:text>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of
                                        select="/*/dri:meta/dri:pageMeta/dri:metadata[@qualifier='feedbackURL']"
                                        />
                            </xsl:attribute>
                            <i18n:text>xmlui.dri2xhtml.structural.feedback-link</i18n:text>
                        </a>
                        <xsl:text> | </xsl:text>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:text>http://www.library.illinois.edu</xsl:text>
                            </xsl:attribute>
                            <xsl:text>University Library</xsl:text>
                        </a>
                        <xsl:text> | </xsl:text>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:text>http://www.cites.illinois.edu/</xsl:text>
                            </xsl:attribute>
                            <xsl:text>CITES</xsl:text>
                        </a>
                        <xsl:text> | </xsl:text>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:text>http://www.provost.illinois.edu/</xsl:text>
                            </xsl:attribute>
                            <xsl:text>Office of the Provost</xsl:text>
                        </a>
                        <p>Copyright &#169; 2005-2013 <a>
                            <xsl:attribute name="href">
                                <xsl:text>http://illinois.edu</xsl:text>
                            </xsl:attribute>
                            <xsl:text>University of Illinois</xsl:text>
                        </a>
                        </p>
                    </div>
                    <div id="ds-footer-right" class="col-xs-12 col-sm-5">
                        <a>
                            <xsl:attribute name="href">
                                <xsl:text>http://validator.w3.org/check?uri=referer</xsl:text>
                            </xsl:attribute>
                            <xsl:text>W3C XHTML 1.0</xsl:text>
                        </a>
                        <xsl:text> | </xsl:text>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:text>http://jigsaw.w3.org/css-validator/</xsl:text>
                            </xsl:attribute>
                            <xsl:text>W3C CSS</xsl:text>
                        </a>
                        <xsl:text> | </xsl:text>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:text>http://www.dspace.org/</xsl:text>
                            </xsl:attribute>
                            <xsl:text>DSpace</xsl:text>
                        </a>
                        <p>Most icons borrowed from: <a>
                            <xsl:attribute name="href">
                                <xsl:text>http://www.famfamfam.com/</xsl:text>
                            </xsl:attribute>
                            <xsl:text>FamFamFam</xsl:text>
                        </a>
                        </p>
                    </div>
                </div>

                <!--Invisible link to HTML sitemap (for search engines) -->
                <div class="hidden">
                    <a class="hidden">
                        <xsl:attribute name="href">
                            <xsl:value-of
                                    select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                            <xsl:text>/htmlmap</xsl:text>
                        </xsl:attribute>
                        <xsl:text>&#160;</xsl:text>
                    </a>
                </div>

            </div>
        </footer>
    </xsl:template>

    <!-- ##################################################-->
    <!-- ##########  RSS Feed customizations ##############-->
    <!-- ##################################################-->
    <!-- INDEX, COMMUNITY, COLLECTION, and ITEM page customizations   -->
    <!-- <div id="ideals-rss" /> is inserted by 'modify-ideals.xsl',  -->
    <!-- just before any Recent Submissions listing.                  -->
    <!-- Add rss feed to ability to index page, community and collection pages -->
    <xsl:template match="dri:div[@id='ideals-rss']">
        <xsl:variable name="rss1"
                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@qualifier='rss+xml'][1]"/>
        <xsl:variable name="rss2"
                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@qualifier='rss+xml'][2]"/>
        <span id="ideals-rss">
            <a href="{$rss2}">
                <img src="{$theme-path}/images/feed.png" alt="RSS feed" title="RSS feed"/>
            </a>
        </span>
    </xsl:template>


    <!--
        The meta, body, options elements; the three top-level elements in the schema
    -->




    <!--
        The template to handle the dri:body element. It simply creates the ds-body div and applies
        templates of the body's child elements (which consists entirely of dri:div tags).
    -->
    <xsl:template match="dri:body">
        <!-- For IDEALS, check if this is the homepage (index page)-->
        <xsl:variable name="special-class">
            <xsl:choose>
                <!-- If this is the index page, it will have the "file.news.div.news" div -->
                <xsl:when test="/dri:document/dri:body//dri:div[@id='file.news.div.news']">
                    <xsl:text>front-page</xsl:text>
                </xsl:when>
                <xsl:otherwise/>
            </xsl:choose>
        </xsl:variable>

        <div id="ds-body" role="main">
            <!--If there's a special-class, add as the @class of main div-->
            <xsl:if test="$special-class">
                <xsl:attribute name="class">
                    <xsl:value-of select="$special-class"/>
                </xsl:attribute>
            </xsl:if>

            <xsl:if
                    test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='alert'][@qualifier='message']">
                <div id="ds-system-wide-alert">
                    <strong>
                        <xsl:copy-of
                                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='alert'][@qualifier='message']/node()"
                                />
                    </strong>
                </div>
            </xsl:if>

            <!--For IDEALS, Add Breadcrumb Trail at top of the <body> section-->
            <div id="ds-trail-wrapper">
                <ul id="ds-trail">
                    <xsl:apply-templates select="/dri:document/dri:meta/dri:pageMeta/dri:trail"/>
                </ul>
            </div>

            <!--For IDEALS, add custom note for EUI communities-->
            <xsl:for-each select="//dri:referenceSet[@type='detailList']/dri:reference/@url">
                <xsl:variable name="col"
                        select="substring-before(substring-after(.,'2142/'),'/mets')"/>
                <xsl:if
                        test="$col='769' or $col='770' or $col='771' or $col='773' or $col='14456' or $col='809' or $col='774' or $col='847' or $col='775'">
                    <div
                            style="margin:5px auto;border:1px solid #CCC;background-color:#F6F6F6;padding:0 5px;">
                        <p><span style="font-weight:bold;">Note:</span>This is a student project
                            from a course affiliated with the <a href="http://www.eui.illinois.edu/"
                                    >Ethnography of the University Initiative</a>. EUI supports faculty
                            development of courses in which students conduct original research on
                            their university, and encourages students to think about colleges and
                            universities in relation to their communities and within larger national
                            and global contexts. </p>
                    </div>
                </xsl:if>
            </xsl:for-each>


            <div id="ds-body-container">
                <xsl:if test="$special-class">
                    <xsl:attribute name="class">
                        <xsl:value-of select="$special-class"/>
                    </xsl:attribute>
                </xsl:if>
                <xsl:apply-templates/>
            </div>

        </div>
    </xsl:template>


    <!-- Currently the dri:meta element is not parsed directly. Instead, parts of it are referenced from inside
        other elements (like reference). The blank template below ends the execution of the meta branch -->
    <xsl:template match="dri:meta"> </xsl:template>

    <!-- Meta's children: userMeta, pageMeta, objectMeta and repositoryMeta may or may not have templates of
        their own. This depends on the meta template implementation, which currently does not go this deep.
        <xsl:template match="dri:userMeta" />
        <xsl:template match="dri:pageMeta" />
        <xsl:template match="dri:objectMeta" />
        <xsl:template match="dri:repositoryMeta" />
    -->

    <xsl:template name="addJavascript">
        <xsl:variable name="jqueryVersion">
            <xsl:text>1.6.2</xsl:text>
        </xsl:variable>

        <xsl:variable name="protocol">
            <xsl:choose>
                <xsl:when test="starts-with(confman:getProperty('dspace.baseUrl'), 'https://')">
                    <xsl:text>https://</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>http://</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <!-- Add theme javascript  -->
        <xsl:for-each
                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='javascript'][@qualifier='url']">
            <script type="text/javascript">
                <xsl:attribute name="src">
                    <xsl:value-of select="."/>
                </xsl:attribute>&#160;</script>
        </xsl:for-each>

        <xsl:for-each
                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='javascript'][not(@qualifier)]">
            <script type="text/javascript">
                <xsl:attribute name="src">
                    <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                    <xsl:text>/themes/</xsl:text>
                    <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                    <xsl:text>/</xsl:text>
                    <xsl:value-of select="."/>
                </xsl:attribute>&#160;</script>
        </xsl:for-each>

        <!-- add "shared" javascript from static, path is relative to webapp root -->
        <xsl:for-each
                select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='javascript'][@qualifier='static']">
            <!--This is a dirty way of keeping the scriptaculous stuff from choice-support
                out of our theme without modifying the administrative and submission sitemaps.
                This is obviously not ideal, but adding those scripts in those sitemaps is far
                from ideal as well-->
            <xsl:choose>
                <xsl:when test="text() = 'static/js/choice-support.js'">
                    <script type="text/javascript">
                        <xsl:attribute name="src">
                            <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                            <xsl:text>/themes/</xsl:text>
                            <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='theme'][@qualifier='path']"/>
                            <xsl:text>/lib/js/choice-support.js</xsl:text>
                        </xsl:attribute>&#160;</script>
                </xsl:when>
                <xsl:when test="not(starts-with(text(), 'static/js/scriptaculous'))">
                    <script type="text/javascript">
                        <xsl:attribute name="src">
                            <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='contextPath'][not(@qualifier)]"/>
                            <xsl:text>/</xsl:text>
                            <xsl:value-of select="."/>
                        </xsl:attribute>&#160;</script>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>

        <!-- add setup JS code if this is a choices lookup page -->
        <xsl:if test="dri:body/dri:div[@n='lookup']">
            <xsl:call-template name="choiceLookupPopUpSetup"/>
        </xsl:if>



        <script type="text/javascript">
            runAfterJSImports.execute();
        </script>

        <!-- Add a google analytics script if the key is present -->
        <xsl:if
                test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='google'][@qualifier='analytics']">
            <script type="text/javascript"><xsl:text>
                var _gaq = _gaq || [];
                _gaq.push(['_setAccount', '</xsl:text><xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='google'][@qualifier='analytics']"/><xsl:text>']);
                    _gaq.push(['_trackPageview']);

                    (function() {
                    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
                    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
                    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
                    })();
                </xsl:text></script>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
