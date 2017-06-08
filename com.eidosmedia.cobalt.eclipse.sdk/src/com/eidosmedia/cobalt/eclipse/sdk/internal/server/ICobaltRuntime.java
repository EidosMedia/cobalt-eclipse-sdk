package com.eidosmedia.cobalt.eclipse.sdk.internal.server;


public interface ICobaltRuntime {

	public String getId();

	public String getVersion();

	public String getLocationPath();

	public boolean isDevWorkspace();

	public void setDevWorkspace(boolean flag);

	public CobaltRuntimeConfigurationData getConfigurationData();
}
