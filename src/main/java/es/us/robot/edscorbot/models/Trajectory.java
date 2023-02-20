package es.us.robot.edscorbot.models;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Trajectory {

    private Owner owner;

    private List<Point> points;

    private long timestamp;

    public Trajectory() {
        this.points = new ArrayList<Point>();
    }


    public Trajectory(Owner owner, long timestamp, List<Point> points) {
        this.owner = owner;
        this.timestamp = timestamp;
        this.points = points;
    }
}
