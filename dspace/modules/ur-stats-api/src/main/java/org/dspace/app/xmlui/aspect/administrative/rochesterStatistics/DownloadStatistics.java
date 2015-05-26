/*
 * DownloadStatistics.java
 *
 * Version: $Revision$
 *
 * Date: $Date$
 */
package org.dspace.app.xmlui.aspect.administrative.rochesterStatistics;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.flow.ContinuationsManager;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.flow.WebContinuationDataBean;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.Item;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Row;
import org.dspace.app.xmlui.wing.element.Select;
import org.dspace.app.xmlui.wing.element.Table;
import org.dspace.app.xmlui.wing.element.TextArea;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.core.ConfigurationManager;
import org.dspace.eperson.EPerson;
import org.dspace.handle.HandleManager;
import org.xml.sax.SAXException;

import edu.ur.dspace.stats.BitstreamDayDownloader;
import edu.ur.dspace.stats.StatsDAO;

/**
 * This page displays download statistics for the current day or month.
 *
 * @author Nicholas Riley
 */
public class DownloadStatistics extends AbstractDSpaceTransformer {
	
	/** Language Strings */
    private static final Message T_DSPACE_HOME =
        message("xmlui.general.dspace_home");
    static final Message T_title 				        = message("xmlui.administrative.rochesterStatistics.DownloadStatistics.title");
	static final Message T_trail	 			        = message("xmlui.administrative.rochesterStatistics.DownloadStatistics.trail");
	static final Message T_head	 				        = message("xmlui.administrative.rochesterStatistics.DownloadStatistics.head");
    private static final Message T_option_day           = message("xmlui.administrative.rochesterStatistics.DownloadStatistics.option_day");
    private static final Message T_option_month         = message("xmlui.administrative.rochesterStatistics.DownloadStatistics.option_month");
    private static final Message T_option_ip_ignore     = message("xmlui.administrative.rochesterStatistics.DownloadStatistics.option_ip_ignore");
    private static final Message T_include_ignored      = message("xmlui.administrative.rochesterStatistics.DownloadStatistics.include_ignored");
    private static final Message T_exclude_ignored      = message("xmlui.administrative.rochesterStatistics.DownloadStatistics.exclude_ignored");
    private static final Message T_head_day             = message("xmlui.administrative.rochesterStatistics.DownloadStatistics.head_day");
    private static final Message T_head_month           = message("xmlui.administrative.rochesterStatistics.DownloadStatistics.head_month");
    private static final Message T_column1              = message("xmlui.administrative.rochesterStatistics.DownloadStatistics.column1");
    private static final Message T_column2              = message("xmlui.administrative.rochesterStatistics.DownloadStatistics.column2");
    private static final Message T_column3              = message("xmlui.administrative.rochesterStatistics.DownloadStatistics.column3");
    private static final Message T_column4              = message("xmlui.administrative.rochesterStatistics.DownloadStatistics.column4");
    private static final Message T_column5              = message("xmlui.administrative.rochesterStatistics.DownloadStatistics.column5");

    /**
     * The three states that this page can be in (note: ip_ignore is handled by IpIgnoreList.java)
     */
    enum OPTIONS {day, month, ip_ignore};
    
	public void addPageMeta(PageMeta pageMeta) throws WingException {
		pageMeta.addMetadata("title").addContent(T_title);

		pageMeta.addTrailLink(contextPath + "/", T_DSPACE_HOME);
		pageMeta.addTrailLink(contextPath + "/admin/download-statistics", T_trail);
	}
	
	static String addOptions(Division div, OPTIONS option, String contextPath) throws WingException {
        // LIST: options
        List options = div.addList("options",List.TYPE_SIMPLE,"horizontal");
        
        String xref = null, selected_xref = null;
        // our options, selected or not...
        xref = contextPath + "/admin/download-statistics?day";
        if (option == OPTIONS.day) {
            options.addItem().addHighlight("bold").addXref(xref,T_option_day);
            selected_xref = xref;
        } else
            options.addItemXref(xref,T_option_day);
        
        xref = contextPath + "/admin/download-statistics?month";
        if (option == OPTIONS.month) {
            options.addItem().addHighlight("bold").addXref(xref,T_option_month);
            selected_xref = xref;
        } else
            options.addItemXref(xref,T_option_month);
        
        xref = contextPath + "/admin/ip-ignore";
        if (option == OPTIONS.ip_ignore) {
            options.addItem().addHighlight("bold").addXref(xref, T_option_ip_ignore);
            selected_xref = xref;
        } else
            options.addItemXref(xref,T_option_ip_ignore);
        
        return selected_xref;
    }

	public void addBody(Body body) throws SAXException, WingException,
			UIException, SQLException, IOException, AuthorizeException {
		
		Request request = ObjectModelHelper.getRequest(objectModel);
		boolean filterOutIgnored = request.getParameter("includeIgnored") == null;

		java.util.List<BitstreamDayDownloader> downloads = null;
		OPTIONS option = null;
		if (request.getParameter("month") != null) {
			option = OPTIONS.month;
            downloads = new StatsDAO().getMonthBitstreamDownloaders(filterOutIgnored);
		} else {
		    option = OPTIONS.day;
            downloads = new StatsDAO().getTodayBitstreamDownloaders(filterOutIgnored);
		}
		
		Division div = body.addInteractiveDivision("download-statistics", contextPath+"/admin/download-statistics", Division.METHOD_POST, "download statistics");
		div.setHead(T_head);
		
		String xref = addOptions(div, option, contextPath);
		
        if (filterOutIgnored)
            div.addPara().addXref(xref + "&includeIgnored").addContent(T_include_ignored);
        else
            div.addPara().addXref(xref).addContent(T_exclude_ignored);
        
        // TABLE: download-statistics
        Table downloadStatistics = div.addTable("download-statistics", 1, 4);
        downloadStatistics.setHead(option == OPTIONS.day ? T_head_day : T_head_month);
        Row row = downloadStatistics.addRow(Row.ROLE_HEADER);
        row.addCellContent(T_column1);
        row.addCellContent(T_column2);
        row.addCellContent(T_column3);
        row.addCellContent(T_column4);
        row.addCellContent(T_column5);

        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);        
        for (BitstreamDayDownloader downloader : downloads) {
            row = downloadStatistics.addRow();
            row.addCellContent(downloader.getIpAddress());

            String bitstreamID = String.valueOf(downloader.getBitstreamId());
            row.addCellContent(bitstreamID);
            
            org.dspace.content.Item item = null;
            int itemID = downloader.getItemId();
            if (itemID != 0)
                item = org.dspace.content.Item.find(context, itemID);
            if (item != null)
                row.addCell().addXref(HandleManager.resolveToURL(context, item.getHandle()), item.getHandle());
            else
                row.addCellContent("");
            
            row.addCellContent(df.format(downloader.getDay()));
            row.addCellContent(String.valueOf(downloader.getCount()));
        }
        
	}
	
}
