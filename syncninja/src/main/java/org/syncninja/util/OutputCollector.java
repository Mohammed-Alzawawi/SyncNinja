package org.syncninja.util;

import java.util.ArrayList;
import java.util.List;

public class OutputCollector {
    private static final List<String> outputList = new ArrayList<>();

    public static void addString(String message) {
        outputList.add(message);
    }

    public static void refresh() {
        outputList.clear();
    }

    public static String getString() {
        StringBuilder output = new StringBuilder();
        for(String message : outputList){
            output.append(message);
        }
        return output.toString();
    }
}
