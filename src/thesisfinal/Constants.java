package thesisfinal;

import java.awt.*;

public interface Constants<standingPedestrianDistributions> {
	int TYPES_OF_CARS = 12;
	int OBJECT_LIMIT = 25; //probability of generating object is 1/this parameter
	double DEFAULT_SCALE = 2;
	double EPSILON = 1E-12;



	boolean OBJECTS_BLOCKAGE_DISTRIBUTION_IS_UNIFORM = false;
	boolean OBJECTS_BLOCKAGE_DISTRIBUTION_IS_MULTIPLE_GAUSSIAN = true;



	int MAX_NUMBER_OF_OBJECTS = Integer.MAX_VALUE;
	int MAX_NUMBER_OF_VEHICLES = Integer.MAX_VALUE;



	Color ROAD_BORDER_COLOR = Color.black;
	Color BACKGROUND_COLOR = new Color(105, 105, 105);



	Color ROAD_CROSSING_PEDESTRIAN_COLOR = Color.white;
	double ROAD_CROSSING_PEDESTRIAN_SIZE = 0.7; //Unit: meter
	int MAX_NUMBER_OF_ROAD_CROSSING_PEDESTRIANS = 100;



	Color PEDESTRIAN_IN_ACCIDENT_COLOR = Color.red;
	double PEDESTRIAN_IN_ACCIDENT_SIZE = 25 * ROAD_CROSSING_PEDESTRIAN_SIZE; //Unit: meter



	Color STANDING_PEDESTRIAN_COLOR = Color.yellow;
	double STANDING_PEDESTRIAN_LENGTH = ROAD_CROSSING_PEDESTRIAN_SIZE; //Unit: meter
	double STANDING_PEDESTRIAN_WIDTH = ROAD_CROSSING_PEDESTRIAN_SIZE; //Unit: meter
	int STANDING_PEDESTRIAN_TIME_LIMIT_FACTOR = 50;
	double STANDING_PEDESTRIAN_MIN_BLOCKAGE = 0.21; //Unit: meter
	double STANDING_PEDESTRIAN_MAX_BLOCKAGE = 6.00; //Unit: meter
	NormalDistribution[] STANDING_PEDESTRIAN_DISTRIBUTIONS = new NormalDistribution[] {
			new NormalDistribution(0.4635,0.825,0.40),
			new NormalDistribution(0.3250,1.925,0.45),
			new NormalDistribution(0.1350,2.900,0.42),
			new NormalDistribution(0.0640,3.710,0.35),
			new NormalDistribution(0.0070,5.750,0.40)
	};
	int MAX_NUMBER_OF_STANDING_PEDESTRIANS = (int)(Math.ceil(15.61*3.83));



	Color PARKED_CAR_COLOR = Color.yellow;
	double PARKED_CAR_LENGTH = 4.5; //Unit: meter
	double PARKED_CAR_WIDTH = 1.7; //Unit: meter
	int PARKED_CAR_TIME_LIMIT_FACTOR = STANDING_PEDESTRIAN_TIME_LIMIT_FACTOR * 10;
	double PARKED_CAR_MIN_BLOCKAGE = 0.70; //Unit: meter
	double PARKED_CAR_MAX_BLOCKAGE = 6.00; //Unit: meter
	NormalDistribution[] PARKED_CAR_DISTRIBUTIONS = new NormalDistribution[] {
			new NormalDistribution(0.050,1.12,0.390),
			new NormalDistribution(0.515,2.07,0.322),
			new NormalDistribution(0.200,2.73,0.310),
			new NormalDistribution(0.172,3.64,0.334),
			new NormalDistribution(0.042,4.48,0.300),
			new NormalDistribution(0.010,5.92,0.310)
	};
	int MAX_NUMBER_OF_PARKED_CARS = (int)(Math.ceil(19.77*3.83));



	Color PARKED_RICKSHAW_COLOR = Color.yellow;
	double PARKED_RICKSHAW_LENGTH = 3; //Unit: meter
	double PARKED_RICKSHAW_WIDTH = 1; //Unit: meter
	int PARKED_RICKSHAW_TIME_LIMIT_FACTOR = PARKED_CAR_TIME_LIMIT_FACTOR/5;
	double PARKED_RICKSHAW_MIN_BLOCKAGE = 0.56; //Unit: meter
	double PARKED_RICKSHAW_MAX_BLOCKAGE = 6.00; //Unit: meter
	NormalDistribution[] PARKED_RICKSHAW_DISTRIBUTIONS = new NormalDistribution[] {
			new NormalDistribution(0.582,1.57,0.500),
			new NormalDistribution(0.258,2.76,0.440),
			new NormalDistribution(0.085,3.67,0.320),
			new NormalDistribution(0.046,4.65,0.380),
			new NormalDistribution(0.009,5.88,0.380)
	};
	int MAX_NUMBER_OF_PARKED_RICKSHAWS = (int)(Math.ceil(15.61*3.83));



	Color PARKED_CNG_COLOR = Color.yellow;
	double PARKED_CNG_LENGTH = 2.6; //Unit: meter
	double PARKED_CNG_WIDTH = 1.3; //Unit: meter
	int PARKED_CNG_TIME_LIMIT_FACTOR = PARKED_CAR_TIME_LIMIT_FACTOR/5;
	double PARKED_CNG_MIN_BLOCKAGE = 0.95; //Unit: meter
	double PARKED_CNG_MAX_BLOCKAGE = 4.67; //Unit: meter
	NormalDistribution[] PARKED_CNG_DISTRIBUTIONS = new NormalDistribution[] {
			new NormalDistribution(0.119,1.13,0.29),
			new NormalDistribution(0.730,2.30,0.55),
			new NormalDistribution(0.065,3.81,0.27),
			new NormalDistribution(0.015,4.65,0.32)
	};
	int MAX_NUMBER_OF_PARKED_CNGS = (int)(Math.ceil(4.67*3.83));
}