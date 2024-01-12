package org.example.kmeans;

import java.util.ArrayList;
import java.util.List;

public class KMeansSingleThread {
    private double[][] dataPoints;
    private int k;
    private double[][] centroids;

    public KMeansSingleThread(double[][] dataPoints, int k) {
        this.dataPoints = dataPoints;
        this.k = k;
        this.centroids = new double[k][2];

        // 초기 중심점 설정
        for (int i = 0; i < k; i++) {
            centroids[i][0] = dataPoints[i][0];
            centroids[i][1] = dataPoints[i][1];
        }
    }

    public String run(int iterations) {
        for (int i = 0; i < iterations; i++) {
            for (double[] point : dataPoints) {
                int closestCluster = 0;
                double minDistance = Double.MAX_VALUE;
                for (int j = 0; j < k; j++) {
                    double distance = distance(point, centroids[j]);
                    if (distance < minDistance) {
                        closestCluster = j;
                        minDistance = distance;
                    }
                }
                point[2] = closestCluster;
            }

            updateCentroids();
        }

        return buildResult();
    }

    private double distance(double[] point, double[] centroid) {
        return Math.sqrt(Math.pow(point[0] - centroid[0], 2) + Math.pow(point[1] - centroid[1], 2));
    }

    private void updateCentroids() {
        double[][] newCentroids = new double[k][2];
        int[] counts = new int[k];

        // 새 중심점 계산
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