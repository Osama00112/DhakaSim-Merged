package thesisfinal;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

import static java.lang.Math.abs;
import static thesisfinal.Constants.*;
import static thesisfinal.LinkSegmentOrientation.getLinkAndSegmentOrientation;
import static thesisfinal.Network.*;
import static thesisfinal.Parameters.*;
import static thesisfinal.Utilities.*;

public class Controller {
	protected static LineNumberReader traceReader;
	protected static BufferedWriter traceWriter;

	private static ArrayList<Integer> nextGenerationTime = new ArrayList<>();
	private static ArrayList<Integer> numberOfVehiclesToGenerate = new ArrayList<>();

	protected static ArrayList<Object> objects = new ArrayList<>();
	protected static ArrayList<Vehicle> vehicleList = new ArrayList<>();

	private static int vehicleId = 0;
	private static int numberOfVehicles = 0;
	private static int numberOfObjects = 0;
	private static int numberOfRoadCrossingPedestrians = 0;
	private static int numberOfStandingPedestrians = 0;
	private static int numberOfParkedCars = 0;
	private static int numberOfParkedRickshaws = 0;
	private static int numberOfParkedCNGS = 0;

	public Controller () {
		try {
			if (TRACE_MODE) {
				traceReader = new LineNumberReader(new FileReader("trace.txt"));
				StringTokenizer tokenizer = new StringTokenizer(traceReader.readLine(), " ");
				simulationSpeed = Integer.parseInt(tokenizer.nextToken());
				simulationEndTime = Integer.parseInt(tokenizer.nextToken());
				objectMode = Boolean.parseBoolean(tokenizer.nextToken());
			} else {
				traceWriter = new BufferedWriter(new FileWriter("trace.txt", false));
				traceWriter.write(simulationSpeed + " " + simulationEndTime + " " + objectMode);
				traceWriter.newLine();
				traceWriter.flush();
			}
		} catch (IOException fe) {
			fe.printStackTrace();
		}

		Statistics s = new Statistics();
		readNetwork();
		readPath();
		readDemand();
		addPathToDemand();

		for (Demand demand1 : demandList) {
			nextGenerationTime.add(1);

			double demand = (double) demand1.getDemandValue();          //returns number of vehicle
			double demandRatio = 3600 / demand;

			if (demandRatio > 1) {
				numberOfVehiclesToGenerate.add(1);
			} else {
				numberOfVehiclesToGenerate.add((int) Math.round(1 / demandRatio));
				System.out.println("ADDED "+(int) Math.round(1 / demandRatio));
			}
		}
	}

