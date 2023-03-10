package es.us.robot.edscorbot.models;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class MovedObject {
    private Client client;
    private boolean errorState;
    private Point content;
   

    public MovedObject() {
    }

    public MovedObject(Client client, boolean error, Point content) {
        this.client = client;
        this.errorState= error;
        this.content = content;
    }
    
}
