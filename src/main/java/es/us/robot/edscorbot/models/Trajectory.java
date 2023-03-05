package es.us.robot.edscorbot.models;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Trajectory {

    private List<Point> points;

    public Trajectory() {
        this.points = new ArrayList<Point>();
    }

    public Trajectory(List<Point> points) {
        this.points = points;
    }
}
