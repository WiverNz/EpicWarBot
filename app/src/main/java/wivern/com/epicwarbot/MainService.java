package wivern.com.epicwarbot;
/**
 * Created by askibin on 12.02.2015.
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

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
    private EpicWarBot mEpicBot;
    /**
     * tag for log.
     */
    private static final String LOG_TAG = "BotService";
    /**
     * main timer.
     */
    private Timer mTimer;
    /**
     * main timer task.
     */
    private TimerTask mTimerTask;
    /**
     * default interval for timer.
     */
    private final long mDefInterval = 1000;
    /**
     * interval for timer.
     */
    private long mInterval = mDefInterval;

    /**
     * on create service.
     */
    public final void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "IN onCreate");
        //mTimer = new Timer();
        //schedule();
    }

    /**
     * on destroy service.
     */
    @Override
    public final void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "IN onDestroy");
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
        public void connect(final IBotServiceCallback listener) {
            Log.d(LOG_TAG, "IN connect");
            try {
                listener.onConnectedResult("test result");
            } catch (RemoteException e) {
                Log.d(LOG_TAG, "onConnectedResult exception: " + e.toString());
            }
        }

        @Override
        public void setVKLoginAndPass(final String login,
                                      final String password) {
            mEpicBot.setVkLoginAndPass(login, password);
        }

        @Override
        public String getVKLoginAndPass(){
            return null;
        }
    };

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
     * @since 1.0
     * schedule
     */
//    public final void schedule() {
//        if (mTimerTask != null) {
//            mTimerTask.cancel();
//        }
//        if (mInterval > 0) {
//            mTimerTask = new TimerTask() {
//                public void run() {
//                    Log.d(LOG_TAG, "run");
//                }
//            };
//            mTimer.schedule(mTimerTask, mDefInterval, mInterval);
//        }
//    }
    /**
     * @param gap step
     * @return current interval
     * @since 1.0
     * up the interval
     */
//    public final long upInterval(final long gap) {
//        mInterval = mInterval + gap;
//        schedule();
//        return mInterval;
//    }
}
