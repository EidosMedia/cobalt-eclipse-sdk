package com.eidosmedia.cobalt.eclipse.sdk.internal.util;

public class ClasspathBuilder {

	private StringBuilder m_stringBuilder = new StringBuilder();

	public ClasspathBuilder appendPath(String path) {
		if (path.contains(";"))
			throw new IllegalArgumentException("path contains \";\" char");
		if (m_stringBuilder.length() > 0)
			m_stringBuilder.append(";");
		m_stringBuilder.append(path);
		return this;
	}

	@Override
	public String toString() {
		return m_stringBuilder.toString();
	}

	public boolean isEmpty() {
		return m_stringBuilder.length() == 0;
	}

}