	@SuppressWarnings("Duplicates")
	private static IntersectionStrip createIntersectionStrip (Vehicle vehicle, int oldLinkIndex, int newLinkIndex) {
		LinkSegmentOrientation linkSegmentOrientation = getLinkAndSegmentOrientation(vehicle.getSegEndX(), vehicle.getSegEndY(), linkList.get(newLinkIndex).getSegment(0), linkList.get(newLinkIndex).getSegment(linkList.get(newLinkIndex).getNumberOfSegments() - 1));

		Segment leavingSegment = vehicle.getLink().getSegment(vehicle.getSegmentIndex()); // leaving segment
		Segment enteringSegment = linkSegmentOrientation.reverseLink ? linkList.get(newLinkIndex).getLastSegment() : linkList.get(newLinkIndex).getFirstSegment(); // entering segment

		int oldStripIndex = vehicle.getStripIndex();
		int newStripIndex = vehicle.getNewStripIndex(leavingSegment, enteringSegment, linkSegmentOrientation.reverseSegment);

		double startPointX, startPointY, endPointX, endPointY;
		double w = 1 * pixelPerFootpathStrip + (vehicle.getStripIndex() - 1) * pixelPerStrip; //single direction so 1 footpath
		double x1 = leavingSegment.getStartX() * pixelPerMeter;
		double y1 = leavingSegment.getStartY() * pixelPerMeter;
		double x2 = leavingSegment.getEndX() * pixelPerMeter;
		double y2 = leavingSegment.getEndY() * pixelPerMeter;
		double x3 = returnX3(x1, y1, x2, y2, w);
		double y3 = returnY3(x1, y1, x2, y2, w);
		double x4 = returnX4(x1, y1, x2, y2, w);
		double y4 = returnY4(x1, y1, x2, y2, w);

		double dist1 = (x3 - vehicle.getSegEndX() * pixelPerMeter) * (x3 - vehicle.getSegEndX() * pixelPerMeter) + (y3 - vehicle.getSegEndY() * pixelPerMeter) * (y3 - vehicle.getSegEndY() * pixelPerMeter);
		double dist2 = (x4 - vehicle.getSegEndX() * pixelPerMeter) * (x4 - vehicle.getSegEndX() * pixelPerMeter) + (y4 - vehicle.getSegEndY() * pixelPerMeter) * (y4 - vehicle.getSegEndY() * pixelPerMeter);
		if (dist1 < dist2) {
			startPointX = x3;
			startPointY = y3;
		} else {
			startPointX = x4;
			startPointY = y4;
		}
		double w1;
		double x_1 = enteringSegment.getStartX() * pixelPerMeter;
		double y_1 = enteringSegment.getStartY() * pixelPerMeter;
		double x_2 = enteringSegment.getEndX() * pixelPerMeter;
		double y_2 = enteringSegment.getEndY() * pixelPerMeter;
		if (vehicle.isReverseSegment() == linkSegmentOrientation.reverseSegment) {
			w1 = 1 * pixelPerFootpathStrip + (vehicle.getNewStripIndex(leavingSegment, enteringSegment, linkSegmentOrientation.reverseSegment) - 1) * pixelPerStrip; //single direction so 1 footpath
		} else {
			w1 = 1 * pixelPerFootpathStrip + (vehicle.getNewStripIndex(leavingSegment, enteringSegment, linkSegmentOrientation.reverseSegment)) * pixelPerStrip; //single direction so 1 footpath
		}

		double x_3 = returnX3(x_1, y_1, x_2, y_2, w1);
		double y_3 = returnY3(x_1, y_1, x_2, y_2, w1);
		double x_4 = returnX4(x_1, y_1, x_2, y_2, w1);
		double y_4 = returnY4(x_1, y_1, x_2, y_2, w1);
		double dist3 = (x_3 - vehicle.getSegEndX() * pixelPerMeter) * (x_3 - vehicle.getSegEndX() * pixelPerMeter) + (y_3 - vehicle.getSegEndY() * pixelPerMeter) * (y_3 - vehicle.getSegEndY() * pixelPerMeter);
		double dist4 = (x_4 - vehicle.getSegEndX() * pixelPerMeter) * (x_4 - vehicle.getSegEndX() * pixelPerMeter) + (y_4 - vehicle.getSegEndY() * pixelPerMeter) * (y_4 - vehicle.getSegEndY() * pixelPerMeter);
		if (dist3 < dist4) {    // choosing closest segment end point from vehicle
			endPointX = x_3;
			endPointY = y_3;
		} else {
			endPointX = x_4;
			endPointY = y_4;
		}
		return new IntersectionStrip(oldLinkIndex, oldStripIndex, newLinkIndex, newStripIndex, startPointX, startPointY, endPointX, endPointY, leavingSegment, enteringSegment);
	}

