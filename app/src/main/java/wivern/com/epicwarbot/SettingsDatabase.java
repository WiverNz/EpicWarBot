package wivern.com.epicwarbot;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * settings database.
 */
public class SettingsDatabase extends SQLiteOpenHelper {
    /**
     * log tag.
     */
    static final String LOG_TAG = "SettingDB";
    /**
     * database name.
     */
    static final String DB_NAME = "bss";
    /**
     * db version.
     */
    static final int DB_VERSION = 1;
    /**
     * main list of bot settings.
     */
    private List<BotServiceSettings> mBssList = new ArrayList<>();

    /**
     * settings table name.
     */
    static final String STABLE_NAME = "settings";
    /**
     * log table name.
     */
    static final String LOG_TABLE_NAME = "logs";
    /**
     * default constructor.
     * @param context context
     */
    public SettingsDatabase(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public final void onCreate(final SQLiteDatabase db) {
        Log.d(LOG_TAG, "onCreate database");

        db.execSQL("create table " + STABLE_NAME + " ("
                + "id integer primary key autoincrement,"
                + "login text, password text,"
                + "resources integer, cemetery integer,"
                + "gifts integer,"
                + "interval integer, last_update integer,"
                + "active integer);");
        db.execSQL("create table " + LOG_TABLE_NAME + " ("
                + "id integer primary key autoincrement,"
                + "time integer, log text);");
    }

    /**
     * Save all bot settings.
     */
    public final void saveSettings() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (BotServiceSettings bss: mBssList) {

            cv.clear();
            cv.put("_id", 1);
            cv.put("login", bss.getVkLogin());
            cv.put("password", bss.getVkPassword());
            //db.insert("settings", null, cv);
            db.replace(STABLE_NAME, null, cv);
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db,
                          final int oldVersion,
                          final int newVersion) {

    }
}
