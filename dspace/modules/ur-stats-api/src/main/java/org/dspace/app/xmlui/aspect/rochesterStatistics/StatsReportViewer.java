/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dspace.app.xmlui.aspect.rochesterStatistics;

/**
 *
 * @author Zhimin Chen (U of Illinois)
 */
import java.util.Iterator;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.sql.Date;
import edu.ur.dspace.stats.StatsDAO;

import org.dspace.content.Metadatum;
import org.dspace.content.Item;
import org.dspace.app.xmlui.cocoon.DSpaceFeedGenerator;
import org.apache.excalibur.source.SourceValidity;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.app.xmlui.utils.DSpaceValidity;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.apache.cocoon.environment.Request;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Para;
import org.dspace.app.xmlui.wing.element.Select;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DCDate;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.xml.sax.SAXException;

public class StatsReportViewer extends AbstractDSpaceTransformer
{

	private static final Logger log = Logger.getLogger(DSpaceFeedGenerator.class);

    /** Top Downloads Listing **/
    private static final Message T_head_download_statsreport =
    	message("xmlui.RochesterStatistics.StatsReportViewer.head");

    private static final Message T_head_history_download_statsreport =
    	message("xmlui.RochesterStatistics.history.StatsReportViewer.head");

    private static final Message T_head_top_monthly_downloads =
    	message("xmlui.RochesterStatistics.StatsReportViewer.head_top_monthly_downloads");

    private static final Message T_head_top_yearly_downloads =
    	message("xmlui.RochesterStatistics.StatsReportViewer.head_top_yearly_downloads");

    private static final Message T_head_top_theSpecialTime_downloads =
    	message("xmlui.RochesterStatistics.StatsReportViewer.head_top_theSpecialTime_downloads");

    private static final Message T_head_top_overall_community_downloads =
    	message("xmlui.RochesterStatistics.StatsReportViewer.head_top_overall_community_downloads");

    static final Message T_head_top_overall_collection_downloads =
    	message("xmlui.RochesterStatistics.StatsReportViewer.head_top_overall_collection_downloads");

    private static final Message P_item_top_download =
    	message("xmlui.RochesterStatistics.StatisticsViewer.item_top_download");

    private static final Message T_dspace_home =
        message("xmlui.general.dspace_home");

    private static final Message P_return_Back_CollectionHomepage =
        message("xmlui.RochesterStatistics.StatsReportViewer.Return-to-Collection-homepage");

     private static final Message P_return_Back_CommunityHomepage =
        message("xmlui.RochesterStatistics.StatsReportViewer.Return-to-Community-homepage");

    private static final Message T_untitled =
		message("xmlui.general.untitled");

    private final static Message T_go =
        message("xmlui.Statistics.StatsReportView.go_getHistoryReport");

    private final static Message T_choose_month =
        message("xmlui.ArtifactBrowser.ConfigurableBrowse.general.choose_month");

    private final static Message T_choose_year =
        message("xmlui.ArtifactBrowser.ConfigurableBrowse.general.choose_year");

    private final static Message T_jump_select =
        message("xmlui.ArtifactBrowser.ConfigurableBrowse.general.jump_select");

    /** Cached validity object */
    private SourceValidity validity;
  
    public SourceValidity getValidity()
    {
    	if (this.validity == null)
    	{
	        try
	        {
	            DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

	            if (dso == null)
	                return null;

	            DSpaceValidity validity = new DSpaceValidity();

	            this.validity = validity.complete();
	        }
	        catch (Exception e)
	        {
	            // Just ignore all errors and return an invalid cache.
	        }
    	}
    	return this.validity;
    }

