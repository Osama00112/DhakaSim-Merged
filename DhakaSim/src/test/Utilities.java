package test;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.Well19937c;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

import static java.lang.Integer.min;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static test.DhakaSimFrame.*;

class Utilities {
    static GammaDistribution gb = new GammaDistribution(new Well19937c(seed), 1.68256, 0.04169);

    static double returnX3(double x1, double y1, double x2, double y2, double d) {
        double u = x2 - x1;
        double v = y2 - y1;
        double temp = u;
        u = -v;
        v = temp;
        double denom = sqrt(u * u + v * v);
        u = u / denom;
        v = v / denom;
        return x1 + d * u;
    }

    static double returnY3(double x1, double y1, double x2, double y2, double d) {
        double u = x2 - x1;
        double v = y2 - y1;
        double temp = u;
        u = -v;
        v = temp;
        double denom = sqrt(u * u + v * v);
        u = u / denom;
        v = v / denom;
        return y1 + d * v;
    }

    static double returnX4(double x1, double y1, double x2, double y2, double d) {
        double u = x2 - x1;
        double v = y2 - y1;
        double temp = u;
        u = -v;
        v = temp;
        double denom = sqrt(u * u + v * v);
        u = u / denom;
        v = v / denom;
        return x2 + d * u;
    }

    static double returnY4(double x1, double y1, double x2, double y2, double d) {
        double u = x2 - x1;
        double v = y2 - y1;
        double temp = u;
        u = -v;
        v = temp;
        double denom = sqrt(u * u + v * v);
        u = u / denom;
        v = v / denom;
        return y2 + d * v;
    }

    static double returnX5(double x1, double y1, double x2, double y2, double d) {
        double u = x2 - x1;
        double v = y2 - y1;
        double temp = u;
        u = v;
        v = -temp;
        double denom = sqrt(u * u + v * v);
        u = u / denom;
        v = v / denom;
        return x1 + d * u;
    }

    static double returnY5(double x1, double y1, double x2, double y2, double d) {
        double u = x2 - x1;
        double v = y2 - y1;
        double temp = u;
        u = v;
        v = -temp;
        double denom = sqrt(u * u + v * v);
        u = u / denom;
        v = v / denom;
        return y1 + d * v;
    }

    static double returnX6(double x1, double y1, double x2, double y2, double d) {
        double u = x2 - x1;
        double v = y2 - y1;
        double temp = u;
        u = v;
        v = -temp;
        double denom = sqrt(u * u + v * v);
        u = u / denom;
        v = v / denom;
        return x2 + d * u;
    }

    static double returnY6(double x1, double y1, double x2, double y2, double d) {
        double u = x2 - x1;
        double v = y2 - y1;
        double temp = u;
        u = v;
        v = -temp;
        double denom = sqrt(u * u + v * v);
        u = u / denom;
        v = v / denom;
        return y2 + d * v;
    }

