package thesisfinal;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static thesisfinal.Constants.*;
import static thesisfinal.Parameters.*;

public class Object extends Vehicle{

	/**
	 * Current object types are:
	 * <ul>
	 *     <li>0 for road-crossing pedestrian</li>
	 *     <li>1 for standing pedestrian</li>
	 *     <li>2 for parked car</li>
	 *     <li>3 for parked rickshaw</li>
	 *     <li>4 for parked CNG</li>
	 * </ul>
	 */
	public int objectType;
	private Segment segment;
	private Strip strip;
	/**
	 * Equivalent to {@link Vehicle#getDistanceInSegment}.
	 */
	private double initPos;
	/**
	 * In case of circular objects, it is the diameter.
	 */
	private double objectLength;
	private double objectWidth;
	private double distanceFromFootpath;
	private double distanceAlongWidth;
	private double speed;
	private boolean reverseDirection;
	private double randomDouble;
	boolean inAccident = false;
	boolean toRemove = false;
	public int index;

	public Object (int objectType, Link link, Segment segment, int segmentId, double initPos, double speed, boolean reverseDirection) {


		super(Controller.vehicleId,simulationStep,0,0,Color.yellow,link,segmentId,initPos);
		//speed 0
		//assigning an id to roadside object too
		//initpos is basically distanceinsegment
		// there are other variables in vechicle that are not set in vehicle


		//Initialize parameters
		this.objectType = objectType;
		this.segment = segment;
		this.initPos = initPos;
		this.speed = speed;
		this.reverseDirection = reverseDirection;

		Random random = new Random();
		randomDouble = random.nextDouble();



		//Initialize other fields
		if (objectType==0) {
			objectLength = ROAD_CROSSING_PEDESTRIAN_SIZE;

			super.setLength(ROAD_CROSSING_PEDESTRIAN_SIZE);
			super.setWidth(ROAD_CROSSING_PEDESTRIAN_SIZE);

			objectWidth = ROAD_CROSSING_PEDESTRIAN_SIZE;
			distanceFromFootpath = -footpathStripWidth;
		}
		else if (objectType==1) {
			objectLength = STANDING_PEDESTRIAN_LENGTH;
			objectWidth = STANDING_PEDESTRIAN_WIDTH;

			super.setLength(objectLength);
			super.setWidth(objectWidth);

			if (OBJECTS_BLOCKAGE_DISTRIBUTION_IS_MULTIPLE_GAUSSIAN) {
				distanceFromFootpath = Utilities.getRandomFromMultipleGaussianDistributionOfObjectsBlockage(objectType) - objectWidth;
			}
			else if (OBJECTS_BLOCKAGE_DISTRIBUTION_IS_UNIFORM) {
				distanceFromFootpath = Utilities.getRandomFromUniformDistributionOfObjectsBlockage(objectType) - objectWidth;
			}
		}
		else if (objectType==2) {
			objectLength = PARKED_CAR_LENGTH;
			objectWidth = PARKED_CAR_WIDTH;

			super.setLength(objectLength);
			super.setWidth(objectWidth);

			if (OBJECTS_BLOCKAGE_DISTRIBUTION_IS_MULTIPLE_GAUSSIAN) {
				distanceFromFootpath = Utilities.getRandomFromMultipleGaussianDistributionOfObjectsBlockage(objectType) - objectWidth;
			}
			else if (OBJECTS_BLOCKAGE_DISTRIBUTION_IS_UNIFORM) {
				distanceFromFootpath = Utilities.getRandomFromUniformDistributionOfObjectsBlockage(objectType) - objectWidth;
			}
		}
		else if (objectType==3) {
			objectLength = PARKED_RICKSHAW_LENGTH;
			objectWidth = PARKED_RICKSHAW_WIDTH;

			super.setLength(objectLength);
			super.setWidth(objectWidth);

			if (OBJECTS_BLOCKAGE_DISTRIBUTION_IS_MULTIPLE_GAUSSIAN) {
				distanceFromFootpath = Utilities.getRandomFromMultipleGaussianDistributionOfObjectsBlockage(objectType) - objectWidth;
			}
			else if (OBJECTS_BLOCKAGE_DISTRIBUTION_IS_UNIFORM) {
				distanceFromFootpath = Utilities.getRandomFromUniformDistributionOfObjectsBlockage(objectType) - objectWidth;
			}
		}
		else if (objectType==4) {
			objectLength = PARKED_CNG_LENGTH;
			objectWidth = PARKED_CNG_WIDTH;

			super.setLength(objectLength);
			super.setWidth(objectWidth);

			if (OBJECTS_BLOCKAGE_DISTRIBUTION_IS_MULTIPLE_GAUSSIAN) {
				distanceFromFootpath = Utilities.getRandomFromMultipleGaussianDistributionOfObjectsBlockage(objectType) - objectWidth;
			}
			else if (OBJECTS_BLOCKAGE_DISTRIBUTION_IS_UNIFORM) {
				distanceFromFootpath = Utilities.getRandomFromUniformDistributionOfObjectsBlockage(objectType) - objectWidth;
			}
		}

//		Segment seg=segment;double segmentLength=seg.getLength();double distanceInSegment=initPos;double length=objectLength;
//		double xp = (distanceInSegment * segment.getEndX() + (segmentLength - distanceInSegment) * segment.getStartX()) / segmentLength * pixelPerMeter;
//		double yp = (distanceInSegment * segment.getEndY() + (segmentLength - distanceInSegment) * segment.getStartY()) / segmentLength * pixelPerMeter;
//		double xq = ((distanceInSegment + length) * segment.getEndX() + (segmentLength - (distanceInSegment + length)) * segment.getStartX()) / segmentLength * pixelPerMeter;
//		double yq = ((distanceInSegment + length) * segment.getEndY() + (segmentLength - (distanceInSegment + length)) * segment.getStartY()) / segmentLength * pixelPerMeter;



		distanceAlongWidth = distanceFromFootpath + footpathStripWidth;

		if (reverseDirection) {
			strip = segment.getStrip(segment.getNumberOfStrips() - 1);
		} else {
			strip = segment.getStrip(0);
		}

		occupyStrips();
	}

