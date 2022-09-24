package com.dds.core.serialport;

import android.util.Log;

import com.dds.App;
import com.wsg.mclibrary.common.ByteUtils;
import com.wsg.mclibrary.common.serial.AbstractUsbSerial;
import com.wsg.mclibrary.common.serial.ISerialListener;
import com.wsg.mclibrary.common.serial.SerialConfig;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author WuSG
 */
public class UsbSerialThread extends AbstractUsbSerial implements ISerialListener {
    private final LinkedBlockingQueue<byte[]> queue;

    public UsbSerialThread() {
        queue = new LinkedBlockingQueue<>();
    }


    @Override
    protected void onSubmitRunnable() {

    }

    @Override
    protected SerialConfig onSerialConfig() {
        return new SerialConfig.Builder()
                .setSerialListener(this)
                .setBaudRate(115200)
                .setVendorId(6790)
                .setProductId(29987)
                .setContext(App.getInstance())
                .builder();

    }

    @Override
    public void onSerialInitComplete(int openCode) {
        Log.e("onSerialInitComplete: ", "加载完成");
        loop();
    }

    private void loop() {
        while (!isTermination) {
            byte[] mData ;
            try {
                mData = queue.take();
                if (mData != null) {
                    onSendSerialData(mData, 300);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onSerialError(int code, String errorMsg) {
        Log.e("onSerialError: ", errorMsg);
    }


    @Override
    protected boolean onTerminationReceive() {
        return false;
    }

    @Override
    protected void onReceiverSerialData(byte[] data) {
        Log.e("onReceiverSerialData: ", ByteUtils.byte2hex(data));
    }

    @Override
    protected boolean onTerminationSend() {
        return isTermination;
    }

    private boolean isTermination;

    public void setTermination(boolean termination) {
        isTermination = termination;
    }

    public void offer(byte[] bytes) {
        queue.offer(bytes);
    }
}
