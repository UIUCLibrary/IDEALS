/*
 * DSpaceGroupSelector.java
 *
 * Version: $Revision$
 *
 * Date: $Date$
 *
 * Copyright (c) 2009, University of Illinois at Urbana-Champaign.
 * All rights reserved.
 */

package edu.uiuc.dspace.app.xmlui.aspect.ideals;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.sitemap.PatternException;

import org.apache.log4j.Logger;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

/**
 * Cocoon "selector" which performs different actions based on whether the
 * current user is a member of a specific DSpace Group
 * 
 * This selector can be used in DSpace XMLUI sitemap.xmap files to perform
 * custom actions whenever the user is a member of a specific group, or ensure
 * that only members of specific groups can get to parts of application.
 * Usage is similar to the following:
 *
 * <map:select type="DSpaceGroupSelector">
 *    <map:when test="MyGroup1">
 *	     ..contents only performed if user is member of "MyGroup1"..
 *    </map:when>
 *    <map:when test="MyOtherGroup">
 *	     ..contents only performed if user is member of "MyOtherGroup"
 *         AND NOT a member of "MyGroup1"..
 *    </map:when>
 *    <map:otherwise>
 *       ..contents only performed if user isn't in any groups listed above..
 *    </map:otherwise>
 * </map:select>
 *
 * 
 * @author Tim Donohue
 */

public class DSpaceGroupSelector extends AbstractLogEnabled implements Selector
{
    private static Logger log = Logger.getLogger(DSpaceGroupSelector.class);

    /**
     * Check and see if user is member of a particular DSpace group.
     * The DSpace group name is set via a required <map:parameter/> tag.
     * 
     * @param expression
     *            the test expression
     * @param objectModel
     *            environment passed through via cocoon
     * @param parameters
     *            the params passed in (<map:parameter/>) in sitemap.xmap
     * @return true if expression matches, false otherwise
     */
    public boolean select(String expression, Map objectModel,
            Parameters parameters)
    {

      try
      {

        Context context = ContextUtil.obtainContext(objectModel);

        EPerson eperson = context.getCurrentUser();
        // No one is authenticated, so no match found
        if (eperson == null)
          return false;

        //Get list of all Group memberships (including groups within groups, etc.)
        Group[] memberships = Group.allMemberGroups(context, context.getCurrentUser());

        //If not a member of any groups, then there is no match found
        if (!(memberships.length > 0))
          return false;

        Map<String, String> result = new HashMap<String, String>();
        for (Group group: memberships)
        {
            //If we find a group name that matches our "test" expression, then we have a match!
            if(group.getName().equalsIgnoreCase(expression.trim()))
            {
               return true;
            }
        }
      }
      catch(Exception e)
      {
          // Log it and returned no match.
          log.error("Error determining user group memberships: " 
                    + e.getMessage());

      }

      //default to no match found
      return false;
    }
}
