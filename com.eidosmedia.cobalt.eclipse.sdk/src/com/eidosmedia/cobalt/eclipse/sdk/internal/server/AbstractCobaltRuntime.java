package com.eidosmedia.cobalt.eclipse.sdk.internal.server;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.model.RuntimeDelegate;

public abstract class AbstractCobaltRuntime extends RuntimeDelegate implements ICobaltRuntime {

    public static final String ID_PREFIX = "com.eidosmedia.cobalt.eclipse.sdk.runtime.";

    public static final String DEV_WORKSPACE = "com.eidosmedia.cobalt.eclipse.sdk.internal.server.CobaltRuntime.DEV_WORKSPACE";

    @Override
    public boolean isDevWorkspace() {
        return this.getAttribute(DEV_WORKSPACE, false);
    }

    @Override
    public void setDevWorkspace(boolean flag) {
        this.setAttribute(DEV_WORKSPACE, flag);
    }

    @Override
    public String getLocationPath() {
        IRuntime runtime = getRuntime();
        IPath location = runtime.getLocation();
        if (location != null) {
            String path = location.toOSString();
            return path;
        }
        return null;
    }

    public static final String getRuntimeIdFromVersion(String version) {
        if (version == null || version.equals("1.0")) {
            return CobaltRuntime10.ID;
        } else {
            throw new IllegalArgumentException("version: " + version);
        }
    }
}
