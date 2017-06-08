package com.eidosmedia.cobalt.eclipse.sdk.internal.server;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;

import com.eidosmedia.cobalt.eclipse.sdk.internal.CobaltSDKPlugin;

public class CobaltRuntime10 extends AbstractCobaltRuntime {

    public static final String ID = ID_PREFIX + "10";

    public static final String VERSION = "1.0";

    public CobaltRuntime10() {
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public IStatus validate() {
        IPath location = getRuntime().getLocation();
        if (location != null) {
            File portalFolder = location.append("lib").append("modules").toFile();
            if (!portalFolder.exists()) {
                return CobaltSDKPlugin.createStatus(IStatus.ERROR, "Invalid Cobalt 1.0 runtime", null);
            }
            // TODO check better Cobalt SDK
        }
        return super.validate();
    }

    @Override
    public CobaltRuntimeConfigurationData getConfigurationData() {
        String path = getLocationPath();
        if (path == null || isDevWorkspace())
            return null; // TODO or return IllegalStateException?
        return new CobaltRuntimeConfigurationData10(path);
    }

    @Override
    protected void initialize() {
        super.initialize();
    }
}
