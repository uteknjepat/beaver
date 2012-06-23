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
package org.jkiss.dbeaver.model.impl.jdbc;

import org.jkiss.dbeaver.model.DBPClientHome;

import java.io.File;

/**
 * JDBCClientHome
 */
public abstract class JDBCClientHome implements DBPClientHome
{
    private final String id;
    private final File path;

    protected JDBCClientHome(String id, String path)
    {
        this.id = id;
        this.path = new File(path);
    }

    @Override
    public String getHomeId()
    {
        return id;
    }

    @Override
    public File getHomePath()
    {
        return path;
    }

}
