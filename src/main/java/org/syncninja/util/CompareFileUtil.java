package org.syncninja.util;

import org.syncninja.service.StatusService;

import java.util.*;

public class CompareFileUtil {

    private final StatusService statusService;

    public CompareFileUtil() {
        statusService = new StatusService();
    }

    public LinesContainer compareFiles(String filePath) throws Exception {
        List<String> newFileList = Fetcher.readFile(filePath);
        List<String> oldFileList;

        FileState fileState = statusService.getState(filePath.substring(0, filePath.lastIndexOf("\\")));
        if(fileState != null){
            oldFileList = fileState.getUntrackedDTO(filePath).getStateFile().getLines();
        }
        else{
            oldFileList = new ArrayList<>();
        }

        return compareNewAndOldLists(newFileList, oldFileList);
    }

    private static LinesContainer compareNewAndOldLists(List<String> newFileList, List<String> oldFileList){
        LinesContainer linesContainer = new LinesContainer(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        int maximumLength = Math.max(newFileList.size(), oldFileList.size());

        int lineNumber = 1;
        String newLine, oldLine;

        while(lineNumber <= maximumLength){
            if(lineNumber > newFileList.size()){ newLine = ""; }
            else {newLine = newFileList.get( lineNumber-1); }

            if(lineNumber > oldFileList.size()){ oldLine = ""; }
            else {oldLine = oldFileList.get( lineNumber-1); }

            if(!newLine.equals(oldLine)){
                linesContainer.getLineNumbers().add(lineNumber);
                linesContainer.getNewLines().add(newLine);
                linesContainer.getOldLines().add(oldLine);
            }

            lineNumber++;
        }
        return linesContainer;
    }
}
