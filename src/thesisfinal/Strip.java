package thesisfinal;

import java.util.ArrayList;
import java.util.Random;

import static thesisfinal.Parameters.*;

public class Strip {

	private int segmentIndex;
	private int stripIndex;
	private boolean isFootPathStrip;
	private static Random rand = random;

	private int parentLinkId;

	private ArrayList<Vehicle> vehicleList = new ArrayList<>();
	private ArrayList<Object> objectList = new ArrayList<>();
	//Constructor sets segment index and strip index

	public Strip(int segIndex, int strIndex, boolean isFootPathStrip, int parentLinkId) {
		segmentIndex = segIndex;
		stripIndex = strIndex;
		this.isFootPathStrip = isFootPathStrip;
		this.parentLinkId = parentLinkId;
	}


	public boolean isFootPathStrip () {
		return isFootPathStrip;
	}

	//gets strip index
	public int getStripIndex() {
		return stripIndex;
	}

	/** Adds vehicle to the strip's vehicle list when vehicle comes over the strip
	 *
	 * @param vehicle vehicle to be added
	 */
	void addVehicle(Vehicle vehicle) {
		if (parentLinkId != vehicle.getLink().getId()) {
			System.out.println("Very big problem");
		}
		vehicleList.add(vehicle);
	}

	/** Removes vehicle from the strip's vehicle list when vehicle lefts the strip
	 *
	 * @param vehicle vehicle to be deleted
	 */
	void delVehicle(Vehicle vehicle) {
		vehicleList.remove(vehicle);
	}

	void addObject(Object object) {
		objectList.add(object);
	}

	void delObject(Object object) {
		objectList.remove(object);
	}

	void delAllObjects () {
		objectList.clear();
	}

	boolean doesHaveObject (Object object) {
		if (objectList.contains(object)) return true;
		else return false;
	}

	//for a vehicle on this strip,finds another vehicle on the same strip with minimum distance ahead.
	Vehicle probableLeader(Vehicle follower) {
		double min = Double.MAX_VALUE;
		Vehicle ret = null;
		double distance = follower.getDistanceInSegment() + follower.getLength();
		for (Vehicle leader : vehicleList) {
			if (leader.getDistanceInSegment() > distance + 0.1) {
				double compare = leader.getDistanceInSegment() - distance;
				if (compare < min) {
					min = compare;
					ret = leader;
				}
			}
		}

		// new code
		for(Object leader : objectList)  // checks if any roadside object is a probable leader
		{
			//System.out.println("simstep "+simulationStep+"speed "+leader.getSpeed());
			if (leader.getDistanceInSegment() > distance + 0.1 && leader.objectType!=0) {
				double compare = leader.getDistanceInSegment() - distance;
				if (compare < min) {
					min = compare;
					ret = leader;
				}
			}
		}
 	    return ret;
	}

	Vehicle probableFollower(Vehicle leader) {
		double min = Double.MAX_VALUE;
		Vehicle ret = null;
		for(Vehicle follower : vehicleList) {
			double distance = follower.getDistanceInSegment() + follower.getLength();
			if (leader.getDistanceInSegment() > distance + 0.1) {
				double compare = leader.getDistanceInSegment() - distance;
				if (compare < min) {
					min = compare;
					ret = follower;
				}
			}
		}
		return ret;
	}

	/**
	 * Checks whether there is space for a vehicle to move forward without a collision and keeping a threshold distance.
	 *
	 * @param v vehicle for which forward movement is being calculated
	 * @return gap for forward movement
	 */
	double getGapForForwardMovement(Vehicle v) {
		double forwardGap;
		double thresholdDistance = v.getThresholdDistance();
		double upperLimit, lowerLimit;

		boolean accident = false;

		Vehicle leader = probableLeader(v);
		if (leader == null) {
			forwardGap = v.getLink().getSegment(v.getSegmentIndex()).getLength() - v.getDistanceInSegment() - v.getLength();
		} else {
			forwardGap = Vehicle.getDx(leader, v);
		}

		if (v.isReverseSegment()) {
			lowerLimit = v.getDistanceInSegment() + v.getLength() + v.getSpeed() + thresholdDistance;
			upperLimit = v.getDistanceInSegment() + v.getLength();
		} else {
			upperLimit = v.getDistanceInSegment() + v.getLength() + v.getSpeed() + thresholdDistance;
			lowerLimit = v.getDistanceInSegment() + v.getLength();
		}

		if (v.isReverseSegment()) {
			double upper = upperLimit;
			double lower = lowerLimit;
			upperLimit = v.getLink().getSegment(v.getSegmentIndex()).getLength() - upper;
			lowerLimit = v.getLink().getSegment(v.getSegmentIndex()).getLength() - lower;
		}

		if (rand.nextInt() % (int) encounterPerAccident == 0) {
			accident = true;
		}
		ArrayList<Object> objectsToRemove = new ArrayList<>();
		for (Object object : objectList) {
			double objectPos = object.getInitPosOfStartingSide();
			double objectLength = object.getObjectLength();
			if (v.isReverseSegment()) {
				objectPos = objectPos+objectLength;
			}
			if (!(objectPos > upperLimit) && !(objectPos+objectLength < lowerLimit)) {
				if (!accident) {
					if (!v.isReverseSegment()) {
						forwardGap = objectPos - lowerLimit - 0.1; //0.1 is a threshold
					}
					else {
						forwardGap = upperLimit - objectPos - 0.1; //0.1 is a threshold
					}
				} else {
					object.getSegment().setAccidentCount(object.getSegment().getAccidentCount() + 1); //updateAccidentCount();
					//System.out.println("Type 1: সামনে থেকে অবজেক্টে ধাক্কা");
					object.inAccident = true;
					objectsToRemove.add(object);
				}
			}
		}
		objectList.removeAll(objectsToRemove);

		return Math.max(forwardGap, 0);
	}

