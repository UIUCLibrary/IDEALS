/*
 * Utils.java
 *
 * Version: $Revision: 918 $
 *
 * Date: $Date: 2008-09-12 16:42:28 -0500 (Fri, 12 Sep 2008) $
 *
 * Copyright (c) 2005-2006, University of Illinois at Urbana-Champaign.  
 * All rights reserved.
 *
 * Frequently used Utility functions for IDEALS
 * 
 * 
 */
package edu.uiuc.ideals.dspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;

import org.dspace.eperson.Group;
import org.dspace.content.Collection;
import org.dspace.content.Community;

/**
 * Dspace Utility functions for IDEALS.
 * 
 * @author Tim Donohue
 * @version $Revision: 918 $
 */
public class DSpaceUtils
{
    /** log4j category */
    private static Logger log = Logger.getLogger(DSpaceUtils.class);
    
    /** The configuration properties */
    private static Properties idealsProps = null;
    
    /** Private Constructor */
    private DSpaceUtils()
    {
    }

    /**
     * Loads the IDEALS configuration file ([dspace]/config/ideals.cfg) and 
     * returns the corresponding Properties file (made up of all the 
     * configuration settings).
     * 
     * @return properties containing all IDEALS config settings
     */
    public static Properties loadIdealsConfig()
    {
        if (idealsProps != null)
        {
            return idealsProps;
        }
        
        InputStream propertiesStream = null;
        String configFile = "";
        String dspaceDir = null;

        // get ideals.cfg system config
        String configProperty = System.getProperty("ideals.configuration");

        
        // try to get path to DSpace directory
        try
        {
        	dspaceDir = ConfigurationManager.getProperty("dspace.dir");
        }
        catch(Exception e)
        {
        	//ignore errors...we'll try to get the ideals.cfg elsewhere
        }
        
        try
        {
        	if (dspaceDir != null && dspaceDir.length()>0)
            {
            	configFile = dspaceDir + File.separator + "config" 
            					+ File.separator + "ideals.cfg";
            	
            	log.info("Loading from DSpace config directory: " + configFile);
            	
            	propertiesStream = new FileInputStream(configFile);
            }
            // Has the default configuration location been overridden?
            else if (configProperty != null && configProperty.length()>0)
            {
                log.info("Loading system provided config property (-Dideals.configuration): " + configProperty);
                propertiesStream = new FileInputStream(configProperty);
            }
            // Load configuration from default location
            else
            {
                URL url = DSpaceUtils.class.getResource("/ideals.cfg");
                if (url != null)
                {
                    log.info("Loading from classloader: " + url);
                    propertiesStream = url.openStream();
                }
            }
        	
            // Read it in to load up our ideals.cfg file
            idealsProps = new Properties();
            idealsProps.load(propertiesStream);
            
            return idealsProps;
        }
        catch(Exception e)
        {
            log.error("IDEALS configuration file not found at " + configFile + ":", e);
            return null;
        }
    }
    
