/*
 * UIUCPersonUtils.java
 *
 * Version: $Revision: 578 $
 *
 * Date: $Date: 2007-10-09 10:42:28 -0500 (Tue, 09 Oct 2007) $
 *
 * Copyright (c) 2005-2009, University of Illinois at Urbana-Champaign.
 * All rights reserved.
 */
package edu.uiuc.dspace.authenticate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

/**
 * Contains utility classes for gathering information
 * about UIUC people logged into IDEALS.
 * <p/>
 *
 * @author Tim Donohue
 * @version $Revision: 578 $
 */
public class UIUCPersonUtils {
    /**
     * log4j category
     */
    private static Logger log = Logger.getLogger(UIUCPersonUtils.class);

    /**
     * Session attribute which stores the values received from the
     * UIUC LDAP system for the current user.
     */
    public static String UIUC_LDAP_FIELD = "UIUC-LDAP-Fields";

    /**
     * Separator for LDAP parameters stored in the users session.
     * This character appears between the Authentication class name and
     * the actual parameter.
     */
    public static String SESSION_PARAM_SEP = ".";


    /**
     * Properties from the IDEALS config file (ideals.cfg) *
     */
    private static Properties idealsProps;

    /**
     * Initialization:
     *  Load IDEALS properties
     */
    static {
        // Load IDEALS configuration file (ideals.cfg)
        idealsProps = edu.uiuc.ideals.dspace.DSpaceUtils.loadIdealsConfig();
    }


    /**
     * Check to see if a DSpace EPerson is a campus user
     *
     * @param person A DSpace EPerson
     * @return true if eperson is a campus user, false otherwise
     */
    public static boolean isCampusUser(EPerson person) {
        //An EPerson is a campus user if he/she has a campus email address
        if (person != null && isCampusEmail(person.getEmail()))
            return true;
        else
            return false;
    }

    /**
     * Check to see if user has a UIUC email address
     *
     * @param email the user's email address
     * @return true if the user has a UIUC email address
     */
    public static boolean isCampusEmail(String email) {
        boolean hasUIUCEmail = false;

        //Read valid email suffixes from ideals.cfg
        String suffixes = idealsProps.getProperty("email.suffixes");
        String[] validSuffixes = suffixes.split(", ");

        if (email != null && email.length() > 0) {
            for (int i = 0; i < validSuffixes.length; i++) {
                if (email.toLowerCase().endsWith(validSuffixes[i].toLowerCase())) {
                    hasUIUCEmail = true;
                    break;
                }
            }
        }

        return hasUIUCEmail;
    }


    /**
     * Builds the UIUC email address based on a valid UIUC Net ID.
     *
     * @param netid the user's net id
     * @return true if the user has a UIUC email address
     */
    public static String getCampusEmail(String netid) {
        String email = netid;

        //first, verify the netid is not already an email address
        if (!email.contains("@")) {
            //append primary email Suffix (e.g. "@illinois.edu) if netID 
            //is not a valid email address
            //(This should work fine with UIS or UIC, since their netIDs
            // will actually come across as netid@uis.edu or netid@uic.edu)
            email += idealsProps.getProperty("email.suffix.primary");
        }

        return email;
    }

