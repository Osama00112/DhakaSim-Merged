package thesisfinal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomPoint2DGenerator
{
    private final int n;
    private final Random random;

    RandomPoint2DGenerator(int n)
    {
        this.n = n;
        random = new Random();
    }

    List<Point2D> getPoints()
    {
        List<Point2D> points = new ArrayList<>();
        for(int i = 0; i < n; i++)
        {
            Point2D p = new Point2D(random.nextDouble()*100, random.nextDouble()*100);
            ///System.out.println("(" + p.x + ", " + p.y + ")");
            points.add(p);
        }
        return points;
    }
}
