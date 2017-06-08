package com.eidosmedia.cobalt.eclipse.sdk.ui.internal.server.command;

import org.eclipse.wst.server.core.IServerWorkingCopy;

import com.eidosmedia.cobalt.eclipse.sdk.internal.server.CobaltServer;

public class SetBaseFolderCommand extends ServerCommand {

    protected String baseFolder;

    protected String oldBaseFolder;

    public SetBaseFolderCommand(IServerWorkingCopy server, String baseFolder) {
        super(server, "Set Cobalt base folder");
        this.baseFolder = baseFolder;
    }

    public void execute() {
        oldBaseFolder = server.getAttribute(CobaltServer.ATTR_BASE_FOLDER, (String) null);
        server.setAttribute(CobaltServer.ATTR_BASE_FOLDER, baseFolder);
    }

    public void undo() {
        server.setAttribute(CobaltServer.ATTR_BASE_FOLDER, oldBaseFolder);
    }
}
