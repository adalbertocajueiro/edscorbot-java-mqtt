package es.us.score.edscorbot.util;

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
