package org.syncninja.util;

import java.util.List;

public class LinesContainer {
    List<Integer> lineNumbers;
    List<String> newLines;
    List<String> oldLines;

    public LinesContainer(List<Integer> lineNumbers, List<String> newLines, List<String> oldLines) {
        this.lineNumbers = lineNumbers;
        this.newLines = newLines;
        this.oldLines = oldLines;
    }

    public List<Integer> getLineNumbers() {
        return lineNumbers;
    }

    public List<String> getNewLines() {
        return newLines;
    }

    public List<String> getOldLines() {
        return oldLines;
    }
}
