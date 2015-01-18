package com.wivern.epicwarbot;

import java.util.HashMap;

public class AnswerInfo {
	public String	szInfo;
	public String	szStatus;
	public boolean	bError;
	public String	szErrorMsg;
	public HashMap<String, String> hmRetValues;
	AnswerInfo()
	{
		szInfo		= "";
		szStatus	= "";
		bError		= false;
		szErrorMsg	= "";
		hmRetValues	= new HashMap<String, String>();
	}
	AnswerInfo(String info, String status, boolean error, String errorMsg)
	{
		hmRetValues	= new HashMap<String, String>();
		Set(info, status, error, errorMsg);
	}
	public void Set(String info, String status, boolean error, String errorMsg)
	{
		szInfo		= info;
		szStatus	= status;
		bError		= error;
		szErrorMsg	= errorMsg;
	}
	public void AddValue(String key, String value)
	{
		hmRetValues.put(key, value);
	}
}