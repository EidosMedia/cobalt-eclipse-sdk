package com.eidosmedia.cobalt.eclipse.sdk.internal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public final class FileUtils {

	/**
	 * 
	 * @param file
	 * @return
	 * @throws CoreException
	 * @throws IOException
	 */
	public static String readContent(IFile file) throws CoreException, IOException {

		String charsetName = file.getCharset();
		if (charsetName == null)
			charsetName = "UTF-8";

		InputStream in = file.getContents();
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[1000000];
			int n;
			while ((n = in.read(buffer)) != -1)
				out.write(buffer, 0, n);

			if (out.size() == 0)
				return "";
			String content = new String(out.toByteArray(), 0, out.size());
			return content;
		} finally {
			if (in != null)
				in.close();
		}
	}

	/**
	 * 
	 * @param file
	 * @param content
	 * @throws CoreException
	 * @throws IOException
	 */
	public static void writeContent(IFile file, String content) throws CoreException, IOException {

		String charsetName = file.getCharset();
		if (charsetName == null)
			charsetName = "UTF-8";

		InputStream in = new ByteArrayInputStream(content.getBytes(charsetName));
		file.setContents(in, IResource.FORCE, null);
	}

	private FileUtils() {

	}
}
