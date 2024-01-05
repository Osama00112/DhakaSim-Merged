package thesisfinal;

import java.util.List;

public class SowdhaTest
{
    public static void main(String[] args)
    {
        List<Point2D> points;
        RandomPoint2DGenerator rand = new RandomPoint2DGenerator(100);
        points = rand.getPoints();

        /*System.out.print("(" + points.get(0).x + "," + points.get(0).y + ")");
        for(int i = 1; i < points.size(); i++)
        {
            System.out.print(", (" + points.get(i).x + "," + points.get(i).y + ")");
        }
        System.out.println();*/

        int k = 10; // Number of clusters
        KMeansClustering kMeans = new KMeansClustering(k, points);
        kMeans.initializeClusters();
        kMeans.run(100); // Run for a maximum of 100 iterations

        List<Cluster> clusters = kMeans.getClusters();
        for (int i = 0; i < clusters.size(); i++)
        {
            System.out.println("Cluster " + (i + 1) + ":");
            clusters.get(i).printPoints();
        }
    }
}
