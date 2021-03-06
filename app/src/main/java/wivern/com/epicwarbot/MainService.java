package wivern.com.epicwarbot;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteCallbackList;
/**
 * remo.
 */
import android.os.RemoteException;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.gson.Gson;

import java.net.CookieManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @since 1.0
 * My service class
 */
public class MainService extends Service {
    /**
     * tag for log.
     */
    private static final String LOG_TAG = "BotService";
    /**
     * callbacks for report answer info to all connected main activity.
     */
    private final RemoteCallbackList<IBotServiceCallback> mCallbacks
            = new RemoteCallbackList<>();
    /**
     * notify icon status.
     */
    private static final int PLAYBACK_SERVICE_STATUS = 1;
    /**
     * cookie string tag for save.
     */
    private static final String PREF_SESSION_COOKIE = "session_cookie";
    /**
     * main timer.
     */
    private Timer mTimer;
    /**
     * main timer task.
     */
    private TimerTask mTimerTask;
    /**
     * bot settings.
     */
    private BotServiceSettings mBotSettings;
    /**
     * log string.
     */
    private String mLogText;
    /**
     * main task alarm.
     */
    private MainTaskAlarm mTaskAlarm;
    /**
     * last time of doAllTasks.
     */
    private Calendar mLastDoAllTasks;
    /**
     * last captcha sid for vk login.
     */
    private String lastCaptchaSid = "";
    /**
     * last captcha key for vk login.
     */
    private String lastCaptchaKey = "";
    /**
     * main bot object.
     */
    EpicWarBot mEpicBot;
    /**
     * add text to log.
     * @param addText text to add
     * @param errorText error text
     */
    public final void addLogText(final String addText, final String errorText) {
        Calendar currDate = Calendar.getInstance();
        SimpleDateFormat dateStringFormatter = new SimpleDateFormat("HH:mm:ss",
                Locale.getDefault());
        String currTime = dateStringFormatter.format(currDate.getTime());
        String currText = currTime + ": " + addText;
        if (errorText != null && !errorText.isEmpty()) {
            currText = currText + " " + errorText;
        }
        currText = currText + "\n";
        if (mLogText == null) {
            mLogText = currText;
        } else {
            mLogText = currText + mLogText;
        }
        mLogText = mLogText.substring(0, Math.min(mLogText.length(), 1000));
    }

