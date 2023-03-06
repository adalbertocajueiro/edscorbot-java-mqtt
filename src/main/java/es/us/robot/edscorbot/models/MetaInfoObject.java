package es.us.robot.edscorbot.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

import es.us.robot.edscorbot.util.ArmMetaInfo;;

@Getter
@Setter
public class MetaInfoObject {
    private ArmMetaInfo signal;
    private String name;
    private List<JointInfo> joints;

    public MetaInfoObject() {
        this.joints = new ArrayList<JointInfo>();
    }

    public MetaInfoObject(ArmMetaInfo option, String name, List<JointInfo> joints) {
        this.signal = option;
        this.name = name;
        this.joints = joints;
    }
}
