package org.example.kmeans;

public class DataPoint {
    private double x;
    private double y;
    private int cluster;

    public DataPoint(double x, double y) {
        this.x = x;
        this.y = y;
        this.cluster = -1;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getCluster() {
        return cluster;
    }

    public void setCluster(int cluster) {
        this.cluster = cluster;
    }
}
