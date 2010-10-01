/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.controls.itemlist;

import net.sf.jkiss.utils.CommonUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.jkiss.dbeaver.ext.ui.INavigatorModelView;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.navigator.DBNTreeFolder;
import org.jkiss.dbeaver.model.navigator.DBNTreeNode;
import org.jkiss.dbeaver.registry.tree.DBXTreeNode;
import org.jkiss.dbeaver.runtime.load.DatabaseLoadService;
import org.jkiss.dbeaver.runtime.load.LoadingUtils;
import org.jkiss.dbeaver.utils.ViewUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * ItemListControl
 */
public class ItemListControl extends ObjectListControl<DBNNode> implements INavigatorModelView
{
    static final Log log = LogFactory.getLog(ItemListControl.class);

    private DBNNode node;

    public ItemListControl(
        Composite parent,
        int style,
        final IWorkbenchPart workbenchPart,
        DBNNode node)
    {
        super(parent, style, workbenchPart);
        this.node = node;


        ViewUtils.addContextMenu(this);

        setDoubleClickHandler(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event)
            {
                // Run default node action
                DBNNode dbmNode = ViewUtils.getSelectedNode(ItemListControl.this);
                if (dbmNode == null) {
                    return;
                }
                ViewUtils.runCommand(dbmNode.getDefaultCommandId(), workbenchPart);
            }
        });
    }

    public void fillData()
    {
        this.fillData(null);
    }

    public void fillData(DBXTreeNode metaNode)
    {
        super.loadData(LoadingUtils.executeService(
            new ItemLoadService(metaNode),
            new ItemsLoadVisualizer()));
    }

    public DBNNode getRootNode() {
        return node;
    }

    public Viewer getNavigatorViewer()
    {
        return getItemsViewer();
    }

    public IWorkbenchPart getWorkbenchPart()
    {
        return workbenchPart;
    }

    @Override
    protected Object getObjectValue(DBNNode item)
    {
        return item.getObject();
    }

    @Override
    protected String getObjectLabel(DBNNode item)
    {
        return item.getNodeName();
    }

    @Override
    protected Image getObjectImage(DBNNode item)
    {
        return item.getNodeIconDefault();
    }

    private class ItemLoadService extends DatabaseLoadService<List<DBNNode>> {

        private DBXTreeNode metaNode;

        protected ItemLoadService(DBXTreeNode metaNode)
        {
            super("Loading items", node);
            this.metaNode = metaNode;
        }

        public List<DBNNode> evaluate()
            throws InvocationTargetException, InterruptedException
        {
            try {
                List<DBNNode> items = new ArrayList<DBNNode>();
                List<? extends DBNNode> children = node.getChildren(getProgressMonitor());
                if (CommonUtils.isEmpty(children)) {
                    return items;
                }
                for (DBNNode item : children) {
                    if (getProgressMonitor().isCanceled()) {
                        break;
                    }
                    if (item instanceof DBNTreeFolder) {
                        continue;
                    }
                    if (metaNode != null) {
                        if (!(item instanceof DBNTreeNode)) {
                            continue;
                        }
                        if (((DBNTreeNode)item).getMeta() != metaNode) {
                            continue;
                        }
                    }
                    items.add(item);
                }
                return items;
            } catch (Throwable ex) {
                if (ex instanceof InvocationTargetException) {
                    throw (InvocationTargetException)ex;
                } else {
                    throw new InvocationTargetException(ex);
                }
            }
        }
    }

    private class ItemsLoadVisualizer extends ObjectsLoadVisualizer {

        @Override
        protected void loadLazyItems(List<ItemCell> lazyItems)
        {
            LoadingUtils.executeService(
                new ValueLoadService(lazyItems),
                new ValuesLoadVisualizer());
        }
    }

    private class ValueLoadService extends DatabaseLoadService<Object> {
        private List<ItemCell> lazyItems;
        public ValueLoadService(List<ItemCell> lazyItems)
        {
            super("Load item values", node);
            this.lazyItems = lazyItems;
        }

        public Object evaluate()
            throws InvocationTargetException, InterruptedException
        {
            for (ItemCell item : lazyItems) {
                // Check control is disposed
                if (isDisposed() || getProgressMonitor().isCanceled()) {
                    break;
                }
                // Extract value with progress monitor
                try {
                    item.value = item.prop.readValue(item.object.getObject(), getProgressMonitor());
                }
                catch (InvocationTargetException e) {
                    log.warn(e.getTargetException());
                    item.value = "???";
                }
                catch (IllegalAccessException e) {
                    log.warn(e);
                    item.value = "???";
                }
            }
            return null;
        }
    }

    private class ValuesLoadVisualizer extends ProgressVisualizer<Object> {

        public void visualizeLoading()
        {
            super.visualizeLoading();
            if (!getItemsViewer().getControl().isDisposed()) {
                getItemsViewer().refresh();
            }
        }
    }

}
