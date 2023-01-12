package thesisfinal;

import java.util.ArrayList;

import static java.lang.Integer.min;

public class Node {

    private int index;
    private int id;
    double x;
    double y;
    private int timePassed;

    private ArrayList<Integer> linkList = new ArrayList<>();
    ArrayList<IntersectionStrip> intersectionStripList = new ArrayList<>();
    private ArrayList<Vehicle> vehicleList = new ArrayList<>();
    private ArrayList<IntersectionStripBundle> intersectionStripBundles = new ArrayList<>();
    private int activeBundleIndex;
    private int pressureOnActiveBundle;
    private final static int MIN_VEHICLES_TO_MAKE_A_SIGNAL_GREEN = 1;

    Node(int index, int nodId, double x, double y) {
        this.index = index;
        this.id = nodId;
        this.x = x;
        this.y = y;
        this.timePassed = 0;
        this.activeBundleIndex = 0;
        pressureOnActiveBundle = 0;
    }

    /**
     * completes the bundle creation task
     */
    void createBundles() {
        for (int linkIndex : linkList) {
            intersectionStripBundles.add(new IntersectionStripBundle(linkIndex));
        }
        changeSignalOfActiveBundleInto(SIGNAL.GREEN);
    }

    private boolean addIntersectionStripToBundle(IntersectionStrip intersectionStrip) {
        for (IntersectionStripBundle intersectionStripBundle : intersectionStripBundles) {
            if (intersectionStripBundle.getIntersectionBundleIndex() == intersectionStrip.startLinkIndex) {
                intersectionStripBundle.addIntersectionStrip(intersectionStrip);
                return true;
            }
        }
        return false;
    }

    /**
     * @param startLinkIndex get the bundle which startLinkIndex matches this parameter
     * @return the bundle if found otherwise null
     */
    private IntersectionStripBundle getBundle(int startLinkIndex) {
        for (IntersectionStripBundle pb : intersectionStripBundles) {
            if (pb.getIntersectionBundleIndex() == startLinkIndex) {
                return pb;
            }
        }
        return null;
    }

    boolean isBundleActive(int startLinkIndex) {
        IntersectionStripBundle pb = getBundle(startLinkIndex);
        if (pb != null) {
            return pb.getSignal() == SIGNAL.GREEN;
        }
        return false;
    }

    private void changeSignalOfActiveBundleInto(SIGNAL signal) {
        intersectionStripBundles.get(activeBundleIndex).setSignal(signal);
    }

    private int getNextActiveBundle() {
        return (activeBundleIndex + 1) % intersectionStripBundles.size();
    }

    void automaticSignaling(int simulationTime) {
        System.out.print("");
        if (simulationTime - timePassed >= min(Parameters.SIGNAL_CHANGE_DURATION + pressureOnActiveBundle, 120)) {
            if (intersectionStripBundles.get(activeBundleIndex).getSignal() == SIGNAL.GREEN) {
                changeSignalOfActiveBundleInto(SIGNAL.YELLOW);
            } else {
                int currentActiveBundle = activeBundleIndex;
                do {
                    switchSignal();
                    if (activeBundleIndex == currentActiveBundle) {
                        break;
                    }
                } while (pressureOnActiveBundle < MIN_VEHICLES_TO_MAKE_A_SIGNAL_GREEN);
                timePassed = simulationTime;
            }
        } else {
            try {
                IntersectionStripBundle isb = intersectionStripBundles.get(activeBundleIndex);
                if (isb.getPressureOnBundle() == 0) {
                    changeSignalOfActiveBundleInto(SIGNAL.YELLOW);
                }
            } catch (NullPointerException ignored) {
            }
        }
    }


    /**
     * turns current signal into red and next into green
     */
    private void switchSignal() {
        changeSignalOfActiveBundleInto(SIGNAL.RED);
        activeBundleIndex = getNextActiveBundle();
        changeSignalOfActiveBundleInto(SIGNAL.GREEN);
        try {
            IntersectionStripBundle isb = intersectionStripBundles.get(activeBundleIndex);
            pressureOnActiveBundle = isb.getPressureOnBundle();
        } catch (NullPointerException ne) {
            pressureOnActiveBundle = 0;
        }
    }

