/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.dbc;

import org.jkiss.dbeaver.DBException;

import java.sql.SQLException;

/**
 * DBCException
 */
public class DBCException extends DBException
{
    public DBCException()
    {
    }

    public DBCException(String message)
    {
        super(message);
    }

    public DBCException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DBCException(Throwable cause)
    {
        super(cause);
    }

    public DBCException(SQLException ex)
    {
        super(ex);
    }
}
