package test;

import org.math.plot.Plot2DPanel;

import javax.swing.*;
import java.awt.*;

class AccidentCountPlot {

    AccidentCountPlot(double[] x) {
        // Adding Plots
        Plot2DPanel plot = new Plot2DPanel();
        plot.addBarPlot("Number of accidents occured", x);
        Font font = new Font("Times New Roman", Font.BOLD, 20);
        plot.setFont(font);
        plot.addLegend("SOUTH");
        plot.setAxisLabels("Link ID", "Count");

        // change axe title position relatively to the base of the plot
        plot.getAxis(0).setLabelPosition(0.5, -0.15);
        plot.getAxis(0).setLightLabelFont(font);
        plot.getAxis(0).setLabelFont(font);

        plot.getAxis(1).setLightLabelFont(font);
        plot.getAxis(1).setLabelFont(font);

        // put the PlotPanel in a JFrame, as a JPanel
        JFrame frame = new JFrame("Accident Count");
        frame.setSize(720, 720);
        frame.setContentPane(plot);
        frame.setVisible(true);
    }
}
