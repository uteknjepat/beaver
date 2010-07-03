/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.dbc.DBCExecutionContext;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;

/**
 * Data Source.
 * Root object of all database structure and data objects.
 */
public interface DBPDataSource extends DBPObject
{
    /**
     * Datasource container
     * @return contaner implementation
     */
    DBSDataSourceContainer getContainer();

    DBPDataSourceInfo getInfo();

    /**
     * Opens new execution context
     * @param monitor progress monitor
     * @return execution context
     */
    DBCExecutionContext openContext(DBRProgressMonitor monitor);

    /**
     * Opens new execution context
     * @param monitor progress monitor
     * @param task task description
     * @return execution context
     */
    DBCExecutionContext openContext(DBRProgressMonitor monitor, String task);

    /**
     * Executes test query agains connected database.
     *
     * @throws org.jkiss.dbeaver.DBException on any error
     * @param monitor
     */
    void checkConnection(DBRProgressMonitor monitor) throws DBException;

    /**
     * Reads base metadata from remote database or do any neccessary initialization routines.
     * @throws DBException on any DB error  @param monitor progress monitor
     */
    void initialize(DBRProgressMonitor monitor) throws DBException;

    /**
     * Refresh data source
     * @throws DBException on any DB error  @param monitor progress monitor
     */
    void refreshDataSource(DBRProgressMonitor monitor)
        throws DBException;

    /**
     * Closes datasource
     * @param monitor
     */
    void close(DBRProgressMonitor monitor);

}
