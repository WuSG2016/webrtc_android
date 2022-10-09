package com.dds;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.dds.core.MainActivity;
import com.dds.core.base.BaseActivity;
import com.dds.core.consts.Urls;
import com.dds.core.serialport.UsbSerialThread;
import com.dds.core.socket.IUserState;
import com.dds.core.socket.SocketManager;
import com.dds.webrtc.R;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;


public class LauncherActivity extends BaseActivity implements IUserState, CameraDialog.CameraDialogParent {
    private Toolbar toolbar;
    private EditText etUser;
    private Button button8;
    private UsbSerialThread mUsbSerialPortThread;
    private static final String TAG = LauncherActivity.class.getSimpleName();
    /**
     * for camera preview display
     */
    private CameraViewInterface mUVCCameraView;

    private static final boolean USE_SURFACE_ENCODER = false;

    /**
     * preview resolution(width)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_WIDTH = 1280;
    /**
     * preview resolution(height)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_HEIGHT = 720;
    /**
     * preview mode
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     * 0:YUYV, other:MJPEG
     */
    private static final int PREVIEW_MODE = 1;

    protected static final int SETTINGS_HIDE_DELAY_MS = 2500;

    /**
     * for accessing USB
     */
    private USBMonitor mUSBMonitor;
    /**
     * Handler to execute camera related methods sequentially on private thread
     */
    private UVCCameraHandler mCameraHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        initView();

        if (SocketManager.getInstance().getUserState() == 1) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        mUsbSerialPortThread = new UsbSerialThread();
        mUsbSerialPortThread.start();
    }


    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar = findViewById(R.id.toolbar);
        etUser = findViewById(R.id.et_user);
        button8 = findViewById(R.id.button8);

        etUser.setText(App.getInstance().getUsername());
        findViewById(R.id.btCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCameraHandler.isOpened()) {
                    CameraDialog.showDialog(LauncherActivity.this);
                }
            }
        });

        initUVCCamera();
        isHeadphonesPlugged();

    }

    private boolean isHeadphonesPlugged() {
       AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioDeviceInfo[] audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);
            Log.e(TAG, "isHeadphonesPlugged: "+audioDevices.length );
            for (AudioDeviceInfo deviceInfo : audioDevices) {
                Log.e(TAG, "isHeadphonesPlugged: "+deviceInfo.getType()+"" );
                if (deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                        || deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                    return true;
                }
            }
            return false;
        } else {
            return audioManager.isWiredHeadsetOn();
        }
    }

    private void initUVCCamera() {
        View view = findViewById(R.id.camera_view);
        mUVCCameraView = (CameraViewInterface) view;
        mUVCCameraView.setAspectRatio(PREVIEW_HEIGHT / (float) PREVIEW_HEIGHT);


        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mCameraHandler = UVCCameraHandler.createHandler(this, mUVCCameraView,
                USE_SURFACE_ENCODER ? 0 : 1, PREVIEW_WIDTH, PREVIEW_HEIGHT, PREVIEW_MODE);


    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            Toast.makeText(LauncherActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            mCameraHandler.open(ctrlBlock);
            Log.e("TT", "onConnect");
            startPreview();
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {

        }

        @Override
        public void onDettach(final UsbDevice device) {
            Toast.makeText(LauncherActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {

        }
    };

    private void startPreview() {
        final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
        mCameraHandler.startPreview(new Surface(st));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor.register();
        if (mUVCCameraView != null)
            mUVCCameraView.onResume();
    }

    @Override
    protected void onStop() {
        mCameraHandler.close();
        if (mUVCCameraView != null)
            mUVCCameraView.onPause();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (mCameraHandler != null) {
            mCameraHandler.release();
            mCameraHandler = null;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        mUVCCameraView = null;
        super.onDestroy();
    }

    public void java(View view) {
        String username = etUser.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "please input your name", Toast.LENGTH_LONG).show();
            return;
        }

        // 设置用户名
        App.getInstance().setUsername(username);
        // 添加登录回调
        SocketManager.getInstance().addUserStateCallback(this);
        // 连接socket:登录
        SocketManager.getInstance().connect(Urls.WS, username, 0, mUsbSerialPortThread);


    }

    @Override
    public void userLogin() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void userLogout() {

    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {

    }
}
