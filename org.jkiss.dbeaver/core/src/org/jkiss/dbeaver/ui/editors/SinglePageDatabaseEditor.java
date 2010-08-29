/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.editors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.ext.IDatabaseEditorInput;
import org.jkiss.dbeaver.ext.ui.IDataSourceUser;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.navigator.DBNEvent;
import org.jkiss.dbeaver.model.navigator.IDBNListener;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;

/**
 * SinglePageDatabaseEditor
 */
public abstract class SinglePageDatabaseEditor<INPUT_TYPE extends IDatabaseEditorInput> extends AbstractDatabaseEditor<INPUT_TYPE> implements IDataSourceUser, IDBNListener
{
    static final Log log = LogFactory.getLog(SinglePageDatabaseEditor.class);

    @SuppressWarnings("unchecked")
    public void init(IEditorSite site, IEditorInput input)
        throws PartInitException
    {
        super.init(site, input);

        DBeaverCore.getInstance().getMetaModel().addListener(this);
    }

    public void dispose()
    {
        DBeaverCore.getInstance().getMetaModel().removeListener(this);
        super.dispose();
    }

    public DBSDataSourceContainer getDataSourceContainer() {
        DBPDataSource dataSource = getDataSource();
        return dataSource == null ? null : dataSource.getContainer();
    }

    public DBPDataSource getDataSource() {
        return getEditorInput() == null || getEditorInput().getDatabaseObject() == null ? null : getEditorInput().getDatabaseObject().getDataSource();
    }

    public void nodeChanged(final DBNEvent event)
    {
        if (event.getNode() == getEditorInput().getTreeNode()) {
            if (event.getAction() == DBNEvent.Action.REMOVE) {
                getSite().getShell().getDisplay().asyncExec(new Runnable() { public void run() {
                    IWorkbenchPage workbenchPage = getSite().getWorkbenchWindow().getActivePage();
                    if (workbenchPage != null) {
                        workbenchPage.closeEditor(SinglePageDatabaseEditor.this, false);
                    }
                }});
            } else if (event.getAction() == DBNEvent.Action.REFRESH) {
                getSite().getShell().getDisplay().asyncExec(new Runnable() { public void run() {
                    refreshContent(event);
                }});
            }
        }
    }

    protected void refreshContent(DBNEvent event)
    {

    }

}