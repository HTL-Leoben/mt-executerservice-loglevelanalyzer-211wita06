package analyze;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class SequentialLogAnalyzer {

    private static final String[] LOG_LEVELS = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR"};

    public static void main(String[] args) {
        Path directory = Paths.get(".");

        Map<String, Integer> totalCounts = initializeLogLevelMap();

        long startTime = System.nanoTime();

        try (DirectoryStream<Path> logFiles = Files.newDirectoryStream(directory, "*.log")) {
            for (Path logFile : logFiles) {
                Map<String, Integer> fileCounts = analyzeFile(logFile);
                System.out.println("Datei: " + logFile.getFileName() + " -> " + fileCounts);
                mergeCounts(totalCounts, fileCounts);
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Log-Dateien: " + e.getMessage());
        }

        long duration = System.nanoTime() - startTime;
        System.out.println("Gesamt: " + totalCounts);
        System.out.printf("Sequentielle Laufzeit: %.2f ms%n", duration / 1_000_000.0);
    }

    private static Map<String, Integer> analyzeFile(Path path) throws IOException {
        Map<String, Integer> counts = initializeLogLevelMap();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                for (String level : LOG_LEVELS) {
                    if (line.contains(" " + level + " ")) {
                        counts.merge(level, 1, Integer::sum);
                        break;
                    }
                }
            }
        }
        return counts;
    }

    private static Map<String, Integer> initializeLogLevelMap() {
        Map<String, Integer> map = new HashMap<>();
        for (String level : LOG_LEVELS) {
            map.put(level, 0);
        }
        return map;
    }

    private static void mergeCounts(Map<String, Integer> total, Map<String, Integer> addition) {
        for (String level : LOG_LEVELS) {
            total.merge(level, addition.getOrDefault(level, 0), Integer::sum);
        }
    }
}
