package es.us.robot.edscorbot.util;

import java.util.List;
import java.util.Arrays;
import com.google.gson.Gson;

import es.us.robot.edscorbot.models.MetaInfoObject;
import es.us.robot.edscorbot.models.Point;
import es.us.robot.edscorbot.models.Trajectory;
import es.us.robot.edscorbot.models.JointInfo;

public class Constants {

    public static final String controllerName = "EDScorbot";

    public static final List<JointInfo> joints = Arrays.asList(
            new JointInfo(-450, 500),
            new JointInfo(-950, 800),
            new JointInfo(-350, 350),
            new JointInfo(-1500, 1600),
            new JointInfo(0, 0),
            new JointInfo(0, 0));

    // parameters to interact with broker
    public static final boolean automaticReconnect = true;
    public static final boolean cleanSession = true;
    public static final int connectionTimeout = 5000;
    public static final String clientIdSub = "SCORBOT-CONTROLLER_SUB";
    public static final String clientIdPub = "SCORBOT-CONTROLLER_PUB";
    public static final String hostname = "localhost";
    public static final int port = 1883;

    //arm status constants
    public static final int FREE = 0;
    public static final int BUSY = -1;
    public static final int ERROR = -2;

    // metainfo constants
    public static final int ARM_GET_METAINFO = 1;
    public static final int ARM_METAINFO = 2;

    // commands constants
    public static final int ARM_CHECK_STATUS = 3;
    public static final int ARM_STATUS = 4;
    public static final int ARM_CONNECT = 5;
    public static final int ARM_CONNECTED = 6;
    public static final int ARM_DISCONNECT = 7;
    public static final int ARM_MOVE_TO_POINT = 8;
    public static final int ARM_APPLY_TRAJECTORY = 9;
    public static final int ARM_CANCEL_TRAJECTORY = 10;

    // subtopics for each robot
    public static final String META_INFO = "metainfo";
    public static final String COMMANDS = "commands";
    public static final String MOVED = "moved";

    public static void main(String[] args) {

        MetaInfoObject obj = new MetaInfoObject();
        obj.setSignal(Constants.ARM_GET_METAINFO);
        Gson gson = new Gson();
        String objStr = gson.toJson(obj);
        System.out.println(objStr);

        Trajectory t = new Trajectory();
        double coordinate = 10.0;
        for (int i = 0; i < 10 ; i++){
            t.getPoints().add(
                new Point(
                    Arrays.asList(coordinate, coordinate, coordinate, coordinate, coordinate, coordinate)));
            coordinate += 10;
        }
        objStr = gson.toJson(t);
        System.out.println(objStr);
        System.out.println("End");
        /*
         * Trajectory traj = new Trajectory();
         * traj.setOwner(new Owner("adalberto@gmail.com"));
         * traj.getPoints().add(new Point(50, 30, 10, 40));
         * traj.getPoints().add(new Point(80, 20, 20, 30));
         * traj.getPoints().add(new Point(90, 10, 30, 20));
         * Gson gson = new Gson();
         * 
         * String trajectory = gson.toJson(traj);
         * String trajStr =
         * "{\"owner\":{\"id\":\"adalberto@gmail.com\"},\"points\":[{\"j1Ref\":50.0,\"j2Ref\":30.0,\"j3Ref\":10.0,\"j4Ref\":40.0},{\"j1Ref\":80.0,\"j2Ref\":20.0,\"j3Ref\":20.0,\"j4Ref\":30.0},{\"j1Ref\":90.0,\"j2Ref\":10.0,\"j3Ref\":30.0,\"j4Ref\":20.0}],\"timestamp\":0}";
         * traj = gson.fromJson(trajStr, Trajectory.class);
         * System.out.println(trajectory);
         */
    }
}
