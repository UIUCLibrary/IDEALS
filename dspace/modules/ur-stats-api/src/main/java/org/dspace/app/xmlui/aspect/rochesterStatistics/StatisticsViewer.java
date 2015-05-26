package org.dspace.app.xmlui.aspect.rochesterStatistics;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.Para;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.ReferenceSet;

import org.dspace.authorize.AuthorizeException;
import org.xml.sax.SAXException;
import edu.ur.dspace.stats.StatsDAO;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Metadatum;
import org.dspace.content.Item;
import org.dspace.content.DSpaceObject;

import org.dspace.browse.BrowseItem;
import org.dspace.core.ConfigurationManager;
import org.dspace.browse.BrowserScope;
import org.dspace.browse.BrowseIndex;
import org.dspace.sort.SortOption;
import org.dspace.browse.BrowseEngine;
import org.dspace.sort.SortException;
import org.dspace.browse.BrowseException;
import org.dspace.app.xmlui.cocoon.DSpaceFeedGenerator;
import org.apache.log4j.Logger;
import org.apache.excalibur.source.SourceValidity;
import org.dspace.app.xmlui.utils.DSpaceValidity;

/**
 * This StatisticsViewer generates the DRI XML which displays
 * current download statistics throughout the repository.
 *
 * @author Zhimin Chen (U of Illinois)
 * @author Tim Donohue (U of Illinois)
 **/
public class StatisticsViewer extends AbstractDSpaceTransformer
{

	private static final Logger log = Logger.getLogger(DSpaceFeedGenerator.class);

	/** Site Wide Statistics**/
	private static final Message T_head_site_download_statistics =
	    message("xmlui.RochesterStatistics.StatisticsViewer.head_site_stats");
	private static final Message P_site_downloads_total =
	    message("xmlui.RochesterStatistics.StatisticsViewer.site_stats_total");
    private static final Message P_site_downloads_month =
	   	message("xmlui.RochesterStatistics.StatisticsViewer.site_stats_month");
	private static final Message P_site_downloads_today =
	   	message("xmlui.RochesterStatistics.StatisticsViewer.site_stats_today");

	/** Community-Level Statistics**/
	 private static final Message T_head_community_download_statistics =
	    message("xmlui.RochesterStatistics.StatisticsViewer.head_community_stats");
    private static final Message P_community_downloads_total =
    	message("xmlui.RochesterStatistics.StatisticsViewer.community_stats_total");
    private static final Message P_community_downloads_month =
    	message("xmlui.RochesterStatistics.StatisticsViewer.community_stats_month");
    private static final Message P_community_downloads_today =
    	message("xmlui.RochesterStatistics.StatisticsViewer.community_stats_today");
    private static final Message P_community_downloads_report =
        message("xmlui.RochesterStatistics.StatisticsViewer.community_stats_report");

    /** Collection-level Statistics**/
    private static final Message T_head_collection_download_statistics =
    	message("xmlui.RochesterStatistics.StatisticsViewer.head_collection_stats");
    private static final Message P_collection_downloads_total =
    	message("xmlui.RochesterStatistics.StatisticsViewer.collection_stats_total");
    private static final Message P_collection_downloads_month =
    	message("xmlui.RochesterStatistics.StatisticsViewer.collection_stats_month");
    private static final Message P_collection_downloads_today =
    	message("xmlui.RochesterStatistics.StatisticsViewer.collection_stats_today");
    private static final Message P_collection_downloads_report =
        message("xmlui.RochesterStatistics.StatisticsViewer.collection_stats_report");

    /** Item-level Statistics**/
    private static final Message T_head_item_download_statistics=
    	message("xmlui.RochesterStatistics.StatisticsViewer.head_item_stats");
    private static final Message P_item_downloads_total =
    	message("xmlui.RochesterStatistics.StatisticsViewer.item_stats_total");
    private static final Message P_item_downloads_month =
    	message("xmlui.RochesterStatistics.StatisticsViewer.item_stats_month");
    private static final Message P_item_downloads_today =
    	message("xmlui.RochesterStatistics.StatisticsViewer.item_stats_today");

    /** Top Downloads Listing **/
    private static final Message T_head_top_downloads =
    	message("xmlui.RochesterStatistics.StatisticsViewer.head_top_downloads");

    private static final Message P_item_top_download =
    	message("xmlui.RochesterStatistics.StatisticsViewer.item_top_download");

    /** Recent Submissions Listing (on Homepage) **/
    private static final Message T_head_recent_submissions =
        message("xmlui.RochesterStatistics.StatisticsViewer.head_recent_submissions");


    private static final Message T_untitled =
		message("xmlui.general.untitled");

    /** How many recent submissions to include in the page */
    private static final int RECENT_SUBMISSIONS = 5;

    /** The cache of recently submitted items */
    private java.util.List<BrowseItem> recentSubmissionItems;

