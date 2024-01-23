package org.example.kmeans;

import java.io.EOFException;
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
    private int[] ports;
    private ServerSocket[] serverSockets;

    public KMeansMultiProcess(double[][] dataPoints, int k) throws IOException {
        this.dataPoints = dataPoints;
        this.k = k;
        this.centroids = initCentroids(dataPoints, k);
    }

    private double[][] initCentroids(double[][] dataPoints, int k) {
        double[][] centroids = new double[k][2];
        for (int i = 0; i < k; i++) {
            centroids[i] = Arrays.copyOf(dataPoints[i], 2);
        }
        return centroids;
    }

    private void initServerSockets(int processes) throws IOException {
        this.serverSockets = new ServerSocket[processes];
        this.ports = new int[processes];
        for (int i = 0; i < processes; i++) {
            serverSockets[i] = new ServerSocket(0);
            ports[i] = serverSockets[i].getLocalPort();
        }
    }

    public String run(int processes, int iterations) throws IOException, InterruptedException {
        initServerSockets(processes);
        ExecutorService executor = Executors.newFixedThreadPool(processes);
        List<ProcessTaskHandler> processHandlers = createProcessHandlers(processes);

        for (int iter = 0; iter < iterations; iter++) {
            boolean b = iter == 0;
            CountDownLatch latch = new CountDownLatch(processes);

            clusterProcess(executor, processHandlers, latch, b);

            latch.await();
            updateCentroids();
        }

        closeResources(processHandlers, executor);
        return buildResult();
    }

    private List<ProcessTaskHandler> createProcessHandlers(int processes) throws IOException {
        List<ProcessTaskHandler> handlers = new ArrayList<>();
        int taskSize = dataPoints.length / processes;
        for (int i = 0; i < processes; i++) {
            int start = i * taskSize;
            int end = (i == processes - 1) ? dataPoints.length : (i + 1) * taskSize;
            handlers.add(new ProcessTaskHandler(start, end, k, ports[i], serverSockets[i]));
        }
        return handlers;
    }

    private void clusterProcess(ExecutorService executor, List<ProcessTaskHandler> handlers, CountDownLatch latch, boolean isFirst) {
        for (int processIdx = 0; processIdx < handlers.size(); processIdx++) {
            final int finalProcessIdx = processIdx;
            int startIdx = handlers.get(finalProcessIdx).startIdx;
            int endIdx = handlers.get(finalProcessIdx).endIdx;
            executor.submit(() -> {
                try {
                    if (isFirst) {
                        double[][] dataSlice = getDataSlice(startIdx, endIdx);
                        handlers.get(finalProcessIdx).sendData(dataSlice);
                    }
                    handlers.get(finalProcessIdx).sendData(centroids);
                    double[] processedData = handlers.get(finalProcessIdx).receiveData();
                    for (int j = startIdx; j < endIdx; j++) {
                        dataPoints[j][2] = processedData[j - startIdx];
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
    }

    private double[][] getDataSlice(int start, int end) {
        return Arrays.copyOfRange(dataPoints, start, end);
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
    public final int startIdx;
    public final int endIdx;

    public ProcessTaskHandler(int startIdx, int endIdx, int k, int port, ServerSocket serverSocket) throws
        IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("java",
            "-cp",
            "build/libs/os-lab.jar",
            "org.example.kmeans.ProcessTask",
            String.valueOf(k),
            String.valueOf(port));
        Process process = processBuilder.start();
        Socket socket = serverSocket.accept();
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.startIdx = startIdx;
        this.endIdx = endIdx;
    }

    public void sendData(double[][] data) throws IOException {
        out.writeObject(data);
    }

    public double[] receiveData() throws IOException, ClassNotFoundException {
        return (double[])in.readObject();
    }

    public void close() throws IOException {
        out.close();
        in.close();
    }
}
