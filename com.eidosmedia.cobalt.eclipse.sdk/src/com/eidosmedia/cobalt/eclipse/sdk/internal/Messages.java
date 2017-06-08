package com.eidosmedia.cobalt.eclipse.sdk.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.eidosmedia.cobalt.eclipse.sdk.internal.Messages"; //$NON-NLS-1$

    public static String DefaultTitle;

    public static String PreferencePageTitle;

    public static String PreferencePageDescription;

    public static String ClasspathContainerDesc;

    public static String ClasspathContainerPageName;

    public static String ClasspathContainerPageTitle;

    public static String ClasspathContainerPageDescription;

    public static String WebClasspathContainerDesc;

    public static String WebClasspathContainerPageName;

    public static String WebClasspathContainerPageTitle;

    public static String WebClasspathContainerPageDescription;

    public static String InvalidContainer;

    public static String CobaltDirLabel;

    public static String VmArgsLabel;

    public static String InvalidateAllAction_Error;

    public static String NewConfigurationFolderAction_FolderAlreadyExists;

    public static String NewConfigurationFolderAction_FolderCreationError;

    public static String NewConfigurationFolderAction_CobaltSDKNotConfigured;

    public static String NewConfigurationFolderAction_Success;

    public static String CobaltRuntimeWizardComposite_lblDevelopingWorkspace_text;

    public static String CobaltRuntimeWizardComposite_btnDevWorkspace_text;

    public static String CobaltServer;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
