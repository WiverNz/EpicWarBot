package com.wivern.epicwarbot;

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
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class EpicWarBot {
	public class GiftInfo {
		public String userId;
		public List<String> ids_arr;
		GiftInfo(String uid, List<String> idsarr) {
			userId = uid;
			ids_arr = idsarr;
		}
	}

	public enum Status {
		NOTINIT, SUCCESS, ERROR
	}

	private String m_vkId;
	private final String m_site = "http://epicwar-vkontakte.progrestar.net/rpc/";
	private HashMap<String, String> m_sessionHeaderX;
	private int m_requestId;
	private String m_auth_key;
	private String m_appId;
	private String m_sid;
	private String m_sig;
	private String m_secret;
	private List<GiftInfo> m_friendGifts;
	private List<String> m_friendSendGifts;
	private List<String> m_friendAlreadySendGifts;

	private final int CASTLE_ID = 1;
	private final int MINE_ID = 2;
	private final int TREASURY_ID = 3;
	private final int MILL_ID = 4;
	private final int BARN_ID = 5;
	private final int BARRACKS_ID = 6;
	private final int STAFF_ID = 7;
	private final int BUILDER_HUT_ID = 8;
	private final int FORGE_ID = 9;
	private final int BALLISTA_ID = 10;
	private final int WALL_ID = 11;
	private final int ARCHER_TOWER_ID = 12;
	private final int CANNON_ID = 13;
	private final int THUNDER_TOWER_ID = 14;
	private final int ICE_TOWER_ID = 15;
	private final int FIRE_TOWER_ID = 16;
	private final int CLAN_HOUSE_ID = 17;
	private final int DARK_TOWER_ID = 18;
	private final int TAVERN_ID = 19;
	private final int ALCHEMIST_ID = 20;

	private final int SAND_ID = 31;

	private final int GOLD_RESOURCE_ID = 1;
	private final int FOOD_RESOURCE_ID = 2;

	private final int[] CAPACITIES = { 0, 5000, 15000, 35000, 75000, 150000,
			300000, 600000, 1000000, 2000000, 3000000, 4000000 };
	static final String COOKIES_HEADER = "Set-Cookie";

	private List<Integer> m_arrayGoldMine;
	private List<Integer> m_arrayMillMine;
	private List<Integer> m_arraySandMine;
	private boolean m_cemetery;
	private static String LOG_PREF = "EPIC_WAR_BOT";
	private CookieManager m_cookieManager;

	private boolean m_vkConnected;
	private boolean m_gameConnected;
	private static boolean m_useProxy = false;
	private static Proxy m_proxy;

	EpicWarBot() {
		init(true);
	}

	private void init(boolean constructor) {
		m_vkId = "";
		m_requestId = 1;
		m_auth_key = "";
		m_appId = "";
		m_sid = "";
		m_sig = "";
		m_secret = "";
		m_cemetery = false;
		m_vkConnected = false;
		m_gameConnected = false;
		m_proxy = null;
		if (constructor == true) {
			m_sessionHeaderX = new HashMap<String, String>();
			m_arrayGoldMine = new ArrayList<Integer>();
			m_arrayMillMine = new ArrayList<Integer>();
			m_arraySandMine = new ArrayList<Integer>();
			m_friendGifts = new ArrayList<GiftInfo>();
			m_friendSendGifts = new ArrayList<String>();
			m_friendAlreadySendGifts = new ArrayList<String>();
			m_cookieManager = new java.net.CookieManager();
		} else {
			m_sessionHeaderX.clear();
			m_arrayGoldMine.clear();
			m_arrayMillMine.clear();
			m_arraySandMine.clear();
			m_friendGifts.clear();
			m_friendSendGifts.clear();
			m_friendAlreadySendGifts.clear();
			m_cookieManager.getCookieStore().removeAll();
		}
	}

	public static void SetUseProxy(boolean useProxy) {
		m_useProxy = useProxy;
	}

	public static void SetProxy(String ip, int port) {
		m_proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
	}

	public AnswerInfo VKConnect(String vkLogin, String vkPassword) {
		AnswerInfo retResult = new AnswerInfo();
		init(false);
		Log.d(LOG_PREF, "VKConnect: " + vkLogin + " " + vkPassword);
		final String urlPath = "http://login.vk.com/";
		HashMap<String, Object> cSendData = new HashMap<String, Object>();

		cSendData.put("email", vkLogin);
		cSendData.put("pass", vkPassword);
		cSendData.put("act", "login");
		cSendData.put("amp;to", "&amp;");

		ReturnData retDict = GetPost(urlPath, "GET", cSendData, null,
				m_cookieManager, false, false);
		if (retDict.status == Status.SUCCESS) {
			HashMap<String, String> vkPairs = FindPairsInText(
					retDict.responseStr, "var vk\\s*=\\s*\\{(.*?)\\}",
					"[,\\s\\n\\r]*([^:]*):\\s*([^\\n^\\r^,]*)");
			if (vkPairs.containsKey("id") == true) {
				m_vkId = vkPairs.get("id");
				if (m_vkId != "0") {
					m_vkConnected = true;
					retResult.Set("Connected!", retDict.status.toString(),
							false, "");
				} else {
					retResult.Set("Not connected!", retDict.status.toString(),
							true, "Authorization problem: vkId == 0");
				}
			} else {
				retResult.Set("Not connected!", retDict.status.toString(),
						true, "Authorization problem: var vk id not found!");
			}
		} else {
			Log.d(LOG_PREF, retDict.errorMsg);
			retResult.Set("Not connected!", retDict.status.toString(), true,
					"retDict.status != Status.SUCCESS");
		}
		return retResult;
	}

	public AnswerInfo VKDisconnect() {
		Log.d(LOG_PREF, "VKDisconnect");
		AnswerInfo retResult = new AnswerInfo();
		init(false);
		final String urlPath = "http://vk.com/login.php";
		HashMap<String, Object> cSendData = new HashMap<String, Object>();

		cSendData.put("op", "logout");

		ReturnData retDict = GetPost(urlPath, "GET", cSendData, null,
				m_cookieManager, false, false);
		if (retDict.status == Status.SUCCESS) {
			retResult
					.Set("Disconnected!", retDict.status.toString(), false, "");
		} else {
			Log.d(LOG_PREF, retDict.errorMsg);
			retResult.Set("Not disconnected!", retDict.status.toString(), true,
					"retDict.status != Status.SUCCESS");
		}
		init(false);

		return retResult;
	}

	public AnswerInfo GameConnect() {
		Log.d(LOG_PREF, "GameConnect");
		AnswerInfo retResult = new AnswerInfo();
		if (m_vkConnected == false) {
			retResult.Set("Not connected!", "", true,
					"Vkontakte not connected!");
			return retResult;
		}

		String urlPath = "http://vk.com/al_profile.php";
		HashMap<String, Object> cSendData = new HashMap<String, Object>();
		HashMap<String, String> cSendHeaders = new HashMap<String, String>();

		cSendData.put("__query", "clashofthrones");
		cSendData.put("_ref", "apps");
		cSendData.put("_tstat", "353%2C110%2C291%2C359%2Capps");
		cSendData.put("al", -1);
		cSendData.put("al_id", m_vkId);
		cSendData.put("mid", m_vkId);
		cSendData.put("ref", 9);
		cSendData.put("_rndVer", 32546);

		ReturnData retDict = GetPost(urlPath, "GET", cSendData, null,
				m_cookieManager, false, false);
		if (retDict.status == Status.SUCCESS) {
			String patternPair = "[,\\s\\n\\r]*\\\\\"([^:^\\\\]*)\\\\\":\\s*[\\\\\"]*(.*?)(\\\\\"|,|$)";
			HashMap<String, String> paramPairs = FindPairsInText(
					retDict.responseStr, "var params\\s*=\\s*\\{(.*?)\\}",
					patternPair);
			HashMap<String, String> optionPairs = FindPairsInText(
					retDict.responseStr, "var options\\s*=\\s*\\{(.*?)\\}",
					patternPair);
			if (optionPairs.containsKey("src") == true) {
				String iUrl = optionPairs.get("src");
				iUrl = iUrl.replace("\\\\\\", "");
				cSendData.clear();
				for (Entry<String, String> cItem : paramPairs.entrySet()) {
					String cKey = cItem.getKey();
					String cVal = cItem.getValue();
					if (cKey == "api_url") {
						cVal = cVal.replace("\\\\\\", "");
					}
					cSendData.put(cKey, cVal);
				}
				m_sid = (String) cSendData.get("sid");
				retDict = GetPost(iUrl, "GET", cSendData, cSendHeaders,
						m_cookieManager, false, false);
				if (retDict.status == Status.SUCCESS) {
					HashMap<String, String> paramFlash = FindPairsInText(
							retDict.responseStr,
							"params.flashvars\\s*=[\\s\\n\\r]*\"(.*?)\"",
							"(.*?)=(.*?)(&|$)");
					if (paramFlash.containsKey("auth_key")) {
						m_requestId = 1;
						m_auth_key = paramFlash.get("auth_key");
						m_appId = optionPairs.get("aid");
						m_secret = paramFlash.get("secret");
						m_sessionHeaderX.put("X-Auth-Token", m_auth_key);
						m_sessionHeaderX.put("X-Auth-Session-Id",
								generateSessionKey());
						m_sessionHeaderX.put("X-Server-Time", GetCurrTimeStr());
						m_sessionHeaderX.put("X-Auth-Application-Id", m_appId);
						m_sessionHeaderX.put("X-Auth-User-Id", m_vkId);
						m_sessionHeaderX.put("X-Auth-Network-Ident",
								"vkontakte");
						m_sessionHeaderX.put("X-Env-Library-Version", "0");
						m_sessionHeaderX
								.put("X-Request-With", "XMLHttpRequest");
						// m_sessionHeaderX.put("X-Auth-Signature",
						// createAuthSignature(post));
						retResult = SendRecvFirstData();
					}
				}
			}

		}
		return retResult;
	}

	private AnswerInfo SendRecvFirstData() {
		AnswerInfo retResult = new AnswerInfo();
		retResult.Set("Not connected!", "", true,
				"SendRecvFirstData return null data");
		HashMap<String, Object> formSendData = new HashMap<String, Object>();
		String cCode = "return{\"user\":API.getProfiles({\"https\":0,\"uids\":"
				+ m_vkId
				+ ",\"fields\":\"can_post,uid,first_name,last_name,nickname,sex,bdate,photo,photo_medium,photo_big,has_mobile,rate,city,country,photo_max_orig\"}),\"friends\":API.friends.get({\"https\":0,\"count\" : 500, \"fields\":\"uid,country,first_name,last_name,photo,photo_medium,photo_big,sex,can_post,bdate,online,photo_max_orig\"}),\"appFriends\":API.getAppFriends(),\"groups\":API.getGroups()};";
		formSendData.put("api_id", m_appId);
		formSendData.put("code", cCode);
		formSendData.put("format", "json");
		formSendData.put("https", "0");
		formSendData.put("method", "execute");
		formSendData.put("rnd", GetRnd());
		formSendData.put("sid", m_sid);

		formSendData.put("v", "3.0");

		formSendData.put("sig", CreateSig(m_vkId, formSendData, m_secret));
		String cSite = "http://vk.com/api.php";
		ReturnData retDictForm = GetPost(cSite, "POST", formSendData, null,
				m_cookieManager, false, true);

		List<String> friendsArray = new ArrayList<String>();
		List<String> appFriendsArray = new ArrayList<String>();
		String vkbirthday = "";
		int vkcity = 0;
		String vkfirstName = "";
		String vkphotoUrl = "";
		String vklastName = "";

		if (retDictForm.status == Status.SUCCESS) {
			JSONObject jresponse;
			try {
				JSONObject retDictFormJson = new JSONObject(new JSONTokener(
						retDictForm.responseStr));
				jresponse = retDictFormJson.getJSONObject("response");
				JSONArray juser = jresponse.getJSONArray("user");
				if (juser.length() > 0) {
					JSONObject userData = new JSONObject();
					userData = juser.getJSONObject(0);
					try {
						vkfirstName = userData.getString("first_name");
						vklastName = userData.getString("last_name");
						vkphotoUrl = userData.getString("photo_medium");
						vkbirthday = userData.getString("bdate");
						vkbirthday = ChangeDateFormat(vkbirthday, "dd.M.yyyy",
								"yyyy-M-dd");
						vkcity = userData.getInt("city");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				JSONArray jfriends = jresponse.getJSONArray("friends");
				for (int i = 0; i < jfriends.length(); i++) {
					JSONObject cjfriend_i = jfriends.getJSONObject(i);
					long cjfriendid = cjfriend_i.getLong("uid");
					friendsArray.add(String.valueOf(cjfriendid));
				}
				JSONArray jappfriends = jresponse.getJSONArray("appFriends");
				for (int i = 0; i < jappfriends.length(); i++) {
					long cjappfriend = (Long) jappfriends.get(i);

					appFriendsArray.add(String.valueOf(cjappfriend));
				}
			} catch (JSONException e) {
				retResult.Set("Not connected!", e.toString(), true,
						"SendRecvFirstData parse json error");
			}
		}

		if (vkfirstName == "") {
			return retResult;
		}
		HashMap<String, Object> j_referrer = new HashMap<String, Object>();
		j_referrer.put("type", ToJsonString("user_apps"));
		j_referrer.put("id", 0);
		HashMap<String, Object> j_user = new HashMap<String, Object>();
		j_user.put("country", ToJsonString("function Function() []"));
		j_user.put("birthday", ToJsonString(vkbirthday));
		j_user.put("sex", ToJsonString("male"));
		j_user.put("locale", ToJsonString("en"));
		j_user.put("id", ToJsonString(m_vkId));
		j_user.put("city", vkcity);
		j_user.put("firstName", ToJsonString(vkfirstName));
		j_user.put("photoUrl", ToJsonString(vkphotoUrl));
		j_user.put("lastName", ToJsonString(vklastName));
		j_user.put("referrer", j_referrer);
		HashMap<String, Object> j_argsRegistration = new HashMap<String, Object>();
		j_argsRegistration.put("friendIds", friendsArray);
		j_argsRegistration.put("user", j_user);
		HashMap<String, Object> j_argscheckRegisteredUsers = new HashMap<String, Object>();
		j_argscheckRegisteredUsers.put("users", appFriendsArray);
		HashMap<String, Object> j_argsinvitationGetUsers = new HashMap<String, Object>();
		j_argsinvitationGetUsers.put("ids", friendsArray);
		HashMap<String, Object> j_argsEmpty = new HashMap<String, Object>();
		List<Object> j_calls = new ArrayList<Object>();
		j_calls.add(GenCall("registration", j_argsRegistration, null));
		j_calls.add(GenCall("boostGetAll", j_argsEmpty, null));
		j_calls.add(GenCall("artefactGetList", j_argsEmpty, null));
		j_calls.add(GenCall("battleStatisticGet", j_argsEmpty, null));
		j_calls.add(GenCall("getTime", j_argsEmpty, null));
		j_calls.add(GenCall("getSelfInfo", j_argsEmpty, null));
		j_calls.add(GenCall("getDynamicParams", j_argsEmpty, null));
		j_calls.add(GenCall("getArmyQueue", j_argsEmpty, null));
		j_calls.add(GenCall("getBuildings", j_argsEmpty, null));
		j_calls.add(GenCall("heroesGetList", j_argsEmpty, null));
		j_calls.add(GenCall("getResearchQueue", j_argsEmpty, null));
		j_calls.add(GenCall("getMissions", j_argsEmpty, null));
		j_calls.add(GenCall("getQuests", j_argsEmpty, null));
		j_calls.add(GenCall("getProtections", j_argsEmpty, null));
		j_calls.add(GenCall("getInvitedBy", j_argsEmpty, null));
		j_calls.add(GenCall("getInvitedUsers", j_argsEmpty, null));
		j_calls.add(GenCall("getBonusCrystals", j_argsEmpty, null));
		j_calls.add(GenCall("getSettings", j_argsEmpty, null));
		j_calls.add(GenCall("promoGetHalfBilling", j_argsEmpty, null));
		j_calls.add(GenCall("giftGetAvailable", j_argsEmpty, null));
		j_calls.add(GenCall("giftGetReceivers", j_argsEmpty, null));
		j_calls.add(GenCall("cloverGetAll", j_argsEmpty, null));
		j_calls.add(GenCall("paymentsCount", j_argsEmpty, null));
		j_calls.add(GenCall("cemeteryGet", j_argsEmpty, null));
		j_calls.add(GenCall("getNotices", j_argsEmpty, null));
		j_calls.add(GenCall("allianceGetMessages", j_argsEmpty, null));
		j_calls.add(GenCall("getGlobalNews", j_argsEmpty, null));
		j_calls.add(GenCall("battleGetActive", j_argsEmpty, null));
		j_calls.add(GenCall("spellList", j_argsEmpty, null));
		j_calls.add(GenCall("spellProductionQueue", j_argsEmpty, null));
		j_calls.add(GenCall("checkRegisteredUsers", j_argscheckRegisteredUsers,
				null));
		j_calls.add(GenCall("invitationGetUsers", j_argsinvitationGetUsers,
				null));
		j_calls.add(GenCall("state", j_argsEmpty, null));

		HashMap<String, Object> jsonData = new HashMap<String, Object>();
		jsonData.put("sesion", null);
		jsonData.put("calls", j_calls);

		ReturnData retDict = SendRecv(jsonData);
		if (retDict.status == Status.SUCCESS) {
			try {
				JSONObject retDictFormJson = new JSONObject(new JSONTokener(
						retDict.responseStr));
				JSONArray jresults = retDictFormJson.getJSONArray("results");
				for (int i = 0; i < jresults.length(); i++) {
					JSONObject fbobj = jresults.getJSONObject(i);
					String fcident = fbobj.getString("ident");
					if (fcident.contentEquals("giftGetAvailable") == true) {
						JSONObject fgiftRes = fbobj.getJSONObject("result");
						JSONArray fgiftArr = fgiftRes.getJSONArray("gift");
						for (int j = 0; j < fgiftArr.length(); j++) {
							JSONObject cGiftInfo = fgiftArr.getJSONObject(j);
							String cGiftIDS = cGiftInfo.getString("id");
							JSONObject cGiftBody = cGiftInfo
									.getJSONObject("body");
							JSONObject cGiftUserInfo = cGiftBody
									.getJSONObject("userInfo");
							String cGiftUserId = cGiftUserInfo.getString("id");
							List<String> cArrayUserIds = new ArrayList<String>();
							cArrayUserIds.add(cGiftIDS);
							GiftInfo gi = new GiftInfo(cGiftUserId,
									cArrayUserIds);
							m_friendGifts.add(gi);
						}
					} else if (fcident.contentEquals("getBuildings") == true) {
						JSONObject fbuildRes = fbobj.getJSONObject("result");
						JSONArray fbuildArray = fbuildRes
								.getJSONArray("building");
						for (int j = 0; j < fbuildArray.length(); j++) {
							JSONObject fcBuildInfo = fbuildArray
									.getJSONObject(j);
							int cTypeOfBuild = fcBuildInfo.getInt("typeId");
							int cIdOfBuild = fcBuildInfo.getInt("id");
							boolean cCompl = fcBuildInfo.getBoolean("completed");
							if (cCompl == true) {
								if (cTypeOfBuild == MILL_ID) {
									m_arrayMillMine.add(cIdOfBuild);
								} else if (cTypeOfBuild == MINE_ID) {
									m_arrayGoldMine.add(cIdOfBuild);
								} else if (cTypeOfBuild == SAND_ID) {
									m_arraySandMine.add(cIdOfBuild);
								}
							}
						}
					} else if (fcident.contentEquals("checkRegisteredUsers") == true) {
						JSONObject fusersRes = fbobj.getJSONObject("result");
						JSONArray fusersArray = fusersRes
								.getJSONArray("result");
						for (int j = 0; j < fusersArray.length(); j++) {
							String cUser = fusersArray.getString(j);
							m_friendSendGifts.add(cUser);
						}
					} else if (fcident.contentEquals("giftGetReceivers") == true) {
						JSONObject fusersRes = fbobj.getJSONObject("result");
						JSONArray fusersArray = fusersRes
								.getJSONArray("receivers");
						for (int j = 0; j < fusersArray.length(); j++) {
							JSONObject cUserObj = fusersArray.getJSONObject(j);
							String cUserId = cUserObj.getString("toUserId");
							m_friendAlreadySendGifts.add(cUserId);
						}
					} else if (fcident.contentEquals("cemeteryGet") == true) {
						JSONObject fcemeteryRes = fbobj.getJSONObject("result");
						JSONArray fcemeteryArray = fcemeteryRes
								.getJSONArray("result");
						if (fcemeteryArray.length() > 0) {
							m_cemetery = true;
						}
					}
				}
				m_gameConnected = true;
				retResult.Set("Game connected!", "", true, "");
			} catch (JSONException e) {
				retResult.Set("Not connected!", e.toString(), true,
						"retDictFormJson parse error " + retDict.responseStr);
			}

		}

		return retResult;
	}

	public static ReturnData GetPost(String urlString, String typePostGet,
			HashMap<String, Object> inData, HashMap<String, String> headers,
			CookieManager cookieManager, boolean flJSON, boolean flForm) {
		boolean autoRedirect = false;

		ReturnData responseData = new ReturnData();
		if (typePostGet.toLowerCase(Locale.getDefault()).contentEquals("post") != true
				&& typePostGet.toLowerCase(Locale.getDefault()).contentEquals(
						"get") != true) {
			Log.d(LOG_PREF, "Wrong getpost type: " + typePostGet);
			responseData.errorMsg = "Wrong getpost type: " + typePostGet;
			responseData.status = Status.ERROR;
			return responseData;
		}
		String cUrlString = urlString;
		String postString = "";

		if (flJSON == false) {
			for (Entry<String, Object> cVal : inData.entrySet()) {
				if (postString != "") {
					postString += "&";
				}
				postString += cVal.getKey() + "=" + cVal.getValue().toString();
			}
			if (typePostGet.toLowerCase(Locale.getDefault()).contentEquals(
					"get") == true
					&& postString != "") {
				cUrlString += "?" + postString;
			}
		} else {
			JSONObject json = new JSONObject(inData);
			postString = json.toString();
		}

		URL reqURL = null;
		try {
			reqURL = new URL(cUrlString);
		} catch (MalformedURLException e) {
			responseData.errorMsg = e.toString();
			responseData.status = Status.ERROR;
			return responseData;
		}
		HttpURLConnection request;
		try {
			if (m_useProxy == true && m_proxy != null) {
				request = (HttpURLConnection) (reqURL.openConnection(m_proxy));
			} else {
				request = (HttpURLConnection) (reqURL.openConnection());
			}
		} catch (IOException e) {
			responseData.errorMsg = e.toString();
			responseData.status = Status.ERROR;
			return responseData;
		}
		request.setInstanceFollowRedirects(autoRedirect);
		// Log.d(LOG_PREF, "Using proxy: " + request.usingProxy());
		request.setDoOutput(true);
		if (typePostGet.toLowerCase(Locale.getDefault()).contentEquals("post") == true) {
			request.setDoInput(true);
		}
		request.setConnectTimeout(1000);
		request.setReadTimeout(15000);
		if (typePostGet.toLowerCase(Locale.getDefault()) == "post") {
			request.addRequestProperty("Content-Length",
					Integer.toString(postString.length()));
		}

		try {
			request.setRequestMethod(typePostGet);
		} catch (ProtocolException e) {
			responseData.errorMsg = e.toString();
			responseData.status = Status.ERROR;
			return responseData;
		}

		request.addRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36 OPR/26.0.1656.60");
		request.addRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		request.addRequestProperty("Accept-Encoding",
				"gzip, deflate, lzma, sdch");
		request.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
		if (flJSON == true) {
			request.addRequestProperty("content-type",
					"application/json; charset=UTF-8");
			// request.addRequestProperty("Accept", "application/json");
		}
		if (flForm == true) {
			request.addRequestProperty("content-type",
					"application/x-www-form-urlencoded");
		}
		if (headers != null) {
			for (Entry<String, String> cVal : headers.entrySet()) {
				request.addRequestProperty(cVal.getKey(), cVal.getValue());
			}
		}
		if (cookieManager.getCookieStore().getCookies().size() > 0) {
			request.setRequestProperty(
					"Cookie",
					JoinCookie(";", cookieManager.getCookieStore().getCookies()));
		}

		if (typePostGet.toLowerCase(Locale.getDefault()).contentEquals("post") == true) {

			try {
				OutputStreamWriter writer = null;
				request.connect();
				writer = new OutputStreamWriter(request.getOutputStream());
				writer.write(postString);
				writer.flush();
				writer.close();
			} catch (IOException e) {
				responseData.errorMsg = e.toString();
				responseData.status = Status.ERROR;
				return responseData;
			}

		}

		if (autoRedirect == false) {
			int HttpResult;
			String currLocation;
			String currUrl = urlString;
			URL baseURL;
			URL nextURL;
			while (true) {
				try {
					HttpResult = request.getResponseCode();
					Map<String, List<String>> headerFields = request
							.getHeaderFields();
					List<String> cookiesHeader = headerFields
							.get(COOKIES_HEADER);

					if (cookiesHeader != null) {
						for (String cookie : cookiesHeader) {
							cookieManager.getCookieStore().add(null,
									HttpCookie.parse(cookie).get(0));
						}
					}
				} catch (IOException e) {
					responseData.errorMsg = e.toString();
					responseData.status = Status.ERROR;
					return responseData;
				}
				switch (HttpResult) {
				case HttpURLConnection.HTTP_MOVED_PERM:
				case HttpURLConnection.HTTP_MOVED_TEMP:
				case HttpURLConnection.HTTP_SEE_OTHER:
					currLocation = request.getHeaderField("Location");
					try {
						baseURL = new URL(currUrl);
						nextURL = new URL(baseURL, currLocation);
					} catch (MalformedURLException e) {
						responseData.errorMsg = e.toString();
						responseData.status = Status.ERROR;
						return responseData;
					}

					currUrl = nextURL.toExternalForm();
					try {
						if (m_useProxy == true && m_proxy != null) {
							request = (HttpURLConnection) nextURL
									.openConnection(m_proxy);
						} else {
							request = (HttpURLConnection) nextURL
									.openConnection();
						}
					} catch (IOException e) {
						responseData.errorMsg = e.toString();
						responseData.status = Status.ERROR;
						return responseData;
					}
					request.addRequestProperty(
							"User-Agent",
							"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36 OPR/26.0.1656.60");
					request.addRequestProperty("Accept",
							"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
					request.addRequestProperty("Accept-Encoding",
							"gzip, deflate, lzma, sdch");
					request.addRequestProperty("Accept-Language",
							"en-US,en;q=0.8");
					if (cookieManager.getCookieStore().getCookies().size() > 0) {
						request.setRequestProperty(
								"Cookie",
								JoinCookie(";", cookieManager.getCookieStore()
										.getCookies()));
					}
					request.setConnectTimeout(15000);
					request.setReadTimeout(15000);
					request.setInstanceFollowRedirects(false);
					continue;
				}

				break;
			}
		}

		try {
			StringBuilder sb = new StringBuilder();
			int HttpResult = request.getResponseCode();
			if (HttpResult == HttpURLConnection.HTTP_OK) {
				InputStreamReader inStream = null;
				String contentEncoding = request.getContentEncoding();
				if (contentEncoding != null
						&& contentEncoding.toLowerCase(Locale.getDefault())
								.contains("gzip") == true) {
					inStream = new InputStreamReader(new GZIPInputStream(
							request.getInputStream()), "windows-1251");
				} else {
					inStream = new InputStreamReader(request.getInputStream(),
							"windows-1251");
				}

				BufferedReader br = new BufferedReader(inStream);
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
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
				responseData.responseStr = sb.toString();

				responseData.status = Status.SUCCESS;
			} else {
				responseData.errorMsg = request.getResponseMessage();
				responseData.status = Status.ERROR;
				return responseData;
			}
		} catch (IOException e) {
			responseData.errorMsg = e.toString();
			responseData.status = Status.ERROR;
			return responseData;
		}

		return responseData;
	}

	boolean ItIsARedirect(int retStatusCode) {
		boolean redirect = false;

		int status = retStatusCode;
		if (status != HttpURLConnection.HTTP_OK) {
			if (status == HttpURLConnection.HTTP_MOVED_TEMP
					|| status == HttpURLConnection.HTTP_MOVED_PERM
					|| status == HttpURLConnection.HTTP_SEE_OTHER)
				redirect = true;
		}

		return redirect;
	}

	static public HashMap<String, String> FindPairsInText(String inputText,
			String patternBlockStr, String patternPairStr) {
		HashMap<String, String> retPairs = new HashMap<String, String>();
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

	static public String Base36FromInt(long value) {
		String base36 = "0123456789abcdefghijklmnopqrstuvwxyz";

		String returnValue = "";
		do {
			int x = 0;
			x = (int) (value % ((long) base36.length()));
			char y = base36.charAt(x);
			returnValue = y + returnValue;
			value = value / 36;
		} while (value != 0);

		return returnValue;
	}

	private static String md5(String s) {
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
		String hashtext = bigInt.toString(16);
		// Now we need to zero pad it if you actually want the full 32 chars.
		while (hashtext.length() < 32) {
			hashtext = "0" + hashtext;
		}
		return hashtext;
	}

	public static String createFingerprint(HashMap<String, String> param1) {
		final Map<String, String> _loc_3 = new TreeMap<String, String>(
				new Comparator<String>() {
					@Override
					public int compare(String lhs, String rhs) {
						return lhs.compareTo(rhs);
					}
				});
		// HashMap<String, String> _loc_3 = new HashMap<String, String>();
		String _loc_4 = "";
		for (Entry<String, String> cvl : param1.entrySet()) {
			String _loc_6_key = cvl.getKey();
			String _loc_6_val = cvl.getValue();
			if (_loc_6_key.contains("X-Env") == true) {
				int index = 6;
				String _loc_2 = _loc_6_key.substring(index).toUpperCase(
						Locale.getDefault());
				_loc_3.put(_loc_2, _loc_6_val);
			}
		}

		for (Entry<String, String> cvl : _loc_3.entrySet()) {
			String _loc_6_key = cvl.getKey();
			String _loc_6_val = cvl.getValue();
			_loc_4 = _loc_4 + (_loc_6_key + "=" + _loc_6_val);
		}
		return _loc_4;
	}

	public static String StrPad(String param1, int param2, String param3,
			int param4) {
		if (param3 == null) {
			param3 = " ";
		}
		if (param4 == 0) {
			param4 = 1;
		}
		int _loc_6 = param4 & 1;
		int _loc_5 = param4 & 2;

		if (param3.length() > 1) {
			char firstChar = param3.charAt(0);
			param3 = "" + firstChar;
		} else {
			param3 = param3.length() == 0 ? " " : param3;
		}
		if (_loc_6 > 0 || _loc_5 > 0) {
			while (param1.length() < param2) {
				if (_loc_6 > 0) {
					param1 = param3 + param1;
				}
				if (param1.length() < param2 && _loc_5 > 0) {
					param1 = param1 + param3;
				}
			}
		}
		return param1;
	}

	public static String ChangeDateFormat(String dateString, String inFormat,
			String outFormat) {
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

	private String createAuthSignature(HashMap<String, Object> inData) {
		String postString = "";
		if (inData != null) {
			JSONObject currJsonData = new JSONObject(inData);
			postString = currJsonData.toString();
		}
		String _loc_3 = "";
		_loc_3 += m_sessionHeaderX.get("X-Request-Id");
		_loc_3 += ":";
		_loc_3 += m_auth_key;
		_loc_3 += ":";
		_loc_3 += m_sessionHeaderX.get("X-Auth-Session-Id");
		_loc_3 += ":";
		_loc_3 += postString;
		_loc_3 += ":";
		_loc_3 += createFingerprint(m_sessionHeaderX);

		return md5(_loc_3);
	}

	public static String CreateSig(String vkId,
			HashMap<String, Object> sendData, String vkSecret) {
		String currSig = vkId;
		TreeMap<String, String> cSendData = new TreeMap<String, String>();
		for (Entry<String, Object> inCKeyVal : sendData.entrySet()) {
			String inKey = inCKeyVal.getKey();
			String inVal = inCKeyVal.getValue().toString();
			cSendData.put(inKey, inVal);
		}

		for (Entry<String, String> inCKeyVal : cSendData.entrySet()) {
			String currKey = inCKeyVal.getKey();
			if (currKey == "sid" || currKey == "sig") {
				continue;
			}
			String currVal = inCKeyVal.getValue();
			currSig = currSig + (currKey + "=" + currVal);
		}
		currSig = currSig + vkSecret;
		currSig = md5(currSig);

		return currSig;
	}

	private String generateSessionKey() {
		Calendar currDate = Calendar.getInstance();
		int _loc_4 = (int) (currDate.getTimeInMillis() / 1000L);
		long _loc_5 = _loc_4 & 4294967295L;
		Random gen_rand = new Random();
		long _loc_2 = (gen_rand.nextLong() % 4294967295L) + 4294967294L;
		String _loc_1 = Base36FromInt(_loc_5);
		String _loc_3 = Base36FromInt(_loc_2);
		String sessionKey = StrPad(_loc_1, 7, "0", 1)
				+ StrPad(_loc_3, 7, "0", 1);

		return sessionKey;
	}

	public static String GetCurrTimeStr() {
		Calendar currDate = Calendar.getInstance();
		int currTime = (int) (currDate.getTimeInMillis() / 1000L);
		String retStr = String.valueOf(currTime);

		return retStr;
	}

	public static String GetRnd() {
		Random gen_rand = new Random();
		int curr_rnd = gen_rand.nextInt(10000);

		return String.valueOf(curr_rnd);
	}

	public static String JoinCookie(CharSequence delimiter,
			List<HttpCookie> list) {
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for (Object token : list) {
			if (firstTime) {
				firstTime = false;
			} else {
				sb.append(delimiter);
			}
			sb.append(token);
		}
		return sb.toString();
	}

	private ReturnData SendRecv(HashMap<String, Object> sendData) {
		if (m_requestId == 1) {
			m_sessionHeaderX.put("X-Auth-Session-Init", "1"); // only for first
		}
		m_sessionHeaderX.put("X-Request-Id", String.valueOf(m_requestId)); // increase
																			// it
																			// after
																			// each
																			// send
																			// 1,
																			// 2,
																			// 3...
		m_sessionHeaderX.put("X-Auth-Signature", createAuthSignature(sendData));
		ReturnData retDict = GetPost(m_site, "POST", sendData,
				m_sessionHeaderX, m_cookieManager, true, false);
		if (m_requestId == 1) {
			m_sessionHeaderX.remove("X-Auth-Session-Init");
		}
		m_requestId = m_requestId + 1;

		return retDict;
	}

	public static String ToJsonString(String str) {
		if (str == null) {
			return "";
		}
		return "" + str + "";
	}

	public static HashMap<String, Object> GenCall(String ident,
			HashMap<String, Object> args, String name) {
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("ident", ToJsonString(ident));
		retMap.put("args", args);
		if (name == null) {
			retMap.put("name", ToJsonString(ident));
		} else {
			retMap.put("name", ToJsonString(name));
		}

		return retMap;
	}
}
