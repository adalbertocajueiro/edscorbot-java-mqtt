package es.us.robot.edscorbot.service;

import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import es.us.robot.edscorbot.models.Owner;
import es.us.robot.edscorbot.models.Point;
import es.us.robot.edscorbot.models.Trajectory;
import es.us.robot.edscorbot.util.ArmStatus;
import es.us.robot.edscorbot.util.Constants;

@Service
public class MqttService {

    private ArmStatus status;
    private Owner owner;

    private IMqttClient mqttClientSubscriber;
    private IMqttClient mqttClientPublisher;

    private Set<String> topicsSubscriber = new HashSet<String>(
            Arrays.asList(  Constants.CHECK_STATUS,Constants.CONNECT,
                            Constants.DISCONNECT,Constants.TRAJECTORY, 
                            Constants.CANCEL_TRAJECTORY));

    public MqttService() throws MqttException, InterruptedException{
        this.status = ArmStatus.FREE;
        System.out.println("Creating MQTT client for subscriptions...");
        this.mqttClientSubscriber = new MqttClient("tcp://" + Constants.hostname + ":" + Constants.port,
                Constants.clientIdSub);
        System.out.println("Connecting MQTT subscriptions client to the broker...");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(5000);

        
        this.mqttClientSubscriber.connect(options);
        System.out.println("Subscribing SCORBOT CONTROLLER in all topics...");
        this.subscribeAllTopics();

        System.out.println("Creating MQTT client for publications...");
        this.mqttClientPublisher = new MqttClient("tcp://" + Constants.hostname + ":" + Constants.port,
                Constants.clientIdPub);
        System.out.println("Connecting MQTT publications client to the broker...");
        mqttClientPublisher.connect(options);
        System.out.println("SCORBOT CONTROLLER is ready to send/receive messages");
    }

    private void subscribeAllTopics() throws MqttException, InterruptedException {
        String[] topicFilters = this.topicsSubscriber.toArray(new String[0]);
        int[] qos = {0,0,0,0,0}; 
        mqttClientSubscriber.setCallback(new MqttCallback() {

            @Override
            public void connectionLost(Throwable cause) {
                // TODO Auto-generated method stub
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                handleResponse(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // TODO Auto-generated method stub
            }
            
        });
        
        mqttClientSubscriber.subscribe(topicFilters,qos);
        System.out.println("    subscribed on topics " + this.topicsSubscriber);
    }

    public void publish(String topic, Object payload, int qos, boolean retained)
            throws MqttPersistenceException, MqttException, InterruptedException {
        
                Gson gson = new Gson();
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(gson.toJson(payload).getBytes());
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);
        
        mqttClientPublisher.publish(topic, mqttMessage);
    }

    private void handleResponse(String topic, MqttMessage message) throws MqttPersistenceException, MqttException, InterruptedException{
        Gson gson = new Gson();
        String content = message.toString();
        Owner owner = null;
        //this handler is invoked only when the controller receives a message (as consumer)
        switch(topic){
            case Constants.CHECK_STATUS:
                System.out.println("Client wants to check status of the arm and it is: " + this.status);
                this.publish(Constants.STATUS, this.status, 0, false);
                break;
            case Constants.CONNECT:
                try{
                    owner =  gson.fromJson(content, Owner.class);
                    System.out.print("Client wants to connect to the arm: " + owner.getId());
                    if(this.owner == null){
                        this.owner = owner;
                        this.status = ArmStatus.BUSY;
                        this.publish(Constants.CONNECTED, owner, 0, false);
                        System.out.println(" ==> CONNECTED");
                    } else{
                        this.publish(Constants.STATUS, this.status, 0, false);
                    }
                }catch(JsonSyntaxException ex){
                    System.out.println("OWNER NOT CAPTURED FROM MESSAGE");
                }
                break;
            case Constants.DISCONNECT:
                try {
                    owner =  gson.fromJson(content, Owner.class);
                    System.out.println("Client wants to disconnect from the arm: " + owner.getId());
                    if (this.owner != null) {
                        if(this.owner.getId().equals(owner.getId())){
                            //this.publish(Constants.DISCONNECTED, owner, 0, false);
                            this.owner = null;
                            this.status = ArmStatus.FREE;
                            this.publish(Constants.STATUS, this.status, 0, false);
                        }                        
                    } 
                    
                } catch (JsonSyntaxException ex) {
                    System.out.println("OWNER NOT CAPTURED FROM MESSAGE");
                }
                
                break;
            case Constants.TRAJECTORY:
                Trajectory trajectory = null;
                try {
                    trajectory = gson.fromJson(content, Trajectory.class);
                    System.out.println("Client wants to execute trajectory: " + trajectory.getOwner().getId());
                    if (this.owner != null) {
                        if(this.owner.getId().equals(trajectory.getOwner().getId())){
                            Iterator<Point> it = trajectory.getPoints().iterator();
                            while(it.hasNext()){
                                Point next = it.next();
                                System.out.println("  -> Arm moved to point " + next);
                                this.publish(Constants.POINT, next, 0, false);
                            }
                        }    
                    } else {
                        this.publish(Constants.STATUS, this.status, 0, false);
                    }

                } catch (JsonSyntaxException ex) {
                    System.out.println("TRAJECTORY NOT CAPTURED FROM MESSAGE");
                }
                break;
            case Constants.CANCEL_TRAJECTORY:
                try {
                    owner = gson.fromJson(content, Owner.class);
                    System.out.println("Client wants requested to cancel the trajectory: " + owner.getId());
                    if (this.owner != null) {
                        if (this.owner.getId().equals(owner.getId())) {
                            //stops the arm and moves it to home
                            System.out.println("  -> Arm moved to home ");
                            Point home = new Point(0,0,0,0);
                            this.publish(Constants.POINT, home, 0, false);
                        }
                    } else {
                        //the requesting user is not the owner
                    }

                } catch (JsonSyntaxException ex) {
                    System.out.println("OWNER NOT CAPTURED FROM MESSAGE");
                }
                break;
            default:
                System.out.println("Topic not recognized!");
        }
    }
}