    /**
     * Add the community's title and trail links to the page's metadata
     */
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException
    {
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (!(dso instanceof Community) && !(dso instanceof Collection)){
            return;
        }
        else if (dso instanceof Community)
        {
            // Set up the major variables
           Community community = (Community) dso;
          // Set the page title
          String name = community.getMetadata("name");
          if (name == null || name.length() == 0)
        	pageMeta.addMetadata("title").addContent(T_untitled);
          else
        	pageMeta.addMetadata("title").addContent(name);

           // Add the trail back to the repository root.
           pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
           HandleUtil.buildHandleTrail(community, pageMeta,contextPath);
        }
        else if (dso instanceof Collection)
        {
            Collection collection = (Collection) dso;

        // Set the page title
            String name = collection.getMetadata("name");
            if (name == null || name.length() == 0)
                pageMeta.addMetadata("title").addContent(T_untitled);
            else
                pageMeta.addMetadata("title").addContent(name);

            pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
            HandleUtil.buildHandleTrail(collection,pageMeta,contextPath);
        }
    }


    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
        {
            DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

            Context context = ContextUtil.obtainContext(objectModel);
            // Build the DRI Body
            Division div = body.addDivision("Year&Month", "Year_Month");
            // Build the internal navigation (jump lists)
            addBrowseJumpNavigation(div);

            //Add the three top downloads table in the body(monthly, yearly and overall)
           
            StatsDAO statsDAO = new StatsDAO();

            //Get value from month/year dropdown list
            Request request = ObjectModelHelper.getRequest(objectModel);
            String month = request.getParameter(BrowseParams.MONTH);
            String year = request.getParameter(BrowseParams.YEAR);

                //Get top download items list
            if (dso != null)
            {
                //The default setting or nothing is selected
                if (((month==null) && (year==null))||((month.equals("0"))&&(year.equals("0"))))
                {

                        // Added the community and collection name As the report title
                        if (!(dso instanceof Community)&&!(dso instanceof Collection))
                        {
                            return;
                        }
                        else if (dso instanceof Community){
                            Community community = (Community) dso;
                            Division statsDiv = body.addDivision("download-statsreportview", "download-statsreportview");
                            List statsList = statsDiv.addList("statistics-site-list");
                            String name = community.getMetadata("name");
                            if (name == null || name.length() == 0)
                                statsList.setHead(T_untitled);
                            else
                                statsList.setHead(T_head_download_statsreport.parameterize(name));

                        }
                        else if (dso instanceof Collection){
                            Collection collection = (Collection) dso;
                            Division statsDiv = body.addDivision("download-statsreportview","download-statsreportview");
                            List statsList = statsDiv.addList("statistics-site-list");
                            String name = collection.getMetadata("name");
                            if (name == null || name.length() == 0)
                                statsList.setHead(T_untitled);
                            else
                                statsList.setHead(T_head_download_statsreport.parameterize(name));
                        }

                        //Community Level Download reprots
                        if (dso instanceof Community)
                        {
                             //Get the Top Download list for current month
                            HashMap monthlyTopDownloads = statsDAO.getMonthlyTopDownloadedItemsInCommunity(dso.getID(),10);

                            //Only display it if it is *not* empty
                            if(monthlyTopDownloads!=null && !monthlyTopDownloads.isEmpty())
                            {
                                Division topTenListDiv = body.addDivision("monthly-top-download-list", "monthly-top-download-list");

                                /* create an ordered list of top downloads*/
                                List list = topTenListDiv.addList("top-download-list-monthly", List.TYPE_ORDERED);
                                list.setHead(T_head_top_monthly_downloads);

                                //Loop through each item
                                Iterator iterator = monthlyTopDownloads.keySet().iterator();
                                while(iterator.hasNext())
                                {
                                    //get reference to item object
                                    Long itemID = (Long) iterator.next();
                                    Long downloadCount = (Long) monthlyTopDownloads.get(itemID);
                                    Item downloadItem = Item.find(context, itemID.intValue());

                                    /** Get Title **/
                                    Metadatum[] titleDC = downloadItem.getMetadata("dc", "title", null, Item.ANY);

                                    String url = contextPath + "/handle/" + downloadItem.getHandle();
                                    org.dspace.app.xmlui.wing.element.Item item = list.addItem("monthly-top-download-list-item","monthly-top-download-list-item");

                                    //add link to item to list
                                    if (titleDC.length == 0)
                                    {   // If there is no title, use "Untitled"
                                        item.addXref(url).addContent(P_item_top_download.parameterize(T_untitled, downloadCount));
                                    }
                                    else
                                    {
                                        item.addXref(url).addContent(P_item_top_download.parameterize(titleDC[0].value, downloadCount));
                                    }
                                }
                            }

                            //Get the Top Download list for current year
                            HashMap yearlyTopDownloads = statsDAO.getYearlyTopDownloadedItemsInCommunity(dso.getID(),10);

                            //Only display it if it is *not* empty
                            if(yearlyTopDownloads!=null && !yearlyTopDownloads.isEmpty())
                            {
                                Division topTenListDiv = body.addDivision("yearly-top-download-list", "yearly-top-download-list");

                                /* create an ordered list of top downloads*/
                                List list = topTenListDiv.addList("top-download-list-yearly", List.TYPE_ORDERED);
                                list.setHead(T_head_top_yearly_downloads);

                                //Loop through each item
                                Iterator iterator = yearlyTopDownloads.keySet().iterator();
                                while(iterator.hasNext())
                                {
                                    //get reference to item object
                                    Long itemID = (Long) iterator.next();
                                    Long downloadCount = (Long) yearlyTopDownloads.get(itemID);
                                    Item downloadItem = Item.find(context, itemID.intValue());


                                    /** Get Title **/
                                    Metadatum[] titleDC = downloadItem.getMetadata("dc", "title", null, Item.ANY);

                                    String url = contextPath + "/handle/" + downloadItem.getHandle();
                                    org.dspace.app.xmlui.wing.element.Item item = list.addItem("yearly-top-download-list-item","yearly-top-download-list-item");

                                    //add link to item to list
                                    if (titleDC.length == 0)
                                    {   // If there is no title, use "Untitled"
                                        item.addXref(url).addContent(P_item_top_download.parameterize(T_untitled, downloadCount));
                                    }
                                    else
                                    {
                                        item.addXref(url).addContent(P_item_top_download.parameterize(titleDC[0].value, downloadCount));
                                    }
                                }
                            }


                             //Get the Top Download list for overall
                            HashMap overallTopDownloads = statsDAO.getTopDownloadedItemsInCommunity(dso.getID(),10);

                            //Only display it if it is *not* empty
                            if(overallTopDownloads!=null && !overallTopDownloads.isEmpty())
                            {
                                Division topTenListDiv = body.addDivision("overall-top-download-list", "overall-top-download-list");

                                /* create an ordered list of top downloads*/
                                List list = topTenListDiv.addList("top-download-list-overall", List.TYPE_ORDERED);
                                list.setHead(T_head_top_overall_community_downloads);

                                //Loop through each item
                                Iterator iterator = overallTopDownloads.keySet().iterator();
                                while(iterator.hasNext())
                                {
                                    //get reference to item object
                                    Long itemID = (Long) iterator.next();
                                    Long downloadCount = (Long) overallTopDownloads.get(itemID);
                                    Item downloadItem = Item.find(context, itemID.intValue());


                                    /** Get Title **/
                                    Metadatum[] titleDC = downloadItem.getMetadata("dc", "title", null, Item.ANY);

                                    String url = contextPath + "/handle/" + downloadItem.getHandle();
                                    org.dspace.app.xmlui.wing.element.Item item = list.addItem("overall-top-download-list-item","overall-top-download-list-item");

                                    //add link to item to list
                                    if (titleDC.length == 0)
                                    {   // If there is no title, use "Untitled"
                                        item.addXref(url).addContent(P_item_top_download.parameterize(T_untitled, downloadCount));
                                    }
                                    else
                                    {
                                        item.addXref(url).addContent(P_item_top_download.parameterize(titleDC[0].value, downloadCount));
                                    }
                                }
                            }
                            //Create a return link to the community home page
                            {
                                Division statsDiv = body.addDivision("download-statistics", "download-statistics");
                                List statsList = statsDiv.addList("statistics-community-list");
                                org.dspace.app.xmlui.wing.element.Item item = statsList.addItem("Return", null);
                                String url = contextPath + "/handle/"+dso.getHandle();
                                item.addXref(url,P_return_Back_CommunityHomepage);
                            }
                        }

                        //Collection Level Download Reports
                        if (dso instanceof Collection)
                        {
                           //Get the Top Download list for current month
                            HashMap monthlyTopDownloads = statsDAO.getMonthlyTopDownloadedItemsInCollection(dso.getID(),10);

                            //Only display it if it is *not* empty
                            if(monthlyTopDownloads!=null && !monthlyTopDownloads.isEmpty())
                            {
                                Division topTenListDiv = body.addDivision("monthly-top-download-list", "monthly-top-download-list");

                                /* create an ordered list of top downloads*/
                                List list = topTenListDiv.addList("top-download-list-monthly", List.TYPE_ORDERED);
                                list.setHead(T_head_top_monthly_downloads);

                                //Loop through each item
                                Iterator iterator = monthlyTopDownloads.keySet().iterator();
                                while(iterator.hasNext())
                                {
                                    //get reference to item object
                                    Long itemID = (Long) iterator.next();
                                    Long downloadCount = (Long) monthlyTopDownloads.get(itemID);
                                    Item downloadItem = Item.find(context, itemID.intValue());

                                    /** Get Title **/
                                    Metadatum[] titleDC = downloadItem.getMetadata("dc", "title", null, Item.ANY);

                                    String url = contextPath + "/handle/" + downloadItem.getHandle();
                                    org.dspace.app.xmlui.wing.element.Item item = list.addItem("monthly-top-download-list-item","monthly-top-download-list-item");

                                    //add link to item to list
                                    if (titleDC.length == 0)
                                    {   // If there is no title, use "Untitled"
                                        item.addXref(url).addContent(P_item_top_download.parameterize(T_untitled, downloadCount));
                                    }
                                    else
                                    {
                                        item.addXref(url).addContent(P_item_top_download.parameterize(titleDC[0].value, downloadCount));
                                    }
                                }
                            }

                            //Get the Top Download list for current year

                            HashMap yearlyTopDownloads = statsDAO.getYearlyTopDownloadedItemsInCollection(dso.getID(),10);

                            //Only display it if it is *not* empty
                            if(yearlyTopDownloads!=null && !yearlyTopDownloads.isEmpty())
                            {
                                Division topTenListDiv = body.addDivision("yearly-top-download-list", "yearly-top-download-list");

                                /* create an ordered list of top downloads*/
                                List list = topTenListDiv.addList("top-download-list-yearly", List.TYPE_ORDERED);
                                list.setHead(T_head_top_yearly_downloads);

                                //Loop through each item
                                Iterator iterator = yearlyTopDownloads.keySet().iterator();
                                while(iterator.hasNext())
                                {
                                    //get reference to item object
                                    Long itemID = (Long) iterator.next();
                                    Long downloadCount = (Long) yearlyTopDownloads.get(itemID);
                                    Item downloadItem = Item.find(context, itemID.intValue());


                                    /** Get Title **/
                                    Metadatum[] titleDC = downloadItem.getMetadata("dc", "title", null, Item.ANY);

                                    String url = contextPath + "/handle/" + downloadItem.getHandle();
                                    org.dspace.app.xmlui.wing.element.Item item = list.addItem("yearly-top-download-list-item","yearly-top-download-list-item");

                                    //add link to item to list
                                    if (titleDC.length == 0)
                                    {   // If there is no title, use "Untitled"
                                        item.addXref(url).addContent(P_item_top_download.parameterize(T_untitled, downloadCount));
                                    }
                                    else
                                    {
                                        item.addXref(url).addContent(P_item_top_download.parameterize(titleDC[0].value, downloadCount));
                                    }
                                }
                            }

                             //Get the Top Download list for overall
                            HashMap overallTopDownloads = statsDAO.getTopDownloadedItemsInCollection(dso.getID(),10);

                            //Only display it if it is *not* empty
                            if(overallTopDownloads!=null && !overallTopDownloads.isEmpty())
                            {
                                Division topTenListDiv = body.addDivision("overall-top-download-list", "overall-top-download-list");

                                /* create an ordered list of top downloads*/
                                List list = topTenListDiv.addList("top-download-list-overall", List.TYPE_ORDERED);
                                list.setHead(T_head_top_overall_collection_downloads);

                                //Loop through each item
                                Iterator iterator = overallTopDownloads.keySet().iterator();
                                while(iterator.hasNext())
                                {
                                    //get reference to item object
                                    Long itemID = (Long) iterator.next();
                                    Long downloadCount = (Long) overallTopDownloads.get(itemID);
                                    Item downloadItem = Item.find(context, itemID.intValue());

                                    /** Get Title **/
                                    Metadatum[] titleDC = downloadItem.getMetadata("dc", "title", null, Item.ANY);

                                    String url = contextPath + "/handle/" + downloadItem.getHandle();
                                    org.dspace.app.xmlui.wing.element.Item item = list.addItem("overall-top-download-list-item","overall-top-download-list-item");

                                    //add link to item to list
                                    if (titleDC.length == 0)
                                    {   // If there is no title, use "Untitled"
                                        item.addXref(url).addContent(P_item_top_download.parameterize(T_untitled, downloadCount));
                                    }
                                    else
                                    {
                                        item.addXref(url).addContent(P_item_top_download.parameterize(titleDC[0].value, downloadCount));
                                    }
                                }
                            }
                            //Create a return link to the collection home page
                            {
                                Division statsDiv = body.addDivision("download-statistics", "download-statistics");
                                List statsList = statsDiv.addList("statistics-collection-list");
                                org.dspace.app.xmlui.wing.element.Item item = statsList.addItem("return", null);
                                String url = contextPath + "/handle/"+dso.getHandle();
                                item.addXref(url,P_return_Back_CollectionHomepage);
                            }
                        }
                }
             // month and year are seleceted
                else if ((!(month.equals("0"))&&!(year.equals("0"))))
                {
                    int theYear = Integer.parseInt(year);
                    int theMonth = Integer.parseInt(month)-1;
                    Date startDay = statsDAO.getStartOftheSpecialtime(theYear, theMonth);
                    Date endDay = statsDAO.getEndOftheSpecialtime(theYear, theMonth);
                    
                    // Added the community and collection name As the report title
                    if (!(dso instanceof Community)&&!(dso instanceof Collection))
                    {
                        return;
                    }
                    else if (dso instanceof Community)
                    {
                        Community community = (Community) dso;
                        Division statsDiv = body.addDivision("historydownload-statsreportview", "historydownload-statsreportview");
                        List statsList = statsDiv.addList("statistics-site-list");
                        String name = community.getMetadata("name");
                        if (name == null || name.length() == 0)
                            statsList.setHead(T_untitled);
                        else
                        {
                            name = name + " ( from : "+ startDay + " To : "+ endDay + " )";
                            statsList.setHead(T_head_history_download_statsreport.parameterize(name));
                        }

                    }
                    else if (dso instanceof Collection)
                    {
                        Collection collection = (Collection) dso;
                        Division statsDiv = body.addDivision("historydownload-statsreportview","historydownload-statsreportview");
                        List statsList = statsDiv.addList("statistics-site-list");
                        String name = collection.getMetadata("name");
                        if (name == null || name.length() == 0)
                            statsList.setHead(T_untitled);
                        else
                        {
                            name = name + " ( from : "+ startDay + " To : "+ endDay + " )";
                            statsList.setHead(T_head_history_download_statsreport.parameterize(name));
                        }
                    }

                    //Create the top-download table
                    if (dso instanceof Community)
                    {
                       //Get the Top Download list for special time

                        HashMap theSpecialTimeTopDownloads = statsDAO.getTopDownloadedItemsInSpecialTimeInCommunity(dso.getID(),10, startDay, endDay);

                        //Only display it if it is *not* empty
                        if(theSpecialTimeTopDownloads!=null && !theSpecialTimeTopDownloads.isEmpty())
                        {
                            Division topTenListDiv = body.addDivision("theSpecialTime-top-download-list", "theSpecialTime-top-download-list");

                            /* create an ordered list of top downloads*/
                            List list = topTenListDiv.addList("top-download-list-theSpecialTime", List.TYPE_ORDERED);
                            list.setHead("  from : "+ startDay + " to : "+ endDay );

                            //Loop through each item
                            Iterator iterator = theSpecialTimeTopDownloads.keySet().iterator();
                            while(iterator.hasNext())
                            {
                                //get reference to item object
                                Long itemID = (Long) iterator.next();
                                Long downloadCount = (Long) theSpecialTimeTopDownloads.get(itemID);
                                Item downloadItem = Item.find(context, itemID.intValue());


                                /** Get Title **/
                                Metadatum[] titleDC = downloadItem.getMetadata("dc", "title", null, Item.ANY);

                                String url = contextPath + "/handle/" + downloadItem.getHandle();
                                org.dspace.app.xmlui.wing.element.Item item = list.addItem("theSpecialTime-top-download-list-item","theSpecialTime-top-download-list-item");

                                //add link to item to list
                                if (titleDC.length == 0)
                                {   // If there is no title, use "Untitled"
                                    item.addXref(url).addContent(P_item_top_download.parameterize(T_untitled, downloadCount));
                                }
                                else
                                {
                                    item.addXref(url).addContent(P_item_top_download.parameterize(titleDC[0].value, downloadCount));
                                }
                            }
                        }

                        //Create a return link to the community home page
                        {
                            Division statsDiv = body.addDivision("download-statistics", "download-statistics");
                            List statsList = statsDiv.addList("statistics-community-list");
                            org.dspace.app.xmlui.wing.element.Item item = statsList.addItem("Return", null);
                            String url = contextPath + "/handle/"+dso.getHandle();
                            item.addXref(url,P_return_Back_CommunityHomepage);
                        }
                    }
                    if (dso instanceof Collection)
                    {
                        //Get the Top Download list for special time

                        HashMap theSpecialTimeTopDownloads = statsDAO.getTopDownloadedItemsInSpecialTimeInCollection(dso.getID(),10, startDay, endDay);

                        //Only display it if it is *not* empty
                        if(theSpecialTimeTopDownloads!=null && !theSpecialTimeTopDownloads.isEmpty())
                        {
                            Division topTenListDiv = body.addDivision("theSpecialTime-top-download-list", "theSpecialTime-top-download-list");

                            /* create an ordered list of top downloads*/
                            List list = topTenListDiv.addList("top-download-list-theSpecialTime", List.TYPE_ORDERED);
                            list.setHead("  from : "+ startDay + " to : "+ endDay );

                            //Loop through each item
                            Iterator iterator = theSpecialTimeTopDownloads.keySet().iterator();
                            while(iterator.hasNext())
                            {
                                //get reference to item object
                                Long itemID = (Long) iterator.next();
                                Long downloadCount = (Long) theSpecialTimeTopDownloads.get(itemID);
                                Item downloadItem = Item.find(context, itemID.intValue());


                                /** Get Title **/
                                Metadatum[] titleDC = downloadItem.getMetadata("dc", "title", null, Item.ANY);

                                String url = contextPath + "/handle/" + downloadItem.getHandle();
                                org.dspace.app.xmlui.wing.element.Item item = list.addItem("theSpecialTime-top-download-list-item","theSpecialTime-top-download-list-item");

                                //add link to item to list
                                if (titleDC.length == 0)
                                {   // If there is no title, use "Untitled"
                                    item.addXref(url).addContent(P_item_top_download.parameterize(T_untitled, downloadCount));
                                }
                                else
                                {
                                    item.addXref(url).addContent(P_item_top_download.parameterize(titleDC[0].value, downloadCount));
                                }
                            }
                        }

                        //Create a return link to the collection home page
                        {
                            Division statsDiv = body.addDivision("download-statistics", "download-statistics");
                            List statsList = statsDiv.addList("statistics-collection-list");
                            org.dspace.app.xmlui.wing.element.Item item = statsList.addItem("Return", null);
                            String url = contextPath + "/handle/"+dso.getHandle();
                            item.addXref(url,P_return_Back_CollectionHomepage);
                        }
                    }
                }
               //month isn't selected bue year is selected
                else if (((month.equals("0"))&&!(year.equals("0"))))
                {
                    int theYear = Integer.parseInt(year);
                    Date startDay = statsDAO.getStartOftheSpecialtime(theYear, 0);
                    Date endDay = statsDAO.getEndOftheSpecialtime(theYear, 11);

                    // Added the community and collection name As the report title
                    if (!(dso instanceof Community)&&!(dso instanceof Collection))
                    {
                        return;
                    }
                    else if (dso instanceof Community)
                    {
                        Community community = (Community) dso;
                        Division statsDiv = body.addDivision("historydownload-statsreportview", "historydownload-statsreportview");
                        List statsList = statsDiv.addList("statistics-site-list");
                        String name = community.getMetadata("name");
                        if (name == null || name.length() == 0)
                            statsList.setHead(T_untitled);
                        else
                        {
                            name = name + " ( from : "+ startDay + " To : "+ endDay + " )";
                            statsList.setHead(T_head_history_download_statsreport.parameterize(name));
                        }

                    }
                    else if (dso instanceof Collection)
                    {
                        Collection collection = (Collection) dso;
                        Division statsDiv = body.addDivision("historydownload-statsreportview","historydownload-statsreportview");
                        List statsList = statsDiv.addList("statistics-site-list");
                        String name = collection.getMetadata("name");
                        if (name == null || name.length() == 0)
                            statsList.setHead(T_untitled);
                        else
                        {
                            name = name + " ( from : "+ startDay + " To : "+ endDay + " )";
                            statsList.setHead(T_head_history_download_statsreport.parameterize(name));
                        }
                    }
                    
                    if (dso instanceof Community)
                    {

                        //Get the Top Download list for special time

                        HashMap theSpecialTimeTopDownloads = statsDAO.getTopDownloadedItemsInSpecialTimeInCommunity(dso.getID(),10, startDay, endDay);

                        //Only display it if it is *not* empty
                        if(theSpecialTimeTopDownloads!=null && !theSpecialTimeTopDownloads.isEmpty())
                        {
                            Division topTenListDiv = body.addDivision("theSpecialTime-top-download-list", "theSpecialTime-top-download-list");

                            /* create an ordered list of top downloads*/
                            List list = topTenListDiv.addList("top-download-list-theSpecialTime", List.TYPE_ORDERED);
                            list.setHead("  from : "+ startDay + " to : "+ endDay );

                            //Loop through each item
                            Iterator iterator = theSpecialTimeTopDownloads.keySet().iterator();
                            while(iterator.hasNext())
                            {
                                //get reference to item object
                                Long itemID = (Long) iterator.next();
                                Long downloadCount = (Long) theSpecialTimeTopDownloads.get(itemID);
                                Item downloadItem = Item.find(context, itemID.intValue());


                                /** Get Title **/
                                Metadatum[] titleDC = downloadItem.getMetadata("dc", "title", null, Item.ANY);

                                String url = contextPath + "/handle/" + downloadItem.getHandle();
                                org.dspace.app.xmlui.wing.element.Item item = list.addItem("theSpecialTime-top-download-list-item","theSpecialTime-top-download-list-item");

                                //add link to item to list
                                if (titleDC.length == 0)
                                {   // If there is no title, use "Untitled"
                                    item.addXref(url).addContent(P_item_top_download.parameterize(T_untitled, downloadCount));
                                }
                                else
                                {
                                    item.addXref(url).addContent(P_item_top_download.parameterize(titleDC[0].value, downloadCount));
                                }
                            }
                        }

                        //Create a return link to the community home page
                        {
                            Division statsDiv = body.addDivision("download-statistics", "download-statistics");
                            List statsList = statsDiv.addList("statistics-community-list");
                            org.dspace.app.xmlui.wing.element.Item item = statsList.addItem("Return", null);
                            String url = contextPath + "/handle/"+dso.getHandle();
                            item.addXref(url,P_return_Back_CommunityHomepage);
                        }
                    }
                    if (dso instanceof Collection)
                    {

                        //Get the Top Download list for special time

                        HashMap theSpecialTimeTopDownloads = statsDAO.getTopDownloadedItemsInSpecialTimeInCollection(dso.getID(),10, startDay, endDay);

                        //Only display it if it is *not* empty
                        if(theSpecialTimeTopDownloads!=null && !theSpecialTimeTopDownloads.isEmpty())
                        {
                            Division topTenListDiv = body.addDivision("theSpecialTime-top-download-list", "theSpecialTime-top-download-list");

                            /* create an ordered list of top downloads*/
                            List list = topTenListDiv.addList("top-download-list-theSpecialTime", List.TYPE_ORDERED);
                            list.setHead("  from : "+ startDay + " to : "+ endDay );

                            //Loop through each item
                            Iterator iterator = theSpecialTimeTopDownloads.keySet().iterator();
                            while(iterator.hasNext())
                            {
                                //get reference to item object
                                Long itemID = (Long) iterator.next();
                                Long downloadCount = (Long) theSpecialTimeTopDownloads.get(itemID);
                                Item downloadItem = Item.find(context, itemID.intValue());


                                /** Get Title **/
                                Metadatum[] titleDC = downloadItem.getMetadata("dc", "title", null, Item.ANY);

                                String url = contextPath + "/handle/" + downloadItem.getHandle();
                                org.dspace.app.xmlui.wing.element.Item item = list.addItem("theSpecialTime-top-download-list-item","theSpecialTime-top-download-list-item");

                                //add link to item to list
                                if (titleDC.length == 0)
                                {   // If there is no title, use "Untitled"
                                    item.addXref(url).addContent(P_item_top_download.parameterize(T_untitled, downloadCount));
                                }
                                else
                                {
                                    item.addXref(url).addContent(P_item_top_download.parameterize(titleDC[0].value, downloadCount));
                                }
                            }
                        }

                        //Create a return link to the collection home page
                        {
                            Division statsDiv = body.addDivision("download-statistics", "download-statistics");
                            List statsList = statsDiv.addList("statistics-collection-list");
                            org.dspace.app.xmlui.wing.element.Item item = statsList.addItem("Return", null);
                            String url = contextPath + "/handle/"+dso.getHandle();
                            item.addXref(url,P_return_Back_CollectionHomepage);
                        }
                    }
                }
               // month is selected but year isn't selected
                else if ((!(month.equals("0"))&&(year.equals("0"))))
                {
                    int theYear = statsDAO.getCurrentYear();
                    int theMonth = Integer.parseInt(month)-1;
                    Date startDay = statsDAO.getStartOftheSpecialtime(theYear, theMonth);
                    Date endDay = statsDAO.getEndOftheSpecialtime(theYear, theMonth);

                    // Added the community and collection name As the report title
                    if (!(dso instanceof Community)&&!(dso instanceof Collection))
                    {
                        return;
                    }
                    else if (dso instanceof Community)
                    {
                        Community community = (Community) dso;
                        Division statsDiv = body.addDivision("historydownload-statsreportview", "historydownload-statsreportview");
                        List statsList = statsDiv.addList("statistics-site-list");
                        String name = community.getMetadata("name");
                        if (name == null || name.length() == 0)
                            statsList.setHead(T_untitled);
                        else
                        {
                            name = name + " ( from : "+ startDay + " To : "+ endDay + " )";
                            statsList.setHead(T_head_history_download_statsreport.parameterize(name));
                        }

                    }
                    else if (dso instanceof Collection)
                    {
                        Collection collection = (Collection) dso;
                        Division statsDiv = body.addDivision("historydownload-statsreportview","historydownload-statsreportview");
                        List statsList = statsDiv.addList("statistics-site-list");
                        String name = collection.getMetadata("name");
                        if (name == null || name.length() == 0)
                            statsList.setHead(T_untitled);
                        else
                        {
                            name = name + " ( from : "+ startDay + " To : "+ endDay + " )";
                            statsList.setHead(T_head_history_download_statsreport.parameterize(name));
                        }
                    }

                    if (dso instanceof Community)
                    {

                        //Get the Top Download list for special time

                        HashMap theSpecialTimeTopDownloads = statsDAO.getTopDownloadedItemsInSpecialTimeInCommunity(dso.getID(),10, startDay, endDay);

                        //Only display it if it is *not* empty
                        if(theSpecialTimeTopDownloads!=null && !theSpecialTimeTopDownloads.isEmpty())
                        {
                            Division topTenListDiv = body.addDivision("theSpecialTime-top-download-list", "theSpecialTime-top-download-list");

                            /* create an ordered list of top downloads*/
                            List list = topTenListDiv.addList("top-download-list-theSpecialTime", List.TYPE_ORDERED);
                            list.setHead("  from : "+ startDay + " to : "+ endDay );

                            //Loop through each item
                            Iterator iterator = theSpecialTimeTopDownloads.keySet().iterator();
                            while(iterator.hasNext())
                            {
                                //get reference to item object
                                Long itemID = (Long) iterator.next();
                                Long downloadCount = (Long) theSpecialTimeTopDownloads.get(itemID);
                                Item downloadItem = Item.find(context, itemID.intValue());


                                /** Get Title **/
                                Metadatum[] titleDC = downloadItem.getMetadata("dc", "title", null, Item.ANY);

                                String url = contextPath + "/handle/" + downloadItem.getHandle();
                                org.dspace.app.xmlui.wing.element.Item item = list.addItem("theSpecialTime-top-download-list-item","theSpecialTime-top-download-list-item");

                                //add link to item to list
                                if (titleDC.length == 0)
                                {   // If there is no title, use "Untitled"
                                    item.addXref(url).addContent(P_item_top_download.parameterize(T_untitled, downloadCount));
                                }
                                else
                                {
                                    item.addXref(url).addContent(P_item_top_download.parameterize(titleDC[0].value, downloadCount));
                                }
                            }
                        }

                        //Create a return link to the community home page
                        {
                            Division statsDiv = body.addDivision("download-statistics", "download-statistics");
                            List statsList = statsDiv.addList("statistics-community-list");
                            org.dspace.app.xmlui.wing.element.Item item = statsList.addItem("Return", null);
                            String url = contextPath + "/handle/"+dso.getHandle();
                            item.addXref(url,P_return_Back_CommunityHomepage);
                        }
                    }
                    if (dso instanceof Collection)
                    {

                        //Get the Top Download list for special time
                        HashMap theSpecialTimeTopDownloads = statsDAO.getTopDownloadedItemsInSpecialTimeInCollection(dso.getID(),10, startDay, endDay);

                        //Only display it if it is *not* empty
                        if(theSpecialTimeTopDownloads!=null && !theSpecialTimeTopDownloads.isEmpty())
                        {
                            Division topTenListDiv = body.addDivision("theSpecialTime-top-download-list", "theSpecialTime-top-download-list");

                            /* create an ordered list of top downloads*/
                            List list = topTenListDiv.addList("top-download-list-theSpecialTime", List.TYPE_ORDERED);
                            list.setHead("  from : "+ startDay + " to : "+ endDay );
                            //Loop through each item
                            Iterator iterator = theSpecialTimeTopDownloads.keySet().iterator();
                            while(iterator.hasNext())
                            {
                                //get reference to item object
                                Long itemID = (Long) iterator.next();
                                Long downloadCount = (Long) theSpecialTimeTopDownloads.get(itemID);
                                Item downloadItem = Item.find(context, itemID.intValue());


                                /** Get Title **/
                                Metadatum[] titleDC = downloadItem.getMetadata("dc", "title", null, Item.ANY);

                                String url = contextPath + "/handle/" + downloadItem.getHandle();
                                org.dspace.app.xmlui.wing.element.Item item = list.addItem("theSpecialTime-top-download-list-item","theSpecialTime-top-download-list-item");

                                //add link to item to list
                                if (titleDC.length == 0)
                                {   // If there is no title, use "Untitled"
                                    item.addXref(url).addContent(P_item_top_download.parameterize(T_untitled, downloadCount));
                                }
                                else
                                {
                                    item.addXref(url).addContent(P_item_top_download.parameterize(titleDC[0].value, downloadCount));
                                }
                            }
                        }

                        //Create a return link to the collection home page
                        {
                            Division statsDiv = body.addDivision("download-statistics", "download-statistics");
                            List statsList = statsDiv.addList("statistics-collection-list");
                            org.dspace.app.xmlui.wing.element.Item item = statsList.addItem("Return", null);
                            String url = contextPath + "/handle/"+dso.getHandle();
                            item.addXref(url,P_return_Back_CollectionHomepage);
                        }
                    }
                }
              }
            
       }


