/*
 * DeleteBitstreamFormatConfirm.java
 */
package org.dspace.app.xmlui.aspect.administrative.rochesterStatistics;

import java.sql.SQLException;
import java.util.ArrayList;

import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Para;
import org.dspace.app.xmlui.wing.element.Row;
import org.dspace.app.xmlui.wing.element.Table;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.BitstreamFormat;

import edu.ur.dspace.stats.IpIgnore;
import edu.ur.dspace.stats.IpIgnoreDAO;

/**
 * Confirm the deletion of IP ignore ranges by listing to-be-deleted
 * ranges and asking the user for confirmation.
 * 
 * @author Nicholas Riley
 */
public class DeleteIpIgnoreConfirm extends AbstractDSpaceTransformer   
{
	
	/** Language Strings */
	private static final Message T_dspace_home = message("xmlui.general.dspace_home");
	private static final Message T_submit_confirm = message("xmlui.administrative.rochesterStatistics.DeleteIpIgnoreConfirm.submit_confirm");
	private static final Message T_submit_cancel = message("xmlui.general.cancel");
	private static final Message T_title = 	message("xmlui.administrative.rochesterStatistics.DeleteIpIgnoreConfirm.title");
    private static final Message T_ip_ignore_trail = message("xmlui.administrative.statistics.general.ip_ignore_trail");
	private static final Message T_trail = message("xmlui.administrative.rochesterStatistics.DeleteIpIgnoreConfirm.trail");
	private static final Message T_head = message("xmlui.administrative.rochesterStatistics.DeleteIpIgnoreConfirm.head");
	private static final Message T_para1 = message("xmlui.administrative.rochesterStatistics.DeleteIpIgnoreConfirm.para1");
	private static final Message T_column1 = message("xmlui.administrative.rochesterStatistics.DeleteIpIgnoreConfirm.column1");
	private static final Message T_column2 = message("xmlui.administrative.rochesterStatistics.DeleteIpIgnoreConfirm.column2");
	private static final Message T_column3 = message("xmlui.administrative.rochesterStatistics.DeleteIpIgnoreConfirm.column3");

	
	public void addPageMeta(PageMeta pageMeta) throws WingException
    {
        pageMeta.addMetadata("title").addContent(T_title);
        pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
        pageMeta.addTrailLink(contextPath + "/admin/download-statistics", DownloadStatistics.T_trail);
        pageMeta.addTrailLink(contextPath + "/admin/ip-ignore", T_ip_ignore_trail);
        pageMeta.addTrail().addContent(T_trail);
    }
	
	
	public void addBody(Body body) throws WingException, SQLException, AuthorizeException
	{
		// DIVISION: ip-ignore-confirm-delete
    	Division deleted = body.addInteractiveDivision("ip-ignore-confirm-delete",contextPath+"/admin/ip-ignore",Division.METHOD_POST,"primary administrative format-registry");
    	deleted.setHead(T_head);
    	deleted.addPara(T_para1);
    	
    	String[] ignoreIDs = parameters.getParameter("ignoreIDs", null).split(",");
    	
    	Table table = deleted.addTable("format-confirm-delete",ignoreIDs.length + 1, 3);
        Row header = table.addRow(Row.ROLE_HEADER);
        header.addCell().addContent(T_column1);
        header.addCell().addContent(T_column2);
        header.addCell().addContent(T_column3);
    	
    	for (String ignoreID : ignoreIDs)
    	{
    		IpIgnore ignore = IpIgnoreDAO.findById(Integer.parseInt(ignoreID));
    		if (ignore == null)
    		    continue;
    		Row row = table.addRow();
    		row.addCell("range", Row.ROLE_DATA, "ip-range").addContent(ignore.getRange());
        	row.addCell().addContent(ignore.getName());
        	row.addCell().addContent(ignore.getReason());
	    }
    	
    	Para buttons = deleted.addPara();
    	buttons.addButton("submit_confirm").setValue(T_submit_confirm);
    	buttons.addButton("submit_cancel").setValue(T_submit_cancel);
    	
    	deleted.addHidden("administrative-continue").setValue(knot.getId());
    }
}
