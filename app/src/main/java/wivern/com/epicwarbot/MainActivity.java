package wivern.com.epicwarbot;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @since 1.0
 * Main activity class
 * http://www.compiletimeerror.com/2015/01/android-aidl-tutorial-with-example.html#.VOB3T9hoh5I
 * http://habrahabr.ru/post/139432/
 */
public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    /**
     * @since 1.0
     * tag for log
     */
    private final String mLogTag = "BotMainActivity";
    /**
     * @since 1.0
     * is bound captured
     */
    private boolean mBound = false;
    /**
     * @since 1.0
     * add service
     */
    public enum MsgId {
        NOT_INIT, CONNECT, DISCONNECT
    }
    Button m_btnConnect;
    Button m_btnDisconnect;
    EditText m_vkLogin;
    EditText m_vkPassword;
    private MHandler m_handler;

    protected IBotService mServiceApi;
    protected IBotServiceCallback mServiceCallback;
    ServiceConnection mBotServiceConnection;
    Intent mIntent;
    MainService mService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_btnConnect = (Button) this.findViewById(R.id.btnConnect);
        m_btnConnect.setOnClickListener(this);
        m_btnDisconnect = (Button) this.findViewById(R.id.btnDisconnect);
        m_btnDisconnect.setOnClickListener(this);
        m_vkLogin = (EditText) this.findViewById(R.id.etVkLogin);
        m_vkPassword = (EditText) this.findViewById(R.id.etVkPassword);
        m_vkLogin.setText("13602098361");
        m_vkPassword.setText("");
        m_handler = new MHandler(this);
        mIntent = new Intent(this, MainService.class);
        mIntent.setAction("service.EpicWarBot");
        ConnectToService();
    }

    protected void ConnectToService()
    {
        mBotServiceConnection = new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mServiceApi = IBotService.Stub.asInterface(service);
                mServiceCallback = new IBotServiceCallback.Stub() {
                    @Override
                    public void OnConnectedResult(String result) throws RemoteException {
                        Message msg = new Message();
                        msg.what = 1;
                        Bundle sendData = new Bundle();
                        sendData.putString("result", result);
                        msg.setData(sendData);
                        m_handler.sendMessage(msg);
                    }
                };
                mBound = true;
                Log.d(mLogTag, "Service connected");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServiceApi = null;
                mServiceCallback = null;
                mBound = false;
                Log.d(mLogTag, "Service disconnected");
            }
        };
        if (mBound == false) {
            // binding to remote service
            bindService(mIntent, mBotServiceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConnect:
                if(mBound == true)
                {
                    try {
                        mServiceApi.Connect(mServiceCallback);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

//                if (m_engineLooper != null) {
//                    Bundle sdata = new Bundle();
//                    sdata.putString("login", m_vkLogin.getText().toString());
//                    sdata.putString("password", m_vkPassword.getText().toString());
//                    m_engineLooper.SendMessage(LooperThread.MSG_ID_CONNECT, sdata);
//                }
                break;
            case R.id.btnDisconnect:
//                if (m_engineLooper != null) {
//                    m_engineLooper
//                            .SendMessage(LooperThread.MSG_ID_DISCONNECT, null);
//                }
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    public class MHandler extends Handler {
        MainActivity m_activity;

        MHandler(MainActivity ma) {
            m_activity = ma;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (m_activity != null) {
                MsgId currMsgId = MsgId.values()[msg.what];
                switch (currMsgId) {
                    case CONNECT:
                        Bundle sendData = msg.getData();
                        if (sendData != null) {
                            String result = sendData.getString("result");
                            Toast.makeText(getApplicationContext(),
                                    result, Toast.LENGTH_SHORT)
                                    .show();
                        }
                                                 // LooperThread.MSG_ID_CONNECT
//                        Bundle sdata = msg.getData();
//                        if (sdata != null) {
//                            String funcInfo = sdata.getString("funcInfo");
//                            String info = sdata.getString("info");
//                            boolean ferror = sdata.getBoolean("error");
//                            if (ferror == true) {
//                                String errorMsg = sdata.getString("errorMsg");
//
//                                Toast.makeText(m_activity, errorMsg,
//                                        Toast.LENGTH_LONG).show();
//                            } else {
//                                if (funcInfo != null) {
//                                    Toast.makeText(m_activity,
//                                            funcInfo + ": " + info,
//                                            Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        }

                        break;
                }
            }
        }

    }
}
