/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.oracle.model.plan;

import org.jkiss.dbeaver.model.exec.plan.DBCPlanNode;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Oracle execution plan node
 */
public class OraclePlanNode implements DBCPlanNode {

    private long id;
    private String selectType;
    private String table;
    private String type;
    private String possibleKeys;
    private String key;
    private String keyLength;
    private String ref;
    private long rowCount;
    private double filtered;
    private String extra;

    private OraclePlanNode parent;
    private List<OraclePlanNode> nested;

    public OraclePlanNode(OraclePlanNode parent, ResultSet dbResult) throws SQLException
    {
        this.parent = parent;
        this.id = JDBCUtils.safeGetLong(dbResult, "id");
        this.selectType = JDBCUtils.safeGetString(dbResult, "select_type");
        this.table = JDBCUtils.safeGetString(dbResult, "table");
        this.type = JDBCUtils.safeGetString(dbResult, "type");
        this.possibleKeys = JDBCUtils.safeGetString(dbResult, "possible_keys");
        this.key = JDBCUtils.safeGetString(dbResult, "key");
        this.keyLength = JDBCUtils.safeGetString(dbResult, "key_len");
        this.ref = JDBCUtils.safeGetString(dbResult, "ref");
        this.rowCount = JDBCUtils.safeGetLong(dbResult, "rows");
        this.filtered = JDBCUtils.safeGetDouble(dbResult, "filtered");
        this.extra = JDBCUtils.safeGetString(dbResult, "extra");
    }

    public DBCPlanNode getParent()
    {
        return parent;
    }

    public List<OraclePlanNode> getNested()
    {
        return nested;
    }

    @Property(name = "ID", order = 0, viewable = true, description = "The SELECT identifier. This is the sequential number of the SELECT within the query")
    public long getId()
    {
        return id;
    }

    @Property(name = "Select Type", order = 1, viewable = true, description = "The type of SELECT")
    public String getSelectType()
    {
        return selectType;
    }

    @Property(name = "Table", order = 2, viewable = true, description = "The table to which the row of output refers")
    public String getTable()
    {
        return table;
    }

    @Property(name = "Type", order = 3, viewable = true, description = "The join type")
    public String getType()
    {
        return type;
    }

    @Property(name = "Possible Keys", order = 4, viewable = true, description = "Indicates which indexes Oracle can choose from use to find the rows in this table")
    public String getPossibleKeys()
    {
        return possibleKeys;
    }

    @Property(name = "Key", order = 5, viewable = true, description = "Key (index) that Oracle actually decided to use")
    public String getKey()
    {
        return key;
    }

    @Property(name = "Key Length", order = 6, viewable = true, description = "Length of the key that Oracle decided to use")
    public String getKeyLength()
    {
        return keyLength;
    }

    @Property(name = "Ref", order = 7, viewable = true, description = "Shows which columns or constants are compared to the index named in the key column to select rows from the table")
    public String getRef()
    {
        return ref;
    }

    @Property(name = "Rows", order = 8, viewable = true, description = "Number of rows Oracle believes it must examine to execute the query")
    public long getRowCount()
    {
        return rowCount;
    }

    @Property(name = "Filtered", order = 9, viewable = true, description = "Estimated percentage of table rows that will be filtered by the table condition")
    public double getFiltered()
    {
        return filtered;
    }

    @Property(name = "Extra", order = 10, viewable = true, description = "Additional information about how Oracle resolves the query")
    public String getExtra()
    {
        return extra;
    }
}