	boolean hasGapForObject(Object p) {
		double lowerLimit, upperLimit, thresholdDistance = 0.08;
		Vehicle v;

		boolean accident = (rand.nextInt() % (int) encounterPerAccident == 0);
		for (Vehicle vehicle : vehicleList) {
			v = vehicle;
			upperLimit = v.getDistanceInSegment() + v.getLength() + thresholdDistance;
			lowerLimit = v.getDistanceInSegment();

			if (v.isReverseSegment()) {
				double lower = lowerLimit;
				double upper = upperLimit;
				lowerLimit = v.getLink().getSegment(v.getSegmentIndex()).getLength() - upper;
				upperLimit = v.getLink().getSegment(v.getSegmentIndex()).getLength() - lower;
			}

			if (lowerLimit < p.getInitPos() && p.getInitPos() < upperLimit) {
				if (!accident) {
					return false;
				}

				p.getSegment().setAccidentCount(p.getSegment().getAccidentCount() + 1); //updateAccidentcount();
				//System.out.println("Type 2: পথচারী নিজেই ধাক্কা লাগাইসে");
				p.inAccident = true;
				delObject(p);
			}
		}
		return true;
	}

	//checks whether there is adequate space for adding a new vehicle
	boolean hasGapForAddingVehicle(double vehicleLength) {

		double lowerLimit = 0.08;
		double upperLimit = 0.6 + vehicleLength;
		boolean accident = (rand.nextInt() % (int) encounterPerAccident == 0);
		for (Vehicle vehicle : vehicleList) {
			if ((vehicle.getDistanceInSegment() < upperLimit
					&& vehicle.getDistanceInSegment() > lowerLimit)) {
				return false;
			}

		}

		ArrayList<Object> objectsToRemove = new ArrayList<>();
		for (Object object : objectList) {
			double objpos = object.getInitPos();
			if (lowerLimit < objpos && objpos < upperLimit) {
				if (!accident) {
					return false;
				}

				object.getSegment().setAccidentCount(object.getSegment().getAccidentCount() + 1);//updateAccidentcount();
				//System.out.println("Type 3: নতুন গাড়ি এসে ধাক্কা দিসে");
				object.inAccident = true;
				objectsToRemove.add(object);
			}
		}
		objectList.removeAll(objectsToRemove);
		return true;

	}

