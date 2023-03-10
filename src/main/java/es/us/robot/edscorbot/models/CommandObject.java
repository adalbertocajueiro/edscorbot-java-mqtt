package es.us.robot.edscorbot.models;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CommandObject {
    private int signal;
    private Client client;
    private boolean errorState;
    private Object content;
   

    public CommandObject(int signal) {
        this.signal = signal;
    }

    public CommandObject(int signal, Client client, boolean error, Object content) {
        this.signal = signal;
        this.client = client;
        this.errorState= error;
        this.content = content;
    }
    
}
