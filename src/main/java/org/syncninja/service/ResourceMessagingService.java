package org.syncninja.service;

import org.syncninja.util.ResourceBundleEnum;

import java.util.ResourceBundle;

public class ResourceMessagingService {
    private static ResourceBundle resourceBundle;

    private ResourceMessagingService() {
        resourceBundle = ResourceBundle.getBundle("messages");
    }

    public static synchronized String getMessage(ResourceBundleEnum resourceBundleEnum) {
        if (resourceBundle == null) {
            new ResourceMessagingService();
        }
        return resourceBundle.getString(resourceBundleEnum.getKey());
    }

    public static synchronized String getMessage(ResourceBundleEnum resourceBundleEnum, Object[] args) {
        if (resourceBundle == null) {
            new ResourceMessagingService();
        }
        String pattern = resourceBundle.getString(resourceBundleEnum.getKey());
        return formatMessage(pattern, args);
    }

    private static String formatMessage(String pattern, Object[] args) {
        return args == null ? pattern : String.format(pattern, args);
    }
}