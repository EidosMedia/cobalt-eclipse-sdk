package com.eidosmedia.cobalt.eclipse.sdk.ui.internal.server.command;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServerWorkingCopy;

/**
 * A command on a server.
 */
public abstract class ServerCommand extends AbstractOperation {

    protected IServerWorkingCopy server;

    /**
     * ServerCommand constructor.
     * 
     * @param server
     *            a server
     * @param name
     *            a label
     */
    public ServerCommand(IServerWorkingCopy server, String name) {
        super(name);
        this.server = server;
    }

    public abstract void execute();

    public IStatus execute(IProgressMonitor monitor, IAdaptable adapt) {
        execute();
        return Status.OK_STATUS;
    }

    public abstract void undo();

    public IStatus undo(IProgressMonitor monitor, IAdaptable adapt) {
        undo();
        return Status.OK_STATUS;
    }

    public IStatus redo(IProgressMonitor monitor, IAdaptable adapt) {
        return execute(monitor, adapt);
    }
}
