/*
 * FilterSearch.java
 *
 * Version: $Revision: 1.18 $
 *
 * Date: $Date: 2006/07/05 21:36:02 $
 *
 * Copyright (c) 2002, Hewlett-Packard Company and Massachusetts
 * Institute of Technology.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Hewlett-Packard Company nor the name of the
 * Massachusetts Institute of Technology nor the names of their
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package edu.uiuc.dspace.filtersearch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.dspace.app.xmlui.aspect.artifactbrowser.AbstractSearch;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.core.ConfigurationManager;
import org.xml.sax.SAXException;

/**
 * Filter search (based upon the Texas Digital Library filter search).
 * This extension to the AbstractSearch mechanism enables 'filter'
 * searches to be preformed. A filter search is basicaly any search
 * that can be expressed as "or clauses" "anded" together.
 * 
 * Thus something like this: (index1:one) AND ((index2:two) OR (index2:three))
 * 
 * The filtersearch is configured by the cocoon sitemap. This configuration
 * determines what filters will be used for the search.
 * 
 * The configuration consists of a series of <filter>s of two types: text or
 * select. The set of filters are contained with in at least one division, these
 * divisions determin where in the DRI document should the filter be placed.
 * Text filters enable the user to enter text in free form while select filters
 * present a list of possible values.
 * 
 * Filter Attributes:
 * 
 * type: text or select
 * 
 * label: The label shown to the user to describe this filter
 * 
 * index: The search index used for this filter. The index must be present in
 * the dspace.cfg config.
 * 
 * multiple: (only for type=select) Determines if the user is able to select
 * multiple values from the select list.
 * 
 * size: (only for type=select) Determines how many select items are viewable on
 * the screen.
 * 
 * 
 * FIXME: This class should support i18n translations.
 * 
 * FIXME: This class should be made generic so that I can apply to all
 * collections.
 * 
 * @author scott
 */

public class FilterSearch extends AbstractSearch implements Configurable
{

    /** The list of divisions where the filters are to be placed. */
    private java.util.List<FilterDivision> divisions = new ArrayList<FilterDivision>();

    /** The list of configured filters. */

    private java.util.List<Filter> filters = new ArrayList<Filter>();

    /** List of handles this filter should apply to */
    
    private java.util.List<String> handles = new ArrayList<String>();

    /** Delimiter for rebuilding URL with or'd parameters intact */
    private static final String OR_DELIMITER = "~";
    
    /**
     * Configure this filtersearch, see the class documentation for a
     * description of the configuration options.
     */
    public void configure(Configuration conf) throws ConfigurationException
    {

        java.util.List<Configuration> divConfs = new ArrayList<Configuration>();

        // Config parameter
        String configLine = null;
        Configuration apply = conf.getChild("apply");
        if (apply != null)
        {
        	String configName = apply.getAttribute("config");
        	configLine = ConfigurationManager.getProperty(configName);
        }
        if (configLine != null)
        {
	        for (String handle : configLine.split(","))
	        {
	        	handles.add(handle.trim());
	        }
        }
        
        // Div structure
        Configuration peek = conf.getChild("div", false);

        while (peek != null)
        {
            String name = peek.getAttribute("n");
            String render = peek.getAttribute("rend");
            divisions.add(new FilterDivision(name, render));

            // Add it to the list and get the next one.
            divConfs.add(peek);
            peek = peek.getChild("div", false);
        }

        peek = divConfs.get(divConfs.size() - 1);

        for (Configuration filterConf : peek.getChildren())
        {

            if (!"filter".equals(filterConf.getName()))
                throw new ConfigurationException(
                        "Expected a <filter> element instead of '"
                                + filterConf.getName() + "'.");

            String type = filterConf.getAttribute("type", "text");
            String label = filterConf.getAttribute("label");
            String index = filterConf.getAttribute("index");
            boolean multiple = filterConf.getAttributeAsBoolean("multiple",
                    true);
            int size = filterConf.getAttributeAsInteger("size", 0);

            if (!("text".equals(type) || "select".equals(type)))
                throw new ConfigurationException(
                        "Unknown filter type for filter '" + index + "'. ");

            Filter filter = new Filter(type, label, index, multiple, size);

            for (Configuration itemConf : filterConf.getChildren())
            {
                String option = itemConf.getName();
                
                if ("item".equals(option)) {
                    String value = itemConf.getAttribute("value");
                    boolean defaultSelect = itemConf.getAttributeAsBoolean("default", false);
                    String itemLabel = itemConf.getValue();
                    Item item = new Item(itemLabel, value, defaultSelect);
                    filter.addItem(item);
                } else if ("or".equals(option)) {
                    String orIndex = itemConf.getAttribute("index");
                    boolean defaultSelect = itemConf.getAttributeAsBoolean("default", false);
                    String orLabel = itemConf.getValue();
                    Item item = new Item(orLabel, orIndex, defaultSelect);
                    filter.addItem(item);
                } else if ("year-range".equals(option)) {
                    int thisYear = Calendar.getInstance().get(Calendar.YEAR);
                    int startYear; 
                    try {
                        startYear = Integer.valueOf(itemConf.getAttribute("start"));
                        if (startYear > thisYear) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        throw new ConfigurationException("Expected <year-range start='YEAR'/> where YEAR <= the current year");
                    }

                    for (int year = thisYear ; year >= startYear ; year--)
                        filter.addItem(new Item(Integer.toString(year), Integer.toString(year), false));
                } else {
                    throw new ConfigurationException(
                            "Expected an <item>, <or> or <year-range> element inside a <filter>.");
                }

            }
            filters.add(filter);
        }

    }

