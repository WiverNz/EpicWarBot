package wivern.com.epicwarbot;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * struct, that contains service settings for bot.
 */
public class BotServiceSettings implements Parcelable {
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
     */
    private final long mDefInterval = 1000;
    /**
     * interval for timer.
     */
    private long mInterval = mDefInterval;
    /**
     * get vk login.
     * @return vk login
     */
    public final String getVkLogin() {
        return mVkLogin;
    }
    /**
     * set vk login.
     * @param login vk login
     */
    public final void setVkLogin(final String login) {
        mVkLogin = login;
    }
    /**
     * get vk password.
     * @return vk password
     */
    public final String getVkPassword() {
        return mVkPassword;
    }
    /**
     * set vk password.
     * @param pass vk password
     */
    public final void setVkPassword(final String pass) {
        mVkPassword = pass;
    }
    /**
     * get flag resources.
     * @return flag collect resources
     */
    public final boolean getFlagResources() {
        return mFlagCollectResources;
    }
    /**
     * set flag resources.
     * @param flag resources
     */
    public final void setFlagResources(final boolean flag) {
        mFlagCollectResources = flag;
    }
    /**
     * get flag gifts.
     * @return flag collect gifts
     */
    public final boolean getFlagGifts() {
        return mFlagSendReceiveGifts;
    }
    /**
     * set flag gifts.
     * @param flag gifts
     */
    public final void setFlagGifts(final boolean flag) {
        mFlagSendReceiveGifts = flag;
    }
    /**
     * get flag cemetery.
     * @return flag collect cemetery
     */
    public final boolean getFlagCemetery() {
        return mFlagCollectCemetery;
    }
    /**
     * set flag cemetery.
     * @param flag cemetery
     */
    public final void setFlagCemetery(final boolean flag) {
        mFlagCollectCemetery = flag;
    }

    /**
     * get timer interval.
     * @return timer interval
     */
    public final long getInterval() {
        return mInterval;
    }

    /**
     * set timer interval.
     * @param interval timer interval
     */
    public final void setInterval(final long interval) {
        mInterval = interval;
    }
    /**
     * default constructor.
     */
    BotServiceSettings() {

    }
    /**
     * set pass and login vk.
     * @param login login
     * @param pass password
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
    }

    /**
     * read info from parcel.
     * @param in parcel
     */
    public final void readFromParcel(final Parcel in) {
        mVkLogin = in.readString();
        mVkPassword = in.readString();
        mFlagCollectResources = intToBool(in.readInt());
        mFlagSendReceiveGifts = intToBool(in.readInt());
    }

    /**
     * boolean to int.
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
     * @param inInt input int
     * @return boolean (true or false)
     */
    private static boolean intToBool(final int inInt) {
        return inInt != 0;
    }
    /**
     * constructor from parcel.
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
