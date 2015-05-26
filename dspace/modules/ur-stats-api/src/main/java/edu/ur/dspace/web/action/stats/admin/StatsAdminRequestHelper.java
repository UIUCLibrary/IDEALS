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
import javax.servlet.http.HttpSession;
import java.sql.SQLException;

import edu.ur.dspace.web.action.Action;
import edu.ur.dspace.web.action.UnauthorizedAction;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

/**
 * Admin Request helper this deals with stats admin processing
 *
 * @author Nate Sarr
 */

public class StatsAdminRequestHelper 
{
	private static Logger log = Logger.getLogger(StatsAdminRequestHelper.class);
	
	public Action getAction(HttpServletRequest request) throws SQLException
	{
        Action action = null;
        log.debug( "processing action: " + request.getParameter("action") );
        String strAction = request.getParameter("action");
 
                // create a context object
        Context dspaceContext = new Context();
       
        try{

            // get the current user making the changes
            HttpSession session = request.getSession();
            int currentUser = ((Integer)session.getAttribute("dspace.current.user.id")).intValue();
            dspaceContext.setCurrentUser( EPerson.find( dspaceContext, currentUser ) );

             // you have to be an administrator to perform these actions
            if( !AuthorizeManager.isAdmin(dspaceContext) )
            {
                action = new UnauthorizedAction();

                //get rid of the context object
                dspaceContext.complete();
            } 
            else if ( strAction != null )
            {
                //get rid of the context object
                dspaceContext.complete();
		        if( strAction.equals("viewIpIgnoreList") ){ action = new ViewIpIgnoreList(); };
		        if( strAction.equals("addToIgnoreList") ){ action = new AddToIgnoreList(); };
		        if( strAction.equals("deleteIpIgnore") ){ action = new DeleteIpIgnore(); };
		        if( strAction.equals("editIpIgnore") ){ action = new EditIpIgnore(); };
				if( strAction.equals("updateIpIgnore") ){ action = new UpdateIpIgnore(); };
                if( strAction.equals("viewTodaysDownloads") ){ action = new GetTodayBitstreamDownloaders(); };
                if( strAction.equals("viewMonthsDownloads") ){ action = new GetMonthBitstreamDownloaders(); };
            }
        }
        finally
        {
            if( (dspaceContext != null) && dspaceContext.isValid() )
            {
                dspaceContext.abort();
            }
        }

        return action;
	}
}
