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
 * Grabs ip information for editing
 *
 * @author A Tse
 */
public class EditIpIgnore implements Action
{
    private static Logger log = Logger.getLogger(EditIpIgnore.class);
    public String execute(HttpServletRequest request, 
                          HttpServletResponse response)
    {
        log.debug( "Edit ip information" );

        // default page to forward to 
        String page = null;
		IpIgnoreDAO ipDAO = new IpIgnoreDAO();

		// get parameters
		int id = Integer.parseInt(request.getParameter("id"));
        try
        { 
		
			IpIgnore ip = ipDAO.findById(id);
			
			// grab ip address and separate it for display purposes
			String mask = ip.getIpAddressMask();
			String octet1 = mask.substring( 0 , mask.indexOf(".") );
			mask = mask.substring( mask.indexOf(".") + 1 );
			String octet2 = mask.substring( 0 , mask.indexOf(".") );
			mask = mask.substring( mask.indexOf(".") + 1 );
			String octet3 = mask;
			// grab ip range
			String octetStart = ""+ip.getIpAddressStart();
			String octetEnd = ""+ip.getIpAddressEnd();
			// grab the rest of ip info
			String name = ip.getName();
			String reason = ip.getReason();
		
			// set display list attribute
			Collection list = ipDAO.findAll();
			request.setAttribute("ignoreList", list);

			//set all other attributes
			request.setAttribute("octet1", octet1);
			request.setAttribute("octet2", octet2);
			request.setAttribute("octet3", octet3);
			request.setAttribute("octetStart", octetStart);
			request.setAttribute("octetEnd", octetEnd);
			request.setAttribute("name", name);
			request.setAttribute("reason", reason);
			request.setAttribute("id", ""+id);

			page = "/dspace-admin/ur-stats/view-ip-ignore.jsp";
        }
        catch( Exception e )
        {
            log.fatal( "Exception", e);
            page = "/error/internal.jsp";
        }
        
        return page;
    }
}