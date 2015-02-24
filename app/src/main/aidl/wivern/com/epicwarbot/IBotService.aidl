// IBotService.aidl
package wivern.com.epicwarbot;
import wivern.com.epicwarbot.IBotServiceCallback;
import wivern.com.epicwarbot.AnswerInfo;

// Declare any non-default types here with import statements

interface IBotService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString);
    // connect to game (async)
    oneway void connect(in IBotServiceCallback listener);
    // set game login and pass (sync)
    void setVKLoginAndPass(in String login, in String password);
    // get game login and pass (when recreate activity) (sync)
    AnswerInfo getVKLoginAndPass();
}
