package es.us.robot.edscorbot.models;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Point{

    private List<Double> coordinates;

    public Point() {
        this.coordinates = new ArrayList<Double>();
    }

    public Point(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.coordinates.toArray());
    }

    
}