    /**
     * Return a list of all UIUC Special Groups which the
     * current user should be added to within IDEALS.
     * <p/>
     * The groups a user should be added to is dependant on
     * their information from LDAP, and on whether they are actually
     * faculty/staff/students or just on a campus computer
     *
     * @param context current DSpace context
     * @param request current HTTP request
     * @return ArrayList of UIUC special groups in IDEALS to add user to
     */
    public static ArrayList getCampusSpecialGroups(Context context, HttpServletRequest request) {
        ArrayList<Group> groupList = new ArrayList<Group>();

        try {
            //if currently logged in user is a campus user
            if (UIUCPersonUtils.isCampusUser(context.getCurrentUser())) {
                // Load user's info from LDAP and store in session
                UIUCPersonUtils.loadUserFields(request, context.getCurrentUser().getNetid());

                // get special group "UIUC Users"
                Group uiucGroup = Group.findByName(context, idealsProps.getProperty("group.uiuc.default"));
                // get special group "UIUC Staff"
                Group staffGroup = Group.findByName(context, idealsProps.getProperty("group.uiuc.staff"));
                // get special group "UIUC Grad Students"
                Group gradGroup = Group.findByName(context, idealsProps.getProperty("group.uiuc.grads"));
                // get special group "UIUC UnderGrad Students"
                Group underGradGroup = Group.findByName(context, idealsProps.getProperty("group.uiuc.undergrads"));

                //check if any of these groups are not created!
                String missingGroups = "";
                if (uiucGroup == null) missingGroups += " " + idealsProps.getProperty("group.uiuc.default") + ",";
                if (staffGroup == null) missingGroups += " " + idealsProps.getProperty("group.uiuc.staff") + ",";
                if (gradGroup == null) missingGroups += " " + idealsProps.getProperty("group.uiuc.grads") + ",";
                if (underGradGroup == null)
                    missingGroups += " " + idealsProps.getProperty("group.uiuc.undergrads") + ",";

                if (missingGroups.length() > 0) {
                    logMissingGroups(context, missingGroups);
                }
                /* -------------------------------------------------------*
                 * [Step 1:] everyone from UIUC goes into "UIUC Users" group
                 * -------------------------------------------------------*/
                if (uiucGroup != null) groupList.add(uiucGroup);

                /* -------------------------------------------------------*
                 * [Step 2:] Determine Faculty/Staff/Student groups
                 * -------------------------------------------------------*/
                //if UIUC staff, add to "UIUC Staff"
                if (UIUCPersonUtils.isStaff(request) && staffGroup != null) groupList.add(staffGroup);
                    //else if UIUC grad student, add to "UIUC Grad Students"
                else if (UIUCPersonUtils.isGradStudent(request) && gradGroup != null) groupList.add(gradGroup);
                    //else if UIUC undergrad student, add to "UIUC UnderGrad Students"
                else if (UIUCPersonUtils.isUnderGradStudent(request) && underGradGroup != null)
                    groupList.add(underGradGroup);

                /* -------------------------------------------------------*
                 * [Step 3:] Look for Departmental Groups
                 * -------------------------------------------------------*/
                //check if there are DSpace groups which corresponds to this
                //user's primary department at UIUC
                List<Group> deptGroupList = UIUCPersonUtils.checkForDepartmentGroups(context, request);

                //if corresponding DSpace groups were found, add them
                //to our list of DSpace groups
                if (deptGroupList != null && deptGroupList.size() > 0) {
                    groupList.addAll(deptGroupList);
                }

                /* -------------------------------------------------------*
                 * [Step 4:] Look for Active Directory Groups in LDAP
                 * -------------------------------------------------------*/
                List<Group> ldapGroupList = UIUCPersonUtils.checkForLDAPGroups(context);

                //if corresponding DSpace groups were found, add them
                //to our list of DSpace groups
                if (ldapGroupList != null && ldapGroupList.size() > 0) {
                    groupList.addAll(ldapGroupList);
                }

            }//end if logged in with UIUC email address
            else if (UIUCPersonUtils.onCampusComputer(request)) //else if on a UIUC Computer, add to UIUC Users
            {
                //get special group "UIUC Users"
                Group uiucGroup = Group.findByName(context, idealsProps.getProperty("group.uiuc.default"));

                //check if any of these groups are not created!
                String missingGroups = "";
                if (uiucGroup == null) missingGroups += " " + idealsProps.getProperty("group.uiuc.default") + ",";

                if (missingGroups.length() > 0) {
                    logMissingGroups(context, missingGroups);
                } else {
                    //everyone connecting from UIUC goes into "UIUC Users" group
                    groupList.add(uiucGroup);
                }
            }

        } catch (SQLException e) {
            // SQL exception occurred
            log.error("Could not retrieve UIUC Special Groups", e);
        }

        return groupList;
    }

    private static void logMissingGroups(Context context, String missingGroups) {
        log.error(LogManager.getHeader(context,
                "Missing UIUC 'special' group(s): " + missingGroups.substring(0, missingGroups.length() - 1),
                " An Administrator should create these groups!"));
    }


