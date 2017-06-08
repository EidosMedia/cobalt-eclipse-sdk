package com.eidosmedia.cobalt.eclipse.sdk.internal.actions;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.wst.server.core.IServer;

import com.eidosmedia.cobalt.eclipse.sdk.internal.CobaltSDKPlugin;
import com.eidosmedia.cobalt.eclipse.sdk.internal.Logger;
import com.eidosmedia.cobalt.eclipse.sdk.internal.Messages;
import com.eidosmedia.cobalt.eclipse.sdk.internal.server.CobaltServer;

/**
 * FIXME refactor this action in "Create sample DB"
 */
public class InvalidateAllAction implements IObjectActionDelegate {

    private IStructuredSelection m_selection;

    public InvalidateAllAction() {
    }

    @Override
    public void run(IAction action) {
        try {

            IServer server = (IServer) m_selection.getFirstElement();
            CobaltServer cobaltServer = (CobaltServer) server.getAdapter(CobaltServer.class);
            int jmxPort = cobaltServer.getJMXPort();

            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + jmxPort + "/jmxrmi");
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
            MBeanServerConnection serverConnection = jmxConnector.getMBeanServerConnection();
            ObjectName mbeanName = new ObjectName("EOMCache:name=Manager");
            serverConnection.invoke(mbeanName, "invalidateAll", null, null);
            // MyConfMBean mbeanProxy = JMX.newMBeanProxy(mbsc, mbeanName, MyConfMBean.class, true);

        } catch (Exception ex) {
            Logger.log(IStatus.ERROR, Messages.InvalidateAllAction_Error, ex);
            CobaltSDKPlugin.warning(Messages.DefaultTitle, Messages.InvalidateAllAction_Error);
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {

        m_selection = (IStructuredSelection) selection;
        if (!m_selection.isEmpty()) {
            IServer server = (IServer) m_selection.getFirstElement();
            if (server.getServerState() == IServer.STATE_STARTED) {
                action.setEnabled(true);
                return;
            }
        }

        m_selection = null;
        action.setEnabled(false);
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

}
