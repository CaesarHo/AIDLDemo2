package com.caesar.aidldemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    @BindView(R.id.editText2)
    EditText editText2;
    @BindView(R.id.tv_modify2)
    TextView tvModify2;
    @BindView(R.id.button2)
    Button button2;
    private Unbinder unbinder;
    @BindView(R.id.editText)
    EditText editText;
    @BindView(R.id.button)
    Button button;
    @BindView(R.id.tv_modify)
    TextView tvModify;

    //由AIDL文件生成的Java类
    private MessageCenter messageCenter = null;

    //标志当前与服务端连接状况的布尔值，false为未连接，true为连接中
    private boolean mBound = false;

    //包含Book对象的list
    private List<Info> mInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        onViewClicked();
    }

    /**
     * 跨进程绑定服务
     */
    private void attemptToBindService() {
        Intent intent = new Intent(this,MyService.class);
        intent.setAction("com.vvvv.aidl");
        intent.setPackage("com.caesar.aidldemo.service");
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "attemptToBindService");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBound) {
            attemptToBindService();
        }
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(getLocalClassName(), "完成绑定aidlserver的AIDLService服务");
            messageCenter = MessageCenter.Stub.asInterface(service);
            mBound = true;

            if (messageCenter != null) {
                try {
                    mInfoList = messageCenter.getInfo();
                    Log.e(getLocalClassName(), mInfoList.toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(getLocalClassName(), "无法绑定aidlserver的AIDLService服务");
            mBound = false;
        }
    };


    public void onViewClicked() {
        RxView.clicks(button).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                String s = editText.getText().toString().trim();
                addMessage(s);
            }
        });
        RxView.clicks( button2).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                String s = editText.getText().toString().trim();
                addMessage(s);
            }
        });
    }

    /**
     * 调用服务端的addInfo方法
     */
    public void addMessage(String content) {
        //如果与服务端的连接处于未连接状态，则尝试连接
        if (!mBound) {
            attemptToBindService();
            Toast.makeText(this, "当前与服务端处于未连接状态，正在尝试重连，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        if (messageCenter == null) return;

        Info info = new Info();
        info.setContent(content);
        try {
            info = messageCenter.addInfo(info);
            tvModify.setText(info.getContent());
            Log.e(getLocalClassName(), "客户端发送：" + info.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
