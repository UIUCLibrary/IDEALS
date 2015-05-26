/*
 *
 * Version: $Revision$
 *
 * Date: $Date$
 *
 * Copyright (c) 2002, Hewlett-Packard Company, Massachusetts
 * Institute of Technology, University of Rochester.  All rights reserved.
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

package edu.ur.dspace.stats;

import java.util.GregorianCalendar;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Calendar;
import org.dspace.storage.rdbms.DatabaseManager;

/**
 * @author Nathan Sarr (U of Rochester)
 * @author Tim Donohue (U of Illinois)
 * @author Zhimin Chen (U of Illinois)
 *
 * This class knows how to persist Stats information 
 * as well as retrieve the stats information
 * <P>
 * It stores this information in two primary tables:
 * <LI>ip_bitstream_stats - latest downloads, with IP
 * <LI>monthly_bitstream_stats - final download counts (IP-removed), month-by-month
 * <P>
 * In addition, the ip_bitstream_stats table should be
 * occasionally cleared (otherwise its size will increase 
 * drastically and cause performance problems), by using
 * CleanIPStats script.
 * 
 */
public class StatsDAO
{
   // logger
   private static Logger log = Logger.getLogger(StatsDAO.class);
   
   // Object to synchronize a lock on in order to be sure that
   // two threads cannot attempt to update the download count at the same time
   private static Object lockObject = StatsDAO.class;

   // we don't want to count logo downloads or any streams that
   // are not linked to bundles
   protected static final String IS_LOGO_BITSTREAM  = "select true " +
       "from bitstream where bitstream_id = ? " + 
       "and ? not in ( select bitstream_id from bundle2bitstream where " +
       "bundle2bitstream.bitstream_id = ?)"; 
   
   // fields needed for insert
   protected static final String FIELDS_INSERT = 
       "item_id, bitstream_id, day, ip_address, count "; 

   // insert a new bitstream for a given month
   protected static final String INSERT_SQL =
       "insert into ip_bitstream_stats ( " + FIELDS_INSERT + " ) values (?,?,?,?,?);";

   // update the count (NOT USED BY IDEALS -- We only count one download per day!)
   /*protected static String UPDATE_SQL =
       "update ip_bitstream_stats set count = count + 1 " +
       "where day = ? " +
       "and item_id = ? " +
       "and bitstream_id = ? " +
       "and ip_address = ?;"; */
   
   // Check if bitstream was already downloaded once on a given day by given IP address
   protected static final String ALREADY_DOWNLOADED  = "select true " +
   "from ip_bitstream_stats " +
   "where day = ? " +
   "and item_id = ? " +
   "and bitstream_id = ? " +
   "and ip_address = ?;"; 
   
   // find the total download count for all of dspace
   protected static final String DSPACE_TOTAL_COUNT = "select sum(count) " +
   "from monthly_bitstream_stats;";
   
   // find the total download count for all of dspace in a given month
   protected static final String DSPACE_MONTH_COUNT = "select sum(count) " +
   "from monthly_bitstream_stats " +
   "where month = ? ;";
   
   // WARNING: THIS COUNT MAY BE INACCURATE FOR LARGE DATE RANGES,
   // since 'ip_bitstreams_stats' table is cleaned on occasion
   protected static final  String DSPACE_RECENT_DATE_RANGE_COUNT = "select sum(count) from ip_bitstream_stats where " +
   "day between ?  and  ? " +
   "and not exists ( select true from ip_address_ignore where ip_address_ignore.ip_address=ip_bitstream_stats.ip_address );";

   
   
   // find the total download count for a community
   protected static final String COMMUNITY_TOTAL_COUNT = "select sum(count) " +
   "from monthly_bitstream_stats, " +
   "communities2item " + 
   "where monthly_bitstream_stats.item_id = communities2item.item_id " +
   "and communities2item.community_id = ?;";

   // find the total download count for a community for given month
   protected static final String COMMUNITY_MONTH_COUNT = "select sum(count) " +
   "from monthly_bitstream_stats, " +
   "communities2item " + 
   "where monthly_bitstream_stats.item_id = communities2item.item_id " +
   "and communities2item.community_id = ? "  + 
   "and month = ? ;";
   
   // find the total download count for a community for a specified date range
   // WARNING: THIS COUNT MAY BE INACCURATE FOR LARGE DATE RANGES,
   // since 'ip_bitstreams_stats' table is cleaned on occasion
   protected static final String COMMUNITY_RECENT_DATE_RANGE_COUNT = "select sum(ip_bitstream_stats.count) " +
   "from ip_bitstream_stats, " +
   "communities2item " + 
   "where ip_bitstream_stats.item_id = communities2item.item_id " +
   "and communities2item.community_id = ? "  + 
   "and day between ?  and  ? " + 
   "and not exists ( select true from ip_address_ignore where ip_address_ignore.ip_address=ip_bitstream_stats.ip_address );";


   
   // find the total download count for a collection
   protected static final String COLLECTION_TOTAL_COUNT = "select sum(count) " + 
   "from monthly_bitstream_stats, " +
   "collection2item " +
   "where monthly_bitstream_stats.item_id = collection2item.item_id " +
   "and collection2item.collection_id = ? ;";
   
   // find the total download count for a collection for given month
   protected static final String COLLECTION_MONTH_COUNT = "select sum(count) " +
   "from monthly_bitstream_stats, " +
   "collection2item " + 
   "where monthly_bitstream_stats.item_id = collection2item.item_id " +
   "and collection2item.collection_id = ? "  + 
   "and month = ? ;";
   
   // find the total download count for a collection in date range
   // WARNING: THIS COUNT MAY BE INACCURATE FOR LARGE DATE RANGES,
   // since 'ip_bitstreams_stats' table is cleaned on occasion
   protected static final String COLLECTION_RECENT_DATE_RANGE_COUNT = "select sum(ip_bitstream_stats.count) " + 
   "from ip_bitstream_stats, " +
   "collection2item " +
   "where ip_bitstream_stats.item_id = collection2item.item_id " +
   "and collection2item.collection_id = ? " + 
   "and day between ?  and  ? " + 
   "and not exists ( select true from ip_address_ignore where ip_address_ignore.ip_address=ip_bitstream_stats.ip_address );";
   
   // find the total download count for an item
   protected static final String ITEM_TOTAL_COUNT = "select sum(count) " +
   "from monthly_bitstream_stats " +
   "where item_id = ? ;";

   // find the total download count for a item for given month
   protected static final String ITEM_MONTH_COUNT = "select sum(count) " +
   "from monthly_bitstream_stats " +
   "where item_id = ? " +
   "and month = ? ;";
   
   // find the total download count for an item in date range
   // WARNING: THIS COUNT MAY BE INACCURATE FOR LARGE DATE RANGES,
   // since 'ip_bitstreams_stats' table is cleaned on occasion
   protected static final String ITEM_RECENT_DATE_RANGE_COUNT = "select sum(ip_bitstream_stats.count) " + 
   "from ip_bitstream_stats " +
   "where ip_bitstream_stats.item_id = ? " +
   "and day between ?  and  ? " + 
   "and not exists ( select true from ip_address_ignore where ip_address_ignore.ip_address=ip_bitstream_stats.ip_address );";
   

   // find the total download count for a bitstream
   protected static final String BITSTREAM_TOTAL_COUNT = "select sum(count) " +
   "from monthly_bitstream_stats " +
   "where bitstream_id = ?;";

   // find the total download count for a bitstream for given month
   protected static final String BITSTREAM_MONTH_COUNT = "select sum(count) " +
   "from monthly_bitstream_stats " +
   "where bitstream_id = ? " +
   "and month = ? ;";
   
   //Determine bitstream downloads for a given date range
   //Used to populate the 'monthly_bitstream_stats' table
   protected static final String BITSTREAM_DATE_RANGE_TOTALS = "select bitstream_id, item_id, sum(ip_bitstream_stats.count) " +
   "from ip_bitstream_stats " +
   "where day between ?  and  ? " +
   "and not exists ( select true from ip_address_ignore where ip_address_ignore.ip_address=ip_bitstream_stats.ip_address ) " +
   "group by bitstream_id, item_id;";


   /**************************************************************************************/
   /*                                                                                    */
   /*  The following three strings for finding most overall,yearly, monthly and           */
   /*   and specialtime download items in the IDEALS System                               */
   /*                                                                                    */
   /**************************************************************************************/

   // find most downloaded item (it will be the first item returned in result list)
   protected static final String TOP_DOWNLOAD_ITEMS = "select item_id, sum(count) " + 
   		"from monthly_bitstream_stats " +
   		"where item_id is not null " +
   		"group by item_id " +
   		"order by 2 DESC " +
   		"limit ?";

