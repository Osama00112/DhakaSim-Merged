package test;

/*
Md Shahrar Fatemi
on 19th April,2021
 */

import java.util.ArrayList;
import java.util.List;

public class Passenger {
    private int id;//passenger Id
    private int currentNodeID;
    private int startingNode;
    private int endingNode;
    private boolean isTravelling;

    List<JourneySegment> journeySegmentList;
    List<Integer> timesPerJourneySegment;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCurrentNodeID() {
        return currentNodeID;
    }

    public void setCurrentNodeID(int currentNodeID) {
        this.currentNodeID = currentNodeID;
    }

    public int getStartingNode() {
        return startingNode;
    }

    public void setStartingNode(int startingNode) {
        this.startingNode = startingNode;
    }

    public int getEndingNode() {
        return endingNode;
    }

    public void setEndingNode(int endingNode) {
        this.endingNode = endingNode;
    }

    public boolean isTravelling() {
        return isTravelling;
    }

    public void setTravelling(boolean travelling) {
        isTravelling = travelling;
    }

    public List<JourneySegment> getJourneySegmentList() {
        return journeySegmentList;
    }

    public Passenger(int id, int currentNodeID) {
        this.journeySegmentList = new ArrayList<>();
        this.timesPerJourneySegment = new ArrayList<>();
        this.id = id;
        this.currentNodeID = currentNodeID;
        this.setStartingNode(currentNodeID);
        this.setEndingNode(currentNodeID);
        this.setTravelling(false);

    }

    //this function will be called when a passenger starts a new journey
    public boolean startJourney(int source, int vehicleType, int time){
        this.setTravelling(true);
        this.setCurrentNodeID(source);
        return journeySegmentList.add(new JourneySegment(source, vehicleType, time));
    }

    //this func is to be called when we need to add a node to the current path of the passenger
    public boolean addNodeToPathWhileTravelling(int node, int time){
        if(this.isTravelling) {
            int l = journeySegmentList.size();
            JourneySegment journeySegment = journeySegmentList.get(l - 1);
            if(journeySegment.addNode(node, time)){
                setCurrentNodeID(journeySegment.getCurrentNode());
                return true;
            }
        }
        return false;
    }

    //this function will be called when the current journey is going to be finished
    public boolean finishJourney(int destination, int time){
        if(this.isTravelling) {
            int l = journeySegmentList.size();
            JourneySegment journeySegment = journeySegmentList.get(l - 1);
            if(journeySegment.addNode(destination, time)){
                int c = journeySegment.getDestination();
                setCurrentNodeID(c);
                setEndingNode(c);
                setTravelling(false);
                return this.timesPerJourneySegment.add(journeySegment.getTravelTime());
            }
        }
        return false;
    }

    public String getTravelledNodes(){
        String str = "";
        for(JourneySegment journeySegment : journeySegmentList){
            str += ("("+journeySegment.getVehicleType()+")");
            for(int node : journeySegment.getNodesTravelled()){
                str += ("-"+node);
            }
            str +="-";
        }
        return str;
    }

    public String getTravelledTimes(){
        String str = "";
        for(int time : timesPerJourneySegment) {
            str += ("-" + time);
        }
        return str;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Passenger){
            if(((Passenger) obj).getId() == this.id){
                return true;
            }
            else return false;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Passenger{" +
                "ID=" + id +
                ", currentNodeID=" + currentNodeID +
                ", startingNode=" + startingNode +
                ", endingNode=" + endingNode +
                ", isTravelling=" + isTravelling +
                ", journeys=" + getTravelledNodes() +
                ", timesPerJourneySegment=" + getTravelledTimes() +
                '}';
    }
}