	@Override
	public double getDistanceInSegment() {
		return initPos;
		//return super.getDistanceInSegment();
	}

	protected double getDistanceAlongWidth() {
		return distanceAlongWidth;
	}

	protected double getDistanceFromFootpath() {
		return distanceFromFootpath;
	}

	double getInitPos() {
		return initPos;
	}

	double getInitPosOfStartingSide() {
		if (objectType==0 || objectType==1) {
			return initPos - objectLength /2;
		}
		else return initPos;
	}

	double getObjectLength () {
		return objectLength;
	}

	Segment getSegment() {
		return segment;
	}

	void moveForwardAndOccupyStrips() {
		//TODO Make objects occupy strips without calling this function. Probably another function for occupying strips?
		if (objectType==0) {
			if (!reverseDirection) {
				//When pedestrian is in footpath
				if (distanceAlongWidth + speed < footpathStripWidth) {
					distanceAlongWidth += speed;
					return;
				}
				//If s/he is out of footpath, then,
				int x;
				x = 1 + (int) ((distanceAlongWidth - footpathStripWidth + speed) / stripWidth);
				if (x < segment.getNumberOfStrips()) {
					if (segment.getStrip(x).hasGapForObject(this)) {
						distanceAlongWidth += speed;
						strip.delObject(this);
						setStrip(segment.getStrip(x));
						strip.addObject(this);
					}

				} else {
					distanceAlongWidth += speed;
					strip.delObject(this);
				}
			} else {
				int x;
				double w = 1 * footpathStripWidth + (segment.getNumberOfStrips() - 1) * stripWidth;
				x = 1 + (int) ((w - distanceAlongWidth - speed - footpathStripWidth) / stripWidth);
				if (x > 1) {
					if (segment.getStrip(x).hasGapForObject(this)) {
						distanceAlongWidth += speed;
						strip.delObject(this);
						setStrip(segment.getStrip(x));
						strip.addObject(this);
					}

				} else {
					distanceAlongWidth += speed;
					strip.delObject(this);
				}
			}
		}
	}

	private void occupyStrips() {
		double occupiedWidth = distanceFromFootpath + objectWidth;
		occupyStripsForRectangularObject(occupiedWidth);
	}

	private void occupyStripsForRectangularObject(double width) {
		//TODO Change this so that it occupies object's strips only
		int numberOfOccupiedStrips;
		if (width%stripWidth==0) {
			numberOfOccupiedStrips = (int) (width/stripWidth);
		}
		else {
			numberOfOccupiedStrips = (int) (width/stripWidth) + 1;
		}
		if (!reverseDirection) {
			for (int i=0; i<=numberOfOccupiedStrips; i++) { //Started from 0, as we want to occupy the footpath strip as well.
				segment.getStrip(i).addObject(this);
			}
		} else {
			for (int i=0; i<=numberOfOccupiedStrips; i++) { //Started from 0, as we want to occupy the footpath strip as well.
				segment.getStrip(segment.getNumberOfStrips()-1 - i).addObject(this);
			}
		}
	}

