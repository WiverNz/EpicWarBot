package wivern.com.epicwarbot;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * struct, that contains service settings for bot.
 */
public class BotServiceSettings implements Parcelable {
    /**
     * id.
     */
    private int mId;
    /**
     * vk login.
     */
    private String mVkLogin;
    /**
     * vk pass.
     */
    private String mVkPassword;

    /**
     * need to collect resources.
     */
    private boolean mFlagCollectResources;
    /**
     * need to send/receive gifts.
     */
    private boolean mFlagSendReceiveGifts;
    /**
     * need to collect cemetery.
     */
    private boolean mFlagCollectCemetery;
    /**
     * default interval for timer.
     * 1 minute
     */
    private final int mDefInterval = 60;
    /**
     * interval for timer.
     */
    private int mInterval = mDefInterval;
    /**
     * last update.
     */
    private int mLastUpdate;
    /**
     * active (if false - not use for collect).
     */
    private boolean mActive;

    /**
     * get vk login.
     *
     * @return vk login
     */
    public final String getVkLogin() {
        return mVkLogin;
    }

    /**
     * set vk login.
     *
     * @param login vk login
     */
    public final void setVkLogin(final String login) {
        mVkLogin = login;
    }

    /**
     * get vk password.
     *
     * @return vk password
     */
    public final String getVkPassword() {
        return mVkPassword;
    }

    /**
     * set vk password.
     *
     * @param pass vk password
     */
    public final void setVkPassword(final String pass) {
        mVkPassword = pass;
    }

    /**
     * get flag resources.
     *
     * @return flag collect resources
     */
    public final boolean getFlagResources() {
        return mFlagCollectResources;
    }

    /**
     * set flag resources.
     *
     * @param flag resources
     */
    public final void setFlagResources(final boolean flag) {
        mFlagCollectResources = flag;
    }

    /**
     * get flag gifts.
     *
     * @return flag collect gifts
     */
    public final boolean getFlagGifts() {
        return mFlagSendReceiveGifts;
    }

    /**
     * set flag gifts.
     *
     * @param flag gifts
     */
    public final void setFlagGifts(final boolean flag) {
        mFlagSendReceiveGifts = flag;
    }

    /**
     * get flag cemetery.
     *
     * @return flag collect cemetery
     */
    public final boolean getFlagCemetery() {
        return mFlagCollectCemetery;
    }

    /**
     * set flag cemetery.
     *
     * @param flag cemetery
     */
    public final void setFlagCemetery(final boolean flag) {
        mFlagCollectCemetery = flag;
    }

    /**
     * get timer interval.
     *
     * @return timer interval
     */
    public final int getInterval() {
        return mInterval;
    }

    /**
     * set timer interval.
     *
     * @param interval timer interval
     */
    public final void setInterval(final int interval) {
        mInterval = interval;
    }

    /**
     * get default interval.
     *
     * @return default interval
     */
    public final int getDefInterval() {
        return mDefInterval;
    }

    /**
     * get id.
     *
     * @return id
     */
    public final int getID() {
        return mId;
    }

    /**
     * set id.
     *
     * @param id id
     */
    public final void setID(final int id) {
        mId = id;
    }

    /**
     * get last update.
     *
     * @return last update
     */
    public final int getLastUpdate() {
        return mLastUpdate;
    }

    /**
     * set last update.
     *
     * @param lastUpdate last update
     */
    public final void setLastUpdate(final int lastUpdate) {
        mLastUpdate = lastUpdate;
    }

    /**
     * get flag active.
     *
     * @return flag active
     */
    public final boolean getActive() {
        return mActive;
    }

    /**
     * set flag active.
     *
     * @param flag active
     */
    public final void setActive(final boolean flag) {
        mActive = flag;
    }

    /**
     * default constructor.
     */
    BotServiceSettings() {

    }

    /**
     * set pass and login vk.
     *
     * @param login login
     * @param pass  password
     */
    public final void setLoginAndPass(final String login,
                                      final String pass) {
        setVkLogin(login);
        setVkPassword(pass);
    }

    /**
     * on create service.
     */
    @Override
    public final int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(mVkLogin);
        dest.writeString(mVkPassword);
        dest.writeInt(boolToInt(mFlagCollectResources));
        dest.writeInt(boolToInt(mFlagSendReceiveGifts));
        dest.writeInt(boolToInt(mFlagCollectCemetery));
        dest.writeInt(mInterval);
    }

    /**
     * read info from parcel.
     *
     * @param in parcel
     */
    public final void readFromParcel(final Parcel in) {
        mVkLogin = in.readString();
        mVkPassword = in.readString();
        mFlagCollectResources = intToBool(in.readInt());
        mFlagSendReceiveGifts = intToBool(in.readInt());
        mFlagCollectCemetery = intToBool(in.readInt());
        mInterval = in.readInt();
    }

    /**
     * boolean to int.
     *
     * @param inBool input boolean
     * @return int (0 or 1)
     */
    private static int boolToInt(final boolean inBool) {
        if (inBool) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * convert int to boolean.
     *
     * @param inInt input int
     * @return boolean (true or false)
     */
    private static boolean intToBool(final int inInt) {
        return inInt != 0;
    }

    /**
     * constructor from parcel.
     *
     * @param in parcel
     */
    BotServiceSettings(final Parcel in) {
        readFromParcel(in);
    }

    /**
     * creator from parcel.
     */
    public static final Creator<BotServiceSettings> CREATOR
            = new Creator<BotServiceSettings>() {
        @Override
        public BotServiceSettings createFromParcel(final Parcel source) {
            return new BotServiceSettings(source);
        }

        @Override
        public BotServiceSettings[] newArray(final int size) {
            return new BotServiceSettings[size];
        }
    };
}