   //uiuc added(April 2)
   protected static final String TOP_DOWNLOAD_YEAR_ITEMS = "select item_id, sum(count) " +
		"from monthly_bitstream_stats " +
		"where item_id is not null " +
        "and month between ?  and ? " +
		"group by item_id " +
		"order by 2 DESC " +
		"limit ? ";

   protected static final String TOP_DOWNLOAD_MONTH_ITEMS = "select item_id, sum(count) " +
		"from monthly_bitstream_stats " +
		"where item_id is not null " +
		"and month=? " +
		"group by item_id " +
		"order by 2 DESC " +
		"limit ?";

   //UIUC ADDED THE FOLLOWING STRINGS FOR COMMUNITY AND COLLECTION STATISTICS(StatsReportViewer)
   /******************************************************************************************************/
   /* The following three strings for finding most overall,yearly, monthly and specialtime in  community */
   /******************************************************************************************************/

   protected static final String TOP_DOWNLOAD_ITEMS_IN_COMMUNITY ="select monthly_bitstream_stats.item_id, sum(monthly_bitstream_stats.count) " +
           "from monthly_bitstream_stats, " +
           "communities2item " +
           "where monthly_bitstream_stats.item_id is not null " +
           "and monthly_bitstream_stats.item_id = communities2item.item_id " +
           "and communities2item.community_id = ?  "  +
           "group by monthly_bitstream_stats.item_id  "+
           "order by 2 DESC  " +
           "limit ?  ";

   protected static final String TOP_DOWNLOAD_YEAR_ITEMS_IN_COMMUNITY ="select monthly_bitstream_stats.item_id, sum(monthly_bitstream_stats.count) " +
           "from monthly_bitstream_stats, " +
           "communities2item  " +
           "where monthly_bitstream_stats.item_id is not null " +
           "and monthly_bitstream_stats.item_id  =  communities2item.item_id  " +
           "and communities2item.community_id = ?  "  +
           "and month between  ?  and ? " +
           "group by monthly_bitstream_stats.item_id  " +
           "order by 2 DESC " +
           "limit ? ";

   protected static final String TOP_DOWNLOAD_MONTH_ITEMS_IN_COMMUNITY = "select monthly_bitstream_stats.item_id, sum(monthly_bitstream_stats.count) " +
		"from monthly_bitstream_stats, " +
        "communities2item "+
		"where monthly_bitstream_stats.item_id is not null " +
        "and monthly_bitstream_stats.item_id = communities2item.item_id " +
        "and communities2item.community_id = ?  "  +
		"and month=?  " +
		"group by monthly_bitstream_stats.item_id  " +
		"order by 2 DESC  " +
		"limit ? ";

   protected static final String TOP_DOWNLOAD_SPECIALTIME_ITEMS_IN_COMMUNITY ="select monthly_bitstream_stats.item_id, sum(monthly_bitstream_stats.count) " +
           "from monthly_bitstream_stats, " +
           "communities2item  " +
           "where monthly_bitstream_stats.item_id is not null " +
           "and monthly_bitstream_stats.item_id  =  communities2item.item_id  " +
           "and communities2item.community_id = ?  "  +
           "and month between  ?  and ? " +
           "group by monthly_bitstream_stats.item_id  " +
           "order by 2 DESC " +
           "limit ? ";

    /*****************************************************************************************************/
    /* The following three strings for finding most overall,yearly, monthly in collection                */
    /*****************************************************************************************************/

   protected static final String TOP_DOWNLOAD_ITEMS_IN_COLLECTION ="select monthly_bitstream_stats.item_id, sum(monthly_bitstream_stats.count) " +
           "from monthly_bitstream_stats, " +
           "collection2item  " +
           "where monthly_bitstream_stats.item_id is not null " +
           "and monthly_bitstream_stats.item_id  =  collection2item.item_id  " +
           "and collection2item.collection_id = ?  "  +
           "group by monthly_bitstream_stats.item_id  " +
           "order by 2 DESC  " +
           "limit ?  ";

   protected static final String TOP_DOWNLOAD_YEAR_ITEMS_IN_COLLECTION ="select monthly_bitstream_stats.item_id, sum(monthly_bitstream_stats.count) " +
           "from monthly_bitstream_stats,  " +
           "collection2item  " +
           "where monthly_bitstream_stats.item_id is not null " +
           "and monthly_bitstream_stats.item_id = collection2item.item_id  " +
           "and collection2item.collection_id = ?  "  +
           "and month between ? and ?  " +
           "group by monthly_bitstream_stats.item_id  " +
           "order by 2 DESC  " +
           "limit ? ";

   protected static final String TOP_DOWNLOAD_MONTH_ITEMS_IN_COLLECTION = "select monthly_bitstream_stats.item_id, sum(monthly_bitstream_stats.count) " +
		"from monthly_bitstream_stats,  " +
        "collection2item "+
		"where monthly_bitstream_stats.item_id is not null " +
        "and monthly_bitstream_stats.item_id = collection2item.item_id  " +
        "and collection2item.collection_id = ?  "  +
		"and month=?  " +
		"group by monthly_bitstream_stats.item_id  " +
		"order by 2 DESC  " +
		"limit ? ";

   protected static final String TOP_DOWNLOAD_SPECIALTIME_ITEMS_IN_COLLECTION ="select monthly_bitstream_stats.item_id, sum(monthly_bitstream_stats.count) " +
           "from monthly_bitstream_stats,  " +
           "collection2item  " +
           "where monthly_bitstream_stats.item_id is not null " +
           "and monthly_bitstream_stats.item_id = collection2item.item_id  " +
           "and collection2item.collection_id = ?  "  +
           "and month between ? and ?  " +
           "group by monthly_bitstream_stats.item_id  " +
           "order by 2 DESC  " +
           "limit ? ";
  
   //UIUC modified - use the PostGres 'inet' function to correctly sort IP addresses
   protected static final String BITSTREAM_IP_DOWNLOADERS_DATE_RANGE = "Select * from ip_bitstream_stats " +
   "where day between ? and ? order by inet(ip_address), bitstream_id";
   
   //UIUC added - allows viewing of only downloads which are not already ignored
   protected static final String BITSTREAM_IP_DOWNLOADERS_DATE_RANGE_FILTERED = "Select * from ip_bitstream_stats " +
   "where day between ? and ? " +
   "and not exists ( select true from ip_address_ignore where ip_address_ignore.ip_address=ip_bitstream_stats.ip_address ) " +
   "order by inet(ip_address), bitstream_id";
   
   //UIUC added - Inserts new values into monthly_bitstream_stats table.
   protected static final String INSERT_MONTHLY_BITSTREAM_COUNT = 
	   "insert into monthly_bitstream_stats (count, month, item_id, bitstream_id) " +
	   "values (?,?,?,?);";
   
   protected static String INSERT_MONTHLY_BITSTREAM_COUNT_NO_BITSTREAM_ID = 
	   "insert into monthly_bitstream_stats (count, month, item_id) " +
	   "values (?,?,?);";
   
   //UIUC added - updates Monthly bitstream count in monthly_bitstream_stats table.
   protected static final String UPDATE_MONTHLY_BITSTREAM_COUNT_BY_ONE = 
	   "update monthly_bitstream_stats set count = count+1 " +
	   "where month = ? " +
	   "and item_id = ? " +
	   "and bitstream_id = ?;";
   
   //UIUC added - updates Monthly bitstream count in monthly_bitstream_stats table.
   protected static final String UPDATE_MONTHLY_BITSTREAM_COUNT = 
	   "update monthly_bitstream_stats set count = ? " +
	   "where month = ? " +
	   "and item_id = ? " +
	   "and bitstream_id = ?;";
   
   //UIUC added - updates Monthly bitstream count in monthly_bitstream_stats table.
   protected static final String UPDATE_MONTHLY_BITSTREAM_COUNT_NO_BITSTREAM_ID = 
	   "update monthly_bitstream_stats set count = ? " +
	   "where month = ? " +
	   "and item_id = ? " +
	   "and bitstream_id is null;";
  
   //UIUC added - clean old statistics from 'ip_bitstream_stats' table
   protected static final String CLEAN_BITSTREAM_IP_STATS = 
	   "delete from ip_bitstream_stats " +
	   "where day < ? ";
   
   
   // count will always start at 1
   protected final static int INITIAL_COUNT = 1;

