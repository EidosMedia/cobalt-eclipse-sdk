package com.eidosmedia.cobalt.eclipse.sdk.internal;

import java.util.HashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

public class CobaltSDKImages {

    private static final HashMap<ImageDescriptor, Image> CACHE = new HashMap<ImageDescriptor, Image>();

    public static final ImageDescriptor ICON_OBJECT;

    public static final ImageDescriptor ICON_FILE;

    public static final ImageDescriptor ICON_FOLDER;

    public static final ImageDescriptor ICON_COBALT;

    static {

        IWorkbench workbench = PlatformUI.getWorkbench();
        ISharedImages sharedImages = workbench.getSharedImages();
        Bundle bundle = CobaltSDKPlugin.getDefault().getBundle();

        ICON_OBJECT = sharedImages.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
        ICON_FILE = sharedImages.getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
        ICON_FOLDER = sharedImages.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
        ICON_COBALT = ImageDescriptor.createFromURL(bundle.getResource("icons/cobalt.gif"));
    }

    public static synchronized Image getImage(ImageDescriptor imageDescriptor) {
        Image image = CACHE.get(imageDescriptor);
        if (image == null) {
            image = imageDescriptor.createImage();
            CACHE.put(imageDescriptor, image);
        }
        return image;
    }

}
