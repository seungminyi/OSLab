package org.example.kmeans;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KMeansMultiProcess {

    private double[][] dataPoints;
    private final int k;
    private double[][] centroids;
    private final ArrayList<double[]> updatedDataPointsList = new ArrayList<>();
    private final int[] ports;
    private final ServerSocket[] serverSockets;

    public KMeansMultiProcess(double[][] dataPoints, int k) throws IOException {
        this.dataPoints = dataPoints;
        this.k = k;
        this.centroids = initCentroids(dataPoints, k);
        this.ports = new int[k];
        this.serverSockets = initServerSockets(k);
    }

    private double[][] initCentroids(double[][] dataPoints, int k) {
        double[][] centroids = new double[k][2];
        for (int i = 0; i < k; i++) {
            centroids[i] = Arrays.copyOf(dataPoints[i], 2);
        }
        return centroids;
    }

    private ServerSocket[] initServerSockets(int k) throws IOException {
        ServerSocket[] serverSockets = new ServerSocket[k];
        for (int i = 0; i < k; i++) {
            serverSockets[i] = new ServerSocket(0);
            ports[i] = serverSockets[i].getLocalPort();
        }
        return serverSockets;
    }

    public String run(int processes, int iterations) throws IOException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(processes);
        List<ProcessTaskHandler> processHandlers = createProcessHandlers(processes);

        for (int iter = 0; iter < iterations; iter++) {
            CountDownLatch latch = new CountDownLatch(processes);
            updatedDataPointsList.clear();

            clusterProcess(executor, processHandlers, latch);

            latch.await();
            updateDataPoints();
            updateCentroids();
        }

        closeResources(processHandlers, executor);
        return buildResult();
    }

    private List<ProcessTaskHandler> createProcessHandlers(int processes) throws IOException {
        List<ProcessTaskHandler> handlers = new ArrayList<>();
        for (int i = 0; i < processes; i++) {
            handlers.add(new ProcessTaskHandler("build/libs/os-lab.jar", "org.example.kmeans.ProcessTask", k, ports[i], serverSockets[i]));
        }
        return handlers;
    }

    private void clusterProcess(ExecutorService executor, List<ProcessTaskHandler> handlers, CountDownLatch latch) {
        for (int i = 0; i < handlers.size(); i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    double[][] dataSlice = getDataSlice(index, handlers.size());
                    handlers.get(index).sendData(dataSlice, centroids);
                    double[][] processedData = handlers.get(index).receiveData();
                    synchronized (updatedDataPointsList) {
                        updatedDataPointsList.addAll(Arrays.asList(processedData));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
    }

    private double[][] getDataSlice(int index, int totalProcesses) {
        int totalDataPoints = dataPoints.length;
        int sliceSize = totalDataPoints / totalProcesses;
        int startIndex = index * sliceSize;
        int endIndex = (index == totalProcesses - 1) ? totalDataPoints : startIndex + sliceSize;

        return Arrays.copyOfRange(dataPoints, startIndex, endIndex);
    }

    private void updateDataPoints() {
        dataPoints = updatedDataPointsList.toArray(new double[0][]);
    }

    private void updateCentroids() {
        double[][] newCentroids = new double[k][2];
        int[] counts = new int[k];

        for (double[] point : dataPoints) {
            int cluster = (int)point[2];
            newCentroids[cluster][0] += point[0];
            newCentroids[cluster][1] += point[1];
            counts[cluster]++;
        }

        for (int i = 0; i < k; i++) {
            if (counts[i] > 0) {
                newCentroids[i][0] /= counts[i];
                newCentroids[i][1] /= counts[i];
            }
        }

        centroids = newCentroids;
    }

    private void closeResources(List<ProcessTaskHandler> handlers, ExecutorService executor) throws IOException {
        executor.shutdown();
        for (ProcessTaskHandler handler : handlers) {
            handler.close();
        }
    }

    private String buildResult() {
        StringBuilder result = new StringBuilder();
        int[] clusterSizes = new int[k];
        for (double[] point : dataPoints) {
            int cluster = (int)point[2];
            clusterSizes[cluster]++;
        }

        for (int i = 0; i < k; i++) {
            result.append("Cluster ").append(i + 1).append(": ").append(clusterSizes[i]).append("\n");
        }
        return result.toString();
    }
}

class ProcessTaskHandler {

    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public ProcessTaskHandler(String jarPath, String className, int k, int port, ServerSocket serverSocket) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", jarPath, className, String.valueOf(k), String.valueOf(port));
        Process process = processBuilder.start();
        Socket socket = serverSocket.accept();
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public void sendData(double[][] dataSlice, double[][] centroids) throws IOException {
        out.writeObject(dataSlice);
        out.writeObject(centroids);
    }

    public double[][] receiveData() throws IOException, ClassNotFoundException {
        return (double[][]) in.readObject();
    }

    public void close() throws IOException {
        out.close();
        in.close();
    }
}
