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
        mVkLogin = login;
        mVkPassword = pass;
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
     * get vk login.
     * @return vk login
     */
    public final String getVkLogin() {
        return mVkLogin;
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
