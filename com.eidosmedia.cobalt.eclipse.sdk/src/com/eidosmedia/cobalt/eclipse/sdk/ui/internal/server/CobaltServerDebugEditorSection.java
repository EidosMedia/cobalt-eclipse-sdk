package com.eidosmedia.cobalt.eclipse.sdk.ui.internal.server;

import static org.eclipse.ui.forms.widgets.ExpandableComposite.*;
import static org.eclipse.ui.forms.widgets.Section.*;

import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

import com.eidosmedia.cobalt.eclipse.sdk.internal.server.CobaltServer;
import com.eidosmedia.cobalt.eclipse.sdk.ui.internal.server.command.ServerCommand;
import com.eidosmedia.cobalt.eclipse.sdk.ui.internal.server.command.SetBaseFolderCommand;
import com.eidosmedia.cobalt.eclipse.sdk.ui.internal.server.command.SetJMXPortCommand;

public class CobaltServerDebugEditorSection extends ServerEditorSection {

    private CobaltServer cobaltServer;

    private Text txtBaseFolder;

    private Text txtJMXPort;

    protected boolean fUpdating;

    @Override
    public void init(IEditorSite site, IEditorInput input) {
        super.init(site, input);

        if (server != null) {
            cobaltServer = (CobaltServer) server.loadAdapter(CobaltServer.class, null);
            // TODO addChangeListener();
        }
        initialize();
    }

    @Override
    public void createSection(Composite parent) {
        super.createSection(parent);

        FormToolkit toolkit = getFormToolkit(parent.getDisplay());

        final Section section = toolkit.createSection(parent, TWISTIE | TITLE_BAR | DESCRIPTION | FOCUS_TITLE);
        section.setText("Cobalt");
        section.setDescription("Specify Cobalt debug settings.");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
        section.setExpanded(true);

        final Composite composite = toolkit.createComposite(section);
        final GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginHeight = 5;
        layout.marginWidth = 10;
        layout.verticalSpacing = 5;
        layout.horizontalSpacing = 5;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
        // IWorkbenchHelpSystem whs = PlatformUI.getWorkbench().getHelpSystem();
        // whs.setHelp(composite, ContextIds.SERVER_EDITOR);
        // toolkit.paintBordersFor(composite);
        section.setClient(composite);

        int decorationWidth = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();

        // base folder

        toolkit.createLabel(composite, "Base folder:");

        txtBaseFolder = toolkit.createText(composite, "");
        {
            txtBaseFolder.setMessage("temporary workspace folder (.metadata)");
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalIndent = decorationWidth;
            data.widthHint = 75;
            txtBaseFolder.setLayoutData(data);
            txtBaseFolder.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    executeUpdateOperation(new SetBaseFolderCommand(server, txtBaseFolder.getText()));
                }
            });
        }

        Button browseButton = toolkit.createButton(composite, "Browse...", SWT.PUSH);
        browseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        browseButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent se) {
                DirectoryDialog dialog = new DirectoryDialog(composite.getShell());
                dialog.setMessage("Select portal folder");
                dialog.setFilterPath(txtBaseFolder.getText());
                String selectedDirectory = dialog.open();
                if (selectedDirectory != null) {
                    txtBaseFolder.setText(selectedDirectory);
                }
            }
        });

        // JMX port

        toolkit.createLabel(composite, "JMX connection port:");

        txtJMXPort = toolkit.createText(composite, "");
        {
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 2;
            data.horizontalIndent = decorationWidth;
            txtJMXPort.setLayoutData(data);
        }
        txtJMXPort.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                try {
                    String value = txtJMXPort.getText();
                    int port = Integer.parseInt(value);
                    executeUpdateOperation(new SetJMXPortCommand(server, port));
                } catch (Exception ex) {
                    // FIXME log error
                    ex.printStackTrace();
                    setErrorMessage(ex.toString());
                }
            }
        });

        //

        initialize();
    }

    /**
     * 
     */
    protected void initialize() {
        if (txtBaseFolder == null) {
            return;
        }
        fUpdating = true;
        try {
            txtBaseFolder.setText(cobaltServer.getBaseFolder());
            txtJMXPort.setText(Integer.toString(cobaltServer.getJMXPort()));
        } finally {
            fUpdating = false;
        }
    }

    /**
     * 
     * @param serverCommand
     */
    protected void executeUpdateOperation(ServerCommand serverCommand) {
        if (!fUpdating) {
            fUpdating = true;
            execute(serverCommand);
            fUpdating = false;
        }
    }

}