	/*similar to isGapForMoveForward but doesn't consider vehicle speed, checks whether there
	 *is enough space for a given vehicle forward movement.
	 */
	boolean hasGapForStripChange(Vehicle v) {

		double thresholdDistance = v.getThresholdDistance();
		double lowerLimit1 = v.getDistanceInSegment() - thresholdDistance;
		double upperLimit1 = v.getDistanceInSegment() + v.getLength() + thresholdDistance;

		for (Vehicle vehicle : vehicleList) {
			if (v == vehicle) {
				continue;
			}
			double lowerLimit2 = vehicle.getDistanceInSegment() - thresholdDistance;
			double upperLimit2 = vehicle.getDistanceInSegment()
					+ vehicle.getLength() + thresholdDistance;
			if ((lowerLimit1 >= lowerLimit2 && lowerLimit1 <= upperLimit2
					|| upperLimit1 >= lowerLimit2 && upperLimit1 <= upperLimit2)
					|| (lowerLimit2 >= lowerLimit1 && lowerLimit2 <= upperLimit1
					|| upperLimit2 >= lowerLimit1 && upperLimit2 <= upperLimit1)) {
				//System.out.println("id "+v.getVehicleId()+" 1 "+vehicle.getVehicleId());
				return false;
			}
		}

		if (v.isReverseSegment()) {
			double lower = lowerLimit1;
			double upper = upperLimit1;
			lowerLimit1 = v.getLink().getSegment(v.getSegmentIndex()).getLength() - upper;
			upperLimit1 = v.getLink().getSegment(v.getSegmentIndex()).getLength() - lower;
		}

		boolean accident = false;
		if (rand.nextInt() % (int) encounterPerAccident == 0) {
			accident = true;
		}
		ArrayList<Object> objectsToRemove = new ArrayList<>();
		for (Object object : objectList) {
			double objectPos = object.getInitPos();
			double objectLength = object.getObjectLength();
			if (!(objectPos > upperLimit1) && !(objectPos+objectLength < lowerLimit1)) {
				if (!accident) {
					//System.out.println("id "+v.getVehicleId()+" 2");
					return false;
				}

				object.getSegment().setAccidentCount(object.getSegment().getAccidentCount() + 1);
				//System.out.println("Type 4: লেন পাল্টাতে গিয়ে ধাক্কা খাইসে");
				object.inAccident = true;
				objectsToRemove.add(object);
			}
		}
		objectList.removeAll(objectsToRemove);

		if (lane_changing_model == DLC_MODEL.NAIVE_MODEL) {
			return true;
		}
		if (lane_changing_model == DLC_MODEL.GIPPS_MODEL || lane_changing_model == DLC_MODEL.GHR_MODEL) {
			/*
			 * checking for feasibility of changing lane
			 */
			Vehicle targetLeader = v.getProbableLeader();
			Vehicle targetFollower = v.getProbableFollower();
        /*
         lane changing feasibility variable
         true means not feasible; false means feasible
         Gipp's method is used for computing feasibility using velocity
         */
			boolean flag;
        /*
         gap acceptance probability calculation variable
         https://www.civil.iitb.ac.in/tvm/nptel/534_LaneChange/web/web.html#x1-50002.2
         this model is used for computing probability
         */
			double probabilty;
			if (targetLeader != null) {
				// lane changing feasibility calculation
				if (lane_changing_model == DLC_MODEL.GIPPS_MODEL) {
					double speedWRTLeader = Vehicle.getSpeedForBraking(targetLeader, v);
					double decelerationWRTLeader = (speedWRTLeader - v.getSpeed()) / Vehicle.TIME_STEP;
					flag = decelerationWRTLeader < v.getMaxBraking();
				} else if (lane_changing_model == DLC_MODEL.GHR_MODEL) {
					flag = Vehicle.getAccelerationGHRModel(targetLeader, v) < v.getMaxBraking();
				} else {
					// dummy
					System.out.println("should not come here");
					flag = false;
				}

				// gap acceptance probability calculation
				double leadTimeGap = Vehicle.getDx(targetLeader, v) / v.getSpeed();
				if (leadTimeGap > Vehicle.SAFE_TIME_GAP) {
					probabilty = 1 - Math.exp(-Vehicle.LAMBDA * (leadTimeGap - Vehicle.SAFE_TIME_GAP));
				} else {
					probabilty = 0; // acceleration model is also incorporated
				}
			} else {
				flag = false;
				probabilty = 1;
			}

			if (targetFollower != null) {
				// lane changing feasibility calculation

				double lagGap = Vehicle.getDx(v, targetFollower);
				double modifiedLagGap = lagGap + v.getSpeed() - targetFollower.getSpeed();
				double speedOfFollower = Vehicle.getSpeedForBraking(v, targetFollower, modifiedLagGap);

				if (lane_changing_model == DLC_MODEL.GIPPS_MODEL) {
					double decelerationOfFollower = (speedOfFollower - targetFollower.getSpeed()) / Vehicle.TIME_STEP;
					flag = flag || (decelerationOfFollower < targetFollower.getMaxBraking());
				} else if (lane_changing_model == DLC_MODEL.GHR_MODEL) {
					flag = flag || (Vehicle.getAccelerationGHRModel(v, targetFollower) < targetFollower.getMaxBraking());
				}

				// gap acceptance probability calculation
				double lagTimeGap = modifiedLagGap / targetFollower.getSpeed();
				if (lagTimeGap > Vehicle.SAFE_TIME_GAP) {
					probabilty *= 1 - Math.exp(-Vehicle.LAMBDA * (lagTimeGap - Vehicle.SAFE_TIME_GAP));
				} else {
					probabilty = 0; // acceleration model is also incorporated
				}
			}

			if (flag) {
				// lane changing not feasible
				//System.out.println("id "+v.getVehicleId()+" 3");
				return false;
			}
			/*
			 * feasibility checking ended. after this lane changing is feasible
			 */

			/*
			 * check for gap acceptance (probabilistic method)
			 */
			double r = rand.nextDouble();
			//System.out.println("id "+v.getVehicleId()+" 4");
			//if(r>=probabilty) System.out.println(4);
			return r < probabilty;
		}

		//System.out.println("id "+v.getVehicleId()+" 5");
		return true;
	}

}
