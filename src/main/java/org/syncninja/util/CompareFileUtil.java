package org.syncninja.util;

import org.syncninja.service.StateFileService;
import java.util.*;

public class CompareFileUtil {
    private static StateFileService stateFileService;

    public CompareFileUtil() {
        stateFileService = new StateFileService();
    }

    public static List<List<String>> compareFiles(String filePath) throws Exception {
        List<String> newFileList = Fetcher.readFile(filePath);
        List<String> oldFileList = stateFileService.getStateFile(filePath).getLines();
        return compareNewAndOldLists(newFileList, oldFileList);
    }

    private static List<List<String>> compareNewAndOldLists(List<String> newFileList, List<String> oldFileList){
        // finding the max length
        int maximumLength = newFileList.size();
        if (oldFileList.size() > maximumLength) {maximumLength = oldFileList.size();}

        List<List<String>> differenceList= new ArrayList<>();
        int lineNumber = 1;
        String newLine, oldLine;

        while(lineNumber <= maximumLength){
            List<String> tempList = new ArrayList<>();

            try{newLine = newFileList.get(lineNumber-1);}
            catch (IndexOutOfBoundsException e) {newLine = "";}

            try{oldLine = oldFileList.get(lineNumber-1);}
            catch (IndexOutOfBoundsException e) {oldLine = "";}

            if(!newLine.equals(oldLine)){
                tempList.add(String.valueOf(lineNumber));
                tempList.add(newLine);
                tempList.add(oldLine);
                differenceList.add(tempList);
            }

            lineNumber++;
        }
        return differenceList;
    }
}
