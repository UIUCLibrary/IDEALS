/*
 * UIUCSpecialGroups.java
 *
 * Version: $Revision: 542 $
 *
 * Date: $Date: 2007-08-27 16:45:45 -0500 (Mon, 27 Aug 2007) $
 *
 * Copyright (c) 2005-2007, University of Illinois at Urbana-Champaign.  
 * All rights reserved.
 * 
 */
package edu.uiuc.dspace.authenticate;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.authenticate.AuthenticationMethod;

/**
 * Uses UIUC LDAP and Active Directory to determine
 * which "special" groups UIUC users should be added to for IDEALS
 *
 * @author Tim Donohue
 * @version $Revision: 542 $
 */
public class UIUCLDAPGroups
        implements AuthenticationMethod {

    /**
     * log4j category
     */
    private static Logger log = Logger.getLogger(UIUCLDAPGroups.class);

    /**
     * Separator for parameters stored in the users session.
     * This character appears between the Authentication class name and
     * the actual parameter.
     */
    public static String SESSION_PARAM_SEP = ".";

    /**
     * Field in Session which caches the user's "Special Groups".  These are
     * LDAP or Active Directory groups which the user is dynamically added
     * during the login session (e.g. UIUC Users, Library Faculty & Staff, etc.)
     */
    static final String SESSION_SPECIAL_GROUPS = UIUCLDAPGroups.class.getName() +
            SESSION_PARAM_SEP + "specialGroups";

    /**
     * We don't care about self registering here.
     * Let a real auth method return true if it wants.
     */
    public boolean canSelfRegister(Context context,
                                   HttpServletRequest request,
                                   String username)
            throws SQLException {
        //let the real authorization method determine this
        return false;
    }

    /**
     * Initialize new EPerson.
     * Policy: Require certificate access for MIT users.
     */
    public void initEPerson(Context context, HttpServletRequest request,
                            EPerson eperson)
            throws SQLException {
        //let the real authorization method initialize e-People
    }

    /**
     * Predicate, is user allowed to set EPerson password.
     * Anyone whose email address ends with @mit.edu must use a Web cert
     * to log in, so can't set a password
     */
    public boolean allowSetPassword(Context context,
                                    HttpServletRequest request,
                                    String username)
            throws SQLException {
        //let the real authorization method determine this
        return false;
    }

    /**
     * This is an implicit method, although it doesn't do authentication.
     */
    public boolean isImplicit() {
        return true;
    }

    /**
     * Check Active Directory and LDAP for any special groups
     * this UIUC user should be added to.
     */
    public int[] getSpecialGroups(Context context, HttpServletRequest request) {
        int[] specialGroups;
        // User has not successfuly authenticated via shibboleth.
        if ( request == null ||
                context.getCurrentUser() == null ||
                request.getSession().getAttribute("shib.authenticated") == null ) {
            return new int[0];
        }
        //if a UIUC user is logged in, check to see if special groups are cached in session
        if (UIUCPersonUtils.isCampusUser(context.getCurrentUser())) {
            // Check session cache for list of Special Groups
            // This ensures we are only querying AD or LDAP ONCE each session
            specialGroups = (int[]) request.getSession().getAttribute(SESSION_SPECIAL_GROUPS);

            //if we found them in session, just return them.
            if (specialGroups != null) {
                log.debug("Found special groups in session");
                return specialGroups;
            }
        }

        //Retrieve a list of special UIUC groups to add user to
        ArrayList groupList = UIUCPersonUtils.getCampusSpecialGroups(context, request);

        if (groupList != null && groupList.size() > 0) {
            // create our array of group IDs
            specialGroups = new int[groupList.size()];
            String groupNames = "";
            for (int i = 0; i < groupList.size(); i++) {
                //Build list of all special campus groups user was added to
                if (groupNames.length() == 0)
                    groupNames = ((Group) groupList.get(i)).getName();
                else
                    groupNames = groupNames + ", " + ((Group) groupList.get(i)).getName();

                specialGroups[i] = ((Group) groupList.get(i)).getID();
            }

            //if a UIUC user is logged in, cache special groups for remainder of user's logged in session
            if (UIUCPersonUtils.isCampusUser(context.getCurrentUser())) {
                request.getSession().setAttribute(SESSION_SPECIAL_GROUPS, specialGroups);
                //Log the campus groups that the user was added to for this session
                log.info(LogManager
                        .getHeader(context, "campus-lookup", "Campus-Special-Groups=" + groupNames));
            }

            return specialGroups;
        } else {
            return new int[0]; //no special groups found!
        }

    }

    /**
     * This method is not used.
     * This class is only for special groups and enforcement of cert policy.
     * Use X509Authentication to authenticate.
     *
     * @return One of: SUCCESS, BAD_CREDENTIALS, NO_SUCH_USER, BAD_ARGS
     */
    public int authenticate(Context context,
                            String username,
                            String password,
                            String realm,
                            HttpServletRequest request)
            throws SQLException {
        return BAD_ARGS;
    }

    /*
     * Returns URL to which to redirect to obtain credentials (either password
     * prompt or e.g. HTTPS port for client cert.); null means no redirect.
     *
     * @param context
     *  DSpace context, will be modified (ePerson set) upon success.
     *
     * @param request
     *  The HTTP request that started this operation, or null if not applicable.
     *
     * @param response
     *  The HTTP response from the servlet method.
     *
     * @return fully-qualified URL
     */
    public String loginPageURL(Context context,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        return null;
    }

    public String loginPageTitle(Context context) {
        return null;
    }

    /**
     * Clear anything that has been cached since the user logged in
     * (in this case, within the user's session)
     * <p/>
     * This method should be called after logout to ensure this cached
     * info doesn't accidentally carry over to another session.
     *
     * @param request Current HttpServletRequest object
     */
    public static void clearCache(HttpServletRequest request) {
        request.getSession().removeAttribute(SESSION_SPECIAL_GROUPS);
    }

}
