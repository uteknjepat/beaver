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
package org.jkiss.dbeaver.ext.oracle.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.jkiss.dbeaver.ext.IDatabaseEditorInput;
import org.jkiss.dbeaver.ext.oracle.OracleMessages;
import org.jkiss.dbeaver.ext.oracle.model.OracleDataSource;
import org.jkiss.dbeaver.ext.oracle.model.session.OracleServerSessionManager;
import org.jkiss.dbeaver.model.admin.sessions.DBAServerSession;
import org.jkiss.dbeaver.model.admin.sessions.DBAServerSessionManager;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.dialogs.ConfirmationDialog;
import org.jkiss.dbeaver.ui.editors.SinglePageDatabaseEditor;
import org.jkiss.dbeaver.ui.views.session.SessionManagerViewer;

import java.util.HashMap;
import java.util.Map;

/**
 * OracleSessionEditor
 */
public class OracleSessionEditor extends SinglePageDatabaseEditor<IDatabaseEditorInput>
{
    private SessionManagerViewer sessionsViewer;
    private DisconnectSessionAction killSessionAction;
    private DisconnectSessionAction disconnectSessionAction;

    @Override
    public void dispose()
    {
        sessionsViewer.dispose();
        super.dispose();
    }

    @Override
    public void createPartControl(Composite parent) {
        killSessionAction = new DisconnectSessionAction(true);
        disconnectSessionAction = new DisconnectSessionAction(false);
        sessionsViewer = new SessionManagerViewer(this, parent, new OracleServerSessionManager((OracleDataSource) getDataSource())) {
            @Override
            protected void contributeToToolbar(DBAServerSessionManager sessionManager, ToolBarManager toolBar)
            {
                toolBar.add(killSessionAction);
                toolBar.add(disconnectSessionAction);
                toolBar.add(new Separator());
            }

            @Override
            protected void onSessionSelect(DBAServerSession session)
            {
                super.onSessionSelect(session);
                killSessionAction.setEnabled(session != null);
                disconnectSessionAction.setEnabled(session != null);
            }
        };

        sessionsViewer.refreshSessions();
    }

    @Override
    public void refreshPart(Object source, boolean force)
    {
        sessionsViewer.refreshSessions();
    }

    private class DisconnectSessionAction extends Action {
        private final boolean kill;
        public DisconnectSessionAction(boolean kill)
        {
            super(
                kill ? OracleMessages.editors_oracle_session_editor_title_kill_session : OracleMessages.editors_oracle_session_editor_title_disconnect_session,
                kill ? DBIcon.REJECT.getImageDescriptor() : DBIcon.SQL_DISCONNECT.getImageDescriptor());
            this.kill = kill;
        }

        @Override
        public void run()
        {
            final DBAServerSession session = sessionsViewer.getSelectedSession();
            final String action = (kill ? OracleMessages.editors_oracle_session_editor_action_kill : OracleMessages.editors_oracle_session_editor_action_disconnect) + OracleMessages.editors_oracle_session_editor_action__session;
            ConfirmationDialog dialog = new ConfirmationDialog(
                getSite().getShell(),
                action,
                null,
                NLS.bind(OracleMessages.editors_oracle_session_editor_confirm_action, action.toLowerCase(), session),
                MessageDialog.CONFIRM,
                new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL },
                0,
                OracleMessages.editors_oracle_session_editor_confirm_title,
                false);
            if (dialog.open() == IDialogConstants.YES_ID) {
                Map<String, Object> options = new HashMap<String, Object>();
                if (kill) {
                    options.put(OracleServerSessionManager.PROP_KILL_SESSION, kill);
                }
                if (dialog.getToggleState()) {
                    options.put(OracleServerSessionManager.PROP_IMMEDIATE, true);
                }
                sessionsViewer.alterSession(session, options);
            }
        }
    }

}