package com.spring.parser.dao;

import java.util.List;

public class Line {
	
	public enum LineType {HEADER, VALIDATION_FAIL, DML_BEGIN, DML_END, FLOW_START_INTERVIEWS_BEGIN, FLOW_START_INTERVIEW_BEGIN, FLOW_START_INTERVIEW_END, FLOW_START_INTERVIEWS_END, CONSTRUCTOR_ENTRY, WF_RULE_EVAL_BEGIN, WF_RULE_EVAL_END, WF_FORMULA, WF_RULE_NOT_EVALUATED, CONSTRUCTOR_EXIT, CUMULATIVE_LIMIT_USAGE_END, SYSTEM_METHOD_ENTRY, SYSTEM_METHOD_EXIT, METHOD_ENTRY, USER_INFO, WF_FLOW_ACTION_BEGIN, WF_FLOW_ACTION_END, WF_CRITERIA_BEGIN, USER_DEBUG, WF_RULE_FILTER, WF_SPOOL_ACTION_BEGIN, WF_ACTION, WF_ACTIONS_END, WF_CRITERIA_END, WF_RULE_EVAL_VALUE, EXECUTION_STARTED, FLOW_CREATE_INTERVIEW_BEGIN, FLOW_CREATE_INTERVIEW_END, EXECUTION_FINISHED, CODE_UNIT_STARTED, CODE_UNIT_FINISHED, HEAP_ALLOCATE, VARIABLE_ASSIGNMENT, VARIABLE_SCOPE_BEGIN, STATEMENT_EXECUTE, CUMULATIVE_LIMIT_USAGE, LIMIT_USAGE_FOR_NS, VALIDATION_PASS, VALIDATION_RULE, VALIDATION_FORMULA, METHOD_EXIT, SOQL_EXECUTE_BEGIN, SOQL_EXECUTE_END};
	
	public boolean isForkType() {
		if( 	lineType == LineType.EXECUTION_STARTED || 
				lineType == LineType.CODE_UNIT_STARTED ||
				lineType == LineType.SOQL_EXECUTE_BEGIN ||
				lineType == LineType.WF_CRITERIA_BEGIN ||
				lineType == LineType.WF_FLOW_ACTION_BEGIN ||
				lineType == LineType.CUMULATIVE_LIMIT_USAGE ||
				lineType == LineType.METHOD_ENTRY ||
				lineType == LineType.SYSTEM_METHOD_ENTRY ||
				lineType == LineType.CONSTRUCTOR_ENTRY ||
				lineType == LineType.WF_RULE_EVAL_BEGIN ||		
				lineType == LineType.FLOW_START_INTERVIEWS_BEGIN ||
				lineType == LineType.FLOW_START_INTERVIEW_BEGIN ||
				lineType == LineType.FLOW_CREATE_INTERVIEW_BEGIN ) {
			return true;
		}
		return false;
	}
	
	public boolean isMergeType() {
		if( 	lineType == LineType.EXECUTION_FINISHED || 
				lineType == LineType.CODE_UNIT_FINISHED ||
				lineType == LineType.SOQL_EXECUTE_END ||
				lineType == LineType.WF_CRITERIA_END ||
				lineType == LineType.WF_FLOW_ACTION_END ||
				lineType == LineType.CUMULATIVE_LIMIT_USAGE_END || 
				lineType == LineType.METHOD_EXIT ||
				lineType == LineType.SYSTEM_METHOD_EXIT ||
				lineType == LineType.CONSTRUCTOR_EXIT ||	
				lineType == LineType.WF_RULE_EVAL_END ||				
				lineType == LineType.FLOW_START_INTERVIEWS_END ||
				lineType == LineType.FLOW_START_INTERVIEW_END ||
				lineType == LineType.FLOW_CREATE_INTERVIEW_END ) {
			return true;
		}
		return false;
	}
	
	public static LineType findLineType(List<String> attributes) {
		if( attributes != null && attributes.size() >= 1 ) {
			String typeAttribute = attributes.get(1);
			for (LineType lineType : LineType.values()) {
				if( lineType.toString().equalsIgnoreCase(typeAttribute) ) {
					return lineType;
				}
			}
		}
		return null;
	}
	
	public LineType lineType;
	public List<String> attributes;
	public String rawData;
	public String text;
}
