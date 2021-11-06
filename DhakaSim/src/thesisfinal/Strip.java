package thesisfinal;

import java.util.ArrayList;
import java.util.Random;

public class Strip {

    private final int segmentIndex;
    private final int stripIndex;
    private final boolean isFootPathStrip;
    private static final Random rand = DhakaSimFrame.random;

    private final int parentLinkId;

    private final ArrayList<Vehicle> vehicleList = new ArrayList<>();
    private final ArrayList<Pedestrian> pedestrianList = new ArrayList<>();
    //Constructor sets segment index and strip index

    public Strip(int segIndex, int strIndex, boolean isFootPathStrip, int parentLinkId) {
        segmentIndex = segIndex;
        stripIndex = strIndex;
        this.isFootPathStrip = isFootPathStrip;
        this.parentLinkId = parentLinkId;
    }


    public boolean isFp() {
        return isFootPathStrip;
    }

    //gets strip index
    public int getStripIndex() {
        return stripIndex;
    }

    //adds vehicle to the strip's vehicle list when vehicle comes over the strip
    void addVehicle(Vehicle v) {
        if (parentLinkId != v.getLink().getId()) {
            System.out.println("Very big problem");
        }
        vehicleList.add(v);
    }

    //removes vehicle from the strip's vehicle list when vehicle lefts the strip
    void delVehicle(Vehicle v) {
        vehicleList.remove(v);
    }

    void addPedestrian(Pedestrian p) {
        pedestrianList.add(p);
    }

    void delPedestrian(Pedestrian p) {
        pedestrianList.remove(p);
    }

