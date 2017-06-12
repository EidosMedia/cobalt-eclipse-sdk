package com.eidosmedia.cobalt.eclipse.sdk.ui.internal.server.command;

import org.eclipse.wst.server.core.IServerWorkingCopy;

import com.eidosmedia.cobalt.eclipse.sdk.internal.server.CobaltServer;

public class SetEmConfPathCommand extends ServerCommand {

    protected String confPath;

    protected String oldConfPath;

    public SetEmConfPathCommand(IServerWorkingCopy server, String confPath) {
        super(server, "Set Cobalt Configuration Path");
        this.confPath = confPath;
    }

    public void execute() {
        oldConfPath = server.getAttribute(CobaltServer.ATTR_CONF_PATH, (String) null);
        if (confPath != null && !confPath.trim().isEmpty()) {
            server.setAttribute(CobaltServer.ATTR_CONF_PATH, confPath);
        } else {
            server.setAttribute(CobaltServer.ATTR_CONF_PATH, (String) null);
        }
    }

    public void undo() {
        server.setAttribute(CobaltServer.ATTR_CONF_PATH, oldConfPath);
    }
}
