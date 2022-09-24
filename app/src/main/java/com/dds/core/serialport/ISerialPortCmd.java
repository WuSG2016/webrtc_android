package com.dds.core.serialport;

public interface ISerialPortCmd {
    int LEFT_CMD = 0;

    int RIGHT_CMD = 1;

    int UP_CMD = 2;

    int DOWN_CMD = 3;

    int UP_LEFT_CMD = 4;

    int UP_RIGHT_CMD = 5;

    int DOWN_LEFT_CMD = 6;

    int DOWN_RIGHT_CMD = 7;

    int CENTER_CMD = 8;


    String CMD_NAME = "__cmd";
    String CMD_DATA = "cmd_data";
    String CMD_TOP_DATA = "cmd_top";
    String CMD_LEVEL_DATA = "cmd_level";
    String CMD_ANGLE_DATA = "cmd_angle";

    int UP_SPEED_TURE = 1;
    int UP_SPEED_FALSE = 0;

    String LEFT_NAME = "__left";
    String RIGHT_NAME = "__right";
    String UP_NAME = "__up";
    String DOWN_NAME = "__down";

}
