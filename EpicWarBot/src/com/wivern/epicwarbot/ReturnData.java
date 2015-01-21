package com.wivern.epicwarbot;

import com.wivern.epicwarbot.EpicWarBot.Status;

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
