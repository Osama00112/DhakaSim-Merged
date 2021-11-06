package thesisfinal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.*;
import static thesisfinal.DhakaSimFrame.*;
import static thesisfinal.LinkSegmentOrientation.getLinkAndSegmentOrientation;
import static thesisfinal.Utilities.*;

public class DhakaSimPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener {

    private final ArrayList<Node> nodeList = new ArrayList<>();
    private final ArrayList<Link> linkList = new ArrayList<>();
    private final ArrayList<Pedestrian> pedestrians = new ArrayList<>();
    private final ArrayList<Vehicle> vehicleList = new ArrayList<>();
    private final ArrayList<Demand> demandList = new ArrayList<>();
    private final ArrayList<Path> pathList = new ArrayList<>();
    private final ArrayList<Integer> nextGenerationTime = new ArrayList<>();
    private final ArrayList<Integer> numberOfVehiclesToGenerate = new ArrayList<>();
    private final ArrayList<Node> intersectionList = new ArrayList<>();

    private Point2D midPoint;
    private double translateX;
    private double translateY;
    private int referenceX = -999999999;
    private int referenceY = -999999999;
    private double scale = 0.10 + Constants.DEFAULT_SCALE * 0.05;

    private LineNumberReader traceReader;
    private BufferedWriter traceWriter;
    private final JFrame jFrame;
    private final Timer timer;

    private final Random random = DhakaSimFrame.random;
    private final boolean drawRoads = true;
    private int vehicleId = 0;

