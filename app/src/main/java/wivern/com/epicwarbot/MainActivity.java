package wivern.com.epicwarbot;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
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
 */
public class MainActivity extends ActionBarActivity
        implements View.OnClickListener {
    /**
     * log tag.
     */
    private final String mLogTag = "BotMainActivity";
    /**
     * is bound captured.
     */
    private boolean mBound = false;

    /**
     * add service.
     */
    public enum MsgId {
        /**
         * not initialized msg.
         */
        NOT_INIT,
        /**
         * connected msg.
         */
        CONNECT,
        /**
         * disconnected msg.
         */
        DISCONNECT
    }

    /**
     * button connect.
     */
    private Button mBtnConnect;
    /**
     * button disconnect.
     */
    private Button mBtnDisconnect;
    /**
     * button start service.
     */
    private Button mBtnStartService;
    /**
     * button stop service.
     */
    private Button mBtnStopService;
    /**
     * edit text field for vk login.
     */
    private EditText mVkLogin;
    /**
     * edit text field for vk password.
     */
    private EditText mVkPassword;
    /**
     * handler for send messages from service callback functions
     * to main activity.
     */
    private MHandler mHandler;
    /**
     * api for use the service.
     */
    private IBotService mServiceApi;
    /**
     * callback stub for service.
     */
    private IBotServiceCallback mServiceCallback;
    /**
     * service connection.
     */
    private ServiceConnection mBotServiceConnection;
    /**
     * intent for service.
     */
    private Intent mIntent;
    /**
     * main service object.
     */
    private MainService mService;

    /**
     * on create activity.
     *
     * @param savedInstanceState state
     */
    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnConnect = (Button) this.findViewById(R.id.btnConnect);
        mBtnConnect.setOnClickListener(this);
        mBtnDisconnect = (Button) this.findViewById(R.id.btnDisconnect);
        mBtnDisconnect.setOnClickListener(this);
        mBtnStartService = (Button) this.findViewById(R.id.btnStartService);
        mBtnStartService.setOnClickListener(this);
        mBtnStopService = (Button) this.findViewById(R.id.btnStopService);
        mBtnStopService.setOnClickListener(this);
        mVkLogin = (EditText) this.findViewById(R.id.etVkLogin);
        mVkPassword = (EditText) this.findViewById(R.id.etVkPassword);
        mVkLogin.setText("13602098361");
        mVkPassword.setText("");
        mHandler = new MHandler(this);
        mIntent = new Intent(this, MainService.class);
        mIntent.setAction("service.EpicWarBot");
        connectToService();
    }

    /**
     * Connect to service.
     *
     * @return is bound (boolean)
     */
    protected final boolean connectToService() {
        mBotServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(final ComponentName name,
                                           final IBinder service) {
                mServiceApi = IBotService.Stub.asInterface(service);
                mServiceCallback = new IBotServiceCallback.Stub() {
                    @Override
                    public void onConnectedResult(final String result)
                            throws RemoteException {
                        Message msg = new Message();
                        msg.what = 1;
                        Bundle sendData = new Bundle();
                        sendData.putString("result", result);
                        msg.setData(sendData);
                        mHandler.sendMessage(msg);
                    }
                };
                mBound = true;
                Log.d(mLogTag, "Service connected");
            }

            @Override
            public void onServiceDisconnected(final ComponentName name) {
                mServiceApi = null;
                mServiceCallback = null;
                mBound = false;
                Log.d(mLogTag, "Service disconnected");
            }
        };
        if (mBound) {
            // binding to remote service
            bindService(mIntent, mBotServiceConnection,
                    Service.BIND_AUTO_CREATE);
        }

        return mBound;
    }

    /**
     * on create options menu.
     *
     * @param menu
     * @return true
     */
    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * on options item selected.
     *
     * @param item menu item
     * @return onOptionsItemSelected
     */
    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
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

    /**
     * listener for button clicks.
     *
     * @param v curr button
     */
    @Override
    public final void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btnConnect:
                if (mBound) {
                    try {
                        mServiceApi.connect(mServiceCallback);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

//                if (m_engineLooper != null) {
//                    Bundle sdata = new Bundle();
//                    sdata.putString("login", m_vkLogin.getText().toString());
//    sdata.putString("password", m_vkPassword.getText().toString());
//   m_engineLooper.SendMessage(LooperThread.MSG_ID_CONNECT, sdata);
//                }
                break;
            case R.id.btnDisconnect:
//                if (m_engineLooper != null) {
//                    m_engineLooper
//   .SendMessage(LooperThread.MSG_ID_DISCONNECT, null);
//                }
                break;
            default:
                break;
        }
    }
    /**
     * @since 1.0
     * handler class
     */
    @SuppressLint("HandlerLeak")
    public class MHandler extends Handler {
        /**
         * link to main activity.
         */
        private MainActivity mActivity;

        /**
         * constructor for handler.
         *
         * @param activity main acivity
         */
        MHandler(final MainActivity activity) {
            mActivity = activity;
        }

        /**
         * handle message.
         *
         * @param msg message
         */
        @Override
        public final void handleMessage(final Message msg) {
            super.handleMessage(msg);
            if (mActivity != null) {
                MsgId currMsgId = MsgId.values()[msg.what];
                switch (currMsgId) {
                    case CONNECT:
                        Bundle sendData = msg.getData();
                        if (sendData != null) {
                            String result = sendData.getString("result");
                            Toast.makeText(getApplicationContext(),
                                    result, Toast.LENGTH_SHORT).show();
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
                    default:
                        break;
                }
            }
        }

    }
}
