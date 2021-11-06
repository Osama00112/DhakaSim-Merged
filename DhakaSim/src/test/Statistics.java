package test;

import java.util.ArrayList;
import java.util.List;

public class Statistics {
    static double[] avgSpeedOfVehicle;
    static int[] noOfVehicles;
    static int[] waitingTime;
    static int[] totalTravelTime;
    static int[][] tripTime;
    static double[][] noOfVehiclesCompletingTrip;
    static double[][] totalFuelConsumption;
    static int noOfCollisions;
    static double[][] noCollisionsPerDemand;
    static double[] flow;
    static int flowCount;
    static ArrayList<Journey> journeysOfVehicles; // newly added

    Statistics(int demandSize) {
        avgSpeedOfVehicle = new double[Constants.TYPES_OF_CARS];
        noOfVehicles = new int[Constants.TYPES_OF_CARS];
        waitingTime = new int[Constants.TYPES_OF_CARS];
        totalTravelTime = new int[Constants.TYPES_OF_CARS];
        totalFuelConsumption = new double[demandSize][Constants.TYPES_OF_CARS];

        noOfVehiclesCompletingTrip = new double[demandSize][Constants.TYPES_OF_CARS];
        noCollisionsPerDemand = new double[demandSize][Constants.TYPES_OF_CARS];
        tripTime = new int[demandSize][Constants.TYPES_OF_CARS];
        noOfCollisions = 0;

        flow = new double[20];
        flowCount = 0;
        //newly added
        journeysOfVehicles = new ArrayList<>();
    }
}
