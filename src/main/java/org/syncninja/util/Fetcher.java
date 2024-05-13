package org.syncninja.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Fetcher {
    public static List<String> readFile(String filePath) throws IOException {
        Path path = Path.of(filePath);
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        try (InputStream inputStream = Files.newInputStream(path)) {
            int charInt;
            while ((charInt = inputStream.read()) != -1) {
                char character = (char) charInt;
                if (character == '\r') {
                    continue;
                }
                if (character == '\n') {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                } else {
                    currentLine.append(character);
                }
            }
            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }
        }

        return lines;
    }

    public static String getRelativePath(String path) {
        String mainDirectoryPath = System.getProperty("user.dir");
        return path.substring(mainDirectoryPath.length() + 1);
    }

    public static String getPathForQuery(String path) {
        return path.replace("\\", "\\\\");
    }
}