    /**
     * Checks to see if the user is using an on-campus computer, by
     * checking the user's IP address.
     * <p/>
     * Note this is independent of user authentication -
     * if the user is an off-site UIUC user, this will still return false.
     *
     * @param request current request
     * @return true if the user is on a UIUC computer.
     */
    public static boolean onCampusComputer(HttpServletRequest request) {
        //Check to see if user is on a UIUC computer
        String myIP = request.getRemoteAddr();

        return isCampusIP(myIP);
    }

    /**
     * Checks to see if the user is using an on-campus computer, by
     * checking the user's IP address.
     * <p/>
     * Note this is independent of user authentication -
     * if the user is an off-site user, this will still return false.
     *
     * @param ipAddress user's ipAddress
     * @return true if the user is on a computer within campus IP range
     */
    public static boolean isCampusIP(String ipAddress) {
        // read in list of all IP range regular expression patterns
        // from the ideals.cfg config file
        ArrayList<String> campusPatternsList = new ArrayList<String>();

        // read in ip.pattern.1, ip.pattern.2, etc. and add to List
        for (int i = 1; idealsProps.getProperty("ip.pattern." + i) != null; i++) {
            campusPatternsList.add(idealsProps.getProperty("ip.pattern." + i));
        }

        if (campusPatternsList.size() > 0) {
            //loop through each regular expression pattern to see
            //if the current IP address matches
            for (String pattern_string : campusPatternsList) {
                //try to match the pattern with the current IP address
                Pattern pattern = Pattern.compile(pattern_string);
                Matcher matcher = pattern.matcher(ipAddress);
                if (matcher.matches())
                    return true;
            }
        }
        return false;
    }

    /**
     * Check to see if user is a Faculty/Staff
     * member (based on what we can retrieve from LDAP).
     *
     * @param request current request
     * @return true if the user is determined to be faculty/staff
     */
    public static boolean isStaff(HttpServletRequest request) {
        //get user's "type" field retrieved from LDAP
        String[] types = getFieldValues(request, idealsProps.getProperty("ldap.field.type"));

        //Check if user's "type" field includes one of the staff-specific values in it
        return hasValue(types, idealsProps.getProperty("type.staff.values"));
    }

    /**
     * Check to see if user is a UIUC Student
     * (based on what we can retrieve from LDAP).
     *
     * @param request current request
     * @return true if the user is determined to be a student
     */
    public static boolean isStudent(HttpServletRequest request) {
        // get user's "type" field retrieved from LDAP
        String[] types = getFieldValues(request, idealsProps.getProperty("ldap.field.type"));

        //Check if user's "type" field includes one of the student-specific values in it
        return hasValue(types, idealsProps.getProperty("type.student.values"));
    }


    /**
     * Check to see if user is a UIUC Graduate (i.e. Masters or PhD) Student
     * (based on what we can retrieve from LDAP).
     *
     * @param request current request
     * @return true if the user is determined to be a grad student
     */
    public static boolean isGradStudent(HttpServletRequest request) {
        // get user's "student_level_code" field retrieved from LDAP
        String[] studentLevel = getFieldValues(request, idealsProps.getProperty("ldap.field.student.level"));

        //Check if user's "student_level_code" field
        //includes one of the Graduate Student values in it
        return hasValue(studentLevel, idealsProps.getProperty("level.grad.values"));
    }

    /**
     * Check to see if user is a UIUC PhD or CAS Student
     * (based on what we can retrieve from LDAP).
     *
     * @param request current request
     * @return true if the user is determined to be a grad student
     */
    public static boolean isPhDStudent(HttpServletRequest request) {
        //get user's 'student_program_code' retrieved from LDAP
        String[] studentProgramCode = getFieldValues(request, idealsProps.getProperty("ldap.field.student.program"));


        //Check if user's "student_program_code" field
        //includes one of the PhD Student values in it
        return hasValueWithSuffix(studentProgramCode, idealsProps.getProperty("program.phd.values"));

    }

    /**
     * Check to see if user is a UIUC Undergraduate Student
     * (based on what we can retrieve from LDAP).
     *
     * @param request current request
     * @return true if the user is determined to be an undergrad
     */
    public static boolean isUnderGradStudent(HttpServletRequest request) {
        // get user's "student_level_code" field retrieved from LDAP
        String[] studentLevel = getFieldValues(request, idealsProps.getProperty("ldap.field.student.level"));

        //Check if user's "student_level_code" field
        //includes one of the Undergraduate Student values in it
        return hasValue(studentLevel, idealsProps.getProperty("level.undergrad.values"));
    }

