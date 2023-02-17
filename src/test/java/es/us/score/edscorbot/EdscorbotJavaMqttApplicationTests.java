package es.us.score.edscorbot;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.Gson;

import es.us.score.edscorbot.controller.EdscorbotMqttApplication;
import es.us.score.edscorbot.util.Constants;
import es.us.score.edscorbot.util.Owner;

@SpringBootTest //(classes = EdscorbotMqttApplication.class)
// @ContextConfiguration (classes = EdscorbotMqttApplication.class)
class EdscorbotJavaMqttApplicationTests {

	MqttConnectOptions options = new MqttConnectOptions();
	private IMqttClient mqttClientPublisher = null;

	
	private EdscorbotJavaMqttApplicationTests() throws MqttException{
		mqttClientPublisher = new MqttClient("tcp://" + Constants.hostname + ":" + Constants.port,
				"SCORBOT-CONTROLLER-PUB-TEST");
		options = new MqttConnectOptions();
		options.setAutomaticReconnect(true);
		options.setCleanSession(true);
		options.setConnectionTimeout(5000);

		System.out.println("Creating MQTT client for publications (unit tests)...");
		this.mqttClientPublisher = new MqttClient("tcp://" + Constants.hostname + ":" + Constants.port,
				Constants.clientIdPub);
		System.out.println("Connecting MQTT publications (unit tests) client to the broker...");
		mqttClientPublisher.connect(options);
		System.out.println("MQTT client publisher for (unit tests) is ready to send messages");
	}
	
	private void connect() throws MqttException{
		this.mqttClientPublisher = new MqttClient("tcp://" + Constants.hostname + ":" + Constants.port,
				Constants.clientIdPub);
		this.mqttClientPublisher.connect(options);
	}

	private void publish(String topic, Object payload, int qos, boolean retained)
			throws MqttPersistenceException, MqttException, InterruptedException {

		Gson gson = new Gson();
		MqttMessage mqttMessage = new MqttMessage();
		mqttMessage.setPayload(gson.toJson(payload).getBytes());
		mqttMessage.setQos(qos);
		mqttMessage.setRetained(retained);
		if(mqttClientPublisher.isConnected()){
			this.mqttClientPublisher.connect(options);
		}
		mqttClientPublisher.publish(topic, mqttMessage);
	}

	@Test
	void testCheckArmStatus() throws MqttPersistenceException, MqttException, InterruptedException {
		Owner payload = new Owner("adalberto.cajueiro@gmail.com");
		this.publish(Constants.CHECK_STATUS, payload, 0, false);
	}

	public static void main(String[] args) throws MqttPersistenceException, MqttException, InterruptedException {
		EdscorbotJavaMqttApplicationTests suite = new EdscorbotJavaMqttApplicationTests();
		Object payload = new Owner("adalberto.cajueiro@gmail.com");
		suite.publish(Constants.CHECK_STATUS, payload, 0, false);
		//suite.mqttClientPublisher.connect(suite.options);
		suite.publish(Constants.CONNECT, payload, 0, false);
		
		payload = "{\"owner\":{\"id\":\"adalberto.cajueiro@gmail.com\"},\"points\":[{\"j1Ref\":50.0,\"j2Ref\":30.0,\"j3Ref\":10.0,\"j4Ref\":40.0},{\"j1Ref\":80.0,\"j2Ref\":20.0,\"j3Ref\":20.0,\"j4Ref\":30.0},{\"j1Ref\":90.0,\"j2Ref\":10.0,\"j3Ref\":30.0,\"j4Ref\":20.0}],\"timestamp\":0}";
		suite.publish(Constants.TRAJECTORY, payload, 0, false);

		payload = new Owner("adalberto.cajueiro@gmail.com");
		suite.publish(Constants.DISCONNECT, payload, 0, false);

		System.out.println("Execution finished!");
	}

}