    public DhakaSimPanel(JFrame jFrame) {
        this.jFrame = jFrame;

        try {
            if (TRACE_MODE) {
                traceReader = new LineNumberReader(new FileReader("trace.txt"));
                StringTokenizer tokenizer = new StringTokenizer(traceReader.readLine(), " ");
                simulationSpeed = Integer.parseInt(tokenizer.nextToken());
                simulationEndTime = Integer.parseInt(tokenizer.nextToken());
                pedestrianMode = Boolean.parseBoolean(tokenizer.nextToken());
            } else {
                traceWriter = new BufferedWriter(new FileWriter("trace.txt", false));
                traceWriter.write(simulationSpeed + " " + simulationEndTime + " " + pedestrianMode);
                traceWriter.newLine();
                traceWriter.flush();
            }
        } catch (IOException fe) {
            fe.printStackTrace();
        }

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        readNetwork();
        readPath();
        readDemand();
        addPathToDemand();

        Statistics s = new Statistics(demandList.size());

        for (Demand demand1 : demandList) {
            nextGenerationTime.add(1);

            double demand = demand1.getDemand();          //returns number of vehicle
            double demandRatio = 3600 / demand;

            if (demandRatio > 1) {
                numberOfVehiclesToGenerate.add(1);
            } else {
                numberOfVehiclesToGenerate.add((int) Math.round(1 / demandRatio));
            }
        }
        if (CENTERED_VIEW) {
            translateX = -midPoint.x * pixelPerMeter;
            translateY = -midPoint.y * pixelPerMeter;
        } else {
            translateX = DEFAULT_TRANSLATE_X;
            translateY = DEFAULT_TRANSLATE_Y;
        }
        timer = new Timer(simulationSpeed, this);
        timer.start();
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Constants.backgroundColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        AffineTransform affineTransform = g2d.getTransform();
        affineTransform.translate(getWidth() / 2.0, getHeight() / 2.0);
        affineTransform.scale(scale, scale);
        affineTransform.translate(-getWidth() / 2.0, -getHeight() / 2.0);
        affineTransform.translate(translateX, translateY);
        g2d.setTransform(affineTransform);

        if (drawRoads) {
            drawRoadNetwork(g2d);
        }

        if (TRACE_MODE) {
            Utilities.drawTrace(traceReader, g2d);
        } else {
            if (pedestrianMode) {
                try {
                    traceWriter.write("Current Pedestrians");
                    traceWriter.newLine();
                } catch (IOException ex) {
                    Logger.getLogger(DhakaSimPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                for (Pedestrian pedestrian : pedestrians) {
                    pedestrian.drawMobilePedestrian(traceWriter, g2d, pixelPerStrip, pixelPerMeter, pixelPerFootpathStrip);
                }
            }
            try {
                traceWriter.write("Current Vehicles");
                traceWriter.newLine();
            } catch (IOException ex) {
                Logger.getLogger(DhakaSimPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (Vehicle vehicle : vehicleList) {
                vehicle.drawVehicle(traceWriter, g2d, pixelPerStrip, pixelPerMeter, pixelPerFootpathStrip);
            }
            try {
                traceWriter.write("End Step");
                traceWriter.newLine();
                traceWriter.flush();
            } catch (IOException ex) {
                Logger.getLogger(DhakaSimPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private int randomVehiclePath(int numberOfPaths) {
        return abs(random.nextInt()) % numberOfPaths;
    }

    private double randomVehicleSpeed() {
        return (random.nextInt(10) + 1); // speed in 1 to 10
        //return random.nextInt((int) maximumSpeed) + 1;
    }

    private double constantVehicleSpeed() {
        return 0;
    }

    private int constantVehicleType() {
        return 4;
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

    private int distributedVehicleType() {
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

    private void removeOldVehicles() {
        ArrayList<Vehicle> vehiclesToRemove = new ArrayList<>();
        for (Vehicle vehicle : vehicleList) {
            if (vehicle.isToRemove()) {
                vehicle.freeStrips();
                vehiclesToRemove.add(vehicle);
                vehicle.calculateStatisticsAtEnd();
                if (DEBUG_MODE) {
                    if (vehicle.getDemandIndex() == 0) {
                        System.out.println(vehicle.getVehicleId() + " : " + vehicle.getDistanceTraveled());
                    }
                }
                vehicle.updateTripTimeStatistics();
            }
        }
        vehicleList.removeAll(vehiclesToRemove);
    }

    private void removeOldPedestrians() {
        ArrayList<Pedestrian> objectsToRemove = new ArrayList<>();
        for (Pedestrian pedestrian : pedestrians) {
            if (pedestrian.isToRemove()) {
                objectsToRemove.add(pedestrian);
            }
        }
        pedestrians.removeAll(objectsToRemove);
    }

    private void generateNewPedestrians() {
        for (Link link : linkList) {
            if (Math.abs(random.nextInt()) % Constants.PEDESTRIAN_LIMIT == 0) {
                int randomSegmentId = Math.abs(random.nextInt()) % link.getNumberOfSegments();
                Segment randomSegment = link.getSegment(randomSegmentId);
                //randomPos = Math.abs(numGenerator.nextInt()) % (int) (randomSegment.getSegmentLength());
                int min = 9;
                int max = (int) (randomSegment.getLength() - 9);
                if ((max - min) + 1 <= 0) {
                    continue;
                }
                int randomPos = random.nextInt((max - min) + 1) + min;
                double randomObjSpeed = Math.abs(random.nextInt()) % 2 + 0.5;
                boolean bo = random.nextBoolean();
                int strip;
                if (!bo) {
                    strip = randomSegment.numberOfStrips() - 1;
                } else {
                    strip = 0;
                }
                Pedestrian pedestrian = new Pedestrian(randomSegment, strip, randomPos, randomObjSpeed);
                pedestrians.add(pedestrian);
            }
        }
    }

    private void createAVehicle(int demandIndex) {
        int numberOfPaths = demandList.get(demandIndex).getNumberOfPaths();
        for (int j = 0; j < numberOfVehiclesToGenerate.get(demandIndex); j++) {
            int pathIndex = randomVehiclePath(numberOfPaths);
//            double speed = randomVehicleSpeed();
            double speed = constantVehicleSpeed();
            int type = constantVehicleType();
//            int type = distributedVehicleType();
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
            } else {
                start = 1;
                end = segment.middleLowStripIndex;
            }

            for (int k = start; k + numberOfStrips - 1 <= end; k++) {
                boolean flag = true;
                for (int l = k; l < k + numberOfStrips; l++) {
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
                    vehicleList.add(new Vehicle(vehicleId++, simulationStep, type, speed, color, demandIndex, pathIndex, 0, startX, startY, endX, endY, linkSegmentOrientation.reverseLink, linkSegmentOrientation.reverseSegment, link, segment.getIndex(), k));
                    break;
                }
            }
        }
    }

    /**
     * @param meanHeadway the constant time gap between two consecutive vehicle
     * @return the time gap to be used based on various distributions
     */
    private double getNextInterArrivalGap(double meanHeadway) {
        double X;
        if (vehicle_generation_rate == VEHICLE_GENERATION_RATE.CONSTANT) {
            X = meanHeadway;
        } else if (vehicle_generation_rate == VEHICLE_GENERATION_RATE.POISSON) {
            X = meanHeadway * -Math.log(random.nextDouble());
        } else {
            X = 0;
            assert false;
        }
        return X;
    }

    /**
     * new vehicles are generated using negative exponential distribution
     * https://www.civil.iitb.ac.in/tvm/nptel/535_TrSim/web/web.html#x1-70003
     */
    private void generateNewVehicles() {
        for (int i = 0; i < demandList.size(); i++) {
            if ((int) (nextGenerationTime.get(i) / Constants.TIME_STEP) == simulationStep) {
                int demand = demandList.get(i).getDemand();
                double meanHeadway = 3600.0 / demand;
                int nextTime;
                do {
                    createAVehicle(i);

                    double X = getNextInterArrivalGap(meanHeadway);

                    nextTime = (int) Math.round(nextGenerationTime.get(i) + X);
                    nextGenerationTime.set(i, nextTime);
                } while ((int)(nextTime / Constants.TIME_STEP) == simulationStep);
            }
        }
    }

    private void movePedestrians() {
        for (Pedestrian pedestrian : pedestrians) {
            if (pedestrian.hasCrossedRoad() || pedestrian.isInAccident()) {
                pedestrian.setToRemove(true);
            } else {
                pedestrian.moveForward();
            }
        }
    }

    @SuppressWarnings("Duplicates")
    private void moveVehicleAtIntersectionEnd(Vehicle vehicle) {
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
            // should not call manualSignaling() if we want to compare performance between different CF models
//            vehicle.getNode().manualSignaling();
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

    private void moveVehicleAtSegmentMiddle(Vehicle vehicle) {
        double oldDistInSegment = vehicle.getDistanceInSegment();
        boolean previous = vehicle.isPassedSensor();

        vehicle.moveVehicleInSegment();

        if (vehicle.hasCollided()) {
            vehicle.resetPosition();
            Statistics.noOfCollisions++;
            Statistics.noCollisionsPerDemand[vehicle.getDemandIndex()][vehicle.getType()]++;
            if (DEBUG_MODE) {
                System.out.println(simulationStep + " :: " + vehicle.getVehicleId() + " -> " + vehicle.getProbableLeader().getVehicleId());
            }
        }

        boolean now = vehicle.isPassedSensor();
        double newDistInSegment = vehicle.getDistanceInSegment();

        if (!previous && now) {
            vehicle.getLink().getSegment(vehicle.getSegmentIndex()).updateInformation(vehicle.getSpeed());
        }

        updateFlow(oldDistInSegment, newDistInSegment, vehicle);
    }

    @SuppressWarnings("Duplicates")
    private IntersectionStrip createIntersectionStrip(Vehicle vehicle, int oldLinkIndex, int newLinkIndex) {
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

    @SuppressWarnings("Duplicates")
    private void moveVehicleAtSegmentEnd(Vehicle vehicle) {
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
                } else {
                    // here the vehicle is at segment end and the signal is red so we need to make the speed 0
                    vehicle.setSpeed(0);
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
            } else {
                vehicle.setSpeed(0);
            }
        }
    }

    private void moveVehicles() {
        for (Vehicle vehicle : vehicleList) {
            System.out.print("");
            if (vehicle.isInIntersection()) {
                vehicle.freeStrips();
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
                vehicle.setSignalOnLink(getNextSignal(vehicle));
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
            vehicle.incrementFuelConsumption();
            vehicle.printVehicleDetails();
        }
        if (simulationStep % 600 == 0) {
            Statistics.flow[(simulationStep / 600) - 1] = Statistics.flowCount;
            Statistics.flowCount = 0;
        }
    }

    void updateFlow(double oldDistInSegment, double newDistInSegment, Vehicle vehicle) {
        int linkId = 3;
        int segmentId = 0;
        double sensorDistance = 960;
        boolean direction = true;

        boolean condition1 = vehicle.getLink().getId() == linkId;
        boolean condition2 = vehicle.getSegmentIndex() == segmentId;
        boolean condition3 = vehicle.isReverseLink() == direction;
        boolean condition4 = oldDistInSegment < sensorDistance;
        boolean condition5 = newDistInSegment > sensorDistance;
        if (condition1 && condition2 && condition3 && condition4 && condition5) {
            Statistics.flowCount++;
            if (DEBUG_MODE) {
                System.out.println(simulationStep + " ::: " + Statistics.flowCount);
            }
        }

    }

    private void controlSignal() {
        for (Node node : intersectionList) {
//            node.adaptiveSignalChange(simulationStep);
            node.constantSignalChange(simulationStep);
        }
    }

    SIGNAL getNextSignal(Vehicle vehicle) {
        int demandIndex = vehicle.getDemandIndex();
        int pathIndex = vehicle.getPathIndex();
        int pathLinkIndex = vehicle.getLinkIndexOnPath();
        int lastLinkInPathIndex = demandList.get(demandIndex).getPath(pathIndex).getNumberOfLinks() - 1;
        if (pathLinkIndex == lastLinkInPathIndex) {
            return SIGNAL.GREEN;
        } else {
            int linkIndex = demandList.get(demandIndex).getPath(pathIndex).getLink(pathLinkIndex);
            Node node = vehicle.isReverseLink() ? nodeList.get(vehicle.getLink().getUpNode()) : nodeList.get(vehicle.getLink().getDownNode());
            return node.getSignalOnLink(linkIndex);
        }
    }

    private void run() throws IOException {
        simulationStep++;
        showProgressSlider.setValue(simulationStep);
        traceWriter.write("SimulationStep: " + simulationStep);
        traceWriter.newLine();

        if (simulationStep % SIGNAL_CHANGE_DURATION == 0) {
            controlSignal();
        }

        if (pedestrianMode) {
            removeOldPedestrians();

            generateNewPedestrians();

            movePedestrians();
        }

        removeOldVehicles();

        generateNewVehicles();

        moveVehicles();

    }

    private void printData(double[] data, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(new File(filename), true))) {
            for (double d : data) {
                writer.printf("%.2f,", d);
            }
            writer.println();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void drawNodeId(Graphics2D g2d, Node node) {
        Font font = new Font("Serif", Font.BOLD, 256);
        g2d.setFont(font);
        Color color_bu = g2d.getColor();
        g2d.setColor(Color.WHITE);
        g2d.drawString(Integer.toString(node.getId()), (int) (node.x * pixelPerMeter), (int) (node.y * pixelPerMeter));
        g2d.setColor(color_bu);
    }

    @SuppressWarnings("Duplicates")
    private void drawRoadNetwork(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        for (Link link : linkList) {
            link.draw(g2d);
        }
        ArrayList<double[]> lineList = new ArrayList<>();
        for (Node node : nodeList) {
            drawNodeId(g2d, node);
            for (int j = 0; j < node.numberOfLinks(); j++) {
                Link link = linkList.get(node.getLink(j));
                double x1, y1, x2, y2, x3, y3, x4, y4;
                if (link.getUpNode() == node.getId()) {
                    x1 = link.getFirstSegment().getStartX() * pixelPerMeter;
                    y1 = link.getFirstSegment().getStartY() * pixelPerMeter;
                    x2 = link.getFirstSegment().getEndX() * pixelPerMeter;
                    y2 = link.getFirstSegment().getEndY() * pixelPerMeter;

                    //double width = 2 * pixelPerFootpathStrip + (link.getSegment(0).numberOfStrips() - 2) * pixelPerStrip;
                    double width = link.getFirstSegment().getSegmentWidth() * pixelPerMeter;

                    x3 = returnX3(x1, y1, x2, y2, width);
                    y3 = returnY3(x1, y1, x2, y2, width);
                    x4 = returnX4(x1, y1, x2, y2, width);
                    y4 = returnY4(x1, y1, x2, y2, width);
                } else {
                    x1 = link.getLastSegment().getStartX() * pixelPerMeter;
                    y1 = link.getLastSegment().getStartY() * pixelPerMeter;
                    x2 = link.getLastSegment().getEndX() * pixelPerMeter;
                    y2 = link.getLastSegment().getEndY() * pixelPerMeter;

//                    double w = 2 * pixelPerFootpathStrip + (link.getSegment(link.getNumberOfSegments() - 1).numberOfStrips() - 2) * pixelPerStrip;
                    double w = link.getLastSegment().getSegmentWidth() * pixelPerMeter;

                    x3 = returnX3(x1, y1, x2, y2, w);
                    y3 = returnY3(x1, y1, x2, y2, w);
                    x4 = returnX4(x1, y1, x2, y2, w);
                    y4 = returnY4(x1, y1, x2, y2, w);

                    x1 = x2;
                    y1 = y2;
                    x3 = x4;
                    y3 = y4;
                }
                for (int k = 0; k < node.numberOfLinks(); k++) {
                    if (j != k) {
                        Link linkPrime = linkList.get(node.getLink(k));
                        double x1Prime, y1Prime, x2Prime, y2Prime, x3Prime, y3Prime, x4Prime, y4Prime;
                        if (linkPrime.getUpNode() == node.getId()) {
                            x1Prime = linkPrime.getFirstSegment().getStartX() * pixelPerMeter;
                            y1Prime = linkPrime.getFirstSegment().getStartY() * pixelPerMeter;
                            x2Prime = linkPrime.getFirstSegment().getEndX() * pixelPerMeter;
                            y2Prime = linkPrime.getFirstSegment().getEndY() * pixelPerMeter;

                            //double w = 2 * pixelPerFootpathStrip + (linkPrime.getSegment(0).numberOfStrips() - 2) * pixelPerStrip;
                            double w = linkPrime.getFirstSegment().getSegmentWidth() * pixelPerMeter;

                            x3Prime = returnX3(x1Prime, y1Prime, x2Prime, y2Prime, w);
                            y3Prime = returnY3(x1Prime, y1Prime, x2Prime, y2Prime, w);
                            x4Prime = returnX4(x1Prime, y1Prime, x2Prime, y2Prime, w);
                            y4Prime = returnY4(x1Prime, y1Prime, x2Prime, y2Prime, w);
                        } else {
                            x1Prime = linkPrime.getLastSegment().getStartX() * pixelPerMeter;
                            y1Prime = linkPrime.getLastSegment().getStartY() * pixelPerMeter;
                            x2Prime = linkPrime.getLastSegment().getEndX() * pixelPerMeter;
                            y2Prime = linkPrime.getLastSegment().getEndY() * pixelPerMeter;

                            //double w = 2 * pixelPerFootpathStrip + (linkPrime.getSegment(linkPrime.getNumberOfSegments() - 1).numberOfStrips() - 2) * pixelPerStrip;
                            double w = linkPrime.getLastSegment().getSegmentWidth() * pixelPerMeter;


                            x3Prime = returnX3(x1Prime, y1Prime, x2Prime, y2Prime, w);
                            y3Prime = returnY3(x1Prime, y1Prime, x2Prime, y2Prime, w);
                            x4Prime = returnX4(x1Prime, y1Prime, x2Prime, y2Prime, w);
                            y4Prime = returnY4(x1Prime, y1Prime, x2Prime, y2Prime, w);

                            x1Prime = x2Prime;
                            y1Prime = y2Prime;
                            x3Prime = x4Prime;
                            y3Prime = y4Prime;
                        }

                        double[] one = new double[]{x1, y1, x1Prime, y1Prime};
                        double[] two = new double[]{x1, y1, x3Prime, y3Prime};
                        double[] three = new double[]{x3, y3, x1Prime, y1Prime};
                        double[] four = new double[]{x3, y3, x3Prime, y3Prime};
                        lineList.add(one);
                        lineList.add(two);
                        lineList.add(three);
                        lineList.add(four);
                    }
                }
            }
        }
        for (int i = 0; i < lineList.size(); i++) {
            boolean doIntersect = false;
            for (int j = 0; j < lineList.size(); j++) {
                if (i != j) {
                    if (doIntersect(lineList.get(i)[0], lineList.get(i)[1],
                            lineList.get(i)[2], lineList.get(i)[3],
                            lineList.get(j)[0], lineList.get(j)[1],
                            lineList.get(j)[2], lineList.get(j)[3])) {
                        doIntersect = true;
                        break;
                    }
                }
            }
            if (!doIntersect) {
                g2d.drawLine((int) round(lineList.get(i)[0]),
                        (int) round(lineList.get(i)[1]),
                        (int) round(lineList.get(i)[2]),
                        (int) round(lineList.get(i)[3]));
            }
        }
        for (Link link : linkList) {
            Segment segment = link.getFirstSegment();
            for (int j = 1; j < link.getNumberOfSegments(); j++) {
                double x1 = segment.getStartX() * pixelPerMeter;
                double y1 = segment.getStartY() * pixelPerMeter;
                double x2 = segment.getEndX() * pixelPerMeter;
                double y2 = segment.getEndY() * pixelPerMeter;

                //double w = 2 * pixelPerFootpathStrip + (segment.numberOfStrips() - 2) * pixelPerStrip;
                double w = segment.getSegmentWidth() * pixelPerMeter;

                double x3 = returnX3(x1, y1, x2, y2, w);
                double y3 = returnY3(x1, y1, x2, y2, w);
                double x4 = returnX4(x1, y1, x2, y2, w);
                double y4 = returnY4(x1, y1, x2, y2, w);

                double x1Prime = link.getSegment(j).getStartX() * pixelPerMeter;
                double y1Prime = link.getSegment(j).getStartY() * pixelPerMeter;
                double x2Prime = link.getSegment(j).getEndX() * pixelPerMeter;
                double y2Prime = link.getSegment(j).getEndY() * pixelPerMeter;

                //double wPrime = 2 * pixelPerFootpathStrip + (link.getSegment(j).numberOfStrips() - 2) * pixelPerStrip;
                double wPrime = link.getSegment(j).getSegmentWidth() * pixelPerMeter;

                double x3Prime = returnX3(x1Prime, y1Prime, x2Prime, y2Prime, wPrime);
                double y3Prime = returnY3(x1Prime, y1Prime, x2Prime, y2Prime, wPrime);
                double x4Prime = returnX4(x1Prime, y1Prime, x2Prime, y2Prime, wPrime);
                double y4Prime = returnY4(x1Prime, y1Prime, x2Prime, y2Prime, wPrime);

                g2d.drawLine((int) round(x2), (int) round(y2), (int) round(x1Prime), (int) round(y1Prime));
                g2d.drawLine((int) round(x4), (int) round(y4), (int) round(x3Prime), (int) round(y3Prime));

                segment = link.getSegment(j);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (simulationStep < simulationEndTime) {
            if (!TRACE_MODE) {
                try {
                    run();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                simulationStep++;
            }
            repaint();
        } else if (simulationStep == simulationEndTime) {
            simulationStep++;

            if (Constants.PRINT_RESULT) {
                generateStatistics();
            }

            JPanel jPanel = new JPanel();
            JButton replay = new JButton(new ImageIcon("display/replay.png"));
            replay.setBackground(Color.WHITE);
            JButton pause = new JButton(new ImageIcon("display/pause.png"));
            pause.setBackground(Color.WHITE);
            JButton rewind = new JButton(new ImageIcon("display/rewind.png"));
            rewind.setBackground(Color.WHITE);
            JButton fastForward = new JButton(new ImageIcon("display/fast_forward.png"));
            fastForward.setBackground(Color.WHITE);
            jPanel.add(replay);
            jPanel.add(pause);
            jPanel.add(rewind);
            jPanel.add(fastForward);
            JSlider jSlider = new JSlider(JSlider.VERTICAL, 0, 0, 0);
            jFrame.getContentPane().add(jPanel, BorderLayout.SOUTH);
            jFrame.getContentPane().add(jSlider, BorderLayout.WEST);
            jFrame.repaint();
            jFrame.revalidate();
        }
    }

    private void generateStatistics() {
        double[] sensorVehicleCount = new double[linkList.size()];
        double[] sensorVehicleAvgSpeed = new double[linkList.size()];
        double[] accidentCount = new double[linkList.size()];
        double[] nearCrashCount = new double[linkList.size()];
        double[] avgSpeedInLink = new double[linkList.size()];
        double[] avgWaitingTimeInLink = new double[linkList.size()]; // in percentage

        int index, index2;

        for (index = 0; index < linkList.size(); index++) {
            int waitingInSegment = 0, leaving = 0;
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

        /*printData(sensorVehicleCount, "statistics/flow_rate.csv");
        printData(sensorVehicleAvgSpeed, "statistics/sensor_speed.csv");
        printData(accidentCount, "statistics/accident.csv");
        printData(nearCrashCount, "statistics/near_crash.csv");
        printData(avgSpeedInLink, "statistics/avg_speed_link.csv");
        printData(avgWaitingTimeInLink, "statistics/avg_waiting_link.csv");*/

        double[] percentageOfWaiting = new double[Constants.TYPES_OF_CARS];
        for (Vehicle vehicle : vehicleList) {
            if (vehicle.isInIntersection()) {
                vehicle.increaseTraveledDistance(vehicle.getDistanceInIntersection());
            } else {
                vehicle.increaseTraveledDistance(vehicle.getDistanceInSegment());
            }

            Statistics.totalTravelTime[vehicle.getType()] += vehicle.getTravelTime();
            Statistics.waitingTime[vehicle.getType()] += vehicle.getWaitingTime();
//            vehicle.calculateStatisticsAtEnd();
        }

        for (int i = 0; i < Constants.TYPES_OF_CARS; i++) {
            if (Statistics.noOfVehicles[i] != 0) {
                Statistics.avgSpeedOfVehicle[i] /= Statistics.noOfVehicles[i];
                Statistics.avgSpeedOfVehicle[i] *= 3.6;
                percentageOfWaiting[i] = 100.0 * Statistics.waitingTime[i] / Statistics.totalTravelTime[i];
            }
        }

        /*printData(Statistics.avgSpeedOfVehicle, "avg_speed_vehicle.csv");
        printData(percentageOfWaiting, "waiting_percentage_vehicle.csv");*/

        double[][] avgFuelConsumption = new double[demandList.size()][Constants.TYPES_OF_CARS];
        double[][] avgTripTime = new double[demandList.size()][Constants.TYPES_OF_CARS];

        int no_of_trip_times = min(1, demandList.size());
        for (int i = 0; i < no_of_trip_times; i++) {
            for (int j = 0; j < Constants.TYPES_OF_CARS; ++j) {
                avgFuelConsumption[i][j] = 1.0 * Statistics.totalFuelConsumption[i][j]
                        / Statistics.noOfVehiclesCompletingTrip[i][j];
                avgTripTime[i][j] = 1.0 * Statistics.tripTime[i][j] / Statistics.noOfVehiclesCompletingTrip[i][j];
            }
            printData(avgFuelConsumption[i], "statistics/fuel" + i + ".csv");
            printData(avgTripTime[i], "statistics/avg_tt" + i + ".csv");
            printData(Statistics.noOfVehiclesCompletingTrip[i], "statistics/trip_complete" + i + ".csv");
            printData(Statistics.noCollisionsPerDemand[i], "statistics/collisions" + i + ".csv");
        }

        if (pedestrianMode) {
            totalAccCount(accidentCount);
        }
        double[] noOfCollisions = {Statistics.noOfCollisions};
        printData(noOfCollisions, "statistics/no_of_collisions.csv");
        printData(Statistics.flow, "statistics/flow.csv");

        System.out.println("Total # of collisions: " + Statistics.noOfCollisions);
        System.out.println("Ended at " + new Date());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        referenceX = e.getX();
        referenceY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        translateX += (e.getX() - referenceX) * 30;
        translateY += (e.getY() - referenceY) * 30;
        referenceX = e.getX();
        referenceY = e.getY();
        if (!TRACE_MODE) {
            repaint();
        }
        if (DEBUG_MODE) {
            System.out.println(translateX + " " + translateY);
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mouseMoved(MouseEvent me) {
    }

    LineNumberReader getTraceReader() {
        return traceReader;
    }

    void setTraceReader(LineNumberReader traceReader) {
        this.traceReader = traceReader;
    }

    @SuppressWarnings("Duplicates")
    private void readNetwork() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("input/link.txt"))));
            String dataLine = bufferedReader.readLine();
            int numLinks = Integer.parseInt(dataLine);
            for (int i = 0; i < numLinks; i++) {
                dataLine = bufferedReader.readLine();
                StringTokenizer stringTokenizer = new StringTokenizer(dataLine, " ");
                int linkId = Integer.parseInt(stringTokenizer.nextToken());
                int nodeId1 = Integer.parseInt(stringTokenizer.nextToken());
                int nodeId2 = Integer.parseInt(stringTokenizer.nextToken());
                int segmentCount = Integer.parseInt(stringTokenizer.nextToken());
                //for a link, at first we need to know the link no, linkID, end nodes, segmentCount
                Link link = new Link(i, linkId, nodeId1, nodeId2);
                for (int j = 0; j < segmentCount; j++) {
                    //then we get the segment information, like
                    //segmentID, starting and ending position of a segment, width

                    dataLine = bufferedReader.readLine();
                    stringTokenizer = new StringTokenizer(dataLine, " ");
                    int segmentId = Integer.parseInt(stringTokenizer.nextToken());
                    double startX = Double.parseDouble(stringTokenizer.nextToken());
                    double startY = Double.parseDouble(stringTokenizer.nextToken());
                    double endX = Double.parseDouble(stringTokenizer.nextToken());
                    double endY = Double.parseDouble(stringTokenizer.nextToken());
                    double segmentWidth = Double.parseDouble(stringTokenizer.nextToken());

                    boolean firstSegment = (j == 0);
                    boolean lastSegment = (j == segmentCount - 1);

                    Segment segment = new Segment(i, j, segmentId, startX, startY, endX, endY, segmentWidth, lastSegment, firstSegment, linkId);
                    link.addSegment(segment);
                }
                linkList.add(link);
            }
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("input/node.txt"))));
            dataLine = bufferedReader.readLine();
            int numNodes = Integer.parseInt(dataLine);
            ArrayList<Point2D> boundaryPoints = new ArrayList<>();

            for (int i = 0; i < numNodes; i++) {
                dataLine = bufferedReader.readLine();
                StringTokenizer stringTokenizer = new StringTokenizer(dataLine, " ");
                int nodeId = Integer.parseInt(stringTokenizer.nextToken());
                double centerX = Double.parseDouble(stringTokenizer.nextToken());
                double centerY = Double.parseDouble(stringTokenizer.nextToken());
                if (centerX != 0 || centerY != 0) {
                    boundaryPoints.add(new Point2D(centerX, centerY));
                }
                Node node = new Node(i, nodeId, centerX, centerY);
                while (stringTokenizer.hasMoreTokens()) {
                    node.addLink(getLinkIndex(Integer.parseInt(stringTokenizer.nextToken())));
                }
                if (node.numberOfLinks() > 1) {
                    node.createBundles();
                    intersectionList.add(node);
                }
                nodeList.add(node);
            }

            double left = Double.MAX_VALUE;
            double right = 0;
            double top = Double.MAX_VALUE;
            double down = 0;

            for (Point2D boundaryPoint : boundaryPoints) {

                left = Math.min(left, boundaryPoint.x);
                right = Math.max(right, boundaryPoint.x);

                top = Math.min(top, boundaryPoint.y);
                down = Math.max(down, boundaryPoint.y);
            }
            midPoint = new Point2D((left + right) / 2, (top + down) / 2);

            bufferedReader.close();
        } catch (IOException ex) {
            Logger.getLogger(DhakaSimPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int getNodeIndex(int nodeId) {
        for (Node node : nodeList) {
            if (node.getId() == nodeId) {
                return node.getIndex();
            }
        }
        return -1;
    }

    private int getLinkIndex(int linkId) {
        for (Link link : linkList) {
            if (link.getId() == linkId) {
                return link.getIndex();
            }
        }
        return -1;
    }

    @SuppressWarnings("Duplicates")
    private void readDemand() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("input/demand.txt"))));
            String dataLine = bufferedReader.readLine();
            int numDemands = Integer.parseInt(dataLine);
            for (int i = 0; i < numDemands; i++) {
                dataLine = bufferedReader.readLine();
                StringTokenizer stringTokenizer = new StringTokenizer(dataLine, " ");
                int nodeId1 = Integer.parseInt(stringTokenizer.nextToken());
                int nodeId2 = Integer.parseInt(stringTokenizer.nextToken());
                int demand = Integer.parseInt(stringTokenizer.nextToken());
                demandList.add(new Demand(getNodeIndex(nodeId1), getNodeIndex(nodeId2), demand));
            }
            bufferedReader.close();
        } catch (IOException ex) {
            Logger.getLogger(DhakaSimPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("Duplicates")
    private void readPath() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("input/path.txt"))));
            String dataLine = bufferedReader.readLine();
            int numPaths = Integer.parseInt(dataLine);
            for (int i = 0; i < numPaths; i++) {
                dataLine = bufferedReader.readLine();
                StringTokenizer stringTokenizer = new StringTokenizer(dataLine, " ");
                int nodeId1 = Integer.parseInt(stringTokenizer.nextToken());
                int nodeId2 = Integer.parseInt(stringTokenizer.nextToken());
                Path path = new Path(getNodeIndex(nodeId1), getNodeIndex(nodeId2));
                while (stringTokenizer.hasMoreTokens()) {
                    int linkId = Integer.parseInt(stringTokenizer.nextToken());
                    path.addLink(getLinkIndex(linkId));
                }
                pathList.add(path);
            }
            bufferedReader.close();
        } catch (IOException ex) {
            Logger.getLogger(DhakaSimPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addPathToDemand() {
        for (Path path : pathList) {
            for (Demand demand : demandList) {
                if (demand.getSource() == path.getSource() && demand.getDestination() == path.getDestination()) {
                    demand.addPath(path);
                }
            }
        }
    }

    private void totalAccCount(double[] acc) {
        double sum = 0;
        for (double v : acc) {
            sum += v;
        }
        System.out.println("Total number of accidents: " + sum);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        double newScaleValue = scale - notches * 0.01;
        scale = Math.min(1.0, Math.max(0.01, newScaleValue));
    }
}
