package com.eidosmedia.cobalt.eclipse.sdk.internal.launching;

import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.*;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;

import com.eidosmedia.cobalt.eclipse.sdk.internal.CobaltSDKImages;
import com.eidosmedia.cobalt.eclipse.sdk.internal.Logger;
import com.eidosmedia.cobalt.eclipse.sdk.internal.server.CobaltServer;
import com.eidosmedia.cobalt.eclipse.sdk.internal.util.SWTFactory;

public class CobaltMainTab extends JavaLaunchTab {

    private Text m_projectText;

    private Button m_projectButton;

    private Text m_configurationText;

    private Button m_configurationButton;

    private Text m_vmArgumentsText;

    private Button m_pgrmArgVariableButton;

    private Text m_jmxPortText;

    private Listener m_listener = new Listener();

    @Override
    public String getName() {
        return "Cobalt";
    }

    @Override
    public Image getImage() {
        return CobaltSDKImages.getImage(CobaltSDKImages.ICON_COBALT);
    }

    @Override
    public String getId() {
        return "com.eidosmedia.cobalt.eclipse.sdk.internal.launching.CobaltMainTab"; //$NON-NLS-1$
    }

    @Override
    public void createControl(Composite parent) {
        Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
        ((GridLayout) comp.getLayout()).verticalSpacing = 0;
        createProjectGroup(comp);
        createConfigurationGroup(comp);
        createVMArgumentsGroup(comp);
        createDebugGroup(comp);
        setControl(comp);
    }

    private void createProjectGroup(Composite comp) {
        Group group = SWTFactory.createGroup(comp, "Project:", 2, 1, GridData.FILL_HORIZONTAL);
        m_projectText = SWTFactory.createSingleText(group, 1);
        m_projectText.addModifyListener(m_listener);
        // ControlAccessibleListener.addListener(fProjText, group.getText());
        m_projectButton = createPushButton(group, "Browse...", null);
        m_projectButton.addSelectionListener(m_listener);
    }

    private void createConfigurationGroup(Composite comp) {
        Group group = SWTFactory.createGroup(comp, "Configuration:", 2, 1, GridData.FILL_HORIZONTAL);
        m_configurationText = SWTFactory.createSingleText(group, 1);
        m_configurationText.addModifyListener(m_listener);
        m_configurationButton = createPushButton(group, "Browse...", null);
        m_configurationButton.addSelectionListener(m_listener);
    }

    private void createVMArgumentsGroup(Composite comp) {
        Group group = SWTFactory.createGroup(comp, "VM arguments:", 2, 1, GridData.FILL_HORIZONTAL);
        m_vmArgumentsText = SWTFactory.createText(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL, 2, 0, 100, GridData.FILL_HORIZONTAL);
        m_vmArgumentsText.addModifyListener(m_listener);
        m_pgrmArgVariableButton = createPushButton(group, "Variables...", null);
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gd.horizontalSpan = 2;
        m_pgrmArgVariableButton.setLayoutData(gd);
        m_pgrmArgVariableButton.addSelectionListener(m_listener);
    }