	private static void createAVehicle (int demandIndex) {
		int numberOfPaths = demandList.get(demandIndex).getNumberOfPaths();
		for (int j = 0; j < numberOfVehiclesToGenerate.get(demandIndex); j++) {
			int pathIndex = randomVehiclePath(numberOfPaths);
			double speed = randomVehicleSpeed();
			int type = distributedVehicleType();
			double length = Utilities.getCarLength(type);
			int numberOfStrips = Vehicle.numberOfStrips(type);
			Path path = demandList.get(demandIndex).getPath(pathIndex);
			Node sourceNode = nodeList.get(path.getSource());
			Link link = linkList.get(path.getLink(0));

			LinkSegmentOrientation linkSegmentOrientation = getLinkAndSegmentOrientation(sourceNode.x, sourceNode.y, link.getFirstSegment(), link.getLastSegment());
			Segment segment = linkSegmentOrientation.reverseLink ? link.getLastSegment() : link.getFirstSegment();
			int start, end;
			if (linkSegmentOrientation.reverseSegment) {
				start = segment.middleHighStripIndex;
				end = segment.lastVehicleStripIndex;
				for (int desiredStripIndex = start; desiredStripIndex + numberOfStrips - 1 <= end; desiredStripIndex++) {
					boolean flag = true;
					for (int l = desiredStripIndex; l < desiredStripIndex + numberOfStrips; l++) {
						if (!segment.getStrip(l).hasGapForAddingVehicle(length)) {
							flag = false;
						}
					}
					if (flag) {
						Color color = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
						double startX, startY, endX, endY;
						if (linkSegmentOrientation.reverseSegment) {
							startX = segment.getEndX();
							startY = segment.getEndY();
							endX = segment.getStartX();
							endY = segment.getStartY();
						} else {
							startX = segment.getStartX();
							startY = segment.getStartY();
							endX = segment.getEndX();
							endY = segment.getEndY();
						}
						vehicleList.add(new Vehicle(vehicleId++, simulationStep, type, speed, color, demandIndex, pathIndex, 0, startX, startY, endX, endY, linkSegmentOrientation.reverseLink, linkSegmentOrientation.reverseSegment, link, segment.getIndex(), desiredStripIndex));
						//System.out.println("Generating vehicle #" + vehicleId + " in strip #" + desiredStripIndex + " with speed " + Utilities.precision2(speed) + " and length " + Utilities.getCarLength(type));
						break;
					}
				}
			} else {
				start = segment.middleLowStripIndex;
				end = 1;
				for (int desiredStripIndex = start; desiredStripIndex - numberOfStrips - 1 >= end; desiredStripIndex--) {
					boolean flag = true;
					for (int l = desiredStripIndex; l < desiredStripIndex + numberOfStrips; l++) {
						if (!segment.getStrip(l).hasGapForAddingVehicle(length)) {
							flag = false;
						}
					}
					if (flag) {
						Color color = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
						double startX, startY, endX, endY;
						if (linkSegmentOrientation.reverseSegment) {
							startX = segment.getEndX();
							startY = segment.getEndY();
							endX = segment.getStartX();
							endY = segment.getStartY();
						} else {
							startX = segment.getStartX();
							startY = segment.getStartY();
							endX = segment.getEndX();
							endY = segment.getEndY();
						}
						vehicleList.add(new Vehicle(vehicleId++, simulationStep, type, speed, color, demandIndex, pathIndex, 0, startX, startY, endX, endY, linkSegmentOrientation.reverseLink, linkSegmentOrientation.reverseSegment, link, segment.getIndex(), desiredStripIndex));
						//System.out.println("Generating vehicle #" + vehicleId + " in strip #" + desiredStripIndex + " with speed " + Utilities.precision2(speed) + " and length " + Utilities.getCarLength(type));
						break;
					}
				}
			}
		}
	}

	/**
	 * new vehicles are generated using negative exponential distribution
	 * https://www.civil.iitb.ac.in/tvm/nptel/535_TrSim/web/web.html#x1-70003
	 */
	private static void generateNewVehicles () {
		for (int i = 0; i < demandList.size(); i++) {
			if (nextGenerationTime.get(i) == simulationStep) {
				int demandValue = demandList.get(i).getDemandValue();
				double meanHeadway = 3600.0 / demandValue;
				int nextTime;
				do {
					if (numberOfVehicles < MAX_NUMBER_OF_VEHICLES) {
						createAVehicle(i);
						numberOfVehicles++;
					}

					double X = meanHeadway * -Math.log(random.nextDouble());
					nextTime = (int) Math.round(nextGenerationTime.get(i) + X);
					nextGenerationTime.set(i, nextTime);
				} while (nextTime == simulationStep);
			}
		}
	}

	private static int randomVehiclePath (int numberOfPaths) {
		return abs(random.nextInt()) % numberOfPaths;
	}

	private static double randomVehicleSpeed () {
		//return (random.nextInt(10) + 1); //Speed in 1 to 10
		return (random.nextDouble());
	}

	private int randomVehicleType() {
		// first 3 are human powered
		int ratio = random.nextInt(101);
		int type;
		if (ratio < slowVehiclePercentage) {
			type = random.nextInt(3); // first 3 are slow human powered vehicle
		} else if (ratio < slowVehiclePercentage + mediumVehiclePercentage) {
			type = random.nextInt(5); // types of medium speed vehicle = 5
			type += 7; // last
		} else {
			type = random.nextInt(4); // types of high speed vehicle = 4
			type += 3; // offset
		}

		return type;
	}

	private static int distributedVehicleType () {
		int ratio = random.nextInt(101);
		if (ratio < slowVehiclePercentage) {
			int ratioN = random.nextInt(101);
			if (ratioN < 9) {
				return 0; // bicycle
			} else if (ratioN < 98) {
				return 1; // rickshaw
			} else {
				return 2; // van/cart
			}
		} else if (ratio < slowVehiclePercentage + mediumVehiclePercentage) {
			int ratioN = random.nextInt(101);
			if (ratioN < 83) {
				return 7; // cng
			} else if (ratioN < 98) {
				return 8 + random.nextInt(2); // bus
			} else {
				return 10 + random.nextInt(2); // truck
			}
		} else {
			int ratioN = random.nextInt(101);
			if (ratioN < 88) {
				return 3; // bike
			} else {
				return 4 + random.nextInt(3); // car
			}
		}
	}

