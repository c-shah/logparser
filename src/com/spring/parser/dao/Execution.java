package com.spring.parser.dao;

import java.util.ArrayList;
import java.util.List;

public class Execution {
	public List<CodeUnit> codeUnits;
	public String startTime;
	public String endTime;
	public UserInfo userInfo;
	public Execution() {
		codeUnits = new ArrayList<CodeUnit>();
	}	
	public boolean isCompleted() {
		if( endTime == null || endTime == "" ) {
			return false;
		}
		return true;
	}
}
