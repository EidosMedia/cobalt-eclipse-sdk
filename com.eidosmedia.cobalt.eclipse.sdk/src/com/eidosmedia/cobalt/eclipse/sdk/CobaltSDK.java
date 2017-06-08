package com.eidosmedia.cobalt.eclipse.sdk;

import com.eidosmedia.cobalt.eclipse.sdk.internal.CobaltSDKPlugin;
import com.eidosmedia.cobalt.eclipse.sdk.internal.server.ICobaltRuntime;

/**
 * 
 */
public final class CobaltSDK {

    private CobaltSDK() {

    }

    /**
     * 
     * @return
     */
    public static String getRuntimePath() {
        ICobaltRuntime runtime = CobaltSDKPlugin.getDefaultRuntime();
        if (runtime == null) {
            throw new IllegalStateException("Cobalt SDK Runtime not configured");
        }
        return runtime.getLocationPath();
    }

}
