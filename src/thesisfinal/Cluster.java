package thesisfinal;

import java.util.ArrayList;
import java.util.List;

class Cluster
{
    Point2D centroid;
    List<Point2D> points;

    public Cluster(Point2D centroid)
    {
        this.centroid = centroid;
        this.points = new ArrayList<>();
    }

    public void addPoint(Point2D point)
    {
        points.add(point);
    }

    public void clearPoints()
    {
        points.clear();
    }

    public List<Point2D> getPoints()
    {
        return points;
    }

    public void printPoints()
    {
        System.out.print("(" + points.get(0).x + "," + points.get(0).y + ")");
        for(int i = 1; i < points.size(); i++)
        {
            System.out.print(", (" + points.get(i).x + "," + points.get(i).y + ")");
        }
        System.out.println();
    }
}
