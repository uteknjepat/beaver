/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.oracle.model;

import org.jkiss.dbeaver.ext.oracle.OracleConstants;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableObject;
import org.jkiss.dbeaver.model.meta.Property;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * OraclePartition
 */
public class OraclePartition extends JDBCTableObject<OracleTable>
{
    private OraclePartition parent;
    private List<OraclePartition> subPartitions;
    private int position;
    private String method;
    private String expression;
    private String description;
    private long tableRows;
    private long avgRowLength;
    private long dataLength;
    private long maxDataLength;
    private long indexLength;
    private long dataFree;
    private Date createTime;
    private Date updateTime;
    private Date checkTime;
    private long checksum;
    private String comment;
    private String nodegroup;

    protected OraclePartition(OracleTable mySQLTable, OraclePartition parent, String name, JDBCResultSet dbResult)
    {
        super(mySQLTable, name, true);
        this.parent = parent;
        if (parent != null) {
            parent.addSubPartitions(this);
        }
        this.position = JDBCUtils.safeGetInt(dbResult,
            parent == null ?
                OracleConstants.COL_PARTITION_ORDINAL_POSITION :
                OracleConstants.COL_SUBPARTITION_ORDINAL_POSITION);
        this.method = JDBCUtils.safeGetString(dbResult,
            parent == null ?
                OracleConstants.COL_PARTITION_METHOD :
                OracleConstants.COL_SUBPARTITION_METHOD);
        this.expression = JDBCUtils.safeGetString(dbResult,
            parent == null ?
                OracleConstants.COL_PARTITION_EXPRESSION :
                OracleConstants.COL_SUBPARTITION_EXPRESSION);
        this.description = JDBCUtils.safeGetString(dbResult, OracleConstants.COL_PARTITION_DESCRIPTION);
        this.tableRows = JDBCUtils.safeGetLong(dbResult, OracleConstants.COL_TABLE_ROWS);
        this.avgRowLength = JDBCUtils.safeGetLong(dbResult, OracleConstants.COL_AVG_ROW_LENGTH);
        this.dataLength = JDBCUtils.safeGetLong(dbResult, OracleConstants.COL_DATA_LENGTH);
        this.maxDataLength = JDBCUtils.safeGetLong(dbResult, OracleConstants.COL_MAX_DATA_LENGTH);
        this.indexLength = JDBCUtils.safeGetLong(dbResult, OracleConstants.COL_INDEX_LENGTH);
        this.dataFree = JDBCUtils.safeGetLong(dbResult, OracleConstants.COL_DATA_FREE);
        this.createTime = JDBCUtils.safeGetTimestamp(dbResult, OracleConstants.COL_CREATE_TIME);
        this.updateTime = JDBCUtils.safeGetTimestamp(dbResult, OracleConstants.COL_UPDATE_TIME);
        this.checkTime = JDBCUtils.safeGetTimestamp(dbResult, OracleConstants.COL_CHECK_TIME);
        this.checksum = JDBCUtils.safeGetLong(dbResult, OracleConstants.COL_CHECKSUM);
        this.comment = JDBCUtils.safeGetString(dbResult, OracleConstants.COL_PARTITION_COMMENT);
        this.nodegroup = JDBCUtils.safeGetString(dbResult, OracleConstants.COL_NODEGROUP);
    }

    protected OraclePartition(JDBCTableObject<OracleTable> source)
    {
        super(source);
    }

    private void addSubPartitions(OraclePartition partition)
    {
        if (subPartitions == null) {
            subPartitions = new ArrayList<OraclePartition>();
        }
        subPartitions.add(partition);
    }

    public OraclePartition getParent()
    {
        return parent;
    }

    public List<OraclePartition> getSubPartitions()
    {
        return subPartitions;
    }

    public OracleDataSource getDataSource()
    {
        return getTable().getDataSource();
    }

    @Property(name = "Partition Name", viewable = true, order = 1)
    public String getName()
    {
        return super.getName();
    }

    @Property(name = "Position", viewable = true, order = 2)
    public int getPosition()
    {
        return position;
    }

    @Property(name = "Method", viewable = true, order = 3)
    public String getMethod()
    {
        return method;
    }

    @Property(name = "Expression", viewable = true, order = 4)
    public String getExpression()
    {
        return expression;
    }

    @Property(name = "Description", viewable = true, order = 5)
    public String getDescription()
    {
        return description;
    }

    @Property(name = "Table Rows", viewable = true, order = 6)
    public long getTableRows()
    {
        return tableRows;
    }

    @Property(name = "Avg Row Len", viewable = true, order = 7)
    public long getAvgRowLength()
    {
        return avgRowLength;
    }

    @Property(name = "Data Len", viewable = true, order = 8)
    public long getDataLength()
    {
        return dataLength;
    }

    @Property(name = "Max Data Len", viewable = true, order = 9)
    public long getMaxDataLength()
    {
        return maxDataLength;
    }

    @Property(name = "Index Len", viewable = true, order = 10)
    public long getIndexLength()
    {
        return indexLength;
    }

    @Property(name = "Data Free", viewable = true, order = 11)
    public long getDataFree()
    {
        return dataFree;
    }

    @Property(name = "Create Time", viewable = false, order = 12)
    public Date getCreateTime()
    {
        return createTime;
    }

    @Property(name = "Update Time", viewable = false, order = 13)
    public Date getUpdateTime()
    {
        return updateTime;
    }

    @Property(name = "Check Time", viewable = false, order = 14)
    public Date getCheckTime()
    {
        return checkTime;
    }

    @Property(name = "Checksum", viewable = true, order = 15)
    public long getChecksum()
    {
        return checksum;
    }

    @Property(name = "Comment", viewable = true, order = 16)
    public String getComment()
    {
        return comment;
    }

    @Property(name = "Node Group", viewable = true, order = 17)
    public String getNodegroup()
    {
        return nodegroup;
    }

}
