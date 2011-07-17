/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.editors.sql;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.jkiss.dbeaver.core.DBeaverActivator;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.ext.IDataSourceProvider;
import org.jkiss.dbeaver.runtime.sql.SQLStatementInfo;
import org.jkiss.dbeaver.ui.ICommandIds;
import org.jkiss.dbeaver.ui.TextUtils;
import org.jkiss.dbeaver.ui.editors.sql.syntax.*;
import org.jkiss.dbeaver.ui.editors.sql.util.SQLSymbolInserter;
import org.jkiss.dbeaver.ui.editors.text.BaseTextEditor;
import org.jkiss.utils.CommonUtils;

import java.util.*;

/**
 * SQL Executor
 */
public abstract class SQLEditorBase extends BaseTextEditor implements IDataSourceProvider
{
    static final Log log = LogFactory.getLog(SQLEditorBase.class);

    private SQLSyntaxManager syntaxManager;

    private ProjectionSupport projectionSupport;

    private ProjectionAnnotationModel annotationModel;
    private Map<Annotation, Position> curAnnotations;

    private IAnnotationAccess annotationAccess;
    private boolean hasVerticalRuler = true;

    public SQLEditorBase()
    {
        super();
        syntaxManager = new SQLSyntaxManager();

        setDocumentProvider(new SQLDocumentProvider());
        setSourceViewerConfiguration(new SQLEditorSourceViewerConfiguration(
            this,
            syntaxManager,
            getCompletionProcessor(),
            new SQLHyperlinkDetector(this, syntaxManager)));
    }

    protected IContentAssistProcessor getCompletionProcessor()
    {
        return new SQLCompletionProcessor(this);
    }

    public SQLSyntaxManager getSyntaxManager()
    {
        return syntaxManager;
    }

    public IDocument getDocument()
    {
        IDocumentProvider provider = getDocumentProvider();
        return provider == null ? null : provider.getDocument(getEditorInput());
    }

    public ProjectionAnnotationModel getAnnotationModel()
    {
        return annotationModel;
    }

    public void createPartControl(Composite parent)
    {
        setRangeIndicator(new DefaultRangeIndicator());

        super.createPartControl(parent);

        ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
        projectionSupport = new ProjectionSupport(
            viewer,
            getAnnotationAccess(),
            getSharedColors());
        projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
        projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
        projectionSupport.install();

        viewer.doOperation(ProjectionViewer.TOGGLE);

        annotationModel = viewer.getProjectionAnnotationModel();

        // Symbol inserter
        {
            SQLSymbolInserter symbolInserter = new SQLSymbolInserter(this, getSourceViewer());

            IPreferenceStore preferenceStore = DBeaverCore.getInstance().getGlobalPreferenceStore();
            boolean closeSingleQuotes = preferenceStore.getBoolean(SQLPreferenceConstants.SQLEDITOR_CLOSE_SINGLE_QUOTES);
            boolean closeDoubleQuotes = preferenceStore.getBoolean(SQLPreferenceConstants.SQLEDITOR_CLOSE_DOUBLE_QUOTES);
            boolean closeBrackets = preferenceStore.getBoolean(SQLPreferenceConstants.SQLEDITOR_CLOSE_BRACKETS);

            symbolInserter.setCloseSingleQuotesEnabled(closeSingleQuotes);
            symbolInserter.setCloseDoubleQuotesEnabled(closeDoubleQuotes);
            symbolInserter.setCloseBracketsEnabled(closeBrackets);

            ISourceViewer sourceViewer = getSourceViewer();
            if (sourceViewer instanceof ITextViewerExtension) {
                ((ITextViewerExtension) sourceViewer).prependVerifyKeyListener(symbolInserter);
            }
        }
    }

    public void updatePartControl(IEditorInput input) {
        super.updatePartControl(input);
    }

    @Override
    protected IVerticalRuler createVerticalRuler()
    {
        return hasVerticalRuler ? super.createVerticalRuler() : new VerticalRuler(0);
    }

