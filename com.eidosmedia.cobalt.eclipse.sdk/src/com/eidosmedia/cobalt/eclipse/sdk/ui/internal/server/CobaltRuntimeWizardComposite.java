package com.eidosmedia.cobalt.eclipse.sdk.ui.internal.server;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;

import com.eidosmedia.cobalt.eclipse.sdk.internal.Messages;
import com.eidosmedia.cobalt.eclipse.sdk.internal.CobaltSDKPlugin;
import com.eidosmedia.cobalt.eclipse.sdk.internal.server.ICobaltRuntime;

public class CobaltRuntimeWizardComposite extends Composite {

	private IWizardHandle m_wizard;
	private IRuntimeWorkingCopy m_runtime;
	private ICobaltRuntime m_portalRuntime;
	private Text m_txtPortalDir;
	private Text m_txtName;
	private Button m_btnDevWorkspace;

	protected CobaltRuntimeWizardComposite(Composite parent, IWizardHandle wizard) {
		super(parent, SWT.NONE);
		m_wizard = wizard;

		m_wizard.setTitle(Messages.CobaltServer);
		m_wizard.setDescription("Specify the Cobalt SDK installation directory");

		setLayout(new GridLayout(2, false));

		Label lblName = new Label(this, SWT.NONE);
		lblName.setText("Name:");
		new Label(this, SWT.NONE);

		m_txtName = new Text(this, SWT.BORDER);
		m_txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		m_txtName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				m_runtime.setName(m_txtName.getText());
				validate();
			}
		});
		new Label(this, SWT.NONE);
		
		Label lblPortalDir = new Label(this, SWT.NONE);
		lblPortalDir.setText(Messages.CobaltDirLabel);
		new Label(this, SWT.NONE);

		m_txtPortalDir = new Text(this, SWT.BORDER);
		m_txtPortalDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		m_txtPortalDir.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				m_runtime.setLocation(new Path(m_txtPortalDir.getText()));
				validate();
			}
		});

		Button browseButton = new Button(this, SWT.NONE);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				DirectoryDialog dialog = new DirectoryDialog(CobaltRuntimeWizardComposite.this.getShell());
				dialog.setMessage("Select portal folder");
				dialog.setFilterPath(m_txtPortalDir.getText());
				String selectedDirectory = dialog.open();
				if (selectedDirectory != null)
					m_txtPortalDir.setText(selectedDirectory);
			}
		});
		
		if (CobaltSDKPlugin.isDebugFeaturesEnable()) {

			new Label(this, SWT.NONE);
			new Label(this, SWT.NONE);
			
			m_btnDevWorkspace = new Button(this, SWT.CHECK);
			m_btnDevWorkspace.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean selected = m_btnDevWorkspace.getSelection();
					m_portalRuntime.setDevWorkspace(selected);
				}
			});
			m_btnDevWorkspace.setText(Messages.CobaltRuntimeWizardComposite_btnDevWorkspace_text);
			new Label(this, SWT.NONE);
		}
	}

	public void setRuntime(IRuntimeWorkingCopy runtimeWC) {

		m_runtime = runtimeWC;
		m_portalRuntime = (ICobaltRuntime) m_runtime.loadAdapter(ICobaltRuntime.class, null);

		String name = runtimeWC.getName();
		if (name != null)
			m_txtName.setText(name);

		IPath location = runtimeWC.getLocation();
		if (location != null)
			m_txtPortalDir.setText(location.toOSString());

		if (m_btnDevWorkspace != null) {
			boolean isDevWorkspace = m_portalRuntime.isDevWorkspace();
			m_btnDevWorkspace.setSelection(isDevWorkspace);
		}
		
		// init();
		// validate();
	}

	protected void validate() {
		if (m_runtime == null) {
			m_wizard.setMessage("", IMessageProvider.ERROR);
			return;
		}

		IStatus status = m_runtime.validate(null);
		if (status == null || status.isOK())
			m_wizard.setMessage(null, IMessageProvider.NONE);
		else if (status.getSeverity() == IStatus.WARNING)
			m_wizard.setMessage(status.getMessage(), IMessageProvider.WARNING);
		else
			m_wizard.setMessage(status.getMessage(), IMessageProvider.ERROR);
		m_wizard.update();
	}
}
