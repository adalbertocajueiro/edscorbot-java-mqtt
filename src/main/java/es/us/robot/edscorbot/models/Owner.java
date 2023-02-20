package es.us.robot.edscorbot.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Owner {

    private String id;

    public Owner(String id) {
        this.id = id;
    }

    public Owner() {
    }

}
