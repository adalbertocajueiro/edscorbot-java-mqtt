package es.us.robot.edscorbot.service;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
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
import es.us.robot.edscorbot.models.MovedObject;
import es.us.robot.edscorbot.util.Constants;
import es.us.robot.edscorbot.models.CommandObject;

@Service
public class MqttService {
    private boolean errorState;
    private Client owner;
    private boolean executingTrajectory = true;

    private IMqttClient mqttClientSubscriber;
    private IMqttClient mqttClientPublisher;

    public MqttService() throws MqttException, InterruptedException {
        this.errorState = false;
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
        System.out.println("Publishing metainfos of " + Constants.CONTROLLER_NAME);
        this.publishMetaInfo();
    }

    private void subscribeAllTopics() throws MqttException, InterruptedException {
        List<String> topics = new ArrayList<String>();

        topics.add(Constants.META_INFO);
        topics.add(Constants.CONTROLLER_NAME + "/" + Constants.COMMANDS);

        int[] qos = new int[topics.size()];

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
        String[] topicsArray = topics.toArray(new String[0]);
        mqttClientSubscriber.subscribe(topicsArray, qos);
        System.out.println("    subscribed on topics " + Arrays.toString(topicsArray));
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
            case Constants.META_INFO:
                this.handleMetaInfo(message);
                break;
            case Constants.CONTROLLER_NAME + "/" + Constants.COMMANDS:
                this.handleCommands(message);
                // this.publish(Constants.STATUS, this.status, 0, false);
                break;
            default:
                // System.out.println("Topic not recognized or out of interest of the
                // controller!");
        }
    }

    private void handleMetaInfo(MqttMessage message)
            throws MqttPersistenceException, MqttException, InterruptedException {

        Gson gson = new Gson();
        String content = message.toString();
        MetaInfoObject input = gson.fromJson(content, MetaInfoObject.class);

        int signal = input.getSignal();

        if (signal == Constants.ARM_GET_METAINFO) { // client requested meta info
            this.publishMetaInfo();
        }
    }

    private void publishMetaInfo() throws MqttPersistenceException, MqttException, InterruptedException{
        Gson gson = new Gson();
        MetaInfoObject output = new MetaInfoObject();
        output.setName(Constants.CONTROLLER_NAME);
        output.setJoints(Constants.joints);
        output.setSignal(Constants.ARM_METAINFO);
        System.out
                .println("Meta info requested. Sending " + gson.toJson(output) + " through " + Constants.META_INFO);
        this.publish(Constants.META_INFO, output, 0, false);
    }

    private void handleCommands(MqttMessage message)
            throws MqttPersistenceException, MqttException, InterruptedException {

        Gson gson = new Gson();
        String content = message.toString();
        JsonElement input = gson.fromJson(content, JsonElement.class);
        int signal = input.getAsJsonObject().get("signal").getAsInt();
        CommandObject output = new CommandObject(Constants.ARM_STATUS);
        switch (signal) {
            case Constants.ARM_CHECK_STATUS: // client requested arm's status
                output.setSignal(Constants.ARM_STATUS);
                output.setErrorState(this.errorState);
                output.setClient(this.owner);
                System.out.println(
                        "Status requested. Sending " + gson.toJson(output) + " through " + Constants.CONTROLLER_NAME
                                + "/" + Constants.COMMANDS);
                this.publish(Constants.CONTROLLER_NAME + "/" + Constants.COMMANDS, output, 0, false);
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
                            output.setErrorState(this.errorState);
                            output.setClient(this.owner);
                            this.publish(Constants.CONTROLLER_NAME + "/" + Constants.COMMANDS, output, 0, false);
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
                                if (target != null) {
                                    this.moveToPointAndPublish(target);
                                } else {
                                    // message does not contains point to move the arm
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
            case Constants.ARM_CANCEL_TRAJECTORY:
                try {
                    JsonElement internalClient = input.getAsJsonObject().get("client");
                    Client client = gson.fromJson(internalClient, Client.class);
                    System.out.println("Client wants to cancel trajectory: " + client.getId());
                    if (this.validatesClient(client)) {
                        if (this.owner != null) {
                            if (this.owner.getId().equals(client.getId())) {
                                // the loop must be cancelled
                                this.executingTrajectory = false;
                                
                                output.setSignal(Constants.ARM_CANCELED_TRAJECTORY);
                                output.setErrorState(this.errorState);
                                output.setClient(this.owner);
                                this.publish(Constants.CONTROLLER_NAME + "/" + Constants.COMMANDS, output, 0, false);
                                System.out.println(" Trajectory cancelled!");
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
                        output.setSignal(Constants.ARM_DISCONNECTED);
                        if (this.owner != null) {
                            if (this.owner.getId().equals(client.getId())) {
                                this.owner = null;
                                output.setErrorState(this.errorState);
                                output.setClient(client);
                                this.publish(Constants.CONTROLLER_NAME + "/" + Constants.COMMANDS, output, 0, false);
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
                // System.out.println("signal//command out of interest of the controller " +
                // signal);
                break;
        }
    }

    private boolean validatesClient(Client client) {
        // it should use some mecnanism to check if the client is authorized to use the
        // arm
        return true;
    }

    private Point extractPointFromMessage(MqttMessage message) {
        Point result = null;
        Gson gson = new Gson();
        String contentStr = message.toString();
        JsonElement content = gson.fromJson(contentStr, JsonElement.class).getAsJsonObject().get("content");

        try {
            result = gson.fromJson(content, Point.class);
        } catch (JsonSyntaxException ex) {
            // point is not present in the json structure
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

    private void moveToPointAndPublish(Point point) throws MqttPersistenceException, MqttException {
        System.out.print("Moving arm to point " + point);
        try {
            Thread.sleep(1000);
            System.out.println(" ==> arm moved. Notifying clients about the last point");
            MovedObject output = new MovedObject();
            output.setClient(this.owner);
            output.setErrorState(this.errorState);
            output.setContent(point);
            this.publish(Constants.CONTROLLER_NAME + "/" + Constants.MOVED, output, 0, false);
        } catch (InterruptedException ex) {

        }

    }

    private void moveAccordingToTrajectoryAndPublish(Trajectory trajectory)
            throws MqttPersistenceException, MqttException {
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

            private void moveToPointAndPublish(Point point) throws MqttPersistenceException, MqttException {
                System.out.print("Moving arm to point " + point);
                try {
                    Thread.sleep(1000);
                    System.out.println(" ==> arm moved. Notifying clients about the last point");
                    MovedObject output = new MovedObject();
                    output.setClient(owner);
                    output.setErrorState(errorState);
                    output.setContent(point);
                    this.publish(Constants.CONTROLLER_NAME + "/" + Constants.MOVED, output, 0, false);
                } catch (InterruptedException ex) {

                }

            }

            @Override
            public void run() {
                while (it.hasNext() && executingTrajectory) {
                    Point next = it.next();
                    try {
                        this.moveToPointAndPublish(next);
                    } catch (MqttPersistenceException ex) {

                    } catch (MqttException ex) {

                    }
                }
                executingTrajectory = false;
            }
        });
        t.start();
    }
}
