package analyze;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class LogAnalyzerTask implements Callable<Map<String, Integer>> {

    private static final String[] LOG_LEVELS = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR"};
    private final Path filePath;

    public LogAnalyzerTask(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public Map<String, Integer> call() {
        Map<String, Integer> logLevelCounts = initializeLogLevelMap();

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                for (String level : LOG_LEVELS) {
                    if (line.contains(" " + level + " ")) {
                        logLevelCounts.merge(level, 1, Integer::sum);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Datei " + filePath + ": " + e.getMessage());
        }

        return logLevelCounts;
    }

    private Map<String, Integer> initializeLogLevelMap() {
        Map<String, Integer> map = new HashMap<>();
        for (String level : LOG_LEVELS) {
            map.put(level, 0);
        }
        return map;
    }
}