	/**
	 * Recycle
	 */
    public void recycle()
    {
    // Clear out our item's cache.
    	this.validity = null;
    	super.recycle();
    }

        /**
     * Makes the jump-list navigation for the results
     *
     * @param div
     * @param info
     * @param params
     * @throws WingException
     */
    //The code is gotten from ConfigurableBrowse.java, adapted by UIUC
    private void addBrowseJumpNavigation(Division div)
            throws WingException, SQLException
    {

        // Prepare a Map of query parameters required for all links
        Map<String, String> queryParams = new HashMap<String, String>();

        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

        String url = contextPath + "/handle/"+dso.getHandle()+"/report";
        Division jump = div.addInteractiveDivision("browse-navigation", url,
                Division.METHOD_POST, "secondary navigation");

        // Add all the query parameters as hidden fields on the form
        for (String key : queryParams.keySet())
            jump.addHidden(key).setValue(queryParams.get(key));

        // If this is a date based browse, render the date navigation

        {
             Para jumpForm = jump.addPara();

            // Create a select list to choose a month
            jumpForm.addContent(T_jump_select);
            Select month = jumpForm.addSelect(BrowseParams.MONTH);

            month.addOption(false, 0, T_choose_month);

            for (int i = 1; i <= 12; i++)
            {
                month.addOption(false, String.valueOf(i), DCDate.getMonthName(i, Locale
                        .getDefault()));
            }

            // Create a dropdown list to choose a year
            Select year = jumpForm.addSelect(BrowseParams.YEAR);
            year.addOption(false, 0, T_choose_year);
            int currentYear = DCDate.getCurrent().getYear();
            int theYear = currentYear;
            int startYear = 2006;

            // Calculate from current year to start year of IDEALS
            while (theYear >= startYear)
            {
                year.addOption(false, String.valueOf(theYear), String.valueOf(theYear));
                theYear--;
            }
            jumpForm.addButton("submit").setValue(T_go);
            
        }
    }

    /*
     * Helper class to track browse parameters
     */
    class BrowseParams
    {
        String month;
        String year;

        final static String MONTH = "month";
        final static String YEAR = "year";
    }
}
