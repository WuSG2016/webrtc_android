package com.dds.skywebrtc.engine.webrtc.uvc;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.SystemClock;
import android.util.Log;

import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import org.webrtc.CapturerObserver;
import org.webrtc.NV21Buffer;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author WuSG
 * @date : 2022/9/29 21:37
 */
public class UsbCapturer implements VideoCapturer, USBMonitor.OnDeviceConnectListener, IFrameCallback {
    private static final String TAG = "UsbCapturer";
    private USBMonitor monitor;
    private SurfaceViewRenderer svVideoRender;
    private CapturerObserver capturerObserver;
    private int mFps;
    private UVCCamera mCamera;
    private final Object mSync = new Object();
    private boolean isRegister;
    private USBMonitor.UsbControlBlock ctrlBlock;
    private final WeakReference<Context> weakReference;

    public UsbCapturer(Context context, SurfaceViewRenderer svVideoRender) {
        this.svVideoRender = svVideoRender;
        weakReference = new WeakReference<>(context);
        monitor = new USBMonitor(context, this);
    }

    public void setVideoRender(SurfaceViewRenderer svVideoRender) {
        this.svVideoRender = svVideoRender;
    }

    @Override
    public void initialize(SurfaceTextureHelper surfaceTextureHelper, Context context, CapturerObserver capturerObserver) {
        this.capturerObserver = capturerObserver;
    }

    @Override
    public void startCapture(int width, int height, int fps) {
        this.mFps = fps;
        if (!isRegister) {
            isRegister = true;
            monitor.register();
        } else if (ctrlBlock != null) {
            startPreview();
        }
    }

    @Override
    public void stopCapture() {
        if (mCamera != null) {
            mCamera.destroy();
            mCamera = null;
        }
    }

    @Override
    public void changeCaptureFormat(int i, int i1, int i2) {
    }

    @Override
    public void dispose() {
        monitor.unregister();
        monitor.destroy();
        monitor = null;
    }

    @Override
    public boolean isScreencast() {
        return false;
    }

    @Override
    public void onAttach(UsbDevice device) {

        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(weakReference.get(), com.serenegiant.uvccamera.R.xml.device_filter2);
        List<UsbDevice> deviceList = monitor.getDeviceList(filter.get(0));
        if (deviceList.isEmpty()) {
            return;
        }
        for (UsbDevice device1 : deviceList) {
            monitor.requestPermission(device1);
        }

    }

    @Override
    public void onDettach(UsbDevice device) {
        if (mCamera != null) {
            mCamera.close();
        }
    }

    private boolean isPreView = false;

    @Override
    public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
        UsbCapturer.this.ctrlBlock = ctrlBlock;
        Log.e(TAG, "onConnect: " + device.toString());
        //防止重复加载
        if (isPreView) {
            return;
        }
        startPreview();
    }

    @Override
    public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
        if (mCamera != null) {
            mCamera.close();
        }
    }

    @Override
    public void onCancel(UsbDevice device) {
    }

    private final ReentrantLock imageArrayLock = new ReentrantLock();

    @Override
    public void onFrame(ByteBuffer frame) {
        if (frame != null) {
            imageArrayLock.lock();
            byte[] imageArray = new byte[frame.remaining()];
            frame.get(imageArray); //关键
            long imageTime = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
            VideoFrame.Buffer mNV21Buffer = new NV21Buffer(imageArray, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, null);
            VideoFrame mVideoFrame = new VideoFrame(mNV21Buffer, 0, imageTime);
            capturerObserver.onFrameCaptured(mVideoFrame);
            mVideoFrame.release();
            imageArrayLock.unlock();
        }
    }

    public USBMonitor getMonitor() {
        return this.monitor;
    }

    private void startPreview() {
        Log.e(TAG, "startPreview: ");
        synchronized (mSync) {
            if (mCamera != null) {
                mCamera.destroy();
            }
            isPreView = true;
        }
        UVCCamera camera = new UVCCamera();
        camera.setAutoFocus(true);
        camera.setAutoWhiteBlance(true);
        try {
            camera.open(ctrlBlock);
            camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.PIXEL_FORMAT_RAW);
            camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, FpsType.FPS_15, mFps, UVCCamera.PIXEL_FORMAT_RAW, 1.0f);
        } catch (Exception e) {
            try {
                camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
                camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, FpsType.FPS_15, mFps, UVCCamera.DEFAULT_PREVIEW_MODE, 1.0f);
            } catch (Exception e1) {
                camera.destroy();
                camera = null;
            }
        }
        if (camera != null) {
            if (svVideoRender != null) {
                camera.setPreviewDisplay(svVideoRender.getHolder().getSurface());
            } else {
                Log.e(TAG, "startPreview: " + "svVideoRender == null");
            }
            camera.setFrameCallback(UsbCapturer.this, UVCCamera.PIXEL_FORMAT_YUV420SP);
            camera.startPreview();
        }
        synchronized (mSync) {
            mCamera = camera;
        }
    }


}

