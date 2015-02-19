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
     * @since 1.0
     * tag for log
     */
    private final String mLogTag = "BotService";
    /**
     * @since 1.0
     * main timer
     */
    private Timer mTimer;
    /**
     * @since 1.0
     * main timer task
     */
    private TimerTask mTimerTask;
    /**
     * @since 1.0
     * default interval for timer
     */
    private final long mDefInterval = 1000;
    /**
     * @since 1.0
     * interval for timer
     */
    private long mInterval = mDefInterval;

    /**
     * @since 1.0
     * on create service
     */
    public final void onCreate() {
        super.onCreate();
        Log.d(mLogTag, "MainService onCreate");
        //mTimer = new Timer();
        //schedule();
    }
    public MainService() {
    }

    public final IBotService.Stub mBinder = new IBotService.Stub() {

        @Override
        public IBinder asBinder() {
            return null;
        }

        @Override
        public void Connect(IBotServiceCallback listener) throws RemoteException {
            listener.OnConnectedResult("test result");
        }

        @Override
        public void SetVKLoginAndPass(String login, String password) throws RemoteException {

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
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
