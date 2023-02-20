package es.us.robot.edscorbot.util;

import org.springframework.stereotype.Component;

@Component
public class Constants {

    public static final boolean automaticReconnect = true;

    public static final boolean cleanSession = true;

    public static final int connectionTimeout = 5000;

    public static final String clientIdSub = "SCORBOT-CONTROLLER_SUB";

    public static final String clientIdPub = "SCORBOT-CONTROLLER_PUB";

    public static final String hostname = "localhost";

    public static final int port = 1883;

    //subscriber topics
    public static final String CHECK_STATUS = "checkArmStatus";
    public static final String CONNECT = "armConnect";
    public static final String DISCONNECT = "armDisconnect";
    public static final String TRAJECTORY = "trajectory";
    public static final String CANCEL_TRAJECTORY = "cancelTrajectory";

    // publisher topics
    public static final String STATUS = "armStatus";
    public static final String CONNECTED = "armConnected";
    public static final String POINT = "point";
    
    /**
     * 
     * 
    public static void main(String[] args) {

        Trajectory traj = new Trajectory();
        traj.setOwner(new Owner("adalberto@gmail.com"));
        traj.getPoints().add(new Point(50,30,10,40));
        traj.getPoints().add(new Point(80,20,20,30));
        traj.getPoints().add(new Point(90,10,30,20));
        Gson gson = new Gson();

        String trajectory = gson.toJson(traj);
        String trajStr = "{\"owner\":{\"id\":\"adalberto@gmail.com\"},\"points\":[{\"j1Ref\":50.0,\"j2Ref\":30.0,\"j3Ref\":10.0,\"j4Ref\":40.0},{\"j1Ref\":80.0,\"j2Ref\":20.0,\"j3Ref\":20.0,\"j4Ref\":30.0},{\"j1Ref\":90.0,\"j2Ref\":10.0,\"j3Ref\":30.0,\"j4Ref\":20.0}],\"timestamp\":0}";
        traj = gson.fromJson(trajStr, Trajectory.class);
        System.out.println(trajectory);
    }
    */
}
