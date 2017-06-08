package com.eidosmedia.cobalt.eclipse.sdk.internal;

import static com.eidosmedia.cobalt.eclipse.sdk.internal.CobaltSDKPlugin.PLUGIN_ID;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * This is a simple logger that logs messages and stack traces to the Eclipse
 * error log.
 */
public class Logger {

    public static final int OK = IStatus.OK;

    public static final int ERROR = IStatus.ERROR;

    public static final int CANCEL = IStatus.CANCEL;

    public static final int INFO = IStatus.INFO;

    public static final int WARNING = IStatus.WARNING;

    private static ILog logger;

    /**
     * Get a reference to the Eclipse error log
     */
    static {
        logger = Platform.getLog(Platform.getBundle(PLUGIN_ID));
    }

    /**
     * Prints stack trace to Eclipse error log
     */
    public static final void log(int severity, Throwable ex) {
        Status s = new Status(severity, PLUGIN_ID, IStatus.OK, ex.getMessage(), ex);
        logger.log(s);
    }

    /**
     * Prints a message to the Eclipse error log
     */
    public static final void log(int severity, String msg) {
        Status s = new Status(severity, PLUGIN_ID, IStatus.OK, msg, null);
        logger.log(s);
    }

    /**
     * Prints a message and stack trace to the Eclipse error log
     */
    public static final void log(int severity, String msg, Throwable ex) {
        Status s = new Status(severity, PLUGIN_ID, IStatus.OK, msg, ex);
        logger.log(s);
    }

}
