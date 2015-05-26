/*
 * IpIgnoreDAO.java
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


import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.ur.dspace.stats.IpIgnore;

import org.dspace.storage.rdbms.DatabaseManager;

/**
 * @author Nathan Sarr (U of Rochester)
 * @author Tim Donohue (U of Illinois)
 * 
 * Accessed the ip ingnore information
 */
public class IpIgnoreDAO
{
   // logger
   private static Logger log = Logger.getLogger(IpIgnoreDAO.class);

   // fields needed for insert of an ip range
   protected static final String RANGE_FIELDS_INSERT =
       "range_ignore_id, ip_address_mask, ip_address_start, ip_address_end, name, reason";

   protected static String INSERT_RANGE_SQL =
       "insert into ip_range_ignore ( " + RANGE_FIELDS_INSERT + " ) values (?,?,?,?,?,?);";
   
   protected static String INSERT_EACH_IP_SQL =
       "INSERT INTO ip_address_ignore ( ip_ignore_id, range_ignore_id, ip_address ) " +
       		"SELECT nextval('ip_address_ignore_seq'), range_ignore_id, ip_address_mask || '.' || generate_series(ip_address_start,ip_address_end) as ip_address " +
       		"FROM ip_range_ignore WHERE range_ignore_id = ?"; 

   protected static String UPDATE_RANGE_SQL =
       "update ip_range_ignore set ip_address_mask = ?, " +
       "ip_address_start = ?, ip_address_end = ?, name = ?, reason = ? " +
       "where range_ignore_id = ?;";

   protected static String DELETE_RANGE_SQL =
       "delete from ip_range_ignore where range_ignore_id = ?;";

   protected static String DELETE_EACH_IP_SQL =
       "delete from ip_address_ignore where range_ignore_id = ?;";
   
   // find by id
   protected static String FIND_RANGE_SQL =
       "select * from ip_range_ignore where range_ignore_id = ?;";
   
   // find all ignored ip ranges in database
   // (UIUC Changed - order by starting IP address)
   protected static String FIND_ALL_RANGE_SQL = 
	   "select * from ip_range_ignore order by inet(ip_address_mask || '.' || ip_address_start);";
   
   // find all by IP mask
   protected static String FIND_BY_IP_MASK = 
	   "select * from ip_range_ignore " +
	   "where ip_address_mask=? " +
	   "order by inet(ip_address_mask || '.' || ip_address_start);";
  
   
   // Find if a specific IP address is ignored
   protected static String IS_IGNORED_ADDRESS = 
	   "select true from ip_address_ignore where ip_address=?;";
   
   /**
    * Create a new IP range to ignore in the database
    */
   public static IpIgnore createIp(String ipMask, int ipStart, int  ipEnd,
		   String name, String reason) throws SQLException
   {
       log.debug("adding ip range to ignore " + ipMask + "." + ipStart +"/" + ipEnd );

	   Connection conn = null; 
       PreparedStatement prepStmt = null;
	   
	   try 
       {
		   conn = DatabaseManager.getConnection();
	       // get the next sequence number for the librarian id
	       Statement statement = conn.createStatement();
	       ResultSet rs = statement.executeQuery( "SELECT nextval('ip_range_ignore_seq');");
	       rs.next();
	
	       int range_ignore_id = rs.getInt(1);
	
	       rs.close();
	       statement.close();
	
	       try
	       {
	           // Create and setup statement to Insert a new IP range!
	           prepStmt = conn.prepareStatement(INSERT_RANGE_SQL);
	
	           prepStmt.setInt( 1, range_ignore_id  );
	           prepStmt.setString( 2, ipMask );
			   prepStmt.setInt( 3, ipStart );
			   prepStmt.setInt( 4, ipEnd );
	           prepStmt.setString( 5, name );
	           prepStmt.setString( 6, reason );
	           
	           //execute the sql statement and insert the new IP Range
	           prepStmt.execute();
	       }
	       finally
	       {
	           if(prepStmt!=null) prepStmt.close();
	       }
	
	       try
	       {
	           // Create and setup statement to Insert each IP in the range
	           // (This table is for quick indexing of each ignored IP)
	           prepStmt = conn.prepareStatement(INSERT_EACH_IP_SQL);
	
	           prepStmt.setInt( 1, range_ignore_id  );
	
	           //execute the sql statement and insert each individual IP
	           prepStmt.execute();
	       }
	       finally
	       {
	           if(prepStmt!=null) prepStmt.close();
	       }

           // commit the statements
           conn.commit();

       }
       finally
       {
           // close the connection
           DatabaseManager.freeConnection(conn);
       }

       return null;
   }      

