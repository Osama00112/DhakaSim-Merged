package thesisfinal;

import java.util.ArrayList;

/**
 * @author mushfiq
 */
public class IntersectionStripBundle {
    private ArrayList<IntersectionStrip> intersectionStrips;
    private int intersectionBundleIndex; // this is the index of the link from where vehicle will enter into the intersection
    private Segment leavingSegment;
    private boolean isReverseSegment;
    private SIGNAL signal;

    IntersectionStripBundle(int intersectionBundleIndex) {
        intersectionStrips = new ArrayList<>();
        this.intersectionBundleIndex = intersectionBundleIndex;
        signal = SIGNAL.RED;
        leavingSegment = null;
    }

    void addIntersectionStrip(IntersectionStrip intersectionStrip) {
        assert intersectionBundleIndex == intersectionStrip.startLinkIndex;
        intersectionStrips.add(intersectionStrip);
        if (leavingSegment == null) {
            setSegmentProperties();
        }
    }

    int getPressureOnBundle() {
        int vs;
        if (isReverseSegment) {
            vs = leavingSegment.getReverseVehicleCount();
        } else {
            vs = leavingSegment.getForwardVehicleCount();
        }
        double areaOfSegment = leavingSegment.getWidth() * leavingSegment.getLength();
        int pb = (int) (5000 * vs / areaOfSegment);
        return vs;
    }

    private void setSegmentProperties() {
        leavingSegment = intersectionStrips.get(0).leavingSegment;
        isReverseSegment = intersectionStrips.get(0).startStrip >= leavingSegment.middleHighStripIndex;
    }

    public void clearBundle() {
        intersectionStrips.clear();
    }

    int getIntersectionBundleIndex() {
        return intersectionBundleIndex;
    }

    public void setIntersectionBundleIndex(int intersectionBundleIndex) {
        this.intersectionBundleIndex = intersectionBundleIndex;
    }

    SIGNAL getSignal() {
        return signal;
    }

    void setSignal(SIGNAL signal) {
        this.signal = signal;
    }
}
