package test;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author USER
 */
public class Pedestrian {

    boolean toRemove = false;
    private Segment segment;
    private double initPos;
    private double distance;
    private double speed;
    private Strip strip;
    boolean inAccident = false;
    private boolean reverse;
    public int clock = DhakaSimFrame.simulationStep;
    public int index;

    public Pedestrian(Segment seg, int strip, double initpos, double sp) {
        //System.out.println(init pos);
        if (strip != 0) {
            reverse = true;
            this.strip = seg.getStrip(seg.numberOfStrips() - 1);
        } else {
            reverse = false;
            this.strip = seg.getStrip(0);
        }
        segment = seg;
        initPos = initpos;
        distance = 0;
        speed = sp;
        this.strip.addPedestrian(this);
    }

    private double getDistance() {
        return distance;
    }

    double getInitPos() {
        return initPos;
    }

    Segment getSegment() {
        return segment;
    }

    void moveForward() {
        if (!reverse) {
            int x;
            //x = (int) ((distance + speed) /Strip.stripWidth);
            if (distance + speed < DhakaSimFrame.footpathStripWidth//segment.getFpWidth()
            ) {
                distance += speed;
                return;
            }
            x = /*segment.getFootPathStripCount()*/ 1 + (int) ((distance - DhakaSimFrame.footpathStripWidth + speed) / DhakaSimFrame.stripWidth);
            if (x < segment.numberOfStrips()) {
                if (segment.getStrip(x).hasGapForPedestrian(this)) {
                    distance = distance + speed;
                    strip.delPedestrian(this);
                    setStrip(segment.getStrip(x));
                    strip.addPedestrian(this);
                }

            } else {
                distance = distance + speed;
                strip.delPedestrian(this);
            }
        } else {
            int x;
            //x = (int) ((distance + speed) /Strip.stripWidth);
            double w = 1 * DhakaSimFrame.footpathStripWidth + (segment.numberOfStrips() - 1) * DhakaSimFrame.stripWidth;
            x = /*segment.getFootPathStripCount()*/ 1 + (int) ((w - distance - speed - DhakaSimFrame.footpathStripWidth) / DhakaSimFrame.stripWidth);
            if (x > 1//segment.getFootPathStripCount()
            ) {
                if (segment.getStrip(x).hasGapForPedestrian(this)) {
                    distance = distance + speed;
                    strip.delPedestrian(this);
                    setStrip(segment.getStrip(x));
                    strip.addPedestrian(this);
                }

            } else {
                distance = distance + speed;
                strip.delPedestrian(this);
            }
        }
    }

    public Strip getStrip() {
        return strip;
    }

    public void setSegment(Segment segment) {
        this.segment = segment;
    }

    private void setStrip(Strip strip) {
        this.strip = strip;
    }

    boolean hasCrossedRoad() {
        //System.out.println(distance + " " + segment.getSegWidth() + " " + strip.getStripIndex());
        return distance >= segment.getSegmentWidth();
    }

    void drawMobilePedestrian(BufferedWriter traceWriter, Graphics2D g, double stripPixelCount, double mpRatio, double fpStripPixelCount) {
        if (!reverse) {
            Segment seg = getSegment();
            double segmentLength = seg.getLength();
            double length = 1;
            //Using internally section or ratio formula,it finds the coordinates along which vehicles are
            double xp = (getInitPos() * seg.getEndX() + (segmentLength - getInitPos()) * seg.getStartX()) / segmentLength * mpRatio;
            double yp = (getInitPos() * seg.getEndY() + (segmentLength - getInitPos()) * seg.getStartY()) / segmentLength * mpRatio;
            double xq = ((getInitPos() + length) * seg.getEndX() + (segmentLength - (getInitPos() + length)) * seg.getStartX()) / segmentLength * mpRatio;
            double yq = ((getInitPos() + length) * seg.getEndY() + (segmentLength - (getInitPos() + length)) * seg.getStartY()) / segmentLength * mpRatio;
            int x1 = (int) Math.round(Utilities.returnX3(xp, yp, xq, yq, (getDistance() / DhakaSimFrame.footpathStripWidth) * fpStripPixelCount)); //obj.getDistance()*mpRatio
            int y1 = (int) Math.round(Utilities.returnY3(xp, yp, xq, yq, (getDistance() / DhakaSimFrame.footpathStripWidth) * fpStripPixelCount));
            if (inAccident) {
                g.setColor(Color.red);
                g.fillOval(x1, y1, 10, 10);
            } else {
                g.setColor(Constants.pedestrianColor);
                g.fillOval(x1, y1, 7, 7);
            }

            try {
                traceWriter.write(x1 + " " + y1 + " " + inAccident);
                traceWriter.newLine();
            } catch (IOException ex) {
                Logger.getLogger(Pedestrian.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            Segment seg = getSegment();
            double segmentLength = seg.getLength();
            double length = 1;
            //Using internally section or ratio formula,it finds the coordinates along which vehicles are
            double xp = (getInitPos() * seg.getEndX() + (segmentLength - getInitPos()) * seg.getStartX()) / segmentLength * mpRatio;
            double yp = (getInitPos() * seg.getEndY() + (segmentLength - getInitPos()) * seg.getStartY()) / segmentLength * mpRatio;
            double xq = ((getInitPos() + length) * seg.getEndX() + (segmentLength - (getInitPos() + length)) * seg.getStartX()) / segmentLength * mpRatio;
            double yq = ((getInitPos() + length) * seg.getEndY() + (segmentLength - (getInitPos() + length)) * seg.getStartY()) / segmentLength * mpRatio;
            double w = 1 * DhakaSimFrame.footpathStripWidth + (segment.numberOfStrips() - 1) * DhakaSimFrame.stripWidth;
            int wi = (int) (w - getDistance());
            int x1 = (int) Math.round(Utilities.returnX3(xp, yp, xq, yq, (wi / DhakaSimFrame.footpathStripWidth) * fpStripPixelCount)); //obj.getDistance()*mpRatio
            int y1 = (int) Math.round(Utilities.returnY3(xp, yp, xq, yq, (wi / DhakaSimFrame.footpathStripWidth) * fpStripPixelCount));
            if (inAccident) {
                g.setColor(Color.red);
                g.fillOval(x1, y1, 10, 10);
            } else {
                g.setColor(Constants.pedestrianColor);
                g.fillOval(x1, y1, 7, 7);
            }
            try {
                traceWriter.write(x1 + " " + y1 + " " + inAccident);
                traceWriter.newLine();
            } catch (IOException ex) {
                Logger.getLogger(Pedestrian.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void printObject() {
        System.out.println(index + " " + initPos + " " + distance + " " + speed);
    }

    boolean isToRemove() {
        return toRemove;
    }

    boolean isInAccident() {
        return inAccident;
    }

    void setToRemove(boolean b) {
        toRemove = b;
    }
}
