package wivern.com.epicwarbot;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
//import android.os.RemoteException;
/**
 * remo.
 */
import android.os.RemoteException;
import android.util.Log;

import java.util.Calendar;
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
     * on create service.
     */
    public final void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "IN onCreate");
        //final int proxyPort = 8888;
        //EpicWarBot.setUseProxy(true);
        //EpicWarBot.setProxy("192.168.0.4", proxyPort);
        //EpicWarBot.testConnection();
        mEpicBot = new EpicWarBot();
        mBotSettings = new BotServiceSettings();
        schedule();
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
            return null;
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
                schedule();
            }
        }

        @Override
        public BotServiceSettings getServiceSettings() {
            synchronized (this) {
                return mBotSettings;
            }
        }

    };

    /**
     * do all bot task.
     */
    private void doAllBotTask() {
        synchronized (this) {
            Log.d(LOG_TAG, "IN doAllBotTask");
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
        return mBinder;
    }

    /**
     * schedule for timer.
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
                doAllBotTask();
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