   /**
    * Update the IP range to ignore in the database
    * 
    * @param ipIgnore 
    * 		IP range to update
    */
   public static void update(IpIgnore ipIgnore) throws SQLException
   {

       Connection conn = null;
       PreparedStatement prepStmt = null;
       try
       {
           conn = DatabaseManager.getConnection();
           
           try
           {
               // create and setup statement to update an IP Range
               prepStmt = conn.prepareStatement(UPDATE_RANGE_SQL);
    
               prepStmt.setString( 1, ipIgnore.getIpAddressMask() );
               prepStmt.setInt( 2, ipIgnore.getIpAddressStart() );
    		   prepStmt.setInt( 3, ipIgnore.getIpAddressEnd() );
    		   prepStmt.setString( 4, ipIgnore.getName() );
    		   prepStmt.setString( 5, ipIgnore.getReason() );
               prepStmt.setInt( 6, ipIgnore.getId() );
               
               prepStmt.execute();
           }
           finally
           {
               if(prepStmt!=null) prepStmt.close();
           }
           
           try
           {
               // Remove all individual IPs in this range
               prepStmt = conn.prepareStatement(DELETE_EACH_IP_SQL);
    
               prepStmt.setInt( 1, ipIgnore.getId() );	 
               prepStmt.execute();
           }
           finally
           {
               if(prepStmt!=null) prepStmt.close();
           }
           
           try
           {
               // Add the updated individual IPs in this range
               prepStmt = conn.prepareStatement(INSERT_EACH_IP_SQL);

               prepStmt.setInt( 1, ipIgnore.getId() );	 
               prepStmt.execute();
           }
           finally
           {
               if(prepStmt!=null) prepStmt.close();
           }

           // commit the statement
           conn.commit();

       }
       finally
       {
           DatabaseManager.freeConnection(conn);
       }
   }

   /**
    * Delete IP range from the database
    *  
    * @param id 
    * 		id of the IP range to delete
    */
   public static void delete(int id) throws SQLException{
       Connection conn = null;
       PreparedStatement prepStmt = null;
       try
       {
           conn = DatabaseManager.getConnection();
           
           try
           {
               // create and setup statement to remove an ignored IP range
               prepStmt = conn.prepareStatement(DELETE_RANGE_SQL);
    
               prepStmt.setInt( 1, id );	 
               prepStmt.execute();
           }
           finally
           {
               if(prepStmt!=null) prepStmt.close();
           }
           
           try
           {
               // Remove all individual IPs falling in this range
               prepStmt = conn.prepareStatement(DELETE_EACH_IP_SQL);
    
               prepStmt.setInt( 1, id );	 
               prepStmt.execute();
           }
           finally
           {
               if(prepStmt!=null) prepStmt.close();
           }

           // commit the statement
           conn.commit();
       }
       finally
       {
           DatabaseManager.freeConnection(conn);
       }
   }


   /**
    * Find the ip range information from id
    * 
    * @param id 
    * 		id of the IP range to find
    * @return IpIgnore object
    */
   public static IpIgnore findById(int id) throws SQLException{
	   Connection conn = null;
       PreparedStatement prepStmt = null;
	   IpIgnore ip = null;
       try
       {
           conn = DatabaseManager.getConnection();
           prepStmt = conn.prepareStatement(FIND_RANGE_SQL);

		   prepStmt.setInt(1, id);
		   
           //execute the sql statement and retrieve ip ignore info
           ResultSet rs = prepStmt.executeQuery();
		
           //load the ips 
           if( rs.next() )
           {
			   ip = new IpIgnore( rs.getInt("range_ignore_id"),
					   rs.getString("ip_address_mask"),
					   rs.getInt("ip_address_start"),
					   rs.getInt("ip_address_end"),
					   rs.getString("name"), 
					   rs.getString("reason"));
          }
          rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
           DatabaseManager.freeConnection(conn);
       }
	   return ip;
   }