    //for a vehicle on this strip,finds another vehicle on the same strip with minimum distance ahead.
    Vehicle probableLeader(Vehicle follower) {
        double min = Double.MAX_VALUE;
        Vehicle ret = null;
        // as accidents can occur so getLength is omitted
        double distance = follower.getDistanceInSegment(); // + follower.getLength();
        for (Vehicle leader : vehicleList) {
            if (leader.getDistanceInSegment() > distance + 0.1) {
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

    //checks whether there is space for a vehicle to move forward without a collision
    //and keeping a threshold distance
    double getGapForForwardMovement(Vehicle v) {
        double forwardGap;
        double thresholdDistance = v.getThresholdDistance();
        double upperLimit, lowerLimit;

        boolean accident = false;

        Vehicle leader = probableLeader(v);
        if (leader == null) {
            forwardGap = v.getLink().getSegment(v.getSegmentIndex()).getLength() - v.getDistanceInSegment() - v.getLength();
        } else {
            forwardGap = Vehicle.getGap(leader, v);
        }

        if (v.isReverseSegment()) {
            lowerLimit = v.getDistanceInSegment() + v.getLength() + v.getSpeed() + thresholdDistance;
            upperLimit = v.getDistanceInSegment() + v.getLength();
        } else {
            upperLimit = v.getDistanceInSegment() + v.getLength() + v.getSpeed() + thresholdDistance;
            lowerLimit = v.getDistanceInSegment() + v.getLength();
        }

        if (v.isReverseSegment()) {
            double lower = lowerLimit;
            double upper = upperLimit;
            lowerLimit = v.getLink().getSegment(v.getSegmentIndex()).getLength() - upper;
            upperLimit = v.getLink().getSegment(v.getSegmentIndex()).getLength() - lower;
        }

        if (rand.nextInt() % (int) DhakaSimFrame.encounterPerAccident == 0) {
            accident = true;
        }
        ArrayList<Pedestrian> pedestriansToRemove = new ArrayList<>();
        for (Pedestrian pedestrian : pedestrianList) {
            double pedestrianPos = pedestrian.getInitPos();
            if (lowerLimit < pedestrianPos && pedestrianPos < upperLimit) {
                if (!accident) {
                    forwardGap = pedestrianPos - lowerLimit - 0.1; // 0.1 is a threshold
                } else {
                    pedestrian.getSegment().setAccidentCount(pedestrian.getSegment().getAccidentCount() + 1);//updateAccidentcount();
                    pedestrian.inAccident = true;
                    pedestriansToRemove.add(pedestrian);
                }
            }
        }
        pedestrianList.removeAll(pedestriansToRemove);
        return Math.max(forwardGap, 0);

    }

    boolean hasGapForPedestrian(Pedestrian p) {
        double lowerLimit, upperLimit, thresholdDistance = 0.08;
        Vehicle v;

        boolean accident = (rand.nextInt() % (int) DhakaSimFrame.encounterPerAccident == 0);
        for (Vehicle vehicle : vehicleList) {
            v = vehicle;
            upperLimit = v.getDistanceInSegment() + v.getLength() + thresholdDistance;
            lowerLimit = v.getDistanceInSegment();
            //____________________
            if (v.isReverseSegment()) {
                double lower = lowerLimit;
                double upper = upperLimit;
                lowerLimit = v.getLink().getSegment(v.getSegmentIndex()).getLength() - upper;
                upperLimit = v.getLink().getSegment(v.getSegmentIndex()).getLength() - lower;
            }
            //____________________
            if (lowerLimit < p.getInitPos() && p.getInitPos() < upperLimit) {
                if (!accident) {
                    return false;
                }  //System.out.println(lowerLimit + " " + obj.getInitPos() + " " + upperLimit);

                p.getSegment().setAccidentCount(p.getSegment().getAccidentCount() + 1);//updateAccidentcount();
                p.inAccident = true;
                delPedestrian(p);
            }
        }
        return true;
    }

    //checks whether there is adequate space for adding a new vehicle
    boolean hasGapForAddingVehicle(double vehicleLength) {
        /*
         * new vehicle enters if it has at least THRESHOLD DISTANCE gap after entering
         * with leader vehicle
         */
        double lowerLimit = 0.08;
        double upperLimit = Constants.THRESHOLD_DISTANCE + 0.08 + vehicleLength;
        boolean accident = (rand.nextInt() % (int) DhakaSimFrame.encounterPerAccident == 0);
        for (Vehicle vehicle : vehicleList) {
            if ((vehicle.getDistanceInSegment() < upperLimit
                    && vehicle.getDistanceInSegment() > lowerLimit)) {
                return false;
            }

        }

        ArrayList<Pedestrian> pedestriansToRemove = new ArrayList<>();
        for (Pedestrian pedestrian : pedestrianList) {
            double objpos = pedestrian.getInitPos();
            if (lowerLimit < objpos && objpos < upperLimit) {
                if (!accident) {
                    return false;
                }

                pedestrian.getSegment().setAccidentCount(pedestrian.getSegment().getAccidentCount() + 1);//updateAccidentcount();
                pedestrian.inAccident = true;
                pedestriansToRemove.add(pedestrian);
            }
        }
        pedestrianList.removeAll(pedestriansToRemove);
        return true;

    }

    /*similar to isGapForMoveForward but doesn't consider vehicle speed, checks whether there
     *is enough space for a given vehicle forward movement.
     */
    boolean hasGapForStripChange(Vehicle subjectVehicle, Vehicle leader, Vehicle follower) {
        double thresholdDistance = subjectVehicle.getThresholdDistance();
        double lowerLimit1 = subjectVehicle.getDistanceInSegment() - thresholdDistance;
        double upperLimit1 = subjectVehicle.getDistanceInSegment() + subjectVehicle.getLength() + thresholdDistance;

        for (Vehicle vehicle : vehicleList) {
            if (subjectVehicle == vehicle) {
                continue;
            }
            double lowerLimit2 = vehicle.getDistanceInSegment() - thresholdDistance;
            double upperLimit2 = vehicle.getDistanceInSegment()
                    + vehicle.getLength() + thresholdDistance;
            if ((lowerLimit1 >= lowerLimit2 && lowerLimit1 <= upperLimit2
                    || upperLimit1 >= lowerLimit2 && upperLimit1 <= upperLimit2)
                    || (lowerLimit2 >= lowerLimit1 && lowerLimit2 <= upperLimit1
                    || upperLimit2 >= lowerLimit1 && upperLimit2 <= upperLimit1)) {
                return false;
            }
        }
        //____________________
        if (subjectVehicle.isReverseSegment()) {
            double lower = lowerLimit1;
            double upper = upperLimit1;
            lowerLimit1 = subjectVehicle.getLink().getSegment(subjectVehicle.getSegmentIndex()).getLength() - upper;
            upperLimit1 = subjectVehicle.getLink().getSegment(subjectVehicle.getSegmentIndex()).getLength() - lower;
        }
        //____________________
        boolean accident = false;
        if (rand.nextInt() % (int) DhakaSimFrame.encounterPerAccident == 0) {
            accident = true;
        }
        ArrayList<Pedestrian> pedestriansToRemove = new ArrayList<>();
        for (Pedestrian pedestrian : pedestrianList) {
            double objpos = pedestrian.getInitPos();
            if (lowerLimit1 < objpos && objpos < upperLimit1) {
                if (!accident) {
                    return false;
                }

                pedestrian.getSegment().setAccidentCount(pedestrian.getSegment().getAccidentCount() + 1);
                pedestrian.inAccident = true;
                pedestriansToRemove.add(pedestrian);
            }
        }
        pedestrianList.removeAll(pedestriansToRemove);

        if (DhakaSimFrame.lane_changing_model == DhakaSimFrame.DLC_MODEL.NAIVE_MODEL) {
            return true;
        }
        if (DhakaSimFrame.lane_changing_model == DhakaSimFrame.DLC_MODEL.GIPPS_MODEL || DhakaSimFrame.lane_changing_model == DhakaSimFrame.DLC_MODEL.GHR_MODEL) {
            /*
             * checking for feasibility of changing lane
             */
            Vehicle targetLeader = subjectVehicle.getProbableLeader();
            Vehicle targetFollower = subjectVehicle.getProbableFollower();
        /*
         lane changing feasibility variable
         true means not feasible; false means feasible
         Gipp's method is used for computing feasibility using velocity
         */
            boolean isNotFeasible;
        /*
         gap acceptance probability calculation variable
         https://www.civil.iitb.ac.in/tvm/nptel/534_LaneChange/web/web.html#x1-50002.2
         this model is used for computing probability
         */
            double probabilty;
            if (targetLeader != null) {
                // lane changing feasibility calculation
                if (DhakaSimFrame.lane_changing_model == DhakaSimFrame.DLC_MODEL.GIPPS_MODEL) {
                    double speedWRTLeader = Vehicle.getSpeedForBraking(targetLeader, subjectVehicle);
                    double decelerationWRTLeader = (speedWRTLeader - subjectVehicle.getSpeed()) / Vehicle.TIME_STEP;
                    isNotFeasible = decelerationWRTLeader < subjectVehicle.getMaxBraking();
                } else if (DhakaSimFrame.lane_changing_model == DhakaSimFrame.DLC_MODEL.GHR_MODEL) {
                    isNotFeasible = Vehicle.getAccelerationGHRModel(targetLeader, subjectVehicle) < subjectVehicle.getMaxBraking();
                } else {
                    // dummy
                    assert false;
                    System.out.println("should not come here");
                    isNotFeasible = false;
                }

                // gap acceptance probability calculation
                double leadTimeGap = Vehicle.getGap(targetLeader, subjectVehicle) / subjectVehicle.getSpeed();
                if (leadTimeGap > Vehicle.SAFE_TIME_GAP) {
                    probabilty = 1 - Math.exp(-Vehicle.LAMBDA * (leadTimeGap - Vehicle.SAFE_TIME_GAP));
                } else {
                    probabilty = 0; // acceleration model is also incorporated
                }
            } else {
                isNotFeasible = false;
                probabilty = 1;
            }

            if (targetFollower != null) {
                // lane changing feasibility calculation

                double lagGap = Vehicle.getGap(subjectVehicle, targetFollower);
                double modifiedLagGap = lagGap + subjectVehicle.getSpeed() - targetFollower.getSpeed();
                double speedOfFollower = Vehicle.getSpeedForBraking(subjectVehicle, targetFollower, modifiedLagGap);

                if (DhakaSimFrame.lane_changing_model == DhakaSimFrame.DLC_MODEL.GIPPS_MODEL) {
                    double decelerationOfFollower = (speedOfFollower - targetFollower.getSpeed()) / Vehicle.TIME_STEP;
                    isNotFeasible = isNotFeasible || (decelerationOfFollower < targetFollower.getMaxBraking());
                } else if (DhakaSimFrame.lane_changing_model == DhakaSimFrame.DLC_MODEL.GHR_MODEL) {
                    isNotFeasible = isNotFeasible || (Vehicle.getAccelerationGHRModel(subjectVehicle, targetFollower) < targetFollower.getMaxBraking());
                }

                // gap acceptance probability calculation
                double lagTimeGap = modifiedLagGap / targetFollower.getSpeed();
                if (lagTimeGap > Vehicle.SAFE_TIME_GAP) {
                    probabilty *= 1 - Math.exp(-Vehicle.LAMBDA * (lagTimeGap - Vehicle.SAFE_TIME_GAP));
                } else {
                    probabilty = 0; // acceleration model is also incorporated
                }
            }

            if (isNotFeasible) {
                // lane changing not feasible
                return false;
            }
            /*
             * feasibility checking ended. after this lane changing is feasible
             */

            /*
             * check for gap acceptance (probabilistic method)
             */
            double r = rand.nextDouble();
            return r < probabilty;
        }

        if (DhakaSimFrame.lane_changing_model == DhakaSimFrame.DLC_MODEL.MOBIL_MODEL) {
            Vehicle target_follower = subjectVehicle.getProbableFollower();
            Vehicle target_leader = subjectVehicle.getProbableLeader(); // this was previously the leader of vehicle_n

            double b_safe = -4; // m/s^2
            double p = 1;
            double a_th = 0.1; // m/s^2

            double a_s = Vehicle.getIDMAcceleration(leader, subjectVehicle);
            double a_s_1 = Vehicle.getIDMAcceleration(subjectVehicle, follower);
            double a_n = Vehicle.getIDMAcceleration(target_leader, target_follower);

            double a_s_prime = Vehicle.getIDMAcceleration(target_leader, subjectVehicle);
            double a_s_1_prime = Vehicle.getIDMAcceleration(leader, follower);
            double a_n_prime = Vehicle.getIDMAcceleration(subjectVehicle, target_follower);

            boolean condition1 = a_n_prime >= b_safe;
            boolean condition2 = ((a_s_prime - a_s) + p * (a_n_prime - a_n + a_s_1_prime - a_s_1)) > a_th;

            return condition1 && condition2;
        }

        return true;
    }

}