    /**
     * Check to see if user's field values includes
     * one or more of the expected values within it.
     *
     * @param values         Array of value(s) of current user's field
     * @param expectedValues Expected Value(s) being matched against (comma separated)
     * @return true if the user has at least one value from expected values.
     */
    public static boolean hasValue(String[] values, String expectedValues) {
        boolean valueFound = false;

        String[] expected = expectedValues.split(", ");
        List expectedList = Arrays.asList(expected);

        if (values != null && values.length > 0) {
            //check to see if one of the user values is one of the expected ones
            for (int i = 0; i < values.length; i++) {
                if (expectedList.contains(values[i])) {
                    valueFound = true;
                    break;
                }
            }//end for
        }

        return valueFound;
    }

    /**
     * Check to see if user's field values ends with
     * one or more of the expected values within it.
     *
     * @param values           Array of value(s) of current user's field
     * @param expectedSuffixes Expected suffix Value(s) being matched against (comma separated)
     * @return true if the user has at least one value ending with one of expected suffixes.
     */
    public static boolean hasValueWithSuffix(String[] values, String expectedSuffixes) {
        boolean valueFound = false;

        String[] suffixes = expectedSuffixes.split(", ");

        if (values != null && values.length > 0) {
            //check to values end with one of the expected suffixes
            for (int i = 0; i < values.length; i++) {
                for (int j = 0; j < suffixes.length; j++) {
                    if (values[i].endsWith(suffixes[j])) {
                        valueFound = true;
                        break;
                    }
                }
            }//end for
        }//end if

        return valueFound;
    }

    /**
     * Check to see if there is a departmental group in DSpace
     * which corresponds to the current user's primary UIUC
     * Department.
     * <p/>
     * If a departmental group is found in DSpace, then return
     * it.  Otherwise return null.
     *
     * @param context DSpace context
     * @param request current request
     * @return List which corresponds to found departmental Group object(s), if any
     */
    public static List<Group> checkForDepartmentGroups(Context context, HttpServletRequest request) {
        //get a list of all departmental group names to look for in DSpace
        List deptGroupNames = buildDepartmentalGroupNames(request);

        // Now, determine which of the above named groups actually exist in DSpace!
        List<Group> deptGroupList = new ArrayList<Group>();

        //loop each departmental group name, and see if it exists
        for (int i = 0; i < deptGroupNames.size(); i++) {
            String groupName = (String) deptGroupNames.get(i);

            log.debug("Looking for departmental group named: " + groupName.trim());

            try {
                //try to find this group in DSpace
                Group dspaceGroup = Group.findByName(context, groupName.trim());

                //if a corresponding DSpace group was found, add it
                //to our list of DSpace groups
                if (dspaceGroup != null) {
                    log.debug("Found departmental group: " + dspaceGroup.getID());
                    deptGroupList.add(dspaceGroup);
                }
            }//we don't care if group isn't found
            catch (Exception e) {
            }
        }//end for loop


        //return whatever departmental groups are found!
        return deptGroupList;
    }

