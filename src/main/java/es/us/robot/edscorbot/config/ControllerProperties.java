package es.us.robot.edscorbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "controller")
@Getter
@Setter
public class ControllerProperties {

    private String name;
    private int joints;

    
}
