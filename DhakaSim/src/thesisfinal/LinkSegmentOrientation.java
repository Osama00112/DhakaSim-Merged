package thesisfinal;

import static java.lang.Math.min;
import static thesisfinal.Utilities.getDistance;

public class LinkSegmentOrientation {
    boolean reverseLink;
    boolean reverseSegment;

    static LinkSegmentOrientation getLinkAndSegmentOrientation(double x, double y, Segment firstSegment, Segment lastSegment) {
        LinkSegmentOrientation linkSegmentOrientation = new LinkSegmentOrientation();
        double distance1 = getDistance(x, y, firstSegment.getStartX(), firstSegment.getStartY());
        double distance2 = getDistance(x, y, firstSegment.getEndX(), firstSegment.getEndY());
        double distance3 = getDistance(x, y, lastSegment.getStartX(), lastSegment.getStartY());
        double distance4 = getDistance(x, y, lastSegment.getEndX(), lastSegment.getEndY());
        double min = min(distance1, min(distance2, min(distance3, distance4)));
        if (firstSegment != lastSegment) {
            if (min == distance1) {
                linkSegmentOrientation.reverseLink = false;
                linkSegmentOrientation.reverseSegment = false;
            } else if (min == distance2) {
                linkSegmentOrientation.reverseLink = false;
                linkSegmentOrientation.reverseSegment = true;
            } else if (min == distance3) {
                linkSegmentOrientation.reverseLink = true;
                linkSegmentOrientation.reverseSegment = false;
            } else if (min == distance4) {
                linkSegmentOrientation.reverseLink = true;
                linkSegmentOrientation.reverseSegment = true;
            }
        } else {
            if (min == distance1) {
                linkSegmentOrientation.reverseLink = false;
                linkSegmentOrientation.reverseSegment = false;
            } else {
                linkSegmentOrientation.reverseLink = true;
                linkSegmentOrientation.reverseSegment = true;
            }
        }
        return linkSegmentOrientation;
    }

}