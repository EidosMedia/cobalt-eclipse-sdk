package com.eidosmedia.cobalt.eclipse.sdk.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.eidosmedia.cobalt.eclipse.sdk.internal.CobaltSDKPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = CobaltSDKPlugin.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_PORTAL_PATH, "");
		store.setDefault(PreferenceConstants.P_VM_ARGS, "");
	}

}
