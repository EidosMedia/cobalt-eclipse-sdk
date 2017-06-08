/*
 * Copyright (c) 2016 EidosMedia S.p.A.. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of EidosMedia
 * S.p.A.. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with EidosMedia.
 * 
 * EIDOSMEDIA MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. EIDOSMEDIA SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES.
 */
package com.eidosmedia.cobalt.eclipse.sdk.internal.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;

import com.eidosmedia.cobalt.eclipse.sdk.internal.Logger;

/**
 *
 */
public class FileSystemSyncExecutor {

    /**
     * 
     */
    public FileSystemSyncExecutor() {

    }

    /**
     * 
     * @param sourceFolder
     * @param destFolder
     * @throws IOException
     */
    public void sync(File sourceFolder, File destFolder, IProgressMonitor monitor) throws Exception {

        if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
            throw new Exception("Invalid source folder");
        }

        monitor.beginTask("Sync", 100);

        Thread.sleep(100);

        File backupFolder = new File(destFolder.getAbsolutePath() + ".bak" + System.currentTimeMillis());
        if (destFolder.exists()) {
            destFolder.renameTo(backupFolder);
        }

        try {

            sync(sourceFolder, destFolder.getParentFile(), destFolder.getName(), 1, monitor);

            if (backupFolder.exists()) {
                delete(backupFolder);
            }
            monitor.done();

        } catch (Exception ex) {

            Logger.log(Logger.ERROR, "Sync error " + sourceFolder + " > " + destFolder, ex);
        }
    }

    private void sync(File source, File destParent, String name, int depth, IProgressMonitor monitor) throws IOException {
        if (source.isDirectory()) {
            if (name == null) {
                name = source.getName();
            }
            File destFolder = new File(destParent, name);
            destFolder.mkdirs();
            for (File child : source.listFiles()) {
                sync(child, destFolder, null, depth + 1, monitor);
            }
        } else {
            syncFile(source, destParent, depth, monitor);
        }

    }

    private void syncFile(File sourceFile, File destParent, int depth, IProgressMonitor monitor) throws IOException {
        File destFile = new File(destParent, sourceFile.getName());
        byte[] buffer = new byte[100000];
        int n;
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(sourceFile);
            out = new FileOutputStream(destFile);
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    private void delete(File file) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                delete(child);
            }
        }
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("Could not delete " + file);
            }
        }
    }
}
