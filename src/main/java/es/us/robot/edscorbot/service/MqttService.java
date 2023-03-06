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
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import es.us.robot.edscorbot.models.Client;
import es.us.robot.edscorbot.models.Point;
import es.us.robot.edscorbot.models.Trajectory;
import es.us.robot.edscorbot.models.MetaInfoObject;
import es.us.robot.edscorbot.util.ArmStatus;
import es.us.robot.edscorbot.util.Constants;
import es.us.robot.edscorbot.util.ArmMetaInfo;
import es.us.robot.edscorbot.models.CommandObject;

@Service
public class MqttService {

    enum CommandSignal {
        ARM_CHECK_STATUS(3),
        ARM_STATUS(4),
        ARM_CONNECT(5),
        ARM_CONNECTED(6),
        ARM_DISCONNECT(7),
        ARM_MOVE_TO_POINT(8),
        ARM_APPLY_TRAJECTORY(9),
        ARM_CANCEL_TRAJECTORY(10);

        private final int number;

        private CommandSignal(int number) {
            this.number = number;
        }

        public int getNumber() {
            return number;
        }
    }

    private ArmStatus status;
    private Client owner;
    private boolean executingTrajectory = true;

    private IMqttClient mqttClientSubscriber;
    private IMqttClient mqttClientPublisher;

    private Set<String> topicsSubscriber = new HashSet<String>(
            Arrays.asList(Constants.META_INFO, Constants.COMMANDS));

