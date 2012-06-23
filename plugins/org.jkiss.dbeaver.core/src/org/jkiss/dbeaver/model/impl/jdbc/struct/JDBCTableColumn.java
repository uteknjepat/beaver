/*
 *
 *  * Copyright (C) 2010-2012 Serge Rieder
 *  * serge@jkiss.org
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.jkiss.dbeaver.model.impl.jdbc.struct;

import org.jkiss.dbeaver.model.DBPDataTypeProvider;
import org.jkiss.dbeaver.model.DBPSaveableObject;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSDataKind;
import org.jkiss.dbeaver.model.struct.DBSDataType;
import org.jkiss.dbeaver.model.struct.DBSTableColumn;
import org.jkiss.dbeaver.ui.properties.IPropertyValueListProvider;
import org.jkiss.utils.CommonUtils;

import java.util.Set;
import java.util.TreeSet;

/**
 * JDBC abstract table column
 */
public abstract class JDBCTableColumn<TABLE_TYPE extends JDBCTable> extends JDBCColumn implements DBSTableColumn, DBPSaveableObject {

    private final TABLE_TYPE table;
    private boolean persisted;

    protected JDBCTableColumn(TABLE_TYPE table, boolean persisted)
    {
        this.table = table;
        this.persisted = persisted;
    }

    protected JDBCTableColumn(TABLE_TYPE table, boolean persisted, String name, String typeName, int valueType, int ordinalPosition, long maxLength, int scale, int precision, boolean nullable)
    {
        super(name, typeName, valueType, ordinalPosition, maxLength, scale, precision, nullable);
        this.table = table;
        this.persisted = persisted;
    }

    public TABLE_TYPE getTable()
    {
        return table;
    }

    @Override
    public TABLE_TYPE getParentObject()
    {
        return getTable();
    }


    @Property(name = "Column Name", viewable = true, editable = true, valueTransformer = DBObjectNameCaseTransformer.class, order = 10)
    @Override
    public String getName()
    {
        return super.getName();
    }

    @Property(name = "Data Type", viewable = true, editable = true, order = 20, listProvider = ColumnTypeNameListProvider.class)
    @Override
    public String getTypeName()
    {
        return super.getTypeName();
    }

    @Property(name = "Length", viewable = true, editable = true, order = 40)
    @Override
    public long getMaxLength()
    {
        return super.getMaxLength();
    }

    @Property(name = "Not Null", viewable = true, editable = true, order = 50)
    @Override
    public boolean isRequired()
    {
        return super.isRequired();
    }

    @Override
    public boolean isPersisted()
    {
        return persisted;
    }

    @Override
    public void setPersisted(boolean persisted)
    {
        this.persisted = persisted;
    }

    public static class ColumnTypeNameListProvider implements IPropertyValueListProvider<JDBCTableColumn> {

        @Override
        public boolean allowCustomValue()
        {
            return true;
        }

        @Override
        public Object[] getPossibleValues(JDBCTableColumn column)
        {
            Set<String> typeNames = new TreeSet<String>();
            if (column.getDataSource() instanceof DBPDataTypeProvider) {
                for (DBSDataType type : ((DBPDataTypeProvider) column.getDataSource()).getDataTypes()) {
                    if (type.getDataKind() != DBSDataKind.UNKNOWN && !CommonUtils.isEmpty(type.getName()) && Character.isLetter(type.getName().charAt(0))) {
                        typeNames.add(type.getName());
                    }
                }
            }
            return typeNames.toArray(new String[typeNames.size()]);
        }
    }

}
