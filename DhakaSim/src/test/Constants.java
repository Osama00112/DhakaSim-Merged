package test;

import java.awt.*;

public interface Constants {
    int TYPES_OF_CARS = 13;
    int PEDESTRIAN_LIMIT = 100; // probability of generating pedestrian is 1/this parameter
    Color pedestrianColor = Color.WHITE;
    Color roadBorderColor = Color.BLACK;
    Color backgroundColor = Color.WHITE;// new Color(105, 105, 105);//Color.DARK_GRAY;
    double DEFAULT_SCALE = 5;
    double TIME_STEP = 0.1;
    double THRESHOLD_DISTANCE = 2.0;
    boolean PRINT_RESULT = true;
}