package wivern.com.epicwarbot;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
     * main bot object.
     */
    private static EpicWarBot mEpicBot;
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
     * add text to log.
     * @param addText text to add
     * @param errorText error text
     */
    public final void addLogText(final String addText, final String errorText) {
        Calendar currDate = Calendar.getInstance();
        SimpleDateFormat dateStringFormatter = new SimpleDateFormat("hh.mm.ss",
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
    }

    /**
     * on create service.
     */
    public final void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "IN onCreate");
        final int proxyPort = 8888;
        //EpicWarBot.setUseProxy(true);
        //EpicWarBot.setProxy("192.168.0.4", proxyPort);
        //EpicWarBot.testConnection();
        mEpicBot = new EpicWarBot();
        mBotSettings = new BotServiceSettings();
        restartMainTaskAlarm();
        //schedule();
    }

    /**
     * restart main task alarm.
     */
    public final void restartMainTaskAlarm() {
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
        public void doAllTask() {
            doAllBotTask();
        }

        @Override
        public void setServiceSettings(final BotServiceSettings settings) {
            synchronized (this) {
                mBotSettings = settings;
                restartMainTaskAlarm();
            }
        }

        @Override
        public BotServiceSettings getServiceSettings() {
            synchronized (this) {
                return mBotSettings;
            }
        }

        @Override
        public String getLogText() {
            return mLogText;
        }

        @Override
        public void restartTaskAlarm() {
            restartMainTaskAlarm();
        }

    };

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
     * do all bot task.
     */
    private void doAllBotTask() {
        synchronized (this) {
            Log.d(LOG_TAG, "IN doAllBotTask");
            if (!isNetworkConnected()) {
                Log.d(LOG_TAG, "There is no internet!");
                addLogText("There is no internet!", null);
            }
            AnswerInfo ai;
            ai = mEpicBot.vkConnect(mBotSettings.getVkLogin(),
                    mBotSettings.getVkPassword());
            sendAnswerToClients(ai);
            if (ai.isbError()) {
                return;
            }
            ai = mEpicBot.gameConnect();
            sendAnswerToClients(ai);
            if (ai.isbError()) {
                Log.d(LOG_TAG, "gameConnect error");
                return;
            }
            if (mBotSettings.getFlagResources()) {
                ai = mEpicBot.collectAllResources();
                sendAnswerToClients(ai);
                if (ai.isbError()) {
                    return;
                }
            }
            if (mBotSettings.getFlagGifts()) {
                mEpicBot.giftSend();
                ai = mEpicBot.farmAllGifts();
                sendAnswerToClients(ai);
                if (ai.isbError()) {
                    return;
                }
            }
            if (mBotSettings.getFlagCemetery()) {
                ai = mEpicBot.cemeteryFarm();
                sendAnswerToClients(ai);
                if (ai.isbError()) {
                    return;
                }
            }
            mEpicBot.vkDisconnect();
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
                Log.d(LOG_TAG, "onTaskResult error: " + e.toString());
            }
        }
        mCallbacks.finishBroadcast();
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
     * schedule for timer (work only when application is opened).
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
