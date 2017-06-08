package com.eidosmedia.cobalt.eclipse.sdk.internal;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.osgi.framework.BundleContext;

import com.eidosmedia.cobalt.eclipse.sdk.internal.server.AbstractCobaltRuntime;
import com.eidosmedia.cobalt.eclipse.sdk.internal.server.CobaltRuntimeLifecycleListener;
import com.eidosmedia.cobalt.eclipse.sdk.internal.server.ICobaltRuntime;

/**
 * The activator class controls the plug-in life cycle
 */
public class CobaltSDKPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.eidosmedia.cobalt.eclipse.sdk"; //$NON-NLS-1$

    // The shared instance
    private static CobaltSDKPlugin plugin;

    private boolean m_debugFeaturesEnable;

    private CobaltRuntimeLifecycleListener m_runtimeListener;

    /**
     * 
     */
    public CobaltSDKPlugin() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
     * BundleContext
     * )
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        // Check if debug features are active
        String userHome = System.getProperty("user.home"); //$NON-NLS-1$
        File debugFeaturesFile = new File(userHome, ".com.eidosmedia.cobalt.eclipse.ide.debug"); //$NON-NLS-1$
        m_debugFeaturesEnable = debugFeaturesFile.exists();

        m_runtimeListener = new CobaltRuntimeLifecycleListener();
        ServerCore.addRuntimeLifecycleListener(m_runtimeListener);
        // TODO add file system monitor
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
     * BundleContext
     * )
     */
    public void stop(BundleContext context) throws Exception {
        ServerCore.removeRuntimeLifecycleListener(m_runtimeListener);
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static final CobaltSDKPlugin getDefault() {
        return plugin;
    }

    /**
     * 
     * @return
     */
    public static final boolean isDebugFeaturesEnable() {
        return getDefault().m_debugFeaturesEnable;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path
     * 
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static final ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * Returns the currently active workbench window shell or <code>null</code>
     * if none.
     * 
     * @return the currently active workbench window shell or <code>null</code>
     */
    public static final Shell getShell() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
            if (windows.length > 0) {
                return windows[0].getShell();
            }
        } else {
            return window.getShell();
        }
        return null;
    }

    /**
     * Returns the standard display to be used. The method first checks, if the
     * thread calling this method has an associated display. If so, this display
     * is returned. Otherwise the method returns the default display.
     */
    public static final Display getStandardDisplay() {
        Display display;
        display = Display.getCurrent();
        if (display == null)
            display = Display.getDefault();
        return display;
    }

    /**
     * Returns the active workbench window
     * 
     * @return the active workbench window
     */
    public static final IWorkbenchWindow getActiveWorkbenchWindow() {
        return getDefault().getWorkbench().getActiveWorkbenchWindow();
    }

    /**
     * 
     * @return
     */
    public static final IWorkbenchPage getActivePage() {
        IWorkbenchWindow w = getActiveWorkbenchWindow();
        if (w != null) {
            return w.getActivePage();
        }
        return null;
    }

    /**
     * 
     * @param title
     * @param message
     */
    public static final void warning(final String title, final String message) {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                MessageDialog.openWarning(CobaltSDKPlugin.getShell(), title, message);
            }
        });
    }

    /**
     * 
     * @param severity
     * @param message
     * @param exception
     * @return
     */
    public static final IStatus createStatus(int severity, String message, Throwable exception) {
        return new Status(severity, PLUGIN_ID, message, exception);
    }

    /**
     * 
     * @return
     */
    public static final IRuntime[] getCobaltRuntimes() {
        IRuntime[] runtimes = ServerCore.getRuntimes();
        if (runtimes == null)
            return new IRuntime[0];
        ArrayList<IRuntime> list = new ArrayList<IRuntime>();
        for (IRuntime runtime : runtimes) {
            IRuntimeType type = runtime.getRuntimeType();
            if (type.getId() != null && type.getId().startsWith(AbstractCobaltRuntime.ID_PREFIX))
                list.add(runtime);
        }
        return list.toArray(new IRuntime[list.size()]);
    }

    /**
     * 
     * @return
     */
    public static final ICobaltRuntime getDefaultRuntime() {
        // TODO get default runtime from configuration
        ICobaltRuntime defaultRuntime = null;
        IRuntime[] runtimes = getCobaltRuntimes();
        for (IRuntime r : runtimes) {
            ICobaltRuntime cobaltRuntime = (ICobaltRuntime) r.loadAdapter(ICobaltRuntime.class, null);
            defaultRuntime = cobaltRuntime;
            break;
            // FIXME get the favorite runtime
        }
        return defaultRuntime;
    }

}
