package wivern.com.epicwarbot;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.0
 * answer info structure
 */
public class AnswerInfo implements Parcelable {
    /**
     * information.
     */
    private String szInfo;
    /**
     * status.
     */
    private String szStatus;
    /**
     * is error occurred.
     */
    private boolean bError;
    /**
     * error information.
     */
    private String szErrorMsg;
    /**
     * additional return data.
     */
    private HashMap<String, String> hmRetValues;

    /**
     * check error flag.
     * @return error flag
     */
    public final boolean isbError() {
        return bError;
    }
    /**
     * get error message.
     * @return error flag
     */
    public final String getError() {
        return szErrorMsg;
    }
    /**
     * default constructor.
     */
    AnswerInfo() {
        szInfo = "";
        szStatus = "";
        bError = false;
        szErrorMsg = "";
        hmRetValues = new HashMap<>();
    }

    /**
     * get szInfo.
     * @return szInfo
     */
    public final String getSzInfo() {
        return szInfo;
    }
    /**
     * constructor with set values.
     * @param info information string
     * @param status status
     * @param error is error
     * @param errorMsg error message
     */
    AnswerInfo(final String info, final String status, final boolean error,
               final String errorMsg) {
        hmRetValues = new HashMap<>();
        set(info, status, error, errorMsg);
    }

    /**
     * set values.
     * @param info information string
     * @param status status
     * @param error is error
     * @param errorMsg error message
     */
    public final void set(final String info, final String status,
                          final boolean error, final String errorMsg) {
        szInfo = info;
        szStatus = status;
        bError = error;
        szErrorMsg = errorMsg;
    }

    /**
     * add value to additional data.
     * @param key key
     * @param value value
     */
    public final void addValue(final String key, final String value) {
        hmRetValues.put(key, value);
    }

    /**
     * get value from map.
     * @param key key
     * @return value
     */
    public final String getRetValue(final String key) {
        if (hmRetValues.containsKey(key)) {
            return hmRetValues.get(key);
        } else {
            return null;
        }
    }
    /**
     * describe contents.
     * @return 0
     */
    @Override
    public final int describeContents() {
        return 0;
    }

    /**
     * write to parcel.
     * @param dest parcel
     * @param flags flags
     */
    @Override
    public final void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(szInfo);
        dest.writeString(szStatus);
        int intBError = 0;
        if (bError) {
            intBError = 1;
        }
        dest.writeInt(intBError);
        dest.writeString(szErrorMsg);
        dest.writeMap(hmRetValues);
    }

    /**
     * read from parcel.
     * @param in parcel
     */
    public final void readFromParcel(final Parcel in) {
        szInfo = in.readString();
        szStatus = in.readString();
        int intBError = in.readInt();
        bError = intBError != 0;
        szErrorMsg = in.readString();
        if (hmRetValues != null) {
            hmRetValues.clear();
        } else {
            hmRetValues = new HashMap<>();
        }
        HashMap<?, ?> inMap = in.readHashMap(HashMap.class.getClassLoader());
        for (Map.Entry<?, ?> currEntry : inMap.entrySet()) {
            hmRetValues.put(currEntry.getKey().toString(),
                    currEntry.getValue().toString());
        }
    }
    /**
     * constructor from parcel.
     * @param source parcel
     */
    AnswerInfo(final Parcel source) {
        readFromParcel(source);
    }

    /**
     * creator from parcel.
     */
    public static final Parcelable.Creator<AnswerInfo> CREATOR = new
            Parcelable.Creator<AnswerInfo>() {
                @Override
                public AnswerInfo createFromParcel(final Parcel source) {
                    return new AnswerInfo(source);
                }

                @Override
                public AnswerInfo[] newArray(final int size) {
                    return new AnswerInfo[size];
                }
            };
}
