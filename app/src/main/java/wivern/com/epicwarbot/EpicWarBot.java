package wivern.com.epicwarbot;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * @since 1.0
 * bot class
 */
public class EpicWarBot {
    /**
     * @since 1.0
     * gift info structure
     */
    public class GiftInfo {
        /**
         * user id.
         */
        private String userId;
        /**
         * array with id of users.
         */
        private List<String> idsArr;

        /**
         * get user id.
         *
         * @return user id
         */
        public final String getUserId() {
            return userId;
        }

        /**
         * get ids array.
         *
         * @return ids array
         */
        public final List<String> getIdsArr() {
            return idsArr;
        }

        /**
         * default constructor.
         *
         * @param uid     user id
         * @param idSLArr array with id of users
         */
        GiftInfo(final String uid, final List<String> idSLArr) {
            userId = uid;
            idsArr = idSLArr;
        }
    }

    /**
     * return status from request.
     */
    public enum Status {
        /**
         * not init.
         */
        NOT_INIT,
        /**
         * ok.
         */
        SUCCESS,
        /**
         * error.
         */
        ERROR
    }

    /**
     * vk id.
     */
    private String mVkId;
    /**
     * epic war main site for json.
     */
    private final String mSite = "http://epicwar-vkontakte.progrestar.net/rpc/";
    /**
     * http header for json.
     */
    private HashMap<String, String> mSessionHeaderX;
    /**
     * request id.
     */
    private int mRequestId;
    /**
     * authentication key (flash vars param).
     */
    private String mAuthKey;
    /**
     * application id.
     */
    private String mAppId;
    /**
     * sid (use for first data).
     */
    private String mSid;
    /**
     * secret from flash vars for sig.
     */
    private String mSecret;
    /**
     * list of friends, who sent gifts.
     */
    private List<GiftInfo> mFriendGifts;
    /**
     * list of friends, to who we can send the gift.
     */
    private List<String> mFriendSendGifts;
    /**
     * list of friends, to who we already sent the gift.
     */
    private List<String> mFriendAlreadySendGifts;

    /**
     * castle building id.
     */
    private static final int CASTLE_ID = 1;
    /**
     * mine building id.
     */
    private static final int MINE_ID = 2;
    /**
     * treasury building id.
     */
    private static final int TREASURY_ID = 3;
    /**
     * mill building id.
     */
    private static final int MILL_ID = 4;
    /**
     * barn building id.
     */
    private static final int BARN_ID = 5;
    /**
     * barrack building id.
     */
    private static final int BARRACKS_ID = 6;
    /**
     * staff building id.
     */
    private static final int STAFF_ID = 7;
    /**
     * builder hut building id.
     */
    private static final int BUILDER_HUT_ID = 8;
    /**
     * forge building id.
     */
    private static final int FORGE_ID = 9;
    /**
     * ballista building id.
     */
    private static final int BALLISTA_ID = 10;
    /**
     * wall building id.
     */
    private static final int WALL_ID = 11;
    /**
     * archer tower building id.
     */
    private static final int ARCHER_TOWER_ID = 12;
    /**
     * cannon building id.
     */
    private static final int CANNON_ID = 13;
    /**
     * thunder tower building id.
     */
    private static final int THUNDER_TOWER_ID = 14;
    /**
     * ice tower building id.
     */
    private static final int ICE_TOWER_ID = 15;
    /**
     * fire tower building id.
     */
    private static final int FIRE_TOWER_ID = 16;
    /**
     * clan house building id.
     */
    private static final int CLAN_HOUSE_ID = 17;
    /**
     * dark tower building id.
     */
    private static final int DARK_TOWER_ID = 18;
    /**
     * tavern building id.
     */
    private static final int TAVERN_ID = 19;
    /**
     * alchemist building id.
     */
    private static final int ALCHEMIST_ID = 20;
    /**
     * sand building id.
     */
    private static final int SAND_ID = 31;
    /**
     * gold resource id.
     */
    private static final int GOLD_RESOURCE_ID = 1;
    /**
     * food resource id.
     */
    private static final int FOOD_RESOURCE_ID = 2;
    /**
     * capacities of barn.
     */
    private static final int[] CAPACITIES = {0, 5000, 15000, 35000, 75000,
            150000, 300000, 600000, 1000000, 2000000, 3000000, 4000000};
    /**
     * set cookie string in http header query.
     */
    private static final String COOKIES_HEADER = "Set-Cookie";
    /**
     * list of gold mine.
     */
    private List<Integer> mArrayGoldMine;
    /**
     * list of mill mine.
     */
    private List<Integer> mArrayMillMine;
    /**
     * list of sand mine.
     */
    private List<Integer> mArraySandMine;
    /**
     * need to collect cemetery (someone attacked us).
     */
    private boolean mCemetery;
    /**
     * log tag.
     */
    private static final String LOG_TAG = "BotClass";
    /**
     * cookie manager for http.
     */
    private CookieManager mCookieManager;

    /**
     * is vk connected.
     */
    private boolean mVkConnected;
    /**
     * is game connected.
     */
    private boolean mGameConnected;
    /**
     * use proxy for http.
     */
    private static boolean mUseProxy = false;
    /**
     * proxy settings.
     */
    private static Proxy mProxy = null;

    /**
     * use auto redirect.
     */
    private static boolean mAutoRedirect = false;
    /**
     * default connection timeout.
     */
    private static final int DEFAULT_CONNECTION_TIMEOUT = 1000;
    /**
     * default read timeout.
     */
    private static final int DEFAULT_READ_TIMEOUT = 15000;
    /**
     * connection timeout.
     */
    private static int mConnectTimeout = DEFAULT_CONNECTION_TIMEOUT;
    /**
     * read timeout.
     */
    private static int mReadTimeout = DEFAULT_READ_TIMEOUT;
    /**
     * default constructor.
     */
    EpicWarBot() {
        init(true);
    }

    /**
     * clear all values.
     *
     * @param constructor if true - create values
     */
    private void init(final boolean constructor) {
        mVkId = "";
        mRequestId = 1;
        mAuthKey = "";
        mAppId = "";
        mSid = "";
        mSecret = "";
        mCemetery = false;
        mVkConnected = false;
        mGameConnected = false;
        if (constructor) {
            mSessionHeaderX = new HashMap<>();
            mArrayGoldMine = new ArrayList<>();
            mArrayMillMine = new ArrayList<>();
            mArraySandMine = new ArrayList<>();
            mFriendGifts = new ArrayList<>();
            mFriendSendGifts = new ArrayList<>();
            mFriendAlreadySendGifts = new ArrayList<>();
            mCookieManager = new java.net.CookieManager();
        } else {
            mSessionHeaderX.clear();
            mArrayGoldMine.clear();
            mArrayMillMine.clear();
            mArraySandMine.clear();
            mFriendGifts.clear();
            mFriendSendGifts.clear();
            mFriendAlreadySendGifts.clear();
            mCookieManager.getCookieStore().removeAll();
        }
    }

