package com.dds.core.voip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dds.core.serialport.CmdBean;
import com.dds.core.serialport.ISerialPortCmd;
import com.dds.core.util.BarUtils;
import com.dds.core.util.OSUtils;
import com.dds.skywebrtc.CallSession;
import com.dds.skywebrtc.EnumType.CallState;
import com.dds.skywebrtc.SkyEngineKit;
import com.dds.webrtc.R;
import com.shy.rockerview.RockerView;

import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by dds on 2018/7/26.
 * android_shuai@163.com
 * 视频通话控制界面
 */
public class FragmentVideo extends SingleCallFragment implements View.OnClickListener {
    private static final String TAG = "FragmentVideo";
    private ImageView outgoingAudioOnlyImageView;
    private LinearLayout audioLayout;
    private ImageView incomingAudioOnlyImageView;
    private LinearLayout hangupLinearLayout;
    private LinearLayout acceptLinearLayout;
    private ImageView connectedAudioOnlyImageView;
    private ImageView connectedHangupImageView;
    private ImageView switchCameraImageView;
    private FrameLayout fullscreenRenderer;
    private FrameLayout pipRenderer;
    private LinearLayout inviteeInfoContainer;
    private boolean isFromFloatingView = false;
    private SurfaceViewRenderer localSurfaceView;
    private SurfaceViewRenderer remoteSurfaceView;
    private ThreadPoolExecutor executors;

