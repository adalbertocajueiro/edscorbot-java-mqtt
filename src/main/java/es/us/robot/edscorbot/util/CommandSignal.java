package es.us.robot.edscorbot.util;

public enum CommandSignal{
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
