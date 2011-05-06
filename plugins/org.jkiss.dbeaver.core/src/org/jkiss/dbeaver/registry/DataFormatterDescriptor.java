/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.registry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.jkiss.dbeaver.model.data.DBDDataFormatter;
import org.jkiss.dbeaver.model.data.DBDDataFormatterSample;
import org.jkiss.dbeaver.ui.properties.PropertyDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * DataFormatterDescriptor
 */
public class DataFormatterDescriptor extends AbstractDescriptor
{
    public static final String EXTENSION_ID = "org.jkiss.dbeaver.dataFormatter";

    private String id;
    private String className;
    private String name;
    private String description;
    private List<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
    private DBDDataFormatterSample sample;
    private Class<?> formatterClass;

    public DataFormatterDescriptor(IConfigurationElement config)
    {
        super(config.getContributor());

        this.id = config.getAttribute("id");
        this.className = config.getAttribute("class");
        this.name = config.getAttribute("label");
        this.description = config.getAttribute("description");

        IConfigurationElement[] propElements = config.getChildren(PropertyDescriptor.TAG_PROPERTY_GROUP);
        for (IConfigurationElement prop : propElements) {
            properties.addAll(PropertyDescriptor.extractProperties(prop));
        }
        Class<?> objectClass = getObjectClass(config.getAttribute("sampleClass"));
        try {
            sample = (DBDDataFormatterSample)objectClass.newInstance();
        } catch (Exception e) {
            log.error("Could not instantiate data formatter '" + getId() + "' sample");
        }
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public DBDDataFormatterSample getSample()
    {
        return sample;
    }

    public List<PropertyDescriptor> getProperties() {
        return properties;
    }

    public Class getFormatterClass()
    {
        if (formatterClass == null) {
            formatterClass = getObjectClass(className);
        }
        return formatterClass;
    }

    public DBDDataFormatter createFormatter() throws IllegalAccessException, InstantiationException
    {
        Class clazz = getFormatterClass();
        if (clazz == null) {
            return null;
        }
        return (DBDDataFormatter)clazz.newInstance();
    }

}
