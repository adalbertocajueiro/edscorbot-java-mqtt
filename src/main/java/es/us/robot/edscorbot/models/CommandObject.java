package es.us.robot.edscorbot.models;

import es.us.robot.edscorbot.util.CommandSignal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommandObject {
    private CommandSignal signal;
    private Client client;
    private Object content;

    public CommandObject(CommandSignal signal) {
        this.signal = signal;
    }

    public CommandObject(CommandSignal signal, Client client, Object content) {
        this.signal = signal;
        this.client = client;
        this.content = content;
    }
    
}