    public void setHasVerticalRuler(boolean hasVerticalRuler)
    {
        this.hasVerticalRuler = hasVerticalRuler;
    }

    @Override
    protected void doSetInput(IEditorInput input) throws CoreException
    {
        IEditorInput oldInput = getEditorInput();
        super.doSetInput(input);
        if (oldInput != null && input != null && !oldInput.equals(input)) {
            // Editor input changed - it may be a result of resource change (move/rename)
            reloadSyntaxRules();
        }
    }

    protected ISharedTextColors getSharedColors()
    {
        return DBeaverCore.getInstance().getSharedTextColors();
    }

    protected ISourceViewer createSourceViewer(Composite parent,
        IVerticalRuler ruler, int styles)
    {
        OverviewRuler overviewRuler = new OverviewRuler(
            getAnnotationAccess(),
            VERTICAL_RULER_WIDTH,
            getSharedColors());

        return new SQLEditorSourceViewer(
            parent,
            ruler,
            overviewRuler,
            true,
            styles);
    }

    private IAnnotationAccess getAnnotationAccess()
    {
        if (annotationAccess == null) {
            annotationAccess = new SQLMarkerAnnotationAccess();
        }
        return annotationAccess;
    }

/*
    protected void adjustHighlightRange(int offset, int length)
    {
        ISourceViewer viewer = getSourceViewer();
        if (viewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
            extension.exposeModelRange(new Region(offset, length));
        }
    }
*/

    public Object getAdapter(Class required)
    {
        if (projectionSupport != null) {
            Object adapter = projectionSupport.getAdapter(
                getSourceViewer(), required);
            if (adapter != null)
                return adapter;
        }

        return super.getAdapter(required);
    }

    public void dispose()
    {
        if (syntaxManager != null) {
            syntaxManager.dispose();
            syntaxManager = null;
        }

        super.dispose();
    }

    protected void createActions()
    {
        super.createActions();

        ResourceBundle bundle = DBeaverActivator.getResourceBundle();

        IAction a = new TextOperationAction(
            bundle,
            SQLEditorContributor.getActionResourcePrefix(SQLEditorContributor.ACTION_CONTENT_ASSIST_PROPOSAL),
            this,
            ISourceViewer.CONTENTASSIST_PROPOSALS);
        a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        setAction(SQLEditorContributor.ACTION_CONTENT_ASSIST_PROPOSAL, a);

        a = new TextOperationAction(
            bundle,
            SQLEditorContributor.getActionResourcePrefix(SQLEditorContributor.ACTION_CONTENT_ASSIST_TIP),
            this,
            ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION);
        a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
        setAction(SQLEditorContributor.ACTION_CONTENT_ASSIST_TIP, a);

        a = new TextOperationAction(
            bundle,
            SQLEditorContributor.getActionResourcePrefix(SQLEditorContributor.ACTION_CONTENT_FORMAT_PROPOSAL),
            this,
            ISourceViewer.FORMAT);
        a.setActionDefinitionId(ICommandIds.CMD_CONTENT_FORMAT);
        setAction(SQLEditorContributor.ACTION_CONTENT_FORMAT_PROPOSAL, a);

/*
        // Add the task action to the Edit pulldown menu (bookmark action is  'free')
        ResourceAction ra = new AddTaskAction(bundle, "AddTask.", this);
        ra.setHelpContextId(ITextEditorHelpContextIds.ADD_TASK_ACTION);
        ra.setActionDefinitionId(ITextEditorActionDefinitionIds.ADD_TASK);
        setAction(IDEActionFactory.ADD_TASK.getId(), ra);
*/
    }

