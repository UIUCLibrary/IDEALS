/*
 * LDAPQuery.java
 *
 * Version: $Revision: 502 $
 *
 * Date: $Date: 2007-07-31 16:20:59 -0500 (Tue, 31 Jul 2007) $
 *
 * Copyright (c) 2009, University of Illinois at Urbana-Champaign.
 * All rights reserved.
 */
package edu.uiuc.dspace.authenticate;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;
import org.dspace.core.LogManager;

/**
 * Queries LDAP system (or Active Directory) for basic user
 * information (e.g. full name, faculty/staff/student status)
 * <p>
 * 
 * @author Tim Donohue
 * @version $Revision: 502 $
 */
public class LDAPQuery
{
    /** Properties from the IDEALS config file (ideals.cfg) **/
    private static Properties idealsProps;

    static
    {
        // Load IDEALS configuration file (ideals.cfg)
        idealsProps = edu.uiuc.ideals.dspace.DSpaceUtils.loadIdealsConfig();
    }

    /** LDAP Connection's directory context */
    private DirContext connectionContext = null;

    /** LDAP config we are using (from ideals.cfg) */
    private String LDAPConfigType = null;

    /** Current user we are searching in LDAP */
    private String currentUser = null;

    /** Current user's groups in LDAP */
    private Set currentUserGroups = null;

    /** Current user's attributes in LDAP */
    private HashMap currentUserAttributes = null;

    /** Separator for multi-valued LDAP attributes **/
    public static String MULTI_VALUE_SEPARATOR=" ~~~ ";

    /** log4j category */
    private static Logger log = Logger.getLogger(LDAPQuery.class);
    
    
    /**
     *  Returns an open LDAP connection, based on the 'default' settings
     *  in ideals.cfg.
     *  <P>
     *  This means that the 'default' type is used, which uses the 'ldap.default.url'
     *  and all corresponding 'ldap.default.*' settings.
     *  
     *  @return
     *      An initialized LDAPQuery object
     *      
     *  @throws NamingException if error establishing LDAP connection
     */
    public static LDAPQuery openContext() throws javax.naming.NamingException
    {
        return openContext(null);
    }

    /**
     *  Returns an open LDAP connection, based on the settings
     *  in ideals.cfg and the specified 'type' identifier
     *  <P>
     *  By default, the 'default' type is used, which uses the 'ldap.default.url'
     *  and all corresponding 'ldap.default.*' settings.
     *
     *  @param type
     *      Type of LDAP connection to open.  When specified, this loads
     *      the 'ldap.[type].url' (and corresponding 'ldap.[type].*' settings)
     *      from ideals.cfg.
     *  @return
     *      An initialized LDAPQuery object
     *
     *  @throws NamingException if error establishing LDAP connection
     */
    public static LDAPQuery openContext(String type) throws javax.naming.NamingException
    {
        // By default, use the 'default' type settings
        if(type==null || type.length()==0) type = "default";

        String ldapPrefix = "ldap." + type;

        // Get the LDAP provider URL (i.e. the LDAP server we're connecting to)
        String ldap_provider_url = idealsProps.getProperty(ldapPrefix + ".url");

        // Set up environment for creating initial context
        Hashtable<String, String> env = new Hashtable<String, String>(11);
        env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(javax.naming.Context.PROVIDER_URL, ldap_provider_url);

        // Specify connection timeout in milliseconds (e.g. 5000 = 5 seconds)
        if(idealsProps.getProperty(ldapPrefix + ".timeout")!=null)
          env.put("com.sun.jndi.ldap.connect.timeout", idealsProps.getProperty(ldapPrefix + ".timeout"));

        //Specify SSL connection (must use port 636 for SSL)
        if(idealsProps.getProperty(ldapPrefix + ".url")!=null && idealsProps.getProperty(ldapPrefix + ".url").endsWith(":636"))
          env.put(javax.naming.Context.SECURITY_PROTOCOL, "ssl");

        // Add Authentication type - should be either 'none', 'simple' or 'strong'
        env.put(javax.naming.Context.SECURITY_AUTHENTICATION, idealsProps.getProperty(ldapPrefix + ".login.type"));

        // If specified, add in the username and password
        if(idealsProps.getProperty(ldapPrefix + ".login.user")!=null)
          env.put(javax.naming.Context.SECURITY_PRINCIPAL, idealsProps.getProperty(ldapPrefix + ".login.user"));
        if(idealsProps.getProperty(ldapPrefix + ".login.password")!=null)
          env.put(javax.naming.Context.SECURITY_CREDENTIALS, idealsProps.getProperty(ldapPrefix + ".login.password"));

        //Initialize new LDAPQuery object
        LDAPQuery ldap = new LDAPQuery();
        
        // Create initial context
        ldap.connectionContext = new InitialDirContext(env);
        // Save the current LDAP config we are using (from ideals.cfg)
        ldap.LDAPConfigType = type;

        return ldap;
    }