    /**
     * on create service.
     * when phone connect to logcat (Android studio)
     * a service restart every 30 minutes for free memory, so it call
     * onCreate without onDestroy or onStartCommand
     */
    public final void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "IN onCreate");
        addLogText("Restart service", null);
        mEpicBot = new EpicWarBot();
        mBotSettings = readSettingsFromPreferences();
        restartMainTaskAlarm();
        //schedule();
        updateNotification();
    }

    /**
     * read settings from preferences.
     * @return preferences.
     */
    private BotServiceSettings readSettingsFromPreferences() {
        Log.d(LOG_TAG, "IN readSettingsFromPreferences");
        BotServiceSettings bss = new BotServiceSettings();
        SharedPreferences sPref = getSharedPreferences("BotSettings",
                MODE_PRIVATE);
        String decPassword = decryptPassword(sPref.getString("PASSWORD", ""));
        bss.setLoginAndPass(sPref.getString("LOGIN", ""), decPassword);
        bss.setFlagResources(sPref.getBoolean("RESOURCES", false));
        bss.setFlagCemetery(sPref.getBoolean("CEMETERY", false));
        bss.setFlagGifts(sPref.getBoolean("GIFTS", false));
        bss.setInterval(sPref.getInt("INTERVAL", 0));
        boolean vkConnected = sPref.getBoolean("VKCONNECTED", false);
        if (vkConnected) {
            String jsonSessionCookie = sPref.getString(PREF_SESSION_COOKIE, "");
            if(!jsonSessionCookie.isEmpty()) {
                Gson gson = new Gson();
                CookieManager cm = gson.fromJson(jsonSessionCookie, CookieManager.class);
                mEpicBot.setCookies(cm);
            }
        }

        return bss;
    }

    /**
     * save settings to preferences.
     * @param bss bot settings
     */
    private void saveSettingsToPreferences(
            final BotServiceSettings bss) {
        Log.d(LOG_TAG, "IN saveSettingsToPreferences");
        SharedPreferences sPref = getSharedPreferences("BotSettings",
                MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("LOGIN", bss.getVkLogin());
        String encPassword = encryptPassword(bss.getVkPassword());
        ed.putString("PASSWORD", encPassword);
        ed.putBoolean("RESOURCES", bss.getFlagResources());
        ed.putBoolean("CEMETERY", bss.getFlagCemetery());
        ed.putBoolean("GIFTS", bss.getFlagGifts());
        ed.putInt("INTERVAL", bss.getInterval());
        ed.putBoolean("VKCONNECTED", mEpicBot != null && mEpicBot.isVkConnected());
        if(mEpicBot != null && mEpicBot.isVkConnected()) {
            CookieManager cm = mEpicBot.getCookies();
            Gson gson = new Gson();
            String jsonSessionCookieString = gson.toJson(cm);
            ed.putString(PREF_SESSION_COOKIE, jsonSessionCookieString);
        }
        ed.apply();
    }

    /**
     * decrypt password.
     * @param src encrypted password
     * @return decrypted password
     */
    private String decryptPassword(final String src) {
        String resultStr = "";
        try {
            SimpleDESCryptoProvider sDesProv = new SimpleDESCryptoProvider();
            resultStr = sDesProv.decrypt(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultStr;
    }

    /**
     * encrypt password.
     * @param src decrypted password
     * @return encrypted password
     */
    private String encryptPassword(final String src) {
        String resultStr = "";
        try {
            SimpleDESCryptoProvider sDesProv = new SimpleDESCryptoProvider();
            resultStr = sDesProv.encrypt(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultStr;
    }
    /**
     * restart main task alarm.
     */
    public final void restartMainTaskAlarm() {
        Log.d(LOG_TAG, "IN restartMainTaskAlarm");
        if (mBotSettings.getInterval() > 0) {
            Bundle bundle = new Bundle();
            mTaskAlarm = new MainTaskAlarm(this, bundle,
                    mBotSettings.getInterval());
        }
    }
    /**
     * on destroy service.
     */
    @Override
    public final void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "IN onDestroy");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTaskAlarm != null) {
            mTaskAlarm = null;
        }
    }

    /**
     * constructor.
     */
    public MainService() {
    }

    /**
     * stub to bot interface.
     */
    private final IBotService.Stub mBinder = new IBotService.Stub() {
        @Override
        public IBinder asBinder() {
            Log.d(LOG_TAG, "IBotService asBinder");
            return this;
        }

        @Override
        public void addCallback(final IBotServiceCallback listener) {
            synchronized (this) {
                mCallbacks.register(listener);
            }
        }

        @Override
        public void removeCallback(final IBotServiceCallback listener) {
            synchronized (this) {
                mCallbacks.unregister(listener);
            }
        }

        @Override
        public boolean isVkConnected() {
            boolean vkConnected;
            synchronized (this) {
                vkConnected = checkVkConnected();
            }
            return vkConnected;
        }

        @Override
        public void doAllTask() {
            doAllBotTask();
        }

        @Override
        public void initVkConnection() throws RemoteException {
            initializeVkConnection();
        }

        @Override
        public void setServiceSettings(final BotServiceSettings settings) {
            synchronized (this) {
                mBotSettings = settings;
                saveSettingsToPreferences(mBotSettings);
                restartMainTaskAlarm();
            }
        }

        @Override
        public void setCurrServiceCaptcha(String captchaKey) {
            lastCaptchaKey = captchaKey;
        }

        @Override
        public BotServiceSettings getServiceSettings() {
            synchronized (this) {
                return mBotSettings;
            }
        }

        @Override
        public void getLogText() {
            synchronized (this) {
                sendLogToClients(mLogText);
            }
        }

        @Override
        public void restartTaskAlarm() {
            restartMainTaskAlarm();
        }

    };

    /**
     * check is vk connected.
     * @return flag vk connected
     */
    public final boolean checkVkConnected() {
        return mEpicBot != null && mEpicBot.isVkConnected();
    }
    /**
     * chec internet connection.
     * @return true - internet is on
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null;
    }
    /**
     * check license.
     * @return true - license is ok
     */
    private boolean checkLicense() {
        AnswerInfo ai;
        ai = new AnswerInfo("License expired!", "", true,
                "");
        //sendAnswerToClients(ai);
        return true;
    }
    /**
     * init vk connection.
     */
    private void initializeVkConnection() {
        Log.d(LOG_TAG, "IN initializeVkConnection");
//        final int proxyPort = 8888;
//        EpicWarBot.setUseProxy(true);
//        EpicWarBot.setProxy("192.168.0.4", proxyPort);

//        final int proxyPort = 8080;
//        EpicWarBot.setUseProxy(true);
//        EpicWarBot.setProxy("176.31.170.136", proxyPort);
        //EpicWarBot.testConnection();
        AnswerInfo ai;
        if (!isNetworkConnected()) {
            ai = new AnswerInfo("Connect error!", "", true,
                    "There is no internet!");
            sendAnswerToClients(ai);
            return;
        }
        ai = mEpicBot.vkConnect(mBotSettings.getVkLogin(),
                mBotSettings.getVkPassword(), lastCaptchaSid, lastCaptchaKey);
        if(ai.isbError() && ai.getSzInfo().contains("CAPTCHA")) {
            lastCaptchaSid = ai.getRetValue("CAPTCHA");
            requestCaptchaFromUser(lastCaptchaSid);
        }
        else if (ai.isbError()) {
            sendAnswerToClients(ai);
        }
    }
    /**
     * do all bot task.
     */
    private void doAllBotTask() {
        AnswerInfo ai;
        Log.d(LOG_TAG, "IN doAllBotTask");
        mLastDoAllTasks = Calendar.getInstance();
        updateNotification();
        synchronized (this) {
            ai = mEpicBot.gameConnect();
            if (ai.isbError()) {
                sendAnswerToClients(ai);
                Log.d(LOG_TAG, "gameConnect error");
                return;
            }
            if (mBotSettings.getFlagResources()) {
                ai = mEpicBot.collectAllResources();
                if (ai.isbError()) {
                    sendAnswerToClients(ai);
                    return;
                }
            }
            if (mBotSettings.getFlagGifts()) {
                mEpicBot.giftSend();
                ai = mEpicBot.farmAllGifts();
                if (ai.isbError()) {
                    sendAnswerToClients(ai);
                    return;
                }
            }
            if (mBotSettings.getFlagCemetery()) {
                ai = mEpicBot.cemeteryFarm();
                if (ai.isbError()) {
                    sendAnswerToClients(ai);
                    return;
                }
            }
            ai = new AnswerInfo("All task completed!", "", false, "");
            sendAnswerToClients(ai);
        }
    }

    /**
     * send answer to all clients.
     * @param ai answer info
     */
    private void sendAnswerToClients(final AnswerInfo ai) {
        addLogText(ai.getSzInfo(), ai.getError());
        int n = mCallbacks.beginBroadcast();

        for (int i = 0; i < n; i++) {
            try {
                mCallbacks.getBroadcastItem(i).onTaskResult(ai);
            } catch (RemoteException e) {
                Log.d(LOG_TAG, "sendAnswerToClients error: " + e.toString());
            }
        }
        mCallbacks.finishBroadcast();
    }

    /**
     * request captcha from user.
     * @param captchaSid captcha sid
     */
    private void requestCaptchaFromUser(final String captchaSid) {
        int n = mCallbacks.beginBroadcast();

        for (int i = 0; i < n; i++) {
            try {
                mCallbacks.getBroadcastItem(i).onCaptcha(captchaSid);
            } catch (RemoteException e) {
                Log.d(LOG_TAG, "sendAnswerToClients error: " + e.toString());
            }
        }
        mCallbacks.finishBroadcast();
    }
    /**
     * send log text to all clients.
     * @param logText log text
     */
    private void sendLogToClients(final String logText) {
        int n = mCallbacks.beginBroadcast();

        for (int i = 0; i < n; i++) {
            try {
                mCallbacks.getBroadcastItem(i).onGetLog(logText);
            } catch (RemoteException e) {
                Log.d(LOG_TAG, "sendLogToClients error: " + e.toString());
            }
        }
        mCallbacks.finishBroadcast();
    }
    /**
     * describing how to continue the service if it is killed.
     * @param intent The Intent supplied to startService(Intent), as given.
     * @param flags Additional data about this start request.
     *              Currently either 0, START_FLAG_REDELIVERY,
     *              or START_FLAG_RETRY.
     * @param startId A unique integer representing this specific request
     *                to start. Use with stopSelfResult(int).
     * @return  The return value indicates what semantics the system should use
     *          for the service's current started state. It may be one of the
     *          constants associated with the START_CONTINUATION_MASK bits.
     *          May be START_STICKY, START_NOT_STICKY, START_REDELIVER_INTENT,
     *          or START_STICKY_COMPATIBILITY.
     */
    @Override
    public final int onStartCommand(final Intent intent, final int flags,
                                    final int startId) {
        int superRetStatus = super.onStartCommand(intent, flags, startId);
        Log.d(LOG_TAG, "IN onStartCommand " + superRetStatus);
        return superRetStatus;
    }

    /**
     * on bind.
     *
     * @param intent intent to bind
     * @return binder
     */
    @Override
    public final IBinder onBind(final Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return mBinder;
    }

    /**
     * update notification.
     */
    private void updateNotification() {
        Intent mIntent = new Intent(this, MainActivity.class);
        RemoteViews views = new RemoteViews(getPackageName(),
                R.layout.statusbar);
        views.setImageViewResource(R.id.icon,
                R.drawable.ic_launcher);
        views.setTextViewText(R.id.logText, "EpicWarBot");
        if (mLastDoAllTasks == null) {
            views.setTextViewText(R.id.lastUpdate, "Last task: ");
        } else {
            SimpleDateFormat dateStringFormatter =
                    new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String currTime =
                    dateStringFormatter.format(mLastDoAllTasks.getTime());
            views.setTextViewText(R.id.lastUpdate, "Last tasks: " + currTime);
        }
        Notification status = new Notification();
        status.contentView = views;
        status.flags |= Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.ic_launcher;
        status.contentIntent = PendingIntent.getActivity(this, 0,
                mIntent, 0);
        startForeground(PLAYBACK_SERVICE_STATUS, status);
    }
    /**
     * schedule for timer (work only when application is opened).
     * not used (instead int task alarm)
     */
    public final void schedule() {
        Log.d(LOG_TAG, "IN schedule");
        long timeFirstTask = mBotSettings.getDefInterval();
        if (mTimerTask != null) {
            /**
             * last task time (if happened).
             */
            long currLastTaskTime = mTimerTask.scheduledExecutionTime();
            if (currLastTaskTime != 0) {
                Calendar currDate = Calendar.getInstance();
                long currTime = currDate.getTimeInMillis();
                /**
                 * time between curr time and last task time.
                 */
                long currLastTask = currTime - currLastTaskTime;
                Log.d(LOG_TAG, "currLastTask " + currLastTask);
                if (currLastTask > 0 && currLastTask < timeFirstTask) {
                    timeFirstTask = timeFirstTask - currLastTask;
                }
            }
            mTimerTask = null;
        }
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "IN TimerTask");
                addLogText("IN TimerTask", null);
                //doAllBotTask();
            }
        };
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mTimer = new Timer();
        if (mBotSettings.getInterval() > 0) {
            /**
             * schedule(TimerTask task, long when, long period)
             * Schedule a task for repeated fixed-delay
             * execution after a specific delay.
             * task	the task to schedule.
             * when	time of first execution.
             * period	amount of time in milliseconds
             * between subsequent executions.
             */
            Log.d(LOG_TAG, "schedule period " + mBotSettings.getInterval()
                + " first task " + timeFirstTask);
            mTimer.schedule(mTimerTask, timeFirstTask,
                    mBotSettings.getInterval());
        }
    }
}
