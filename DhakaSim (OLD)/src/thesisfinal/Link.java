package thesisfinal;

import java.awt.*;
import java.util.ArrayList;

public class Link {

    private int index;
    private int id;
    private int upNode;
    private int downNode;
    private ArrayList<Segment> segmentList = new ArrayList<>();

    Link(int index, int id, int upNode, int downNode) {
        this.index = index;
        this.id = id;
        this.upNode = upNode;
        this.downNode = downNode;
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

    int getUpNode() {
        return upNode;
    }

    public void setUpNode(int upNode) {
        this.upNode = upNode;
    }

    int getDownNode() {
        return downNode;
    }

    public void setDownNode(int downNode) {
        this.downNode = downNode;
    }

    Segment getSegment(int index) {
        return segmentList.get(index);
    }

    Segment getFirstSegment() {
        assert !segmentList.isEmpty();

        return segmentList.get(0);
    }

    Segment getLastSegment() {
        assert !segmentList.isEmpty();

        return segmentList.get(segmentList.size() - 1);
    }

    void addSegment(Segment segment) {
        segmentList.add(segment);
    }

    int getNumberOfSegments() {
        return segmentList.size();
    }

    public void draw(Graphics2D g2d) {
        for (int i = 0; i < getNumberOfSegments(); i++) {
            getSegment(i).draw(g2d);
        }
    }

}
