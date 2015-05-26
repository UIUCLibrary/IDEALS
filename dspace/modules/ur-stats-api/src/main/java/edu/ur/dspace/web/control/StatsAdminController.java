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
package edu.ur.dspace.web.control;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import org.apache.log4j.Logger;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.app.webui.util.UIUtil;
import org.dspace.core.Context;
import edu.ur.dspace.web.action.Action;

import edu.ur.dspace.web.action.stats.admin.StatsAdminRequestHelper;

public class StatsAdminController extends HttpServlet {
    
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;
    
	  /** Logger */
	  private static Logger log = Logger.getLogger(StatsAdminController.class);
	  
	  // Initializes the servlet.
	  public void init(ServletConfig config) throws 
	    ServletException {
	    super.init(config);
	  }

	  // Destroys the servlet.
	  public void destroy() {
	  }

	  /** Processes requests for both HTTP  
	   * <code>GET</code> and <code>POST</code> methods.
	   * @param request servlet request
	   * @param response servlet response
	   */
	  protected void processRequest(HttpServletRequest 
	    request, HttpServletResponse response)
	    throws ServletException, java.io.IOException {
	    String page = null;

	    try {

              // put the current eperson in the request 
              Context dspaceContext = UIUtil.obtainContext(request);
              dspaceContext.complete();
              request.removeAttribute("dspace.context");

              log.debug( "Processing the request in Stats Admin Controller" );

	      // Use a helper object to gather parameter 
	      // specific information.
	      StatsAdminRequestHelper helper = new StatsAdminRequestHelper();

              // get the action for this request
	      Action actionHelper= helper.getAction(request);

	      // Command helper perform custom operation
	      page = actionHelper.execute(request, response);

              log.debug( "The page is " + page );

	    }
	    catch (Exception e) {
	      log.info(
	        "StatsController:exception : " + 
	        e.getMessage());

               log.fatal("Error in StatsController", e);
	      
	       // forward to the error page if we have an error
	       JSPManager.showInternalError(request, response);
	    }

	    // dispatch control to view
	    dispatch(request, response, page);
	  }

	  /** Handles the HTTP <code>GET</code> method.
	   * @param request servlet request
	   * @param response servlet response
	   */
	  protected void doGet(HttpServletRequest request, 
	    HttpServletResponse response)
	    throws ServletException, java.io.IOException {
	      processRequest(request, response);
	  }

	  /** Handles the HTTP <code>POST</code> method.
	   * @param request servlet request
	   * @param response servlet response
	   */
	  protected void doPost(HttpServletRequest request, 
	    HttpServletResponse response)
	    throws ServletException, java.io.IOException {
	        processRequest(request, response);
	  }

	  /** Returns a short description of the servlet */
	  public String getServletInfo() {
	    return "Front Controller Pattern" + 
	      " Servlet Front Strategy Example";
	  }

	  protected void dispatch(HttpServletRequest request, 
	    HttpServletResponse response,
	    String page) 
	  throws  javax.servlet.ServletException, 
	    java.io.IOException {
	    RequestDispatcher dispatcher = 
	      getServletContext().getRequestDispatcher(page);
	    dispatcher.forward(request, response);
	  }
	}