    /**
     *  Closes the currently open LDAP connection/context
     */
    public void closeContext()
    {
        clearCurrentUserData();

        // Close the context when we're done
        try
        {
            if (this.connectionContext != null)
                this.connectionContext.close();
        }
        catch (NamingException e)
        {
           //do nothing
        }
    }

    /**
     *  Clear the cache of the last user's data.
     *
     *  This cache is kept in to avoid hitting LDAP too frequently for
     *  the same user information
     */
    protected void clearCurrentUserData()
    {
       this.currentUser = null;
       this.currentUserAttributes = null;
       this.currentUserGroups = null;
    }

    /**
     * Contact to campus Active Directory via an LDAP connection to
     * determine what AD groups this user is a member of.
     * This method performs a recursive search to also find groups
     * within groups, etc.
     *
     * @param member
     *          the user's NetID or an AD Group name
     * @param domain
     *          domain to be checked "UIUC" or "UOFI"
     * 
     * @return a Set of strings which give all the unique LDAP
     *          group names this user is a member of
     */
    public Set getGroupMemberships(String member, String domain)
    {
        //First, attempt to load groups from a cache.
        if(getGroupCache(member)!=null)
        {
            log.debug("Loading cached LDAP Group Memberships for '" + member + "'");
            return getGroupCache(member);
        }
        //initialize set of group names
        Set<String> groupNames = new HashSet<String>();

        try
        {
            log.debug("Searching LDAP Group Memberships for '" + member + "'");
            //actually perform the search (recursively) in LDAP
            //(This determines all groups the member belongs to, and all groups
            // those groups belong to, etc.  The final result is saved to "groupNames")
            searchGroupMembership(member, domain, null, groupNames);

            //cache these groups (in case same search is performed later)
            setGroupCache(member, groupNames);
        }
        catch (NamingException e)
        {
            log.warn(LogManager.getHeader(null,
                                "LDAP_group_lookup", "type=failed_LDAP_search "+e));
        }

        return groupNames;
    }

    /**
     * Attempt to load group memberships from cache
     *
     * @param member 
     *          the user's UIUC NetID or a AD Group name
     * @return a Set of strings which give all the unique LDAP
     *          group names this user is a member of
     */
    protected Set getGroupCache(String member)
    {
       if(this.currentUser!=null && member.equals(this.currentUser))
       {
         return this.currentUserGroups;
       }

       return null;
    }

    /**
     * Attempt to cache group memberships for specified user
     *
     * @param member
     *          the user's UIUC NetID or a AD Group name
     * @param groups
     *          Set of strings which give all the unique LDAP
     *          group names this user is a member of
     */
    protected void setGroupCache(String member, Set groups)
    {
       //cache the current user name & groups in current object
        resetCurrentUser(member);
        this.currentUserGroups = groups;
    }

    private void resetCurrentUser(String member) {
        if(!member.equals(this.currentUser))
        {
          clearCurrentUserData();
          this.currentUser = member;
        }
    }

