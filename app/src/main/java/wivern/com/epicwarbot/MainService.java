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
     * tag for log.
     */
    private final String mLogTag = "BotService";
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
        Log.d(mLogTag, "MainService onCreate");
        //mTimer = new Timer();
        //schedule();
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
            try {
                listener.onConnectedResult("test result");
            } catch (RemoteException e) {
                Log.d(mLogTag, "onConnectedResult exception: " + e.toString());
            }
        }

        @Override
        public void setVKLoginAndPass(final String login,
                                      final String password) {

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
//                    Log.d(mLogTag, "run");
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