    private int upInt;
    private int speedLevel;
    private int oInt;
    private int level;
    private int angle;
    private long startTime = 0L;
    Handler mH;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (callSingleActivity != null) {
            isFromFloatingView = callSingleActivity.isFromFloatingView();
        }
    }

    @Override
    int getLayout() {
        return R.layout.fragment_video;
    }

    @SuppressLint("StringFormatMatches")
    @Override
    public void initView(View view) {
        super.initView(view);
        fullscreenRenderer = view.findViewById(R.id.fullscreen_video_view);
        pipRenderer = view.findViewById(R.id.pip_video_view);
        inviteeInfoContainer = view.findViewById(R.id.inviteeInfoContainer);
        outgoingAudioOnlyImageView = view.findViewById(R.id.outgoingAudioOnlyImageView);
        audioLayout = view.findViewById(R.id.audioLayout);
        incomingAudioOnlyImageView = view.findViewById(R.id.incomingAudioOnlyImageView);
        hangupLinearLayout = view.findViewById(R.id.hangupLinearLayout);
        acceptLinearLayout = view.findViewById(R.id.acceptLinearLayout);
        connectedAudioOnlyImageView = view.findViewById(R.id.connectedAudioOnlyImageView);
        connectedHangupImageView = view.findViewById(R.id.connectedHangupImageView);
        switchCameraImageView = view.findViewById(R.id.switchCameraImageView);
        executors = new ThreadPoolExecutor(1, 1, 30L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1), new ThreadPoolExecutor.DiscardOldestPolicy());
        HandlerThread handlerThread = new HandlerThread("CmdHandler");

        handlerThread.start();

        mH = new Handler(handlerThread.getLooper(), msg -> {
            angle %= 90;
            switch (oInt) {
                case ISerialPortCmd.CENTER_CMD:
                    return false;
                case ISerialPortCmd.DOWN_CMD:
                case ISerialPortCmd.UP_CMD:
                    angle = 0;
                    break;
                case ISerialPortCmd.LEFT_CMD:
                case ISerialPortCmd.RIGHT_CMD:
                    angle = 90;
                    break;
                default:
                    break;
            }
            gEngineKit.getCurrentSession().sendSerialPortEvent(oInt, upInt, level, angle);
            return false;
        });
        RockerView rockerViewXy = view.findViewById(R.id.rockerXY_View);
        TextView tvRxyAngle = view.findViewById(R.id.tvAngleXy);
        TextView tvOrientationXy = view.findViewById(R.id.tvOrientationXy);

        RockerView rockerView = view.findViewById(R.id.rockerZ_View);
        TextView tvRzAngle = view.findViewById(R.id.tvAngleZ);
        TextView tvRzLevel = view.findViewById(R.id.tvLevelZ);

        String tvOrientation = callSingleActivity.getString(R.string.text_orientation);
        String tvAngle = callSingleActivity.getString(R.string.text_angle);
        String tvLevel = callSingleActivity.getString(R.string.text_level);
        CallSession callSession = gEngineKit.getCurrentSession();
        rockerViewXy.setOnDistanceLevelListener(new RockerView.OnDistanceLevelListener() {
            @Override
            public void onDistanceLevel(int level) {
                FragmentVideo.this.level = (int) (level * 2.5f);

            }
        });
        rockerViewXy.setOnShakeListener(RockerView.DirectionMode.DIRECTION_8, new RockerView.OnShakeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void direction(RockerView.Direction direction) {
                switch (direction) {
                    case DIRECTION_UP:
                        tvOrientationXy.setText(String.format(tvOrientation, "上"));
//                        callSession.sendSerialPortEvent(ISerialPortCmd.UP_CMD, upInt, speedLevel);
                        upInt = ISerialPortCmd.UP_SPEED_TURE;
                        oInt = ISerialPortCmd.UP_CMD;
                        break;
                    case DIRECTION_UP_LEFT:
                        tvOrientationXy.setText(String.format(tvOrientation, "左上"));
//                        callSession.sendSerialPortEvent(ISerialPortCmd.UP_LEFT_CMD, upInt, speedLevel);
                        oInt = ISerialPortCmd.UP_LEFT_CMD;
                        upInt = ISerialPortCmd.UP_SPEED_TURE;
                        break;
                    case DIRECTION_UP_RIGHT:
                        tvOrientationXy.setText(String.format(tvOrientation, "右上"));
//                        callSession.sendSerialPortEvent(ISerialPortCmd.UP_RIGHT_CMD, upInt, speedLevel);
                        oInt = ISerialPortCmd.UP_RIGHT_CMD;
                        upInt = ISerialPortCmd.UP_SPEED_TURE;
                        break;
                    case DIRECTION_DOWN:
                        tvOrientationXy.setText(String.format(tvOrientation, "下"));
//                        callSession.sendSerialPortEvent(ISerialPortCmd.DOWN_CMD, upInt, speedLevel);
                        oInt = ISerialPortCmd.DOWN_CMD;
                        upInt = ISerialPortCmd.UP_SPEED_FALSE;
                        break;
                    case DIRECTION_DOWN_LEFT:
                        tvOrientationXy.setText(String.format(tvOrientation, "左下"));
//                        callSession.sendSerialPortEvent(ISerialPortCmd.DOWN_LEFT_CMD, upInt, speedLevel);
                        oInt = ISerialPortCmd.DOWN_LEFT_CMD;
                        upInt = ISerialPortCmd.UP_SPEED_FALSE;
                        break;
                    case DIRECTION_DOWN_RIGHT:
                        tvOrientationXy.setText(String.format(tvOrientation, "右下"));
//                        callSession.sendSerialPortEvent(ISerialPortCmd.DOWN_RIGHT_CMD, upInt, speedLevel);
                        oInt = ISerialPortCmd.DOWN_RIGHT_CMD;
                        upInt = ISerialPortCmd.UP_SPEED_FALSE;
                        break;

                    case DIRECTION_LEFT:
                        tvOrientationXy.setText(String.format(tvOrientation, "左"));
//                        callSession.sendSerialPortEvent(ISerialPortCmd.LEFT_CMD, upInt, speedLevel);
                        oInt = ISerialPortCmd.LEFT_CMD;
                        break;
                    case DIRECTION_RIGHT:
                        tvOrientationXy.setText(String.format(tvOrientation, "右"));
//                        callSession.sendSerialPortEvent(ISerialPortCmd.RIGHT_CMD, upInt, speedLevel);
                        oInt = ISerialPortCmd.RIGHT_CMD;
                        break;
                    case DIRECTION_CENTER:
                        tvOrientationXy.setText(String.format(tvOrientation, "中心"));
                        callSession.sendSerialPortEvent(ISerialPortCmd.CENTER_CMD, upInt, speedLevel * 2, 0);

                        break;

                    default:
                        break;

                }

            }

            @Override
            public void onFinish() {

            }
        });


        rockerViewXy.setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
            @Override
            public void onStart() {

            }

            @SuppressLint("StringFormatMatches")
            @Override
            public void angle(double angle) {
                FragmentVideo.this.angle = (int) angle;
                long nowTime = System.currentTimeMillis();
                if (nowTime - startTime > 500) {
                    startTime = nowTime;
                    mH.sendEmptyMessage(1);
                }
                tvRxyAngle.setText(String.format(tvAngle, angle));
            }

            @Override
            public void onFinish() {

            }
        });
