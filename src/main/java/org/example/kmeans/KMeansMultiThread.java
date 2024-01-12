package org.example.kmeans;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KMeansMultiThread {
    private double[][] dataPoints;
    private int k;
    private double[][] centroids;

    public KMeansMultiThread(double[][] dataPoints, int k) {
        this.dataPoints = dataPoints;
        this.k = k;
        this.centroids = new double[k][2];

        // 초기 중심점 설정
        for (int i = 0; i < k; i++) {
            centroids[i][0] = dataPoints[i][0];
            centroids[i][1] = dataPoints[i][1];
        }
    }

    public String run(int threads, int iterations) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int iter = 0; iter < iterations; iter++) {
            int taskSize = dataPoints.length / threads;
            for (int j = 0; j < threads; j++) {
                int start = j * taskSize;
                int end = (j == threads - 1) ? dataPoints.length : (j + 1) * taskSize;
                executor.submit(() -> assignPointsToClusters(start, end));
            }

            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.MINUTES);

            if (iter < iterations - 1) {
                executor = Executors.newFixedThreadPool(threads);
            }

            updateCentroids();
        }

        executor.shutdown();
        return buildResult();
    }

    private void assignPointsToClusters(int start, int end) {
        for (int i = start; i < end; i++) {
            double[] point = dataPoints[i];
            int closestCluster = findClosestCluster(point);
            point[2] = closestCluster;
        }
    }

    private int findClosestCluster(double[] point) {
        int closestCluster = 0;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < k; i++) {
            double distance = Math.sqrt(Math.pow(point[0] - centroids[i][0], 2) + Math.pow(point[1] - centroids[i][1], 2));
            if (distance < minDistance) {
                closestCluster = i;
                minDistance = distance;
            }
        }
        return closestCluster;
    }

    private void updateCentroids() {
        double[][] newCentroids = new double[k][2];
        int[] counts = new int[k];

        for (double[] point : dataPoints) {
            int cluster = (int) point[2];
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

    private String buildResult() {
        StringBuilder result = new StringBuilder();
        int[] clusterSizes = new int[k];
        for (double[] point : dataPoints) {
            int cluster = (int) point[2];
            clusterSizes[cluster]++;
        }

        for (int i = 0; i < k; i++) {
            result.append("Cluster ").append(i + 1).append(": ").append(clusterSizes[i]).append("\n");
        }
        return result.toString();
    }
}