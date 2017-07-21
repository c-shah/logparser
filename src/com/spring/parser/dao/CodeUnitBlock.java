package com.spring.parser.dao;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"parentBlock", "summary"})
public class CodeUnitBlock {
	public String summary;
	public enum BlockType { VALIDATION_RULE, WORKFLOW, PROCESS_FLOW, APEX_METHOD } 
	public BlockType blockType;
	public String startTime;
	public String endTime;
	public String name;
	public String id;
	public int heap;
	public CodeUnitBlock parentBlock; 
	public List<CodeUnitBlock> blocks;
	public List<CodeUnitBlock> soqls;	// only applicable to APEX METHOD, but i didn't want to sub class and introduce polyM just yet.
	public List<String> debugs;
	public String workflowEvaluationCriteria;
	
	public CodeUnitBlock() {
		blocks = new ArrayList<CodeUnitBlock>();
		soqls  = new ArrayList<CodeUnitBlock>();
		debugs = new ArrayList<String>();
	}
	
	public boolean isCompleted() {
		if( endTime == null || endTime == "" ) {
			return false;
		}
		return true;
	}
	public static String friendlyBlockType(BlockType blockType) {
		String input = blockType.toString().toLowerCase().replace("_", " ");
	    StringBuilder titleCase = new StringBuilder();
	    boolean nextTitleCase = true;

	    for (char c : input.toCharArray()) {
	        if (Character.isSpaceChar(c)) {
	            nextTitleCase = true;
	        } else if (nextTitleCase) {
	            c = Character.toTitleCase(c);
	            nextTitleCase = false;
	        }

	        titleCase.append(c);
	    }
	    return titleCase.toString();		
	}
}
