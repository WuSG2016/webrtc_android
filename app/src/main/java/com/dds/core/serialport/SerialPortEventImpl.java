package com.dds.core.serialport;


import android.util.Log;

import com.dds.core.util.ByteUtils;

public class SerialPortEventImpl implements ISerialPortEvent {
    private final UsbSerialThread mUsbSerialThread;
    private byte[] mData = new byte[]{0x55, 0x00, 0x00, 0x00, 0x00, 0x00};

    public SerialPortEventImpl(UsbSerialThread mUsbSerialThread) {
        this.mUsbSerialThread = mUsbSerialThread;
    }

    @Override
    public void doSerialPortEvent(String fromId, int cmd, int isUp, int level,int angle) {
        boolean isLeft = false;
        switch (cmd) {
            case ISerialPortCmd.DOWN_CMD:
                isLeft = false;
                Log.e("SerialPortEventImpl", "doSerialPortEvent: DOWN_CMD");
                break;
            case ISerialPortCmd.UP_CMD:
                isLeft = false;
                Log.e("SerialPortEventImpl", "doSerialPortEvent: UP_CMD");
                break;
            case ISerialPortCmd.LEFT_CMD:
                Log.e("SerialPortEventImpl", "doSerialPortEvent: LEFT_CMD");
                isLeft = true;
                break;
            case ISerialPortCmd.RIGHT_CMD:
                isLeft = false;
                Log.e("SerialPortEventImpl", "doSerialPortEvent: RIGHT_CMD");
                break;
            case ISerialPortCmd.DOWN_LEFT_CMD:
                isLeft = true;
                Log.e("SerialPortEventImpl", "doSerialPortEvent: DOWN_LEFT_CMD");
                break;
            case ISerialPortCmd.DOWN_RIGHT_CMD:
                isLeft = false;
                Log.e("SerialPortEventImpl", "doSerialPortEvent: DOWN_RIGHT_CMD");
                break;
            case ISerialPortCmd.UP_LEFT_CMD:
                isLeft = true;
                Log.e("SerialPortEventImpl", "doSerialPortEvent: UP_LEFT_CMD");
                break;
            case ISerialPortCmd.UP_RIGHT_CMD:
                isLeft = false;
                Log.e("SerialPortEventImpl", "doSerialPortEvent: UP_RIGHT_CMD");
                break;
            case ISerialPortCmd.CENTER_CMD:
                isLeft = false;
                angle = 0;
                isUp = ISerialPortCmd.UP_SPEED_FALSE;
                level = 0;
                break;
            default:
                break;
        }
        handleAddCmd(getByteByCmd(isLeft, angle, isUp == ISerialPortCmd.UP_SPEED_TURE, level));

    }


    private byte[] getByteByCmd(boolean isLeft, int angle, boolean isUp, int level) {
        byte sum = 0;
        mData[1] = isLeft ? (byte) (angle) : 0x00;
        sum += mData[1];
        mData[2] = isLeft ? 0x00 : (byte) angle;
        sum += mData[2];
        mData[3] = isUp ? (byte) (level) : 0x00;
        sum += mData[3];
        mData[4] = isUp ? 0x00 : (byte) level;
        sum += mData[4];
        mData[5] = sum;
        Log.e("getByteByCmd: ", ByteUtils.byte2hex(mData));
        return mData;

    }

    private void handleAddCmd(byte[] bytes) {
        if (mUsbSerialThread != null && bytes != null) {
            mUsbSerialThread.offer(bytes);
        }
    }
}
