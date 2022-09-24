package com.dds.core.serialport;


public interface ISerialPortEvent extends ISerialPortCmd{
    void doSerialPortEvent(String fromId,int cmd,int isUp,int level,int angle);
}