	private int distributedVehicleTypeMiami() {
		int ratio = random.nextInt(101);
		if (ratio < slowVehiclePercentage) {
			return 0; // always bicycle
		} else if (ratio < slowVehiclePercentage + mediumVehiclePercentage) {
			return 8 + random.nextInt(2); // always bus
		} else {
			int ratioN = random.nextInt(101);
			if (ratioN < 12) {
				return 3; // bike
			} else {
				return 4 + random.nextInt(3); // car
			}
		}
	}

	private static void removeOldVehicles () {
		ArrayList<Vehicle> vehiclesToRemove = new ArrayList<>();
		for (Vehicle vehicle : vehicleList) {
			if (vehicle.isToRemove()) {
				vehicle.freeStrips();
				vehiclesToRemove.add(vehicle);
				vehicle.setAvgTravelSpeed();
			}
		}
		numberOfVehicles = numberOfVehicles - vehiclesToRemove.size();
		vehicleList.removeAll(vehiclesToRemove);
	}

	@SuppressWarnings("Duplicates")
	private static void moveVehicleAtIntersectionEnd (Vehicle vehicle) {
		int demandIndex = vehicle.getDemandIndex();
		int pathIndex = vehicle.getPathIndex();
		int linkIndexOnPath = vehicle.getLinkIndexOnPath();

		int newLinkIndex = demandList.get(demandIndex).getPath(pathIndex).getLink(linkIndexOnPath + 1);
		Link link = linkList.get(newLinkIndex);

		Segment firstSegment = link.getFirstSegment();
		Segment lastSegment = link.getLastSegment();

		LinkSegmentOrientation linkSegmentOrientation = getLinkAndSegmentOrientation(vehicle.getSegEndX(), vehicle.getSegEndY(), firstSegment, lastSegment);

		Segment enteringSegment = linkSegmentOrientation.reverseLink ? lastSegment : firstSegment;

		int stripIndex = vehicle.getCurrentIntersectionStrip().endStrip;

		boolean flag = true;
		for (int j = 0; j < vehicle.getNumberOfStrips(); j++) {
			if (!enteringSegment.getStrip(stripIndex + j).hasGapForAddingVehicle(vehicle.getLength())) {
				flag = false;
				break;
			}
		}
		if (!flag) {
			// try to enter into a different strip
			int beginLimit, endLimit;

			if (stripIndex <= enteringSegment.middleLowStripIndex) {
				beginLimit = 1;
				endLimit = enteringSegment.middleLowStripIndex - (vehicle.getNumberOfStrips() - 1);
			} else {
				beginLimit = enteringSegment.middleHighStripIndex;
				endLimit = enteringSegment.lastVehicleStripIndex - (vehicle.getNumberOfStrips() - 1);
			}
			for (int i = beginLimit; i <= endLimit; i++) {
				flag = true;
				for (int j = 0; j < vehicle.getNumberOfStrips(); j++) {
					if (!enteringSegment.getStrip(i + j).hasGapForAddingVehicle(vehicle.getLength())) {
						flag = false;
						break;
					}
				}
				if (flag) {
					stripIndex = i;
					break;
				}
			}
		}

		if (flag) {
			//check this
			vehicle.getNode().removeVehicle(vehicle);
			vehicle.getNode().manualSignaling();
			vehicle.setNode(null);
			vehicle.increaseTraveledDistance(vehicle.getDistanceInIntersection() + vehicle.getLength() + Vehicle.MARGIN);
			vehicle.setDistanceInIntersection(0);

			vehicle.freeStrips();

			vehicle.setInIntersection(false);
			vehicle.setReverseLink(linkSegmentOrientation.reverseLink);
			vehicle.setReverseSegment(linkSegmentOrientation.reverseSegment);
			vehicle.linkChange(linkIndexOnPath + 1, link, enteringSegment.getIndex(), stripIndex);
			vehicle.updateSegmentEnteringData();
			if (linkSegmentOrientation.reverseSegment) {
				vehicle.setSegStartX(enteringSegment.getEndX());
				vehicle.setSegStartY(enteringSegment.getEndY());
				vehicle.setSegEndX(enteringSegment.getStartX());
				vehicle.setSegEndY(enteringSegment.getStartY());
			} else {
				vehicle.setSegStartX(enteringSegment.getStartX());
				vehicle.setSegStartY(enteringSegment.getStartY());
				vehicle.setSegEndX(enteringSegment.getEndX());
				vehicle.setSegEndY(enteringSegment.getEndY());
			}
		}
	}

