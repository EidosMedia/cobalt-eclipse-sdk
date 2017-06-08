package com.eidosmedia.cobalt.eclipse.sdk.internal.server;

public abstract class CobaltRuntimeConfigurationData {

    protected final String m_runtimePath;

    public CobaltRuntimeConfigurationData(String runtimePath) {
        m_runtimePath = runtimePath;
    }

    public String getCatalinaBasePath() {
        return m_runtimePath;
    }

}
