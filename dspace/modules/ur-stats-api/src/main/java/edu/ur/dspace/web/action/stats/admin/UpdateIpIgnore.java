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
import java.util.Collection;

import org.apache.log4j.Logger;

import edu.ur.dspace.stats.IpIgnoreDAO;
import edu.ur.dspace.stats.IpIgnore;
import edu.ur.dspace.web.action.Action;
/**
 * Update ip information from editing page
 *
 * @author A Tse
 */
public class UpdateIpIgnore implements Action
{
    private static Logger log = Logger.getLogger(UpdateIpIgnore.class);
    public String execute(HttpServletRequest request, 
                          HttpServletResponse response)
    {
        log.debug( "Update ip to ignore list" );

        // default page to forward to 
        String page = null;

	    IpIgnoreDAO ipDAO = new IpIgnoreDAO();
		
		// get parameters
		int id = Integer.parseInt(request.getParameter("id"));
		String octet1 = request.getParameter("octet1");
		String octet2 = request.getParameter("octet2");
		String octet3 = request.getParameter("octet3");
		String octetStart = request.getParameter("octetStart");
		String octetEnd = request.getParameter("octetEnd");
		String name = request.getParameter("name");
		String reason = request.getParameter("reason");

		// default addresses to 0 if empty
		if (octet1.equals("")){
			octet1 = "0";
		}
		if (octet2.equals("")){
			octet2 = "0";
		}
		if (octet3.equals("")){
			octet3 = "0";
		}
		if (octetStart.equals("")){
			octetStart = "0";
		}
		// set end range as start range if empty
		if (octetEnd.equals("")){
			octetEnd = octetStart;
		}
		
		// put parameters into appropriate format
		String ipAddressMask = octet1 + "." + octet2 + "." + octet3;
		int ipAddressStart = Integer.parseInt(octetStart);
		int ipAddressEnd = Integer.parseInt(octetEnd);
		
		
        try
        { 
			// Check that end range is larger than start range
			if ( ipAddressEnd < ipAddressStart ){
				
				IpIgnore ip = ipDAO.findById(id);
				// redisplay all parameters
				Collection list = ipDAO.findAll();
				request.setAttribute("octet1", octet1);
				request.setAttribute("octet2", octet2);
				request.setAttribute("octet3", octet3);
				request.setAttribute("octetStart", ""+ip.getIpAddressStart());
				request.setAttribute("octetEnd", ""+ip.getIpAddressEnd());
				request.setAttribute("name", name);
				request.setAttribute("reason", reason);
				request.setAttribute("id", ""+id);
				
				// show error
				request.setAttribute("ignoreList", list);
				request.setAttribute("error", new Boolean(true));
				page = "/dspace-admin/ur-stats/view-ip-ignore.jsp";
				
			}
			else
			{
				//update the ip to ignore
				IpIgnore ip = ipDAO.findById(id);
				
				ip.setIpAddressMask( ipAddressMask );
				ip.setIpAddressStart( ipAddressStart );
				ip.setIpAddressEnd( ipAddressEnd );
				ip.setName(name);
				ip.setReason(reason);
				
				ipDAO.update(ip);
				
				// redisplay list
				Collection list = ipDAO.findAll();
				request.setAttribute("ignoreList", list);
				page = "/dspace-admin/ur-stats/view-ip-ignore.jsp";
	        }
        }
        catch( Exception e )
        {
            log.fatal( "Exception", e);
            page = "/error/internal.jsp";
        }
        

        return page;
    }
}