   /**
    * Find all the ignore ip ranges in the database
    * 
    * @return collection of IpIgnore objects
    */
	public static List<IpIgnore> findAll() throws SQLException{
	   Connection conn = null;
       PreparedStatement prepStmt = null;
	   List<IpIgnore> list = new ArrayList<IpIgnore>();
       try
       {
           conn = DatabaseManager.getConnection();
           prepStmt = conn.prepareStatement(FIND_ALL_RANGE_SQL);

           //execute the sql statement and retrieve ip ignore info
           ResultSet rs = prepStmt.executeQuery();
		
		   IpIgnore ip = null;
           //load the ips 
           while( rs.next() )
           {
			   ip = new IpIgnore( rs.getInt("range_ignore_id"),
					   rs.getString("ip_address_mask"),
					   rs.getInt("ip_address_start"),
					   rs.getInt("ip_address_end"),
					   rs.getString("name"), 
					   rs.getString("reason"));
			   list.add(ip);
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
	
	
	/**
    * Find all ignored ip ranges that match a
    * provide IP mask
    * 
    * @return collection of IpIgnore objects
    */
	public static List<IpIgnore> findByMask(String mask) throws SQLException
	{
		Connection conn = null; 
		List<IpIgnore> list = null;
		   
		try 
		{
		   //initialize new DB connection
		   conn = DatabaseManager.getConnection();
      
		   list = findByMask(conn, mask);
		}
		finally
		{
           // close the connection
           DatabaseManager.freeConnection(conn);
		}
       
		return list;
	}
	
	/**
    * Find all ignored ip ranges that match a
    * provide IP mask
    * 
    * @return collection of IpIgnore objects
    */
	protected static List<IpIgnore> findByMask(Connection conn, String mask) throws SQLException
	{
	   PreparedStatement prepStmt = null;
	   List<IpIgnore> list = new ArrayList<IpIgnore>();

	   try
	   {
           prepStmt = conn.prepareStatement(FIND_BY_IP_MASK);
           prepStmt.setString( 1, mask );
           
           //execute the sql statement and retrieve ip ignore info
           ResultSet rs = prepStmt.executeQuery();
		
		   IpIgnore ip = null;
           //load the ips 
           while( rs.next() )
           {
			   ip = new IpIgnore( rs.getInt("range_ignore_id"),
					   rs.getString("ip_address_mask"),
					   rs.getInt("ip_address_start"),
					   rs.getInt("ip_address_end"),
					   rs.getString("name"), 
					   rs.getString("reason"));
			   list.add(ip);
           }
           rs.close();
       }
       finally
       {
           if(prepStmt!=null) prepStmt.close();
       }
	   return list;
   }
	
	
	/**
    * Return whether or not a given IP address is ignored.
    * Uses an existing Connection object.
    * @return true if ignored, false otherwise
    */
	public static boolean isIgnored(String ipAddress) throws SQLException
	{
		Connection conn = null; 
		boolean ignoredIp = false;
		   
		try 
		{
		   //initialize new DB connection
		   conn = DatabaseManager.getConnection();
      
		   ignoredIp = isIgnored(conn, ipAddress);
		}
		finally
		{
           // close the connection
           DatabaseManager.freeConnection(conn);
		}
       
		return ignoredIp;
	}
	
	
	/**
    * Return whether or not a given IP address is ignored.
    * Uses an existing Connection object.
    * @return true if ignored, false otherwise
    */
	protected static boolean isIgnored(Connection conn, String ipAddress) throws SQLException
	{
		PreparedStatement selectStmt = null;
	    boolean ignoredIP = false;   
		
		//Check if IP address is in current list of ignored addresses
        try
        {
	    	selectStmt = conn.prepareStatement(IS_IGNORED_ADDRESS); 
	    	selectStmt.setString(1, ipAddress);
	        
	        ResultSet rs = selectStmt.executeQuery();
	    	
	        // if it returns a row we know 
	        // that this IP is in the ignored table
	        if( rs.next() )
	        {
	        	ignoredIP = true;        
	        }
	
	        // clean up
	        rs.close();
        }
        finally
        {
            // clean up
            if(selectStmt!=null) selectStmt.close();
        }
		return ignoredIP;
	}

}
