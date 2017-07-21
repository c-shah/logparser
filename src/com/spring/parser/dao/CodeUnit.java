package com.spring.parser.dao;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties({"parentCodeUnit"})
public class CodeUnit {
	public String summary;
	public String name;
	public int heap;
	public List<CodeUnitBlock> blocks;
	public List<CodeUnit> codeUnits;
	public List<Limit> limits;
	public String startTime;
	public String endTime;
	public CodeUnit parentCodeUnit;
	public CodeUnit() {
		blocks = new ArrayList<CodeUnitBlock>();
		codeUnits = new ArrayList<CodeUnit>();
		limits = new ArrayList<Limit>();
	}	
	public boolean isCompleted() {
		if( endTime == null || endTime == "" ) {
			return false;
		}
		return true;
	}
}
