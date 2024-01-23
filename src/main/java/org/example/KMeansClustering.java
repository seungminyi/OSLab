package org.example;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.example.kmeans.KMeansMultiProcess;
import org.example.kmeans.KMeansMultiThread;
import org.example.kmeans.KMeansSingleThread;

public class KMeansClustering {

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        CommandLineOptions options = parseCommandLineArgs(args);

        switch (options.mode) {
            case "dataGenerate":
                generateData(options.dataPoints, options.dataPath);
                break;
            case "run":
                runSingleThread(options.dataPath, options.clusters, options.iterations);
                runMultiThread(options.dataPath, options.clusters, options.threads, options.iterations);
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

    private static void runSingleThread(String dataPath, int clusters, int iterations) throws FileNotFoundException {
        double[][] dataPoints = readDataPoints(dataPath);
        KMeansSingleThread kMeans = new KMeansSingleThread(dataPoints, clusters);

        long startTime = System.currentTimeMillis();
        String result = kMeans.run(iterations);
        long endTime = System.currentTimeMillis();

        long executionTime = endTime - startTime;
        System.out.println(result);
        System.out.println("싱글 스레드 실행 시간: " + executionTime + "ms");
    }

    private static void runMultiThread(String dataPath, int clusters, int threads, int iterations) throws FileNotFoundException, InterruptedException {
        double[][] dataPoints = readDataPoints(dataPath);
        KMeansMultiThread kMeans = new KMeansMultiThread(dataPoints, clusters);

        long startTime = System.currentTimeMillis();
        String result = kMeans.run(threads, iterations);
        long endTime = System.currentTimeMillis();

        long executionTime = endTime - startTime;
        System.out.println(result);
        System.out.println("멀티 스레드 실행 시간: " + executionTime + "ms");
    }

    private static void runMultiProcess(String dataPath, int clusters, int processes, int iterations)
        throws IOException, ClassNotFoundException, InterruptedException {

        double[][] dataPoints = readDataPoints(dataPath);
        KMeansMultiProcess kMeans = new KMeansMultiProcess(dataPoints, clusters);

        long startTime = System.currentTimeMillis();
        String result = kMeans.run(processes, iterations);
        long endTime = System.currentTimeMillis();

        long executionTime = endTime - startTime;
        System.out.println(result);
        System.out.println("멀티 프로세스 실행 시간: " + executionTime + "ms");
    }


    private static double[][] readDataPoints(String dataPath) throws FileNotFoundException {
        List<double[]> dataList = new ArrayList<>();
        File file = new File(dataPath);
        if (!file.exists()) {
            System.err.println("File not found: " + dataPath);
            throw new FileNotFoundException();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                dataList.add(new double[] {x, y, 0});
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing number: " + e.getMessage());
        }

        double[][] dataPoints = new double[dataList.size()][3];
        for (int i = 0; i < dataList.size(); i++) {
            dataPoints[i] = dataList.get(i);
        }
        return dataPoints;
    }


    static class CommandLineOptions {
        String mode = "";
        String dataPath = "";
        int clusters = 5;
        int threads = 8;
        int processes = 8;
        int dataPoints = 0;
        int iterations = 10;
    }
}
