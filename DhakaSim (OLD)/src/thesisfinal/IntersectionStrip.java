package thesisfinal;

import static thesisfinal.Utilities.getDistance;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author mishkat076
 */
public class IntersectionStrip {
    int startLinkIndex;
    int startStrip;
    int endLinkIndex;
    int endStrip;
    double startPointX;
    double startPointY;
    double endPointX;
    double endPointY;
    Segment leavingSegment;
    Segment enteringSegment;

    IntersectionStrip(int startLinkIndex, int startStrip, int endLinkIndex, int endStrip, double startPointX, double startPointY, double endPointX, double endPointY, Segment leavingSegment, Segment enteringSegment) {
        this.startLinkIndex = startLinkIndex;
        this.startStrip = startStrip;
        this.endLinkIndex = endLinkIndex;
        this.endStrip = endStrip;
        this.startPointX = startPointX;
        this.startPointY = startPointY;
        this.endPointX = endPointX;
        this.endPointY = endPointY;
        this.leavingSegment = leavingSegment;
        this.enteringSegment = enteringSegment;
    }

    public int getStartLinkIndex() {
        return startLinkIndex;
    }

    double getLength() {
        return getDistance(startPointX, startPointY, endPointX, endPointY);
    }

}
