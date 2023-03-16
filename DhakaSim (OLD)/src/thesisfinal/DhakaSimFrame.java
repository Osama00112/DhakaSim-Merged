package thesisfinal;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.round;

public class DhakaSimFrame extends JFrame {

    static int simulationStep;
    static int simulationEndTime;
    static double pixelPerStrip;
    static double pixelPerMeter;
    static double pixelPerFootpathStrip;
    static int simulationSpeed;
    static double encounterPerAccident;//-----------------------------------rename and redefine
    static double stripWidth;
    static double footpathStripWidth;
    static double maximumSpeed;
    static boolean pedestrianMode;

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

    enum DLC_MODEL {    // Discretionary lane changing models
        NAIVE_MODEL,    // no model at all; just instantly changes lane if possible
        GIPPS_MODEL,
        GHR_MODEL,
        MOBIL_MODEL
    }

    enum CAR_FOLLOWING_MODEL {
        NAIVE_MODEL,    // no model at all; just moves as fast as possible and brakes as hard as possible
        GIPPS_MODEL,    // moving velocity is controlled; but no braking; so collision could not be avoided
        HYBRID_MODEL,    // velocity from GIPPS model, braking hard in emergency
        KRAUSS_MODEL,
        GFM_MODEL,
        IDM_MODEL,
        RVF_MODEL,
        VFIAC_MODEL,
        OVCM_MODEL,
        KFTM_MODEL,
        HDM_MODEL
    }

    enum VEHICLE_GENERATION_RATE {
        POISSON,
        CONSTANT
    }

    static DLC_MODEL lane_changing_model;
    static CAR_FOLLOWING_MODEL car_following_model;
    static VEHICLE_GENERATION_RATE vehicle_generation_rate;
    static boolean ERROR_MODE;

    static JSlider showProgressSlider;

    static ArrayList<Integer> simulationStepLineNos;

    public DhakaSimFrame() {
        initialize();
        indexTraceFile();
        getContentPane().add(new OptionPanel(this));
        setTitle("DhakaSim");
        setSize(1250, 700);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initialize() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("input/parameter.txt")));
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
                    case "PedestrianMode":
                        pedestrianMode = value.equalsIgnoreCase("On");
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
                        SIGNAL_CHANGE_DURATION = (int)(round(SIGNAL_CHANGE_DURATION / Constants.TIME_STEP));
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
                            case 3:
                                lane_changing_model = DLC_MODEL.MOBIL_MODEL;
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
                                car_following_model = CAR_FOLLOWING_MODEL.HYBRID_MODEL;
                                break;
                            case 2:
                                car_following_model = CAR_FOLLOWING_MODEL.GIPPS_MODEL;
                                break;
                            case 4:
                                car_following_model = CAR_FOLLOWING_MODEL.KRAUSS_MODEL;
                                break;
                            case 5:
                                // no collisions
                                car_following_model = CAR_FOLLOWING_MODEL.GFM_MODEL;
                                break;
                            case 3:
                                //todo lots of collisions
                                car_following_model = CAR_FOLLOWING_MODEL.IDM_MODEL;
                                break;
                            case 9:
                                car_following_model = CAR_FOLLOWING_MODEL.RVF_MODEL;
                                break;
                            case 8:
                                car_following_model = CAR_FOLLOWING_MODEL.VFIAC_MODEL;
                                break;
                            case 10:
                                // no collision (1s->time step)
                                car_following_model = CAR_FOLLOWING_MODEL.OVCM_MODEL;
                                break;
                            case 7:
                                car_following_model = CAR_FOLLOWING_MODEL.KFTM_MODEL;
                                break;
                            case 6:
                                car_following_model = CAR_FOLLOWING_MODEL.HDM_MODEL;
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
                    case "VehicleGenerationRate":
                        int q = Integer.parseInt(value);
                        switch (q) {
                            case 0:
                                vehicle_generation_rate = VEHICLE_GENERATION_RATE.CONSTANT;
                                break;
                            case 1:
                                vehicle_generation_rate = VEHICLE_GENERATION_RATE.POISSON;
                                break;
                            default:
                                vehicle_generation_rate = VEHICLE_GENERATION_RATE.CONSTANT;
                                break;
                        }
                    case "ErrorMode":
                        ERROR_MODE = value.equalsIgnoreCase("On");
                        break;
                    default:
                        break;
                }
            }
            pixelPerFootpathStrip = pixelPerMeter * footpathStripWidth;
            pixelPerStrip = pixelPerMeter * stripWidth;
            assert (round(slowVehiclePercentage + mediumVehiclePercentage + fastVehiclePercentage) == 100.0);

        } catch (IOException ex) {
            Logger.getLogger(DhakaSimPanel.class.getName()).log(Level.SEVERE, null, ex);
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