    /**
     * set use proxy for http.
     *
     * @param useProxy is use the proxy
     */
    public static void setUseProxy(final boolean useProxy) {
        mUseProxy = useProxy;
        if (mUseProxy) {
            System.setProperty("java.net.useSystemProxies", "false");
        } else {
            System.setProperty("java.net.useSystemProxies", "true");
        }
    }

    /**
     * set proxy settings.
     *
     * @param ip   ip
     * @param port port
     */
    public static void setProxy(final String ip, final int port) {
        mProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
        Log.d(LOG_TAG, "mProxy " + mProxy);
    }
    /**
     * connect to vk.
     *
     * @param vkLogin    login
     * @param vkPassword password
     * @return answer info structure
     */
    public final AnswerInfo vkConnect(final String vkLogin,
                                      final String vkPassword) {
        AnswerInfo retResult = new AnswerInfo();
        init(false);
        Log.d(LOG_TAG, "vkConnect: " + vkLogin + " " + vkPassword);
        final String urlPath = "http://login.vk.com/";
        HashMap<String, Object> cSendData = new HashMap<>();

        cSendData.put("email", vkLogin);
        cSendData.put("pass", vkPassword);
        cSendData.put("act", "login");
        cSendData.put("amp;to", "&amp;");

        ReturnData retDict = getPost(urlPath, "GET", cSendData, null,
                mCookieManager, false, false);
        if (retDict.getStatus() == Status.SUCCESS) {
            HashMap<String, String> vkPairs = findPairsInText(
                    retDict.getResponseStr(), "var vk\\s*=\\s*\\{(.*?)\\}",
                    "[,\\s\\n\\r]*([^:]*):\\s*([^\\n^\\r^,]*)");
            if (vkPairs.containsKey("id")) {
                mVkId = vkPairs.get("id");
                if (!mVkId.equals("0")) {
                    mVkConnected = true;
                    retResult.set("VK Connected! vk id: " + mVkId,
                            retDict.getStatus().toString(),
                            false, "");
                } else {
                    retResult.set("Not connected!",
                            retDict.getStatus().toString(),
                            true, "Authorization problem: vkId == 0");
                }
            } else {
                retResult.set("Not connected!", retDict.getStatus().toString(),
                        true, "Authorization problem: var vk id not found!");
            }
        } else {
            Log.d(LOG_TAG, retDict.getErrorMsg());
            retResult.set("Not connected!", retDict.getStatus().toString(),
                    true, "retDict.getStatus() != Status.SUCCESS");
        }
        return retResult;
    }

    /**
     * disconnect from vk.
     *
     * @return answer info structure
     */
    public final AnswerInfo vkDisconnect() {
        Log.d(LOG_TAG, "vkDisconnect");
        AnswerInfo retResult = new AnswerInfo();
        init(false);
        final String urlPath = "http://vk.com/login.php";
        HashMap<String, Object> cSendData = new HashMap<>();

        cSendData.put("op", "logout");

        ReturnData retDict = getPost(urlPath, "GET", cSendData, null,
                mCookieManager, false, false);
        if (retDict.getStatus() == Status.SUCCESS) {
            retResult.set("Disconnected!",
                    retDict.getStatus().toString(), false, "");
        } else {
            Log.d(LOG_TAG, retDict.getErrorMsg());
            retResult.set("Not disconnected!",
                    retDict.getStatus().toString(), true,
                    "retDict.getStatus() != Status.SUCCESS");
        }
        init(false);

        return retResult;
    }

    /**
     * connect to game.
     *
     * @return answer info structure
     */
    public final AnswerInfo gameConnect() {
        final int refId = 9;
        final int rndVer = 32546;
        final int al = -1;
        Log.d(LOG_TAG, "GameConnect");
        AnswerInfo retResult = new AnswerInfo();
        if (!mVkConnected) {
            retResult.set("Not connected!", "", true,
                    "Vkontakte not connected!");
            return retResult;
        }

        String urlPath = "http://vk.com/al_profile.php";
        HashMap<String, Object> cSendData = new HashMap<>();
        HashMap<String, String> cSendHeaders = new HashMap<>();

        cSendData.put("__query", "clashofthrones");
        cSendData.put("_ref", "apps");
        cSendData.put("_tstat", "353%2C110%2C291%2C359%2Capps");
        cSendData.put("al", al);
        cSendData.put("al_id", mVkId);
        cSendData.put("mid", mVkId);
        cSendData.put("ref", refId);
        cSendData.put("_rndVer", rndVer);

        ReturnData retDict = getPost(urlPath, "GET", cSendData, null,
                mCookieManager, false, false);
        if (retDict.getStatus() == Status.SUCCESS) {
            String patternPair = "[,\\s\\n\\r]*\\\\\"([^:^\\\\]*"
                    + ")\\\\\":\\s*[\\\\\"]*(.*?)(\\\\\"|,|$)";
            HashMap<String, String> paramPairs = findPairsInText(
                    retDict.getResponseStr(), "var params\\s*=\\s*\\{(.*?)\\}",
                    patternPair);
            HashMap<String, String> optionPairs = findPairsInText(
                    retDict.getResponseStr(), "var options\\s*=\\s*\\{(.*?)\\}",
                    patternPair);
            if (optionPairs.containsKey("src")) {
                String iUrl = optionPairs.get("src");
                iUrl = iUrl.replace("\\\\\\", "");
                cSendData.clear();
                for (Entry<String, String> cItem : paramPairs.entrySet()) {
                    String cKey = cItem.getKey();
                    String cVal = cItem.getValue();
                    if (cKey.equals("api_url")) {
                        cVal = cVal.replace("\\\\\\", "");
                    }
                    cSendData.put(cKey, cVal);
                }
                mSid = (String) cSendData.get("sid");
                retDict = getPost(iUrl, "GET", cSendData, cSendHeaders,
                        mCookieManager, false, false);
                if (retDict.getStatus() == Status.SUCCESS) {
                    HashMap<String, String> paramFlash = findPairsInText(
                            retDict.getResponseStr(),
                            "params.flashvars\\s*=[\\s\\n\\r]*\"(.*?)\"",
                            "(.*?)=(.*?)(&|$)");
                    if (paramFlash.containsKey("auth_key")) {
                        mRequestId = 1;
                        mAuthKey = paramFlash.get("auth_key");
                        mAppId = optionPairs.get("aid");
                        mSecret = paramFlash.get("secret");
                        mSessionHeaderX.put("X-Auth-Token", mAuthKey);
                        mSessionHeaderX.put("X-Auth-Session-Id",
                                generateSessionKey());
                        mSessionHeaderX.put("X-Server-Time", getCurrTimeStr());
                        mSessionHeaderX.put("X-Auth-Application-Id", mAppId);
                        mSessionHeaderX.put("X-Auth-User-Id", mVkId);
                        mSessionHeaderX.put("X-Auth-Network-Ident",
                                "vkontakte");
                        mSessionHeaderX.put("X-Env-Library-Version", "0");
                        mSessionHeaderX
                                .put("X-Request-With", "XMLHttpRequest");
                        // mSessionHeaderX.put("X-Auth-Signature",
                        // createAuthSignature(post));
                        retResult = sendReceiveFirstData();
                    }
                }
            }

        }
        return retResult;
    }

