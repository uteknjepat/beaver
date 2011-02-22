/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.controls;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.jkiss.dbeaver.registry.DataSourceProviderDescriptor;
import org.jkiss.dbeaver.registry.DataSourceProviderRegistry;
import org.jkiss.dbeaver.registry.DriverDescriptor;
import org.jkiss.dbeaver.ui.UIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * DriverTreeControl
 *
 * @author Serge Rieder
 */
public class DriverTreeControl extends TreeViewer implements ISelectionChangedListener, IDoubleClickListener {

    private Object site;
    private List<DataSourceProviderDescriptor> providers;

    public DriverTreeControl(Composite parent)
    {
        super(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    }

    public void initDrivers(Object site, List<DataSourceProviderDescriptor> providers)
    {
        this.site = site;
        this.providers = providers;
        if (this.providers == null) {
            this.providers = DataSourceProviderRegistry.getDefault().getDataSourceProviders();
        }

        TreeColumn nameColumn = new TreeColumn(getTree(), SWT.LEFT);
        nameColumn.setText("Name");

        //TreeColumn descColumn = new TreeColumn(getTree(), SWT.RIGHT);
        //descColumn.setText("Description");

        this.setContentProvider(new ViewContentProvider());
        this.setLabelProvider(new ViewLabelProvider());
        this.setInput(DataSourceProviderRegistry.getDefault());
        this.expandAll();
        this.addSelectionChangedListener(this);
        this.addDoubleClickListener(this);
        this.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

        UIUtils.packColumns(getTree(), false);
    }

    class ViewContentProvider implements IStructuredContentProvider,
        ITreeContentProvider
    {
        public void inputChanged(Viewer v, Object oldInput, Object newInput)
        {
        }

        public void dispose()
        {
        }

        public Object[] getElements(Object parent)
        {
            return getChildren(parent);
        }

        public Object getParent(Object child)
        {
            if (child instanceof DriverDescriptor) {
                return ((DriverDescriptor) child).getProviderDescriptor();
            } else if (child instanceof DataSourceProviderDescriptor) {
                return ((DataSourceProviderDescriptor) child).getRegistry();
            } else {
                return null;
            }
        }

        public Object[] getChildren(Object parent)
        {
            if (parent instanceof DataSourceProviderRegistry) {
                List<Object> children = new ArrayList<Object>();
                for (DataSourceProviderDescriptor provider : providers) {
                    List<DriverDescriptor> drivers = provider.getEnabledDrivers();
                    if (drivers.isEmpty()) {
                        // skip
                    } else if (drivers.size() == 1) {
                        children.add(drivers.get(0));
                    } else {
                        children.add(provider);
                    }
                }
                return children.toArray();
            } else if (parent instanceof DataSourceProviderDescriptor) {
                final List<DriverDescriptor> drivers = ((DataSourceProviderDescriptor) parent).getEnabledDrivers();
                Collections.sort(drivers, new Comparator<DriverDescriptor>() {
                    public int compare(DriverDescriptor o1, DriverDescriptor o2)
                    {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                return drivers.toArray();
            } else {
                return new Object[0];
            }
        }

        public boolean hasChildren(Object parent)
        {
            if (parent instanceof DataSourceProviderRegistry) {
                return !providers.isEmpty();
            } else if (parent instanceof DataSourceProviderDescriptor) {
                return !((DataSourceProviderDescriptor) parent).getEnabledDrivers().isEmpty();
            }
            return false;
        }
    }

    class ViewLabelProvider extends CellLabelProvider
    {

        public void update(ViewerCell cell) {
            cell.setText(getText(cell.getElement(), cell.getColumnIndex()));
            cell.setImage(getImage(cell.getElement(), cell.getColumnIndex()));
        }

        public String getText(Object obj, int index)
        {
            if (obj instanceof DataSourceProviderDescriptor) {
                return index == 0 ? ((DataSourceProviderDescriptor) obj).getName() : ((DataSourceProviderDescriptor) obj).getDescription();
            } else if (obj instanceof DriverDescriptor) {
                return index == 0 ? ((DriverDescriptor) obj).getName() : ((DriverDescriptor) obj).getDescription();
            } else {
                return index == 0 ? obj.toString() : "";
            }
        }

        public Image getImage(Object obj, int index)
        {
            if (index != 0) {
                return null;
            }
            String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (obj instanceof DataSourceProviderDescriptor) {
                Image icon = ((DataSourceProviderDescriptor) obj).getIcon();
                if (icon != null) {
                    return icon;
                }
			    imageKey = ISharedImages.IMG_OBJ_FOLDER;
            } else if (obj instanceof DriverDescriptor) {
                Image icon = ((DriverDescriptor) obj).getIcon();
                if (icon != null) {
                    return icon;
                }
            }

            return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
        }
    }


    public void selectionChanged(SelectionChangedEvent event)
    {
        if (site instanceof ISelectionChangedListener) {
            ((ISelectionChangedListener)site).selectionChanged(event);
        }
    }

    public void doubleClick(DoubleClickEvent event)
    {
        if (site instanceof IDoubleClickListener) {
            ((IDoubleClickListener)site).doubleClick(event);
        }
    }

}
