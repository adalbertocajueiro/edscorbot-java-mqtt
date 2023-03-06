package es.us.robot.edscorbot.models;

import lombok.Getter;
import lombok.Setter;

import es.us.robot.edscorbot.util.ArmStatus;

@Getter
@Setter
public class CommandObject {
    private int signal;
    private Client client;
    private ArmStatus status;
    private Object content;
   

    public CommandObject(int signal) {
        this.signal = signal;
    }

    public CommandObject(int signal, Client client, ArmStatus status, Object content) {
        this.signal = signal;
        this.client = client;
        this.status = status;
        this.content = content;
    }
    
}