	private static void moveVehicleAtSegmentMiddle (Vehicle vehicle) {
		boolean previous = vehicle.isPassedSensor();
		vehicle.moveVehicleInSegment();
		boolean now = vehicle.isPassedSensor();
		if (!previous && now) {
			vehicle.getLink().getSegment(vehicle.getSegmentIndex()).updateInformation(vehicle.getSpeed());
		}
	}

	@SuppressWarnings("Duplicates")
	private static void moveVehicleAtSegmentEnd (Vehicle vehicle) {
		if ((vehicle.isReverseLink() && vehicle.getLink().getSegment(vehicle.getSegmentIndex()).isFirstSegment())
				|| (!vehicle.isReverseLink() && vehicle.getLink().getSegment(vehicle.getSegmentIndex()).isLastSegment())) {
			//at link end
			int demandIndex = vehicle.getDemandIndex();
			int pathIndex = vehicle.getPathIndex();
			int pathLinkIndex = vehicle.getLinkIndexOnPath();
			int lastLinkInPathIndex = demandList.get(demandIndex).getPath(pathIndex).getNumberOfLinks() - 1;
			if (pathLinkIndex == lastLinkInPathIndex) {
				//at path end
				vehicle.setToRemove(true);

				vehicle.updateSegmentLeavingData();
			} else {
				//at path middle
				int oldLinkIndex = demandList.get(demandIndex).getPath(pathIndex).getLink(pathLinkIndex);
				int newLinkIndex = demandList.get(demandIndex).getPath(pathIndex).getLink(pathLinkIndex + 1);

				Node node = vehicle.isReverseLink() ? nodeList.get(vehicle.getLink().getUpNode()) : nodeList.get(vehicle.getLink().getDownNode());

				LinkSegmentOrientation linkSegmentOrientation = getLinkAndSegmentOrientation(vehicle.getSegEndX(), vehicle.getSegEndY(), linkList.get(newLinkIndex).getSegment(0), linkList.get(newLinkIndex).getSegment(linkList.get(newLinkIndex).getNumberOfSegments() - 1));

				Segment leavingSegment = vehicle.getLink().getSegment(vehicle.getSegmentIndex()); // leaving segment
				Segment enteringSegment = linkSegmentOrientation.reverseLink ? linkList.get(newLinkIndex).getSegment(linkList.get(newLinkIndex).getNumberOfSegments() - 1) : linkList.get(newLinkIndex).getSegment(0); // entering segment

				int oldStripIndex = vehicle.getStripIndex();
				int newStripIndex = vehicle.getNewStripIndex(leavingSegment, enteringSegment, linkSegmentOrientation.reverseSegment);

				if (!node.intersectionStripExists(oldLinkIndex, oldStripIndex, newLinkIndex, newStripIndex)) {

					node.addIntersectionStrip(createIntersectionStrip(vehicle, oldLinkIndex, newLinkIndex));
				}

				if (node.isBundleActive(oldLinkIndex)) {
					vehicle.setIntersectionStripIndex(node.getMyIntersectionStrip(oldLinkIndex, oldStripIndex, newLinkIndex, newStripIndex));
					vehicle.setNode(node);

					node.addVehicle(vehicle);
					vehicle.setDistanceInIntersection(0);
					vehicle.setInIntersection(true);
					if (node.doOverlap(vehicle)) {
						node.removeVehicle(vehicle);
						vehicle.setInIntersection(false);
					} else {
						// in next step vehicle will start moving in intersection; so here we update statistics
						vehicle.updateSegmentLeavingData();
					}
				}

			}
		} else {
			//at link middle
			Link link = vehicle.getLink();
			int currentSegmentIndex = vehicle.getSegmentIndex();

			Segment enteringSegment = vehicle.isReverseLink() ? link.getSegment(currentSegmentIndex - 1) : link.getSegment(currentSegmentIndex + 1);

			LinkSegmentOrientation linkSegmentOrientation = getLinkAndSegmentOrientation(vehicle.getSegEndX(), vehicle.getSegEndY(), enteringSegment, enteringSegment);

			int stripIndex = vehicle.getNewStripIndex(link.getSegment(currentSegmentIndex), enteringSegment, linkSegmentOrientation.reverseSegment);

			boolean flag = true;
			for (int j = 0; j < vehicle.getNumberOfStrips(); j++) {
				if (!enteringSegment.getStrip(stripIndex + j).hasGapForAddingVehicle(vehicle.getLength())) {
					flag = false;
				}
			}

			if (flag) {
				vehicle.freeStrips();
				vehicle.decreaseVehicleCountOnSegment();
				vehicle.updateSegmentLeavingData();

				vehicle.setReverseSegment(linkSegmentOrientation.reverseSegment);
				vehicle.segmentChange(enteringSegment.getIndex(), stripIndex);
				vehicle.updateSegmentEnteringData();

				if (linkSegmentOrientation.reverseSegment) {
					vehicle.setSegStartX(enteringSegment.getEndX());
					vehicle.setSegStartY(enteringSegment.getEndY());
					vehicle.setSegEndX(enteringSegment.getStartX());
					vehicle.setSegEndY(enteringSegment.getStartY());
				} else {
					vehicle.setSegStartX(enteringSegment.getStartX());
					vehicle.setSegStartY(enteringSegment.getStartY());
					vehicle.setSegEndX(enteringSegment.getEndX());
					vehicle.setSegEndY(enteringSegment.getEndY());
				}
			}
		}
	}