    static double getDistance(double x1, double y1, double x2, double y2) {
        return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    static boolean doIntersect(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        Point2D.Double temp1 = new Point2D.Double(x1, y1);
        Point2D.Double temp2 = new Point2D.Double(x2, y2);
        Point2D.Double temp3 = new Point2D.Double(x3, y3);
        Point2D.Double temp4 = new Point2D.Double(x4, y4);
        boolean intersects = Line2D.linesIntersect(temp1.x, temp1.y, temp2.x, temp2.y, temp3.x, temp3.y, temp4.x, temp4.y);
        boolean shareAnyPoint = shareAnyPoint(temp1, temp2, temp3, temp4);
        return intersects && !shareAnyPoint;
    }

    private static boolean shareAnyPoint(Point2D.Double A, Point2D.Double B, Point2D.Double C, Point2D.Double D) {
        if (isPointOnTheLine(A, B, C)) {
            return true;
        } else if (isPointOnTheLine(A, B, D)) {
            return true;
        } else if (isPointOnTheLine(C, D, A)) {
            return true;
        } else {
            return isPointOnTheLine(C, D, B);
        }
    }

    private static boolean isPointOnTheLine(Point2D.Double A, Point2D.Double B, Point2D.Double P) {
        double m = (B.y - A.y) / (B.x - A.x);
        if (Double.isInfinite(m)) {
            return abs(A.x - P.x) < 0.001;
        }
        return abs((P.y - A.y) - m * (P.x - A.x)) < 0.001;
    }

    /*
        cycle, rickshaw, van, bike, car, car, car, CNG, bus, bus, truck, truck, train
     */
    static double getCarWidth(int type) {
        double[] carWidths = {0.55, 1.20, 1.22, 0.6, 1.7, 1.76, 1.78, 1.4, 2.4, 2.30, 2.44, 2.46,3};
        int index = abs(type) % min(Constants.TYPES_OF_CARS, carWidths.length);
        return carWidths[index];
    }

    static double getCarLength(int type) {
        double[] carLengths = {1.9, 2.4, 2.5, 1.8, 5, 4.55, 4.3, 2.65, 10.3, 9.5, 7.2, 7.5, 100};
        int index = abs(type) % min(Constants.TYPES_OF_CARS, carLengths.length);
        return carLengths[index];
    }

    //newly added
    static int getCarPassenger(int type) {   // to get passenger info
        Random random = new Random();

        ArrayList<Integer> passenger = new ArrayList<>();
        passenger.add(1); //cycle
        passenger.add(random.nextInt(3 - 1 + 1) + 1); // rickshaw
        passenger.add(random.nextInt(4 - 1 + 1) + 1); // van
        passenger.add(random.nextInt(2 - 1 + 1) + 1);  //bike
        passenger.add(random.nextInt(4 - 1 + 1) + 1);  // car
        passenger.add(random.nextInt(4 - 1 + 1) + 1);   //car
        passenger.add(random.nextInt(4 - 1 + 1) + 1);  //car
        passenger.add(random.nextInt(4 - 1 + 1) + 1);   //cng
        passenger.add(random.nextInt(40 - 5 + 1) + 5);  //bus
        passenger.add(random.nextInt(35 - 10 + 1) + 10);  //bus
        passenger.add(random.nextInt(5 - 1 + 1) + 1);  // truck
        passenger.add(random.nextInt(5 - 1 + 1) + 1);  //truck
        passenger.add(random.nextInt(200 - 40 + 1) + 40);  //train

        int index = abs(type) % min(Constants.TYPES_OF_CARS, passenger.size());
        return passenger.get(index);
    }

    //newly added by Shahrar

    static int getCarPassengersCount(int type) {   // to get passenger info
        Random random = new Random();
        int res = 0;
        switch (type) {
            case 0:
                res = random.nextInt(1) + 1;
                break;

            case 1:
                res = random.nextInt(3) + 1;
                break;

            case 3:
                res = random.nextInt(2) + 1;
                break;

            case 2:
            case 4:
            case 5:
            case 6:
            case 7://as cases are same
                res = random.nextInt(4) + 1;
                break;

            case 8:
                res = random.nextInt(36) + 5;
                break;

            case 9:
                res = random.nextInt(26) + 10;
                break;

            case 10:
            case 11: // as case 10 and 11 are same
                res = random.nextInt(5) + 1;
                break;

            case 12:
                res = random.nextInt(160) + 40;
                break;

        }

        return res;

    }



    /**
     * @param type of the car
     * @return the maximum speed of the car in m/s
     */
    static double getCarMaxSpeed(int type) {
        double[] carSpeeds = {15, 8, 7, 100, 100, 60, 110, 40, 30, 40, 35, 45,150}; // km/hour
        int index = abs(type) % min(Constants.TYPES_OF_CARS, carSpeeds.length);
        double speed = carSpeeds[index] * 1000 / 3600; // m/s
        return precision2(speed);
        /*double nd = random.nextGaussian();
        return precision2(speed + nd * speed / 10.0);*/
    }

    /**
     * @param type of the car
     * @return the acceleration in m/s^2
     */
    static double getCarAcceleration(int type) {
        // m/s^2;  approximately maxSpeed/10 i.e. takes 10 seconds to reach max speed
        double[] carAccelerations = {0.42, 0.25, 0.20, 2.80, 3, 2.80, 3.34, 1.11, 0.84, 1.12, 0.96, 1.24,2};
        int index = abs(type) % min(Constants.TYPES_OF_CARS, carAccelerations.length);
        return carAccelerations[index];
        /*double nd = random.nextGaussian();
        return precision2(carAccelerations[index] + nd * carAccelerations[index] / 10.0); // m/s^2*/
    }

    @SuppressWarnings("Duplicates")
    static test.Point2D getNewEndPointForIntersectionStrip(Vehicle vehicle, IntersectionStrip is, int newStripIndex) {
        test.Point2D endPoint;
        Segment enteringSegment = is.enteringSegment;
        double w1 = 1 * pixelPerFootpathStrip + (newStripIndex - 1) * pixelPerStrip;
        double x_1 = enteringSegment.getStartX() * pixelPerMeter;
        double y_1 = enteringSegment.getStartY() * pixelPerMeter;
        double x_2 = enteringSegment.getEndX() * pixelPerMeter;
        double y_2 = enteringSegment.getEndY() * pixelPerMeter;

        double x_3 = returnX3(x_1, y_1, x_2, y_2, w1);
        double y_3 = returnY3(x_1, y_1, x_2, y_2, w1);
        double x_4 = returnX4(x_1, y_1, x_2, y_2, w1);
        double y_4 = returnY4(x_1, y_1, x_2, y_2, w1);
        double dist1 = (x_3 - vehicle.getSegEndX() * pixelPerMeter) * (x_3 - vehicle.getSegEndX() * pixelPerMeter) + (y_3 - vehicle.getSegEndY() * pixelPerMeter) * (y_3 - vehicle.getSegEndY() * pixelPerMeter);
        double dist2 = (x_4 - vehicle.getSegEndX() * pixelPerMeter) * (x_4 - vehicle.getSegEndX() * pixelPerMeter) + (y_4 - vehicle.getSegEndY() * pixelPerMeter) * (y_4 - vehicle.getSegEndY() * pixelPerMeter);
        if (dist1 < dist2) {    // choosing closest segment end point from vehicle
            endPoint = new test.Point2D(x_3, y_3);
        } else {
            endPoint = new test.Point2D(x_4, y_4);
        }
        return endPoint;
    }

    static void drawTrace(LineNumberReader traceReader, Graphics g) {
        try {
            String s;
            do {
                s = traceReader.readLine();
            } while (s != null && !s.startsWith("Simulation"));
            /*
             here first line Current pedestrians
             then the pedestrians list
             then 1 line Current vehicles
             then the vehicles list
            */
            if (s != null && s.startsWith("Simulation")) {
                StringTokenizer tokenizer = new StringTokenizer(s);
                tokenizer.nextToken();
                int simStep = Integer.parseInt(tokenizer.nextToken());
                showProgressSlider.setValue(simStep);
            }
            traceReader.readLine();

            if (pedestrianMode) {
                while (true) {
                    s = traceReader.readLine();
                    if (s == null || s.startsWith("Current")) {
                        break;
                    }
                    StringTokenizer tokenizer = new StringTokenizer(s);
                    int x, y;
                    boolean inAccident;
                    x = Integer.parseInt(tokenizer.nextToken());
                    y = Integer.parseInt(tokenizer.nextToken());
                    inAccident = Boolean.parseBoolean(tokenizer.nextToken());
                    if (inAccident) {
                        g.setColor(Color.red);
                        g.fillOval(x, y, 10, 10);
                    } else {
                        g.setColor(Constants.pedestrianColor);
                        g.fillOval(x, y, 7, 7);
                    }
                }
            }
            // Lines after current vehicles
            while (true) {
                s = traceReader.readLine();
                if (s == null || s.startsWith("End")) {
                    break;
                }
                StringTokenizer tokenizer = new StringTokenizer(s);
                int[] xs = new int[4];
                int[] ys = new int[4];
                int red, green, blue;
                xs[0] = Integer.parseInt(tokenizer.nextToken());
                xs[1] = Integer.parseInt(tokenizer.nextToken());
                xs[3] = Integer.parseInt(tokenizer.nextToken());
                xs[2] = Integer.parseInt(tokenizer.nextToken());

                ys[0] = Integer.parseInt(tokenizer.nextToken());
                ys[1] = Integer.parseInt(tokenizer.nextToken());
                ys[3] = Integer.parseInt(tokenizer.nextToken());
                ys[2] = Integer.parseInt(tokenizer.nextToken());

                red = Integer.parseInt(tokenizer.nextToken());
                green = Integer.parseInt(tokenizer.nextToken());
                blue = Integer.parseInt(tokenizer.nextToken());
                Color c = new Color(red, green, blue);

                g.setColor(c);
                g.fillPolygon(xs, ys, 4);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static double precision2(double d) {
        return ((int) (d * 100)) / 100.0;
    }

    static double truncatedGaussian(double sigma, double lb, double ub) {
        double x;
        for (int i = 0; i < 5; i++) {
            x = random.nextGaussian() * sigma;
            if (lb <= x && x <= ub) {
                return x;
            }
        }
        return 0;
    }

    /**
     * @return a time penalty for collision in minutes following gamma
     * random variable
     */
    static double getCollisionPenalty() {
        return gb.sample();
    }
}