    /**
     * Builds a "pretty" name for a given Group in DSpace.
     * In particular, "renames" any groups with a name like:
     * COLLECTION_2_SUBMIT
     * COLLECTION_2_WORKFLOW_STEP_1
     * COLLECTION_2_ADMIN
     * COMMUNITY_1_ADMIN
     * 
     * This method builds a "pretty", human understandable name 
     * for any of the above types of internal DSpace groups 
     * 
     * @param context
     *          current Context object
     * @param group
     *          Group object
     * @return the "pretty" group name as a String
     */
    public static String buildGroupPrettyName(Context context, Group group)
    {
        if(group==null)
            return "";
        
        String prettyName = group.getName();
        String actualName = group.getName();
        
        Properties idealsProps = DSpaceUtils.loadIdealsConfig();
        
        try
        {
            //if an internal COLLECTION group (e.g. COLLECTION_2_SUBMIT, COLLECTION_1_WORKFLOW_STEP_1
            if(actualName.startsWith("COLLECTION_"))
            {
                //remove "COLLECTION_" from name
                String tempName = actualName.substring("COLLECTION_".length());
                
                //extract ID of collection (which should come before next "_")
                String collIDString = tempName.substring(0,tempName.indexOf("_"));
                int collectionID = Integer.parseInt(collIDString);
                
                //see what the "role" of this collection is (everything after next "_")
                String roleString = tempName.substring(tempName.indexOf("_")+1);

                //if ADMIN role
                if(roleString.equals("ADMIN"))
                {
                    prettyName = "Administrators of ";
                }//if SUBMIT role
                else if (roleString.equals("SUBMIT"))
                {
                    prettyName = "Submitters for ";
                }//if a WORKFLOW_STEP role
                else if (roleString.startsWith("WORKFLOW_STEP_"))
                {   
                    String stepDescription = "";
                    String stepNumber = roleString.substring("WORKFLOW_STEP_".length());
                    int step = Integer.parseInt(stepNumber);
                    
                    switch(step)
                    {
                        case 1:  stepDescription="Accept/Reject"; break;
                        case 2:  stepDescription="Accept/Reject/Edit Metadata"; break;
                        case 3:  stepDescription="Edit Metadata"; break;
                    }
                    
                    prettyName = stepDescription + " for ";
                }
                
                Collection collection = Collection.find(context, collectionID);
                
                if(collection!=null)
                {
                    prettyName += collection.getName();
                }
                else
                {
                    prettyName += "Collection " + collIDString;
                }
            } //end if COLLECTION group
            //else if a COMMUNITY_ group (e.g. COMMUNITY_ADMIN)
            else if(actualName.startsWith("COMMUNITY_"))
            {
                //remove "COMMUNITY_" from name
                String tempName = actualName.substring("COMMUNITY_".length());
                
                //extract ID of collection (which should come before next "_")
                String commIDString = tempName.substring(0,tempName.indexOf("_"));
                int communityID = Integer.parseInt(commIDString);
                
                //see what the "role" in this community is (everything after next "_")
                String roleString = tempName.substring(tempName.indexOf("_")+1);

                //if ADMIN role (currently this is the only COMMUNITY role)
                if(roleString.equals("ADMIN"))
                {
                    prettyName = "Administrators of ";
                }
                
                Community community = Community.find(context, communityID);
                
                if(community!=null)
                {
                    prettyName += community.getName();
                }
                else
                {
                    prettyName += "Community " + commIDString;
                }
            }
            //if a group which is "automated" by an Active Directory group
            else if(actualName.endsWith(idealsProps.getProperty("group.uiuc.automated-suffix")))
            {
                //remove the "[automated]" suffix from the "pretty name"
                prettyName = actualName.replace(idealsProps.getProperty("group.uiuc.automated-suffix"), "").trim();
            }
            else if(group.getID()==1)  //Administrator group is group #1
            {
                prettyName = "IDEALS Administrators";
            }
            else if(group.getID()==0) //Anonymous group is group #0
            {
                prettyName = "Everyone";
            }
        }
        catch(Exception e)
        {
            log.warn("Could not create 'pretty name' for Group " + group.getName());
        }
        return prettyName;
    }
    
    
    /**
     * Retrieves the associated Collection for groups with
     * names like:
     * COLLECTION_2_SUBMIT
     * COLLECTION_2_WORKFLOW_STEP_1
     * COLLECTION_2_ADMIN
     * 
     * If this group is not associated with a Collection,
     * null is returned 
     * 
     * @param context
     *          current Context object
     * @param group
     *          Group object
     * @return the associated Collection
     */
    public static Collection getAssociatedCollection(Context context, Group group)
    {
        if(group==null)
            return null;
        
        String groupName = group.getName();
        
        try
        {
            //if an internal COLLECTION group (e.g. COLLECTION_2_SUBMIT, COLLECTION_1_WORKFLOW_STEP_1
            if(groupName.startsWith("COLLECTION_"))
            {
                //remove "COLLECTION_" from name
                String tempName = groupName.substring("COLLECTION_".length());
                
                //extract ID of collection (which should come before next "_")
                String collIDString = tempName.substring(0,tempName.indexOf("_"));
                int collectionID = Integer.parseInt(collIDString);
                
                Collection collection = Collection.find(context, collectionID);
                
                return collection;
            } //end if COLLECTION group
            else
            {
                return null;
            }
        }
        catch(Exception e)
        {
            log.warn("Could not retrieve associated Collection for Group " + group.getName());
            return null;
        }
    }
    
    
    /**
     * Retrieves the associated Community for groups with
     * names like:
     * COMMUNITY_2_ADMIN
     * 
     * If this group is not associated with a Community,
     * null is returned 
     * 
     * @param context
     *          current Context object
     * @param group
     *          Group object
     * @return the associated Collection
     */
    public static Community getAssociatedCommunity(Context context, Group group)
    {
        if(group==null)
            return null;
        
        String groupName = group.getName();
        
        try
        {
            //if an internal COMMUNITY group (e.g. COMMUNITY_2_ADMIN)
            if(groupName.startsWith("COMMUNITY_"))
            {
                //remove "COMMUNITY_" from name
                String tempName = groupName.substring("COMMUNITY_".length());
                
                //extract ID of collection (which should come before next "_")
                String commIDString = tempName.substring(0,tempName.indexOf("_"));
                int communityID = Integer.parseInt(commIDString);
                
                Community community = Community.find(context, communityID);
                
                return community;
            } //end if COMMUNITY_ group
            else
            {
                return null;
            }
        }
        catch(Exception e)
        {
            log.warn("Could not retrieve associated Community for Group " + group.getName());
            return null;
        }
    }
    
    /**
     * Get DSpace Group which represents all campus users
     * 
     * @param context
     * 		The current DSpace context
     * 
     * @return a DSpace Group representing all campus users
     */
    public static Group getCampusGroup(Context context) throws SQLException, ServletException
    {
        // Load IDEALS configuration file (ideals.cfg)
        Properties idealsProps = edu.uiuc.ideals.dspace.DSpaceUtils.loadIdealsConfig();
        
        // get special group "UIUC Users"
        Group uiucGroup = Group.findByName(context, idealsProps.getProperty("group.uiuc.default"));
        
        if(uiucGroup==null)
        {
        	// Oops - UIUC Group isn't there.
            String error = "Missing UIUC 'special' group: " + idealsProps.getProperty("group.uiuc.default") + 
                            " An Administrator should create this group!";
            
            log.error(LogManager.getHeader(context, error, 
                    " An Administrator should create this group!"));
            
            throw new ServletException(error);
        }
        
        return uiucGroup;
    }
}
