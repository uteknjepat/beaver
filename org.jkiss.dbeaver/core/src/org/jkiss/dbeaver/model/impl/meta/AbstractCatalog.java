/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.impl.meta;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.anno.Property;
import org.jkiss.dbeaver.model.struct.DBSCatalog;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSSchema;

import java.util.Collection;

/**
 * AbstractCatalog
 */
public abstract class AbstractCatalog<DATASOURCE extends DBPDataSource> implements DBSCatalog
{
    private DATASOURCE dataSource;
    private String catalogName;

    public AbstractCatalog(DATASOURCE dataSource, String catalogName)
    {
        this.dataSource = dataSource;
        this.catalogName = catalogName;
    }

    public DATASOURCE getDataSource()
    {
        return dataSource;
    }

    @Property(name = "Catalog Name", viewable = true, order = 1)
    public String getName()
    {
        return catalogName;
    }

    protected void setName(String catalogName)
    {
        this.catalogName = catalogName;
    }

    public Collection<? extends DBSSchema> getSchemas(DBRProgressMonitor monitor)
        throws DBException
    {
        return null;
    }

    public DBSObject getParentObject()
    {
        return getDataSource().getContainer();
    }

    public boolean refreshObject(DBRProgressMonitor monitor)
        throws DBException
    {
        return false;
    }

    @Override
    public String toString()
    {
        return getName() + " [" + getDataSource().getContainer().getName() + "]";
    }

}
