package es.us.score.edscorbot.util;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Point{

    private double j1Ref;
    private double j2Ref;
    private double j3Ref;
    private double j4Ref;
    public Point() {
    }
    public Point(double j1Ref, double j2Ref, double j3Ref, double j4Ref) {
        this.j1Ref = j1Ref;
        this.j2Ref = j2Ref;
        this.j3Ref = j3Ref;
        this.j4Ref = j4Ref;
    }
    @Override
    public String toString() {
        return String.format("(%s,%s,%s,%s)", j1Ref, j2Ref, j3Ref, j4Ref);
    }
}
