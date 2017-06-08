package com.eidosmedia.cobalt.eclipse.sdk.internal.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

import com.eidosmedia.cobalt.eclipse.sdk.internal.CobaltSDKPlugin;
import com.eidosmedia.cobalt.eclipse.sdk.internal.Logger;
import com.eidosmedia.cobalt.eclipse.sdk.internal.Messages;
import com.eidosmedia.cobalt.eclipse.sdk.internal.server.ICobaltRuntime;
import com.eidosmedia.cobalt.eclipse.sdk.internal.util.FileUtils;

public class NewConfigurationFolderAction implements IObjectActionDelegate {

    private static final String CONFIGURATION_FOLDER_NAME = "conf"; //$NON-NLS-1$

    private Shell shell;

    public NewConfigurationFolderAction() {
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        shell = targetPart.getSite().getShell();
    }

    public void run(IAction action) {

        IStructuredSelection selection = getCurrentSelection();
        if (selection == null || selection.size() != 1)
            return;

        ICobaltRuntime runtime = CobaltSDKPlugin.getDefaultRuntime();
        if (runtime == null) {
            throw new IllegalStateException("Cobalt SDK Runtime not configured");
        }

        String runtimePath = runtime.getLocationPath();
        if (runtimePath == null || runtimePath.isEmpty()) {
            MessageDialog.openWarning(shell, Messages.DefaultTitle, Messages.NewConfigurationFolderAction_CobaltSDKNotConfigured);
            return;
        }

        IAdaptable adaptable = (IAdaptable) selection.getFirstElement();
        IProject project = (IProject) adaptable.getAdapter(IProject.class);

        if (project.findMember(CONFIGURATION_FOLDER_NAME) != null) {
            MessageDialog.openWarning(shell, Messages.DefaultTitle, Messages.NewConfigurationFolderAction_FolderAlreadyExists);
            return;
        }

        IFolder configFolder = null;
        try {

            configFolder = project.getFolder(CONFIGURATION_FOLDER_NAME);
            configFolder.create(true, true, null);
            copyConfigurationFolder(runtimePath, configFolder);

        } catch (Exception ex) {

            // Rollback
            try {
                if (configFolder != null && configFolder.exists()) {
                    configFolder.delete(true, null);
                }
            } catch (Exception rbex) {
            }

            // Log and display error
            String message = Messages.NewConfigurationFolderAction_FolderCreationError;
            Logger.log(Logger.ERROR, message, ex);
            MessageDialog.openError(shell, Messages.DefaultTitle, message + "\n" + ex); //$NON-NLS-1$
            return;
        }

        MessageDialog.openInformation(shell, Messages.DefaultTitle, Messages.NewConfigurationFolderAction_Success);
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

    /**
     * Returns the currently selected item(s) from the current workbench page or
     * <code>null</code> if the current active page could not be resolved.
     * 
     * @return the currently selected item(s) or <code>null</code>
     */
    protected IStructuredSelection getCurrentSelection() {
        IWorkbenchPage page = CobaltSDKPlugin.getActivePage();
        if (page != null) {
            ISelection selection = page.getSelection();
            if (selection instanceof IStructuredSelection) {
                return (IStructuredSelection) selection;
            }
        }
        return null;
    }

    /**
     * 
     * @param runtimePath
     * @param destinationFolder
     * @throws CoreException
     * @throws IOException
     */
    public static final void copyConfigurationFolder(String runtimePath, IFolder destinationFolder) throws CoreException, IOException {
        File file = new File(runtimePath, "conf"); //$NON-NLS-1$
        copy(file, destinationFolder, true);
        // FIXME editConfigurationFiles(runtimePath, destinationFolder);
    }

    /**
     * 
     * @param runtimePath
     * @param destinationFolder
     * @throws CoreException
     * @throws IOException
     */
    public static final void copySrcFolder(String runtimePath, IFolder destinationFolder) throws CoreException, IOException {
        File file = new File(runtimePath, "src"); //$NON-NLS-1$
        copy(file, destinationFolder, true);
        // FIXME editConfigurationFiles(runtimePath, destinationFolder);
    }

    /**
     * 
     * @param source
     * @param destination
     * @param createFolder
     * @throws CoreException
     * @throws FileNotFoundException
     */
    private static final void copy(File source, IFolder destination, boolean createFolder) throws CoreException, FileNotFoundException {
        String name = source.getName();
        if (source.isDirectory()) {
            IFolder subFolder = destination;
            if (createFolder) {
                subFolder = destination.getFolder(name);
                if (!subFolder.exists())
                    subFolder.create(true, true, null);
            }
            for (File child : source.listFiles())
                copy(child, subFolder, true);
        } else {
            IFile file = destination.getFile(name);
            if (!file.exists())
                file.create(new FileInputStream(source), true, null);
        }
    }

    /**
     * FIXME editConfigurationFiles
     * @param runtimePath
     * @param configFolder
     * @throws CoreException
     * @throws IOException
     */
    private static final void editConfigurationFiles(String runtimePath, IFolder configFolder) throws CoreException, IOException {

        IFile globalsDtd = configFolder.getFile("globals.dtd"); //$NON-NLS-1$
        if (!globalsDtd.exists())
            return;
        // set LOG_BASE_FOLDER in globals.dtd
        editGlobalsDtd(runtimePath, globalsDtd);

        // set DOCTYPE in tomcat server.xml
        IFile serverXml = configFolder.getFile("server.xml"); //$NON-NLS-1$
        if (serverXml.exists()) {
            String globalsDtdPath = globalsDtd.getLocation().toFile().getCanonicalPath();
            editServerXml(serverXml, globalsDtdPath);
        }
    }

    /**
     * 
     * @param runtimePath
     * @param globalsDtd
     * @throws CoreException
     * @throws IOException
     */
    private static final void editGlobalsDtd(String runtimePath, IFile globalsDtd) throws CoreException, IOException {

        String logFolder = new File(runtimePath, "temp/logs").getAbsolutePath(); //$NON-NLS-1$

        String content = FileUtils.readContent(globalsDtd);

        StringBuilder replacement = new StringBuilder("<!ENTITY LOG_BASE_FOLDER \""); //$NON-NLS-1$
        replacement.append(logFolder);
        replacement.append("\">"); //$NON-NLS-1$
        content = content.replace("<!ENTITY LOG_BASE_FOLDER \"/temp/logs/portal\">", replacement.toString()); //$NON-NLS-1$

        FileUtils.writeContent(globalsDtd, content);
    }

    /**
     * 
     * @param serverXml
     * @param globalsDtdPath
     * @throws CoreException
     * @throws IOException
     */
    private static void editServerXml(IFile serverXml, String globalsDtdPath) throws CoreException, IOException {

        String content = FileUtils.readContent(serverXml);

        StringBuilder replacement = new StringBuilder("<!DOCTYPE Server SYSTEM \""); //$NON-NLS-1$
        replacement.append("file:"); //$NON-NLS-1$
        replacement.append(globalsDtdPath);
        replacement.append("\">"); //$NON-NLS-1$
        content = content.replace("<!DOCTYPE Server SYSTEM \"globals.dtd\">", replacement.toString()); //$NON-NLS-1$

        FileUtils.writeContent(serverXml, content);
    }
}