    /**
     * Add the community's title and trail links to the page's metadata
     */
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException
    {
    	if (!apply())
    		return;
    	
        if (!parameters.getParameterAsBoolean("use-divisions", true))
        {
            DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
            if (dso instanceof Community)
            {
                // Set up the major variables
                Community community = (Community) dso;
                // Set the page title
                pageMeta.addMetadata("title").addContent(
                        community.getMetadata("name"));

                // Add the trail back to the repository root.
                pageMeta.addTrailLink(contextPath + "/",
                        message("xmlui.general.dspace_home"));
                HandleUtil.buildHandleTrail(community, pageMeta,contextPath);
            }
            else if (dso instanceof Collection)
            {
                // Set up the major variables
                Collection collection = (Collection) dso;
                // Set the page title
                pageMeta.addMetadata("title").addContent(
                        collection.getMetadata("name"));

                // Add the trail back to the repository root.
                pageMeta.addTrailLink(contextPath + "/",
                        message("xmlui.general.dspace_home"));
                HandleUtil.buildHandleTrail(collection, pageMeta,contextPath);
            }
            else
            {
                // FIXME: I don't know what to do if it's not a community or
                // collection.
                pageMeta.addMetadata("title").addContent("Search");
                
                pageMeta.addTrailLink(contextPath + "/",
                        message("xmlui.general.dspace_home"));
                
            }
            
            pageMeta.addTrail().addContent("Search");
        }
        
    }

    /** What to add at the end of the body */
    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {
    	if (!apply())
    		return;

        Request request = ObjectModelHelper.getRequest(objectModel);
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

        // if "Return" clicked or no search criteria, redirect to community/collection page
        if (request.getParameter("return") != null ||
            (request.getParameter("submit") != null && getQuery().length() == 0))
        {
            ((HttpServletResponse)objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT)).sendRedirect(request.getContextPath() + "/handle/" + dso.getHandle());
            return;
        }

        Division current = null;

        if (!parameters.getParameterAsBoolean("use-divisions", true))
        {
            current = body.addDivision("filter-search", "primary filter-search-results");

            if (dso instanceof Community)
            {
                Community community = (Community) dso;
                
                current.setHead("Search in " + community.getMetadata("name"));
            }
            else if (dso instanceof Collection)
            {
                Collection collection = (Collection) dso;
                
                current.setHead("Search in " + collection.getMetadata("name"));
            }
            else
            {
                current.setHead("Search");
            }
            
        }
        else
        {
            for (FilterDivision filterDivision : divisions)
            {
                if (current == null)
                {
                    current = body.addDivision(filterDivision.name,
                            filterDivision.render);
                }
                else
                {
                    current = current.addDivision(filterDivision.name,
                            filterDivision.render);
                }
            }
        }

        Division query = current.addInteractiveDivision("filter-search",
                contextPath + "/handle/" + dso.getHandle() + "/filter-search",
                Division.METHOD_POST, "secondary filter-search");
        List list = query.addList("filter-search");

        for (Filter filter : filters)
        {
            list.addLabel(filter.label);

            String[] values = request.getParameterValues(filter.index);

            if ("text".equals(filter.type))
            {
                org.dspace.app.xmlui.wing.element.Item textItem =
                        list.addItem(null, filter.multiple ? null : "filter-search-or");
                Text text = textItem.addText(filter.index);
                if (!(values == null || values.length == 0))
                    text.setValue(values[0]);
                if (filter.items.size() > 0)
                {
                    org.dspace.app.xmlui.wing.element.Item orItem;
                    if (filter.multiple) {
                        list.addLabel();
                        orItem = list.addItem(null, "filter-search-or");
                        orItem.addContent("or:");
                    } else {
                        orItem = textItem;
                    }
                    CheckBox orIndexes = orItem.addCheckBox(filter.index + "-or");
                    for (Item item : filter.items)
                    {
                        String[] orValues = request.getParameterValues(filter.index + "-or");
                        boolean selected = shouldSelectItem(orValues, item);
                        orIndexes.addOption(selected, item.value).addContent(item.label);
                    }
                }
            }
            else if ("select".equals(filter.type))
            {
                Select select = list.addItem().addSelect(filter.index);
                select.setMultiple(filter.multiple);
                select.setSize(filter.size);

                for (Item item : filter.items)
                {
                    boolean selected = shouldSelectItem(values, item);
                    select.addOption(selected, item.value).addContent(item.label);
                }
            }

        }