//        rockerView.setOnDistanceLevelListener(level -> {
//            this.speedLevel = level;
//            tvRzLevel.setText(String.format(tvLevel, level));
//            callSession.sendSerialPortEvent(oInt, upInt, speedLevel, angle);
//        });
//        rockerView.setOnShakeListener(RockerView.DirectionMode.DIRECTION_2_VERTICAL, new RockerView.OnShakeListener() {
//            @Override
//            public void onStart() {
//
//            }
//
//            @Override
//            public void direction(RockerView.Direction direction) {
//                switch (direction) {
//                    case DIRECTION_UP:
//                        tvRzAngle.setText(String.format(tvOrientation, "上"));
//                        upInt = ISerialPortCmd.UP_SPEED_TURE;
//                        break;
//                    case DIRECTION_DOWN:
//                        upInt = ISerialPortCmd.UP_SPEED_FALSE;
//                        tvRzAngle.setText(String.format(tvOrientation, "下"));
//                        break;
//                }
//
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        });

        outgoingHangupImageView.setOnClickListener(this);
        incomingHangupImageView.setOnClickListener(this);
        minimizeImageView.setOnClickListener(this);
        connectedHangupImageView.setOnClickListener(this);
        acceptImageView.setOnClickListener(this);
        switchCameraImageView.setOnClickListener(this);
        pipRenderer.setOnClickListener(this);
        outgoingAudioOnlyImageView.setOnClickListener(this);
        incomingAudioOnlyImageView.setOnClickListener(this);
        connectedAudioOnlyImageView.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || OSUtils.isMiui() || OSUtils.isFlyme()) {
            lytParent.post(() -> {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) inviteeInfoContainer.getLayoutParams();
                params.topMargin = (int) (BarUtils.getStatusBarHeight() * 1.2);
                inviteeInfoContainer.setLayoutParams(params);
                RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) minimizeImageView.getLayoutParams();
                params1.topMargin = BarUtils.getStatusBarHeight();
                minimizeImageView.setLayoutParams(params1);
            });

            pipRenderer.post(() -> {
                FrameLayout.LayoutParams params2 = (FrameLayout.LayoutParams) pipRenderer.getLayoutParams();
                params2.topMargin = (int) (BarUtils.getStatusBarHeight() * 1.2);
                pipRenderer.setLayoutParams(params2);
            });
        }
