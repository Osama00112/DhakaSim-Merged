package test;
/*
Md Shahrar Fatemi
on 19th April,2021
 */

import java.util.ArrayList;
import java.util.List;

public class JourneySegment {
    //the plan is to keep track of journey stats for each vehicle
    private int startingTime;
    private int source;
    private int destination;
    private int vehicleType;
    private int travelTime;
    private List<Integer> nodesTravelled;

    public int getSource() {
        return source;
    }

    public int getDestination() {
        return destination;
    }

    public int getVehicleType() {
        return vehicleType;
    }

    public int getTravelTime() {
        return travelTime;
    }

    public int getCurrentNode(){return nodesTravelled.get(nodesTravelled.size()-1);}

    public List<Integer> getNodesTravelled() {
        return nodesTravelled;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public void setTravelTime(int travelTime) {
        this.travelTime = travelTime;
    }

    public JourneySegment(int source, int vehicleType, int startingTime) {
        this.nodesTravelled = new ArrayList<>();
        this.source = source;
        this.destination = source;// at first the passenger is not sure of the destination
        this.vehicleType = vehicleType;
        this.startingTime = startingTime;
        this.addNode(source, startingTime);
    }

    public boolean addNode(int node, int time){
        int t = Math.abs(time - startingTime);//using of abs maybe redundant
        setTravelTime(t);//to update time after the passenger has passed this particular node
        setDestination(node);
        return nodesTravelled.add(node);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof JourneySegment){
            for (int node:
                 ((JourneySegment) obj).getNodesTravelled()) {
                for (int myNode:
                     this.nodesTravelled) {
                    if(node != myNode){
                        return false;
                    }
                }
            }
            return true;
        }
        else if(obj instanceof ArrayList){
            for (int node:
                    ((ArrayList<Integer>) obj)) {
                for (int myNode:
                        this.nodesTravelled) {
                    if(node != myNode){
                        return false;
                    }
                }
            }
        }
        return false;
    }
}