    public void editorContextMenuAboutToShow(IMenuManager menu)
    {
        super.editorContextMenuAboutToShow(menu);

        menu.add(new Separator("content"));//$NON-NLS-1$
        addAction(menu, SQLEditorContributor.ACTION_CONTENT_ASSIST_PROPOSAL);
        addAction(menu, SQLEditorContributor.ACTION_CONTENT_ASSIST_TIP);
        addAction(menu, SQLEditorContributor.ACTION_CONTENT_FORMAT_PROPOSAL);
        //addAction(menu, ACTION_DEFINE_FOLDING_REGION);
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    public void reloadSyntaxRules()
    {
        // Refresh syntax
        final SQLSyntaxManager syntaxManager = getSyntaxManager();
        if (syntaxManager != null) {
            syntaxManager.setDataSource(getDataSource());
            syntaxManager.refreshRules();
        }

        Document document = (Document) getDocument();
        if (document != null) {
            IDocumentPartitioner partitioner = new FastPartitioner(
                new SQLPartitionScanner(syntaxManager),
                SQLPartitionScanner.SQL_PARTITION_TYPES );
            partitioner.connect( document );
            document.setDocumentPartitioner( SQLPartitionScanner.SQL_PARTITIONING, partitioner );

            ProjectionViewer projectionViewer = (ProjectionViewer)getSourceViewer();
            if (projectionViewer != null && document.getLength() > 0) {
                // Refresh viewer
                //projectionViewer.getTextWidget().redraw();
                try {
                    projectionViewer.reinitializeProjection();
                } catch (Throwable ex) {
                    // We can catch OutOfMemory here for too big/complex documents
                    log.warn("Can't initialize SQL syntax projection", ex); //$NON-NLS-1$
                }
            }
        }

        // Update configuration
        if (getSourceViewerConfiguration() instanceof SQLEditorSourceViewerConfiguration) {
            ((SQLEditorSourceViewerConfiguration) getSourceViewerConfiguration()).onDataSourceChange();
        }
    }

    protected SQLStatementInfo extractActiveQuery()
    {
        SQLStatementInfo sqlQuery;
        ITextSelection selection = (ITextSelection) getSelectionProvider().getSelection();
        String selText = selection.getText().trim();
        selText = selText.trim();
        if (selText.endsWith(getSyntaxManager().getStatementDelimiter())) {
            selText = selText.substring(0, selText.length() - getSyntaxManager().getStatementDelimiter().length());
        }
        if (!CommonUtils.isEmpty(selText)) {
            sqlQuery = new SQLStatementInfo(selText);
            sqlQuery.setOffset(selection.getOffset());
            sqlQuery.setLength(selection.getLength());
        } else {
            sqlQuery = extractQueryAtPos(selection.getOffset());
        }
        // Check query do not ends with delimiter
        // (this may occur if user selected statement including delimiter)
        if (sqlQuery == null || CommonUtils.isEmpty(sqlQuery.getQuery())) {
            return null;
        }
        return sqlQuery;
    }

    public SQLStatementInfo extractQueryAtPos(int currentPos)
    {
        IDocument document = getDocument();
        if (document.getLength() == 0) {
            return null;
        }
        // Extract part of document between empty lines
        int startPos = 0;
        int endPos = document.getLength();
        try {
            int currentLine = document.getLineOfOffset(currentPos);
            int lineOffset = document.getLineOffset(currentLine);
            int linesCount = document.getNumberOfLines();
            int firstLine = currentLine, lastLine = currentLine;
            while (firstLine > 0) {
                if (TextUtils.isEmptyLine(document, firstLine)) {
                    break;
                }
                firstLine--;
            }
            while (lastLine < linesCount) {
                if (TextUtils.isEmptyLine(document, lastLine)) {
                    break;
                }
                lastLine++;
            }
            if (lastLine >= linesCount) {
                lastLine = linesCount - 1;
            }
            startPos = document.getLineOffset(firstLine);
            endPos = document.getLineOffset(lastLine) + document.getLineLength(lastLine);
            //String lastDelimiter = document.getLineDelimiter(lastLine);
            //if (lastDelimiter != null) {
            //    endPos += lastDelimiter.length();
            //}

            // Move currentPos at line begin
            currentPos = lineOffset;
        }
        catch (BadLocationException e) {
            log.warn(e);
        }

        // Parse range
        SQLSyntaxManager syntaxManager = getSyntaxManager();
        syntaxManager.setRange(document, startPos, endPos - startPos);
        int statementStart = startPos;
        for (;;) {
            IToken token = syntaxManager.nextToken();
            int tokenOffset = syntaxManager.getTokenOffset();
            if (token.isEOF() ||
                (token instanceof SQLDelimiterToken && tokenOffset >= currentPos)||
                tokenOffset > endPos)
            {
                // get position before last token start
                if (tokenOffset > endPos) {
                    tokenOffset = endPos;
                }

                if (tokenOffset >= document.getLength()) {
                    // Sometimes (e.g. when comment finishing script text)
                    // last token offset is beyon document range
                    tokenOffset = document.getLength();
                }
                assert (tokenOffset >= currentPos);
                try {
                    // remove leading spaces
                    while (statementStart < tokenOffset && Character.isWhitespace(document.getChar(statementStart))) {
                        statementStart++;
                    }
                    // remove trailing spaces
                    while (statementStart < tokenOffset && Character.isWhitespace(document.getChar(tokenOffset - 1))) {
                        tokenOffset--;
                    }
                    String queryText = document.get(statementStart, tokenOffset - statementStart);
                    queryText = queryText.trim();
                    if (queryText.endsWith(syntaxManager.getStatementDelimiter())) {
                        queryText = queryText.substring(0, queryText.length() - syntaxManager.getStatementDelimiter().length());
                    }
                    // make script line
                    SQLStatementInfo statementInfo = new SQLStatementInfo(queryText.trim());
                    statementInfo.setOffset(statementStart);
                    statementInfo.setLength(tokenOffset - statementStart);
                    return statementInfo;
                } catch (BadLocationException ex) {
                    log.warn("Can't extract query", ex); //$NON-NLS-1$
                    return null;
                }
            }
            if (token instanceof SQLDelimiterToken) {
                statementStart = tokenOffset + syntaxManager.getTokenLength();
            }
            if (token.isEOF()) {
                return null;
            }
        }
    }

    public void doOperation(int operation)
    {
        ((SQLEditorSourceViewer) getSourceViewer()).doOperation(operation);
    }

    public synchronized void updateFoldingStructure(int offset, int length, List<Position> positions)
    {
        if (curAnnotations == null) {
            curAnnotations = new HashMap<Annotation, Position>();
        }
        List<Annotation> deletedAnnotations = new ArrayList<Annotation>();
        Map<Annotation, Position> newAnnotations = new HashMap<Annotation, Position>();

        // Delete all annotations if specified range
        for (Map.Entry<Annotation,Position> entry : curAnnotations.entrySet()) {
            int entryOffset = entry.getValue().getOffset();
            if (entryOffset >= offset && entryOffset < offset + length) {
                deletedAnnotations.add(entry.getKey());
            }
        }
        for (Annotation annotation : deletedAnnotations) {
            curAnnotations.remove(annotation);
        }

        // Add new annotations
        for (Position position : positions) {
            ProjectionAnnotation annotation = new ProjectionAnnotation();
            newAnnotations.put(annotation, position);
        }

        // Modify annotation set
        annotationModel.modifyAnnotations(
            deletedAnnotations.toArray(new Annotation[deletedAnnotations.size()]),
            newAnnotations,
            null);

        // Update current annotations
        curAnnotations.putAll(newAnnotations);
    }

    protected void syncExec(Runnable runnable)
    {
        Display.getDefault().syncExec(runnable);
    }

    protected void asyncExec(Runnable runnable)
    {
        Display.getDefault().asyncExec(runnable);
    }

    public boolean isDisposed()
    {
        return
            getSourceViewer() == null ||
            getSourceViewer().getTextWidget() == null ||
            getSourceViewer().getTextWidget().isDisposed();
    }

}
