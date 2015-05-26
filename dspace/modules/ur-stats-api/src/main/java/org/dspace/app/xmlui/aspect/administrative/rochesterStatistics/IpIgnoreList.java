/*
 * IpIgnoreList.java
 */
package org.dspace.app.xmlui.aspect.administrative.rochesterStatistics;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.dspace.app.xmlui.aspect.administrative.FlowResult;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.CheckBox;
import org.dspace.app.xmlui.wing.element.Composite;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.Item;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Row;
import org.dspace.app.xmlui.wing.element.Table;
import org.dspace.app.xmlui.wing.element.Text;
import org.dspace.app.xmlui.wing.element.TextArea;
import org.dspace.core.Context;

import edu.ur.dspace.stats.IpIgnore;
import edu.ur.dspace.stats.IpIgnoreDAO;

/**
 * This is the main entry point for managing the IP ignore list. This transformer 
 * shows the list of address ranges and a form for adding new ranges.
 * 
 * @author Nicholas Riley
 */
public class IpIgnoreList extends AbstractDSpaceTransformer   
{	
	/** Language Strings */
	private static final Message T_dspace_home =
		message("xmlui.general.dspace_home");
	private static final Message T_title =
		message("xmlui.administrative.rochesterStatistics.IpIgnoreList.title");
	private static final Message T_ip_ignore_trail =
		message("xmlui.administrative.statistics.general.ip_ignore_trail");
	private static final Message T_para1 =
		message("xmlui.administrative.rochesterStatistics.IpIgnoreList.para1");
    private static final Message T_head_add =
        message("xmlui.administrative.rochesterStatistics.IpIgnoreList.head_add");
    private static final Message T_head_edit =
        message("xmlui.administrative.rochesterStatistics.IpIgnoreList.head_edit");
	private static final Message T_range_start =
		message("xmlui.administrative.rochesterStatistics.IpIgnoreList.range_start");
	private static final Message T_range_help =
		message("xmlui.administrative.rochesterStatistics.IpIgnoreList.range_help");
	private static final Message T_range_start_error =
		message("xmlui.administrative.rochesterStatistics.IpIgnoreList.range_start_error");
	private static final Message T_range_end_error =
	    message("xmlui.administrative.rochesterStatistics.IpIgnoreList.range_end_error");
	private static final Message T_name =
		message("xmlui.administrative.rochesterStatistics.IpIgnoreList.name");
    private static final Message T_reason =
        message("xmlui.administrative.rochesterStatistics.IpIgnoreList.reason");
	private static final Message T_submit_add =
		message("xmlui.administrative.rochesterStatistics.IpIgnoreList.submit_add");
	private static final Message T_submit_update =
	    message("xmlui.general.update");
	private static final Message T_submit_cancel =
	    message("xmlui.general.cancel");
    private static final Message T_head3 =
        message("xmlui.administrative.rochesterStatistics.IpIgnoreList.head3");
    private static final Message T_column1 =
        message("xmlui.administrative.rochesterStatistics.IpIgnoreList.column1");
    private static final Message T_column2 =
        message("xmlui.administrative.rochesterStatistics.IpIgnoreList.column2");
    private static final Message T_column3 =
        message("xmlui.administrative.rochesterStatistics.IpIgnoreList.column3");
    private static final Message T_column4 =
        message("xmlui.administrative.rochesterStatistics.IpIgnoreList.column4");
    private static final Message T_submit_delete =
        message("xmlui.administrative.rochesterStatistics.IpIgnoreList.submit_delete");
    
    private static final Message T_add_success_notice =
        message("xmlui.administrative.rochesterStatistics.IpIgnoreList.add_success_notice");
    private static final Message T_edit_success_notice =
        message("xmlui.administrative.rochesterStatistics.IpIgnoreList.edit_success_notice");
    private static final Message T_delete_success_notice =
        message("xmlui.administrative.rochesterStatistics.IpIgnoreList.delete_success_notice");
	
