package wivern.com.epicwarbot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.Calendar;

/**
 * class for timer tasks.
 */
public class MainTaskAlarm extends BroadcastReceiver {
    /**
     * reminder bundle.
     */
    private static final String REMINDER_BUNDLE = "MainTaskReminderBundle";
    /**
     * tag for log.
     */
    private static final String LOG_TAG = "MainTaskAlarm";
    /**
     * this constructor is called by the alarm manager.
     */
    public MainTaskAlarm() {
    }
    /**
     * you can use this constructor to create the alarm.
     * Just pass in the main activity as the context,
     * any extras you'd like to get later when triggered
     * and the timeout
     *
     * @param context          context
     * @param extras           bundle
     * @param timeoutInSeconds timeout in seconds
     */
    public MainTaskAlarm(final Context context,
                         final Bundle extras, final int timeoutInSeconds) {
        AlarmManager alarmMgr =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MainTaskAlarm.class);
        intent.putExtra(REMINDER_BUNDLE, extras);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.add(Calendar.SECOND, timeoutInSeconds);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                pendingIntent);
    }

    /**
     * @param context context
     * @param intent  intent
     */
    @Override
    public final void onReceive(final Context context,
                                final Intent intent) {
        // here you can get the extras you passed in when creating the alarm
        //intent.getBundleExtra(REMINDER_BUNDLE));
        //addLogText("IN TimerTask", null);
        Log.d(LOG_TAG, "IN onReceive");
        Intent mIntent = new Intent(context, MainService.class);
        mIntent.setAction("service.EpicWarBot");
        IBinder ibinder = peekService(context, mIntent);
        if (ibinder != null) {
            IBotService mServiceApi = IBotService.Stub.asInterface(ibinder);
            try {
                mServiceApi.doAllTask();
                mServiceApi.restartTaskAlarm();
            } catch (RemoteException e) {
                Log.d(LOG_TAG, "onReceive doAllTask RemoteException: "
                + e.toString());
            }
        } else {
            Log.d(LOG_TAG, "IN onReceive binder = null");
        }
    }
}
