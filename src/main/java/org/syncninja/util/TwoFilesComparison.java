package org.syncninja.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TwoFilesComparison {

    public static ArrayList<String[]> compareFiles(String newFilePath, String oldFilePath) throws IOException {
        try (BufferedReader reader1 = new BufferedReader(new FileReader(newFilePath));
             BufferedReader reader2 = new BufferedReader(new FileReader(oldFilePath))) {

            ArrayList<String[]> differanceList= new ArrayList<>();
            String[] tempList;

            int lineNumber = 1;
            String newLine, oldLine;
            while ((newLine = reader1.readLine()) != null && (oldLine = reader2.readLine()) != null) {
                if (!newLine.equals(oldLine)) {
                    tempList = new String[]{String.valueOf(lineNumber),newLine, oldLine};
                    differanceList.add(tempList);
                }
                lineNumber++;
            }
            return differanceList;
        }
    }
}