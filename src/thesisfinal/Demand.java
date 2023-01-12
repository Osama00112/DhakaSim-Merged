package thesisfinal;

import java.util.ArrayList;

public class Demand {

    private int source;
    private int destination;
    private int demandValue;
    private ArrayList<Path> pathList;

    public Demand(int source, int destination, int demandValue) {
        this.source = source;
        this.destination = destination;
        this.demandValue = demandValue;
        this.pathList = new ArrayList<>();
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public int getDemandValue () {
        return demandValue;
    }

    public void setDemandValue (int demandValue) {
        this.demandValue = demandValue;
    }

    public Path getPath(int index) {
        return pathList.get(index);
    }

    public void addPath(Path path) {
        pathList.add(path);
    }

    public int getNumberOfPaths() {
        return pathList.size();
    }

}
