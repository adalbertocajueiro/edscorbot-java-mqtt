package es.us.robot.edscorbot.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Client {

    private String id;

    public Client(String id) {
        this.id = id;
    }

    public Client() {
    }

    @Override
    public String toString() {
        return this.id;
    }
    
}
