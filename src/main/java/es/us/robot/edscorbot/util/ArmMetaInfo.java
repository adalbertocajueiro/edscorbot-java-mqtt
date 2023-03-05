package es.us.robot.edscorbot.util;

public enum ArmMetaInfo {
    ARM_GET_METAINFO(1),
    ARM_METAINFO(2);

    private final int number;

    private ArmMetaInfo(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
