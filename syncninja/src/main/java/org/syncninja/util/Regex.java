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
        for (int index = 0; index < listOfFilesToBeAdded.size(); index++) {
            String path = listOfFilesToBeAdded.get(index);
            regexBuilder.append(path);
            if (path.endsWith(".")) {
                regexBuilder.append("*");
            }
            if (index != listOfFilesToBeAdded.size() - 1) {

                regexBuilder.append("|");
            }
        }

        return regexBuilder.toString();
    }
}