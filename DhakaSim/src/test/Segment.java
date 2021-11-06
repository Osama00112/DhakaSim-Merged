package test;

import java.awt.*;
import java.util.ArrayList;

import static java.lang.Math.*;
import static test.DhakaSimFrame.*;
import static test.Utilities.*;

public class Segment {
    private int linkIndex;
    private int index;
    private int id;
    private double startX;
    private double startY;
    private double startZ;
    private double endX;
    private double endY;
    private double endZ;
    private double segmentWidth;
    private boolean lastSegment;
    private boolean firstSegment;
    private double sensor;

    private int parentLinkId;
    private int stripCount;

    private int sensorPassengerCount ; // count passengers in a segment // newly added
    private int sensorVehicleCount;
    private int enteringVehicleCount;
    private int leavingVehicleCount;
    private double avgSpeedInSegment;
    private int nearCrashCount; // number of almost crash
    private double sensorVehicleAvgSpeed;
    private int accidentCount;
    // when a vehicle leaves the segment it increases this by its waiting time on this segment
    private int totalWaitingTime;

    private int forwardVehicleCount;
    private int reverseVehicleCount;

    int middleLowStripIndex;
    int middleHighStripIndex; // first strip of the 2nd side of the road
    int lastVehicleStripIndex;

    private ArrayList<Strip> stripList = new ArrayList<>();

    Segment(int linkIndex, int index, int id, double startX, double startY,double startZ, double endX, double endY,double endZ, double segmentWidth, boolean lastSegment, boolean firstSegment, int parentLinkId) {
        this.linkIndex = linkIndex;
        this.index = index;
        this.id = id;
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
        this.segmentWidth = segmentWidth;
        this.lastSegment = lastSegment;
        this.firstSegment = firstSegment;
        this.parentLinkId = parentLinkId;
        sensor = 0.5 * getDistance(startX, startY, endX, endY);
        sensorPassengerCount = 0 ;
        sensorVehicleCount = 0;
        sensorVehicleAvgSpeed = 0;
        accidentCount = 0;
        enteringVehicleCount = 0;
        leavingVehicleCount = 0;
        avgSpeedInSegment = 0;
        nearCrashCount = 0;
        totalWaitingTime = 0;
        forwardVehicleCount = 0;
        reverseVehicleCount = 0;
        initialize();
    }

    int getForwardVehicleCount() {
        return forwardVehicleCount;
    }

    int getReverseVehicleCount() {
        return reverseVehicleCount;
    }

    void increaseForwardVehicleCount() {
        forwardVehicleCount++;
    }
    void increaseReverseVehicleCount() {
        reverseVehicleCount++;
    }
    void decreaseForwardVehicleCount() {
        forwardVehicleCount--;
    }
    void decreaseReverseVehicleCount() {
        reverseVehicleCount--;
    }

    void increaseEnteringVehicleCount() {
        enteringVehicleCount++;
    }

    private void increaseLeavingVehicleCount() {
        leavingVehicleCount++;
        assert leavingVehicleCount <= enteringVehicleCount;
    }

    void increaseTotalWaitingTime(int amount) {
        totalWaitingTime += amount;
    }

    int getTotalWaitingTime() {
        return totalWaitingTime;
    }

    void updateAvgSpeedInSegment(double speed) {
        increaseLeavingVehicleCount();
        avgSpeedInSegment = (avgSpeedInSegment * (leavingVehicleCount - 1) + speed) / leavingVehicleCount;
    }

    public double getAvgSpeedInSegment() {
        return avgSpeedInSegment;
    }

    public int getEnteringVehicleCount() {
        return enteringVehicleCount;
    }

    public int getLeavingVehicleCount() {
        return leavingVehicleCount;
    }

    public int getNearCrashCount() {
        return nearCrashCount;
    }

    public void increaseNearCrashCount() {
        this.nearCrashCount++;
    }


    public int getStripCount() {
        return stripCount;
    }

    private void initialize() {
        stripCount = 2 + (int) floor((segmentWidth - 2 * footpathStripWidth) / stripWidth);
        for (int i = 0; i < stripCount; i++) {
            if (i == 0) {
                stripList.add(new Strip(index, i, true, parentLinkId));
            } else if (i == stripCount - 1) {
                stripList.add(new Strip(index, i, true, parentLinkId));
            } else {
                stripList.add(new Strip(index, i, false, parentLinkId));
            }
        }

        middleHighStripIndex = (int) ceil(stripCount / 2.0);
        if (stripCount % 2 == 0) {
            middleLowStripIndex = middleHighStripIndex - 1;
        } else {
            middleLowStripIndex = middleHighStripIndex - 2;
        }
        lastVehicleStripIndex = stripCount - 2;
    }

