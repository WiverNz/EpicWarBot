// IBotService.aidl
package wivern.com.epicwarbot;
import wivern.com.epicwarbot.IBotServiceCallback;



// Declare any non-default types here with import statements

interface IBotService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString);
    oneway void connect(in IBotServiceCallback listener);           // connect to game (async)
    void setVKLoginAndPass(in String login, in String password);    // set game login and pass (sync)
    String getVKLoginAndPass();
}
