package thesisfinal;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Random;
import java.util.StringTokenizer;

import static java.lang.Integer.min;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static thesisfinal.Parameters.*;

class Utilities {

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

	/**
	 * The types are, respectively: cycle, rickshaw, van, bike, car, car, car, CNG, bus, bus, truck, truck
	 */
	static double getCarWidth(int type) {
		double[] carWidths = {0.55, 1.20, 1.22, 0.6, 1.7, 1.76, 1.78, 1.4, 2.4, 2.30, 2.44, 2.46};
		int index = abs(type) % min(Constants.TYPES_OF_CARS, carWidths.length);
		return carWidths[index];
	}

	/**
	 * The types are, respectively: cycle, rickshaw, van, bike, car, car, car, CNG, bus, bus, truck, truck
	 */
	static double getCarLength(int type) {
		double[] carLengths = {1.9, 2.4, 2.5, 1.8, 4.2, 4.55, 4.3, 2.65, 10.3, 9.5, 7.2, 7.5};
		int index = abs(type) % min(Constants.TYPES_OF_CARS, carLengths.length);
		return carLengths[index];
	}


	/**
	 * @param type of the car
	 * @return the maximum speed of the car in m/s
	 */
	static double getCarMaxSpeed(int type) {
		double[] carSpeeds = {15, 8, 7, 100, 80, 60, 110, 40, 30, 40, 35, 45}; // km/hour
		int index = abs(type) % min(Constants.TYPES_OF_CARS, carSpeeds.length);
		double speed = carSpeeds[index] * 1000 / 3600; // m/s
		double nd = random.nextGaussian();
		return precision2(speed + nd * speed / 10.0);
	}

	/**
	 * @param type of the car
	 * @return the acceleration in m/s^2
	 */
	static double getCarAcceleration(int type) {
		// m/s^2;  approximately maxSpeed/10 i.e. takes 10 seconds to reach max speed
		double[] carAccelerations = {0.42, 0.25, 0.20, 2.80, 2.22, 2.80, 3.34, 1.11, 0.84, 1.12, 0.96, 1.24};
		int index = abs(type) % min(Constants.TYPES_OF_CARS, carAccelerations.length);
		double nd = random.nextGaussian();
		return precision2(carAccelerations[index] + nd * carAccelerations[index] / 10.0); // m/s^2
	}

	@SuppressWarnings("Duplicates")
	static thesisfinal.Point2D getNewEndPointForIntersectionStrip(Vehicle vehicle, IntersectionStrip is, int newStripIndex) {
		thesisfinal.Point2D endPoint;
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
			endPoint = new thesisfinal.Point2D(x_3, y_3);
		} else {
			endPoint = new thesisfinal.Point2D(x_4, y_4);
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
             here first line Current objects
             then the objects list
             then 1 line Current vehicles
             then the vehicles list
            */
			if (s != null && s.startsWith("Simulation")) {
				StringTokenizer tokenizer = new StringTokenizer(s);
				tokenizer.nextToken();
				int simStep = Integer.parseInt(tokenizer.nextToken());
				DhakaSimFrame.showProgressSlider.setValue(simStep);
			}
			traceReader.readLine();

			if (objectMode) {
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
						g.setColor(Constants.ROAD_CROSSING_PEDESTRIAN_COLOR);
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
		return ((int)(d * 100)) / 100.0;
	}

	/**
	 * Concept taken from https://stackoverflow.com/a/13548135
	 * This function can be more soft-coded.
	 */
	static public double nextSkewedBoundedDoubleSample(double min, double max) {
		double range = max - min;
		double mid = min + range / 2.0;
		double skew = 1.5;
		double bias = -.35;
		double unitGaussian = new Random().nextGaussian();
		double biasFactor = Math.exp(bias);
		return mid+(range*(biasFactor/(biasFactor+Math.exp(-unitGaussian/skew))-0.5));
	}

	private static double getProbabilityDensityOfObjectBlockage (NormalDistribution[] distributions, double x) {
		double y = 0;
		for (NormalDistribution distribution : distributions) {
			y += distribution.getProbabilityDensityWithFactor(x);
		}
		return y;
	}

	/**
	 * Concept taken from: https://stackoverflow.com/a/5969719
	 */
	protected static double getRandomFromMultipleGaussianDistributionOfObjectsBlockage (int objectType) {
		double randomMultiplier = 0;
		NormalDistribution[] distributionsOfObject;

		double startIndex, stopIndex;
		switch (objectType) {
			case 1:
				distributionsOfObject = Constants.STANDING_PEDESTRIAN_DISTRIBUTIONS;
				startIndex = Constants.STANDING_PEDESTRIAN_MIN_BLOCKAGE;
				stopIndex = Constants.STANDING_PEDESTRIAN_MAX_BLOCKAGE;
				break;
			case 2:
				distributionsOfObject = Constants.PARKED_CAR_DISTRIBUTIONS;
				startIndex = Constants.PARKED_CAR_MIN_BLOCKAGE;
				stopIndex = Constants.PARKED_CAR_MAX_BLOCKAGE;
				break;
			case 3:
				distributionsOfObject = Constants.PARKED_RICKSHAW_DISTRIBUTIONS;
				startIndex = Constants.PARKED_RICKSHAW_MIN_BLOCKAGE;
				stopIndex = Constants.PARKED_RICKSHAW_MAX_BLOCKAGE;
				break;
			case 4:
				distributionsOfObject = Constants.PARKED_CNG_DISTRIBUTIONS;
				startIndex = Constants.PARKED_CNG_MIN_BLOCKAGE;
				stopIndex = Constants.PARKED_CNG_MAX_BLOCKAGE;
				break;
			default:
				System.out.println("Error: Distribution for objectType=" + objectType + " does not exist.");
				return 0; //Distance from footpath will be zero.
		}

		for (double i = startIndex; i<= stopIndex; i+=0.01) {
			randomMultiplier += getProbabilityDensityOfObjectBlockage(distributionsOfObject, i);
		}
		Random r = new Random();
		double randomDouble = r.nextDouble() * randomMultiplier;

		//For each possible integer return value, subtract yourFunction value for that possible return value till you get below 0.  Once you get below 0, return the current value.
		double theRandomNumber = startIndex;
		randomDouble = randomDouble - getProbabilityDensityOfObjectBlockage(distributionsOfObject, theRandomNumber);
		while (randomDouble >= 0) {
			theRandomNumber += 0.01;
			randomDouble = randomDouble - getProbabilityDensityOfObjectBlockage(distributionsOfObject, theRandomNumber);
		}

		return theRandomNumber;
	}

	protected static double getRandomFromUniformDistributionOfObjectsBlockage (int objectType) {
		double min, max, blockage;
		if (objectType==1) {
			min = Constants.STANDING_PEDESTRIAN_MIN_BLOCKAGE;
			max = Constants.STANDING_PEDESTRIAN_MAX_BLOCKAGE;
		}
		else if (objectType==2) {
			min = Constants.PARKED_CAR_MIN_BLOCKAGE;
			max = Constants.PARKED_CAR_MAX_BLOCKAGE;
		}
		else if (objectType==3) {
			min = Constants.PARKED_RICKSHAW_MIN_BLOCKAGE;
			max = Constants.PARKED_RICKSHAW_MAX_BLOCKAGE;
		}
		else if (objectType==4) {
			min = Constants.PARKED_CNG_MIN_BLOCKAGE;
			max = Constants.PARKED_CNG_MAX_BLOCKAGE;
		}
		else {
			return 0;
		}
		return min + new Random().nextDouble()*(max-min);
	}
}
