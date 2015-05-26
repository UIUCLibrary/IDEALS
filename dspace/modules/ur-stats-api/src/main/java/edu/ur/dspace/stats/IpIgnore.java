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

package edu.ur.dspace.stats;

import org.apache.log4j.Logger;
import java.util.regex.Pattern;

/**
 * @author Nate Sarr
 * @version 1.0
 * @created 12-APR-2005 09:32:03 AM
 * 
 * Version: $Revision$
 * 
 * Date: $Date$
 * 
 * 
 * This is an object that represents an ip address range that should be ignored
 * 
 */
public class IpIgnore
{

    private static Logger log = Logger.getLogger(IpIgnore.class);

    /**
     * ip address range to ignore, divided into mask and start and end
     */
    String ipAddressMask;

    int ipAddressStart;

    int ipAddressEnd;

    /**
     * Robot or company name of the ip range
     */
    private String name;

    /**
     * Description of why the address range is ignored
     */
    private String reason;

    /**
     * Uniuqe id of the ip range to ignore
     */
    private int id;

    /**
     * Default constructor
     * 
     * @param id
     *            Unique id for the ip range
     * @param ip
     *            the ip range to ignore
     */
    public IpIgnore(int id, String ipAddressMask, int ipAddressStart,
            int ipAddressEnd)
    {
        this.id = id;
        this.ipAddressMask = ipAddressMask;
        this.ipAddressStart = ipAddressStart;
        this.ipAddressEnd = ipAddressEnd;
        this.name = "";
        this.reason = "";
    }

    /**
     * Default constructor
     * 
     * @param id
     *            Unique id for the ip range
     * @param ip
     *            the ip range to ignore
     * @param name
     *            of the company
     * @param reason
     *            of why not to count the ip range
     */
    public IpIgnore(int id, String ipAddressMask, int ipAddressStart,
            int ipAddressEnd, String name, String reason)
    {
        this.id = id;
        this.ipAddressMask = ipAddressMask;
        this.ipAddressStart = ipAddressStart;
        this.ipAddressEnd = ipAddressEnd;
        this.name = name;
        this.reason = reason;
    }

    /**
     * Get the unique identifier for this ip range to ignore
     * 
     * @return the id
     */
    public int getId()
    {
        return this.id;
    }

    /**
     * Set the name of the company, robot or group
     * 
     * @param name
     *            of the company
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Return the name of the company or group
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Set ip mask to ignore
     * 
     * @param name
     *            of the company
     */
    public void setIpAddressMask(String ipAddressMask)
    {
        this.ipAddressMask = ipAddressMask;
    }
    
    /**
     * Return a nicely formatted version of the IP range
     */
    public String getRange()
    {
        String range = ipAddressMask + '.' + ipAddressStart;
        if (ipAddressStart != ipAddressEnd)
            range += " \u2013 " + ipAddressMask + '.' + ipAddressEnd;
        
        range = Pattern.compile("(?<!\\d)(\\d)(?!\\d)").matcher(range).replaceAll("\u2007\u2007$1");
        range = Pattern.compile("(?<!\\d)(\\d\\d)(?!\\d)").matcher(range).replaceAll("\u2007$1");
        return range;
    }

    /**
     * Return the ip mask to ignore
     * 
     * @return ip to ignore
     */
    public String getIpAddressMask()
    {
        return this.ipAddressMask;
    }

    /**
     * Set ip address start to ignore
     * 
     * @param name
     *            of the company
     */
    public void setIpAddressStart(int ipAddressStart)
    {
        this.ipAddressStart = ipAddressStart;
    }

    /**
     * Return the ip address start to ignore
     * 
     * @return ip to ignore
     */
    public int getIpAddressStart()
    {
        return this.ipAddressStart;
    }

    /**
     * Set ip address end to ignore
     * 
     * @param name
     *            of the company
     */
    public void setIpAddressEnd(int ipAddressEnd)
    {
        this.ipAddressEnd = ipAddressEnd;
    }

    /**
     * Return the ip address end to ignore
     * 
     * @return ip to ignore
     */
    public int getIpAddressEnd()
    {
        return this.ipAddressEnd;
    }

    /**
     * Set the reason of why this is ignored
     * 
     * @param reason
     *            of why this is ignored
     */
    public void setReason(String reason)
    {
        this.reason = reason;
    }

    /**
     * Return the reason of why this ip range is ignored
     * 
     * @return reason
     */
    public String getReason()
    {
        return this.reason;
    }
}
