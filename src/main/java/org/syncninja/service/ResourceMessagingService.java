package org.syncninja.service;

import org.syncninja.Utilities.ResourceBundleEnum;

import java.util.ResourceBundle;

public class ResourceMessagingService {
    private final ResourceBundle resourceBundle;

    public ResourceMessagingService() {
        this.resourceBundle = ResourceBundle.getBundle("messages");
    }

    public String getMessage(ResourceBundleEnum resourceBundleEnum) {
        return resourceBundle.getString(resourceBundleEnum.getKey());
    }

    public String getMessage(ResourceBundleEnum resourceBundleEnum, Object[] args) {
        String pattern = resourceBundle.getString(resourceBundleEnum.getKey());
        return formatMessage(pattern, args);
    }

    private String formatMessage(String pattern, Object[] args) {
        return args == null ? pattern : String.format(pattern, args);
    }
}