    /**
     * Search current campus LDAP connection to
     * determine what groups the current user/group is a member of.
     * <P>
     * This method works recursively, and constantly updates
     * the set of "groupMemberships".  Once this method fully completes,
     * the Set passed in as the "groupMemberships" will contain a list
     * of the names of all AD Groups this member belongs to.
     *
     * @param member
     *          the user's NetID or a AD Group name
     * @param domain
     *          domain to be checked "UIUC" or "UOFI"
     * @param adSearchContext
     *          the active directory context to search for the 'member' under.
     *          (if unspecified, defaults to
     *          "OU=Campus Accounts,DC=ad,DC=uiuc,DC=edu")
     * @param groupMemberships
     *          current set of groups which this user has been found
     *          to be a member of
     *
     * @throws NamingException if an error contacting LDAP or closing connection
     */
    protected void searchGroupMembership(String member, String domain, String adSearchContext, Set<String> groupMemberships)
        throws NamingException
    {
        //default context for searching in AD
        final String DEFAULT_CONTEXT = idealsProps.getProperty("ldap." + LDAPConfigType + ".search.context");

        // We're going to look at the field where NetID is stored
        String ldap_id_field = idealsProps.getProperty("ldap." + LDAPConfigType + ".field.id");
        String ldap_search_context = (adSearchContext!=null && adSearchContext.length()>0)? adSearchContext : DEFAULT_CONTEXT;

        log.debug("Searching LDAP for '" + member + "' in context '" + ldap_search_context + "'");

        //initialize set of group names, if uninitialized
        if(groupMemberships==null)
            groupMemberships = new HashSet<String>();

        //we are looking at the "memberOf" field in Active Directory
        //since this lists all groups this user is a member of
        String ad_group_memberships = idealsProps.getProperty("ldap." + domain + ".field.groups");

        //search based on the user's NetID
        Attributes matchAttributes = new BasicAttributes(true);
        matchAttributes.put(new BasicAttribute(ldap_id_field, member));

        String attlist[] = {ad_group_memberships};

        NamingEnumeration answer = null;

        // look up attributes in attlist
        try
        {
            answer = addGroupsFromLDAP(domain, groupMemberships, ldap_search_context, matchAttributes, attlist);
        }
        catch (NamingException e)
        {
            //Since we are recursively searching, ignore NamingExceptions that occur during search...
            // we will just return no results if they occur -- this ensures our search completes no matter what
        }
        finally
        {
            //close search results
            if(answer!=null)
                answer.close();
        }
    }

    private NamingEnumeration addGroupsFromLDAP(String domain, Set<String> groupMemberships, String ldap_search_context, Attributes matchAttributes, String[] attlist) throws NamingException {
        //actually search LDAP
        NamingEnumeration answer = this.connectionContext.search(ldap_search_context, matchAttributes, attlist);

        //Loop through the search results
        while (answer.hasMore())
        {
            SearchResult sr = (SearchResult) answer.next();

            //Print out the groups
            Attributes attributes = sr.getAttributes();
            if (attributes != null)
            {
                //get all matching attributes
                NamingEnumeration ae = attributes.getAll();
                while(ae.hasMore())
                {
                    Attribute attr = (Attribute)ae.next();

                    //loop through attribute values (which are the group names)
                    NamingEnumeration e = attr.getAll();
                    while(e.hasMore())
                    {
                        String groupCN = (String)e.next();
                        String groupName = parseGroupName(groupCN.trim(), domain);

                        //if this Group is not already in our list, add it and recurse!
                        if(!groupMemberships.contains(groupName))
                        {
                            groupMemberships.add(groupName);

                            //get the updated AD search context from the group CN
                            String searchContext = groupCN.substring(groupCN.indexOf(',')+1);

                            //recurse to check for nested groups in LDAP / AD
                            searchGroupMembership(groupName, domain, searchContext, groupMemberships);
                        }
                    }//end while

                    //close attribute values
                    e.close();

                }//end while

                //close list of attributes
                ae.close();
            }
        }//end while
        return answer;
    }

