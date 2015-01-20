package com.wivern.epicwarbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
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

import org.json.JSONObject;

import android.util.Log;

public class EpicWarBot {
	public class GiftInfo {
		public String userId;
		public List<String> ids_arr;
	}

	public enum Status {
		NOTINIT, SUCCESS, ERROR
	}

	public class ReturnData {
		public String errorMsg;
		public Status status;
		public String responseStr;

		ReturnData() {
			errorMsg = "";
			status = Status.NOTINIT;
			responseStr = "";
		}
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
	private java.net.CookieManager m_cookieManager;

	private boolean m_vkConnected;
	private boolean m_gameConnected;

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

		ReturnData retDict = GetPost(urlPath, "GET", cSendData, null, false,
				false);
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

		ReturnData retDict = GetPost(urlPath, "GET", cSendData, null, false,
				false);
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

		ReturnData retDict = GetPost(urlPath, "GET", cSendData, null, false,
				false);
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
				retDict = GetPost(iUrl, "GET", cSendData, cSendHeaders, false,
						false);
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
						SendRecvFirstData();
					}
				}
			}

		}
		return retResult;
	}

	private void SendRecvFirstData()
	{
        HashMap<String, Object> formSendData = new HashMap<String, Object>();
        String cCode = "return{\"user\":API.getProfiles({\"https\":0,\"uids\":" + m_vkId + ",\"fields\":\"can_post,uid,first_name,last_name,nickname,sex,bdate,photo,photo_medium,photo_big,has_mobile,rate,city,country,photo_max_orig\"}),\"friends\":API.friends.get({\"https\":0,\"count\" : 500, \"fields\":\"uid,country,first_name,last_name,photo,photo_medium,photo_big,sex,can_post,bdate,online,photo_max_orig\"}),\"appFriends\":API.getAppFriends(),\"groups\":API.getGroups()};";
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
        ReturnData retDictForm = GetPost(cSite, "POST", formSendData, null, false, true);
		m_gameConnected = true;
	}
	
	protected ReturnData GetPost(String urlString, String typePostGet,
			HashMap<String, Object> inData, HashMap<String, String> headers,
			boolean flJSON, boolean flForm) {
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
			// Proxy proxy = new Proxy(Proxy.Type.HTTP, new
			// InetSocketAddress("192.168.0.4", 8888));
			request = (HttpURLConnection) (reqURL.openConnection());
		} catch (IOException e) {
			responseData.errorMsg = e.toString();
			responseData.status = Status.ERROR;
			return responseData;
		}
		request.setInstanceFollowRedirects(autoRedirect);
		Log.d(LOG_PREF, "Using proxy: " + request.usingProxy());
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
		if (m_cookieManager.getCookieStore().getCookies().size() > 0) {
			request.setRequestProperty("Cookie", JoinCookie(";",
					m_cookieManager.getCookieStore().getCookies()));
		}
		
		if (typePostGet.toLowerCase(Locale.getDefault()).contentEquals(
				"post") == true) {

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
							m_cookieManager.getCookieStore().add(null,
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
						request = (HttpURLConnection) nextURL.openConnection();
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
					if (m_cookieManager.getCookieStore().getCookies().size() > 0) {
						request.setRequestProperty("Cookie", JoinCookie(
								";", m_cookieManager.getCookieStore()
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
						m_cookieManager.getCookieStore().add(null,
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

		// var responseStr:NSString = NSString(data:urlData!,
		// encoding:NSASCIIStringEncoding)!
		// responseData["responseStr"] = responseStr;
		//
		// // NSLog("Response ==> %@", responseStr)
		//
		// if (res.statusCode >= 200 && res.statusCode < 300 && (flJSON ||
		// flForm))
		// {
		// var error: NSError?
		// let jsonData:NSDictionary =
		// NSJSONSerialization.JSONObjectWithData(urlData!,
		// options:NSJSONReadingOptions.MutableContainers , error: &error) as
		// NSDictionary
		// responseData["responseJson"] = jsonData
		//
		// } else {
		// // NSLog("res.statusCode < 200 || res.statusCode >= 300");
		// }
		// } else {
		// NSLog("urlData == nil");
		// }

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
		try {
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";

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
	
	public static String GetRnd()
	{
		Random gen_rand = new Random();
		int curr_rnd = gen_rand.nextInt(10000);
		
		return String.valueOf(curr_rnd);
	}
	
    public static String JoinCookie(CharSequence delimiter, List<HttpCookie> list) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token: list) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }
}
