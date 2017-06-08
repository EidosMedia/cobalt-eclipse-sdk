package com.eidosmedia.cobalt.eclipse.sdk.ui.internal.server;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;

public class CobaltRuntimeWizardFragment extends WizardFragment {

    private CobaltRuntimeWizardComposite m_composite;

    @Override
    public boolean isComplete() {
        IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
        if (runtime == null)
            return false;
        IStatus status = runtime.validate(null);
        return (status == null || status.getSeverity() != IStatus.ERROR);
    }

    public CobaltRuntimeWizardFragment() {
    }

    @Override
    public void enter() {
        if (m_composite != null) {
            IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
            m_composite.setRuntime(runtime);
        }
    }

    @Override
    public void exit() {
        IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
        IPath path = runtime.getLocation();
        // if (runtime.validate(null).getSeverity() != IStatus.ERROR)
        // TomcatPlugin.setPreference("location" + runtime.getRuntimeType().getId(), path.toString());
    }

    @Override
    public boolean hasComposite() {
        return true;
    }

    @Override
    public Composite createComposite(Composite parent, IWizardHandle wizard) {
        m_composite = new CobaltRuntimeWizardComposite(parent, wizard);
        return m_composite;
    }

    @Override
    protected void createChildFragments(List<WizardFragment> list) {
        super.createChildFragments(list);
    }

    @Override
    public List getChildFragments() {
        return super.getChildFragments();
    }

    @Override
    public void performFinish(IProgressMonitor monitor) throws CoreException {
        super.performFinish(monitor);
    }

    @Override
    protected void setComplete(boolean complete) {
        super.setComplete(complete);
    }

    @Override
    public TaskModel getTaskModel() {
        return super.getTaskModel();
    }
}