    public MqttService() throws MqttException, InterruptedException {
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
        String[] topicFilters = this.topicsSubscriber
                .stream()
                .map(top -> (Constants.controllerName + "/" + top))
                .toList()
                .toArray(new String[0]);

        int[] qos = new int[this.topicsSubscriber.size()];

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

        mqttClientSubscriber.subscribe(topicFilters, qos);
        System.out.println("    subscribed on topics " + Arrays.toString(topicFilters));
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

    private void handleResponse(String topic, MqttMessage message)
            throws MqttPersistenceException, MqttException, InterruptedException {

        switch (topic) {
            case Constants.controllerName + "/" + Constants.META_INFO:
                this.handleMetaInfo(message);
                break;
            case Constants.controllerName + "/" + Constants.COMMANDS:
                this.handleCommands(message);
                // this.publish(Constants.STATUS, this.status, 0, false);
                break;
            default:
                // System.out.println("Topic not recognized or out of interest of the controller!");
        }
    }

    private void handleMetaInfo(MqttMessage message)
            throws MqttPersistenceException, MqttException, InterruptedException {

        Gson gson = new Gson();
        String content = message.toString();
        MetaInfoObject input = gson.fromJson(content, MetaInfoObject.class);

        ArmMetaInfo option = input.getSignal();

        if (option.equals(ArmMetaInfo.ARM_GET_METAINFO)) { // client has sent this message
            MetaInfoObject output = new MetaInfoObject();
            output.setName(Constants.controllerName);
            output.setJoints(Constants.joints);
            output.setSignal(ArmMetaInfo.ARM_METAINFO);
            System.out.println("Meta info requested. Sending...");
            this.publish((Constants.controllerName + "/" + Constants.META_INFO), output, 0, false);
        }
        // in the other case the controller has sent this message and ignores it
    }

    private void handleCommands(MqttMessage message)
            throws MqttPersistenceException, MqttException, InterruptedException {

        Gson gson = new Gson();
        String content = message.toString();
        JsonElement input = gson.fromJson(content, JsonElement.class);
        int signal = input.getAsJsonObject().get("signal").getAsInt();
        CommandObject output = new CommandObject(Constants.ARM_STATUS);
        switch (signal) {
            case Constants.ARM_CHECK_STATUS:
                output.setSignal(Constants.ARM_STATUS);
                output.setStatus(this.status);
                System.out.println("Status requested. Sending...");
                this.publish(Constants.controllerName + "/" + Constants.COMMANDS, output, 0, false);
                break;
            case Constants.ARM_CONNECT:
                try {
                    JsonElement internalClient = input.getAsJsonObject().get("client");
                    Client client = gson.fromJson(internalClient, Client.class);
                    System.out.print("Client wants to connect to the arm: " + client.getId());
                    if (this.validatesClient(client)) {
                        output.setSignal(Constants.ARM_CONNECTED);
                        if (this.owner == null) {
                            this.owner = client;
                            this.status = ArmStatus.BUSY;
                            output.setStatus(this.status);
                            output.setClient(this.owner);
                            this.publish(Constants.controllerName + "/" + Constants.COMMANDS, output, 0, false);
                            System.out.println(" ==> Connected!");
                        } else {
                            System.out.println(" ==> Connection refused. Arm is busy");
                        }   
                    } else {
                        System.out.println("CLIENT NOT AUTHORIZED");
                    }

                } catch (JsonSyntaxException ex) {
                    System.out.println("OWNER NOT CAPTURED FROM MESSAGE");
                }

                break;
            case Constants.ARM_MOVE_TO_POINT:
                try {
                    JsonElement internalClient = input.getAsJsonObject().get("client");
                    Client client = gson.fromJson(internalClient, Client.class);
                    System.out.println("Client wants to move the arm: " + client.getId());
                    Point target = new Point();
                    if (this.validatesClient(client)) {
                        if (this.owner != null) {
                            if (this.owner.getId().equals(client.getId())) {
                                target = this.extractPointFromMessage(message);
                                if(target != null){
                                    this.moveToPointAndPublish(target);
                                } else {
                                    //message does not contains point to move the arm
                                }
                                
                            } else {
                                // athother owner tried to mode ve arm ==> ignore
                            }
                        } else {
                            // arms has no owner ==> ignore
                        }
                    } else {
                        // client not authorized
                        System.out.println("CLIENT NOT AUTHORIZED");
                    }

                } catch (JsonSyntaxException ex) {
                    System.out.println("OWNER NOT CAPTURED FROM MESSAGE");
                }

                break;
            case Constants.ARM_APPLY_TRAJECTORY:
                try {
                    JsonElement internalClient = input.getAsJsonObject().get("client");
                    Client client = gson.fromJson(internalClient, Client.class);
                    System.out.println("Client wants to apply trajectory: " + client.getId());
                    Trajectory target = new Trajectory();
                    if (this.validatesClient(client)) {
                        if (this.owner != null) {
                            if (this.owner.getId().equals(client.getId())) {
                                target = this.extractTrajectoryFromMessage(message);
                                if (target != null) {
                                    this.moveAccordingToTrajectoryAndPublish(target);
                                } else {
                                    // message does not contains trajectory move the arm
                                }

                            } else {
                                // athother owner tried to apply trajectory  ==> ignore
                            }
                        } else {
                            // arms has no owner ==> ignore
                        }
                    } else {
                        // client not authorized
                        System.out.println("CLIENT NOT AUTHORIZED");
                    }

                } catch (JsonSyntaxException ex) {
                    System.out.println("OWNER NOT CAPTURED FROM MESSAGE");
                }

                break;
            case Constants.ARM_CANCEL_TRAJECTORY:
                try {
                    JsonElement internalClient = input.getAsJsonObject().get("client");
                    Client client = gson.fromJson(internalClient, Client.class);
                    System.out.println("Client wants to cancel trajectory: " + client.getId());
                    if (this.validatesClient(client)) {
                        if (this.owner != null) {
                            if (this.owner.getId().equals(client.getId())) {
                                //the loop must be cancelled 
                                this.executingTrajectory = false;
                            } else {
                                // athother owner tried to apply trajectory ==> ignore
                            }
                        } else {
                            // arms has no owner ==> ignore
                        }
                    } else {
                        // client not authorized
                        System.out.println("CLIENT NOT AUTHORIZED");
                    }

                } catch (JsonSyntaxException ex) {
                    System.out.println("OWNER NOT CAPTURED FROM MESSAGE");
                }

                break;
            case Constants.ARM_DISCONNECT:
                try {
                    String ownerStr = input.getAsJsonObject().get("client").toString();
                    Client client = gson.fromJson(ownerStr, Client.class);
                    System.out.print("Client wants to disconnect to the arm: " + client.getId());
                    if (this.validatesClient(client)) {
                        output.setSignal(Constants.ARM_STATUS);
                        if (this.owner != null) {
                            if (this.owner.getId().equals(client.getId())) {
                                this.owner = null;
                                this.status = ArmStatus.FREE;
                                output.setStatus(this.status);
                                output.setClient(this.owner);
                                this.publish(Constants.controllerName + "/" + Constants.COMMANDS, output, 0, false);
                                System.out.println(" ==> Diconnected!");
                            } else {
                                // athother owner tried to disconnect ==> ignore
                            }
                        } else {
                            // arms has no owner ==> ignore
                        }
                    } else {
                        // client not authorized
                        System.out.println("CLIENT NOT AUTHORIZED");
                    }

                } catch (JsonSyntaxException ex) {
                    System.out.println("OWNER NOT CAPTURED FROM MESSAGE");
                }

                break;
            default:
                // System.out.println("signal//command out of interest of the controller " + signal);
                break;
        }
    }

    private boolean validatesClient(Client client) {
        // it should use some mecnanism to check if the client is authorized to use the
        // arm
        return true;
    }

    private Point extractPointFromMessage(MqttMessage message){
        Point result = null;
        Gson gson = new Gson();
        String contentStr = message.toString();
        JsonElement content = gson.fromJson(contentStr, JsonElement.class).getAsJsonObject().get("content");
        
        try{
            result = gson.fromJson(content, Point.class);
        } catch(JsonSyntaxException ex){
            //point is not present in the json structure
        }

        return result;
    }

    private Trajectory extractTrajectoryFromMessage(MqttMessage message) {
        Trajectory result = null;
        Gson gson = new Gson();
        String contentStr = message.toString();
        JsonElement content = gson.fromJson(contentStr, JsonElement.class).getAsJsonObject().get("content");

        try {
            result = gson.fromJson(content, Trajectory.class);
        } catch (JsonSyntaxException ex) {
            // point is not present in the json structure
        }

        return result;
    }

    private void moveToPointAndPublish(Point p) throws MqttPersistenceException, MqttException{
        System.out.print("Moving arm to point " + p);
        try{
            Thread.sleep(1000);
            System.out.println(" ==> arm moved. Notifying clients about the last point");
            this.publish(Constants.controllerName + "/" + Constants.MOVED, p, 0, false);
        } catch(InterruptedException ex){

        }
        
    }



    private void moveAccordingToTrajectoryAndPublish(Trajectory trajectory) throws MqttPersistenceException, MqttException{
        Iterator<Point> it = trajectory.getPoints().iterator();
        this.executingTrajectory = true;

        Thread t = new Thread(new Runnable() {
            
            public void publish(String topic, Object payload, int qos, boolean retained)
                    throws MqttPersistenceException, MqttException, InterruptedException {

                Gson gson = new Gson();
                MqttMessage mqttMessage = new MqttMessage();
                mqttMessage.setPayload(gson.toJson(payload).getBytes());
                mqttMessage.setQos(qos);
                mqttMessage.setRetained(retained);

                mqttClientPublisher.publish(topic, mqttMessage);
            }
            private void moveToPointAndPublish(Point p) throws MqttPersistenceException, MqttException {
                System.out.print("Moving arm to point " + p);
                try {
                    Thread.sleep(1000);
                    System.out.println(" ==> arm moved. Notifying clients about the last point");
                    this.publish(Constants.controllerName + "/" + Constants.MOVED, p, 0, false);
                } catch (InterruptedException ex) {

                }

            }

            @Override
            public void run(){
                while (it.hasNext() && executingTrajectory) {
                    Point next = it.next();
                    try{
                        this.moveToPointAndPublish(next);
                    } catch(MqttPersistenceException ex){

                    } catch(MqttException ex){

                    }
                }
                executingTrajectory = false;
            }
        });
        t.start(); 
    }
}
