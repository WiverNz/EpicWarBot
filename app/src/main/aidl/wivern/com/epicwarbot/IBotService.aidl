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
    // add callback listener to service (sync)
    void addCallback(in IBotServiceCallback listener);
    // remove callback listener from service (sync)
    void removeCallback(in IBotServiceCallback listener);
    // do all bot task (async)
    oneway void doAllTask();
    // set game login and pass (sync)
    void setVKLoginAndPass(in String login, in String password);
    // get service variables (when recreate activity) (sync)
    AnswerInfo getServiceVariables();
}