	public Strip getStrip() {
		return strip;
	}

	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	private void setStrip(Strip strip) {
		this.strip = strip;
	}

	boolean hasCrossedRoad() {
		return distanceAlongWidth >= segment.getWidth();
	}

	void drawObject (BufferedWriter traceWriter, Graphics2D g, double stripPixelCount, double mpRatio, double fpStripPixelCount) {
		if (objectType==0) {
			if (!reverseDirection) {
				Segment seg = getSegment();
				double segmentLength = seg.getLength();
				double length = 1;
				//Using internally section or ratio formula,it finds the coordinates along which vehicles are
				double xp = (getInitPos() * seg.getEndX() + (segmentLength - getInitPos()) * seg.getStartX()) / segmentLength * mpRatio;
				double yp = (getInitPos() * seg.getEndY() + (segmentLength - getInitPos()) * seg.getStartY()) / segmentLength * mpRatio;
				double xq = ((getInitPos() + length) * seg.getEndX() + (segmentLength - (getInitPos() + length)) * seg.getStartX()) / segmentLength * mpRatio;
				double yq = ((getInitPos() + length) * seg.getEndY() + (segmentLength - (getInitPos() + length)) * seg.getStartY()) / segmentLength * mpRatio;
				int x1 = (int) Math.round(Utilities.returnX3(xp, yp, xq, yq, (getDistanceAlongWidth() / footpathStripWidth) * fpStripPixelCount));
				int y1 = (int) Math.round(Utilities.returnY3(xp, yp, xq, yq, (getDistanceAlongWidth() / footpathStripWidth) * fpStripPixelCount));
				if (inAccident) {
					g.setColor(PEDESTRIAN_IN_ACCIDENT_COLOR);
					double radiusInPixel = PEDESTRIAN_IN_ACCIDENT_SIZE*pixelPerMeter/2;
					g.fillOval((int) (x1 - radiusInPixel), (int) (y1 - radiusInPixel), (int)(radiusInPixel*2), (int)(radiusInPixel*2));
				} else {
					g.setColor(ROAD_CROSSING_PEDESTRIAN_COLOR);
					double radiusInPixel = ROAD_CROSSING_PEDESTRIAN_SIZE *pixelPerMeter/2;
					g.fillOval((int) (x1 - radiusInPixel), (int) (y1 - radiusInPixel), (int)(radiusInPixel*2), (int)(radiusInPixel*2));
				}

				try {
					traceWriter.write(x1 + " " + y1 + " " + inAccident);
					traceWriter.newLine();
				} catch (IOException ex) {
					Logger.getLogger(Object.class.getName()).log(Level.SEVERE, null, ex);
				}

			}
			else {
				Segment seg = getSegment();
				double segmentLength = seg.getLength();
				double length = 1;
				//Using internally section or ratio formula,it finds the coordinates along which vehicles are
				double xp = (getInitPos() * seg.getEndX() + (segmentLength - getInitPos()) * seg.getStartX()) / segmentLength * mpRatio;
				double yp = (getInitPos() * seg.getEndY() + (segmentLength - getInitPos()) * seg.getStartY()) / segmentLength * mpRatio;
				double xq = ((getInitPos() + length) * seg.getEndX() + (segmentLength - (getInitPos() + length)) * seg.getStartX()) / segmentLength * mpRatio;
				double yq = ((getInitPos() + length) * seg.getEndY() + (segmentLength - (getInitPos() + length)) * seg.getStartY()) / segmentLength * mpRatio;
				double w = 1 * footpathStripWidth + (segment.getNumberOfStrips() - 1) * stripWidth;
				int wi = (int) (w - getDistanceAlongWidth());
				int x1 = (int) Math.round(Utilities.returnX3(xp, yp, xq, yq, (wi / footpathStripWidth) * fpStripPixelCount));
				int y1 = (int) Math.round(Utilities.returnY3(xp, yp, xq, yq, (wi / footpathStripWidth) * fpStripPixelCount));
				if (inAccident) {
					g.setColor(PEDESTRIAN_IN_ACCIDENT_COLOR);
					double radiusInPixel = PEDESTRIAN_IN_ACCIDENT_SIZE*pixelPerMeter/2;
					g.fillOval((int) (x1 - radiusInPixel), (int) (y1 - radiusInPixel), (int)(radiusInPixel*2), (int)(radiusInPixel*2));
				} else {
					g.setColor(ROAD_CROSSING_PEDESTRIAN_COLOR);
					double radiusInPixel = ROAD_CROSSING_PEDESTRIAN_SIZE *pixelPerMeter/2;
					g.fillOval((int) (x1 - radiusInPixel), (int) (y1 - radiusInPixel), (int)(radiusInPixel*2), (int)(radiusInPixel*2));
				}
				try {
					traceWriter.write(x1 + " " + y1 + " " + inAccident);
					traceWriter.newLine();
				} catch (IOException ex) {
					Logger.getLogger(Object.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		else if (objectType==1) {
			drawRectangularObject(g, STANDING_PEDESTRIAN_LENGTH, STANDING_PEDESTRIAN_WIDTH, STANDING_PEDESTRIAN_COLOR);
		}
		else if (objectType==2) {
			drawRectangularObject(g, PARKED_CAR_LENGTH, PARKED_CAR_WIDTH, PARKED_CAR_COLOR);
		}
		else if (objectType==3) {
			drawRectangularObject(g, PARKED_RICKSHAW_LENGTH, PARKED_RICKSHAW_WIDTH, PARKED_RICKSHAW_COLOR);
		}
		else if (objectType==4) {
			drawRectangularObject(g, PARKED_CNG_LENGTH, PARKED_CNG_WIDTH, PARKED_CNG_COLOR);
		}
	}

	private void drawRectangularObject(Graphics2D g, double length, double width, Color color) {
		int x1,y1,x2,y2,x3,y3,x4,y4;
		int[] xs;
		int[] ys;
		Segment segment = getSegment();
		double segmentLength = segment.getLength();
		double distanceInSegment = initPos;

		double xp = (distanceInSegment * segment.getEndX() + (segmentLength - distanceInSegment) * segment.getStartX()) / segmentLength * pixelPerMeter;
		double yp = (distanceInSegment * segment.getEndY() + (segmentLength - distanceInSegment) * segment.getStartY()) / segmentLength * pixelPerMeter;
		double xq = ((distanceInSegment + length) * segment.getEndX() + (segmentLength - (distanceInSegment + length)) * segment.getStartX()) / segmentLength * pixelPerMeter;
		double yq = ((distanceInSegment + length) * segment.getEndY() + (segmentLength - (distanceInSegment + length)) * segment.getStartY()) / segmentLength * pixelPerMeter;

		double w;
		int objectWidthInPixel = (int) Math.round(width * pixelPerMeter);
		if (!reverseDirection) {
			w = (distanceAlongWidth) * pixelPerMeter;
			x1 = (int) Math.round(Utilities.returnX3(xp, yp, xq, yq, w));
			y1 = (int) Math.round(Utilities.returnY3(xp, yp, xq, yq, w));
			x2 = (int) Math.round(Utilities.returnX4(xp, yp, xq, yq, w));
			y2 = (int) Math.round(Utilities.returnY4(xp, yp, xq, yq, w));
			x3 = (int) Math.round(Utilities.returnX3(xp, yp, xq, yq, w + objectWidthInPixel));
			y3 = (int) Math.round(Utilities.returnY3(xp, yp, xq, yq, w + objectWidthInPixel));
			x4 = (int) Math.round(Utilities.returnX4(xp, yp, xq, yq, w + objectWidthInPixel));
			y4 = (int) Math.round(Utilities.returnY4(xp, yp, xq, yq, w + objectWidthInPixel));
		}
		else {
			w = (segment.getWidth()-distanceAlongWidth-width) * pixelPerMeter;
			x1 = (int) Math.round(Utilities.returnX3(xp, yp, xq, yq, w));
			y1 = (int) Math.round(Utilities.returnY3(xp, yp, xq, yq, w));
			x2 = (int) Math.round(Utilities.returnX4(xp, yp, xq, yq, w));
			y2 = (int) Math.round(Utilities.returnY4(xp, yp, xq, yq, w));
			x3 = (int) Math.round(Utilities.returnX3(xp, yp, xq, yq, w + objectWidthInPixel));
			y3 = (int) Math.round(Utilities.returnY3(xp, yp, xq, yq, w + objectWidthInPixel));
			x4 = (int) Math.round(Utilities.returnX4(xp, yp, xq, yq, w + objectWidthInPixel));
			y4 = (int) Math.round(Utilities.returnY4(xp, yp, xq, yq, w + objectWidthInPixel));
		}
		xs = new int[]{x1, x2, x4, x3};
		ys = new int[]{y1, y2, y4, y3};
		g.setColor(color);
		g.fillPolygon(xs, ys, 4);
	}

	void printObject() {
		System.out.println(index + " " + initPos + " " + distanceAlongWidth + " " + speed);
	}

	public boolean isToRemove() {
		return toRemove;
	}

	boolean isInAccident() {
		return inAccident;
	}

	public boolean isReverseDirection () {
		return reverseDirection;
	}

	public void setToRemove(boolean b) {
		toRemove = b;
	}
}
