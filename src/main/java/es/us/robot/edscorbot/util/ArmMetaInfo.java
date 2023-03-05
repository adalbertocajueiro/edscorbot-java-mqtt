package es.us.robot.edscorbot.util;

public enum ArmMetaInfo{
    GET_METAINFO (1),
    METAINFO (2);

    private final int number;

    private ArmMetaInfo(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
