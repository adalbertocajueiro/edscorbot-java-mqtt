package es.us.score.edscorbot.controller;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import es.us.score.edscorbot.service.MqttService;

@SpringBootApplication
public class EdscorbotMqttApplication implements CommandLineRunner {

	public EdscorbotMqttApplication() throws MqttException, InterruptedException{
		new MqttService();
	}

	public static void main(String[] args) {
		SpringApplication.run(EdscorbotMqttApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		boolean SERVER_UP = true;

		while (SERVER_UP){
			;
		}
	}

}