    public int getLinkIndex() {
        return linkIndex;
    }

    public void setLinkIndex(int linkIndex) {
        this.linkIndex = linkIndex;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    double getStartX() {
        return startX;
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    double getStartY() {
        return startY;
    }

    public void setStartY(double startY) {
        this.startY = startY;
    }

    double getEndX() {
        return endX;
    }

    public double getStartZ() {
        return startZ;
    }

    public void setStartZ(double startZ) {
        this.startZ = startZ;
    }

    public double getEndZ() {
        return endZ;
    }

    public void setEndZ(double endZ) {
        this.endZ = endZ;
    }

    public void setEndX(double endX) {
        this.endX = endX;
    }

    double getEndY() {
        return endY;
    }

    public void setEndY(double endY) {
        this.endY = endY;
    }

    double getSegmentWidth() {
        return segmentWidth;
    }

    public void setSegmentWidth(double segmentWidth) {
        this.segmentWidth = segmentWidth;
    }

    boolean isLastSegment() {
        return lastSegment;
    }

    public void setLastSegment(boolean lastSegment) {
        this.lastSegment = lastSegment;
    }

    boolean isFirstSegment() {
        return firstSegment;
    }

    public void setFirstSegment(boolean firstSegment) {
        this.firstSegment = firstSegment;
    }

    double getSensor() {
        return sensor;
    }

    public void setSensor(double sensor) {
        this.sensor = sensor;
    }

    public int getVehicleCountAtSensor() {
        return sensorVehicleCount;
    }

    public void setVehicleCountAtSensor(int vehicleCountAtSensor) {
        this.sensorVehicleCount = vehicleCountAtSensor;
    }

    public double getAverageSpeedAtSensor() {
        return sensorVehicleAvgSpeed;
    }

    public void setAverageSpeedAtSensor(double averageSpeedAtSensor) {
        this.sensorVehicleAvgSpeed = averageSpeedAtSensor;
    }

    void setAccidentCount(int accidentCount) {
        this.accidentCount = accidentCount;
    }

    Strip getStrip(int index) {
        assert index >= 0 && index < stripList.size();
        return stripList.get(index);
    }

    int numberOfStrips() {
        return stripList.size();
    }

    public double getLength() {
        return getDistance(startX, startY, endX, endY);
    }

    void updateInformation(double speed, int passengerInVehicle) {
        sensorPassengerCount += passengerInVehicle ; //of a passenger vehicle + prev passenger = new passenger // newly added
        sensorVehicleCount++;
        sensorVehicleAvgSpeed = (sensorVehicleAvgSpeed * (sensorVehicleCount - 1) + speed) / sensorVehicleCount;
    }

    public int getSensorPassengerCount() { //newly added
        return sensorPassengerCount;
    }

    public void setSensorPassengerCount(int sensorPassengerCount) {   //newly added
        this.sensorPassengerCount = sensorPassengerCount;
    }

    public void draw(Graphics2D g2d) {
        int x1 = (int) round(startX * pixelPerMeter);
        int y1 = (int) round(startY * pixelPerMeter);
        int x2 = (int) round(endX * pixelPerMeter);
        int y2 = (int) round(endY * pixelPerMeter);
        double w = segmentWidth * pixelPerMeter;
        int x3 = (int) round(returnX3(x1, y1, x2, y2, w));
        int y3 = (int) round(returnY3(x1, y1, x2, y2, w));
        int x4 = (int) round(returnX4(x1, y1, x2, y2, w));
        int y4 = (int) round(returnY4(x1, y1, x2, y2, w));
        g2d.setColor(Constants.roadBorderColor);
        g2d.setStroke(new BasicStroke(2));  // divider
        g2d.drawLine((x1 + x3) / 2, (y1 + y3) / 2, (x2 + x4) / 2, (y2 + y4) / 2); // divider
        g2d.setStroke(new BasicStroke(4));
        g2d.drawLine(x1, y1, x2, y2);
        g2d.drawLine(x3, y3, x4, y4);

    }

    //counts number of vehicle passed over the sensor and updates average speed
    public void updateSensorInfo(double newSpeed) {
        sensorVehicleCount++;
        sensorVehicleAvgSpeed = sensorVehicleAvgSpeed / sensorVehicleCount * (sensorVehicleCount - 1)
                + newSpeed / sensorVehicleCount;
    }

    int getSensorVehicleCount() {
        return sensorVehicleCount;
    }

    double getSensorVehicleAvgSpeed() {
        return sensorVehicleAvgSpeed;
    }

    public void updateAccidentcount() {
        accidentCount++;
    }

    int getAccidentCount() {
        return accidentCount++;
    }

}
