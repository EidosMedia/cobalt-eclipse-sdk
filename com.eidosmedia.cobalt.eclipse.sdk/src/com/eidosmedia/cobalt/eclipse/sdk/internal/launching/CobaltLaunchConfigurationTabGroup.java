package com.eidosmedia.cobalt.eclipse.sdk.internal.launching;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;

public class CobaltLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	public CobaltLaunchConfigurationTabGroup() {
	}

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		// new ServerLaunchConfigurationTab(),
		// new JavaArgumentsTab(),
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new CobaltMainTab(),
				new JavaClasspathTab(),
				new SourceLookupTab(),
				new EnvironmentTab(),
				new CommonTab() };
		setTabs(tabs);
	}

}
