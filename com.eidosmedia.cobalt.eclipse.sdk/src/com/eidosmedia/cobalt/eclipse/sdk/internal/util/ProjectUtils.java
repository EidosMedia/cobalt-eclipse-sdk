package com.eidosmedia.cobalt.eclipse.sdk.internal.util;

import java.util.HashSet;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

public final class ProjectUtils {

	private ProjectUtils() {

	}

	public static final IPath[] getOutputLocations(IJavaProject javaProject) throws JavaModelException {

		HashSet<IPath> paths = new HashSet<IPath>();

		IPath path = javaProject.getOutputLocation();
		paths.add(path);

		IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
		for (IClasspathEntry entry : classpathEntries) {
			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				path = entry.getOutputLocation();
				if (path != null)
					paths.add(path);
			}
		}

		return paths.toArray(new IPath[paths.size()]);
	}

}
