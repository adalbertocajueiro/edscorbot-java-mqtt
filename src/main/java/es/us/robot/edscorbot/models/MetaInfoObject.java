package es.us.robot.edscorbot.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;


@Getter
@Setter
public class MetaInfoObject {
    private int signal; //can be two values: ARM_GET_METAINFO = 1 and ARM_METAINFO
    private String name;
    private List<JointInfo> joints;

    public MetaInfoObject() {
        this.joints = new ArrayList<JointInfo>();
    }

    public MetaInfoObject(int signal, String name, List<JointInfo> joints) {
        this.signal = signal;
        this.name = name;
        this.joints = joints;
    }
    
}
