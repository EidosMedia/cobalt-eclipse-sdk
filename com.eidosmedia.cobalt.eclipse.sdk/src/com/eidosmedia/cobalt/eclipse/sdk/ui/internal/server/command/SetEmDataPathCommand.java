package com.eidosmedia.cobalt.eclipse.sdk.ui.internal.server.command;

import org.eclipse.wst.server.core.IServerWorkingCopy;

import com.eidosmedia.cobalt.eclipse.sdk.internal.server.CobaltServer;

public class SetEmDataPathCommand extends ServerCommand {

    protected String dataPath;

    protected String oldDataPath;

    public SetEmDataPathCommand(IServerWorkingCopy server, String dataPath) {
        super(server, "Set Cobalt Data Path");
        this.dataPath = dataPath;
    }

    public void execute() {
        oldDataPath = server.getAttribute(CobaltServer.ATTR_DATA_PATH, (String) null);
        if (dataPath != null && !dataPath.trim().isEmpty()) {
            server.setAttribute(CobaltServer.ATTR_DATA_PATH, dataPath);
        } else {
            server.setAttribute(CobaltServer.ATTR_DATA_PATH, (String) null);
        }
    }

    public void undo() {
        server.setAttribute(CobaltServer.ATTR_DATA_PATH, oldDataPath);
    }
}
