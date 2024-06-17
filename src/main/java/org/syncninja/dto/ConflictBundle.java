package org.syncninja.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConflictBundle {
    private Map<String, List<String>> conflictFileMap;

    public ConflictBundle() {
        this.conflictFileMap = new HashMap<>();
    }

    public Map<String, List<String>> getConflictFileMap() {
        return conflictFileMap;
    }

    public void setConflictFileMap(Map<String, List<String>> conflictFileMap) {
        this.conflictFileMap = conflictFileMap;
    }

    public boolean containsConflict() {
        return !conflictFileMap.isEmpty();
    }
}