//        if(isOutgoing){ //测试崩溃对方是否会停止
//            lytParent.postDelayed(() -> {
//                int i = 1 / 0;
//            }, 10000);
//        }

    }


    @Override
    public void init() {
        super.init();
        CallSession session = gEngineKit.getCurrentSession();
        if (session != null) {
            currentState = session.getState();
        }
        if (session == null || CallState.Idle == session.getState()) {
            if (callSingleActivity != null) {
                callSingleActivity.finish();
            }
        } else if (CallState.Connected == session.getState()) {
            incomingActionContainer.setVisibility(View.GONE);
            outgoingActionContainer.setVisibility(View.GONE);
            connectedActionContainer.setVisibility(View.GONE);
            inviteeInfoContainer.setVisibility(View.GONE);
            minimizeImageView.setVisibility(View.VISIBLE);
            startRefreshTime();
        } else {
            if (isOutgoing) {
                incomingActionContainer.setVisibility(View.GONE);
                outgoingActionContainer.setVisibility(View.VISIBLE);//
                connectedActionContainer.setVisibility(View.GONE);
                descTextView.setText(R.string.av_waiting);
            } else {
                incomingActionContainer.setVisibility(View.VISIBLE);
                outgoingActionContainer.setVisibility(View.GONE);
                connectedActionContainer.setVisibility(View.GONE);
                descTextView.setText(R.string.av_video_invite);
                if (currentState == CallState.Incoming) {
                    View surfaceView = gEngineKit.getCurrentSession().setupLocalVideo(false);
                    Log.d(TAG, "init surfaceView != null is " + (surfaceView != null) + "; isOutgoing = " + isOutgoing + "; currentState = " + currentState);
                    if (surfaceView != null) {
                        localSurfaceView = (SurfaceViewRenderer) surfaceView;
                        localSurfaceView.setZOrderMediaOverlay(false);
                        fullscreenRenderer.addView(localSurfaceView);
                    }
                }
            }
        }
        if (isFromFloatingView) {
            didCreateLocalVideoTrack();
            if (session != null) {
                didReceiveRemoteVideoTrack(session.mTargetId);
            }
        }
    }

    @Override
    public void didChangeState(CallState state) {
        currentState = state;
        Log.d(TAG, "didChangeState, state = " + state);
        runOnUiThread(() -> {
            if (state == CallState.Connected) {

                incomingActionContainer.setVisibility(View.GONE);
                outgoingActionContainer.setVisibility(View.GONE);
                connectedActionContainer.setVisibility(View.GONE);
                inviteeInfoContainer.setVisibility(View.GONE);
                descTextView.setVisibility(View.GONE);
                minimizeImageView.setVisibility(View.VISIBLE);
                // 开启计时器
                startRefreshTime();
            } else {
                // do nothing now
            }
        });
    }

    @Override
    public void didChangeMode(Boolean isAudio) {
        runOnUiThread(() -> callSingleActivity.switchAudio());
    }


    @Override
    public void didCreateLocalVideoTrack() {
        if (localSurfaceView == null) {
            View surfaceView = gEngineKit.getCurrentSession().setupLocalVideo(true);
            if (surfaceView != null) {
                localSurfaceView = (SurfaceViewRenderer) surfaceView;
            } else {
                if (callSingleActivity != null) callSingleActivity.finish();
                return;
            }
        } else {
            localSurfaceView.setZOrderMediaOverlay(true);
        }
        Log.d(TAG,
                "didCreateLocalVideoTrack localSurfaceView != null is " + (localSurfaceView != null) + "; remoteSurfaceView == null = " + (remoteSurfaceView == null)
        );

        if (localSurfaceView.getParent() != null) {
            ((ViewGroup) localSurfaceView.getParent()).removeView(localSurfaceView);
        }
        if (isOutgoing && remoteSurfaceView == null) {
            if (fullscreenRenderer != null && fullscreenRenderer.getChildCount() != 0)
                fullscreenRenderer.removeAllViews();
            fullscreenRenderer.addView(localSurfaceView);
        } else {
            if (pipRenderer.getChildCount() != 0) pipRenderer.removeAllViews();
            pipRenderer.addView(localSurfaceView);
        }
    }


    @Override
    public void didReceiveRemoteVideoTrack(String userId) {
        pipRenderer.setVisibility(View.VISIBLE);
        if (localSurfaceView != null) {
            localSurfaceView.setZOrderMediaOverlay(true);
            if (isOutgoing) {
                if (localSurfaceView.getParent() != null) {
                    ((ViewGroup) localSurfaceView.getParent()).removeView(localSurfaceView);
                }
                pipRenderer.addView(localSurfaceView);
            }
        }


        View surfaceView = gEngineKit.getCurrentSession().setupRemoteVideo(userId, false);
        Log.d(TAG, "didReceiveRemoteVideoTrack,surfaceView = " + surfaceView);
        if (surfaceView != null) {
            fullscreenRenderer.setVisibility(View.VISIBLE);
            remoteSurfaceView = (SurfaceViewRenderer) surfaceView;
            fullscreenRenderer.removeAllViews();
            if (remoteSurfaceView.getParent() != null) {
                ((ViewGroup) remoteSurfaceView.getParent()).removeView(remoteSurfaceView);
            }
            fullscreenRenderer.addView(remoteSurfaceView);
        }
    }

    @Override
    public void didUserLeave(String userId) {

    }

    @Override
    public void didError(String error) {

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        // 接听
        CallSession session = gEngineKit.getCurrentSession();

        // 接听
        if (id == R.id.acceptImageView) {
            if (session != null && session.getState() == CallState.Incoming) {
                session.joinHome(session.getRoomId());
            } else if (session != null) {
                if (callSingleActivity != null) {
                    session.sendRefuse();
                    callSingleActivity.finish();
                }
            }
        }
        // 挂断电话
        if (id == R.id.incomingHangupImageView || id == R.id.outgoingHangupImageView || id == R.id.connectedHangupImageView) {
            if (session != null) {
                Log.d(TAG, "endCall");
                SkyEngineKit.Instance().endCall();
            }
            if (callSingleActivity != null) callSingleActivity.finish();
        }

        // 切换摄像头
        if (id == R.id.switchCameraImageView) {
            session.switchCamera();
        }
        if (id == R.id.pip_video_view) {
            boolean isFullScreenRemote = fullscreenRenderer.getChildAt(0) == remoteSurfaceView;
            fullscreenRenderer.removeAllViews();
            pipRenderer.removeAllViews();
            if (isFullScreenRemote) {
                remoteSurfaceView.setZOrderMediaOverlay(true);
                pipRenderer.addView(remoteSurfaceView);
                localSurfaceView.setZOrderMediaOverlay(false);
                fullscreenRenderer.addView(localSurfaceView);
            } else {
                localSurfaceView.setZOrderMediaOverlay(true);
                pipRenderer.addView(localSurfaceView);
                remoteSurfaceView.setZOrderMediaOverlay(false);
                fullscreenRenderer.addView(remoteSurfaceView);
            }
        }

        // 切换到语音拨打
        if (id == R.id.outgoingAudioOnlyImageView || id == R.id.incomingAudioOnlyImageView || id == R.id.connectedAudioOnlyImageView) {
            if (session != null) {
                if (callSingleActivity != null) callSingleActivity.isAudioOnly = true;
                session.switchToAudio();
            }
        }

        // 小窗
        if (id == R.id.minimizeImageView) {
            if (callSingleActivity != null) callSingleActivity.showFloatingView();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fullscreenRenderer.removeAllViews();
        pipRenderer.removeAllViews();
    }
}