package wivern.com.epicwarbot;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
//import android.widget.Toast;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;

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
        NOT_INIT(0),
        /**
         * task result.
         */
        TASK_RESULT(1),
        /**
         * set settings to service.
         */
        SET_SETTINGS(2),
        /**
         * get settings from service.
         */
        GET_SETTINGS(3),
        /**
         * set log text.
         */
        SET_LOG_TEXT(4),
        /**
         * disconnected msg.
         */
        DISCONNECT(5),
        /**
         * captcha.
         */
        CAPTCHA(6);
        /**
         * int value.
         */
        private final int value;

        /**
         * default private constructor.
         *
         * @param inValue int value
         */
        private MsgId(final int inValue) {
            this.value = inValue;
        }

        /**
         * get int value.
         *
         * @return int value
         */
        public int getValue() {
            return value;
        }
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
     * check box "service is started".
     */
    private CheckBox mChbIsServiceStarted;
    /**
     * check box "collect resources".
     */
    private CheckBox mChbFlagResources;
    /**
     * check box "Send and receive gifts".
     */
    private CheckBox mChbFlagSendReceiveGifts;
    /**
     * check box "Collect cemetery".
     */
    private CheckBox mChbFlagCollectCemetery;
    /**
     * timer interval.
     */
    private EditText mEtInterval;
    /**
     * edit text task log.
     */
    private EditText mEtTasksLog;
    /**
     * edit text field for vk login.
     */
    private EditText mEtVkLogin;
    /**
     * edit text field for vk password.
     */
    private EditText mEtVkPassword;

    /**
     * captcha dialog.
     */
    final int CAPTCHA_DIALOG = 1;

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
        Button btnDoAllTasks;
        /**
         * button disconnect.
         */
        Button btnUpdateSettings;
        /**
         * button start service.
         */
        Button btnStartService;
        /**
         * button stop service.
         */
        Button btnStopService;

        mChbIsServiceStarted =
                (CheckBox) this.findViewById(R.id.chbIsServiceStarted);
        mChbFlagResources =
                (CheckBox) this.findViewById(R.id.chbFlagResources);
        mChbFlagResources.setChecked(true);
        mChbFlagSendReceiveGifts =
                (CheckBox) this.findViewById(R.id.chbFlagSendReceiveGifts);
        mChbFlagSendReceiveGifts.setChecked(true);
        mChbFlagCollectCemetery =
                (CheckBox) this.findViewById(R.id.chbFlagCollectCemetery);
        mChbFlagCollectCemetery.setChecked(true);
        btnDoAllTasks = (Button) this.findViewById(R.id.btnDoAllTasks);
        btnDoAllTasks.setOnClickListener(this);
        btnUpdateSettings = (Button) this.findViewById(R.id.btnUpdateSettings);
        btnUpdateSettings.setOnClickListener(this);
        btnStartService = (Button) this.findViewById(R.id.btnStartService);
        btnStartService.setOnClickListener(this);
        btnStopService = (Button) this.findViewById(R.id.btnStopService);
        btnStopService.setOnClickListener(this);
        mEtTasksLog = (EditText) this.findViewById(R.id.etTasksLog);
        mEtVkLogin = (EditText) this.findViewById(R.id.etVkLogin);
        mEtVkPassword = (EditText) this.findViewById(R.id.etVkPassword);
        mEtVkLogin.setText("13602098361");
        mEtVkPassword.setText("");
        mEtInterval = (EditText) this.findViewById(R.id.etInterval);
        mEtInterval.setText("120");
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
                        msg.what = MsgId.TASK_RESULT.getValue();
                        Bundle sendData = new Bundle();
                        sendData.putParcelable("AnswerInfo", result);
                        msg.setData(sendData);
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onGetLog(final String logText) {
                        Message msg = new Message();
                        msg.what = MsgId.SET_LOG_TEXT.getValue();
                        Bundle sendData = new Bundle();
                        sendData.putString("logText", logText);
                        msg.setData(sendData);
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onCaptcha(String captchaSid) {
                        Message msg = new Message();
                        msg.what = MsgId.CAPTCHA.getValue();
                        Bundle sendData = new Bundle();
                        sendData.putString("CAPTCHA", captchaSid);
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
                    mHandler.sendEmptyMessage(MsgId.GET_SETTINGS.getValue());
                } else {
                    mHandler.sendEmptyMessage(MsgId.SET_SETTINGS.getValue());
                }
                mStartService = false;
                Log.d(LOG_TAG, "Service connected");
                try {
                    mServiceApi.initVkConnection();
                } catch (RemoteException e) {
                    Log.d(LOG_TAG, "addCallback error: " + e.toString());
                }
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
                mChbIsServiceStarted.setChecked(true);
                bindService(mIntent, mBotServiceConnection,
                        Service.BIND_AUTO_CREATE);
            } else {
                mChbIsServiceStarted.setChecked(false);
            }
        }

        return mBound;
    }

    /**
     * on task result.
     *
     * @param ai answer info
     */
    public final void onTaskResult(final AnswerInfo ai) {
        String result = ai.getSzInfo();
        Calendar currDate = Calendar.getInstance();
        SimpleDateFormat dateStringFormatter = new SimpleDateFormat("HH:mm:ss",
                Locale.getDefault());
        String currTime = dateStringFormatter.format(currDate.getTime());
        String currText = currTime + ": " + result;
        if (ai.isbError()) {
            currText = currText + " " + ai.getError();
        }
        currText = currText + "\n";
        mEtTasksLog.setText(currText + mEtTasksLog.getText().toString());
        if (ai.isbError()) {
            Log.d(LOG_TAG, ai.getSzInfo() + " :: " + ai.getError());
        }
        //mEtTasksLog.append(currText, 0, currText.length());
        //Toast.makeText(getApplicationContext(),
        //        result, Toast.LENGTH_SHORT).show();
    }

    /**
     * on captcha.
     *
     * @param captchaSid captcha sid
     */
    public final void onNeedCaptcha(final String captchaSid) {
        String currCaptcha = "http://vk.com/captcha.php?s=1&sid=" + captchaSid;
        captchaDialog(currCaptcha);
    }

    /**
     * create captcha dialog.
     * @param captchaUrl captcha url
     */
    private void captchaDialog(final String captchaUrl)
    {
        android.widget.LinearLayout linLayout = new android.widget.LinearLayout(this);
        linLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        LayoutParams linLayoutParam = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        LayoutParams lpView = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        URL url = null;
        try {
            url = new URL(captchaUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        android.widget.ImageView iv = new android.widget.ImageView(this);
        iv.setImageBitmap(bmp);
        linLayout.addView(iv);

        final android.widget.EditText tvCaptcha = new android.widget.EditText(this);
        tvCaptcha.setLayoutParams(lpView);
        linLayout.addView(tvCaptcha);

        android.app.AlertDialog.Builder adb = new android.app.AlertDialog.Builder(this);
        adb.setTitle("Enter captcha");
        adb.setView(linLayout);
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String newCaptcha = tvCaptcha.getText().toString();
                if(!newCaptcha.isEmpty()) {
                    setNewCaptcha(newCaptcha);
                }
                return;
            }
        });
        Dialog dialog = adb.create();
        dialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                String newCaptcha = tvCaptcha.getText().toString();
                if(!newCaptcha.isEmpty()) {
                    setNewCaptcha(newCaptcha);
                }
                return;
            }
        });
        dialog.show();
    }

    /**
     * continue with capcha.
     */
    private void setNewCaptcha(final String newCaptcha) {
        String newCaptcha2 = newCaptcha;
        //        if (mBound) {
//            try {
//                mServiceApi.setCurrServiceCaptcha(currCaptchaKey);
//                mServiceApi.initVkConnection();
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
    }
    /**
     * set params from service to main activity.
     * this must be in main activity thread
     */
    private void setParamsFromService() {
        Log.d(LOG_TAG, "IN setParamsFromService");
        if (!mBound) {
            return;
        }
        final int secInMin = 60;
        BotServiceSettings bss;
        try {
            bss = mServiceApi.getServiceSettings();
            if (bss != null) {
                Log.d(LOG_TAG, "getServiceSettings login result: "
                        + bss.getVkLogin());
                mEtVkLogin.setText(bss.getVkLogin());
                mEtVkPassword.setText(bss.getVkPassword());
                mChbFlagResources.setChecked(bss.getFlagResources());
                mChbFlagCollectCemetery.setChecked(bss.getFlagCemetery());
                mChbFlagSendReceiveGifts.setChecked(bss.getFlagGifts());
                int interval = bss.getInterval()
                        / secInMin;
                mEtInterval.setText(String.valueOf(interval));
            } else {
                Log.d(LOG_TAG, "getServiceSettings result: null");
            }
            mServiceApi.getLogText();
        } catch (RemoteException e) {
            Log.d(LOG_TAG, "getServiceSettings error: " + e.toString());
        }
    }
    /**
     * set log from service.
     * @param logText log text
     */
    private void setLogFromService(final String logText) {
        if (mEtTasksLog.getText().toString().isEmpty()) {
            mEtTasksLog.setText(logText);
        }
    }
    /**
     * set params from main activity to service.
     * this must be in main activity thread
     */
    private void setParamsToService() {
        Log.d(LOG_TAG, "IN setParamsToService");
        if (!mBound) {
            return;
        }
        final int secInMin = 60;
        BotServiceSettings bss = new BotServiceSettings();
        bss.setLoginAndPass(mEtVkLogin.getText().toString(),
                mEtVkPassword.getText().toString());
        bss.setFlagResources(mChbFlagResources.isChecked());
        bss.setFlagCemetery(mChbFlagCollectCemetery.isChecked());
        bss.setFlagGifts(mChbFlagSendReceiveGifts.isChecked());
        int interval = Integer.parseInt(mEtInterval.getText().toString())
                * secInMin;
        bss.setInterval(interval);
        try {
            mServiceApi.setServiceSettings(bss);
        } catch (RemoteException e) {
            Log.d(LOG_TAG, "setServiceSettings error: " + e.toString());
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
            case R.id.btnDoAllTasks:
                if (mBound) {
                    try {
                        mServiceApi.doAllTask();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btnUpdateSettings:
                setParamsToService();
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
     *
     * @param serviceClass service to check
     * @return true - started
     */
    public final boolean isServiceRunning(final Class<?> serviceClass) {
        ActivityManager manager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service
                : manager.getRunningServices(Integer.MAX_VALUE)) {
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
                Bundle sendData;
                switch (currMsgId) {
                    case TASK_RESULT:
                        sendData = msg.getData();
                        if (sendData != null) {
                            AnswerInfo ai =
                                    sendData.getParcelable("AnswerInfo");
                            if (ai != null) {
                                mActivity.onTaskResult(ai);
                            }
                        }
                        break;
                    case GET_SETTINGS:
                        mActivity.setParamsFromService();
                        break;
                    case SET_SETTINGS:
                        mActivity.setParamsToService();
                        break;
                    case SET_LOG_TEXT:
                        sendData = msg.getData();
                        if (sendData != null) {
                            String logText =
                                    sendData.getString("logText");
                            if (logText != null) {
                                mActivity.setLogFromService(logText);
                            }
                        }
                        break;
                    case CAPTCHA:
                        sendData = msg.getData();
                        if (sendData != null) {
                            String captchaSid =
                                    sendData.getString("CAPTCHA");
                            if (captchaSid != null) {
                                mActivity.onNeedCaptcha(captchaSid);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

    }
}
