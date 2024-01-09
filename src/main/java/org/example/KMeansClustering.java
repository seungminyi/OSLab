package org.example;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class KMeansClustering {

    public static void main(String[] args) {
        CommandLineOptions options = parseCommandLineArgs(args);

        switch (options.mode) {
            case "dataGenerate":
                generateData(options.dataPoints, options.dataPath);
                break;
            case "single":
                runSingleThread(options.dataPath, options.clusters, options.iterations);
                break;
            case "multiThread":
                runMultiThread(options.dataPath, options.clusters, options.threads, options.iterations);
                break;
            case "multiProcess":
                runMultiProcess(options.dataPath, options.clusters, options.processes, options.iterations);
                break;
            default:
                System.out.println("잘못된 모드입니다.");
                break;
        }
    }

    private static CommandLineOptions parseCommandLineArgs(String[] args) {
        CommandLineOptions options = new CommandLineOptions();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-mode":
                    options.mode = args[++i];
                    break;
                case "-dataPath":
                    options.dataPath = args[++i];
                    break;
                case "-clusters":
                    options.clusters = Integer.parseInt(args[++i]);
                    break;
                case "-threads":
                    options.threads = Integer.parseInt(args[++i]);
                    break;
                case "-processes":
                    options.processes = Integer.parseInt(args[++i]);
                    break;
                case "-dataPoints":
                    options.dataPoints = Integer.parseInt(args[++i]);
                    break;
                case "-iterations":
                    options.iterations = Integer.parseInt(args[++i]);
                    break;
                default:
                    break;
            }
        }
        return options;
    }

    private static void generateData(int numberOfPoints, String fileName) {
        Random random = new Random();
        try {
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                for (int i = 0; i < numberOfPoints; i++) {
                    int x = random.nextInt(20001) - 10000;
                    int y = random.nextInt(20001) - 10000;
                    writer.write(x + "," + y + "\n");
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void runSingleThread(String dataPath, int clusters, int iterations) {
    }

    private static void runMultiThread(String dataPath, int clusters, int threads, int iterations) {
    }

    private static void runMultiProcess(String dataPath, int clusters, int processes, int iterations) {
    }

    static class CommandLineOptions {
        String mode = "";
        String dataPath = "";
        int clusters = 0;
        int threads = 0;
        int processes = 0;
        int dataPoints = 0;
        int iterations = 10;
    }
}