    /**
     * Parse out the actual Group name from the full CN.
     * <P>
     * For example, given the following CN:
     * CN=Library Faculty and Staff,OU=Units,OU=Library,DC=ad,DC=uiuc,DC=edu
     * <P>
     * The group name parsed out should be:
     * "Library Faculty and Staff"
     *
     * @param fullCN
     *      the full name of the group in LDAP
     * @return the actual group name
     **/
    private String parseGroupName(String fullCN, String domain)
    {
        //The Group name is contained in the first CN!
        Pattern pattern = Pattern.compile(idealsProps.getProperty("ldap." + domain + ".pattern.group"));
        Matcher matcher = pattern.matcher(fullCN);
        matcher.find();

        String adGroupName = "";
        try
        {
            // Group name is whatever comes after first "CN="
            adGroupName = matcher.group(Integer.parseInt(idealsProps.getProperty("ldap." + domain + ".pattern.group.match.index")));
        }
        catch(Exception e)
        {
            //For some reason the regular expression didn't match this group
            log.warn("Failed to parse out Active Directory Group Name for the following:" + fullCN);
        }

        return adGroupName;
    }

    /**
     * Search current campus LDAP connection to
     * find specified user's information/data.
     * This method only returns the *first* value for multi-valued
     * fields.  To retrieve all values, use getFieldValues().
     *
     * @param member
     *          the user's UIUC NetID
     * @param fieldName
     *          the name of the LDAP field we want for this user
     *
     * @return the first value found or null
     * @throws NamingException if an error contacting LDAP or closing connection
     */
    public String getFieldValue(String member, String fieldName)
        throws NamingException
    {
        //First, get all values
        String[] values = getFieldValues(member, fieldName);
        
        if(values!=null && values.length>0)
        {
          return values[0];
        }
        else
          return null;
    }


    /**
     * Search current campus LDAP connection to
     * find specified user's information/data.
     *
     * @param member
     *          the user's UIUC NetID
     * @param fieldName
     *          the name of the LDAP field we want for this user
     *
     * @return array of field values or null
     * @throws NamingException if an error contacting LDAP or closing connection
     */
    public String[] getFieldValues(String member, String fieldName)
        throws NamingException
    {
        //First, get all user data (from LDAP or from cache)
        HashMap userData = getUserData(member);

        //Now, get values of the specified field
        String values = (String) userData.get(fieldName);

        if(values!=null && values.length()>0)
        {
          //split the values on the separator
          return values.split(MULTI_VALUE_SEPARATOR);
        }
        else
          return null;
    }

    /**
     * Search current campus LDAP connection to
     * find all of a user's information/data.
     *
     * @param member
     *          the user's UIUC NetID or a AD Group name
     *
     * @return HashMap of attribute names & values found for this user in LDAP
     * @throws NamingException if an error contacting LDAP or closing connection
     */
    public HashMap getUserData(String member)
        throws NamingException
    {
        //Pass 'null' as the LDAP search context,
        //so that the default context (from ideals.cfg) will be used
        return getUserData(member, null);
    }
    
