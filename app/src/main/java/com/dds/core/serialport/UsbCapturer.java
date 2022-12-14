package com.dds.core.serialport;

/**
 * @author WuSG
 * @date : 2022/9/22 00:19
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

    public UsbCapturer(Context context, SurfaceViewRenderer svVideoRender) {
        this.svVideoRender = svVideoRender;
        monitor = new USBMonitor(context, this);
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
        LogKt.Loges(TAG, "onAttach:");
        monitor.requestPermission(device);
    }

    @Override
    public void onDettach(UsbDevice device) {
        LogKt.Loges(TAG, "onDettach:");
        if (mCamera != null) {
            mCamera.close();
        }
    }

    @Override
    public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
        LogKt.Loges(TAG, "onConnect:");
        UsbCapturer.this.ctrlBlock = ctrlBlock;
        startPreview();
    }

    @Override
    public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
        LogKt.Loges(TAG, "onDisconnect:");
        if (mCamera != null) {
            mCamera.close();
        }
    }

    @Override
    public void onCancel(UsbDevice device) {
        LogKt.Loges(TAG, "onCancel:");
    }

    private ReentrantLock imageArrayLock = new ReentrantLock();

    @Override
    public void onFrame(ByteBuffer frame) {
        if (frame != null) {
            imageArrayLock.lock();
            byte[] imageArray = new byte[frame.remaining()];
            frame.get(imageArray);
            //?????? long imageTime = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
            // VideoFrame.Buffer mNV21Buffer = new NV21Buffer(imageArray ,
            // UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT , null);
            // VideoFrame mVideoFrame = new VideoFrame(mNV21Buffer, 0, imageTime);
            // capturerObserver.onFrameCaptured(mVideoFrame);
            // mVideoFrame.release();
            // imageArrayLock.unlock(); } }
            // public USBMonitor getMonitor() { return this.monitor; } private void startPreview() { synchronized (mSync) { if (mCamera != null) { mCamera.destroy(); } } UVCCamera camera = new UVCCamera(); camera.setAutoFocus(true); camera.setAutoWhiteBlance(true); try { camera.open(ctrlBlock); // camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.PIXEL_FORMAT_RAW); camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, FpsType.FPS_15, mFps, UVCCamera.PIXEL_FORMAT_RAW, 1.0f); } catch (Exception e) { try { // camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE); camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, FpsType.FPS_15, mFps, UVCCamera.DEFAULT_PREVIEW_MODE, 1.0f); } catch (Exception e1) { camera.destroy(); camera = null; } } if (camera != null) { if (svVideoRender != null) { camera.setPreviewDisplay(svVideoRender.getHolder().getSurface()); } camera.setFrameCallback(UsbCapturer.this, UVCCamera.PIXEL_FORMAT_YUV420SP); camera.startPreview(); } synchronized (mSync) { mCamera = camera; } } public void setSvVideoRender(YQRTCSurfaceViewRenderer svVideoRender) { this.svVideoRender = svVideoRender; } }

        }
    }
}
