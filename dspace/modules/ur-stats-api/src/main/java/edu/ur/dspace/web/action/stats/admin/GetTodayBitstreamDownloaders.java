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

package edu.ur.dspace.web.action.stats.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.ur.dspace.stats.StatsDAO;
import edu.ur.dspace.web.action.Action;
/**
 * Grabs download information for today
 *
 * @author Nate Sarr
 */
public class GetTodayBitstreamDownloaders implements Action
{
    private static Logger log = Logger.getLogger(GetTodayBitstreamDownloaders.class);
    public String execute(HttpServletRequest request, 
                          HttpServletResponse response)
    {
        // default page to forward to 
        String page = null;
        StatsDAO statsDAO = new StatsDAO();

        // get parameters
        try
        {     
        	boolean filterOutIgnoredIPs = false;
        	if(request.getParameter("filterOutIgnoredIPs")!=null &&
        			request.getParameter("filterOutIgnoredIPs").equalsIgnoreCase("true"))
        		filterOutIgnoredIPs = true;
        	
        	request.setAttribute("downloads", statsDAO.getTodayBitstreamDownloaders(filterOutIgnoredIPs));
            page = "/dspace-admin/ur-stats/view-day-downloaders.jsp";
        }
        catch( Exception e )
        {
            log.fatal( "Exception", e);
            page = "/error/internal.jsp";
        }
        
        return page;
    }
}
