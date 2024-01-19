package org.example.kmeans;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ProcessTask {

    public static void main(String[] args) {
        int k = Integer.parseInt(args[0]);
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            while (true) {
                double[][] dataPointsSlice = (double[][])in.readObject();
                double[][] centroids = (double[][])in.readObject();

                double[][] processedResults = performClustering(dataPointsSlice, centroids, k);
                out.writeObject(processedResults);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static double[][] performClustering(double[][] dataPointsSlice, double[][] centroids, int k) {
        for (double[] point : dataPointsSlice) {
            point[2] = findClosestCluster(point, centroids, k);
        }
        return dataPointsSlice;
    }

    private static int findClosestCluster(double[] point, double[][] centroids, int k) {
        int closestCluster = 0;
        double minDistance = Double.MAX_VALUE;

        for (int j = 0; j < k; j++) {
            double distance = calculateDistance(point, centroids[j]);
            if (distance < minDistance) {
                closestCluster = j;
                minDistance = distance;
            }
        }

        return closestCluster;
    }

    private static double calculateDistance(double[] point, double[] centroid) {
        return Math.sqrt(Math.pow(point[0] - centroid[0], 2) + Math.pow(point[1] - centroid[1], 2));
    }
}
