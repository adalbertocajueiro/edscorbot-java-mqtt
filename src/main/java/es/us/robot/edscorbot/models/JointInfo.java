package es.us.robot.edscorbot.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JointInfo {
    private int minimum;
    private int maximum;
    
    public JointInfo() {
    
    }

    public JointInfo(int minimum, int maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }
    
}
