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
package org.jkiss.dbeaver.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.jkiss.dbeaver.ext.IDatabaseEditor;
import org.jkiss.dbeaver.ext.IDatabaseEditorInput;
import org.jkiss.dbeaver.ext.ui.IActiveWorkbenchPart;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.edit.DBECommand;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBECommandReflector;
import org.jkiss.dbeaver.model.struct.DBSObject;

/**
 * AbstractDatabaseObjectEditor
 */
public abstract class AbstractDatabaseObjectEditor<OBJECT_TYPE extends DBSObject>
    extends EditorPart implements IDatabaseEditor, IActiveWorkbenchPart
{

    @Override
    public void init(IEditorSite site, IEditorInput input)
        throws PartInitException
    {
        super.setSite(site);
        super.setInput(input);
    }

    @Override
    public void doSave(IProgressMonitor monitor)
    {
    }

    @Override
    public void doSaveAs()
    {
    }

    @Override
    public boolean isDirty()
    {
        return getEditorInput().getCommandContext().isDirty();
    }

    @Override
    public boolean isSaveAsAllowed()
    {
        return false;
    }

    @Override
    public void activatePart()
    {
        // do nothing by default
    }

    @Override
    public void deactivatePart() {
        // do nothing by default
    }

    @Override
    public DBPDataSource getDataSource() {
        OBJECT_TYPE object = getDatabaseObject();
        return object == null ? null : object.getDataSource();
    }

    public OBJECT_TYPE getDatabaseObject() {
        return (OBJECT_TYPE) getEditorInput().getDatabaseObject();
    }

    @Override
    public IDatabaseEditorInput getEditorInput()
    {
        return (IDatabaseEditorInput)super.getEditorInput();
    }

    /*
    protected void addChangeCommand(DBECommand<OBJECT_TYPE> command)
    {
        this.objectManager.addCommand(command, null);
        firePropertyChange(PROP_DIRTY);
    }
*/

    private DBECommandContext getObjectCommander()
    {
        return getEditorInput().getCommandContext();
    }

    public void addChangeCommand(
        DBECommand<OBJECT_TYPE> command,
        DBECommandReflector<OBJECT_TYPE, ? extends DBECommand<OBJECT_TYPE>> reflector)
    {
        getObjectCommander().addCommand(command, reflector);
    }

    public void removeChangeCommand(DBECommand<OBJECT_TYPE> command)
    {
        getObjectCommander().removeCommand(command);
    }

    public void updateChangeCommand(DBECommand<OBJECT_TYPE> command,
        DBECommandReflector<OBJECT_TYPE, ? extends DBECommand<OBJECT_TYPE>> reflector)
    {
        getObjectCommander().updateCommand(command, reflector);
    }
}