    /**
     * Search current campus LDAP connection to
     * find all of a user's information/data.
     *
     * @param member
     *          the user's UIUC NetID or a AD Group name
     * @param ldapSearchContext
     *          the LDAP context to search for the 'member' under.
     *          (if unspecified, defaults to
     *          "OU=people,DC=uiuc,DC=edu")
     *
     * @return HashMap of attribute names & values found for this user in LDAP
     * @throws NamingException if an error contacting LDAP or closing connection
     */
    public HashMap getUserData(String member, String ldapSearchContext)
        throws NamingException
    {
        // default context for searching in LDAP
        final String DEFAULT_CONTEXT = idealsProps.getProperty("ldap." + LDAPConfigType + ".search.context");

        // We're going to search on the field where NetID is stored
        String ldap_id_field = idealsProps.getProperty("ldap." + LDAPConfigType + ".field.id");
        String ldap_search_context = (ldapSearchContext!=null && ldapSearchContext.length()>0)? ldapSearchContext : DEFAULT_CONTEXT;

        // If the user data can be found in local cache, return it from cache immediately.
        if(getUserDataCache(member)!=null)
        {
            log.debug("Loading cached LDAP info for '" + member + "' in context '" + ldap_search_context + "'");
            return getUserDataCache(member);
        }

        log.debug("Searching LDAP for '" + member + "' in context '" + ldap_search_context + "'");

        //search based on the user's NetID
        Attributes matchAttributes = new BasicAttributes(true);
        matchAttributes.put(new BasicAttribute(ldap_id_field, member));

        //Initialize our results HashMap
        HashMap<String, String> userInfo = new HashMap<String,String>();

        NamingEnumeration answer = null;

        // look up attributes in attlist
        try
        {
            answer = addUserDataFromLDAP(member, ldap_search_context, matchAttributes, userInfo);
        }
        catch (NamingException e)
        {
            //If there is an error, we want to log it...but, as the LDAP information is not
            // necessary to access IDEALS, we don't want to throw the actual error.
            log.error("Error retrieving user information for '" + member + "' from LDAP", e);
        }
        finally
        {
            //close search results
            if(answer!=null)
                answer.close();
        }

        //return all LDAP attributes found as a hashmap (key: attribute name, value: attribute value)
        if (userInfo.isEmpty())
          return null;
        else
          return userInfo;
    }

    private NamingEnumeration addUserDataFromLDAP(String member, String ldap_search_context, Attributes matchAttributes, HashMap<String, String> userInfo) throws NamingException {
        //actually search LDAP based on NetID, and return all attributes
        NamingEnumeration answer = this.connectionContext.search(ldap_search_context, matchAttributes);

        //Loop through the search results
        while (answer.hasMore())
        {
            SearchResult sr = (SearchResult) answer.next();

            //Print out the groups
            Attributes attributes = sr.getAttributes();
            if (attributes != null)
            {
                //get all matching attributes
                NamingEnumeration ae = attributes.getAll();
                while(ae.hasMore())
                {
                    Attribute attr = (Attribute)ae.next();
                    String attrName = attr.getID();

                    //loop through each of this attribute's values
                    NamingEnumeration e = attr.getAll();
                    while(e.hasMore())
                    {
                        String attrValue = (String)e.next();

                        //add to our hashmap of user info
                        if(userInfo.containsKey(attrName))
                        {
                          //separate multiple values by a common separator
                          String currentValue = userInfo.get(attrName);
                          userInfo.put(attrName, currentValue + MULTI_VALUE_SEPARATOR + attrValue);
                        }
                        else
                          userInfo.put(attrName, attrValue);
                    }//end while

                    //close attribute values
                    e.close();

                }//end while

                //close list of attributes
                ae.close();
            }
        }//end while

        //cache this user data (in case same search is performed later)
        setUserDataCache(member, userInfo);
        return answer;
    }

    /**
     * Attempt to load user data from cache
     *
     * @param member
     *          the user's UIUC NetID or a AD Group name
     *
     * @return HashMap of attribute names & values found for this user from LDAP
     */
    protected HashMap getUserDataCache(String member)
    {
       if(this.currentUser!=null && member.equals(this.currentUser))
       {
         return this.currentUserAttributes;
       }

       return null;
    }

    /**
     * Attempt to cache LDAP data for specified user
     *
     * @param member
     *          the user's UIUC NetID or a AD Group name
     *
     * @param userInfo
     *          HashMap of attribute names & values found for this user from LDAP
     */
    protected void setUserDataCache(String member, HashMap userInfo)
    {
       //cache the current user info in current object
        resetCurrentUser(member);
        this.currentUserAttributes = userInfo;
    }
    
}