package thesisfinal;

import java.awt.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Double.min;
import static java.lang.Math.*;
import static thesisfinal.Parameters.*;
import static thesisfinal.Utilities.getCarWidth;

public class Vehicle {

	private final int vehicleId;
	private int type;
	private double length;
	private double width;
	private int numberOfStrips;
	private double maximumSpeedCapable; // vehicle's max speed capability
	private double currentMaxSpeed; // max speed limit of the road/topology
	private double maxAcceleration;
	private double maxBraking;  // negative(must)
	private double speed;
	private double acceleration;
	private Color color;
	private int demandIndex;
	private int pathIndex;
	private int linkIndexOnPath;
	private Point2D[] corners;
	private Point2D segmentStartPoint;
	private Point2D segmentEndPoint;
	private boolean isInIntersection;
	private boolean reverseLink;
	private boolean reverseSegment;
	private boolean passedSensor;
	private boolean toRemove;
	private Link link;
	private int segmentIndex;
	private int stripIndex;
	private double distanceInSegment;   // tail of the vehicle is at distance in segment; so head = tail + length
	private Node node;
	private int intersectionStripIndex;
	private double distanceInIntersection;
	private Vehicle leader;
	/**
	 * statistics generation variables
	 */
	private final int startTime;
	private int endTime;
	private int segmentEnterTime;
	private int segmentLeaveTime;
	private int waitingTime; // time when the vehicle could not move
	private int waitingTimeInSegment;
	private double distanceTraveled;

	final static double MARGIN = 3; // margin from segment end in meter
	private final static double THRESHOLD_DISTANCE = 1.0; // default gap in meter between two standstill vehicles
	private final static double REACTION_TIME = 1.0; // second
	final static double TIME_STEP = 1.0; // second
	final static double SAFE_TIME_GAP = 1.0; // second
	final static double LAMBDA = 0.78; // lane changing gap acceptance parameter
	final static double ALPHA = 15; // sensitivity co-efficient for GHR/General Motors DLC model
	final static double M = 1; // speed exponent of GHR/General Motors DLC model
	final static double L = 2; // speed exponent of GHR/General Motors DLC model

	/**
	 * when this variable is true vehicles cannot overlap in node
	 * but if this variable is false the vehicle can make a jump to avoid deadlock in intersection
	 */
	private boolean noForceMove;
	/**
	 * this variable determines for how many seconds a variable is stuck in an intersection
	 */
	private int stuckInIntersection;

	public Vehicle(int vehicleId, int startTime, int type, double speed, Color color, int demandIndex, int pathIndex, int linkIndexOnPath, double segStartX, double segStartY, double segEndX, double segEndY, boolean reverseLink, boolean reverseSegment, Link link, int segmentIndex, int stripIndex) {
		this.vehicleId = vehicleId;
		this.startTime = startTime;
		this.segmentEnterTime = startTime;
		this.distanceTraveled = 0;
		this.type = type;

		this.length = Utilities.getCarLength(type);
		this.width = Utilities.getCarWidth(type);
		this.maximumSpeedCapable = Utilities.getCarMaxSpeed(type);
		this.currentMaxSpeed = min(maximumSpeed, maximumSpeedCapable);
		this.numberOfStrips = numberOfStrips(type);

		this.speed = speed;
		this.maxAcceleration = Utilities.getCarAcceleration(type);
		this.acceleration = this.maxAcceleration;
		this.maxBraking = -1.5 * maxAcceleration;
		this.color = color;
		this.demandIndex = demandIndex;
		this.pathIndex = pathIndex;
		this.linkIndexOnPath = linkIndexOnPath;
		segmentStartPoint = new Point2D(0, 0);
		segmentEndPoint = new Point2D(0, 0);
		this.segmentStartPoint.x = segStartX;
		this.segmentStartPoint.y = segStartY;
		this.segmentEndPoint.x = segEndX;
		this.segmentEndPoint.y = segEndY;

		this.isInIntersection = false;

		this.reverseLink = reverseLink;
		this.reverseSegment = reverseSegment;

		this.passedSensor = false;
		this.toRemove = false;

		this.noForceMove = true;
		this.stuckInIntersection = 0;

		this.link = link;
		this.segmentIndex = segmentIndex;
		this.stripIndex = stripIndex;
		this.distanceInSegment = 0.1;

		this.corners = new Point2D[4];
		occupyStrips();
		increaseVehicleCountOnSegment();
		getSegment().increaseEnteringVehicleCount();
	}

	/* new code roadside obj*/
	public Vehicle(int vehicleId, int startTime, int type, double speed, Color color,Link link, int segmentIndex,double initPos) {
		this.vehicleId = vehicleId;
		this.startTime = startTime;
		this.segmentEnterTime = startTime;
		this.distanceTraveled = 0;
		this.type = type;
		this.segmentIndex=segmentIndex;
		this.link=link;

		this.maximumSpeedCapable = 0;
		this.currentMaxSpeed = 0;
		this.numberOfStrips = numberOfStrips(type);

		this.speed = speed;
		this.maxAcceleration = 0;
		this.acceleration = 0;
		this.maxBraking = 0;
		this.color = color;
		segmentStartPoint = new Point2D(0, 0);
		segmentEndPoint = new Point2D(0, 0);


		this.isInIntersection = false;

//		this.reverseLink = reverseLink;
//		this.reverseSegment = reverseSegment;
//
//		this.passedSensor = false;
//		this.toRemove = false;
//
//		this.noForceMove = true;
//		this.stuckInIntersection = 0;
//
//		this.link = link;
//		this.segmentIndex = segmentIndex;
//		this.stripIndex = stripIndex;
		this.distanceInSegment = initPos;
//
//		this.corners = new Point2D[4];
		occupyStrips();
//		increaseVehicleCountOnSegment();
//		getSegment().increaseEnteringVehicleCount();
	}

