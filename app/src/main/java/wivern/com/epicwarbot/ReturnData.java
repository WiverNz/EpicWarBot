
package wivern.com.epicwarbot;

import wivern.com.epicwarbot.EpicWarBot.Status;

/**
 * @since 1.0
 * Return data structure
 */
public class ReturnData {
    /**
     * error message.
     */
    private String errorMsg;

    /**
     * get error message.
     *
     * @return errorMsg
     */
    public final String getErrorMsg() {
        return errorMsg;
    }

    /**
     * set error message.
     *
     * @param newErrorMsg new error message
     */
    public final void setErrorMsg(final String newErrorMsg) {
        errorMsg = newErrorMsg;
    }

    /**
     * status.
     */
    private Status status;

    /**
     * get status.
     *
     * @return status
     */
    public final Status getStatus() {
        return status;
    }

    /**
     * set status.
     *
     * @param newStatus new status
     */
    public final void setStatus(final Status newStatus) {
        status = newStatus;
    }

    /**
     * response string.
     */
    private String responseStr;

    /**
     * get response string.
     *
     * @return responseStr
     */
    public final String getResponseStr() {
        return responseStr;
    }

    /**
     * set response string.
     *
     * @param str new response string
     */
    public final void setResponseStr(final String str) {
        responseStr = str;
    }

    /**
     * default constructor.
     */
    ReturnData() {
        errorMsg = "";
        status = Status.NOT_INIT;
        responseStr = "";
    }
}

