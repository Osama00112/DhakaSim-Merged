package thesisfinal;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Parameters {
	static int simulationStep;
	static int simulationEndTime;
	static double pixelPerStrip;
	static double pixelPerMeter;
	static double pixelPerFootpathStrip;
	static int simulationSpeed;
	static double probabilityOfAccident;
	static double encounterPerAccident; //TODO Rename and redefine
	static double stripWidth;
	static double footpathStripWidth;
	static double maximumSpeed;
	static boolean objectMode;

	static double totalTypesOfObjects;
	static double roadCrossingPedestrianProbability;
	static double standingPedestrianProbability;
	static double parkedCarProbability;
	static double parkedRickshawProbability;
	static double parkedCNGProbability;

	static boolean DEBUG_MODE;
	static boolean TRACE_MODE;
	static Random random;
	static int seed;
	static int SIGNAL_CHANGE_DURATION;
	static double DEFAULT_TRANSLATE_X;
	static double DEFAULT_TRANSLATE_Y;
	static boolean CENTERED_VIEW;
	static double slowVehiclePercentage;
	static double mediumVehiclePercentage;
	static double fastVehiclePercentage;
	static double TTC_THRESHOLD;
	static boolean OBJECTS_BLOCKAGE_DISTRIBUTION_IS_UNIFORM;
	static boolean OBJECTS_BLOCKAGE_DISTRIBUTION_IS_MULTIPLE_GAUSSIAN;

	static ArrayList<Integer> simulationStepLineNos;

	enum DLC_MODEL {    // Discretionary lane changing models
		NAIVE_MODEL,    // no model at all; just instantly changes lane if possible
		GIPPS_MODEL,
		GHR_MODEL
	}

	enum CAR_FOLLOWING_MODEL {
		NAIVE_MODEL,    // no model at all; just moves as fast as possible and brakes as hard as possible
		GIPPS_MODEL,    // moving velocity is controlled; but no braking; so collision could not be avoided
		HYBRID_MODEL    // velocity from GIPPS model, braking hard in emergency
	}

	static DLC_MODEL lane_changing_model;
	static CAR_FOLLOWING_MODEL car_following_model;

	Parameters (boolean isGauss, String time, String speed, double stdPedProb, double carProb, double rickshawProb, double CNGProb) {
		//initialize();

		// new initialization method instead of previous one
		initialize3(time, speed, stdPedProb, carProb, rickshawProb, CNGProb);

		initializePart2();
		indexTraceFile();
		OBJECTS_BLOCKAGE_DISTRIBUTION_IS_MULTIPLE_GAUSSIAN = isGauss;
		OBJECTS_BLOCKAGE_DISTRIBUTION_IS_UNIFORM = (!isGauss);
		
	}

	private void initialize() {
		totalTypesOfObjects = 0;
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("parameter.txt")));
			while (true) {
				String dataLine = bufferedReader.readLine();
				if (dataLine == null) {
					bufferedReader.close();
					break;
				}
				StringTokenizer stringTokenizer = new StringTokenizer(dataLine);
				String name = stringTokenizer.nextToken();
				String value = stringTokenizer.nextToken();
				switch (name) {
					case "SimulationEndTime":
						simulationEndTime = Integer.parseInt(value);
						break;
					case "PixelPerMeter":
						pixelPerMeter = Integer.parseInt(value);
						break;
					case "SimulationSpeed":
						simulationSpeed = Integer.parseInt(value);
						break;
					case "EncounterPerAccident":
						encounterPerAccident = Double.parseDouble(value);
						break;
					case "StripWidth":
						stripWidth = Double.parseDouble(value);
						break;
					case "FootpathStripWidth":
						footpathStripWidth = Double.parseDouble(value);
						break;
					case "MaximumSpeed":
						maximumSpeed = Double.parseDouble(value);
						break;
					case "ObjectMode":
						objectMode = value.equalsIgnoreCase("On");
						break;
					case "DebugMode":
						DEBUG_MODE = value.equalsIgnoreCase("On");
						break;
					case "TraceMode":
						TRACE_MODE = value.equalsIgnoreCase("On");
						break;
					case "RandomSeed":
						seed = Integer.parseInt(value);
						break;
					case "SignalChangeDuration":
						SIGNAL_CHANGE_DURATION = Integer.parseInt(value);
						break;
					case "DefaultTranslateX":
						DEFAULT_TRANSLATE_X = Double.parseDouble(value);
						break;
					case "DefaultTranslateY":
						DEFAULT_TRANSLATE_Y = Double.parseDouble(value);
						break;
					case "CenteredView":
						CENTERED_VIEW = value.equalsIgnoreCase("on");
						break;
					case "DLC_model":
						int o = Integer.parseInt(value);
						switch (o) {
							case 0:
								lane_changing_model = DLC_MODEL.NAIVE_MODEL;
								break;
							case 1:
								lane_changing_model = DLC_MODEL.GHR_MODEL;
								break;
							case 2:
								lane_changing_model = DLC_MODEL.GIPPS_MODEL;
								break;
							default:
								lane_changing_model = DLC_MODEL.GIPPS_MODEL;
								break;
						}
					case "CF_model":
						int p = Integer.parseInt(value);
						switch (p) {
							case 0:
								car_following_model = CAR_FOLLOWING_MODEL.NAIVE_MODEL;
								break;
							case 1:
								car_following_model = CAR_FOLLOWING_MODEL.GIPPS_MODEL;
								break;
							case 2:
								car_following_model = CAR_FOLLOWING_MODEL.HYBRID_MODEL;
								break;
							default:
								car_following_model = CAR_FOLLOWING_MODEL.HYBRID_MODEL;
								break;
						}
					case "SlowVehicle":
						slowVehiclePercentage = Double.parseDouble(value);
						break;
					case "MediumVehicle":
						mediumVehiclePercentage = Double.parseDouble(value);
						break;
					case "FastVehicle":
						fastVehiclePercentage = Double.parseDouble(value);
						break;
					case "TTC_Threshold":
						TTC_THRESHOLD = Double.parseDouble(value);
						break;
					case "RoadCrossingPedestrianProbability":
						roadCrossingPedestrianProbability = Double.parseDouble(value);
						totalTypesOfObjects++;
						break;
					case "StandingPedestrianProbability":
						standingPedestrianProbability = Double.parseDouble(value);
						totalTypesOfObjects++;
						break;
					case "ParkedCarProbability":
						parkedCarProbability = Double.parseDouble(value);
						totalTypesOfObjects++;
						break;
					case "ParkedRickshawProbability":
						parkedRickshawProbability = Double.parseDouble(value);
						totalTypesOfObjects++;
						break;
					case "ParkedCNGProbability":
						parkedCNGProbability = Double.parseDouble(value);
						totalTypesOfObjects++;
						break;
					default:
						break;
				}
			}
			pixelPerFootpathStrip = pixelPerMeter * footpathStripWidth;
			pixelPerStrip = pixelPerMeter * stripWidth;
			assert (Math.round(slowVehiclePercentage + mediumVehiclePercentage + fastVehiclePercentage) == 100.0);
		} catch (IOException ex) {
			Logger.getLogger(DhakaSimPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void initialize3(String time, String speed, double stdPedProb, double carProb, double rickshawProb, double CNGProb) {
		totalTypesOfObjects = 0;
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("parameter.txt")));
			while (true) {
				String dataLine = bufferedReader.readLine();
				if (dataLine == null) {
					bufferedReader.close();
					break;
				}
				StringTokenizer stringTokenizer = new StringTokenizer(dataLine);
				String name = stringTokenizer.nextToken();
				String value = stringTokenizer.nextToken();
				switch (name) {
					case "SimulationEndTime":
						//simulationEndTime = Integer.parseInt(value);
						simulationEndTime = Integer.parseInt(time);
						break;
					case "PixelPerMeter":
						pixelPerMeter = Integer.parseInt(value);
						break;
					case "SimulationSpeed":
						//simulationSpeed = Integer.parseInt(value);
						simulationSpeed = Integer.parseInt(speed);
						break;
					case "EncounterPerAccident":
						encounterPerAccident = Double.parseDouble(value);
						break;
					case "StripWidth":
						stripWidth = Double.parseDouble(value);
						break;
					case "FootpathStripWidth":
						footpathStripWidth = Double.parseDouble(value);
						break;
					case "MaximumSpeed":
						maximumSpeed = Double.parseDouble(value);
						break;
					case "ObjectMode":
						objectMode = value.equalsIgnoreCase("On");
						break;
					case "DebugMode":
						DEBUG_MODE = value.equalsIgnoreCase("On");
						break;
					case "TraceMode":
						TRACE_MODE = value.equalsIgnoreCase("On");
						break;
					case "RandomSeed":
						seed = Integer.parseInt(value);
						break;
					case "SignalChangeDuration":
						SIGNAL_CHANGE_DURATION = Integer.parseInt(value);
						break;
					case "DefaultTranslateX":
						DEFAULT_TRANSLATE_X = Double.parseDouble(value);
						break;
					case "DefaultTranslateY":
						DEFAULT_TRANSLATE_Y = Double.parseDouble(value);
						break;
					case "CenteredView":
						CENTERED_VIEW = value.equalsIgnoreCase("on");
						break;
					case "DLC_model":
						int o = Integer.parseInt(value);
						switch (o) {
							case 0:
								lane_changing_model = DLC_MODEL.NAIVE_MODEL;
								break;
							case 1:
								lane_changing_model = DLC_MODEL.GHR_MODEL;
								break;
							case 2:
								lane_changing_model = DLC_MODEL.GIPPS_MODEL;
								break;
							default:
								lane_changing_model = DLC_MODEL.GIPPS_MODEL;
								break;
						}
					case "CF_model":
						int p = Integer.parseInt(value);
						switch (p) {
							case 0:
								car_following_model = CAR_FOLLOWING_MODEL.NAIVE_MODEL;
								break;
							case 1:
								car_following_model = CAR_FOLLOWING_MODEL.GIPPS_MODEL;
								break;
							case 2:
								car_following_model = CAR_FOLLOWING_MODEL.HYBRID_MODEL;
								break;
							default:
								car_following_model = CAR_FOLLOWING_MODEL.HYBRID_MODEL;
								break;
						}
					case "SlowVehicle":
						slowVehiclePercentage = Double.parseDouble(value);
						break;
					case "MediumVehicle":
						mediumVehiclePercentage = Double.parseDouble(value);
						break;
					case "FastVehicle":
						fastVehiclePercentage = Double.parseDouble(value);
						break;
					case "TTC_Threshold":
						TTC_THRESHOLD = Double.parseDouble(value);
						break;
					case "RoadCrossingPedestrianProbability":
						roadCrossingPedestrianProbability = Double.parseDouble(value);
						totalTypesOfObjects++;
						break;
					case "StandingPedestrianProbability":
						standingPedestrianProbability = stdPedProb;
						totalTypesOfObjects++;
						break;
					case "ParkedCarProbability":
						parkedCarProbability = carProb;
						totalTypesOfObjects++;
						break;
					case "ParkedRickshawProbability":
						parkedRickshawProbability = rickshawProb;
						totalTypesOfObjects++;
						break;
					case "ParkedCNGProbability":
						parkedCNGProbability = CNGProb;
						totalTypesOfObjects++;
						break;
					default:
						break;
				}
			}
			pixelPerFootpathStrip = pixelPerMeter * footpathStripWidth;
			pixelPerStrip = pixelPerMeter * stripWidth;
			assert (Math.round(slowVehiclePercentage + mediumVehiclePercentage + fastVehiclePercentage) == 100.0);
		} catch (IOException ex) {
			Logger.getLogger(DhakaSimPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void initializePart2() {
		//random = seed < 0 ? new Random() : new Random(seed); //TODO Review whether this is really necessary.
		random = new Random();
		pixelPerFootpathStrip = pixelPerMeter * footpathStripWidth;
		pixelPerStrip = pixelPerMeter * stripWidth; //TODO Check whether this is really necessary
		probabilityOfAccident = encounterPerAccident;
		if (encounterPerAccident < 1) {
			encounterPerAccident = 100 / encounterPerAccident;
		} else {
			encounterPerAccident = 100 - encounterPerAccident + 1;
		}
	}

	private void indexTraceFile() {
		if (TRACE_MODE) {
			simulationStepLineNos  = new ArrayList<>(simulationEndTime);
			try(LineNumberReader traceReader = new LineNumberReader(new FileReader("trace.txt"))) {
				String s;
				while((s = traceReader.readLine()) != null) {
					if (s.startsWith("Sim")) {
						simulationStepLineNos.add(traceReader.getLineNumber());
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
