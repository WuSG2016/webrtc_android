package com.dds;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.dds.core.MainActivity;
import com.dds.core.base.BaseActivity;
import com.dds.core.consts.Urls;
import com.dds.core.serialport.SerialCmdBean;
import com.dds.core.serialport.UsbSerialThread;
import com.dds.core.socket.IUserState;
import com.dds.core.socket.SocketManager;
import com.dds.webrtc.R;
import com.shy.rockerview.RockerView;

public class LauncherActivity extends BaseActivity implements IUserState {
    private Toolbar toolbar;
    private EditText etUser;
    private Button button8;
    private UsbSerialThread mUsbSerialPortThread;

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

        RockerView rockerView = findViewById(R.id.rockerXY_View);
        rockerView.setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void angle(double angle) {
                double result = (angle) % 90;
                Log.e("angle: ", angle + "");
                Log.e("result: ", result + "");

            }

            @Override
            public void onFinish() {

            }
        });
        rockerView.setOnDistanceLevelListener(new RockerView.OnDistanceLevelListener() {
            @Override
            public void onDistanceLevel(int level) {
                Log.e("onDistanceLevel: ", level + "");
            }
        });
        rockerView.setOnShakeListener(RockerView.DirectionMode.DIRECTION_8, new RockerView.OnShakeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void direction(RockerView.Direction direction) {
                Log.e("angle: ", direction + "");



            }


            @Override
            public void onFinish() {

            }
        });
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
}