        Para para = query.addPara(null, "button-list");

        para.addButton("submit").setValue(message("xmlui.general.go"));
        if (request.getParameter("submit") != null)
        {
            para.addButton("return").setValue(message("xmlui.general.cancel"));
        }

        // Add the result division only if they selected search
        buildSearchResultsDivision(current);

    }

    private static boolean shouldSelectItem(String[] values, Item item) {
        if (values == null)
            return item.defaultSelect;
        for (String value : values)
        {
            if (value.equals(item.value))
                return true;
        }
        return false;
    }


    /**
     * Determine if the filter search should be applied to this handle.
     * 
     * @return True if it should be applied, otherwise false.
     */
    private boolean apply() throws SQLException
    {
    	DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
    	
    	String handle = dso.getHandle();
    	
    	for (String test : handles)
    		if (test.equals(handle))
    			return true;
    			
    	return false;
    }
    
    
    
    /**
     * Return the query string for this filterSearch based upon the user
     * selected filters.
     */
    protected String getQuery()
    {
        Request request = ObjectModelHelper.getRequest(objectModel);

        String andQuery = "";
        boolean andFirst = true;
        for (Filter filter : filters)
        {
            String[] values = request.getParameterValues(filter.index);

            // If there are none this short curcit
            if (values == null || values.length == 0)
                continue;

            String subQuery = null;
            if ("text".equals(filter.type)) {
                // Text fields build a subquery with ANDed keywords. So we 
                //split the string up into words and AND each of them together.
                String[] words = values[0].split(" ");
                subQuery = getSubquery(words, filter.index, "AND");
                
                if (subQuery.length() == 0)
                    continue;

                // If there are any additional indexes to search, OR them to the query.
                String[] orIndexes = request.getParameterValues(filter.index + "-or");
                if (orIndexes != null && orIndexes.length > 0) {
                    if (filter.multiple)
                        subQuery = "(" + subQuery + ")";
                    else // Use only the selected indexes, not the default index.
                        subQuery = "";
                    for (String index : orIndexes)
                        subQuery += " OR (" + getSubquery(words, index, "AND") + ")";
                    if (!filter.multiple)
                        subQuery = subQuery.substring(3);
                }
            } else if ("select".equals(filter.type)) {
                // Select fields produce a subquery with ORed values.
                // Build an OR list of possible values for this index.
                subQuery = getSubquery(values, filter.index, "OR");
            }

            // If no subquery was built then it doesn't need to be put into the
            // final query.
            if (subQuery == null || subQuery.length() == 0)
                continue;

            // Add the subquery.
            if (andFirst)
                andFirst = false;
            else
                andQuery += " AND ";
            andQuery += "(" + subQuery + ")";
        }

        return andQuery;
    }

    private static String getSubquery(String[] values, String index, String conjunction) {
        String subQuery = "";
        boolean subFirst = true;
        for (String value : values)
        {
            //skip any empty elements
            if (value == null || value.length() == 0)
                continue;

            // remove quotes from value: they'll break the search string
            value = value.replace("\"", "");

            if (subFirst)
            {
                subFirst = false;
            }
            else
            {
                subQuery += " " + conjunction + " ";
            }
            if ("ANY".equals(index))
                subQuery += "(\""+value+"\")";
            else
                subQuery += "(" + index + ":\"" + value + "\")";
        }
        return subQuery;
    }

    /**
     * Generate the url string for this page which includes all the filter
     * parameters.
     * 
     * @param parameters
     *            Any extra parameters to add to the URL.
     */
    protected String generateURL(Map<String, String> parameters)
    {
        Request request = ObjectModelHelper.getRequest(objectModel);

        // Relay all the query parameters
        boolean triped = false;
        for (Filter filter : filters)
        {
            String[] values = request.getParameterValues(filter.index);

            if (values != null) {
                // Pass on all the current values for the filter.
                for (String value : values)
                {
                    parameters.put(filter.index, value);
                    triped = true;
                    if (filter.items.size() > 0)
                    {
                        parameters.put(filter.index+"-or", buildOrIndexParamString(filter));
                    }
                }
            }
        }

        // Mimic the submit button.
        if (triped)
            parameters.put("submit", "go");

        try
        {
            DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
            //need to process the items so can't use super here :(
            return generateURLBuffer(contextPath + "/handle/" + dso.getHandle()
                    + "/filter-search", parameters);

        }
        catch (SQLException sqle)
        {
            // FIXME: I don't know what to do with this very unlikely error.
            //return "";
            throw new RuntimeException("SQL Error in FilterSearch.java#generateURL");
        }
    }

    /**
     * This acts just like the super method for generateURL, but handles the "or-indexes."
     *  To maek updating versions easier, we didn't want to change the super method, so we've had to duplicate some code here.
     * @param baseURL
     * @param parameters
     * @return
     */
    public static String generateURLBuffer(String baseURL,
                                     Map<String, String> parameters)
    {
        StringBuilder urlBuffer = new StringBuilder();
        for (Map.Entry<String, String> param : parameters.entrySet())
        {
            if (urlBuffer.length() == 0)
            {
                urlBuffer.append(baseURL).append('?');
            }
            else
            {
                urlBuffer.append( '&');
            }
            //Begin the chages for Filter Search
            if (param.getKey().endsWith("-or"))
            {
                urlBuffer.append(buildOrIndexQueryString(param));
            }
            else
            {
                urlBuffer.append(param.getKey()).append("=").append(param.getValue());
            }
        }

        return urlBuffer.length() > 0 ? urlBuffer.toString() : baseURL;
    }
    private static String buildOrIndexParamString(Filter filter)
    {
        String orValue = "";
        for (Item item: filter.items)
        {
            if(orValue.equals("")) {
                orValue += item.value;
            }else{
                orValue += OR_DELIMITER+item.value;
            }
        }
        return orValue;
    }
    /**
     * Format the string and key from the or-index parameter to
     * @param param
     * @return
     */
    private static String buildOrIndexQueryString(Map.Entry<String, String> param)
    {
        String itemString = "";
        for(String value: param.getValue().split(OR_DELIMITER))
        {
            if(itemString.equals(""))
            {
                itemString+=param.getKey()+"="+value;
            }
            else
            {
                itemString+="&"+param.getKey()+"="+value;
            }
        }
        return itemString;

    }
    /**
     * A private division class that represents divisions where filters are to
     * be placed.
     */
    private class FilterDivision
    {
        /** The division's name */
        protected String name;

        /** The division's render */
        protected String render;

        /**
         * Construct a new filter division
         * 
         * @param name
         *            The name of the division.
         * @param render
         *            The render of the division.
         */
        public FilterDivision(String name, String render)
        {
            this.name = name;
            this.render = render;
        }
    }

    /**
     * A private filter class that represents a configured filter.
     */
    private class Filter
    {
        /** The filter type (ethir select or text) */
        protected String type;

        /** The filter's label, what's shown on the screen */
        protected String label;

        /** The internal DSpace index to search for this filter */
        protected String index;

        /** (select only) Weather multiple values are selectable by the user */
        protected boolean multiple;

        /** (select only) How many options to show on the screen */
        protected int size;

        /** (select only) All the possible items that may be selected */
        protected java.util.List<Item> items = new ArrayList<Item>();

        /**
         * Create a new filter.
         * 
         * @param type
         *            The filter's type (select or text)
         * @param label
         *            The filters label to describe it to the user.
         * @param index
         *            The internal DSpace index to search.
         * @param multiple
         *            If multiple values are possible.
         * @param size
         *            How many of those multiple values to show on the screen.
         */
        public Filter(String type, String label, String index,
                boolean multiple, int size)
        {
            this.type = type;
            this.label = label;
            this.index = index;
            this.multiple = multiple;
            this.size = size;
        }

        /**
         * (select/text only) Add a possible item to the select list.
         * 
         * @param item
         */
        public void addItem(Item item)
        {
            items.add(item);
        }

    }

    /**
     * Private class to represent an possible item of the select filter
     */
    private class Item
    {
        /** The label shown to the user. */
        protected String label;

        /** The internal value used for the search */
        protected String value;

        /** Dose this item default to being selected */
        protected boolean defaultSelect;

        /**
         * Construct a new item.
         * 
         * @param label
         *            The label shown to the user.
         * @param value
         *            The value used in the search
         * @param defaultSelect
         *            If the item should default to being selected.
         */
        public Item(String label, String value, boolean defaultSelect)
        {
            this.label = label;
            this.value = value;
            this.defaultSelect = defaultSelect;
        }
    }

}