	public void addPageMeta(PageMeta pageMeta) throws WingException
    {
        pageMeta.addMetadata("title").addContent(T_title);

        pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
        pageMeta.addTrailLink(contextPath + "/admin/download-statistics", DownloadStatistics.T_trail);
        pageMeta.addTrailLink(contextPath + "/admin/ip-ignore", T_ip_ignore_trail);
    }
	
	public void addBody(Body body) throws WingException, SQLException 
	{
		// Get all our parameters first
		Request request = ObjectModelHelper.getRequest(objectModel);
		String rangeStart = request.getParameter("range_start");
		String rangeEnd = request.getParameter("range_end");
		String nameValue = request.getParameter("name");
        String reasonValue = request.getParameter("reason");
        String editID = parameters.getParameter("ignoreID",null);
		String errorString = parameters.getParameter("errors",null);
		ArrayList<String> errors = new ArrayList<String>();
		// unfortunately, null passed from JavaScript ends up as an empty string
		if (editID != null && editID.length() == 0)
		    editID = null;
		if (errorString != null && errorString.length() > 0) {
			for (String error : errorString.split(","))
				errors.add(error);
		} else if (editID != null) {
		    IpIgnore ignore = IpIgnoreDAO.findById(Integer.parseInt(editID));
		    rangeStart = ignore.getIpAddressMask() + "." + ignore.getIpAddressStart();
		    rangeEnd = "" + ignore.getIpAddressEnd();
		    nameValue = ignore.getName();
		    reasonValue = ignore.getReason();
		}
		
        // DIVISION: ip-ignore
        Division ipIgnore = body.addInteractiveDivision("ip-ignore",contextPath+"/admin/ip-ignore",Division.METHOD_POST,"Ignored IP list");
        ipIgnore.setHead(DownloadStatistics.T_head);
        DownloadStatistics.addOptions(ipIgnore, DownloadStatistics.OPTIONS.ip_ignore, contextPath);
        ipIgnore.addPara(T_para1);

		// DIVISION: add-ip-ignore or edit-ip-ignore
		Division addEditIgnore = ipIgnore.addDivision(editID == null ? "add-ip-ignore" : "edit-ip-ignore");
		addEditIgnore.setHead(editID == null ? T_head_add : T_head_edit);
		
		List form = addEditIgnore.addList("ip-ignore",List.TYPE_FORM);
		
		Composite range = form.addItem().addComposite("range");
		range.setLabel(T_range_start);
		range.setHelp(T_range_help);

		Text range_start = range.addText("range_start", "ip-range ip-range-start");
        range_start.setRequired();
        range_start.setSize(15, 15);
        if (rangeStart != null)
            range_start.setValue(rangeStart);
        if (errors.contains("range_start")) {
            range_start.addError(T_range_start_error);
        }

        Text range_mask = range.addText("range_mask", "ip-range ip-range-mask");
        range_mask.setDisabled(true);
        range_mask.setSize(14, 14);

		Text range_end = range.addText("range_end", "ip-range ip-range-end");
		range_end.setSize(3, 3);
        if (rangeEnd != null)
            range_end.setValue(rangeEnd);
        if (errors.contains("range_end")) {
            range_end.addError(T_range_end_error);
            range.setHelp(""); // interferes
        }

		Text name = form.addItem().addText("name");
		name.setLabel(T_name);
		name.setSize(30, 254);
		if (nameValue != null)
			name.setValue(nameValue);

		TextArea reason = form.addItem().addTextArea("reason");
		reason.setLabel(T_reason);
		reason.setSize(2, 50);
		if (reasonValue != null)
		    reason.setValue(reasonValue);

		Item buttons = form.addItem();
		if (editID == null) {
		    buttons.addButton("submit_add").setValue(T_submit_add);
 		} else {
            buttons.addButton("submit_update").setValue(T_submit_update);
 		    buttons.addButton("submit_cancel").setValue(T_submit_cancel);
 		}
		
        ipIgnore.addHidden("administrative-continue").setValue(knot.getId());

        // ignored IP list
        java.util.List<IpIgnore> ignores = IpIgnoreDAO.findAll();
        if (ignores.isEmpty())
            return;
        
        Division ignoreList = ipIgnore.addDivision("ip-ignore-list");
        ignoreList.setHead(T_head3);
        ignoreList.addPara().addButton("submit_delete").setValue(T_submit_delete);

        Table table = ignoreList.addTable("ip-ignore-list-table", ignores.size()+1, 5);
        
        Row header = table.addRow(Row.ROLE_HEADER);
        header.addCellContent(T_column1);
        header.addCellContent(T_column2);
        header.addCellContent(T_column3);
        header.addCellContent(T_column4);
        
        for (IpIgnore ignore : ignores)
        {
            int ignoreID = ignore.getId();
            String url = contextPath + "/admin/ip-ignore?administrative-continue="+knot.getId()+"&submit_edit&ignoreID="+ignoreID;
            
            Row row = table.addRow();
            CheckBox select = row.addCell().addCheckBox("select_ignore");
            select.setLabel(String.valueOf(ignoreID));
            select.addOption(String.valueOf(ignoreID));
            
            row.addCell("range", Row.ROLE_DATA, "ip-range").addContent(ignore.getRange());
            row.addCell().addXref(url, ignore.getName());
            row.addCell().addXref(url, ignore.getReason());
        }

        ignoreList.addPara().addButton("submit_delete").setValue(T_submit_delete);
    }
	
