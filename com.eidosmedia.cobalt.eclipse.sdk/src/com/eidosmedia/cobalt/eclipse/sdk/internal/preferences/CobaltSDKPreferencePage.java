package com.eidosmedia.cobalt.eclipse.sdk.internal.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.eidosmedia.cobalt.eclipse.sdk.internal.CobaltSDKPlugin;
import com.eidosmedia.cobalt.eclipse.sdk.internal.Messages;

/**
 * 
 */
public class CobaltSDKPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private DirectoryFieldEditor portalPathField;

	private StringFieldEditor vmArgsField;

	public CobaltSDKPreferencePage() {
		super(GRID);
		setPreferenceStore(CobaltSDKPlugin.getDefault().getPreferenceStore());
		//setDescription(Messages.PreferencePageTitle);
	}

	public void createFieldEditors() {
		portalPathField = new DirectoryFieldEditor(PreferenceConstants.P_PORTAL_PATH, Messages.CobaltDirLabel, getFieldEditorParent());
		addField(portalPathField);
		vmArgsField = new StringFieldEditor(PreferenceConstants.P_VM_ARGS, Messages.VmArgsLabel, getFieldEditorParent());
		vmArgsField.setEnabled(false, getFieldEditorParent());
		addField(vmArgsField);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	public boolean isValid() {
		/*
		if (portalPathField != null) {
			String value = portalPathField.getStringValue();
			if (value != null) {
				value = value.trim();
				if (!value.isEmpty()) {
					File file = new File(value);
					return file.exists();
				}
			}
		}
		return false;
		*/
		return true;
	}
}