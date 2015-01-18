package com.wivern.epicwarbot;

import java.lang.ref.WeakReference;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class LooperThread extends AsyncTask<Void, Void, Void> {
	private MHandler m_handler;
	private EpicWarBot m_epicBot;
	public final static int MSG_ID_CONNECT		= 1;
	public final static int MSG_ID_DISCONNECT	= 2;
	public final static int MSG_ID_TIMER1		= 3;
	private WeakReference<MainActivity> m_wrActivity;

	LooperThread(MainActivity mainActivity) {
		m_wrActivity = new WeakReference<MainActivity>(mainActivity);
		m_epicBot = new EpicWarBot();
	}

	private void SendResponseMsg(Message msg) {
		if (m_wrActivity != null) {
			MainActivity mainActivity = m_wrActivity.get();
			if (mainActivity != null) {
				Handler handlerActivity = mainActivity.GetHandler();
				if (handlerActivity != null) {
					handlerActivity.sendMessage(msg);
				}
			}
		}
	}

	private void SendResponse(int msgId, String funcInfo, AnswerInfo ainfo) {
		Bundle sdata = new Bundle();
		sdata.putString("funcInfo", funcInfo);
		sdata.putString("status", ainfo.szStatus);
		sdata.putBoolean("error", ainfo.bError);
		if (ainfo.bError == true) {
			sdata.putString("errorMsg", ainfo.szErrorMsg);
		}
		sdata.putString("info", ainfo.szInfo);
		for (Entry<String, String> cVal : ainfo.hmRetValues.entrySet()) {
			sdata.putString(cVal.getKey(), cVal.getValue());
		}
		Message msg = new Message();
		msg.what = msgId;
		if (sdata != null) {
			msg.setData(sdata);
		}
		SendResponseMsg(msg);
	}

	protected void finalize() {
		if (m_handler != null)
			m_handler.removeCallbacksAndMessages(null);
	}

	public void SendMessage(int msg_id, Bundle sdata) {
		Message msg = new Message();
		msg.what = msg_id;
		if (sdata != null) {
			msg.setData(sdata);
		}
		m_handler.sendMessage(msg);
	}

	@SuppressLint("HandlerLeak")
	@Override
	protected Void doInBackground(Void... params) {
		Looper.prepare();
		m_handler = new MHandler(); // All magic here
		Looper.loop();
		return null;
	}

	@SuppressLint("HandlerLeak")
	public class MHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			AnswerInfo ainfo = null;
			switch (msg.what) {
			case MSG_ID_CONNECT:
				Bundle sdata = msg.getData();
				if (sdata != null) {
					String vkLogin = sdata.getString("login");
					String vkPassword = sdata.getString("password");
					if (vkLogin != null && vkPassword != null) {
						ainfo = m_epicBot.VKConnect(vkLogin, vkPassword);
						if (ainfo.bError == true) {
							SendResponse(msg.what, "VKConnect", ainfo);
						} else {
							SendResponse(msg.what, "VKConnect", ainfo);
							ainfo = m_epicBot.GameConnect();
						}
					}
				}
				break;
			case MSG_ID_DISCONNECT:
				ainfo = m_epicBot.VKDisconnect();
				SendResponse(msg.what, "VKDisconnect", ainfo);
				break;
			case MSG_ID_TIMER1:
				//m_epicBot.CollectResources();
				break;
			}
			return;
		}
	}
}
