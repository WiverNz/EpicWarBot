// IBotService.aidl
package wivern.com.epicwarbot;
import wivern.com.epicwarbot.IBotServiceCallback;
import wivern.com.epicwarbot.AnswerInfo;
import wivern.com.epicwarbot.BotServiceSettings;
// Declare any non-default types here with import statements

interface IBotService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean,
    // float aFloat, double aDouble, String aString);

    // add callback listener to service (sync)
    void addCallback(in IBotServiceCallback listener);
    // remove callback listener from service (sync)
    void removeCallback(in IBotServiceCallback listener);
    // do all bot task (async)
    oneway void doAllTask();
    oneway void initVkConnection();
    // set service settings (vk login, pass, timer, flags) (sync)
    void setServiceSettings(in BotServiceSettings settings);
    void setCurrServiceCaptcha(in String captchaKey);
    // get service settings (when recreate activity) (sync)
    BotServiceSettings getServiceSettings();
    // get log text (async).
    oneway void getLogText();
    // restart task alarm (async).
    oneway void restartTaskAlarm();
}
