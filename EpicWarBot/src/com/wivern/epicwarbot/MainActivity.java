package com.wivern.epicwarbot;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener {
	Button m_btnConnect;
	Button m_btnDisconnect;
	EditText m_vkLogin;
	EditText m_vkPassword;
	LooperThread m_engineLooper;
	private MHandler m_handler;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		m_btnConnect = (Button) this.findViewById(R.id.btnConnect);
		m_btnConnect.setOnClickListener(this);
		m_btnDisconnect = (Button) this.findViewById(R.id.btnDisconnect);
		m_btnDisconnect.setOnClickListener(this);
		m_vkLogin = (EditText) this.findViewById(R.id.etVkLogin);
		m_vkPassword = (EditText) this.findViewById(R.id.etVkPassword);
		m_vkLogin.setText("13602098361");
		m_vkPassword.setText("");
		m_handler = new MHandler(this);
		m_engineLooper = new LooperThread(this);
		m_engineLooper.execute();
	}

	@SuppressLint("HandlerLeak")
	public class MHandler extends Handler {
		MainActivity m_activity;

		MHandler(MainActivity ma) {
			m_activity = ma;
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (m_activity != null) {

				switch (msg.what) {
				case LooperThread.MSG_ID_CONNECT:
					Bundle sdata = msg.getData();
					if (sdata != null) {
						String funcInfo = sdata.getString("funcInfo");
						String info = sdata.getString("info");
						boolean ferror = sdata.getBoolean("error");
						if (ferror == true) {
							String errorMsg = sdata.getString("errorMsg");

							Toast.makeText(m_activity, errorMsg,
									Toast.LENGTH_LONG).show();
						} else {
							if (funcInfo != null) {
								Toast.makeText(m_activity,
										funcInfo + ": " + info,
										Toast.LENGTH_SHORT).show();
							}
						}
					}

					break;
				}
			}
		}

	}

	public Handler GetHandler() {
		return m_handler;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnConnect:
			if (m_engineLooper != null) {
				Bundle sdata = new Bundle();
				sdata.putString("login", m_vkLogin.getText().toString());
				sdata.putString("password", m_vkPassword.getText().toString());
				m_engineLooper.SendMessage(LooperThread.MSG_ID_CONNECT, sdata);
			}
			break;
		case R.id.btnDisconnect:
			if (m_engineLooper != null) {
				m_engineLooper
						.SendMessage(LooperThread.MSG_ID_DISCONNECT, null);
			}
			break;
		}

	}
}
