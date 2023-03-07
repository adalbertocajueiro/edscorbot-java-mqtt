package es.us.robot.edscorbot.models;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CommandObject {
    private int signal;
    private Client client;
    private int status;
    private Object content;
   

    public CommandObject(int signal) {
        this.signal = signal;
    }

    public CommandObject(int signal, Client client, int status, Object content) {
        this.signal = signal;
        this.client = client;
        this.status = status;
        this.content = content;
    }
    
}