	public static FlowResult processAdd(Context context, String start, String end, String name, String reason) throws UIException {
	    return addOrEdit(context, null, start, end, name, reason);
	}
	
	public static FlowResult processEdit(Context context, int ignoreID, String start, String end, String name, String reason) throws UIException {
	    IpIgnore ignore = null;
	    try {
	        ignore = IpIgnoreDAO.findById(ignoreID);
	    } catch (SQLException e) {}
	    return addOrEdit(context, ignore, start, end, name, reason);
	}
	
	protected static FlowResult addOrEdit(Context context, IpIgnore ignore, String start, String end, String name, String reason) throws UIException {
	    FlowResult result = new FlowResult();
	    result.setContinue(false);
	    
	    String mask = null;
	    int startLastOctet = 0;
	    int endLastOctet = -1;
	    
	    try {
	        byte[] address = ((Inet4Address) Inet4Address.getByName(start)).getAddress();
	        mask = String.format("%d.%d.%d", address[0] & 0xff, address[1] & 0xff, address[2] & 0xff);
            startLastOctet = address[3] & 0xff;
	    } catch (UnknownHostException e) {
	        result.addError("range_start");
	    } catch (ClassCastException e) {
	        result.addError("range_start");
	    }
	    
	    try {
	        endLastOctet = Integer.parseInt(end);
	        if (endLastOctet < startLastOctet || endLastOctet > 255)
	            result.addError("range_end");
	    } catch (NumberFormatException e) {
	        result.addError("range_end");
	    }
	
	    if (result.getErrors() == null || result.getErrors().isEmpty()) {
	        try {
	            if (ignore == null) {
	                IpIgnoreDAO.createIp(mask, startLastOctet, endLastOctet, name, reason);
	                result.setMessage(T_add_success_notice);
	            } else {
	                ignore.setIpAddressMask(mask);
	                ignore.setIpAddressStart(startLastOctet);
	                ignore.setIpAddressEnd(endLastOctet);
	                ignore.setName(name);
	                ignore.setReason(reason);
	                IpIgnoreDAO.update(ignore);
	                result.setMessage(T_edit_success_notice);
	            }
	            context.commit();
                result.setOutcome(true);
                result.setContinue(true);
            } catch (SQLException e) {
                throw new UIException(e);
            }
	    }
	    
	    return result;
	}
	
	public static FlowResult processDelete(Context context, String[] ignoreIDs) throws NumberFormatException, SQLException {
	    FlowResult result = new FlowResult();
	    result.setContinue(false);
	    
	    for (String ignoreID : ignoreIDs) {
            IpIgnoreDAO.delete(Integer.parseInt(ignoreID));
	    }

	    context.commit();
        
        result.setContinue(true);
        result.setOutcome(true);
        result.setMessage(T_delete_success_notice);
	    return result;
	}
}
