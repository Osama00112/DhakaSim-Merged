package thesisfinal;

import org.math.plot.Plot2DPanel;

import javax.swing.*;
import java.awt.*;

class SensorVehicleAvgSpeedPlot {

    //plots vehicle average speed vs segment
    SensorVehicleAvgSpeedPlot(double[] Y) {
        // Adding Plots
        // create your PlotPanel (you can use it as a JPanel)
        Plot2DPanel plot = new Plot2DPanel();
        plot.addBarPlot("Average vehicle speed (km per hour)", Y);
        Font font = new Font("Times New Roman", Font.BOLD, 20);
        plot.setFont(font);
        plot.addLegend("SOUTH");
        plot.setAxisLabels("Link ID", "Speed (kph)");

        // change axe title position relatively to the base of the plot
        plot.getAxis(0).setLabelPosition(0.5, -0.15);
        plot.getAxis(0).setLightLabelFont(font);
        plot.getAxis(0).setLabelFont(font);

        plot.getAxis(1).setLightLabelFont(font);
        plot.getAxis(1).setLabelFont(font);

        // put the PlotPanel in a JFrame, as a JPanel
        JFrame frame = new JFrame("Vehicle Average Speed");
        frame.setSize(720, 720);
        frame.setContentPane(plot);
        frame.setVisible(true);
    }

}
