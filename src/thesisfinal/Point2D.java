package thesisfinal;


class Point2D {
    double x;
    double y;

    Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    @Override
    public String toString(){
        return "(" + x + "," + y + ")";
    }
}
