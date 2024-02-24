package org.syncninja.Utilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

}