   /**
    * Create a new bitstream statistics row
    * Updates the ip count if one has not yet been start
    * Otherwise inserts a new row for the day
    */
   public void updateCount(int itemId, int bitstreamId, String ipAddress) throws SQLException
   {
       log.debug("updating count for bitstream " + bitstreamId );
       Connection conn = null; 
       PreparedStatement logoStmt = null;
       PreparedStatement downloadStmt = null;
       PreparedStatement insertStmt = null;
       
       boolean isLogo = false; 
       boolean alreadyDownloaded = false; 
       
       try
       {
	           conn = DatabaseManager.getConnection();
	        
	           try
    	       {
    	           logoStmt = conn.prepareStatement(IS_LOGO_BITSTREAM);
    	
    	           logoStmt.setInt( 1, bitstreamId );
    	           logoStmt.setInt( 2, bitstreamId );
    	           logoStmt.setInt( 3, bitstreamId );
    	         
    	           ResultSet rs = logoStmt.executeQuery();
    	
    	           // if it returns a row we know 
    	           // it is a logo
    	           if( rs.next() )
    	           {
    	               isLogo = true;        
    	           }
    	
    	           // clean up
    	           rs.close();
    	       }    
	           finally
	           {
	               if(logoStmt!=null) logoStmt.close();
	           }

               log.debug( "isLogo = " + isLogo );
	            
	           // only insert if this is not a logo for a community/collection
	           if( !isLogo )
	           {
                   //Synchronize the actual update/insert queries to ensure
                   //that two requests don't attempt to update/insert at the
                   //exact same moment
                   synchronized (lockObject)
                   {
                       log.debug( "attempting to update existing count" );
    	               
                       //Check if already downloaded today by the given IP address
                       try
                       {
                           downloadStmt = conn.prepareStatement(ALREADY_DOWNLOADED); 
        	               downloadStmt.setDate( 1, new Date(new java.util.Date().getTime()));
        	               downloadStmt.setInt( 2, itemId);
        	               downloadStmt.setInt( 3, bitstreamId );
        	               downloadStmt.setString(4, ipAddress);
                           
        	               ResultSet rs = downloadStmt.executeQuery();
        	           	
            	           // if it returns a row we know 
            	           // that this bitstream has already been downloaded
        	               // by this IP address
            	           if( rs.next() )
            	           {
            	        	   alreadyDownloaded = true;        
            	           }
            	
            	           // clean up
            	           rs.close();
                       }
    	               finally
    	               {
    	                   // clean up
    	                   if(downloadStmt!=null) downloadStmt.close();
    	               }
    	               
    	          
    	               // if not already counted as a download then insert new row
    	               // for this bitstreams count
    	               if (!alreadyDownloaded )
    	               {
                           log.debug("This is a new download, so we need to insert a row");
    	                   try
    	                   {
                               // create and setup statement
        	                   insertStmt = conn.prepareStatement(INSERT_SQL); 
        	         
        	                   // insert a new row that will look something like
        	                   //12 (item_id), 44 (bitstream id), 2005-1-25(date), 128.122.122.133(ip address)  
        	                   insertStmt.setInt( 1, itemId );
        	                   insertStmt.setInt( 2, bitstreamId );
        	                   insertStmt.setDate( 3, new Date(new java.util.Date().getTime()));
        	  				   insertStmt.setString( 4, ipAddress);
        					   insertStmt.setInt( 5, INITIAL_COUNT );
        	
        	                   //execute the sql statement and insert the new row 
        	                   insertStmt.execute();
        	                   
        	                   //Check if this is an ignored IP address
        	                   IpIgnoreDAO ipDAO = new IpIgnoreDAO();
        	                   if(!ipDAO.isIgnored(conn, ipAddress))
        	                   {	   
        	                	   //update monthly count as well
        	                	   increaseMonthlyCountByOne(conn, itemId, bitstreamId);
        	                   }
        	                   //commit all changes
                               conn.commit();
    	                   }
    	                   finally
    	                   {
    	                       // clean up
    	                       if(insertStmt!=null) insertStmt.close();
    	                   }
    	               }
    	               
                   }//end synchronized    
	           }
	       }
	       finally
	       {
	           // close the connection
               DatabaseManager.freeConnection(conn);
	       }
   }      

