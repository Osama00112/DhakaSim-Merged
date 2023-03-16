package thesisfinal;

import java.util.ArrayList;

public class Path {

    private int source;
    private int destination;
    private ArrayList<Integer> linkList = new ArrayList<>();

    public Path(int source, int destination) {
        this.source = source;
        this.destination = destination;
    }

    int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    int getLink(int index) {
        return linkList.get(index);
    }

    void addLink(int link) {
        linkList.add(link);
    }

    int getNumberOfLinks() {
        return linkList.size();
    }

}
