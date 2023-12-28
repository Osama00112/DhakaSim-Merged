package thesisfinal;

import java.util.*;

public class ConvexHull {
    static Point2D startingPoint;

    // collinear: 0, counter-clockwise: +1, clockwise: -1
    static int getOrientation(Point2D a, Point2D b, Point2D c) {
        double cross = (b.x - a.x)*(c.y-a.y) - (c.x-a.x)*(b.y-a.y); // AB cross AC
        if(cross > 0) {
            return 1;
        }
        else if(cross < 0){
            return -1;
        }
        else{
            return 0;
        }
    }

    static double getSquaredDistance(Point2D a, Point2D b){
        return (a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y);
    }

    // Graham Scan - O(nlogn)
    static ArrayList<Point2D> findConvexHull(ArrayList<Point2D>pointList){

        if(pointList.size() == 0) return null;

        Point2D[] points = pointList.toArray(new Point2D[0]);
        int bottomMostPointIndex = 0;
        double leastY = points[0].y;

        // finding bottommost point breaking ties arbitrarily
        // the processing will start from this point
        for(int i=1; i<points.length; i++){
            if(points[i].y < leastY){
                leastY = points[i].y;
                bottomMostPointIndex = i;
            }
        }

        startingPoint = points[bottomMostPointIndex];

        // swapping the bottommost point with the first point
        Point2D temp = points[0];
        points[0] = points[bottomMostPointIndex];
        points[bottomMostPointIndex] = temp;

        Arrays.sort(points, 1, points.length, new Comparator<Point2D>() {
            @Override
            public int compare(Point2D point1, Point2D point2) {
                int orientation = getOrientation(startingPoint, point1, point2);
                if(orientation == 0){ // handling collienear points
                    if(getSquaredDistance(startingPoint, point1) < getSquaredDistance(startingPoint, point2)){
                        return -1; // ordering the nearer point earlier
                    }
                    else {
                        return 1;
                    }
                } else {
                    return -orientation; // because if it is counter-clockwise, it will be placed first in the ordering
                }
            }
        });

        ArrayList<Point2D>candidatePoints = new ArrayList<>();
        candidatePoints.add(points[0]);

        int i=1;
        while(i<points.length){
            int j = i+1; // finding next non-collinear index
            while(j<points.length && (getOrientation(startingPoint, points[i], points[j]) == 0)) {
                j++;
            }
            candidatePoints.add(points[j-1]); // taking the farthest point in case of points that are collinear wrt the starting point
            i = j;
        }

        ArrayList<Point2D>hullPoints = new ArrayList<>();
        if(candidatePoints.size() <= 2){
            // single point
            for(Point2D p: candidatePoints){
                hullPoints.add(p);
            }
            return hullPoints;
        }

        Stack<Point2D>st = new Stack<>();

        for(i=0; i<3; i++){
            st.push(candidatePoints.get(i));
        }

        for(i=3; i<candidatePoints.size(); i++){
            while(st.size() >= 2){
                Point2D top = st.peek();
                st.pop();
                Point2D secondTop = st.peek();
                st.push(top);
                if(getOrientation(secondTop, top, candidatePoints.get(i)) != 1){
                    st.pop();
                }
                else{
                    break;
                }
            }
            st.push(candidatePoints.get(i));
        }

        while(st.size() > 0){
            hullPoints.add(st.peek());
            st.pop();
        }

        Collections.reverse(hullPoints);

        return hullPoints;

    }

    static double calculateHullArea(ArrayList<Point2D>hullPoints){
        if(hullPoints.size() < 3){
            return 0;
        }
        int n = hullPoints.size();
        double area = 0.0;
        for(int i=0; i<n; i++){
            Point2D p1 = hullPoints.get(i);
            Point2D p2 = hullPoints.get((i+1)%n);
            area += ((p1.x*p2.y) - (p1.y*p2.x));
        }
        area /= 2;
        return area;
    }

    public static void main(String[] args) {
        test();
    }

    public static void test() {
        while(true){
            int n;
            Scanner in = new Scanner(System.in);
            n = in.nextInt();
            if(n == 0){
                break;
            }
            ArrayList<Point2D> pointList = new ArrayList<>();
            for(int i=0; i<n; i++){
                int x,y;
                x = in.nextInt();
                y = in.nextInt();
                pointList.add(new Point2D(x,y));
            }
            ArrayList<Point2D>hull = findConvexHull(pointList);
            System.out.println(hull.size());
            for(Point2D p: hull){
                System.out.println(p);
            }
            System.out.println(calculateHullArea(hull));
        }
    }
}



// Testing

/*

Test 1

8 7 7 7 -7 -7 -7 -7 7 9 0 -9 0 0 9 0 -9

Test 2

16 7 7 7 -7 -7 -7 -7 7 9 0 -9 0 0 9 0 -9 0 0 1 2 -2 1 -1 -1 3 4 4 3 -5 4 6 5

Test 3

72 0 0 1 2 -2 1 -1 -1 3 4 4 3 -5 4 6 5 7 7 7 -7 -7 -7 -7 7 9 0 -9 0 0 9 0 -9 -8 0 8 0 -7 0 7 0 -6 0 6 0 -5 0 5 0 -4 0 4 0 -3 0 3 0 -2 0 2 0 -1 0 1 0 0 -8 0 8 0 -7 0 7 0 -6 0 6 0 -5 0 5 0 -4 0 4 0 -3 0 3 0 -2 0 2 0 -1 0 1 1 1 2 2 3 3 4 4 5 5 6 6 1 -1 2 -2 3 -3 4 -4 5 -5 6 -6 -1 1 -2 2 -3 3 -4 4 -5 5 -6 6 -1 -1 -2 -2 -3 -3 -4 -4 -5 -5 -6 -6

All answers are the same: 8 (0, -9) (7, -7) (9, 0) (7, 7) (0, 9) (-7, 7) (-9, 0) (-7, -7)

*/