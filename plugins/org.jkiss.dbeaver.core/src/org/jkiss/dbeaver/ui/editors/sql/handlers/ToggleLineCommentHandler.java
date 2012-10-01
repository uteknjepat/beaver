/*
 * Copyright (C) 2010-2012 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.ui.editors.sql.handlers;

import org.eclipse.jface.text.*;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditorBase;
import org.jkiss.utils.CommonUtils;

public final class ToggleLineCommentHandler extends AbstractCommentHandler {

    @Override
    protected void processAction(SQLEditorBase textEditor, IDocument document, ITextSelection textSelection) throws BadLocationException
    {
        if (textEditor.getDataSource() == null) {
            return;
        }
        String[] singleLineComments = textEditor.getDataSource().getContainer().getKeywordManager().getSingleLineComments();
        if (CommonUtils.isEmpty(singleLineComments)) {
            // Single line comments are not supported
            return;
        }
        int selOffset = textSelection.getOffset();
        int selLength = textSelection.getLength();
        DocumentRewriteSession rewriteSession = null;
        if (document instanceof IDocumentExtension4) {
            rewriteSession = ((IDocumentExtension4) document).startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
        }
        for (int lineNum = textSelection.getEndLine(); lineNum >= textSelection.getStartLine(); lineNum--) {
            int lineOffset = document.getLineOffset(lineNum);
            int lineLength = document.getLineLength(lineNum);
            String lineComment = null;
            for (String commentString : singleLineComments) {
                if (document.get(lineOffset, lineLength).startsWith(commentString)) {
                    lineComment = commentString;
                    break;
                }
            }
            if (lineComment != null) {
                // Remove comment
                document.replace(lineOffset, lineComment.length(), "");
                selLength -= lineComment.length();
            } else {
                // Add comment
                document.replace(lineOffset, 0, singleLineComments[0]);
                selLength += singleLineComments[0].length();
            }
        }
        if (rewriteSession != null) {
            ((IDocumentExtension4) document).stopRewriteSession(rewriteSession);
        }

        textEditor.getSelectionProvider().setSelection(new TextSelection(selOffset, selLength));
    }
}