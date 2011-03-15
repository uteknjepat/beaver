/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.mysql.model;

import net.sf.jkiss.utils.CommonUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.mysql.MySQLConstants;
import org.jkiss.dbeaver.ext.mysql.MySQLUtils;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCColumn;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSDataType;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSTableColumn;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.OverlayImageDescriptor;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MySQLTableColumn
 */
public class MySQLTableColumn extends JDBCColumn implements DBSTableColumn
{
    static final Log log = LogFactory.getLog(MySQLTableColumn.class);

    private static Pattern enumPattern = Pattern.compile("'([^']*)'");
    private Image columnImage;

    public static enum KeyType {
        PRI,
        UNI,
        MUL
    }

    private MySQLTable table;
    private String defaultValue;
    private long charLength;
    private boolean autoIncrement;
    private KeyType keyType;

    private List<String> enumValues;

    public MySQLTableColumn(
        MySQLTable table,
        ResultSet dbResult)
        throws DBException
    {
        this.table = table;
        loadInfo(dbResult);
    }

    private void loadInfo(ResultSet dbResult)
        throws DBException
    {
        setName(JDBCUtils.safeGetString(dbResult, MySQLConstants.COL_COLUMN_NAME));
        setOrdinalPosition(JDBCUtils.safeGetInt(dbResult, MySQLConstants.COL_ORDINAL_POSITION));
        String typeName = JDBCUtils.safeGetString(dbResult, MySQLConstants.COL_DATA_TYPE);
        String keyTypeName = JDBCUtils.safeGetString(dbResult, MySQLConstants.COL_COLUMN_KEY);
        if (!CommonUtils.isEmpty(keyTypeName)) {
            try {
                keyType = KeyType.valueOf(keyTypeName);
            } catch (IllegalArgumentException e) {
                log.debug(e);
            }
        }
        setTypeName(typeName);
        setValueType(MySQLUtils.typeNameToValueType(typeName));
        DBSDataType dataType = getDataSource().getInfo().getSupportedDataType(typeName.toUpperCase());
        this.charLength = JDBCUtils.safeGetLong(dbResult, MySQLConstants.COL_CHARACTER_MAXIMUM_LENGTH);
        if (this.charLength <= 0) {
            if (dataType != null) {
                setMaxLength(dataType.getPrecision());
            }
        } else {
            setMaxLength(this.charLength);
        }
        setDescription(JDBCUtils.safeGetString(dbResult, MySQLConstants.COL_COLUMN_COMMENT));
        setNullable("YES".equals(JDBCUtils.safeGetString(dbResult, MySQLConstants.COL_IS_NULLABLE)));
        setScale(JDBCUtils.safeGetInt(dbResult, MySQLConstants.COL_NUMERIC_SCALE));
        setPrecision(JDBCUtils.safeGetInt(dbResult, MySQLConstants.COL_NUMERIC_PRECISION));
        this.defaultValue = JDBCUtils.safeGetString(dbResult, MySQLConstants.COL_COLUMN_DEFAULT);
        String extra = JDBCUtils.safeGetString(dbResult, MySQLConstants.COL_COLUMN_EXTRA);
        this.autoIncrement = extra != null && extra.contains(MySQLConstants.EXTRA_AUTO_INCREMENT);

        String typeDesc = JDBCUtils.safeGetString(dbResult, MySQLConstants.COL_COLUMN_TYPE);
        if (!CommonUtils.isEmpty(typeDesc) &&
            (typeName.equalsIgnoreCase(MySQLConstants.TYPE_NAME_ENUM) || typeName.equalsIgnoreCase(MySQLConstants.TYPE_NAME_SET)))
        {
            enumValues = new ArrayList<String>();
            Matcher enumMatcher = enumPattern.matcher(typeDesc);
            while (enumMatcher.find()) {
                String enumStr = enumMatcher.group(1);
                enumValues.add(enumStr);
            }
        }
    }

    public DBSObject getParentObject()
    {
        return getTable();
    }

    public MySQLDataSource getDataSource()
    {
        return table.getDataSource();
    }

    @Property(name = "Table", viewable = true, order = 9)
    public MySQLTable getTable()
    {
        return table;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public long getCharLength()
    {
        return charLength;
    }

    @Property(name = "Auto Increment", viewable = true, order = 10)
    public boolean isAutoIncrement()
    {
        return autoIncrement;
    }

    @Property(name = "Key", viewable = true, order = 11)
    public KeyType getKeyType()
    {
        return keyType;
    }

    public List<String> getEnumValues()
    {
        return enumValues;
    }

    public Image getObjectImage()
    {
        if (columnImage == null) {
            columnImage = super.getObjectImage();
            if (keyType != null) {
                columnImage = getOverlayImage(columnImage, keyType);
            }
        }
        return columnImage;
    }

    private static final Map<Image, Map<KeyType, Image>> overlayCache = new IdentityHashMap<Image, Map<KeyType, Image>>();

    private static Image getOverlayImage(Image columnImage, KeyType keyType)
    {
        synchronized (overlayCache) {
            Map<KeyType, Image> keyTypeImageMap = overlayCache.get(columnImage);
            if (keyTypeImageMap == null) {
                keyTypeImageMap = new IdentityHashMap<KeyType, Image>();
                overlayCache.put(columnImage, keyTypeImageMap);
            }
            Image finalImage = keyTypeImageMap.get(keyType);
            if (finalImage == null) {
                OverlayImageDescriptor overlay = new OverlayImageDescriptor(columnImage.getImageData());
                ImageDescriptor overImage;
                switch (keyType) {
                    case PRI:
                    case UNI:
                        overImage = DBIcon.OVER_KEY.getImageDescriptor();
                        break;
                    default:
                        overImage = DBIcon.OVER_REFERENCE.getImageDescriptor();
                        break;
                }
                overlay.setBottomRight(new ImageDescriptor[] {overImage} );
                finalImage = overlay.createImage();
                keyTypeImageMap.put(keyType, finalImage);
            }

            return finalImage;
        }
    }

}
