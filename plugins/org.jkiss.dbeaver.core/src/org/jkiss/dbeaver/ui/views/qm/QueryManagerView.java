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
package org.jkiss.dbeaver.ui.views.qm;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.part.ViewPart;
import org.jkiss.dbeaver.ui.help.IHelpContextIds;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.querylog.QueryLogViewer;
import org.jkiss.dbeaver.ui.preferences.PrefPageQueryManager;

public class QueryManagerView extends ViewPart
{
    public static final String VIEW_ID = "org.jkiss.dbeaver.core.queryManager";

    private QueryLogViewer queryLogViewer;

    public QueryLogViewer getQueryLogViewer()
    {
        return queryLogViewer;
    }

    @Override
    public void createPartControl(Composite parent)
    {
        Composite group = UIUtils.createPlaceholder(parent, 1);

        queryLogViewer = new QueryLogViewer(group, getSite(), null);
        UIUtils.setHelp(group, IHelpContextIds.CTX_QUERY_MANAGER);
    }

    @Override
    public void setFocus()
    {
        queryLogViewer.getControl().setFocus();
    }

    public void openFilterDialog()
    {
        PreferenceDialog propDialog = PreferencesUtil.createPropertyDialogOn(
            getSite().getShell(),
            this,
            PrefPageQueryManager.PAGE_ID,
            null,//new String[]{pageId},
            null);
        if (propDialog != null) {
            propDialog.open();
        }
    }

    @Override
    public Object getAdapter(Class adapter)
    {
        if (adapter == IWorkbenchAdapter.class) {
            return new WorkbenchAdapter() {
                @Override
                public String getLabel(Object o)
                {
                    return "Query Manager";
                }
            };
        }
        return super.getAdapter(adapter);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
