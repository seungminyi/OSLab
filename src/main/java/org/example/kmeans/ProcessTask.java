package org.example.kmeans;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ProcessTask {

    public static void main(String[] args) {
        int k = Integer.parseInt(args[0]);
        int port = Integer.parseInt(args[1]);
        double[][] dataPointsSlice;

        try (Socket socket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            dataPointsSlice = (double[][])in.readObject();

            while (true) {
                double[][] centroids = (double[][])in.readObject();

                double[] processedResults = performClustering(dataPointsSlice, centroids, k);
                out.writeObject(processedResults);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static double[] performClustering(double[][] dataPointsSlice, double[][] centroids, int k) {
        double[] result = new double[dataPointsSlice.length];
        for (int i = 0;  i < dataPointsSlice.length; i++) {
            result[i] = findClosestCluster(dataPointsSlice[i], centroids, k);
        }
        return result;
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