    private void createDebugGroup(Composite comp) {
        Group group = SWTFactory.createGroup(comp, "Debug:", 2, 1, GridData.FILL_HORIZONTAL);
        SWTFactory.createLabel(group, "JMX port:", 1);
        m_jmxPortText = SWTFactory.createSingleText(group, 1);
        m_jmxPortText.addModifyListener(m_listener);
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        IJavaElement javaElement = getContext();
        if (javaElement != null) {
            IJavaProject javaProject = javaElement.getJavaProject();
            String project = javaProject.getElementName();
            configuration.setAttribute(ATTR_PROJECT_NAME, project);
        }
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            if (isServerLaunch(configuration)) {
                m_projectText.setText("");
                m_projectText.setEnabled(false);
                m_projectButton.setEnabled(false);
            } else {
                String project = configuration.getAttribute(ATTR_PROJECT_NAME, "");
                m_projectText.setText(project);
                m_projectText.setEnabled(true);
                m_projectButton.setEnabled(true);
            }
            String configurationFolder = configuration.getAttribute(CobaltLaunchConfiguration.ATTR_CONFIGURATION_FOLDER, "");
            m_configurationText.setText(configurationFolder);
            String vmArguments = configuration.getAttribute(ATTR_VM_ARGUMENTS, "");
            m_vmArgumentsText.setText(vmArguments);
            int jmxPort = configuration.getAttribute(CobaltLaunchConfiguration.ATTR_JMX_PORT, CobaltServer.DEFAULT_JMX_PORT);
            m_jmxPortText.setText(Integer.toString(jmxPort));
        } catch (CoreException ex) {
            Logger.log(Logger.ERROR, "Initialize lancher configuration main tab error", ex);
        }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        String project = valueFrom(m_projectText);
        configuration.setAttribute(ATTR_PROJECT_NAME, project);
        String configurationFolder = valueFrom(m_configurationText);
        configuration.setAttribute(CobaltLaunchConfiguration.ATTR_CONFIGURATION_FOLDER, configurationFolder);
        String vmArguments = valueFrom(m_vmArgumentsText);
        configuration.setAttribute(ATTR_VM_ARGUMENTS, vmArguments);
        int jmxPort = intValueFrom(m_jmxPortText, 9999);
        configuration.setAttribute(CobaltLaunchConfiguration.ATTR_JMX_PORT, jmxPort);
    }

    @Override
    public boolean isValid(ILaunchConfiguration launchConfig) {

        setMessage(null);
        setErrorMessage(null);

        IJavaProject project = getJavaProject();
        if (project == null || !project.exists()) {
            //
        } else if (!project.isOpen()) {
            setErrorMessage("Java project is closed");
            return false;
        } else {
            try {
                boolean libraryChecked = true; // FIXME check if it is a real Cobalt project
                //				boolean libraryChecked = false;
                //                for (IClasspathEntry e : project.getRawClasspath()) {
                //                    if (e.getPath().segment(0).equals(CobaltSDK.CLASSPATH_CONTAINER_ID.segment(0))) {
                //                        libraryChecked = true;
                //                        break;
                //                    }
                //                }
                if (!libraryChecked) {
                    setErrorMessage("Invalid java project: add Cobalt SDK Library to the project Build Path");
                    return false;
                }
            } catch (Exception ex) {
                Logger.log(Logger.ERROR, "Check Cobalt SDK Library error", ex);
                setErrorMessage("Check Cobalt SDK Library error");
                return false;
            }
        }

        if (getConfigurationFolder() == null) {
            setErrorMessage("Missing configuration folder");
            return false;
        }

        return super.isValid(launchConfig);
    }

    /**
     * Returns <code>true</code> if this {@link ILaunchConfiguration} has been
     * created from an {@link IServer}.
     * 
     * @param launchConfig
     * @return
     * @throws CoreException
     */
    private boolean isServerLaunch(ILaunchConfiguration launchConfig) throws CoreException {
        IServer server = ServerUtil.getServer(launchConfig);
        return server != null;
    }

    private IJavaProject chooseJavaProject() {
        ILabelProvider labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
        dialog.setTitle("Project selection:");
        dialog.setMessage("Select a project");
        try {
            dialog.setElements(JavaCore.create(getWorkspaceRoot()).getJavaProjects());
        } catch (JavaModelException jmex) {
            Logger.log(Logger.ERROR, jmex);
        }
        IJavaProject javaProject = getJavaProject();
        if (javaProject != null)
            dialog.setInitialSelections(new Object[] { javaProject });
        if (dialog.open() != Window.OK)
            return null;
        javaProject = (IJavaProject) dialog.getFirstResult();
        if (javaProject != null) {
            String projectName = javaProject.getElementName();
            m_projectText.setText(projectName);
        }
        return javaProject;
    }

    private IPath chooseConfigurationFolder() {
        IJavaProject javaProject = getJavaProject();
        IContainer container = null;
        if (javaProject != null)
            container = javaProject.getProject();
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(getControl().getShell(), container, true,
                "Choose a location relative to the workspace:");
        if (dialog.open() != Window.OK)
            return null;
        Object[] result = dialog.getResult();
        if (result.length == 0)
            return null;
        IPath path = (IPath) result[0];
        m_configurationText.setText("${workspace_loc:" + path.makeRelative().toString() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
        return path;
    }

    private String chooseVariable() {
        StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
        dialog.open();
        String variable = dialog.getVariableExpression();
        if (variable != null)
            m_vmArgumentsText.insert(variable);
        return variable;
    }

    /**
     * Return the IJavaProject corresponding to the project name in the project
     * name text field, or null if the text does not match a project name.
     */
    private IJavaProject getJavaProject() {
        String projectName = m_projectText.getText().trim();
        if (projectName.length() < 1) {
            return null;
        }
        return getJavaModel().getJavaProject(projectName);
    }

    private String getConfigurationFolder() {
        String folder = m_configurationText.getText();
        if (folder.isEmpty())
            return null;
        return folder;
    }

    /**
     * Convenience method to get access to the java model.
     */
    private IJavaModel getJavaModel() {
        return JavaCore.create(getWorkspaceRoot());
    }

    /**
     * Convenience method to get the workspace root.
     */
    private IWorkspaceRoot getWorkspaceRoot() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    /**
     * 
     * @param text
     * @return
     */
    private String valueFrom(Text text) {
        String content = text.getText().trim();
        if (content.length() > 0)
            return content;
        return null;
    }

    private int intValueFrom(Text text, int defaultValue) {
        String content = text.getText().trim();
        if (content.length() > 0)
            return Integer.parseInt(content);
        return defaultValue;
    }

    /**
     * 
     */
    class Listener implements ModifyListener, SelectionListener {

        @Override
        public void widgetSelected(SelectionEvent e) {
            Object source = e.getSource();
            if (source == m_projectButton) {
                chooseJavaProject();
            } else if (source == m_configurationButton) {
                chooseConfigurationFolder();
            } else if (source == m_pgrmArgVariableButton) {
                chooseVariable();
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public void modifyText(ModifyEvent e) {
            scheduleUpdateJob();
        }

    }

}
