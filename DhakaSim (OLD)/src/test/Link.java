package test;

import java.awt.*;
import java.util.ArrayList;

public class Link {

    private int index;
    private int id;
    private int upNode;
    private int downNode;
    private double length; // CSE18


    // CSE18
    private double totalTime1;                      //Riya    // up -> down
    public int leavingVehicleCountOnLink1;          //Riya
    private double averageTimeonLink1;              //Riya

    private double totalTime2;                      //Rumi  // down -> up
    public int leavingVehicleCountOnLink2;          //Rumi
    private double averageTimeonLink2;              //Rumi

    private final double average_vehicle_speed= 13.66;
    private final int minimum_vehiclecount_tostartstats=5;
    // CSE18

    private ArrayList<Segment> segmentList = new ArrayList<>();

    Link(int index, int id, int upNode, int downNode) {
        this.index = index;
        this.id = id;
        this.upNode = upNode;
        this.downNode = downNode;

        // CSE18
        this.length=0;  // Rumi

        this.totalTime1=0;//Riya
        this.leavingVehicleCountOnLink1=0;//Riya
        this.averageTimeonLink1=0;//Riya

        this.totalTime2=0;// Rumi
        this.leavingVehicleCountOnLink2=0;// Rumi
        this.averageTimeonLink2=0;// Rumi
        // CSE18
    }

    public void setTotalTime1(double time) {        // CSE18
        //System.out.println(time);
        totalTime1+=time;//Riya
    }
    //Riya
    public double getTotalTime1()       // CSE18
    {
        return totalTime1;
    }
    public double getTotalTime2()
    {
        return totalTime2;
    }       // CSE18
    //Riya
    public void setleavingVehicleonlink1(int number)        // CSE18
    {
        leavingVehicleCountOnLink1+=number;

    }
    //Riya
    public double getAverageTimeonLink1()           // CSE18
    {
        if(leavingVehicleCountOnLink1 < minimum_vehiclecount_tostartstats)
        {
            return (length*0.2)/average_vehicle_speed;
        }
        else
        {
            return totalTime1/leavingVehicleCountOnLink1;
        }
    }

    public void setTotalTime2(double time) {        // CSE18
        //System.out.println(time);
        totalTime2+=time;//Riya
    }
    //Riya
    public void setleavingVehicleonlink2(int number)        // CSE18
    {
        leavingVehicleCountOnLink2+=number;
        /*if(leavingVehicleCountOnLink%20==0)
        {
            System.out.println(id+" "+leavingVehicleCountOnLink+" "+totalTime);
        }*/
    }
    //Riya
    public double getAverageTimeonLink2()       // CSE18
    {
        if(leavingVehicleCountOnLink2 < minimum_vehiclecount_tostartstats)
        {
            return (length*0.2)/average_vehicle_speed;
        }
        else
        {
            return totalTime2/leavingVehicleCountOnLink2;
        }
    }
    //Rumi
    public double getLinkLength() {
        return length;
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

    int getUpNode() {
        return upNode;
    }

    public void setUpNode(int upNode) {
        this.upNode = upNode;
    }

    int getDownNode() {
        return downNode;
    }

    public void setDownNode(int downNode) {
        this.downNode = downNode;
    }

    Segment getSegment(int index) {
        return segmentList.get(index);
    }

    Segment getFirstSegment() {
        assert !segmentList.isEmpty();

        return segmentList.get(0);
    }

    Segment getLastSegment() {
        assert !segmentList.isEmpty();

        return segmentList.get(segmentList.size() - 1);
    }

    void addSegment(Segment segment) {
        segmentList.add(segment);
    }

    int getNumberOfSegments() {
        return segmentList.size();
    }

    public void draw(Graphics2D g2d) {
        for (int i = 0; i < getNumberOfSegments(); i++) {
            getSegment(i).draw(g2d);
        }
    }

}