	public int getVehicleId()
	{
		return vehicleId;
	}

	private void occupyStrips() {
		Segment segment = link.getSegment(segmentIndex);
		for (int i = 0; i < numberOfStrips; i++) {
			segment.getStrip(stripIndex + i).addVehicle(this);
		}
	}

	private void increaseVehicleCountOnSegment() {
		Segment segment = link.getSegment(segmentIndex);
		if (isReverseSegment()) {
			segment.increaseReverseVehicleCount();
		} else {
			segment.increaseForwardVehicleCount();
		}
	}

	void decreaseVehicleCountOnSegment() {
		Segment segment = link.getSegment(segmentIndex);
		if (isReverseSegment()) {
			segment.decreaseReverseVehicleCount();
		} else {
			segment.decreaseForwardVehicleCount();
		}
	}

	void freeStrips() {
		Segment segment = link.getSegment(segmentIndex);
		for (int i = stripIndex; i < stripIndex + numberOfStrips; i++) {
			segment.getStrip(i).delVehicle(this);
		}

	}

	@SuppressWarnings("Duplicates")
	private boolean moveToHigherIndexLane() {
		Segment segment = link.getSegment(segmentIndex);

		int limit = isReverseSegment() ? segment.lastVehicleStripIndex : segment.middleLowStripIndex;

		int storedStripIndex = stripIndex;

		for (int i = stripIndex; i + numberOfStrips <= limit; i++) {
			changeStrip(i, i + numberOfStrips, 1);
			if (segment.getStrip(i + numberOfStrips).hasGapForStripChange(this)) {
				//System.out.println(simulationStep+" id "+vehicleId+"  "+(i+numberOfStrips));
				if (isMoveForwardPossible() && !isSlowerVehicleInProximity()) {
					moveForwardInSegment();
					return true;
				}
			} else {
				freeStrips();
				stripIndex = storedStripIndex;
				occupyStrips();
				return false;
			}
		}

		return false;
	}

	@SuppressWarnings("Duplicates")
	private boolean moveToLowerIndexLane() {
		Segment segment = link.getSegment(segmentIndex);

		int limit = isReverseSegment() ? segment.middleHighStripIndex : 1;

		int storedStripIndex = stripIndex;

		for (int i = stripIndex; i > limit; i--) {
			changeStrip(i + numberOfStrips - 1, i - 1, -1);
			if (segment.getStrip(i - 1).hasGapForStripChange(this)) {
				if (isMoveForwardPossible() && !isSlowerVehicleInProximity()) {
					moveForwardInSegment();
					return true;
				}
			} else {
				freeStrips();
				stripIndex = storedStripIndex;
				occupyStrips();
				return false;
			}
		}

		return false;
	}

	/**
	 * Helper function to simplify changing strip
	 *
	 * @param removeFrom vehicle is removed from this strip
	 * @param addTo      vehicle is added to this strip
	 * @param deltaIndex change of index (+/-1)
	 */
	private void  changeStrip(int removeFrom, int addTo, int deltaIndex) {
		Segment segment = link.getSegment(segmentIndex);

		segment.getStrip(removeFrom).delVehicle(this);
		segment.getStrip(addTo).addVehicle(this);
		stripIndex = stripIndex + deltaIndex;
	}

	private double getNewDistanceInSegment() {
		return distanceInSegment + speed * TIME_STEP;
	}

	private double getSpeedForAcceleration() {
		double v_n_t = speed;
		double a_n_mx = maxAcceleration;
		double v_n_desired = currentMaxSpeed;
		return v_n_t + 2.5 * a_n_mx * REACTION_TIME * (1 - v_n_t / v_n_desired)
				* sqrt(0.025 + v_n_t / v_n_desired); // reaction time equals time step
	}

	static double getSpeedForBraking(Vehicle leader, Vehicle follower) {
		double Dx = getDx(leader, follower);
		return getSpeedForBraking(leader, follower, Dx);
	}

	static double getSpeedForBraking(Vehicle leader, Vehicle follower, double Dx) {
		double d_n_mx = follower.maxBraking;
		double d_T = d_n_mx * REACTION_TIME;
		double v_n_t = follower.speed;
		double v_n_1_t = leader.speed;
		double d_n_1 = getLeadersPerceivedDeceleration(leader, follower);
		return d_T + sqrt(d_T * d_T - d_n_mx * (2 * Dx - v_n_t * REACTION_TIME - (v_n_1_t * v_n_1_t) / d_n_1));
	}

	private double getNewSpeedGippsModel() {

		double v_a = getSpeedForAcceleration();
		double v_b;

		Vehicle leader = getProbableLeader();
		if (leader == null) {
			v_b = getSpeedForAcceleration();
		} else {
			v_b = getSpeedForBraking(leader, this);
		}

		return Utilities.precision2(max(0.0, min(v_a, v_b)));
	}

	/**
	 * @return new speed according to Gipp's model
	 */
	private double getNewSpeed() {
		switch (car_following_model) {
			case NAIVE_MODEL:
				return speed + maxAcceleration;
			case GIPPS_MODEL:
			case HYBRID_MODEL:
			default:
				return getNewSpeedGippsModel();
		}
	}