    /**
     * get information about vk id (user name, last name, photo url).
     * and information about friends
     *
     * @param friendsArray    friends list
     * @param appFriendsArray application friends list
     * @return map with information
     */
    private HashMap<String, Object> getApiVkAboutUsers(
            final List<String> friendsArray,
            final List<String> appFriendsArray) {
        Log.d(LOG_TAG, "IN getApiVkAboutUsers");
        String errorMessage = "";
        HashMap<String, Object> retApiUsers = new HashMap<>();

        HashMap<String, Object> formSendData = new HashMap<>();
        String cCode = "return{\"user\":API.getProfiles({\"https\":0,\"uids\":"
                + mVkId
                + ",\"fields\":\"can_post,uid,first_name,"
                + "last_name,nickname,sex,"
                + "bdate,photo,photo_medium,photo_big,"
                + "has_mobile,rate,city,country,"
                + "photo_max_orig\"}),\"friends\":"
                + "API.friends.get({\"https\":0,\"count\" "
                + ": 500, \"fields\":\"uid,country,first_name,"
                + "last_name,photo,photo_medium,"
                + "photo_big,sex,can_post,bdate,online,"
                + "photo_max_orig\"}),\"appFriends\":"
                + "API.getAppFriends(),\"groups\":API.getGroups()};";
        formSendData.put("api_id", mAppId);
        formSendData.put("code", cCode);
        formSendData.put("format", "json");
        formSendData.put("https", "0");
        formSendData.put("method", "execute");
        formSendData.put("rnd", getRnd());
        formSendData.put("sid", mSid);

        formSendData.put("v", "3.0");

        formSendData.put("sig", createSig(mVkId, formSendData, mSecret));
        String cSite = "http://vk.com/api.php";
        ReturnData retDictForm = getPost(cSite, "POST", formSendData, null,
                mCookieManager, false, true);

        String vkBirthday = "";
        int vkCity = 0;
        String vkFirstName = "";
        String vkPhotoUrl = "";
        String vkLastName = "";

        if (retDictForm.getStatus() == Status.SUCCESS) {
            JSONObject jResponse;
            try {
                JSONObject retDictFormJson = new JSONObject(new JSONTokener(
                        retDictForm.getResponseStr()));
                jResponse = retDictFormJson.getJSONObject("response");
                JSONArray jUser = jResponse.getJSONArray("user");
                if (jUser.length() > 0) {
                    JSONObject userData;
                    userData = jUser.getJSONObject(0);
                    try {
                        vkFirstName = userData.getString("first_name");
                        vkLastName = userData.getString("last_name");
                        vkPhotoUrl = userData.getString("photo_medium");
                        vkBirthday = userData.getString("bdate");
                        vkBirthday = changeDateFormat(vkBirthday, "dd.M.yyyy",
                                "yyyy-M-dd");
                        vkCity = userData.getInt("city");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                JSONArray jFriends = jResponse.getJSONArray("friends");
                for (int i = 0; i < jFriends.length(); i++) {
                    JSONObject cjFriendI = jFriends.getJSONObject(i);
                    long cjFriendId = cjFriendI.getLong("uid");
                    friendsArray.add(String.valueOf(cjFriendId));
                }
                JSONArray jAppFriends = jResponse.getJSONArray("appFriends");
                for (int i = 0; i < jAppFriends.length(); i++) {
                    long cjAppFriend = (Long) jAppFriends.get(i);

                    appFriendsArray.add(String.valueOf(cjAppFriend));
                }
            } catch (JSONException e) {
                errorMessage = "Not connected!" + e.toString()
                        + "sendRecvFirstData parse json error";
                Log.d(LOG_TAG, "getApiVkAboutUsers " + errorMessage);
            }
        }
        retApiUsers.put("friendsArray", friendsArray);
        retApiUsers.put("appFriendsArray", appFriendsArray);
        retApiUsers.put("vkbirthday", vkBirthday);
        retApiUsers.put("vkcity", vkCity);
        retApiUsers.put("vkfirstName", vkFirstName);
        retApiUsers.put("vkphotoUrl", vkPhotoUrl);
        retApiUsers.put("vklastName", vkLastName);
        retApiUsers.put("errorMessage", errorMessage);

        return retApiUsers;
    }

    /**
     * send first data to game.
     *
     * @return return data from html
     */
    private ReturnData sendFirstData() {
        Log.d(LOG_TAG, "IN sendFirstData");
        ReturnData retDict = new ReturnData();

        List<String> friendsArray = new ArrayList<>();
        List<String> appFriendsArray = new ArrayList<>();

        HashMap<String, Object> retApiUsers = getApiVkAboutUsers(friendsArray,
                appFriendsArray);
        String vkBirthday = retApiUsers.get("vkbirthday").toString();
        int vkCity = (int) retApiUsers.get("vkcity");
        String vkFirstName = retApiUsers.get("vkfirstName").toString();
        String vkPhotoUrl = retApiUsers.get("vkphotoUrl").toString();
        String vkLastName = retApiUsers.get("vklastName").toString();
        String errorMessage = retApiUsers.get("errorMessage").toString();

        if (vkFirstName.equals("")) {
            retDict.setErrorMsg(errorMessage);
            return retDict;
        }
        HashMap<String, Object> jReferrer = new HashMap<>();
        jReferrer.put("type", toJsonString("user_apps"));
        jReferrer.put("id", 0);
        HashMap<String, Object> jUser = new HashMap<>();
        jUser.put("country", toJsonString("function Function() []"));
        jUser.put("birthday", toJsonString(vkBirthday));
        jUser.put("sex", toJsonString("male"));
        jUser.put("locale", toJsonString("en"));
        jUser.put("id", toJsonString(mVkId));
        jUser.put("city", vkCity);
        jUser.put("firstName", toJsonString(vkFirstName));
        jUser.put("photoUrl", toJsonString(vkPhotoUrl));
        jUser.put("lastName", toJsonString(vkLastName));
        jUser.put("referrer", jReferrer);
        HashMap<String, Object> jArgsRegistration =
                new HashMap<>();
        jArgsRegistration.put("friendIds", friendsArray);
        jArgsRegistration.put("user", jUser);
        HashMap<String, Object> jArgsCheckRegisteredUsers =
                new HashMap<>();
        jArgsCheckRegisteredUsers.put("users", appFriendsArray);
        HashMap<String, Object> jArgsInvitationGetUsers =
                new HashMap<>();
        jArgsInvitationGetUsers.put("ids", friendsArray);
        HashMap<String, Object> jArgsEmpty =
                new HashMap<>();
        List<Object> jCalls = new ArrayList<>();
        jCalls.add(genCall("registration", jArgsRegistration, null));
        jCalls.add(genCall("boostGetAll", jArgsEmpty, null));
        jCalls.add(genCall("artefactGetList", jArgsEmpty, null));
        jCalls.add(genCall("battleStatisticGet", jArgsEmpty, null));
        jCalls.add(genCall("getTime", jArgsEmpty, null));
        jCalls.add(genCall("getSelfInfo", jArgsEmpty, null));
        jCalls.add(genCall("getDynamicParams", jArgsEmpty, null));
        jCalls.add(genCall("getArmyQueue", jArgsEmpty, null));
        jCalls.add(genCall("getBuildings", jArgsEmpty, null));
        jCalls.add(genCall("heroesGetList", jArgsEmpty, null));
        jCalls.add(genCall("getResearchQueue", jArgsEmpty, null));
        jCalls.add(genCall("getMissions", jArgsEmpty, null));
        jCalls.add(genCall("getQuests", jArgsEmpty, null));
        jCalls.add(genCall("getProtections", jArgsEmpty, null));
        jCalls.add(genCall("getInvitedBy", jArgsEmpty, null));
        jCalls.add(genCall("getInvitedUsers", jArgsEmpty, null));
        jCalls.add(genCall("getBonusCrystals", jArgsEmpty, null));
        jCalls.add(genCall("getSettings", jArgsEmpty, null));
        jCalls.add(genCall("promoGetHalfBilling", jArgsEmpty, null));
        jCalls.add(genCall("giftGetAvailable", jArgsEmpty, null));
        jCalls.add(genCall("giftGetReceivers", jArgsEmpty, null));
        jCalls.add(genCall("cloverGetAll", jArgsEmpty, null));
        jCalls.add(genCall("paymentsCount", jArgsEmpty, null));
        jCalls.add(genCall("cemeteryGet", jArgsEmpty, null));
        jCalls.add(genCall("getNotices", jArgsEmpty, null));
        jCalls.add(genCall("allianceGetMessages", jArgsEmpty, null));
        jCalls.add(genCall("getGlobalNews", jArgsEmpty, null));
        jCalls.add(genCall("battleGetActive", jArgsEmpty, null));
        jCalls.add(genCall("spellList", jArgsEmpty, null));
        jCalls.add(genCall("spellProductionQueue", jArgsEmpty, null));
        jCalls.add(genCall("checkRegisteredUsers", jArgsCheckRegisteredUsers,
                null));
        jCalls.add(genCall("invitationGetUsers", jArgsInvitationGetUsers,
                null));
        jCalls.add(genCall("state", jArgsEmpty, null));

        HashMap<String, Object> jsonData = new HashMap<>();
        jsonData.put("session", null);
        jsonData.put("calls", jCalls);

        retDict = sendReceive(jsonData);

        return retDict;
    }

    /**
     * send and analyse first data from game.
     *
     * @return answer info
     */
    private AnswerInfo sendReceiveFirstData() {
        Log.d(LOG_TAG, "IN sendReceiveFirstData");
        AnswerInfo retResult = new AnswerInfo();
        ReturnData retDict = sendFirstData();
        if (retDict.getStatus() == Status.SUCCESS) {
            try {
                JSONObject retDictFormJson = new JSONObject(new JSONTokener(
                        retDict.getResponseStr()));
                JSONArray jResults = retDictFormJson.getJSONArray("results");
                for (int i = 0; i < jResults.length(); i++) {
                    JSONObject fbObj = jResults.getJSONObject(i);
                    String fIdentify = fbObj.getString("ident");
                    if (fIdentify.contentEquals("giftGetAvailable")) {
                        JSONObject fGiftRes = fbObj.getJSONObject("result");
                        JSONArray fGiftArr = fGiftRes.getJSONArray("gift");
                        for (int j = 0; j < fGiftArr.length(); j++) {
                            JSONObject cGiftInfo = fGiftArr.getJSONObject(j);
                            String cGiftIDS = cGiftInfo.getString("id");
                            JSONObject cGiftBody = cGiftInfo
                                    .getJSONObject("body");
                            JSONObject cGiftUserInfo = cGiftBody
                                    .getJSONObject("userInfo");
                            String cGiftUserId = cGiftUserInfo.getString("id");
                            List<String> cArrayUserIds =
                                    new ArrayList<>();
                            cArrayUserIds.add(cGiftIDS);
                            GiftInfo gi = new GiftInfo(cGiftUserId,
                                    cArrayUserIds);
                            mFriendGifts.add(gi);
                        }
                    } else if (fIdentify.contentEquals("getBuildings")) {
                        JSONObject fBuildRes = fbObj.getJSONObject("result");
                        JSONArray fBuildArray = fBuildRes
                                .getJSONArray("building");
                        for (int j = 0; j < fBuildArray.length(); j++) {
                            JSONObject fcBuildInfo = fBuildArray
                                    .getJSONObject(j);
                            int cTypeOfBuild = fcBuildInfo.getInt("typeId");
                            int cIdOfBuild = fcBuildInfo.getInt("id");
                            boolean cCompleted =
                                    fcBuildInfo.getBoolean("completed");
                            if (cCompleted) {
                                if (cTypeOfBuild == MILL_ID) {
                                    mArrayMillMine.add(cIdOfBuild);
                                } else if (cTypeOfBuild == MINE_ID) {
                                    mArrayGoldMine.add(cIdOfBuild);
                                } else if (cTypeOfBuild == SAND_ID) {
                                    mArraySandMine.add(cIdOfBuild);
                                }
                            }
                        }
                    } else if (fIdentify.
                            contentEquals("checkRegisteredUsers")) {
                        JSONObject fUsersRes = fbObj.getJSONObject("result");
                        JSONArray fUsersArray = fUsersRes
                                .getJSONArray("result");
                        for (int j = 0; j < fUsersArray.length(); j++) {
                            String cUser = fUsersArray.getString(j);
                            mFriendSendGifts.add(cUser);
                        }
                    } else if (fIdentify.contentEquals("giftGetReceivers")) {
                        JSONObject fUsersRes = fbObj.getJSONObject("result");
                        JSONArray fUsersArray = fUsersRes
                                .getJSONArray("receivers");
                        for (int j = 0; j < fUsersArray.length(); j++) {
                            JSONObject cUserObj = fUsersArray.getJSONObject(j);
                            String cUserId = cUserObj.getString("toUserId");
                            mFriendAlreadySendGifts.add(cUserId);
                        }
                    } else if (fIdentify.contentEquals("cemeteryGet")) {
                        JSONObject fCemeteryRes = fbObj.getJSONObject("result");
                        JSONArray fCemeteryArray = fCemeteryRes
                                .getJSONArray("result");
                        if (fCemeteryArray.length() > 0) {
                            mCemetery = true;
                        }
                    }
                }
                mGameConnected = true;
                retResult.set("Game connected!", "", false, "");
            } catch (JSONException e) {
                retResult.set("Not connected!", e.toString(), true,
                        "retDictFormJson parse error "
                                + retDict.getResponseStr());
                Log.d(LOG_TAG, "sendReceiveFirstData error parse: "
                        + retDict.getResponseStr());
            }

        } else {
            retResult.set("Not connected!", "", true,
                    "sendReceiveFirstData return null data: "
                            + retDict.getErrorMsg());
        }

        return retResult;
    }

    /**
     * cemetery farm.
     *
     * @return answer info
     */
    public final AnswerInfo cemeteryFarm() {
        AnswerInfo retResult = new AnswerInfo();

        if (mCemetery) {
            HashMap<String, Object> jArgsEmpty = new HashMap<>();
            List<Object> jCalls = new ArrayList<>();
            jCalls.add(genCall("group_0_body", jArgsEmpty, "cemeteryFarm"));
            jCalls.add(genCall("group_1_body", jArgsEmpty, "state"));
            HashMap<String, Object> jsonData = new HashMap<>();
            jsonData.put("session", null);
            jsonData.put("calls", jCalls);

            ReturnData retDict = sendReceive(jsonData);
            if (retDict.getStatus() == Status.SUCCESS) {
                retResult.set("Cemetery farm ok!", "", false, "");
            }

            mCemetery = false;
        }
        return retResult;
    }

    /**
     * send gift.
     *
     * @return answer info
     */
    public final AnswerInfo giftSend() {
        AnswerInfo retResult = new AnswerInfo();
        List<String> friendNeedSendGifts = new ArrayList<>();
        for (String cUserSend : mFriendSendGifts) {
            boolean needSend = true;
            for (String cUserAlreadySend : mFriendAlreadySendGifts) {
                if (cUserSend.contentEquals(cUserAlreadySend)) {
                    needSend = false;
                    break;
                }
            }
            if (needSend) {
                friendNeedSendGifts.add(cUserSend);
            }
        }
        if (friendNeedSendGifts.size() > 0) {
            HashMap<String, Object> jArgsEmpty = new HashMap<>();
            HashMap<String, Object> jArgsUsers = new HashMap<>();
            jArgsUsers.put("users", friendNeedSendGifts);
            HashMap<String, Object> jArgsUsersEmpty = new HashMap<>();
            jArgsUsersEmpty.put("users", new ArrayList<>());
            List<Object> jCalls = new ArrayList<>();
            jCalls.add(genCall("group_0_body", jArgsUsers, "giftSend"));
            jCalls.add(genCall("group_1_body", jArgsUsersEmpty,
                    "getUsersInfo"));
            jCalls.add(genCall("group_2_body", jArgsEmpty, "state"));
            HashMap<String, Object> jsonData = new HashMap<>();
            jsonData.put("session", null);
            jsonData.put("calls", jCalls);

            ReturnData retDict = sendReceive(jsonData);
            if (retDict.getStatus() == Status.SUCCESS) {
                retResult.set("Gift send ok!", "", false, "");
            }
        }

        return retResult;
    }

    /**
     * gift farm from friend.
     *
     * @param giftInfo gift info
     * @return true - farmed
     */
    public final boolean giftFarm(final GiftInfo giftInfo) {
        boolean farmed = false;
        HashMap<String, Object> jArgsEmpty = new HashMap<>();
        HashMap<String, Object> jArgsUserId = new HashMap<>();
        jArgsUserId.put("userId", giftInfo.getUserId());
        HashMap<String, Object> jArgsIds = new HashMap<>();
        jArgsIds.put("ids", giftInfo.getIdsArr());
        List<Object> jCalls = new ArrayList<>();
        jCalls.add(genCall("group_0_body", jArgsUserId, "giftFarm"));
        jCalls.add(genCall("group_1_body", jArgsIds, "removeNotices"));
        jCalls.add(genCall("group_2_body", jArgsEmpty, "giftGetAvailable"));
        jCalls.add(genCall("group_3_body", jArgsEmpty, "state"));
        HashMap<String, Object> jsonData = new HashMap<>();
        jsonData.put("session", null);
        jsonData.put("calls", jCalls);

        ReturnData retDict = sendReceive(jsonData);
        if (retDict.getStatus() == Status.SUCCESS) {
            farmed = true;
        }

        return farmed;
    }

    /**
     * farm all gift from friends.
     *
     * @return answer info structure
     */
    public final AnswerInfo farmAllGifts() {
        AnswerInfo retResult = new AnswerInfo();
        boolean allFarmed = true;
        for (GiftInfo currGiftInfo : mFriendGifts) {
            if (!giftFarm(currGiftInfo)) {
                allFarmed = false;
                break;
            }
        }
        if (allFarmed) {
            retResult.set("All farmed!", "", false, "");
        } else {
            retResult.set("Error farmed!", "", true, "");
        }

        return retResult;
    }

    /**
     * collect resource from building.
     *
     * @param id of building
     * @return true - collected
     */
    public final boolean collectResourceFromBuilding(final int id) {
        boolean collected = false;
        HashMap<String, Object> jArgsEmpty = new HashMap<>();
        HashMap<String, Object> jArgsBuildingId = new HashMap<>();
        jArgsBuildingId.put("buildingId", id);
        List<Object> jCalls = new ArrayList<>();
        jCalls.add(genCall("group_0_body", jArgsBuildingId, "collectResource"));
        jCalls.add(genCall("group_1_body", jArgsEmpty, "state"));
        HashMap<String, Object> jsonData = new HashMap<>();
        jsonData.put("session", null);
        jsonData.put("calls", jCalls);

        ReturnData retDict = sendReceive(jsonData);
        if (retDict.getStatus() == Status.SUCCESS) {
            collected = true;
        }

        return collected;
    }

    /**
     * collect all resources.
     *
     * @return answer info structure
     */
    public final AnswerInfo collectAllResources() {
        Log.d(LOG_TAG, "IN collectAllResources");
        AnswerInfo retResult = new AnswerInfo();
        boolean collected = true;

        for (int cId : mArrayMillMine) {
            if (!collectResourceFromBuilding(cId)) {
                collected = false;
                break;
            }
        }
        for (int cId : mArrayGoldMine) {
            if (!collectResourceFromBuilding(cId)) {
                collected = false;
                break;
            }
        }
        for (int cId : mArraySandMine) {
            if (!collectResourceFromBuilding(cId)) {
                collected = false;
                break;
            }
        }

        if (collected) {
            retResult.set("All resources collected!", "", false, "");
        } else {
            retResult.set("Error resources collected!", "", true, "");
        }

        return retResult;
    }

    /**
     * test connection.
     */
    public static void testConnection() {

        Thread myThready = new Thread(new Runnable() {
            @Override
            public void run() {
                CookieManager cookieManager = new java.net.CookieManager();
                getPost("http://ya.ru/", "POST", null, null,
                        cookieManager, true, false);
            }
        });
        myThready.start();

    }
    /**
     * get or post http query.
     *
     * @param urlString     url
     * @param typePostGet   type - "POST" or "GET"
     * @param inData        input data
     * @param header        headers data
     * @param cookieManager cookie manager
     * @param flJSON        is json
     * @param flForm        is form
     * @return return data structure
     */
    public static ReturnData getPost(final String urlString,
                                     final String typePostGet,
                                     final HashMap<String, Object> inData,
                                     final HashMap<String, String> header,
                                     final CookieManager cookieManager,
                                     final boolean flJSON,
                                     final boolean flForm) {
        ReturnData responseData = new ReturnData();
        if (!typePostGet.toLowerCase(Locale.getDefault()).contentEquals("post")
                && !typePostGet.toLowerCase(Locale.getDefault()).contentEquals(
                "get")) {
            Log.d(LOG_TAG, "Wrong getPost type: " + typePostGet);
            responseData.setErrorMsg("Wrong getPost type: " + typePostGet);
            responseData.setStatus(Status.ERROR);
            return responseData;
        }
        String cUrlString = urlString;
        String postString = "";

        if (!flJSON) {
            if (inData != null) {
                for (Entry<String, Object> cVal : inData.entrySet()) {
                    if (!postString.equals("")) {
                        postString += "&";
                    }
                    postString += cVal.getKey() + "="
                            + cVal.getValue().toString();
                }
                if (typePostGet.toLowerCase(Locale.getDefault()).contentEquals(
                        "get")
                        && !postString.equals("")) {
                    cUrlString += "?" + postString;
                }
            }
        } else {
            if (inData != null) {
                //JSONObject json = null;
                //json = new JSONObject(inData);    // not work in service
                Gson gson = new Gson();
                postString = gson.toJson(inData);
            }
        }

        URL reqURL;
        try {
            reqURL = new URL(cUrlString);
        } catch (MalformedURLException e) {
            responseData.setErrorMsg(e.toString());
            responseData.setStatus(Status.ERROR);
            return responseData;
        }
        HttpURLConnection request;
        try {
            if (mUseProxy && mProxy != null) {
                request = (HttpURLConnection) (reqURL.openConnection(mProxy));
            } else {
                request = (HttpURLConnection) (reqURL.openConnection());
            }
        } catch (IOException e) {
            responseData.setErrorMsg(e.toString());
            responseData.setStatus(Status.ERROR);
            return responseData;
        }
        try {
            request.setInstanceFollowRedirects(mAutoRedirect);
            setUrlConnection(request, postString, typePostGet, header, flJSON,
                    flForm, cookieManager);
        } catch (ProtocolException e) {
            responseData.setErrorMsg(e.toString());
            responseData.setStatus(Status.ERROR);
            return responseData;
        }

        if (typePostGet.toLowerCase(Locale.getDefault())
                .contentEquals("post")) {

            try {
                OutputStreamWriter writer;
                request.connect();
                writer = new OutputStreamWriter(request.getOutputStream());
                writer.write(postString);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                responseData.setErrorMsg(e.toString());
                responseData.setStatus(Status.ERROR);
                return responseData;
            }

        }

        if (!mAutoRedirect) {
            try {
                request = aRedirect(
                        urlString, cookieManager,
                        request);
            } catch (IOException e) {
                responseData.setErrorMsg(e.toString());
                responseData.setStatus(Status.ERROR);
                return responseData;
            }
        }

        try {
            StringBuilder sb = new StringBuilder();
            int httpResult = request.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                InputStreamReader inStream;
                String contentEncoding = request.getContentEncoding();
                if (contentEncoding != null
                        && contentEncoding.toLowerCase(Locale.getDefault())
                        .contains("gzip")) {
                    inStream = new InputStreamReader(new GZIPInputStream(
                            request.getInputStream()), "windows-1251");
                } else {
                    inStream = new InputStreamReader(request.getInputStream(),
                            "windows-1251");
                }

                BufferedReader br = new BufferedReader(inStream);
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                br.close();
                Map<String, List<String>> headerFields = request
                        .getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

                if (cookiesHeader != null) {
                    for (String cookie : cookiesHeader) {
                        cookieManager.getCookieStore().add(null,
                                HttpCookie.parse(cookie).get(0));
                    }
                }
                responseData.setResponseStr(sb.toString());

                responseData.setStatus(Status.SUCCESS);
            } else {
                responseData.setErrorMsg(request.getResponseMessage());
                responseData.setStatus(Status.ERROR);
                return responseData;
            }
        } catch (IOException e) {
            responseData.setErrorMsg(e.toString());
            responseData.setStatus(Status.ERROR);
            return responseData;
        }

        return responseData;
    }

    /**
     * set up url connection.
     *
     * @param request     http url connection
     * @param postString  post string
     * @param typePostGet type - "POST" or "GET"
     * @param header      headers data
     * @param flJSON      is json
     * @param flForm      is form
     * @param cm          cookie manager
     * @throws ProtocolException setRequestMethod exception
     */
    private static void setUrlConnection(final HttpURLConnection request,
                                         final String postString,
                                         final String typePostGet,
                                         final HashMap<String, String> header,
                                         final boolean flJSON,
                                         final boolean flForm,
                                         final CookieManager cm)
            throws ProtocolException {


        // Log.d(LOG_TAG, "Using proxy: " + request.usingProxy());
        request.setDoOutput(true);
        if (typePostGet.toLowerCase(Locale.getDefault())
                .contentEquals("post")) {
            request.setDoInput(true);
        }
        request.setConnectTimeout(mConnectTimeout);
        request.setReadTimeout(mReadTimeout);
        if (typePostGet.toLowerCase(Locale.getDefault()).equals("post")) {
            request.addRequestProperty("Content-Length",
                    Integer.toString(postString.length()));
        }
        request.setRequestMethod(typePostGet);

        request.addRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) "
                        + "AppleWebKit/537.36 (KHTML, like Gecko) "
                        + "Chrome/39.0.2171.95 "
                        + "Safari/537.36 OPR/26.0.1656.60");
        request.addRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,"
                        + "image/webp,*/*;q=0.8");
        request.addRequestProperty("Accept-Encoding",
                "gzip, deflate, lzma, sdch");
        request.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
        if (flJSON) {
            request.addRequestProperty("content-type",
                    "application/json; charset=UTF-8");
            // request.addRequestProperty("Accept", "application/json");
        }
        if (flForm) {
            request.addRequestProperty("content-type",
                    "application/x-www-form-urlencoded");
        }
        if (header != null) {
            for (Entry<String, String> cVal : header.entrySet()) {
                request.addRequestProperty(cVal.getKey(), cVal.getValue());
            }
        }
        if (cm.getCookieStore().getCookies().size() > 0) {
            request.setRequestProperty(
                    "Cookie",
                    joinCookie(";", cm.getCookieStore()
                            .getCookies()));
        }
    }

    /**
     * auto redirect http query.
     *
     * @param urlString url
     * @param cm        cookie manager
     * @param rqt       http url connection
     * @return http url connection
     * @throws IOException http connect error
     */
    private static HttpURLConnection aRedirect(final String urlString,
                                               final CookieManager cm,
                                               final HttpURLConnection rqt)
            throws IOException {
        final int connectTimeout = 1000;
        final int readTimeout = 15000;
        //ReturnData responseData = new ReturnData();
        int httpResult;
        String currLocation;
        String currUrl = urlString;
        URL baseURL;
        URL nextURL;
        HttpURLConnection request = rqt;
        while (true) {
            httpResult = request.getResponseCode();
            Map<String, List<String>> headerFields = request
                    .getHeaderFields();
            List<String> cookiesHeader = headerFields
                    .get(COOKIES_HEADER);

            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    cm.getCookieStore().add(null,
                            HttpCookie.parse(cookie).get(0));
                }
            }
            switch (httpResult) {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case HttpURLConnection.HTTP_SEE_OTHER:
                    currLocation = request.getHeaderField("Location");
                    baseURL = new URL(currUrl);
                    nextURL = new URL(baseURL, currLocation);

                    currUrl = nextURL.toExternalForm();
                    if (mUseProxy && mProxy != null) {
                        request = (HttpURLConnection) nextURL
                                .openConnection(mProxy);
                    } else {
                        request = (HttpURLConnection) nextURL
                                .openConnection();
                    }
                    request.addRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X "
                                    + "10_10_2) AppleWebKit/537.36 (KHTML, "
                                    + "like Gecko) Chrome/"
                                    + "39.0.2171.95 Safari"
                                    + "/537.36 OPR/26.0.1656.60");
                    request.addRequestProperty("Accept",
                            "text/html,application/xhtml+xml,application/"
                                    + "xml;q=0.9,image/webp,*/*;q=0.8");
                    request.addRequestProperty("Accept-Encoding",
                            "gzip, deflate, lzma, sdch");
                    request.addRequestProperty("Accept-Language",
                            "en-US,en;q=0.8");
                    if (cm.getCookieStore().
                            getCookies().size() > 0) {
                        request.setRequestProperty(
                                "Cookie",
                                joinCookie(";", cm
                                        .getCookieStore()
                                        .getCookies()));
                    }
                    request.setConnectTimeout(connectTimeout);
                    request.setReadTimeout(readTimeout);
                    request.setInstanceFollowRedirects(false);
                    continue;
                default:
                    break;
            }

            break;
        }

        return request;
    }

    /**
     * get pairs from http response.
     *
     * @param inputText       http response text
     * @param patternBlockStr pattern for block
     * @param patternPairStr  pattern for pairs
     * @return map of paris
     */
    public static HashMap<String, String> findPairsInText(
            final String inputText,
            final String patternBlockStr, final String patternPairStr) {
        HashMap<String, String> retPairs = new HashMap<>();
        final Pattern patternBlock = Pattern.compile(patternBlockStr,
                Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        final Pattern patternPair = Pattern.compile(patternPairStr,
                Pattern.CASE_INSENSITIVE);
        Matcher matchesBlock = patternBlock.matcher(inputText);
        if (matchesBlock.find()) {
            String currBlockStr = matchesBlock.group(1);
            Matcher matchesPair = patternPair.matcher(currBlockStr);
            while (matchesPair.find()) {
                String currPairKey = matchesPair.group(1);
                String currPairValue = matchesPair.group(2);
                retPairs.put(currPairKey, currPairValue);
            }
        }
        return retPairs;
    }

    /**
     * convert from long to base36 string format.
     *
     * @param value value to convert
     * @return string in base 36 format
     */
    public static String base36FromInt(final long value) {
        String base36 = "0123456789abcdefghijklmnopqrstuvwxyz";
        long locValue = value;
        final long lengthBase = 36;
        String returnValue = "";
        do {
            int x;
            x = (int) (locValue % ((long) base36.length()));
            char y = base36.charAt(x);
            returnValue = y + returnValue;
            locValue = locValue / lengthBase;
        } while (locValue != 0);

        return returnValue;
    }

    /**
     * generate md5 from string.
     *
     * @param s input string
     * @return md5 string
     */
    public static String md5(final String s) {
        final int lenMax = 32;
        final int valHex = 16;
        MessageDigest m;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        m.reset();
        m.update(s.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String hashText = bigInt.toString(valHex);
        // Now we need to zero pad it if you actually want the full 32 chars.
        while (hashText.length() < lenMax) {
            hashText = "0" + hashText;
        }
        return hashText;
    }

    /**
     * create fingerprint to game.
     *
     * @param param1 param
     * @return fingerprint
     */
    public static String createFingerprint(
            final HashMap<String, String> param1) {
        final int startIndexXEnv = 6;
        final Map<String, String> loc3 = new TreeMap<>(
                new Comparator<String>() {
                    @Override
                    public int compare(final String lhs,
                                       final String rhs) {
                        return lhs.compareTo(rhs);
                    }
                });
        // HashMap<String, String> loc3 = new HashMap<String, String>();
        String loc4 = "";
        for (Entry<String, String> cvl : param1.entrySet()) {
            String loc6key = cvl.getKey();
            String loc6val = cvl.getValue();
            if (loc6key.contains("X-Env")) {
                String loc2 = loc6key.substring(startIndexXEnv).toUpperCase(
                        Locale.getDefault());
                loc3.put(loc2, loc6val);
            }
        }

        for (Entry<String, String> cvl : loc3.entrySet()) {
            String loc6key = cvl.getKey();
            String loc6val = cvl.getValue();
            loc4 = loc4 + (loc6key + "=" + loc6val);
        }
        return loc4;
    }

    /**
     * str pad from flash.
     *
     * @param param1 param 1
     * @param param2 param 2
     * @param param3 param 3
     * @param param4 param 4
     * @return string pad
     */
    public static String strPad(final String param1,
                                final int param2,
                                final String param3,
                                final int param4) {
        String locParam1 = param1;
        String locParam3 = param3;
        int locParam4 = param4;
        if (locParam3 == null) {
            locParam3 = " ";
        }
        if (locParam4 == 0) {
            locParam4 = 1;
        }
        int loc6 = locParam4 & 1;
        int loc5 = locParam4 & 2;

        if (locParam3.length() > 1) {
            char firstChar = locParam3.charAt(0);
            locParam3 = "" + firstChar;
        } else {
            if (locParam3.length() == 0) {
                locParam3 = " ";
            }
        }
        if (loc6 > 0 || loc5 > 0) {
            while (locParam1.length() < param2) {
                if (loc6 > 0) {
                    locParam1 = locParam3 + locParam1;
                }
                if (locParam1.length() < param2 && loc5 > 0) {
                    locParam1 = locParam1 + locParam3;
                }
            }
        }
        return locParam1;
    }

    /**
     * change date format.
     *
     * @param dateString date string
     * @param inFormat   input format of string
     * @param outFormat  output format of string
     * @return date in new format
     */
    public static String changeDateFormat(final String dateString,
                                          final String inFormat,
                                          final String outFormat) {
        SimpleDateFormat dateStringFormatter = new SimpleDateFormat(inFormat,
                Locale.getDefault());
        Calendar currDate = new GregorianCalendar();
        try {
            currDate.setTime(dateStringFormatter.parse(dateString));
            dateStringFormatter.applyPattern(outFormat);

            return dateStringFormatter.format(currDate.getTime());
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return "";
    }

    /**
     * generate auth signature to game.
     *
     * @param inData input data
     * @return auth signature
     */
    private String createAuthSignature(
            final HashMap<String, Object> inData) {
        String postString = "";
        if (inData != null) {
            //JSONObject currJsonData = new JSONObject(inData);
            //postString = currJsonData.toString();
            Gson gson = new Gson();
            postString = gson.toJson(inData);
        }
        String loc3 = "";
        loc3 += mSessionHeaderX.get("X-Request-Id");
        loc3 += ":";
        loc3 += mAuthKey;
        loc3 += ":";
        loc3 += mSessionHeaderX.get("X-Auth-Session-Id");
        loc3 += ":";
        loc3 += postString;
        loc3 += ":";
        loc3 += createFingerprint(mSessionHeaderX);

        return md5(loc3);
    }

    /**
     * create sig.
     *
     * @param vkId     vk id
     * @param sendData send data
     * @param vkSecret vk secret key
     * @return sig string
     */
    public static String createSig(final String vkId,
                                   final HashMap<String, Object> sendData,
                                   final String vkSecret) {
        String currSig = vkId;
        TreeMap<String, String> cSendData = new TreeMap<>();
        for (Entry<String, Object> inCKeyVal : sendData.entrySet()) {
            String inKey = inCKeyVal.getKey();
            String inVal = inCKeyVal.getValue().toString();
            cSendData.put(inKey, inVal);
        }

        for (Entry<String, String> inCKeyVal : cSendData.entrySet()) {
            String currKey = inCKeyVal.getKey();
            if (currKey.equals("sid") || currKey.equals("sig")) {
                continue;
            }
            String currVal = inCKeyVal.getValue();
            currSig = currSig + (currKey + "=" + currVal);
        }
        currSig = currSig + vkSecret;
        currSig = md5(currSig);

        return currSig;
    }

    /**
     * generate session key.
     *
     * @return session key
     */
    private String generateSessionKey() {
        final long msInSecond = 1000L;
        final long maxInt = 4294967295L; // 2^32 − 1
        final int param2 = 7;
        Calendar currDate = Calendar.getInstance();
        int loc4 = (int) (currDate.getTimeInMillis() / msInSecond);
        long loc5 = loc4 & maxInt;
        Random genRand = new Random();
        long loc2 = (genRand.nextLong() % maxInt) + maxInt;
        String loc1 = base36FromInt(loc5);
        String loc3 = base36FromInt(loc2);

        return strPad(loc1, param2, "0", 1) + strPad(loc3, param2, "0", 1);
    }

    /**
     * get curr time in string format.
     *
     * @return curr time string
     */
    public static String getCurrTimeStr() {
        final long msInSecond = 1000L;
        Calendar currDate = Calendar.getInstance();
        int currTime = (int) (currDate.getTimeInMillis() / msInSecond);

        return String.valueOf(currTime);
    }

    /**
     * get random to 10000.
     *
     * @return random in string format
     */
    public static String getRnd() {
        final int maxValue = 10000;
        Random genRand = new Random();
        int currRnd = genRand.nextInt(maxValue);

        return String.valueOf(currRnd);
    }

    /**
     * join cookie to string.
     *
     * @param delimiter  delimiter for pairs
     * @param listCookie list of cookie
     * @return string cookies
     */
    public static String joinCookie(
            final CharSequence delimiter,
            final List<HttpCookie> listCookie) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : listCookie) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }

    /**
     * send data.
     *
     * @param sendData data to send
     * @return return data
     */
    private ReturnData sendReceive(
            final HashMap<String, Object> sendData) {
        if (mRequestId == 1) {
            mSessionHeaderX.put("X-Auth-Session-Init", "1"); // only for first
        }
        mSessionHeaderX.put("X-Request-Id",
                String.valueOf(mRequestId)); // increase
        // it
        // after
        // each
        // send
        // 1,
        // 2,
        // 3...
        mSessionHeaderX.put("X-Auth-Signature", createAuthSignature(sendData));
        ReturnData retDict = getPost(mSite, "POST", sendData,
                mSessionHeaderX, mCookieManager, true, false);
        if (mRequestId == 1) {
            mSessionHeaderX.remove("X-Auth-Session-Init");
        }
        mRequestId = mRequestId + 1;

        return retDict;
    }

    /**
     * to json string.
     *
     * @param str input string
     * @return json string
     */
    public static String toJsonString(final String str) {
        if (str == null) {
            return "";
        }
        return "" + str + "";
    }

    /**
     * generate call to json.
     *
     * @param ident ident
     * @param args  args
     * @param name  name
     * @return map call
     */
    public static HashMap<String, Object> genCall(
            final String ident,
            final HashMap<String, Object> args, final String name) {
        HashMap<String, Object> retMap = new HashMap<>();
        retMap.put("ident", toJsonString(ident));
        retMap.put("args", args);
        if (name == null) {
            retMap.put("name", toJsonString(ident));
        } else {
            retMap.put("name", toJsonString(name));
        }

        return retMap;
    }
}
