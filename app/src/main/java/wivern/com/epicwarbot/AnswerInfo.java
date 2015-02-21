package wivern.com.epicwarbot;

import java.util.HashMap;

/**
 * @since 1.0
 * answer info structure
 */
public class AnswerInfo {
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
     * default constructor.
     */
    AnswerInfo() {
        szInfo = "";
        szStatus = "";
        bError = false;
        szErrorMsg = "";
        hmRetValues = new HashMap<String, String>();
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
        hmRetValues = new HashMap<String, String>();
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
}
