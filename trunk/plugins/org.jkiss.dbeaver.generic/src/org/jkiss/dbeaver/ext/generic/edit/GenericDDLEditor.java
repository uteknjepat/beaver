package org.jkiss.dbeaver.ext.generic.edit;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.IDatabasePersistAction;
import org.jkiss.dbeaver.ext.generic.model.GenericTable;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditorNested;

public class GenericDDLEditor extends SQLEditorNested<GenericTable> {

    public GenericDDLEditor()
    {
    }

    @Override
    protected boolean isReadOnly()
    {
        return true;
    }

    @Override
    protected String getSourceText(DBRProgressMonitor monitor) throws DBException
    {
        if (!getSourceObject().isPersisted()) {
            return "";
        }
        GenericTableManager tableManager = new GenericTableManager();
        IDatabasePersistAction[] ddlActions = tableManager.getTableDDL(monitor, getSourceObject());
        return DBUtils.generateScript(getSourceObject().getDataSource(), ddlActions);
    }

    @Override
    protected void setSourceText(String sourceText)
    {
        // We are read-only
    }

}