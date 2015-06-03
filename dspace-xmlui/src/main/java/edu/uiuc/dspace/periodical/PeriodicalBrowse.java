/*
 * PeriodicalBrowse.java
 *
 * Version: $Revision$
 *
 * Date: $Date$
 *
 * Copyright (c) 2002, Hewlett-Packard Company and Massachusetts
 * Institute of Technology.  All rights reserved.
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

package edu.uiuc.dspace.periodical;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.core.Context;
import org.dspace.search.DSIndexer;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

public class PeriodicalBrowse extends AbstractDSpaceTransformer {
    public void addPageMeta(PageMeta pageMeta)
            throws SQLException, AuthorizeException, WingException {
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (dso == null)
            return;
        if (dso instanceof Item)
            dso = ((Item)dso).getOwningCollection();
        if (!(dso instanceof Collection))
            return;

        java.util.List<String> periodicalQueries = getPeriodicalQueries((Collection)dso, context);
        if (periodicalQueries != null && !periodicalQueries.isEmpty()) {
            pageMeta.addMetadata("collectionIsPeriodical");
            return;
        }

        // If we have no periodical information but a Series/Report (dc.relation.ispartof) field, allow browsing by it
        MetadataField dcRelationIsPartOf = MetadataField.findByElement(context, MetadataSchema.DC_SCHEMA_ID, "relation", "ispartof");
        if (dcRelationIsPartOf == null)
            return;
        TableRow tr = DatabaseManager.querySingle(context,
                "SELECT COUNT(*) AS has_series_metadata FROM metadatavalue JOIN item USING (item_id) WHERE owning_collection = ? AND metadata_field_id = ?",
                Long.valueOf(((Collection)dso).getID()), Long.valueOf(dcRelationIsPartOf.getFieldID()));
        if (tr.getLongColumn("has_series_metadata") > 0)
            pageMeta.addMetadata("collectionHasSeriesMetadata");
    }

        // XXX this location is temporary; should be an instance method on Collection?
    public static List<String> getPeriodicalQueries(Collection c, Context ourContext) throws AuthorizeException, SQLException
    {
        String[] qualifiers = DSIndexer.getPeriodicalQualifiers();

        if (qualifiers == null || qualifiers.length <= 1)
            return null;

        // XXX support things other than mods.part.*?
        MetadataSchema schema = MetadataSchema.find(ourContext, "mods");
        if(schema==null) return null;
        int metadataSchemaID = schema.getSchemaID();
        String metadataElement = "part";

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT DISTINCT ");
        for (int i = 0 ; i < qualifiers.length - 1 ; i++)
        {
            if (i != 0)
                sb.append(", ");
            sb.append(String.format("%s_metadatavalue.text_value::TEXT::INTEGER AS %s", qualifiers[i], qualifiers[i]));
        }
        sb.append(" FROM ");
        for (int i = 0 ; i < qualifiers.length - 1 ; i++)
        {
            int metadataFieldID = MetadataField.findByElement(ourContext, metadataSchemaID, metadataElement, qualifiers[i]).getFieldID();
            if (i != 0)
                sb.append("FULL JOIN ");
            sb.append(String.format("(SELECT item_id, text_value FROM metadatavalue WHERE metadata_field_id = %d) %s_metadatavalue ",
                    Long.valueOf(metadataFieldID), qualifiers[i]));
            if (i != 0)
                sb.append("USING (item_id) ");
        }
        sb.append("JOIN item USING (item_id) WHERE owning_collection = ? ");
        sb.append("ORDER BY ");
        for (int i = 0 ; i < qualifiers.length - 1 ; i++)
        {
            if (i != 0)
                sb.append(", ");
            sb.append(qualifiers[i]);
            sb.append(" DESC");
        }

        TableRowIterator tri = DatabaseManager.queryTable(ourContext, null,
                        sb.toString(), c.getID());
        ArrayList<String> queries = new ArrayList<String>();

        StringBuilder parts = new StringBuilder();
        try
        {
            while (tri.hasNext())
            {
                TableRow row = tri.next();
                parts.setLength(0);
                for (int i = 0 ; i < qualifiers.length - 1 ; i++)
                {
                    long index = row.getLongColumn(qualifiers[i]);
                    if (index == -1)
                        continue;
                    if (parts.length() > 0)
                        parts.append(' ');
                    parts.append(qualifiers[i]);
                    parts.append(DSIndexer.toPeriodicalIndex(index));
                }
                if (parts.length() > 0)
                    queries.add("periodical:" + parts.toString());
            }
        }
        finally
        {
            // close the TableRowIterator to free up resources
            if (tri != null)
                tri.close();
        }
        return queries;
    }

}