    /**
     * Generate Department Group names corresponds to the
     * current user's primary UIUC Department.
     * <p/>
     * Group name format:
     * {LDAP department name} (PhD/Masters/Undergrad/Staff) [automated]
     *
     * @param request current request
     * @return List which corresponds to generated departmental group names
     */
    protected static List buildDepartmentalGroupNames(HttpServletRequest request) {
        //list of all departmental group names to look for in DSpace
        List<String> deptGroupNames = new ArrayList<String>();

        String deptName;

        //if user is staff, look for groups pertaining to
        //the department he/she is staff in
        if (isStaff(request)) {
            // Get staff member's department from LDAP
            deptName = getFieldValue(request, idealsProps.getProperty("ldap.field.staff.department"));

            if (deptName != null && deptName.length() > 0) {
                //add general departmental group name to list
                //format: department_name [automated]
                deptGroupNames.add(deptName + " " + idealsProps.getProperty("group.uiuc.automated-suffix"));


                //add name of departmental staff group
                //format: department_name (Staff) [automated]
                deptGroupNames.add(deptName + " " +
                        idealsProps.getProperty("group.uiuc.staff-suffix") + " " +
                        idealsProps.getProperty("group.uiuc.automated-suffix"));
            }
        }//end if staff

        //if user is a student, look for groups pertaining to the
        //department he/she is a student in
        if (isStudent(request)) {
            // Get student's dept from LDAP
            deptName = getFieldValue(request, idealsProps.getProperty("ldap.field.student.department"));

            //only continue if a department name was found
            if (deptName != null && deptName.length() > 0) {
                //add general departmental group (if not already added)
                //format: department_name [automated]
                String deptGroup = (deptName + " " + idealsProps.getProperty("group.uiuc.automated-suffix"));
                if (!deptGroupNames.contains(deptGroup))
                    deptGroupNames.add(deptGroup);

                if (isGradStudent(request)) {
                    //add Grad student departmental group
                    //format: department_name (Grad) [automated]
                    deptGroupNames.add(deptName + " " +
                            idealsProps.getProperty("group.uiuc.grad-suffix") + " " +
                            idealsProps.getProperty("group.uiuc.automated-suffix"));

                    if (isPhDStudent(request)) {
                        //add PhD student departmental group
                        //format: department_name (PhD) [automated]
                        deptGroupNames.add(deptName + " " +
                                idealsProps.getProperty("group.uiuc.phd-suffix") + " " +
                                idealsProps.getProperty("group.uiuc.automated-suffix"));
                    } else {
                        //add Masters student departmental group
                        //format: department_name (Masters) [automated]
                        deptGroupNames.add(deptName + " " +
                                idealsProps.getProperty("group.uiuc.masters-suffix") + " " +
                                idealsProps.getProperty("group.uiuc.automated-suffix"));
                    }
                } else if (isUnderGradStudent(request)) {
                    //add UnderGrad student departmental group
                    //format: department_name (UnderGrad) [automated]
                    deptGroupNames.add(deptName + " " +
                            idealsProps.getProperty("group.uiuc.undergrad-suffix") + " " +
                            idealsProps.getProperty("group.uiuc.automated-suffix"));
                }

            }//end if dept name
        }//end if student

        return deptGroupNames;
    }

    /**
     * Check to see if there is an Group in DSpace
     * which corresponds to the current user's Active Directory
     * group memberships listed in campus LDAP.
     * <p/>
     * Returns the Group list (if any are found), otherwise returns null.
     *
     *
     * @param context DSpace context
     * @return List which corresponds to found LDAP-based Group object(s), if any
     */
    public static List<Group> checkForLDAPGroups(Context context) {
        //Check for a campus netID
        String netID = context.getCurrentUser().getNetid();

        //User must have a campus netID to query LDAP with
        if (netID != null && netID.length() > 0) {
            List<Group> dspaceGroupList = new ArrayList<Group>(); //list of DSpace Group objects

            try {
                addGroupsFromLDAP(context, netID, dspaceGroupList, "UIUC");
                addGroupsFromLDAP(context, netID, dspaceGroupList, "UOFI");
                //return list of DSpace groups found which match LDAP groups' names
                return dspaceGroupList;
            } catch (NamingException e) {
                log.error("Unable to load AD Groups from LDAP for user '" + netID + "': ", e);
            }
        }

        //by default, return null
        return null;
    }

    private static void addGroupsFromLDAP(Context context, String netID, List<Group> dspaceGroupList, String domain) throws NamingException {
        //Open up a connection to Active Directory via LDAP
        LDAPQuery ldap = LDAPQuery.openContext(domain);

        //Get AD Group memberships based on netID
        Set groupNameSet = ldap.getGroupMemberships(netID, domain);

        //if we have some group names from AD, see if there
        //are corresponding DSpace groups to add the user to!
        if (groupNameSet != null && groupNameSet.size() > 0) {
            Iterator iterator = groupNameSet.iterator();
            //loop through all our group names
            while (iterator.hasNext()) {
                String groupName = (String) iterator.next();

                try {
                    //search for DSpace group with "[automated]" suffix
                    String searchName = groupName.trim() + " " + idealsProps.getProperty("group.uiuc.automated-suffix").trim();

                    //try to find this group in DSpace
                    Group dspaceGroup = Group.findByName(context, searchName);

                    //if a corresponding DSpace group was found, add it
                    //to our list of DSpace groups
                    if (dspaceGroup != null) {
                        dspaceGroupList.add(dspaceGroup);
                    }
                }//we don't care if group isn't found
                catch (Exception e) {
                }
            }
        }//end if group names

        //close LDAP connection
        ldap.closeContext();
    }

