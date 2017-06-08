package com.eidosmedia.cobalt.eclipse.sdk.internal.server;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;

import com.eidosmedia.cobalt.eclipse.sdk.internal.Logger;

/**
 * 
 */
public class CobaltRuntimeLifecycleListener implements IRuntimeLifecycleListener {

	@Override
	public void runtimeAdded(IRuntime runtime) {
		refreshProjectsClasspath();
	}

	@Override
	public void runtimeChanged(IRuntime runtime) {
		refreshProjectsClasspath();
	}

	@Override
	public void runtimeRemoved(IRuntime runtime) {
		refreshProjectsClasspath();
	}

	/**
	 * Refresh projects classpath if they use a {@link CobaltClasspathContainer}
	 * .
	 */
	private void refreshProjectsClasspath() {
		try {
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			IJavaModel model = JavaCore.create(workspaceRoot);
			IJavaProject[] projects = model.getJavaProjects();
			for (IJavaProject project : projects) {
				IClasspathEntry[] entries = project.getRawClasspath();
				for (IClasspathEntry entry : entries) {
					if (IClasspathEntry.CPE_CONTAINER == entry.getEntryKind()) {
						IPath path = entry.getPath();
						// FIXME refreshProjectsClasspath
//						if (CobaltSDK.CLASSPATH_CONTAINER_ID_PREFIX.equals(path.segment(0))) {
//							CobaltClasspathContainerInitializer.setPortalClasspathContainer(path, project);
//							break;
//						}
					}
				}
			}
		} catch (Exception ex) {
			Logger.log(IStatus.ERROR, "Refresh projects classpath error", ex);
		}
	}

}
