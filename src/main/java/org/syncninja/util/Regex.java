package org.syncninja.util;

import java.util.ArrayList;
import java.util.List;

public class Regex {
    private final List<String> listOfFilesToBeAdded;

    public Regex() {
        listOfFilesToBeAdded = new ArrayList<>();
    }

    public void addFilePath(String path) {
        listOfFilesToBeAdded.add(path);
    }

    public String buildRegex() {
        StringBuilder regexBuilder = new StringBuilder();
        for (String path : listOfFilesToBeAdded) {
            if (path.endsWith(".")) {
                path = path + "*";
            }
            regexBuilder.append("|").append(path);
        }
        if (!regexBuilder.isEmpty()) {
            regexBuilder.deleteCharAt(0);
        }
        return regexBuilder.toString();
    }
}

