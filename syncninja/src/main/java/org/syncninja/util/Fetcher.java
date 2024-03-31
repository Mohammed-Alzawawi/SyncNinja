package org.syncninja.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Fetcher {
    public static List<String> readFile(String filePath) throws IOException {
        Path path = Path.of(filePath);
        List<String> lines = new ArrayList<>();

        try (InputStream inputStream = Files.newInputStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        return lines;
    }

    public static String getRelativePath(String path){
        String mainDirectoryPath = System.getProperty("user.dir");
        return path.substring(mainDirectoryPath.length() + 1);
    }
}