   /**
    * Get the total count for all dspace downloads
    */
   public long getDSpaceDownloadCount() throws SQLException
   {
       long count = 0;
       Connection conn = null; 
       PreparedStatement prepStmt = null;
       
       try
       {
           conn = DatabaseManager.getConnection();

           // get the total download count
           prepStmt = conn.prepareStatement(DSPACE_TOTAL_COUNT);
           ResultSet rs = prepStmt.executeQuery();       

           if( rs.next() )
           {
               count = rs.getLong(1);
           }

           rs.close(); 
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return count;
   }

   /**
    * Get the total count for all dspace downloads
    * for this month
    */
   public long getMonthDSpaceDownloadCount() throws SQLException
   {
	   Date startOfMonth = this.getStartOfMonth(); 
       return getMonthDSpaceDownloadCount(startOfMonth);
   }
   
   /**
    * Get the total count for all dspace downloads
    * for a given month
    * 
    * @param Date startDateOfMonth
    */
   public long getMonthDSpaceDownloadCount(Date startDateOfMonth) throws SQLException
   {
       long count = 0;
       Connection conn = null; 
       PreparedStatement prepStmt = null;
       
       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(DSPACE_MONTH_COUNT); 
           prepStmt.setDate( 1, startDateOfMonth );

           ResultSet rs = prepStmt.executeQuery();        

           if( rs.next() )
           {
               count = rs.getLong(1);
           }

           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return count;
   }
   
   
   
   
   /**
    * Get the total count for all dspace downloads
    * for today
    */
   public long getTodayDSpaceDownloadCount() throws SQLException
   {
       Date today = new Date(new java.util.Date().getTime()); 
       return getDSpaceRecentDateRangeDownloadCount(today, today);
   }
   
   /**
    * Get the total count for all dspace downloads
    * for the given date range.
    * 
    * This method is PRIVATE since it only returns
    * download counts from 'ip_bitstream_stats'.  Since
    * 'ip_bitstream_stats' may be CLEANED on occasion,
    * it will give an INACCURATE COUNT for large date ranges!
    */
   private long getDSpaceRecentDateRangeDownloadCount(Date startDate, Date endDate) throws SQLException
   {
       long count = 0;
       Connection conn = null; 
       PreparedStatement prepStmt = null;
       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(DSPACE_RECENT_DATE_RANGE_COUNT); 
           prepStmt.setDate( 1, startDate );
           prepStmt.setDate( 2, endDate );

           ResultSet rs = prepStmt.executeQuery();        

           if( rs.next() )
           {
               count = rs.getLong(1);
           }

           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return count;
   }

   /**
    * Get the total number of downloads for a community
    * @param int the community id
    */
   public long getCommunityDownloadCount(int communityId) throws SQLException
   {
       long count = 0;
       Connection conn = null; 
       PreparedStatement prepStmt = null;

       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(COMMUNITY_TOTAL_COUNT); 
         
           prepStmt.setInt( 1, communityId );

           ResultSet rs = prepStmt.executeQuery();

           if( rs.next() )
           {
               count = rs.getLong(1);
           }
          
           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return count;
   }

   /**
    * Get the total number of downloads for a community
    * for the current month
    * @param int the community id
    */
   public long getMonthCommunityDownloadCount(int communityId) throws SQLException
   {
	   Date startOfMonth = this.getStartOfMonth(); 
       return getMonthCommunityDownloadCount(communityId, startOfMonth);
   }
   
   /**
    * Get the number of downloads for a community in a given month
    * 
    * @param int community id
    * @param Date startDateOfMonth
    */
   public long getMonthCommunityDownloadCount(int communityId, Date startDateOfMonth) throws SQLException
   {
       long count = 0;
       Connection conn = null; 
       PreparedStatement prepStmt = null;
       
       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(COMMUNITY_MONTH_COUNT); 
           prepStmt.setInt( 1, communityId );
           prepStmt.setDate( 2, startDateOfMonth );

           ResultSet rs = prepStmt.executeQuery();        

           if( rs.next() )
           {
               count = rs.getLong(1);
           }

           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return count;
   }
   
   
   
   public long getTodayCommunityDownloadCount(int communityId)throws SQLException
   {
       Date today = new Date(new java.util.Date().getTime());
       return getCommunityDateRangeDownloadCount( communityId, today, today );
   }
   
   /**
    * Get the total number of downloads for a community
    * for given date range
    * 
    * WARNING: THIS COUNT MAY BE INACCURATE FOR LARGE DATE RANGES,
    * since 'ip_bitstreams_stats' table is cleaned on occasion
    * 
    * @param int the community id
    */
   private long getCommunityDateRangeDownloadCount(int communityId, Date startDate, 
                                               Date endDate) throws SQLException
   {
       long count = 0;
       Connection conn = null; 
       PreparedStatement prepStmt = null;
       
       try{

           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(COMMUNITY_RECENT_DATE_RANGE_COUNT);  

           prepStmt.setInt( 1, communityId );
           prepStmt.setDate( 2, startDate );
           prepStmt.setDate( 3, endDate );

           ResultSet rs = prepStmt.executeQuery();

           if( rs.next() )
           {
               count = rs.getLong(1);
           }
          
           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return count;  
   }
   
   /**
    * Get the total number of downloads for a collection
    * @param int collection id
    */
   public long getCollectionDownloadCount(int collectionId) throws SQLException
   {
       long count = 0;
       Connection conn = null; 
       PreparedStatement prepStmt = null;
       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(COLLECTION_TOTAL_COUNT); 
         
           prepStmt.setInt( 1, collectionId );

           ResultSet rs = prepStmt.executeQuery();

           if( rs.next() )
           {
               count = rs.getLong(1);
           }
          
           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return count;
   }

   /**
    * Get the number of downloads for a collection in current month
    * 
    * @param int collection id
    */
   public long getMonthCollectionDownloadCount(int collectionId) throws SQLException
   {
	   Date startOfMonth = this.getStartOfMonth(); 
       return getMonthCollectionDownloadCount(collectionId, startOfMonth);
   }
   
   /**
    * Get the number of downloads for a collection in a given month
    * 
    * @param int collection id
    * @param Date startDateOfMonth
    */
   public long getMonthCollectionDownloadCount(int collectionId, Date startDateOfMonth) throws SQLException
   {
       long count = 0;
       Connection conn = null; 
       PreparedStatement prepStmt = null;
       
       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(COLLECTION_MONTH_COUNT); 
           prepStmt.setInt( 1, collectionId );
           prepStmt.setDate( 2, startDateOfMonth );

           ResultSet rs = prepStmt.executeQuery();        

           if( rs.next() )
           {
               count = rs.getLong(1);
           }

           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return count;
   }
   
   
   /**
    * Get the number of downloads for a collection using todays date 
    * 
    * @param int collection id
    */
   public long getTodayCollectionDownloadCount(int collectionId) throws SQLException
   {
       Date today = new Date(new java.util.Date().getTime());
       return getCollectionDateRangeDownloadCount(collectionId, today, today);
   }

   
   /**
    * Get the total number of downloads for a collection
    * for given date range
    * 
    * WARNING: THIS COUNT MAY BE INACCURATE FOR LARGE DATE RANGES,
    * since 'ip_bitstreams_stats' table is cleaned on occasion
    * 
    * @param int the community id
    */
   private long getCollectionDateRangeDownloadCount(int collectionId, Date startDate, 
                                                   Date endDate) throws SQLException
   {
       long count = 0;
       Connection conn = null; 
       PreparedStatement prepStmt = null;
       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(COLLECTION_RECENT_DATE_RANGE_COUNT); 
         
           prepStmt.setInt( 1, collectionId );
           prepStmt.setDate( 2, startDate );
           prepStmt.setDate( 3, endDate );

           ResultSet rs = prepStmt.executeQuery();

           if( rs.next() )
           {
               count = rs.getLong(1);
           }
          
           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return count;
    }
   

   /**
    * Get the total number of downloads for an Item 
    *
    * @param int itemId
    */
   public long getItemDownloadCount(int itemId) throws SQLException
   {
       long count = 0;
       Connection conn = null; 
       PreparedStatement prepStmt = null;

       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(ITEM_TOTAL_COUNT); 
         
           prepStmt.setInt( 1, itemId );

           ResultSet rs = prepStmt.executeQuery();

           if( rs.next() )
           {
               count = rs.getLong(1);
           }
          
           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return count;
   }
   
   /**
    * Get the total count for all item downloads
    * for this month
    */
   public long getMonthItemDownloadCount(int itemId) throws SQLException
   {
       Date startOfMonth = this.getStartOfMonth(); 
       return getMonthItemDownloadCount(itemId, startOfMonth);
   }
   
   /**
    * Get the total count for all item downloads
    * for the given month
    * 
    */
   public long getMonthItemDownloadCount(int itemId, Date startDateOfMonth) throws SQLException
   {
       long count = 0;
       Connection conn = null; 
       PreparedStatement prepStmt = null;
       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(ITEM_MONTH_COUNT); 
           prepStmt.setInt( 1, itemId );
           prepStmt.setDate( 2, startDateOfMonth );

           ResultSet rs = prepStmt.executeQuery();        

           if( rs.next() )
           {
               count = rs.getLong(1);
           }

           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return count;
   }
   
   /**
    * Get the number of downloads for a item using todays date 
    * 
    * @param int item id
    */
   public long getTodayItemDownloadCount(int itemId) throws SQLException
   {
       Date today = new Date(new java.util.Date().getTime());
       return getItemDateRangeDownloadCount(itemId, today, today);
   }
   
   /**
    * Get the total number of downloads for an item
    * for given date range
    * 
    * WARNING: THIS COUNT MAY BE INACCURATE FOR LARGE DATE RANGES,
    * since 'ip_bitstreams_stats' table is cleaned on occasion
    * 
    * @param int the item id
    */
   private long getItemDateRangeDownloadCount(int itemId, Date startDate, 
                                                   Date endDate) throws SQLException
   {
       long count = 0;
       Connection conn = null; 
       PreparedStatement prepStmt = null;
       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(ITEM_RECENT_DATE_RANGE_COUNT); 
         
           prepStmt.setInt( 1, itemId );
           prepStmt.setDate( 2, startDate );
           prepStmt.setDate( 3, endDate );

           ResultSet rs = prepStmt.executeQuery();

           if( rs.next() )
           {
               count = rs.getLong(1);
           }
          
           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return count;
    }
   
   
   /**
    * Get the specified number of top downloaded item(s) overall
    * (and the number of downloads each had)
    *
    * @param numberItems
    * 			the number of the top downloaded items to return.
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @return HashMap
				where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getTopDownloadedItems(int numberItems) throws SQLException
   {
       Connection conn = null; 
       PreparedStatement prepStmt = null;

       //LinkedHashmap of top downloaded items (to preserve order)
       //key = item_id, value = number of downloads
       HashMap topDownloads = null;
       
       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(TOP_DOWNLOAD_ITEMS); 
         
           prepStmt.setInt( 1, numberItems);

           ResultSet rs = prepStmt.executeQuery();

           topDownloads = new LinkedHashMap(numberItems);
           
           while(rs.next())
           {
        	   Long itemId = new Long(rs.getLong(1));
        	   Long downloads = new Long(rs.getLong(2));
        	   topDownloads.put(itemId, downloads);
           }
           
           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return topDownloads;
   }

   /**
    * Get the specified number of top downloaded item(s) overall in Community
    * (and the number of downloads each had)
    * @param int communityId : the community id
    * @param numberItems
    * 			the number of the top downloaded items to return.
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @return HashMap
				where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getTopDownloadedItemsInCommunity(int communityID,int numberItems) throws SQLException
   {
       Connection conn = null;
       PreparedStatement prepStmt = null;

       //LinkedHashmap of top downloaded items (to preserve order)
       //key = item_id, value = number of downloads
       HashMap topDownloads = null;

       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(TOP_DOWNLOAD_ITEMS_IN_COMMUNITY);

           prepStmt.setInt( 1, communityID);
           prepStmt.setInt( 2, numberItems);

           ResultSet rs = prepStmt.executeQuery();

           topDownloads = new LinkedHashMap(numberItems);

           while(rs.next())
           {
        	   Long itemId = new Long(rs.getLong(1));
        	   Long downloads = new Long(rs.getLong(2));
        	   topDownloads.put(itemId, downloads);
           }

           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return topDownloads;
   }

 /**
    * Get the specified number of top downloaded item(s) overall in Collection
    * (and the number of downloads each had)
    *
    * @param int collectionId : the collection id
    * @param numberItems
    * 			the number of the top downloaded items to return.
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @return HashMap
				where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getTopDownloadedItemsInCollection(int collectionId,int numberItems) throws SQLException
   {
       Connection conn = null;
       PreparedStatement prepStmt = null;

       //LinkedHashmap of top downloaded items (to preserve order)
       //key = item_id, value = number of downloads
       HashMap topDownloads = null;

       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(TOP_DOWNLOAD_ITEMS_IN_COLLECTION);

           prepStmt.setInt( 1, collectionId);
           prepStmt.setInt( 2, numberItems);

           ResultSet rs = prepStmt.executeQuery();

           topDownloads = new LinkedHashMap(numberItems);

           while(rs.next())
           {
        	   Long itemId = new Long(rs.getLong(1));
        	   Long downloads = new Long(rs.getLong(2));
        	   topDownloads.put(itemId, downloads);
           }

           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return topDownloads;
   }

   /**
    * Get the specified number of top downloaded item(s) this month
    * (and the number of downloads each had)
    * 
    * @param numberItems
    * 			the number of the top downloaded items to return
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @return HashMap 
    * 			where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getMonthlyTopDownloadedItems(int numberItems) throws SQLException
   {
       Date startOfMonth = this.getStartOfMonth(); 
       return getTopDownloadedItemsInMonth(numberItems, startOfMonth);
   }
   
   
   /**
    * Get the specified number of top downloaded item(s) in the given month
    *
    * @param numberItems
    * 			the number of the top downloaded items to return.
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @param startDateOfMonth
    * 			start date of a month (e.g. Jun 1, 2008)
    * @return HashMap 
    * 			where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getTopDownloadedItemsInMonth(int numberItems, Date startDateOfMonth) throws SQLException
   {
	   Connection conn = null; 
       PreparedStatement prepStmt = null;

       //LinkedHashmap of top downloaded items (to preserve order)
       //key = item_id, value = number of downloads
       HashMap topDownloads = null;
       
       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(TOP_DOWNLOAD_MONTH_ITEMS); 
         
           prepStmt.setDate( 1, startDateOfMonth );
           prepStmt.setInt( 2, numberItems);

           ResultSet rs = prepStmt.executeQuery();

           topDownloads = new LinkedHashMap(numberItems);
           
           while(rs.next())
           {
        	   Long itemId = new Long(rs.getLong(1));
        	   Long downloads = new Long(rs.getLong(2));
        	   topDownloads.put(itemId, downloads);
           }
           
           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return topDownloads;
   }

   /**
    * Get the specified number of  top downloaded item(s) in special community this month
    * (and the number of downloads each had)
    *
    * @param numberItems
    * 			the number of the top downloaded items to return
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @return HashMap
    * 			where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getMonthlyTopDownloadedItemsInCommunity(int communityId,int numberItems) throws SQLException
   {
       Date startOfMonth = this.getStartOfMonth();
       return getTopDownloadedItemsInMonthInCommunity(communityId,numberItems, startOfMonth);
   }


   /**
    * Get the specified number of top downloaded item(s) in special community in the given month
    *
    * @param numberItems
    * 			the number of the top downloaded items to return.
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @param startDateOfMonth
    * 			start date of a month (e.g. Jun 1, 2008)
    * @return HashMap
    * 			where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getTopDownloadedItemsInMonthInCommunity(int communityId, int numberItems, Date startDateOfMonth) throws SQLException
   {
	   Connection conn = null;
       PreparedStatement prepStmt = null;

       //LinkedHashmap of top downloaded items (to preserve order)
       //key = item_id, value = number of downloads
       HashMap topDownloads = null;

       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(TOP_DOWNLOAD_MONTH_ITEMS_IN_COMMUNITY);
           prepStmt.setInt( 1, communityId );
           prepStmt.setDate( 2, startDateOfMonth );
           prepStmt.setInt( 3, numberItems);

           ResultSet rs = prepStmt.executeQuery();

           topDownloads = new LinkedHashMap(numberItems);

           while(rs.next())
           {
        	   Long itemId = new Long(rs.getLong(1));
        	   Long downloads = new Long(rs.getLong(2));
        	   topDownloads.put(itemId, downloads);
           }

           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return topDownloads;
   }

   /**
    * Get the specified number of  top downloaded item(s) in special community this month
    * (and the number of downloads each had)
    * @param int collectionId the collection id
    * @param numberItems
    * 			the number of the top downloaded items to return
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @return HashMap
    * 			where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getMonthlyTopDownloadedItemsInCollection(int collectionId,int numberItems) throws SQLException
   {
       Date startOfMonth = this.getStartOfMonth();
       return getTopDownloadedItemsInMonthInCollection(collectionId,numberItems, startOfMonth);
   }


   /**
    * Get the specified number of top downloaded item(s) in special community in the given month
    * @param int collectionId the collection id
    * @param numberItems
    * 			the number of the top downloaded items to return.
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @param startDateOfMonth
    * 			start date of a month (e.g. Jun 1, 2008)
    * @return HashMap
    * 			where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getTopDownloadedItemsInMonthInCollection(int collectionId,int numberItems, Date startDateOfMonth) throws SQLException
   {
	   Connection conn = null;
       PreparedStatement prepStmt = null;

       //LinkedHashmap of top downloaded items (to preserve order)
       //key = item_id, value = number of downloads
       HashMap topDownloads = null;

       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(TOP_DOWNLOAD_MONTH_ITEMS_IN_COLLECTION);

           prepStmt.setInt( 1, collectionId );
           prepStmt.setDate( 2, startDateOfMonth );
           prepStmt.setInt( 3, numberItems);

           ResultSet rs = prepStmt.executeQuery();

           topDownloads = new LinkedHashMap(numberItems);

           while(rs.next())
           {
        	   Long itemId = new Long(rs.getLong(1));
        	   Long downloads = new Long(rs.getLong(2));
        	   topDownloads.put(itemId, downloads);
           }

           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return topDownloads;
   }

   /**uiuc added for yearly top down load (April 02, 2009)
    * Get the specified number of top downloaded item(s) this Year
    * (and the number of downloads each had)
    *
    * @param numberItems
    * 			the number of the top downloaded items to return
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @return HashMap
    * 			where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getYearlyTopDownloadedItems(int numberItems) throws SQLException
   {
       Date startofYear = this.getStartOfYear();
       Date endofYear = this.getEndOfYear();
       return getTopDownloadedItemsInYear(numberItems,startofYear, endofYear );
   }


   /**
    * Get the specified number of top downloaded item(s) in the given month
    *
    * @param numberItems
    * 			the number of the top downloaded items to return.
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @param currentYear
    * 			get the currentYear (e.g. 2009)
    * @return HashMap
    * 			where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getTopDownloadedItemsInYear(int numberItems, Date getStatOfYear,Date getEndOfYear) throws SQLException
   {
	   Connection conn = null;
       PreparedStatement prepStmt = null;

       //LinkedHashmap of top downloaded items (to preserve order)
       //key = item_id, value = number of downloads
       HashMap topDownloads = null;

       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(TOP_DOWNLOAD_YEAR_ITEMS);

           prepStmt.setDate( 1, getStatOfYear );
           prepStmt.setDate ( 2, getEndOfYear );
           prepStmt.setInt( 3, numberItems);

           ResultSet rs = prepStmt.executeQuery();

           topDownloads = new LinkedHashMap(numberItems);

           while(rs.next())
           {
        	   Long itemId = new Long(rs.getLong(1));
        	   Long downloads = new Long(rs.getLong(2));
        	   topDownloads.put(itemId, downloads);
           }

           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return topDownloads;
   }

   /* Get the specified number of top downloaded item(s) this Year
    * (and the number of downloads each had)
    *
    * @param int communityId the community id
    * @param numberItems
    * 			the number of the top downloaded items to return
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @return HashMap
    * 			where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getYearlyTopDownloadedItemsInCommunity(int communityId,int numberItems) throws SQLException
   {
       Date startofYear = this.getStartOfYear();
       Date endofYear = this.getEndOfYear();
       return getTopDownloadedItemsInYearInCommunity(communityId,numberItems,startofYear, endofYear );
   }


   /**
    * Get the specified number of top downloaded item(s) in the given month
    *
    * @param int communityId
    * the community id
    * @param numberItems
    * 			the number of the top downloaded items to return.
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @param getStatOfYear
    * 			get the getStartOfYear (e.g. 01/01/2009)
    * @param getEnd0fYear
    *           get the getEndOfYear (e.g.12/31/2009)
    * @return HashMap
    * 			where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getTopDownloadedItemsInYearInCommunity(int communityId, int numberItems, Date getStatOfYear,Date getEndOfYear) throws SQLException
   {
	   Connection conn = null;
       PreparedStatement prepStmt = null;

       //LinkedHashmap of top downloaded items (to preserve order)
       //key = item_id, value = number of downloads
       HashMap topDownloads = null;

       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(TOP_DOWNLOAD_YEAR_ITEMS_IN_COMMUNITY);

           prepStmt.setInt( 1, communityId );
           prepStmt.setDate( 2, getStatOfYear );
           prepStmt.setDate ( 3, getEndOfYear );
           prepStmt.setInt( 4, numberItems);

           ResultSet rs = prepStmt.executeQuery();

           topDownloads = new LinkedHashMap(numberItems);

           while(rs.next())
           {
        	   Long itemId = new Long(rs.getLong(1));
        	   Long downloads = new Long(rs.getLong(2));
        	   topDownloads.put(itemId, downloads);
           }

           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return topDownloads;
   }


      /**
    * Get the specified number of top downloaded item(s) in the given month
    *
    * @param int communityId the community id
    * @param numberItems
    * 			the number of the top downloaded items to return.
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @param Date getStartOftheSpecialtime
    * @param Date getEndOftheSpecialtime
    * @return HashMap
    * 			where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getTopDownloadedItemsInSpecialTimeInCommunity(int communityId, int numberItems, Date getStartOftheSpecialtime,Date getEndOftheSpecialtime) throws SQLException
   {
	   Connection conn = null;
       PreparedStatement prepStmt = null;

       //LinkedHashmap of top downloaded items (to preserve order)
       //key = item_id, value = number of downloads
       HashMap topDownloads = null;

       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(TOP_DOWNLOAD_SPECIALTIME_ITEMS_IN_COMMUNITY);

           prepStmt.setInt( 1, communityId );
           prepStmt.setDate( 2, getStartOftheSpecialtime );
           prepStmt.setDate ( 3, getEndOftheSpecialtime );
           prepStmt.setInt( 4, numberItems);

           ResultSet rs = prepStmt.executeQuery();

           topDownloads = new LinkedHashMap(numberItems);

           while(rs.next())
           {
        	   Long itemId = new Long(rs.getLong(1));
        	   Long downloads = new Long(rs.getLong(2));
        	   topDownloads.put(itemId, downloads);
           }

           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return topDownloads;
   }

  /* Get the specified number of top downloaded item(s) this Year
    * (and the number of downloads each had)
    *
    * @param int collectionId the collection id
    * @param numberItems
    * 			the number of the top downloaded items to return
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @return HashMap
    * 			where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getYearlyTopDownloadedItemsInCollection(int collectionId,int numberItems) throws SQLException
   {
       Date startofYear = this.getStartOfYear();
       Date endofYear = this.getEndOfYear();
       return getTopDownloadedItemsInYearInCollection(collectionId,numberItems,startofYear, endofYear );
   }


   /**
    * Get the specified number of top downloaded item(s) in the given month
    *
    * @param int collectionId the collection id
    * @param numberItems
    * 			the number of the top downloaded items to return.
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @param getStatOfYear
    * 			get the getStartOfYear (e.g. 01/01/2009)
    * @param getEnd0fYear
    *           get the getEndOfYear (e.g.12/31/2009)
    * @return HashMap
    * 			where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getTopDownloadedItemsInYearInCollection(int collectionId,int numberItems, Date getStatOfYear,Date getEndOfYear) throws SQLException
   {
	   Connection conn = null;
       PreparedStatement prepStmt = null;

       //LinkedHashmap of top downloaded items (to preserve order)
       //key = item_id, value = number of downloads
       HashMap topDownloads = null;

       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(TOP_DOWNLOAD_YEAR_ITEMS_IN_COLLECTION);

           prepStmt.setInt( 1, collectionId );
           prepStmt.setDate( 2, getStatOfYear );
           prepStmt.setDate ( 3, getEndOfYear );
           prepStmt.setInt( 4, numberItems);

           ResultSet rs = prepStmt.executeQuery();

           topDownloads = new LinkedHashMap(numberItems);

           while(rs.next())
           {
        	   Long itemId = new Long(rs.getLong(1));
        	   Long downloads = new Long(rs.getLong(2));
        	   topDownloads.put(itemId, downloads);
           }

           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return topDownloads;
   }


   /**
    * Get the specified number of top downloaded item(s) in the given month
    *
    * @param int collectionId the collection id
    * @param numberItems
    * 			the number of the top downloaded items to return.
    * 			(e.g. if 5, the top 5 downloaded items will be returned)
    * @param Date getStartOftheSpecialtime
    * @param Date getEndOftheSpecialtime
    * @return HashMap
    * 			where item_id (as a Long) is the key, and total download count (as a Long) is the value.
    */
   public HashMap getTopDownloadedItemsInSpecialTimeInCollection(int collectionId,int numberItems, Date getStartOftheSpecialtime,Date getEndOftheSpecialtime) throws SQLException
   {
	   Connection conn = null;
       PreparedStatement prepStmt = null;

       //LinkedHashmap of top downloaded items (to preserve order)
       //key = item_id, value = number of downloads
       HashMap topDownloads = null;

       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(TOP_DOWNLOAD_SPECIALTIME_ITEMS_IN_COLLECTION);

           prepStmt.setInt( 1, collectionId );
           prepStmt.setDate( 2, getStartOftheSpecialtime );
           prepStmt.setDate ( 3, getEndOftheSpecialtime );
           prepStmt.setInt( 4, numberItems);

           ResultSet rs = prepStmt.executeQuery();

           topDownloads = new LinkedHashMap(numberItems);

           while(rs.next())
           {
        	   Long itemId = new Long(rs.getLong(1));
        	   Long downloads = new Long(rs.getLong(2));
        	   topDownloads.put(itemId, downloads);
           }

           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return topDownloads;
   }

   /**
    * Get the total number of downloads for a bitstream 
    *
    * @param int bitstream id 
    */
   public long getBitstreamDownloadCount(int bitstreamId) throws SQLException
   {
       long count = 0;
       Connection conn = null; 
       PreparedStatement prepStmt = null;
       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(BITSTREAM_TOTAL_COUNT); 
         
           prepStmt.setInt( 1, bitstreamId );

           ResultSet rs = prepStmt.executeQuery();

           if( rs.next() )
           {
               count = rs.getLong(1);
           }
          
           // clean up
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return count;
   }
   
   /**
    * Get the total count for bitstream downloads
    * for this month
    */
   public long getMonthBitstreamDownloadCount(int bitstreamId) throws SQLException
   {
       Date startOfMonth = this.getStartOfMonth(); 
       return getMonthBitstreamDownloadCount(bitstreamId, startOfMonth);
   }
   
   /**
    * Get the total count for bitstream downloads
    * for the given month
    * 
    */
   public long getMonthBitstreamDownloadCount(int bitstreamId, Date startDateOfMonth) throws SQLException
   {
       long count = 0;
       Connection conn = null; 
       PreparedStatement prepStmt = null;
       
       try{
           conn = DatabaseManager.getConnection();

           prepStmt = conn.prepareStatement(BITSTREAM_MONTH_COUNT); 
           prepStmt.setInt( 1, bitstreamId );
           prepStmt.setDate( 2, startDateOfMonth );

           ResultSet rs = prepStmt.executeQuery();        

           if( rs.next() )
           {
               count = rs.getLong(1);
           }

           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return count;
   }
   
   
   /**
    * Get the total count for all dspace downloads
    * for today
    */
   public List getTodayBitstreamDownloaders( boolean filterOutIgnored) throws SQLException
   {
       Date today = new Date(new java.util.Date().getTime()); 
       return getBitstreamDayDownloaders( today, today, filterOutIgnored);
   }

   /**
    * Get the total for all dspace downloads
    * for this month
    */
   public List getMonthBitstreamDownloaders( boolean filterOutIgnored) throws SQLException
   {
       return getBitstreamDayDownloaders( this.getStartOfMonth(), this.getEndOfMonth(), filterOutIgnored);
   }
   
   /**
    * Get the total count for all dspace downloads
    * for the given date range
    */
   public List getBitstreamDayDownloaders(Date startDate, Date endDate, boolean filterOutIgnored) throws SQLException
   {
       Connection conn = null; 
       ArrayList list = new ArrayList();
       PreparedStatement prepStmt = null;
 
       try{
           conn = DatabaseManager.getConnection();

           String query = "";
           if(filterOutIgnored)
        	   query = BITSTREAM_IP_DOWNLOADERS_DATE_RANGE_FILTERED;
           else
        	   query = BITSTREAM_IP_DOWNLOADERS_DATE_RANGE;
           
           prepStmt = conn.prepareStatement(query); 
           prepStmt.setDate( 1, startDate );
           prepStmt.setDate( 2, endDate );

           ResultSet rs = prepStmt.executeQuery();        

           while( rs.next() )
           {
                                            
              list.add( new BitstreamDayDownloader( rs.getLong("bitstream_stats_id"),
                       rs.getInt("bitstream_id"), rs.getInt("item_id"),
                       rs.getDate("day"), rs.getString("ip_address"),
                               rs.getInt("count")));
           }

           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }

       return list;
   }
//uiuc added----get the current year(April 02)
   public int getCurrentYear()
   {
	   // create a new gregorian calendar
	   GregorianCalendar calendar = new GregorianCalendar();
       return getCurrentYear(calendar);
   }
//uiuc added (April 02, 2009)
   private int getCurrentYear(GregorianCalendar calendar)
   {
       // get the current year of the data
       int currentYearoftheday = calendar.get(GregorianCalendar.YEAR);
       return currentYearoftheday;
   }

   private Date getStartOfMonth()
   {
	   // create a new gregorian calendar
	   GregorianCalendar calendar = new GregorianCalendar();
       return getStartOfMonth(calendar); 
   }
   
   private Date getStartOfMonth(GregorianCalendar calendar)
   {
       Date startOfMonth = null;
       
       // get the current day of the month
       int currentDayOfMonth = calendar.get(GregorianCalendar.DAY_OF_MONTH);

       //subtract the current day of the month + 1 to get the first of the month 
       calendar.add( GregorianCalendar.DAY_OF_MONTH, (( -1 * currentDayOfMonth) + 1 ));
                   
       //now we have the first day of the month
       startOfMonth = new Date( calendar.getTime().getTime() );  

       return startOfMonth; 
   }

//UIUC added (April 08, 2009)
   private Date getStartOfYear()
   {
       Date startOfYear = null;

       // get the current year of the day
       int currentYear = getCurrentYear();

       //make the first day of the year
       Calendar firstDayofYear = new GregorianCalendar(currentYear, Calendar.JANUARY, 1);

       //now we have the begin of day in the year
       startOfYear = new Date( firstDayofYear.getTime().getTime() );
       return startOfYear;
   }

  //UIUC added (April 08, 2009)
   private Date getEndOfYear()
   {
       Date endOfYear = null;

       // get the current year of the day
       int currentYear = getCurrentYear();

       //make the END day of the year
       Calendar endDayofYear = new GregorianCalendar(currentYear, Calendar.DECEMBER, 31);

       //now we have the end of day in the year
       endOfYear = new Date( endDayofYear.getTime().getTime() );
       return endOfYear;
   }

   //UIUC ADDED (JULY 30, 2009)
   public Date getStartOftheSpecialtime(int theYear, int theMonth)
   {
       Date startoftheSpecialtime = null;

       //make the first day of the Specialtime
       Calendar firstDayoftheSpecialtime = new GregorianCalendar(theYear, theMonth, 1);

       //now we have the begin of day of the special time
       startoftheSpecialtime = new Date( firstDayoftheSpecialtime.getTime().getTime());
  
       return startoftheSpecialtime ;

   }

  //UIUC added (JULY 30, 2009)
   public Date getEndOftheSpecialtime(int theYear, int theMonth)
   {
       Date endoftheSpecialtime = null;

       GregorianCalendar cal = new GregorianCalendar();
       boolean leapYear = cal.isLeapYear(theYear);

       //make the END day of the Special time
       
       if (theMonth == 1)
       {
           if (leapYear)
           {
               Calendar endDayoftheSpecialtime = new GregorianCalendar(theYear, theMonth, 29);
               endoftheSpecialtime = new Date( endDayoftheSpecialtime.getTime().getTime() );
           }

           else
           {
               Calendar endDayoftheSpecialtime = new GregorianCalendar(theYear, theMonth, 28);
               endoftheSpecialtime = new Date( endDayoftheSpecialtime.getTime().getTime() );
           }

        }
       else if ((theMonth+1)<=7)
       {
           if((theMonth+1)%2 == 1)
           {
                Calendar endDayoftheSpecialtime = new GregorianCalendar(theYear, theMonth, 31);
                endoftheSpecialtime = new Date( endDayoftheSpecialtime.getTime().getTime() );
           }
           else
           {
               Calendar endDayoftheSpecialtime = new GregorianCalendar(theYear, theMonth, 30);
               endoftheSpecialtime = new Date( endDayoftheSpecialtime.getTime().getTime() );
           }
       }
       else
       {
           if(((theMonth+1)% 2) == 0)
           {
               Calendar endDayoftheSpecialtime = new GregorianCalendar(theYear, theMonth, 31);
               endoftheSpecialtime = new Date( endDayoftheSpecialtime.getTime().getTime() );
           }
           else
           {
               Calendar endDayoftheSpecialtime = new GregorianCalendar(theYear, theMonth, 30);
               endoftheSpecialtime = new Date( endDayoftheSpecialtime.getTime().getTime() );
           }
       }
       return endoftheSpecialtime;
   }

   private Date getEndOfMonth()
   {
	   // create a new gregorian calendar
	   GregorianCalendar calendar = new GregorianCalendar();
       return getEndOfMonth(calendar); 
   }
   
   private Date getEndOfMonth(GregorianCalendar calendar)
   {
       Date endOfMonth = null;

       // get the last day of the month 
       int lastDayOfMonth = calendar.getActualMaximum( GregorianCalendar.DAY_OF_MONTH);   

       // get the current day of the month
       int currentDayOfMonth = calendar.get(GregorianCalendar.DAY_OF_MONTH);

       // add the days to get the last day of the month
       calendar.add( GregorianCalendar.DAY_OF_MONTH, lastDayOfMonth - currentDayOfMonth);
                   
       //now we have the last day of the month
       endOfMonth = new Date( calendar.getTime().getTime() );  

       return endOfMonth; 
   }
   
   //return the first three octets of the ip address
   private String getMask(String ipAddress)
   {
       //Separate address into the first three octet and the last octet
       return ipAddress.substring(0,ipAddress.lastIndexOf("."));
       
   }
   
   //return the last octet of the address
   private int getIpValue(String ipAddress)
   {
       return Integer.parseInt(ipAddress.substring(ipAddress.lastIndexOf(".") + 1));
   }
   
   
   /**
    * Updates ALL monthly download counts for EVERYTHING
    * in the database, between a given Start date and End date.
    * 
    * Each date should be a given month.
    */
   public void updateAllMonthlyCounts(java.util.Date startDate, java.util.Date endDate) throws SQLException
   {
	   Connection conn = null; 
	   PreparedStatement downloadStmt = null;
       
	   //Update monthly statistics based on the date provided
       try
       {
    	   conn = DatabaseManager.getConnection();
    	   
		   GregorianCalendar startCalendar = new GregorianCalendar();
		   startCalendar.setTime(startDate);
	       java.util.Date beginMonth = getStartOfMonth(startCalendar);
	       
	       GregorianCalendar endCalendar = new GregorianCalendar();
	       endCalendar.setTime(endDate);
	       java.util.Date finishMonth = getEndOfMonth(endCalendar);
		   
	       GregorianCalendar currentCalendar = (GregorianCalendar) startCalendar.clone();
	       java.util.Date currentMonth = (Date) beginMonth.clone();
	       
	       //For EVERY month between start and end dates...
	       while(currentMonth.before(finishMonth))
	       {
	    	   Date startOfCurrentMonth = getStartOfMonth(currentCalendar);
	    	   Date endOfCurrentMonth = getEndOfMonth(currentCalendar);
	    	   
		       //Determine current count for current month
		       try
	           {
	               // try and insert for the given date
	               downloadStmt = conn.prepareStatement(BITSTREAM_DATE_RANGE_TOTALS); 
	               downloadStmt.setDate( 1, startOfCurrentMonth);
	               downloadStmt.setDate( 2, endOfCurrentMonth); 
	               
	               ResultSet rs = downloadStmt.executeQuery();        
	
	               while( rs.next() )
	               {
	            	  int bitstreamId = rs.getInt(1);
	            	  int itemId = rs.getInt(2);
	                  long downloads = rs.getLong(3);
	                  
	                  //update monthly count for this bitstream
	                  updateMonthlyCountTotal(conn, itemId, bitstreamId, startOfCurrentMonth, downloads);
	               }
	
	               rs.close();
	           }
	           finally
	           {
	               // clean up
	               if(downloadStmt!=null) downloadStmt.close();
	           }
	           
	           //add one to current month
	           currentCalendar.add(Calendar.MONTH, 1);
	           currentMonth = getStartOfMonth(currentCalendar);
	           
	       }//end while  
		}
		finally
		{
		// close the connection
		DatabaseManager.freeConnection(conn);
		}
   }
   
   /**
    * Updates all monthly download counts for the current month
    */
   public void updateAllMonthlyCounts() throws SQLException
   {
	 //Construct a calendar for this date
	 GregorianCalendar calendar = new GregorianCalendar();
	 
	 //Call updateMonthlyCount with current date
	 updateAllMonthlyCounts(calendar);
   }
   
   /**
    * Updates all monthly download counts for a given date.
    * 
    * The date should represent a specific month.
    */
   public void updateAllMonthlyCounts(java.util.Date date) throws SQLException
   {
	 //Construct a calendar for this date
	 GregorianCalendar calendar = new GregorianCalendar();
	 calendar.setTime(date);
	 
	 //Call updateMonthlyCount with current date
	 updateAllMonthlyCounts(calendar);
   }
   
   /**
    * Updates all monthly download counts for a given date.
    * 
    * The calendar should represent a specific month.
    */
   public void updateAllMonthlyCounts(GregorianCalendar calendar) throws SQLException
   {
	   Connection conn = null; 
	   PreparedStatement downloadStmt = null;
       
       //Update monthly statistics based on the date provided
       try
       {
    	   conn = DatabaseManager.getConnection();
    	   
	       //Get start & end of month specified
	       Date startOfMonth = getStartOfMonth(calendar);
	       Date endOfMonth = getEndOfMonth(calendar);
	       
	       //Determine current count for this month
	       try
           {
               // try and insert for the given date
               downloadStmt = conn.prepareStatement(BITSTREAM_DATE_RANGE_TOTALS); 
               downloadStmt.setDate( 1, startOfMonth);
               downloadStmt.setDate( 2, endOfMonth);
               
               ResultSet rs = downloadStmt.executeQuery();        

               while( rs.next() )
               {
            	  int bitstreamId = rs.getInt(1);
            	  int itemId = rs.getInt(2);
                  long downloads = rs.getLong(3);
                  
                  //update monthly count for this bitstream
                  updateMonthlyCountTotal(conn, itemId, bitstreamId, startOfMonth, downloads);
               }

               rs.close();
           }
           finally
           {
               // clean up
               if(downloadStmt!=null) downloadStmt.close();
           }
		}
		finally
		{
		// close the connection
		DatabaseManager.freeConnection(conn);
		}
   }
   
   /**
    * Increases current monthly download count for specific
    * bitstream by one.
    */
   private void increaseMonthlyCountByOne(Connection conn, int itemId, int bitstreamId) throws SQLException
   {
	   PreparedStatement updateStmt = null;
       PreparedStatement insertStmt = null;
       
       //get start of current month
       Date startOfMonth = getStartOfMonth();
       
       //Update monthly statistics based on the date provided
	   int rowcount;
       try
       {
    	   // try and insert for the given date
           updateStmt = conn.prepareStatement(UPDATE_MONTHLY_BITSTREAM_COUNT_BY_ONE); 
           updateStmt.setDate( 1, startOfMonth);
           updateStmt.setInt( 2, itemId);
           updateStmt.setInt( 3, bitstreamId );
         
           //execute the sql statement and see if any rows updated
           rowcount = updateStmt.executeUpdate();
           
           //commit all changes
           conn.commit();
       }
       finally
       {
           // clean up
           if(updateStmt!=null) updateStmt.close();
       }
       
  
       // if nothing updated then insert new row
       // for this bitstreams count
       if (rowcount == 0)
       {

           try
           {
        	   // create and setup statement
               insertStmt = conn.prepareStatement(INSERT_MONTHLY_BITSTREAM_COUNT); 
     
               // insert a new row that will look something like
               //12 (total downloads),  2005-1-25(date), 10 (item id), 44 (bitstream id)  
               insertStmt.setLong( 1, INITIAL_COUNT);
               insertStmt.setDate( 2, startOfMonth);
               insertStmt.setInt( 3, itemId);
               insertStmt.setInt( 4, bitstreamId );
              
               //execute the sql statement and insert the new row 
               insertStmt.execute();
               
               //commit all changes
               conn.commit();
           }
           finally
           {
               // clean up
               if(insertStmt!=null) insertStmt.close();
           }
       } //end if rowcount==0
   }
   
   /**
    * Create a new monthly bitstream statistics row
    * or updates the count (if row already exists)
    */
   private void updateMonthlyCountTotal(Connection conn, int itemId, int bitstreamId, Date startOfMonth, long downloads) throws SQLException
   {
	   PreparedStatement updateStmt = null;
       PreparedStatement insertStmt = null;
       
       //Update monthly statistics based on the date provided
	   int rowcount;
       try
       {
           if(bitstreamId > 0)
           {
        	   // try and insert for the given date
	           updateStmt = conn.prepareStatement(UPDATE_MONTHLY_BITSTREAM_COUNT); 
	           updateStmt.setLong( 1, downloads);
	           updateStmt.setDate( 2, startOfMonth);
	           updateStmt.setInt( 3, itemId);
	           updateStmt.setInt( 4, bitstreamId );
           }
           else
           {   //update with no bitstream id
        	   updateStmt = conn.prepareStatement(UPDATE_MONTHLY_BITSTREAM_COUNT_NO_BITSTREAM_ID); 
	           updateStmt.setLong( 1, downloads);
	           updateStmt.setDate( 2, startOfMonth);
	           updateStmt.setInt( 3, itemId);
           }
	           
           //execute the sql statement and see if any rows updated
           rowcount = updateStmt.executeUpdate();
           
           //commit all changes
           conn.commit();
       }
       finally
       {
           // clean up
           if(updateStmt!=null) updateStmt.close();
       }
       
  
       // if nothing updated then insert new row
       // for this bitstreams count
       if (rowcount == 0 && downloads > 0)
       {

           try
           {
               if(bitstreamId>0)
               {	   
	        	   // create and setup statement
	               insertStmt = conn.prepareStatement(INSERT_MONTHLY_BITSTREAM_COUNT); 
	     
	               // insert a new row that will look something like
	               //12 (total downloads),  2005-1-25(date), 10 (item id), 44 (bitstream id)  
	               insertStmt.setLong( 1, downloads);
	               insertStmt.setDate( 2, startOfMonth);
	               insertStmt.setInt( 3, itemId);
	               insertStmt.setInt( 4, bitstreamId );
               }
               else
               {
            	// create and setup statement
	               insertStmt = conn.prepareStatement(INSERT_MONTHLY_BITSTREAM_COUNT_NO_BITSTREAM_ID); 
	     
	               // insert a new row that will look something like
	               //12 (total downloads),  2005-1-25(date), 10 (item id) 
	               insertStmt.setLong( 1, downloads);
	               insertStmt.setDate( 2, startOfMonth);
	               insertStmt.setInt( 3, itemId);
               }
               //execute the sql statement and insert the new row 
               insertStmt.execute();
               
               //commit all changes
               conn.commit();
           }
           finally
           {
               // clean up
               if(insertStmt!=null) insertStmt.close();
           }
       } //end if rowcount==0
   }
   
   /**
    * Cleans IP statistics out of the 'ip_bitstream_stats'
    * table.  It only keeps statistics for the specified
    * number of months (NOT including current month which
    * is ALWAYS KEPT).  A value of '0' will only keep
    * current months statistics.
    * 
    * (e.g.) cleanIPStats(2) called on Sept 9, 2008
    * will keep ALL stats from July, Aug & Sept 2008,
    * and remove any statistics from before that period.
    */
   public void cleanIPStats(int numMonthsToKeep) throws SQLException
   {
	   Connection conn = null; 
	   PreparedStatement deleteStmt = null;
       
	   if (numMonthsToKeep >= 0)
	   {	   
	       //Update monthly statistics based on the date provided
	       try
	       {
	    	   conn = DatabaseManager.getConnection();
	    	   
	    	   GregorianCalendar calendar = new GregorianCalendar();
	      	   if (numMonthsToKeep > 0)
	      		   calendar.add(Calendar.MONTH, (-1 * numMonthsToKeep)); //subtract number of months

		       //Get start of month specified
		       Date startOfMonth = getStartOfMonth(calendar);
		       
		       //Remove all stats before the start of specified month
		       try
	           {
		    	   deleteStmt = conn.prepareStatement(CLEAN_BITSTREAM_IP_STATS); 
		    	   deleteStmt.setDate( 1, startOfMonth);
	               
	               //execute the sql statement to clean up datea
	               deleteStmt.executeUpdate();
	               
	               //commit all changes
	               conn.commit();
	           }
	           finally
	           {
	               // clean up
	               if(deleteStmt!=null) deleteStmt.close();
	           }
			}
			finally
			{
			// close the connection
			DatabaseManager.freeConnection(conn);
			}
	   }//if numMonthsToKeep >=0
   }
 
}
