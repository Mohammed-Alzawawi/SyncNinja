package org.syncninja;

import java.util.HashMap;
import java.util.Map;

public class OutputCollector {
    private static Map<String, StringBuilder> stringBuilderMap = new HashMap<>();
    public static void addString(String id, String s) {
        if(stringBuilderMap.get(id) == null){
            stringBuilderMap.put(id, new StringBuilder());
        }
        StringBuilder stringBuilder = stringBuilderMap.get(id);
        stringBuilder.append(s);
        stringBuilder.append('\n');
    }
    public static void refresh(String id) {
        stringBuilderMap.remove(id);
    }
    public static String getString(String id) {
        return stringBuilderMap.get(id).toString();
    }
}
