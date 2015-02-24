// IBotServiceCallback.aidl
package wivern.com.epicwarbot;
import wivern.com.epicwarbot.AnswerInfo;
//import wivern.com.epicwarbot.IAnswerInfo;
// Declare any non-default types here with import statements

interface IBotServiceCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString);
    oneway void onConnectedResult(in AnswerInfo result);
}
