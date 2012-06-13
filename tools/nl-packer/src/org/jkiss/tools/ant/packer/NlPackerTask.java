/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.tools.ant.packer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Extract resources properties files from the project for localization.
 */
public class NlPackerTask extends Task
{
    public static final FilenameFilter PROPERTIES_FILTER = new FilenameFilter()
    {
        @Override
        public boolean accept(File dir, String name)
        {
            return name.endsWith(".properties");
        }
    };
    public static final FilenameFilter JARS_FILTER = new FilenameFilter()
    {
        @Override
        public boolean accept(File dir, String name)
        {
            return name.endsWith(".jar");
        }
    };
    public static final String JKISS_PREFIX = "org.jkiss.dbeaver";
    public static final FilenameFilter JKISS_PLUGINS_FILTER = new FilenameFilter()
    {
        @Override
        public boolean accept(File dir, String name)
        {
            return name.startsWith(JKISS_PREFIX) && name.endsWith(".jar");
        }
    };

    private String dbeaverLocation;
    private String nlPropertiesLocation;

    // The method executing the task
    @Override
    public void execute() throws BuildException
    {
/*
        Project prj = this.getProject();
        File baseDir = prj.getBaseDir();
        String defaultTarget = prj.getDefaultTarget();
        String dscrptn = prj.getDescription();
        Hashtable inheritedProperties = prj.getInheritedProperties();
        Hashtable userProperties = prj.getUserProperties();
        Target owningTarget = this.getOwningTarget();
        Hashtable properties = prj.getProperties();
*/
        System.out.println("dbeaverLocation = " + dbeaverLocation);
        System.out.println("nlPropertiesLocation = " + nlPropertiesLocation);

        File dbeaverDir = new File(dbeaverLocation);
        if (!dbeaverDir.exists() || !dbeaverDir.isDirectory()) {
            throw new BuildException("Can't find DBeaver directory " + dbeaverLocation);
        }

        File nlPropertiesDir = new File(nlPropertiesLocation);
        if (!nlPropertiesDir.exists() || !nlPropertiesDir.isDirectory()) {
            throw new BuildException("Can't find nl-properties directory " + nlPropertiesLocation);
        }

        File pluginsDir = new File(dbeaverDir, "plugins");
        File[] dbeaverPlugins = pluginsDir.listFiles(JKISS_PLUGINS_FILTER);

        File[] nlPropertiesDirs = nlPropertiesDir.listFiles();
        for (File dbeaverPlugin : dbeaverPlugins) {
            String dbeaverPluginName = dbeaverPlugin.getName();
            if (dbeaverPluginName.indexOf(".ext") == JKISS_PREFIX.length()) {
                dbeaverPluginName = JKISS_PREFIX + dbeaverPluginName.substring(JKISS_PREFIX.length() + 4);
            }
            //System.out.println(":: " + dbeaverPluginName);
            for (File propertiesDir : nlPropertiesDirs) {
                final String pluginName = propertiesDir.getName();
                if (dbeaverPluginName.startsWith(pluginName)) { // searching an appropriate plugin
                    try {
                        List<File> filesToPack = new ArrayList<File>();
                        filesToPack.addAll(Arrays.asList(propertiesDir.listFiles(PROPERTIES_FILTER)));
                        File srcDir = new File(propertiesDir, "src");
                        filesToPack.addAll(Arrays.asList(srcDir.listFiles()));
                        File pluginZipFile = new File(nlPropertiesLocation, pluginName + ".zip");

                        Packager.packZip(pluginZipFile, filesToPack);

                        localizePlugin(Arrays.asList(pluginZipFile), dbeaverPlugin);

                        if (!pluginZipFile.delete()) {
                            System.out.println("Can't delete temp packed plugin file " + pluginZipFile.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    public static void localizePlugin(List<File> babelFiles, File pluginFile)
    {
        if (!pluginFile.exists() || babelFiles == null || babelFiles.isEmpty()) {
            return;
        }
        // get a temp file
        File tempFile = new File(pluginFile.getName() + ".tmp");

        ZipFile destZip = null;
        try {
            boolean renameOk = pluginFile.renameTo(tempFile);
            if (!renameOk) {
                throw new BuildException("could not rename the file " + pluginFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
            }

            ZipOutputStream out;
            try {
                out = new ZipOutputStream(new FileOutputStream(pluginFile));
            } catch (FileNotFoundException e) {
                System.out.println("Can't create an output stream for destination zip file " + pluginFile.getAbsolutePath());
                return;
            }

            try {
                destZip = new ZipFile(tempFile);
            } catch (IOException e) {
                System.out.println("A problem with processing destination zip file " + pluginFile.getAbsolutePath());
                return;
            }

            for (File sourceZipFile : babelFiles) {
                // append source file entries
                ZipFile sourceZip = null;
                try {
                    sourceZip = new ZipFile(sourceZipFile);
                } catch (IOException e) {
                    System.out.println("A problem with processing source zip file " + sourceZipFile.getAbsolutePath());
                    continue;
                }
                Enumeration<? extends ZipEntry> srcEntries = sourceZip.entries();
                while (srcEntries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) srcEntries.nextElement();
                    String entryName = entry.getName();
                    //System.out.println("entryName = " + entryName);
                    ZipEntry newEntry = new ZipEntry(entryName);
                    try {
                        out.putNextEntry(newEntry);

                        BufferedInputStream bis = new BufferedInputStream(sourceZip.getInputStream(entry));
                        while (bis.available() > 0) {
                            out.write(bis.read());
                        }
                        out.closeEntry();
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Can't copy " + entryName + " from " + sourceZipFile.getAbsolutePath() +
                                " to " + pluginFile.getAbsolutePath() + ". " + e.getMessage());
                        //break;
                    }
                }
                try {
                    sourceZip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // append dest file own entries
            Enumeration<? extends ZipEntry> destEntries = destZip.entries();
            while (destEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) destEntries.nextElement();
                //System.out.println(entry.getName());
                String entryName = entry.getName();
                ZipEntry newEntry = new ZipEntry(entryName);
                try {
                    out.putNextEntry(newEntry);

                    BufferedInputStream bis = new BufferedInputStream(destZip.getInputStream(entry));
                    while (bis.available() > 0) {
                        out.write(bis.read());
                    }
                    out.closeEntry();
                    bis.close();
                } catch (IOException e) {
                    System.out.println("Can't copy " + entryName + " from temporary file to " +
                            pluginFile.getAbsolutePath() + ". " + e.getMessage());
                    //break;
                }
            }

            // Complete the ZIP file
            try {
                out.close();
            } catch (IOException e) {
                System.out.println("Can't close output stream for destination " + pluginFile.getAbsolutePath());
            }
        } finally {
            try {
                destZip.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!tempFile.delete()) {
                System.out.println("Can't delete temp file " + tempFile.getAbsolutePath());
            }
        }
    }

    public void setDbeaverLocation(String dbeaverLocation)
    {
        this.dbeaverLocation = dbeaverLocation;
    }

    public void setNlPropertiesLocation(String nlPropertiesLocation)
    {
        this.nlPropertiesLocation = nlPropertiesLocation;
    }
}