    boolean intersectionStripExists(int startLink, int startStrip, int endLink, int endStrip) {
        for (IntersectionStrip intersectionStrip : intersectionStripList) {
            if (intersectionStrip.startLinkIndex == startLink
                    && intersectionStrip.startStrip == startStrip
                    && intersectionStrip.endLinkIndex == endLink
                    && intersectionStrip.endStrip == endStrip) {
                return true;
            }
        }
        return false;
    }

    int getMyIntersectionStrip(int startLink, int startStrip, int endLink, int endStrip) {
        for (int i = 0; i < intersectionStripList.size(); i++) {
            if (intersectionStripList.get(i).startLinkIndex == startLink
                    && intersectionStripList.get(i).startStrip == startStrip
                    && intersectionStripList.get(i).endLinkIndex == endLink
                    && intersectionStripList.get(i).endStrip == endStrip) {
                return i;
            }
        }
        return -1;
    }

    @SuppressWarnings("Duplicates")
    private boolean isColliding(Vehicle a, Vehicle b) {
        for (int x = 0; x < 2; x++) {
            Vehicle vehicle = (x == 0) ? a : b;

            for (int i1 = 0; i1 < vehicle.getCorners().length; i1++) {
                int i2 = (i1 + 1) % vehicle.getCorners().length;
                Point2D p1 = vehicle.getCorners()[i1];
                Point2D p2 = vehicle.getCorners()[i2];

                Point2D normal = new Point2D(p2.y - p1.y, p1.x - p2.x);

                double minA = Double.MAX_VALUE;
                double maxA = Double.MIN_VALUE;

                for (Point2D p : a.getCorners()) {
                    double projected = normal.x * p.x + normal.y * p.y;

                    if (projected < minA)
                        minA = projected;
                    if (projected > maxA)
                        maxA = projected;
                }

                double minB = Double.MAX_VALUE;
                double maxB = Double.MIN_VALUE;

                for (Point2D p : b.getCorners()) {
                    double projected = normal.x * p.x + normal.y * p.y;

                    if (projected < minB)
                        minB = projected;
                    if (projected > maxB)
                        maxB = projected;
                }

                if (maxA < minB || maxB < minA)
                    return false;
            }
        }

        return true;
    }

    Vehicle getOverlappingVehicle(Vehicle v) {
        v.calculateCornerPoints();
        for (Vehicle vehicle : vehicleList) {
            if (vehicle != v) {
                vehicle.calculateCornerPoints();
                if (isColliding(vehicle, v)) {
                    return vehicle;
                }
            }
        }
        return null;
    }

    boolean doOverlap(Vehicle v) {
        v.calculateCornerPoints();
        for (Vehicle vehicle : vehicleList) {
            if (vehicle != v) {
                vehicle.calculateCornerPoints();
                if (isColliding(vehicle, v)) {
                    return true;
                }
            }
        }
        return false;
    }

    void manualSignaling() {
        if (isNodeClear()) {
            int currentActiveBundle = activeBundleIndex;
            do {
                switchSignal();
                if (activeBundleIndex == currentActiveBundle) {
                    break;
                }
            } while (pressureOnActiveBundle < MIN_VEHICLES_TO_MAKE_A_SIGNAL_GREEN);
            timePassed = Parameters.simulationStep;

        }
    }

    void removeVehicle(Vehicle v) {
        vehicleList.remove(v);
    }

    private boolean isNodeClear() {
        return vehicleList.isEmpty();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getTimePassed() {
        return timePassed;
    }

    public void setTimePassed(int timePassed) {
        this.timePassed = timePassed;
    }

    int getLink(int index) {
        return linkList.get(index);
    }

    void addLink(int link) {
        linkList.add(link);
    }

    int numberOfLinks() {
        return linkList.size();
    }

    IntersectionStrip getIntersectionStrip(int index) {
        return intersectionStripList.get(index);
    }

    void addIntersectionStrip(IntersectionStrip intersectionStrip) {
        intersectionStripList.add(intersectionStrip);
        if (!addIntersectionStripToBundle(intersectionStrip)) {
            System.out.println("Problem");
        }
    }

    public int numberOfIntersectionStrips() {
        return intersectionStripList.size();
    }

    public Vehicle getVehicle(int index) {
        return vehicleList.get(index);
    }

    public void addVehicle(Vehicle vehicle) {
        vehicleList.add(vehicle);
    }

    public int numberOfVehicles() {
        return vehicleList.size();
    }

}
