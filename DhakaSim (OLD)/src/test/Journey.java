package test;
/*
Md Shahrar Fatemi
on 19th April,2021
 */

public class Journey {
    //the plan is to keep track of journey stats for each vehicle
    private int passengerCount;
    private int source;
    private int destination;
    private int vehicleType;
    private int travelTime;
    static boolean done = false;
    //[1,2,3],[1,4,5],[4,6,7]

    public int getPassengerCount() {
        return passengerCount;
    }

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

    public Journey(int passengerCount, int source, int destination, int vehicleType, int travelTime) {
        this.passengerCount = passengerCount;
        this.source = source;
        this.destination = destination;
        this.vehicleType = vehicleType;
        this.travelTime = travelTime;
    }
}
