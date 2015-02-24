package wivern.com.epicwarbot;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
//import android.support.v7.app.ActionBarActivity;
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
public class MainActivity extends Activity
        implements View.OnClickListener {
    /**
     * log tag.
     */
    private static final String LOG_TAG = "BotMainActivity";
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
     * intent for service.
     */
    private Intent mIntent;
    /**
     * service connection.
     */
    private ServiceConnection mBotServiceConnection;
    /**
     * on start service - true.
     */
    private boolean mStartService;
    /**
     * on create activity.
     *
     * @param savedInstanceState state
     */
    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * button connect.
         */
        Button btnConnect;
        /**
         * button disconnect.
         */
        Button btnDisconnect;
        /**
         * button start service.
         */
        Button btnStartService;
        /**
         * button stop service.
         */
        Button btnStopService;
        /**
         * edit text field for vk login.
         */
        EditText vkLogin;
        /**
         * edit text field for vk password.
         */
        EditText vkPassword;
        btnConnect = (Button) this.findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(this);
        btnDisconnect = (Button) this.findViewById(R.id.btnDisconnect);
        btnDisconnect.setOnClickListener(this);
        btnStartService = (Button) this.findViewById(R.id.btnStartService);
        btnStartService.setOnClickListener(this);
        btnStopService = (Button) this.findViewById(R.id.btnStopService);
        btnStopService.setOnClickListener(this);
        vkLogin = (EditText) this.findViewById(R.id.etVkLogin);
        vkPassword = (EditText) this.findViewById(R.id.etVkPassword);
        vkLogin.setText("13602098361");
        vkPassword.setText("");
        mHandler = new MHandler(this);
        mIntent = new Intent(this, MainService.class);
        mIntent.setAction("service.EpicWarBot");
        connectToService();
    }

    /**
     * on destroy activity.
     */
    @Override
    protected final void onDestroy() {
        super.onDestroy();
        disconnectFromService();
    }

    /**
     * unbind from service.
     */
    private void disconnectFromService() {
        if (mBound) {
            try {
                mServiceApi.removeCallback(mServiceCallback);
            } catch (RemoteException e) {
                Log.d(LOG_TAG, "removeCallback error: " + e.toString());
            }
            unbindService(mBotServiceConnection);
            mBound = false;
            mServiceApi = null;
            mServiceCallback = null;
        }
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
                    /**
                     * callback for connection result.
                     * @param result string with result
                     */
                    @Override
                    public void onTaskResult(final AnswerInfo result) {
                        Message msg = new Message();
                        msg.what = 1;
                        Bundle sendData = new Bundle();
                        sendData.putString("result", result.getSzInfo());
                        msg.setData(sendData);
                        mHandler.sendMessage(msg);
                    }
                };
                try {
                    mServiceApi.addCallback(mServiceCallback);
                } catch (RemoteException e) {
                    Log.d(LOG_TAG, "addCallback error: " + e.toString());
                }
                mBound = true;
                if (!mStartService) {
                    setParamsFromService();
                }
                Log.d(LOG_TAG, "Service connected");
            }

            /**
             * onServiceDisconnected is only called in extreme situations.
             * (crashed / killed)
             * @param name component name
             */
            @Override
            public void onServiceDisconnected(final ComponentName name) {
                mServiceApi = null;
                mServiceCallback = null;
                mBound = false;
                Log.d(LOG_TAG, "Service disconnected");
            }
        };
        if (!mBound) {
            // binding to remote service
            if (isServiceRunning(MainService.class)) {
                bindService(mIntent, mBotServiceConnection,
                        Service.BIND_AUTO_CREATE);
            }
        }

        mStartService = false;

        return mBound;
    }

    /**
     * set params from service to main activity.
     */
    private void setParamsFromService() {
        Log.d(LOG_TAG, "IN setServiceParams");
        if (!mBound) {
            return;
        }
        AnswerInfo ai;
        try {
            ai = mServiceApi.getServiceVariables();
            if (ai != null) {
                Log.d(LOG_TAG, "getServiceVariables result: " + ai.getSzInfo());
            } else {
                Log.d(LOG_TAG, "getServiceVariables result: null");
            }
        } catch (RemoteException e) {
            Log.d(LOG_TAG, "getServiceVariables error: " + e.toString());
        }

    }

    /**
     * on create options menu.
     *
     * @param menu menu
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
                        mServiceApi.doAllTask();
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
            case R.id.btnStartService:
                if (!mBound) {
                    mStartService = true;
                    startService(mIntent);
                    connectToService();
                }

                break;
            case R.id.btnStopService:
                disconnectFromService();
                stopService(mIntent);
                break;
            default:
                break;
        }
    }
    /**
     * check that service is running.
     * @param serviceClass service to check
     * @return true - started
     */
    public final boolean isServiceRunning(final Class<?> serviceClass) {
        ActivityManager manager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service
                :manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    /**
     * @since 1.0
     * handler class
     */
    public static class MHandler extends Handler {
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
                            Toast.makeText(mActivity.getApplicationContext(),
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