	void moveVehicleInSegment() {

		if (!moveForwardInSegment() || isSlowerVehicleInProximity()) {

			if (isReverseSegment()) {
				if (!moveToLowerIndexLane()) {
					moveToHigherIndexLane();
				}
			} else {
				if (!moveToHigherIndexLane()) {
					moveToLowerIndexLane();
				}
			}
		}
	}

	private boolean controlSpeedInSegment() {
		speed = getNewSpeed();
		if (speed > currentMaxSpeed) {
			speed = currentMaxSpeed;
		}
		checkCrashCondition(speed);
		if (car_following_model == CAR_FOLLOWING_MODEL.NAIVE_MODEL || car_following_model == CAR_FOLLOWING_MODEL.HYBRID_MODEL) {

			double gap = speed * TIME_STEP;
			for (int i = stripIndex; i < stripIndex + numberOfStrips; i++) {
				Segment segment = link.getSegment(segmentIndex);
				Strip strip = segment.getStrip(i);
				double stripGap = strip.getGapForForwardMovement(this);
				if (stripGap == 0) {
					return false;
				} else {
					gap = min(gap, stripGap);
				}
			}
			speed = gap / TIME_STEP;
			return true;
		} else if (car_following_model == CAR_FOLLOWING_MODEL.GIPPS_MODEL) {

			return speed > 0;
		} else {
			// should not come here; dummy return
			return true;
		}
	}

	private boolean isMoveForwardPossible() {
		double storeSpeed = speed;
		speed = getNewSpeed();
		if (speed > currentMaxSpeed) {
			speed = currentMaxSpeed;
		}
		if (car_following_model == CAR_FOLLOWING_MODEL.NAIVE_MODEL || car_following_model == CAR_FOLLOWING_MODEL.HYBRID_MODEL) {
			for (int i = stripIndex; i < stripIndex + numberOfStrips; i++) {
				Segment segment = link.getSegment(segmentIndex);
				Strip strip = segment.getStrip(i);
				double stripGap = strip.getGapForForwardMovement(this);
				if (stripGap == 0) {
					speed = storeSpeed;
					return false;
				}
			}
			speed = storeSpeed;
			return true;
		} else if (car_following_model == CAR_FOLLOWING_MODEL.GIPPS_MODEL) {
			boolean res = speed > 0;
			speed = storeSpeed;
			return res;
		} else {
			// should not come here; dummy return
			return true;
		}
	}

