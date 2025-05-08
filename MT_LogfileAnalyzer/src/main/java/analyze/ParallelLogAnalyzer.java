package analyze;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class ParallelLogAnalyzer {

    private static final String[] LOG_LEVELS = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR"};

    public static void main(String[] args) throws Exception {
        Path directory = Paths.get(".");
        Map<String, Integer> totalCounts = initializeLogLevelMap();

        int coreCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(coreCount);
        List<Future<Map<String, Integer>>> futures = new ArrayList<>();

        long startTime = System.nanoTime();

        try (DirectoryStream<Path> logFiles = Files.newDirectoryStream(directory, "*.log")) {
            for (Path file : logFiles) {
                futures.add(executor.submit(new LogAnalyzerTask(file)));
            }
        }

        int fileIndex = 1;
        for (Future<Map<String, Integer>> future : futures) {
            Map<String, Integer> result = future.get();
            System.out.println("Datei " + fileIndex++ + ": " + result);
            mergeCounts(totalCounts, result);
        }

        executor.shutdown();
        long duration = System.nanoTime() - startTime;

        System.out.println("Gesamt: " + totalCounts);
        System.out.printf("Parallele Laufzeit: %.2f ms%n", duration / 1_000_000.0);
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
