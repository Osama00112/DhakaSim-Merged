package thesisfinal;

import java.util.ArrayList;
import java.util.List;

public class KMeansClustering
{
    private final int k;
    private final List<Point2D> points;
    private final List<Cluster> clusters;

    public KMeansClustering(int k, List<Point2D> points) {
        this.k = Math.min(k, points.size());
        this.points = points;
        this.clusters = new ArrayList<>();
    }

    public void initializeClusters() {
        ///Random random = new Random();
        for (int i = 0; i < k; i++) {
            ///Point2D randomPoint = points.get(random.nextInt(points.size()));
            Point2D randomPoint = points.get(i);
            Cluster cluster = new Cluster(randomPoint);
            clusters.add(cluster);
        }
    }

    private double calculateDistance(Point2D p1, Point2D p2)
    {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    private Cluster findClosestCluster(Point2D point) {
        Cluster closestCluster = null;
        double minDistance = Double.MAX_VALUE;

        for (Cluster cluster : clusters) {
            double distance = calculateDistance(point, cluster.centroid);
            if (distance < minDistance) {
                minDistance = distance;
                closestCluster = cluster;
            }
        }
        return closestCluster;
    }

    private void assignPointsToClusters()
    {
        for (Cluster cluster : clusters) {
            cluster.clearPoints();
        }

        for (Point2D point : points) {
            Cluster closestCluster = findClosestCluster(point);
            closestCluster.addPoint(point);
        }
    }

    private void updateClusterCentroids()
    {
        for (Cluster cluster : clusters) {
            double sumX = 0, sumY = 0;

            for (Point2D point : cluster.points) {
                sumX += point.x;
                sumY += point.y;
            }

            if (!cluster.points.isEmpty()) {
                cluster.centroid.x = sumX / cluster.points.size();
                cluster.centroid.y = sumY / cluster.points.size();
            }
        }
    }

    public List<Cluster> getClusters()
    {
        return clusters;
    }

    public void run(int maxIterations)
    {
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            assignPointsToClusters();
            updateClusterCentroids();
        }
    }
}
