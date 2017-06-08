package com.eidosmedia.cobalt.eclipse.sdk.ui.internal.server;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.PublishOperation;
import org.eclipse.wst.server.core.model.PublishTaskDelegate;

import com.eidosmedia.cobalt.eclipse.sdk.internal.server.CobaltServerBehaviour;

/**
 *
 */
public class PublishTask extends PublishTaskDelegate {
    
    /**
     * 
     */
	public PublishOperation[] getTasks(IServer server, int kind, List modules, List kindList) {
		if (modules == null)
			return null;
		
		CobaltServerBehaviour cobaltServer = (CobaltServerBehaviour) server.loadAdapter(CobaltServerBehaviour.class, null);
		// if (!tomcatServer.getTomcatServer().isTestEnvironment())
		//	return null;
		
		List<PublishOperation> tasks = new ArrayList<PublishOperation>();
		int size = modules.size();
		for (int i = 0; i < size; i++) {
			IModule[] module = (IModule[]) modules.get(i);
			Integer in = (Integer) kindList.get(i);
			//tasks.add(new PublishOperation2(cobaltServer, kind, module, in.intValue()));
		}
		
		return tasks.toArray(new PublishOperation[tasks.size()]);
	}
}