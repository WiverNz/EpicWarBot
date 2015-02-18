package wivern.com.epicwarbot;

import wivern.com.epicwarbot.EpicWarBot.Status;

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
