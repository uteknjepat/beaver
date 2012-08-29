/*
 * Copyright (C) 2010-2012 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.ext.oracle.model;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Association;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * OracleTable
 */
public class OracleTable extends OracleTablePhysical
{
    private OracleDataType tableType;
    private boolean temporary;
    private boolean secondary;
    private boolean nested;

    public static class AdditionalInfo extends TableAdditionalInfo {
    }

    private final AdditionalInfo additionalInfo = new AdditionalInfo();

    private List<OracleTableForeignKey> foreignKeys;

    public OracleTable(OracleSchema schema, String name)
    {
        super(schema, name);
    }

    public OracleTable(
        DBRProgressMonitor monitor,
        OracleSchema schema,
        ResultSet dbResult)
    {
        super(schema, dbResult);
        String typeOwner = JDBCUtils.safeGetString(dbResult, "TABLE_TYPE_OWNER");
        if (!CommonUtils.isEmpty(typeOwner)) {
            tableType = OracleDataType.resolveDataType(
                monitor,
                schema.getDataSource(),
                typeOwner,
                JDBCUtils.safeGetString(dbResult, "TABLE_TYPE"));
        }
        this.temporary = JDBCUtils.safeGetBoolean(dbResult, "TEMPORARY", "Y");
        this.secondary = JDBCUtils.safeGetBoolean(dbResult, "SECONDARY", "Y");
        this.nested = JDBCUtils.safeGetBoolean(dbResult, "NESTED", "Y");
    }

    @Override
    public TableAdditionalInfo getAdditionalInfo()
    {
        return additionalInfo;
    }

    @Override
    protected String getTableTypeName()
    {
        return "TABLE";
    }

    @Override
    public boolean isView()
    {
        return false;
    }

    @Property(name = "Table Type", viewable = false, order = 5, description = "If an object table, type of the table")
    public OracleDataType getTableType()
    {
        return tableType;
    }

    @Property(name = "Temporary", viewable = false, order = 6, description = "Indicates whether the table is temporary")
    public boolean isTemporary()
    {
        return temporary;
    }

    @Property(name = "Secondary", viewable = false, order = 6, description = "Indicates whether the table is a secondary object created by the ODCIIndexCreate method of the Oracle Data Cartridge to contain the contents of a domain index")
    public boolean isSecondary()
    {
        return secondary;
    }

    @Property(name = "Nested", viewable = false, order = 7, description = "Indicates whether the table is a nested table")
    public boolean isNested()
    {
        return nested;
    }

    @Override
    public Collection<OracleTableForeignKey> getReferences(DBRProgressMonitor monitor)
        throws DBException
    {
        List<OracleTableForeignKey> refs = new ArrayList<OracleTableForeignKey>();
        // This is dummy implementation
        // Get references from this schema only
        final Collection<OracleTableForeignKey> allForeignKeys =
            getContainer().foreignKeyCache.getObjects(monitor, getContainer(), null);
        for (OracleTableForeignKey constraint : allForeignKeys) {
            if (constraint.getReferencedTable() == this) {
                refs.add(constraint);
            }
        }
        return refs;
    }

    @Override
    @Association
    public Collection<OracleTableForeignKey> getAssociations(DBRProgressMonitor monitor)
        throws DBException
    {
        if (foreignKeys == null) {
            getContainer().foreignKeyCache.getObjects(monitor, getContainer(), this);
        }
        return foreignKeys;
    }

    void setForeignKeys(List<OracleTableForeignKey> constraints)
    {
        this.foreignKeys = constraints;
    }

    List<OracleTableForeignKey> getForeignKeysCache()
    {
        return foreignKeys;
    }

//    public OracleTableForeignKey getForeignKey(DBRProgressMonitor monitor, String fkName)
//        throws DBException
//    {
//        return DBUtils.findObject(getForeignKeys(monitor), fkName);
//    }

//    public OracleTrigger getTrigger(DBRProgressMonitor monitor, String triggerName)
//        throws DBException
//    {
//        return DBUtils.findObject(getTriggers(monitor), triggerName);
//    }

    @Override
    public boolean refreshObject(DBRProgressMonitor monitor) throws DBException
    {
        super.refreshObject(monitor);

        foreignKeys = null;
        return true;
    }

}
