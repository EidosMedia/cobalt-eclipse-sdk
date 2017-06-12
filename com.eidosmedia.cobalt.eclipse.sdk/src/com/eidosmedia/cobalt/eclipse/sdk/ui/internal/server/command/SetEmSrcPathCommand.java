package com.eidosmedia.cobalt.eclipse.sdk.ui.internal.server.command;

import org.eclipse.wst.server.core.IServerWorkingCopy;

import com.eidosmedia.cobalt.eclipse.sdk.internal.server.CobaltServer;

public class SetEmSrcPathCommand extends ServerCommand {

    protected String srcPath;

    protected String oldSrcPath;

    public SetEmSrcPathCommand(IServerWorkingCopy server, String srcPath) {
        super(server, "Set Cobalt Source Path");
        this.srcPath = srcPath;
    }

    public void execute() {
        oldSrcPath = server.getAttribute(CobaltServer.ATTR_SRC_PATH, (String) null);
        if (srcPath != null && !srcPath.trim().isEmpty()) {
            server.setAttribute(CobaltServer.ATTR_SRC_PATH, srcPath);
        } else {
            server.setAttribute(CobaltServer.ATTR_SRC_PATH, (String) null);
        }
    }

    public void undo() {
        server.setAttribute(CobaltServer.ATTR_SRC_PATH, oldSrcPath);
    }
}