    /** Cached validity object */
    private SourceValidity validity;
  //sublist


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
	            for(BrowseItem item : getRecentlySubmittedIems())
	            {
	                validity.add(item);
	            }

	            this.validity = validity.complete();
	        }
	        catch (Exception e)
	        {
	            // Just ignore all errors and return an invalid cache.
	        }
    	}
    	return this.validity;
    }

    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {
    	StatsDAO statsDAO = new StatsDAO();

    	DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

        if (dso != null)
        {

        	//Community Level Statistics
        	if (dso instanceof Community)
            {
        		Division statsDiv = body.addDivision("download-statistics", "download-statistics");

//                String name = ((Community) dso).getMetadata("name");

	            List statsList = statsDiv.addList("statistics-community-list");
	   		 	statsList.setHead(T_head_community_download_statistics);

	            org.dspace.app.xmlui.wing.element.Item item1 = statsList.addItem("statistics-community-total", null);
	            long totalCount = statsDAO.getCommunityDownloadCount(dso.getID());
	            Long totalStats = Long.valueOf(totalCount);
	            item1.addContent(P_community_downloads_total.parameterize(totalStats));

	            org.dspace.app.xmlui.wing.element.Item item2 = statsList.addItem("statistics-community-month", null);
	            long monthCount = statsDAO.getMonthCommunityDownloadCount(dso.getID());
	            Long monthStats = Long.valueOf(monthCount);
	            item2.addContent(P_community_downloads_month.parameterize(monthStats));

                org.dspace.app.xmlui.wing.element.Item item3 = statsList.addItem("statistics-community-today", null);
	            long todayCount = statsDAO.getTodayCommunityDownloadCount(dso.getID());
	            Long todayStats = Long.valueOf(todayCount);
	            item3.addContent(P_community_downloads_today.parameterize(todayStats));

                org.dspace.app.xmlui.wing.element.Item item4 = statsList.addItem("Statictid-Report", null);
                String url = contextPath + "/handle/"+dso.getHandle()+"/report";
                item4.addXref(url,P_community_downloads_report);
	            
            }

        	//Collection Level Statistics
            if (dso instanceof Collection)
            {
            	Division statsDiv = body.addDivision("download-statistics", "download-statistics");

	            List statsList = statsDiv.addList("statistics-collection-list");
	   		 	statsList.setHead(T_head_collection_download_statistics);

	            org.dspace.app.xmlui.wing.element.Item item1 = statsList.addItem("statistics-collection-total", null);
	            long totalCount = statsDAO.getCollectionDownloadCount(dso.getID());
	            Long totalStats = Long.valueOf(totalCount);
	            item1.addContent(P_collection_downloads_total.parameterize(totalStats));

	            org.dspace.app.xmlui.wing.element.Item item2 = statsList.addItem("statistics-collection-month", null);
	            long monthCount = statsDAO.getMonthCollectionDownloadCount(dso.getID());
	            Long monthStats = Long.valueOf(monthCount);
	            item2.addContent(P_collection_downloads_month.parameterize(monthStats));

	            org.dspace.app.xmlui.wing.element.Item item3 = statsList.addItem("statistics-collection-today", null);
	            long todayCount = statsDAO.getTodayCollectionDownloadCount(dso.getID());
	            Long todayStats = Long.valueOf(todayCount);
	            item3.addContent(P_collection_downloads_today.parameterize(todayStats));

                org.dspace.app.xmlui.wing.element.Item item4 = statsList.addItem("Statictid-Report", null);
                String url = contextPath + "/handle/"+dso.getHandle()+"/report";
                item4.addXref(url,P_collection_downloads_report);
            }

            //Item Level Statistics
            if (dso instanceof Item)
            {
            	Division statsDiv = body.addDivision("download-statistics", "download-statistics");

	            List statsList = statsDiv.addList("statistics-item-list");
	   		 	statsList.setHead(T_head_item_download_statistics);

	            org.dspace.app.xmlui.wing.element.Item item1 = statsList.addItem("statistics-item-total", null);
	            long totalCount = statsDAO.getItemDownloadCount(dso.getID());
	            Long totalStats = Long.valueOf(totalCount);
	            item1.addContent(P_item_downloads_total.parameterize(totalStats));

	            org.dspace.app.xmlui.wing.element.Item item2 = statsList.addItem("statistics-item-month", null);
	            long monthCount = statsDAO.getMonthItemDownloadCount(dso.getID());
	            Long monthStats = Long.valueOf(monthCount);
	            item2.addContent(P_item_downloads_month.parameterize(monthStats));

	            org.dspace.app.xmlui.wing.element.Item item3 = statsList.addItem("statistics-item-today", null);
	            long todayCount = statsDAO.getTodayItemDownloadCount(dso.getID());
	            Long todayStats = Long.valueOf(todayCount);
	            item3.addContent(P_item_downloads_today.parameterize(todayStats));
            }

        }
        //Site Wide Statistics
        else
        {
        	//Top Download Listing (on homepage)
        	{
        		//Get the Top Download list for current month
                HashMap topDownloads = statsDAO.getMonthlyTopDownloadedItems(10);

	   		 	//Only display it if it is *not* empty
	   		 	if(topDownloads!=null && !topDownloads.isEmpty())
	        	{
		   		 	Division topTenListDiv = body.addDivision("top-download-list", "top-download-list");

		   		 	/* create an ordered list of top downloads*/
		   		 	List list = topTenListDiv.addList("top-download-list-monthly", List.TYPE_ORDERED);
		   		 	list.setHead(T_head_top_downloads);

		   		 	//Loop through each item
					Iterator iterator = topDownloads.keySet().iterator();
			        while(iterator.hasNext())
			        {
			        	//get reference to item object
			        	Long itemID = (Long) iterator.next();
						Long downloadCount = (Long) topDownloads.get(itemID);
			        	Item downloadItem = Item.find(context, itemID.intValue());


						BrowseItem browseItem = new BrowseItem(context,
                                                               downloadItem.getID(),
															   downloadItem.isArchived(),
                                                               downloadItem.isWithdrawn(),
                                                               downloadItem.isDiscoverable());
			        	/** Get Title **/
			        	Metadatum[] titleDC = downloadItem.getMetadata("dc", "title", null, Item.ANY);

			        	String url = contextPath + "/handle/" + downloadItem.getHandle();
			        	org.dspace.app.xmlui.wing.element.Item item = list.addItem("top-download-list-item","top-download-list-item");

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

        	}//end top downloads list

        	//Recent Submissions Listing (on homepage)
        	{

        		 // Recently submitted items
                {

                    java.util.List<BrowseItem> items = getRecentlySubmittedIems();

                    Division lastSubmittedDiv = body
                            .addDivision("site-recent-submission","main recent-submission");
                    lastSubmittedDiv.setHead(T_head_recent_submissions);
                    ReferenceSet lastSubmitted = lastSubmittedDiv.addReferenceSet(
                            "site-last-submitted", ReferenceSet.TYPE_SUMMARY_LIST,
                            null, "recent-submissions");
                    for (BrowseItem item : items)
                    {
                        lastSubmitted.addReference(item);
                    }
                }

        	}
        	//end of recent submissions

        	//Site-level Statistics
        	{
	        	Division statsDiv = body.addDivision("download-statistics", "download-statistics");

	            List statsList = statsDiv.addList("statistics-site-list");
	   		 	statsList.setHead(T_head_site_download_statistics);

	            org.dspace.app.xmlui.wing.element.Item item1 = statsList.addItem("statistics-site-total", null);
	            long totalCount = statsDAO.getDSpaceDownloadCount();
	            Long totalStats = Long.valueOf(totalCount);
	            item1.addContent(P_site_downloads_total.parameterize(totalStats));

	            org.dspace.app.xmlui.wing.element.Item item2 = statsList.addItem("statistics-site-month", null);
	            long monthCount = statsDAO.getMonthDSpaceDownloadCount();
	            Long monthStats = Long.valueOf(monthCount);
	            item2.addContent(P_site_downloads_month.parameterize(monthStats));

	            org.dspace.app.xmlui.wing.element.Item item3 = statsList.addItem("statistics-site-today", null);
	            long todayCount = statsDAO.getTodayDSpaceDownloadCount();
	            Long todayStats = Long.valueOf(todayCount);
	            item3.addContent(P_site_downloads_today.parameterize(todayStats));
        	}
        }
    }


    /**
     * Retrieves a list of all Recently Submitted Items to the Repository
     * @return List of BrowseItem objects
     * @throws SQLException
     */
    private java.util.List<BrowseItem> getRecentlySubmittedIems()
    throws SQLException
    {
    	if (recentSubmissionItems != null)
        return recentSubmissionItems;

    	String source = ConfigurationManager.getProperty("recent.submissions.sort-option");

    	BrowserScope scope = new BrowserScope(context);
    	scope.setResultsPerPage(RECENT_SUBMISSIONS);

    	// FIXME Exception Handling
    	try
    	{
    		scope.setBrowseIndex(BrowseIndex.getItemBrowseIndex());
    		for (SortOption so : SortOption.getSortOptions())
    		{
    				if (so.getName().equals(source))
    				{
    					scope.setSortBy(so.getNumber());
    					scope.setOrder(SortOption.DESCENDING);
    				}
    		}

    		BrowseEngine be = new BrowseEngine(context);
    		this.recentSubmissionItems = be.browse(scope).getResults();
    	}
    	catch (SortException se)
    	{
    		log.error("Caught SortException", se);
    	}
    	catch (BrowseException bex)
    	{
    		log.error("Caught BrowseException", bex);
    	}

    	return this.recentSubmissionItems;
    }


	/**
	 * Recycle
	 */
    public void recycle()
    {
    // Clear out our item's cache.
    	this.recentSubmissionItems = null;
    	this.validity = null;
    	super.recycle();
    }

}