	void printVehicleDetails() {
		if (DEBUG_MODE) {
			if (vehicleId == 275 || vehicleId == 315) {
				String pathname = "debug" + vehicleId + ".txt";
				try (PrintWriter writer = new PrintWriter(new FileOutputStream(new File(pathname), true))) {
					writer.println("Sim step: " + simulationStep);
					writer.println("Vehicle ID: " + vehicleId);
					writer.printf("Speed: %.2f\n", speed);
					writer.printf("Acceleration: %.2f\n", acceleration);
					if (isInIntersection) {
						writer.println("Intersection Mode: " + true);
						writer.println("Intersection ID: " + node.getId());
						writer.printf("Distance in Intersection: %.2f\n", distanceInIntersection);
						writer.printf("Intersection Length: %.2f\n", node.getIntersectionStrip(intersectionStripIndex).getLength() / pixelPerMeter);
					} else {
						writer.println("Link ID: " + link.getId());
						writer.println("Segment Index: " + segmentIndex);
						writer.println("Strip Index: " + stripIndex);
						int leaderId = getProbableLeader() == null ? -1 : getProbableLeader().vehicleId;
						int followerId = getProbableFollower() == null ? -1 : getProbableFollower().vehicleId;
						writer.println("Leader: " + leaderId);
						writer.println("Follower: " + followerId);
						writer.printf("Distance in Segment: %.2f\n", distanceInSegment);
						writer.printf("Segment Length: %.2f\n", link.getSegment(segmentIndex).getLength());
						writer.printf("Segment Width: %.2f\n", link.getSegment(segmentIndex).getWidth());
						writer.println("Reverse Link: " + reverseLink);
						writer.println("Reverse Segment: " + reverseSegment);
					}
					writer.println();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

		}
	}
	/**
	 * @return the vehicle closest and in front of this among all the strips (null if none is in front of it)
	 */
	Vehicle getProbableLeader() {
		Vehicle leader = null;
		for (int i = stripIndex; i < stripIndex + numberOfStrips; i++) {
			Segment segment = link.getSegment(segmentIndex);
			Strip strip = segment.getStrip(i);
			Vehicle v = strip.probableLeader(this);
			if (v != null) {
				if (leader == null) {
					leader = v;
				} else {
					if (leader.getDistanceInSegment() > v.getDistanceInSegment()) {
						leader = v;
					}
				}
			}
		}
		return leader;
	}

	Vehicle getProbableFollower() {
		Vehicle follower = null;
		for (int i = stripIndex; i < stripIndex + numberOfStrips; i++) {
			Segment segment = link.getSegment(segmentIndex);
			Strip strip = segment.getStrip(i);
			Vehicle v = strip.probableFollower(this);
			if (v != null) {
				if (follower == null) {
					follower = v;
				} else {
					if (follower.getDistanceInSegment() + follower.length < v.getDistanceInSegment() + v.length) {
						follower = v;
					}
				}
			}
		}
		return follower;
	}

	private boolean isSlowerVehicleInProximity() {
		Vehicle leader = getProbableLeader();
		if (leader != null) {
			switch (lane_changing_model) {
				case NAIVE_MODEL:
					return leader.currentMaxSpeed < this.currentMaxSpeed && leader.getDistanceInSegment() < this.getDistanceInSegment() + length + getNewSpeed();
				case GHR_MODEL:
					return getAccelerationGHRModel(leader, this) < 0;
				case GIPPS_MODEL:
				default:
					return getDx(leader, this) < getDistanceForDesiredSpeed(leader, this);
			}

		} else {
			// no leader
			return false;
		}
	}

	/**
	 * Does not change the distance in intersection, just changes the speed if necessary.
	 *
	 * @return Whether movement is possible or not.
	 */
	@SuppressWarnings("Duplicates")
	private boolean controlSpeedInIntersection() {
		double storeDistanceInIntersection = distanceInIntersection;
		speed += acceleration;

		// this block is for making a small jump while deadlocked in intersection
		if (!noForceMove) {
			speed += length * 1.5;
		}

		if (speed > currentMaxSpeed) {
			speed = currentMaxSpeed;
		}
		double storeSpeed = speed;

		double tempSpeed = 0;
		tempSpeed += acceleration;
		boolean movementPossible = false;

		while (tempSpeed <= storeSpeed) {
			speed = tempSpeed;
			distanceInIntersection += tempSpeed;
			if (node.doOverlap(this) && noForceMove) {
				distanceInIntersection = storeDistanceInIntersection;
				tempSpeed -= acceleration;
				speed = tempSpeed;
				return movementPossible;
			}
			movementPossible = true;
			tempSpeed += acceleration;
			distanceInIntersection = storeDistanceInIntersection;
		}
		distanceInIntersection = storeDistanceInIntersection;
		return true;
	}

	private boolean moveForwardInSegment() {
		Segment segment = link.getSegment(segmentIndex);
		if (controlSpeedInSegment()) {

			distanceInSegment = getNewDistanceInSegment();
			if (distanceInSegment > segment.getLength() - length) { // distance in segment is the tail of the vehicle so vehicle length is subtracted from segment length
				distanceInSegment = segment.getLength() - length - (MARGIN - 0.1);   // here MARGIN is the margin, slightly less than the margin in isSegmentEnd function
			}
			if (!passedSensor) {
				if (distanceInSegment > segment.getSensor()) {
					passedSensor = true;
				}
			}

			return true;
		} else {
			waitingTime++;
			waitingTimeInSegment++;
			return false;
		}
	}

	private void printEndPoints(int index, Point2D p) {
		if (vehicleId == 86 || vehicleId == 186) {
			String pathname = "debug_ep" + vehicleId + ".txt";
			try (PrintWriter writer = new PrintWriter(new FileOutputStream(new File(pathname), true))) {
				writer.println("Index: " + index + " End Point: " + p.x + " " + p.y);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * try to move forward by changing the end point i.e. direction of the intersection strip
	 * @param newStripIndex calculate end point from this index on the entering segment of the intersection strip
	 * @return by changing into new direction whether we can move forward or not
	 */
	private boolean isChangeDirectionInIntersectionFruitful(int newStripIndex) {
		IntersectionStrip is = getCurrentIntersectionStrip();
		int storeNewStripIndex = is.endStrip;
		double storeEndPointX = is.endPointX;
		double storeEndPointY = is.endPointY;

		Point2D newEndPoint = Utilities.getNewEndPointForIntersectionStrip(this, is, newStripIndex);
		is.endPointX = newEndPoint.x;
		is.endPointY = newEndPoint.y;
		is.endStrip = newStripIndex;
		if (isMoveForwardPossibleInIntersection()) {
			return true;
		} else {
			is.endStrip = storeNewStripIndex;
			is.endPointX = storeEndPointX;
			is.endPointY = storeEndPointY;
			return false;
		}
	}


	/**
	 * Changes nothing. Just checks if move forward is possible. Similar to controlSpeedInIntersection
	 * @return move forward possible or not
	 */
	@SuppressWarnings("Duplicates")
	private boolean isMoveForwardPossibleInIntersection() {
		double storeDistanceInIntersection = distanceInIntersection;
		double storeInitialSpeed = speed;

		speed += acceleration;
		if (speed > currentMaxSpeed) {
			speed = currentMaxSpeed;
		}
		double storeSpeed = speed;

		double tempSpeed = 0;
		tempSpeed += acceleration;
		boolean movementPossible = false;

		while (tempSpeed <= storeSpeed) {
			speed = tempSpeed;
			distanceInIntersection += tempSpeed;
			if (node.doOverlap(this)) {
				distanceInIntersection = storeDistanceInIntersection;
				speed = storeInitialSpeed;
				return movementPossible;
			}
			movementPossible = true;
			tempSpeed += acceleration;
			distanceInIntersection = storeDistanceInIntersection;
		}
		distanceInIntersection = storeDistanceInIntersection;
		speed = storeInitialSpeed;
		return true;
	}

	/**
	 * try all the strips of the entering segment to enter when stuck in current direction
	 * returns whether it can move forward or not after changing direction
	 */
	private boolean tryChangingDirectionInIntersection() {
//        System.out.printf("Vehicle: %d stuck in node: %d at time %d %n", vehicleId, node.getId(), simulationStep);
		IntersectionStrip is = getCurrentIntersectionStrip();
		int beginLimit, endLimit;
		if (is.endStrip <= is.enteringSegment.middleLowStripIndex) {
			beginLimit = 1;
			endLimit = is.enteringSegment.middleLowStripIndex - (numberOfStrips - 1);
		} else {
			beginLimit = is.enteringSegment.middleHighStripIndex;
			endLimit = is.enteringSegment.lastVehicleStripIndex - (numberOfStrips - 1);
		}
		for (int i = beginLimit; i <= endLimit; i++) {
			if (isChangeDirectionInIntersectionFruitful(i)) {
				stuckInIntersection = 0;
				return true;
			}
		}
		return false;
	}

	private boolean isSlowerVehicleInProximityInIntersection() {
		double storeDistanceInIntersection = distanceInIntersection;
		distanceInIntersection += speed;
		if (node.doOverlap(this)) {
			// overlaps when moves forward in full speed; so vehicle in proximity
			Vehicle obstructingVehicle = node.getOverlappingVehicle(this);
			if (obstructingVehicle.speed < this.speed) {
				// obstructing vehicle is slower
				distanceInIntersection = storeDistanceInIntersection;
				return true;
			}
			// obstructing vehicle is not slow
		}
		// no vehicle in proximity
		distanceInIntersection = storeDistanceInIntersection;
		return false;
	}

	void moveVehicleInIntersection() {
		if (!moveForwardInIntersection() || isSlowerVehicleInProximityInIntersection()) {
			tryChangingDirectionInIntersection();
		}
	}

	private boolean moveForwardInIntersection() {
		if (controlSpeedInIntersection()) {
			distanceInIntersection += speed;
			double pseudoLength = node.getIntersectionStrip(intersectionStripIndex).getLength() / pixelPerMeter;
			if (distanceInIntersection > pseudoLength - length) {
				distanceInIntersection = pseudoLength - length - (MARGIN - 0.1);
			}
			stuckInIntersection = 0;
			noForceMove = true;
			return true;
		} else {
			stuckInIntersection++;
			noForceMove = stuckInIntersection <= 40;
			waitingTime++;
			return false;
		}
	}

	boolean isAtSegmentEnd() {
		Segment segment = link.getSegment(segmentIndex);
		return distanceInSegment + length + MARGIN >= segment.getLength();
	}

	boolean isAtIntersectionEnd() {
		IntersectionStrip intersectionStrip = node.getIntersectionStrip(intersectionStripIndex);
		return (distanceInIntersection + length + MARGIN) * pixelPerMeter >= intersectionStrip.getLength();
	}

	/**
	 * oldSegmentWidth != newSegmentWidth; So we need to calculate new strip index
	 *
	 * @param leavingSegment  = current/leaving segment
	 * @param enteringSegment = entering segment
	 * @param isReverse       = whether the entering segment is reverse or not
	 * @return strip index in the new link
	 */
	int getNewStripIndex(Segment leavingSegment, Segment enteringSegment, boolean isReverse) {
		int newStripIndex;
		int stripIndexForVehicle, oldLimit, newLimit;

		if (stripIndex >= leavingSegment.middleHighStripIndex) {
			stripIndexForVehicle = stripIndex - leavingSegment.middleHighStripIndex + 1;
			oldLimit = leavingSegment.lastVehicleStripIndex - leavingSegment.middleHighStripIndex + 1;
		} else {
			stripIndexForVehicle = stripIndex;
			oldLimit = leavingSegment.middleLowStripIndex;
		}

		if (isReverse) {
			newLimit = enteringSegment.lastVehicleStripIndex - enteringSegment.middleHighStripIndex + 1;
			newStripIndex = (int) round(1.0 * stripIndexForVehicle / oldLimit * newLimit);

			if (newStripIndex == 0) {
				newStripIndex++;
			}

			if (!isReverseSegment()) {
				newStripIndex = newLimit - newStripIndex + 1;
			}

			newStripIndex = enteringSegment.middleHighStripIndex + newStripIndex - 1;

			if (enteringSegment.lastVehicleStripIndex - newStripIndex + 1 < numberOfStrips) {
				newStripIndex = enteringSegment.lastVehicleStripIndex - numberOfStrips + 1;
			}

			if (!(newStripIndex >= enteringSegment.middleHighStripIndex && (newStripIndex + numberOfStrips - 1) <= enteringSegment.lastVehicleStripIndex)) {
				System.out.println("Reverse: >>>>>>>>>>>>>>>>>" + vehicleId);
				assert false;  // if the road is not wide enough then it can be here
			}
		} else {
			newLimit = enteringSegment.middleLowStripIndex;
			newStripIndex = (int) round(1.0 * stripIndexForVehicle / oldLimit * newLimit);

			if (newStripIndex == 0) {
				newStripIndex++;
			}

			if (isReverseSegment()) {
				newStripIndex = newLimit - newStripIndex + 1;
			}

			if (enteringSegment.middleLowStripIndex - newStripIndex + 1 < numberOfStrips) {
				newStripIndex = enteringSegment.middleLowStripIndex - numberOfStrips + 1;
			}

			if (!(newStripIndex >= 1 && (newStripIndex + numberOfStrips - 1) <= enteringSegment.middleLowStripIndex)) {
				System.out.println("Straight: >>>>>>>>>>>>>" + vehicleId);
				assert false; // if the road is not wide enough then it can be here
			}
		}

		return newStripIndex;
	}

	IntersectionStrip getCurrentIntersectionStrip() {
		return node.getIntersectionStrip(intersectionStripIndex);
	}

	double getThresholdDistance() {
		return THRESHOLD_DISTANCE + SAFE_TIME_GAP * speed; // here 1.8 is the desired time gap
	}

	static double getDx(Vehicle leader, Vehicle follower) {
		double x_n_1 = leader.distanceInSegment + leader.length;
		double x_n = follower.distanceInSegment + follower.length;
		double s_n_1 = leader.length + THRESHOLD_DISTANCE;
		//System.out.println("id "+follower.getVehicleId()+" "+leader.distanceInSegment+" "+leader.length);
		return x_n_1 - s_n_1 - x_n;
	}

	private static double getLeadersPerceivedDeceleration(Vehicle leader, Vehicle follower) {
		return (leader.maxBraking + follower.maxBraking) / 2.0;
	}

	/**
	 *
	 * @return this method returns the minimum distance between leader and follower to achieve desired speed of follower
	 */
	private static double getDistanceForDesiredSpeed(Vehicle leader, Vehicle follower) {
		double Vd = follower.currentMaxSpeed;
		double T = TIME_STEP;
		double d_n_mx = follower.maxBraking;
		double v_n = follower.speed;
		double v_n_1 = leader.speed;
		double d_n_1 = getLeadersPerceivedDeceleration(leader, follower);
		return Vd * T + (v_n * T + v_n_1 * v_n_1 / d_n_1 - Vd * Vd / d_n_mx) / 2.0;
	}

	static double getAccelerationGHRModel(Vehicle leader, Vehicle follower) {
		double v = follower.speed;
		double delV = leader.speed - follower.speed;
		double delX = getDx(leader, follower);
		return ALPHA * Math.pow(v, M) * delV / Math.pow(delX, L);
	}

	double getMaxBraking() {
		return maxBraking;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	int getNumberOfStrips() {
		return numberOfStrips;
	}

	public void setNumberOfStrips(int numberOfStrips) {
		this.numberOfStrips = numberOfStrips;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	int getDemandIndex() {
		return demandIndex;
	}

	public void setDemandIndex(int demandIndex) {
		this.demandIndex = demandIndex;
	}

	int getPathIndex() {
		return pathIndex;
	}

	public void setPathIndex(int pathIndex) {
		this.pathIndex = pathIndex;
	}

	int getLinkIndexOnPath() {
		return linkIndexOnPath;
	}

	public void setLinkIndexOnPath(int linkIndexOnPath) {
		this.linkIndexOnPath = linkIndexOnPath;
	}

	public double getSegStartX() {
		return segmentStartPoint.x;
	}

	public void setSegStartX(double segStartX) {
		this.segmentStartPoint.x = segStartX;
	}

	public double getSegStartY() {
		return segmentStartPoint.y;
	}

	public void setSegStartY(double segStartY) {
		this.segmentStartPoint.y = segStartY;
	}

	public double getSegEndX() {
		return segmentEndPoint.x;
	}

	public void setSegEndX(double segEndX) {
		this.segmentEndPoint.x = segEndX;
	}

	public double getSegEndY() {
		return segmentEndPoint.y;
	}

	void setSegEndY(double segEndY) {
		this.segmentEndPoint.y = segEndY;
	}

	boolean isInIntersection() {
		return isInIntersection;
	}

	void setInIntersection(boolean inIntersection) {
		this.isInIntersection = inIntersection;
	}

	boolean isReverseLink() {
		return reverseLink;
	}

	void setReverseLink(boolean reverseLink) {
		this.reverseLink = reverseLink;
	}

	boolean isReverseSegment() {
		return reverseSegment;
	}

	void setReverseSegment(boolean reverseSegment) {
		this.reverseSegment = reverseSegment;
	}

	boolean isPassedSensor() {
		return passedSensor;
	}

	public void setPassedSensor(boolean passedSensor) {
		this.passedSensor = passedSensor;
	}

	public boolean isToRemove() {
		return toRemove;
	}

	public void setToRemove(boolean toRemove) {
		this.toRemove = toRemove;
	}

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public int getSegmentIndex() {
		return segmentIndex;
	}

	public void setSegmentIndex(int segmentIndex) {
		this.segmentIndex = segmentIndex;
	}

	public int getStripIndex() {
		return stripIndex;
	}

	public double getDistanceInSegment() {
		return distanceInSegment;
	}

	public void setDistanceInSegment(double distanceInSegment) {
		this.distanceInSegment = distanceInSegment;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	int getIntersectionStripIndex() {
		return intersectionStripIndex;
	}

	void setIntersectionStripIndex(int intersectionStripIndex) {
		this.intersectionStripIndex = intersectionStripIndex;
	}

	public double getDistanceInIntersection() {
		return distanceInIntersection;
	}

	void setDistanceInIntersection(double distanceInIntersection) {
		this.distanceInIntersection = distanceInIntersection;
	}

	public Vehicle getLeader() {
		return leader;
	}

	public void setLeader(Vehicle leader) {
		this.leader = leader;
	}

	Point2D[] getCorners() {
		return corners;
	}

	void calculateCornerPoints() {
		double x1, x2, x3, x4;
		double y1, y2, y3, y4;
		double x_1 = node.getIntersectionStrip(intersectionStripIndex).startPointX;
		double x_2 = node.getIntersectionStrip(intersectionStripIndex).endPointX;
		double y_1 = node.getIntersectionStrip(intersectionStripIndex).startPointY;
		double y_2 = node.getIntersectionStrip(intersectionStripIndex).endPointY;
		double p_s_l = Math.hypot(x_1 - x_2, y_1 - y_2);
		double l = getLength() * pixelPerMeter;
		double d_i_j = distanceInIntersection * pixelPerMeter;
		double t1 = d_i_j / p_s_l;
		x1 = (1 - t1) * x_1 + t1 * x_2;
		y1 = (1 - t1) * y_1 + t1 * y_2;
		double t2 = (d_i_j + l) / p_s_l;
		x2 = (1 - t2) * x_1 + t2 * x_2;
		y2 = (1 - t2) * y_1 + t2 * y_2;
		double w = getWidth() * pixelPerMeter;
		x3 = Utilities.returnX3(x1, y1, x2, y2, w);
		y3 = Utilities.returnY3(x1, y1, x2, y2, w);
		x4 = Utilities.returnX4(x1, y1, x2, y2, w);
		y4 = Utilities.returnY4(x1, y1, x2, y2, w);
		corners[0] = new Point2D(x1, y1);
		corners[1] = new Point2D(x2, y2);
		corners[2] = new Point2D(x4, y4);
		corners[3] = new Point2D(x3, y3);
	}

	/**
	 * this function updates required statistical info before leaving a segment
	 */
	void updateSegmentLeavingData() {
		setSegmentLeaveTime(simulationStep);
		increaseTraveledDistance(getSegment().getLength());
		getSegment().updateAvgSpeedInSegment(getAvgSpeedInSegment());
		getSegment().increaseTotalWaitingTime(waitingTimeInSegment);
	}

	/**
	 * this function updates required statistical info before entering a segment
	 */
	void updateSegmentEnteringData() {
		waitingTimeInSegment = 0;
		setSegmentEnterTime(simulationStep);
		getSegment().increaseEnteringVehicleCount();
	}

	private double getAvgSpeedInSegment() {
		return getSegment().getLength() / (segmentLeaveTime - segmentEnterTime);
	}

	private void setSegmentEnterTime(int segmentEnterTime) {
		this.segmentEnterTime = segmentEnterTime;
	}

	private void setSegmentLeaveTime(int segmentLeaveTime) {
		this.segmentLeaveTime = segmentLeaveTime;
	}

	int getWaitingTime() {
		return waitingTime;
	}

	int getTravelTime() {
		if (endTime == 0) {
			endTime = simulationStep;
		}
		return endTime - startTime;
	}

	void setAvgTravelSpeed() {
		endTime = simulationStep;
		int travelTime = endTime - startTime;
		double avgSpeed = distanceTraveled / travelTime;
		Statistics.noOfVehicles[type]++;
		Statistics.avgSpeedOfVehicle[type] += avgSpeed;
		Statistics.waitingTime[type] += waitingTime;
		Statistics.totalTravelTime[type] += travelTime;
	}

	void increaseTraveledDistance(double amount) {
		distanceTraveled += amount;
	}

	private void checkCrashCondition(double currentSpeed) {
		Vehicle leader = getProbableLeader();
		if (leader != null) {
			double gap = leader.distanceInSegment - distanceInSegment - length;
			double leaderSpeed = leader.vehicleId < vehicleId ? leader.getSpeed() : leader.getNewSpeed();
			if (currentSpeed > leaderSpeed) {
				double TTC = gap / (currentSpeed - leaderSpeed);

				if (TTC <= TTC_THRESHOLD) {
					getSegment().increaseNearCrashCount();
				}
			}

		}
	}

	Segment getSegment() {
		return getLink().getSegment(segmentIndex);
	}

	void segmentChange(int segmentIndex, int stripIndex) {
		this.segmentIndex = segmentIndex;
		this.stripIndex = stripIndex;

		this.distanceInSegment = 0.1;
		this.passedSensor = false;
		occupyStrips();
		increaseVehicleCountOnSegment();
	}

	void linkChange(int linkIndexOnPath, Link link, int segmentIndex, int stripIndex) {
		this.linkIndexOnPath = linkIndexOnPath;
		this.link = link;
		segmentChange(segmentIndex, stripIndex);
	}


	static int numberOfStrips(int type) {
		return (int) ceil(getCarWidth(type) / stripWidth);
	}

	@SuppressWarnings("Duplicates")
	void drawVehicle(BufferedWriter traceWriter, Graphics g, double pixelPerStrip, double pixelPerMeter, double pixelPerFootpathStrip) {
		int x1, x2, x3, x4, y1, y2, y3, y4;
		int[] xs;
		int[] ys;

		if (isInIntersection) {
			IntersectionStrip intersectionStrip = node.getIntersectionStrip(intersectionStripIndex);
			double intersectionStripLength = intersectionStrip.getLength();

			int length = (int) (getLength() * pixelPerMeter);

			double dis = distanceInIntersection * pixelPerMeter;
			//Using internally section or ratio formula,it finds the coordinates along which vehicles are
			double xp = (dis * intersectionStrip.endPointX + (intersectionStripLength - dis) * intersectionStrip.startPointX) / intersectionStripLength;// * mpRatio;
			double yp = (dis * intersectionStrip.endPointY + (intersectionStripLength - dis) * intersectionStrip.startPointY) / intersectionStripLength;// * mpRatio;
			double xq = ((dis + length) * intersectionStrip.endPointX + (intersectionStripLength - (dis + length)) * intersectionStrip.startPointX) / intersectionStripLength;// * mpRatio;
			double yq = ((dis + length) * intersectionStrip.endPointY + (intersectionStripLength - (dis + length)) * intersectionStrip.startPointY) / intersectionStripLength;// * mpRatio;

			x1 = (int) Math.round(xp);
			y1 = (int) Math.round(yp);
			x2 = (int) Math.round(xq);
			y2 = (int) Math.round(yq);

			int width = (int) Math.round(this.width * pixelPerMeter);

			//finds the coordinates of perpendicularly opposite lower(right) points
			if (reverseSegment) {
				x3 = (int) Math.round(Utilities.returnX5(x1, y1, x2, y2, width));
				y3 = (int) Math.round(Utilities.returnY5(x1, y1, x2, y2, width));
				x4 = (int) Math.round(Utilities.returnX6(x1, y1, x2, y2, width));
				y4 = (int) Math.round(Utilities.returnY6(x1, y1, x2, y2, width));
			} else {
				x3 = (int) Math.round(Utilities.returnX3(x1, y1, x2, y2, width));
				y3 = (int) Math.round(Utilities.returnY3(x1, y1, x2, y2, width));
				x4 = (int) Math.round(Utilities.returnX4(x1, y1, x2, y2, width));
				y4 = (int) Math.round(Utilities.returnY4(x1, y1, x2, y2, width));
			}
			xs = new int[]{x1, x2, x4, x3};
			ys = new int[]{y1, y2, y4, y3};
		} else {

			Segment segment = link.getSegment(segmentIndex);
			double segmentLength = segment.getLength();
			double marginStrip = (getNumberOfStrips() * stripWidth - width) / 2.0;

			//--->strategy: if its source then change it to int right away
			//int length = (int) Math.ceil(getLength());
			//Using internally section or ratio formula,it finds the coordinates along which vehicles are
			double xp = (getDistanceInSegment() * segmentEndPoint.x + (segmentLength - getDistanceInSegment()) * segmentStartPoint.x) / segmentLength * pixelPerMeter;
			double yp = (getDistanceInSegment() * segmentEndPoint.y + (segmentLength - getDistanceInSegment()) * segmentStartPoint.y) / segmentLength * pixelPerMeter;
			double xq = ((getDistanceInSegment() + length) * segmentEndPoint.x + (segmentLength - (getDistanceInSegment() + length)) * segmentStartPoint.x) / segmentLength * pixelPerMeter;
			double yq = ((getDistanceInSegment() + length) * segmentEndPoint.y + (segmentLength - (getDistanceInSegment() + length)) * segmentStartPoint.y) / segmentLength * pixelPerMeter;

			double w = pixelPerFootpathStrip + (stripIndex - 1) * pixelPerStrip + marginStrip * pixelPerMeter;
			//finds the coordinates of vehicles starting and ending upper points depending on which strip their upper(left) portion are.
			if (reverseSegment) {
				x1 = (int) Math.round(Utilities.returnX5(xp, yp, xq, yq, w));
				y1 = (int) Math.round(Utilities.returnY5(xp, yp, xq, yq, w));
				x2 = (int) Math.round(Utilities.returnX6(xp, yp, xq, yq, w));
				y2 = (int) Math.round(Utilities.returnY6(xp, yp, xq, yq, w)); //(v.getStrip().getStripIndex())*stripPixelCount
			} else {
				x1 = (int) Math.round(Utilities.returnX3(xp, yp, xq, yq, w));
				y1 = (int) Math.round(Utilities.returnY3(xp, yp, xq, yq, w));
				x2 = (int) Math.round(Utilities.returnX4(xp, yp, xq, yq, w));
				y2 = (int) Math.round(Utilities.returnY4(xp, yp, xq, yq, w)); //(v.getStrip().getStripIndex())*stripPixelCount
			}
			int widthInPixel = (int) Math.round(this.width * pixelPerMeter);
			//finds the coordinates of perpendicularly opposite lower(right) points
			if (reverseSegment) {
				x3 = (int) Math.round(Utilities.returnX5(xp, yp, xq, yq, w + widthInPixel));
				y3 = (int) Math.round(Utilities.returnY5(xp, yp, xq, yq, w + widthInPixel));
				x4 = (int) Math.round(Utilities.returnX6(xp, yp, xq, yq, w + widthInPixel));
				y4 = (int) Math.round(Utilities.returnY6(xp, yp, xq, yq, w + widthInPixel));
			} else {
				x3 = (int) Math.round(Utilities.returnX3(xp, yp, xq, yq, w + widthInPixel));
				y3 = (int) Math.round(Utilities.returnY3(xp, yp, xq, yq, w + widthInPixel));
				x4 = (int) Math.round(Utilities.returnX4(xp, yp, xq, yq, w + widthInPixel));
				y4 = (int) Math.round(Utilities.returnY4(xp, yp, xq, yq, w + widthInPixel));
			}
			xs = new int[]{x1, x2, x4, x3};
			ys = new int[]{y1, y2, y4, y3};

		}
		g.setColor(color);
		g.fillPolygon(xs, ys, 4);
		if (DEBUG_MODE) {
			Font font = new Font("Serif", Font.PLAIN, 64);
			g.setFont(font);
			g.drawString(Integer.toString(vehicleId), x1, y1);
		}
		try {
			traceWriter.write(x1 + " " + x2 + " " + x3 + " " + x4 + " " + y1 + " " + y2 + " " + y3 + " " + y4 + " ");
			traceWriter.write(color.getRed() + " " + color.getBlue() + " " + color.getGreen());
			traceWriter.newLine();
		} catch (IOException ex) {
			Logger.getLogger(Vehicle.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