	private static void moveVehicles () {
		for (Vehicle vehicle : vehicleList) {
			System.out.print("");
			if (vehicle.isInIntersection()) {
				if (vehicle.isAtIntersectionEnd()) {
					moveVehicleAtIntersectionEnd(vehicle);
				} else {
					vehicle.moveVehicleInIntersection();
					if (vehicle.isAtIntersectionEnd()) {
						vehicle.printVehicleDetails();
						moveVehicleAtIntersectionEnd(vehicle);
					}
				}
			} else {
				if (vehicle.isAtSegmentEnd()) {
					moveVehicleAtSegmentEnd(vehicle);
				} else {
					moveVehicleAtSegmentMiddle(vehicle);
					if (vehicle.isAtSegmentEnd()) {
						vehicle.printVehicleDetails();
						moveVehicleAtSegmentEnd(vehicle);
					}
				}
			}
			vehicle.printVehicleDetails();
		}
	}

	private static void removeOldObjects () {
		ArrayList<Object> objectsToRemove = new ArrayList<>();
		for (Object object : objects) {
			//for road-crossing pedestrians
			if (object.isToRemove()) {
				objectsToRemove.add(object);
				numberOfRoadCrossingPedestrians--;
			}
			//for standing pedestrians
			if (object.objectType==1) {
				if (new Random().nextInt(STANDING_PEDESTRIAN_TIME_LIMIT_FACTOR)==0) {
					numberOfStandingPedestrians--;
					double width = object.getDistanceFromFootpath() + STANDING_PEDESTRIAN_WIDTH;
					removeRectangularObject(object, width);
					objectsToRemove.add(object);
				}
			}
			//for parked cars
			if (object.objectType==2) {
				if (new Random().nextInt(PARKED_CAR_TIME_LIMIT_FACTOR)==0) {
					numberOfParkedCars--;
					double width = object.getDistanceFromFootpath() + PARKED_CAR_WIDTH;
					removeRectangularObject(object, width);
					objectsToRemove.add(object);
				}
			}
			//for parked rickshaws
			if (object.objectType==3) {
				if (new Random().nextInt(PARKED_RICKSHAW_TIME_LIMIT_FACTOR)==0) {
					numberOfParkedRickshaws--;
					double width = object.getDistanceFromFootpath() + PARKED_RICKSHAW_WIDTH;
					removeRectangularObject(object, width);
					objectsToRemove.add(object);
				}
			}
			//for parked CNGs
			if (object.objectType==4) {
				if (new Random().nextInt(PARKED_CNG_TIME_LIMIT_FACTOR)==0) {
					numberOfParkedCNGS--;
					double width = object.getDistanceFromFootpath() + PARKED_CNG_WIDTH;
					removeRectangularObject(object, width);
					objectsToRemove.add(object);
				}
			}
		}
		numberOfObjects = numberOfObjects - objectsToRemove.size();
		objects.removeAll(objectsToRemove);
	}

