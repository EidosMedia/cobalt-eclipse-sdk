package com.eidosmedia.cobalt.eclipse.sdk.ui.internal.server.command;

import org.eclipse.wst.server.core.IServerWorkingCopy;

import com.eidosmedia.cobalt.eclipse.sdk.internal.server.CobaltServer;

public class SetJMXPortCommand extends ServerCommand {

    protected int jmxPort;

    protected int oldJmxPort;

    public SetJMXPortCommand(IServerWorkingCopy server, int jmxPort) {
        super(server, "Set Cobalt JMX port");
        this.jmxPort = jmxPort;
    }

    public void execute() {
        oldJmxPort = server.getAttribute(CobaltServer.ATTR_JMX_PORT, 0); // FIXME default port
        server.setAttribute(CobaltServer.ATTR_JMX_PORT, jmxPort);
    }

    public void undo() {
        server.setAttribute(CobaltServer.ATTR_JMX_PORT, oldJmxPort);
    }
}