    /**
     * Loads all the user information from UIUC LDAP
     * system and saves to the current session.
     * <p/>
     * This ensures that these parameters are available to all
     * of the isUIUC___() methods at any time, since they rely on
     * information about the UIUC user obtained from LDAP.
     *
     * @param request current HTTP request
     * @param netID   UIUC user's NetID
     */
    protected static void loadUserFields(HttpServletRequest request, String netID) {
        //obtain a reference to current session
        HttpSession session = request.getSession();

        if (netID != null && netID.length() > 0) {
            //First, check session cache
            HashMap fields = getSessionLDAPFields(session);

            //Only re-load if not already in Session cache
            if (fields == null || fields.isEmpty()) {
                try {
                    //load user information from LDAP
                    LDAPQuery ldap = LDAPQuery.openContext();
                    fields = ldap.getUserData(netID);

                    //Save information for remainder of user's current session
                    if (fields != null) {
                        session.setAttribute(UIUCPersonUtils.class.getName() +
                                SESSION_PARAM_SEP + UIUC_LDAP_FIELD, fields);
                    }

                    //close connection to LDAP
                    ldap.closeContext();
                } catch (NamingException e) {
                    log.error("Unable to load information from LDAP for user '" + netID + "': ", e);
                }
            }
        }
    }//end loadUserFields

    private static HashMap getSessionLDAPFields(HttpSession session) {
        return (HashMap) session.getAttribute(UIUCPersonUtils.class.getName() +
                SESSION_PARAM_SEP + UIUC_LDAP_FIELD);
    }

    /**
     * Returns the values from a particular UIUC LDAP
     * field that has been saved into session for this user.
     * <p/>
     * This ensures that these parameters are available to all
     * of the above methods at any time, since they rely on
     * information about the UIUC user obtained from LDAP.
     * <p/>
     * NOTE: UIUCPersonUtils.loadUserFields() method must first be
     * called to load all LDAP fields into current user's session!
     *
     * @param request   current HTTP request
     * @param fieldName A valid UIUC LDAP field name
     * @return the array of values of the specified field for the current user.
     *         Returns null if field is not found.
     */
    public static String[] getFieldValues(HttpServletRequest request, String fieldName) {
        //obtain a reference to current session
        HttpSession session = request.getSession();

        //Pull LDAP information out of user session
        HashMap fields = getSessionLDAPFields(session);

        //LDAP info must be in session, otherwise we cannot return values
        if (fields != null && !fields.isEmpty()) {
            //Now, get values of the specified field
            String values = (String) fields.get(fieldName);

            if (values != null && values.length() > 0) {
                //split the values on the separator
                return values.split(LDAPQuery.MULTI_VALUE_SEPARATOR);
            }
        }

        //by default, return nothing
        return null;
    }//end getFieldValues


    /**
     * Returns the value from a particular UIUC LDAP
     * field that has been saved into session for this user.
     * This only returns the *first* value for multi-valued fields.
     * <p/>
     * This ensures that these parameters are available to all
     * of the above methods at any time, since they rely on
     * information about the UIUC user obtained from LDAP.
     * <p/>
     * NOTE: UIUCPersonUtils.loadUserFields() method must first be
     * called to load all LDAP fields into current user's session!
     *
     * @param request   current HTTP request
     * @param fieldName A valid UIUC LDAP field name
     * @return the first value of the specified field for the current user.
     *         Returns null if field is not found.
     */
    public static String getFieldValue(HttpServletRequest request, String fieldName) {
        //First, get all values
        String[] values = getFieldValues(request, fieldName);

        if (ArrayUtils.isNotEmpty(values)) {
            return values[0];
        } else
            return null;
    }//end getFieldValue

}