	private static void removeRectangularObject(Object object, double width) {
		int numberOfOccupiedStrips;
		if (width%stripWidth==0) {
			numberOfOccupiedStrips = (int) (width/stripWidth);
		}
		else {
			numberOfOccupiedStrips = (int) (width/stripWidth) + 1;
		}
		if (!object.isReverseDirection()) {
			for (int i=0; i<=numberOfOccupiedStrips; i++) { //Started from 0, as footpath strip should be freed as well.
				object.getSegment().getStrip(i).delObject(object);
			}
		} else {
			for (int i=0; i<=numberOfOccupiedStrips; i++) { //Started from 0, as footpath strip should be freed as well.
				object.getSegment().getStrip(object.getSegment().getNumberOfStrips()-1 - i).delObject(object);
			}
		}
	}

	private static void generateNewObjects () {
		for (Link link : linkList) {
			double randomObjectProbability = random.nextDouble()* totalTypesOfObjects;
			int randomObjectType;
			if (numberOfObjects < MAX_NUMBER_OF_OBJECTS) {
				if (randomObjectProbability < roadCrossingPedestrianProbability && numberOfRoadCrossingPedestrians < MAX_NUMBER_OF_ROAD_CROSSING_PEDESTRIANS) {
					randomObjectType = 0; //Road-crossing Pedestrian
					numberOfRoadCrossingPedestrians++;
				}
				else if (randomObjectProbability>1 && randomObjectProbability < (1+standingPedestrianProbability) && numberOfStandingPedestrians < MAX_NUMBER_OF_STANDING_PEDESTRIANS) {
					randomObjectType = 1; //Standing Pedestrian
					numberOfStandingPedestrians++;
				}
				else if (randomObjectProbability>2 && randomObjectProbability < (2+parkedCarProbability) && numberOfParkedCars < MAX_NUMBER_OF_PARKED_CARS) {
					randomObjectType = 2; //Parked Car
					numberOfParkedCars++;
				}
				else if (randomObjectProbability>3 && randomObjectProbability < (3+parkedRickshawProbability) && numberOfParkedRickshaws < MAX_NUMBER_OF_PARKED_RICKSHAWS) {
					randomObjectType = 3; //Parked Rickshaw
					numberOfParkedRickshaws++;
				}
				else if (randomObjectProbability>4 && randomObjectProbability < (4+ parkedCNGProbability) && numberOfParkedCNGS < MAX_NUMBER_OF_PARKED_CNGS) {
					randomObjectType = 4; //Parked CNG
					numberOfParkedCNGS++;
				}
				else {
					continue; //An object is "no one".
				}
			}
			else {
				continue; //Max number of objects reached.
			}

			int randomSegmentID = Math.abs(random.nextInt()) % link.getNumberOfSegments();
			Segment randomSegment = link.getSegment(randomSegmentID);
			int min = 9;
			int max = (int) (randomSegment.getLength() - 9);
			if ((max - min) + 1 <= 0) {
				continue;
			}
			int randomInitPos = random.nextInt((max - min) + 1) + min;
			double randomObjectSpeed = Math.abs(random.nextInt()) % 2 + 0.5;
			boolean reverseDirection = random.nextBoolean();
			if (reverseDirection) {
				if (!randomSegment.getStrip(randomSegment.getNumberOfStrips()-1).hasGapForAddingVehicle(PARKED_CAR_LENGTH)) {
					continue; //There's no gap for adding parked car
				}
			} else {
				if (!randomSegment.getStrip(0).hasGapForAddingVehicle(PARKED_CAR_LENGTH)) {
					continue; //There's no gap for adding parked car
				}
			}
			Object object = new Object(randomObjectType, randomSegment, randomInitPos, randomObjectSpeed, reverseDirection);
			objects.add(object);
			numberOfObjects++;
		}
	}

	private static void moveObjects () {
		for (Object object : objects) {
			if (object.hasCrossedRoad() || object.isInAccident()) {
				object.setToRemove(true);
			} else {
				object.moveForwardAndOccupyStrips();
			}
		}
	}

