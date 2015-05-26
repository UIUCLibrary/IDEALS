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

import org.apache.log4j.Logger;
import java.util.Date;

/**
* @author Nate Sarr
* @version 1.0
* @created 12-APR-2005 09:32:03 AM
* 
* Version: $Revision$
* 
* Date: $Date$
* 
* 
* This is an object that represents the download count
* for a particular bitstream by a specific ip address and date
* 
*/
public class BitstreamDayDownloader
{

   private static Logger log = Logger.getLogger(IpIgnore.class);

   private long id;
   
   private int bitstreamId;
   
   private int itemId;
   
   private String ipAddress;
   
   private Date day;
   
   private int count;
   
   /**
    * Default constructor
    * 
    * @param id
    *            Unique id for the ip
    * @param ip
    *            the ip to ignore
    */
   public BitstreamDayDownloader(long id, int bitstreamId, int itemId, Date day, String ipAddress, int count)
   {
       this.id = id;
       this.bitstreamId = bitstreamId;
       this.itemId = itemId;
       this.day = day;
       this.ipAddress = ipAddress;
       this.count = count;
   }


   /**
    * Get the unique identifier for this ip bitstream download
    * 
    * @return the id
    */
   public long getId()
   {
       return this.id;
   }

   public void setBitstreamId(int bitstreamId)
   {
       this.bitstreamId = bitstreamId;
   }

   public int getBitstreamId()
   {
       return this.bitstreamId;
   }
   
   public int getItemId()
   {
       return this.itemId;
   }

   public void setDay(Date day)
   {
       this.day = day;
   }

   public Date getDay()
   {
       return this.day;
   }
   
   public void setIpAddress(String ipAddress)
   {
       this.ipAddress = ipAddress;
   }

   public String getIpAddress()
   {
       return this.ipAddress;
   }

   public void setCount(int count)
   {
       this.count = count;
   }

   public int getCount()
   {
       return this.count;
   }
 
}