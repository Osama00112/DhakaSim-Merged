package thesisfinal;

public class Statistics {
    static double[] avgSpeedOfVehicle;
    static int[] noOfVehicles;
    static int[] waitingTime;
    static int[] totalTravelTime;

    Statistics() {
        avgSpeedOfVehicle = new double[Constants.TYPES_OF_CARS];
        noOfVehicles = new int[Constants.TYPES_OF_CARS];
        waitingTime = new int[Constants.TYPES_OF_CARS];
        totalTravelTime = new int[Constants.TYPES_OF_CARS];
    }
}