	private static void printData (double[] data, String filename) {
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(new File(filename), true))) {
			for (double d :
					data) {
				writer.printf("%.2f,", d);
			}
			writer.println();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected static void generateStatistics () {
		double[] sensorVehicleCount = new double[linkList.size()];
		double[] sensorVehicleAvgSpeed = new double[linkList.size()];
		double[] accidentCount = new double[linkList.size()];
		double[] nearCrashCount = new double[linkList.size()];
		double[] avgSpeedInLink = new double[linkList.size()];
		double[] avgWaitingTimeInLink = new double[linkList.size()]; // in percentage

		int index, index2;

		//For each link--
		for (index = 0; index < linkList.size(); index++) {
			int waitingInSegment = 0, leaving = 0;
			//For each segment of that link--
			for (index2 = 0; index2 < linkList.get(index).getNumberOfSegments(); index2++) {
				sensorVehicleCount[index] += linkList.get(index).getSegment(index2).getSensorVehicleCount();
				sensorVehicleAvgSpeed[index] += linkList.get(index).getSegment(index2).getSensorVehicleAvgSpeed() * 3600.0 / 1000;
				accidentCount[index] += linkList.get(index).getSegment(index2).getAccidentCount();
				nearCrashCount[index] += linkList.get(index).getSegment(index2).getNearCrashCount();
				avgSpeedInLink[index] += linkList.get(index).getSegment(index2).getAvgSpeedInSegment();
				waitingInSegment += linkList.get(index).getSegment(index2).getTotalWaitingTime();
				leaving += linkList.get(index).getSegment(index2).getLeavingVehicleCount();
			}
			// this means the flow rate on a link (vehicle/hour)
			sensorVehicleCount[index] = (int) Math.round(1.0 * sensorVehicleCount[index] / index2 * 3600 / simulationEndTime);

			sensorVehicleAvgSpeed[index] /= index2;
			avgSpeedInLink[index] /= index2;
			avgWaitingTimeInLink[index] = leaving > 0 ? 1.0 * waitingInSegment / leaving : 0;
		}

		//plots graph
        /*SensorVehicleCountPlot p1 = new SensorVehicleCountPlot(sensorVehicleCount);
        SensorVehicleAvgSpeedPlot p2 = new SensorVehicleAvgSpeedPlot(sensorVehicleAvgSpeed);
        AccidentCountPlot p3 = new AccidentCountPlot(accidentCount);*/

		printData(sensorVehicleCount, "flow_rate.csv");
		printData(sensorVehicleAvgSpeed, "sensor_speed.csv");
		printData(accidentCount, "accident.csv");
		printData(nearCrashCount, "near_crash.csv");
		printData(avgSpeedInLink, "avg_speed_link.csv");
		printData(avgWaitingTimeInLink, "avg_waiting_link.csv");

		double[] percentageOfWaiting = new double[TYPES_OF_CARS];
		for (Vehicle vehicle : vehicleList) {
			if (vehicle.isInIntersection()) {
				vehicle.increaseTraveledDistance(vehicle.getDistanceInIntersection());
			} else {
				vehicle.increaseTraveledDistance(vehicle.getDistanceInSegment());
			}

			Statistics.totalTravelTime[vehicle.getType()] += vehicle.getTravelTime();
			Statistics.waitingTime[vehicle.getType()] += vehicle.getWaitingTime();
			vehicle.setAvgTravelSpeed();
		}

		for (int i = 0; i < TYPES_OF_CARS; i++) {
			if (Statistics.noOfVehicles[i] != 0) {
				Statistics.avgSpeedOfVehicle[i] /= Statistics.noOfVehicles[i];
				Statistics.avgSpeedOfVehicle[i] *= 3.6;
				percentageOfWaiting[i] = 100.0 * Statistics.waitingTime[i] / Statistics.totalTravelTime[i];
			}
		}

		printData(Statistics.avgSpeedOfVehicle, "avg_speed_vehicle.csv");
		printData(percentageOfWaiting, "waiting_percentage_vehicle.csv");

		totalAccCount(accidentCount);
	}

	private static void totalAccCount (double[] acc) {
		double sum = 0;
		for (double v : acc) {
			sum += v;
		}
		//System.out.println("Total number of accidents: " + sum);
	}

	static LineNumberReader getTraceReader () {
		return traceReader;
	}

	static void setTraceReader (LineNumberReader traceReaderToSet) {
		traceReader = traceReaderToSet;
	}

	private static void controlSignal () {
		for (Node node : Network.intersectionList) {
			node.automaticSignaling(simulationStep);
		}
	}

	protected static void run() throws IOException {
		simulationStep++;
		traceWriter.write("SimulationStep: " + simulationStep);
		traceWriter.newLine();

		if (simulationStep % SIGNAL_CHANGE_DURATION == 0) {
			controlSignal();
		}

		if (objectMode) {
			removeOldObjects();
			generateNewObjects();
			moveObjects();
		}

		removeOldVehicles();
		generateNewVehicles();
		moveVehicles();
	}

	protected static void startSimulation() {
		if (simulationStep < simulationEndTime) {
			if (!TRACE_MODE) {
				try {
					run();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			else {
				simulationStep++;
			}
			startSimulation();
		}
		else if (simulationStep == simulationEndTime) {
			simulationStep++;
			generateStatistics();
		}
	}
}
