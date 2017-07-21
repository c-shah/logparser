package com.spring.parser.engine;

import java.util.List;
import java.util.Set;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;
import com.spring.parser.dao.CodeUnit;
import com.spring.parser.dao.CodeUnitBlock;
import com.spring.parser.dao.Execution;
import com.spring.parser.dao.ExecutionSuite;
import com.spring.parser.dao.Limit;
import com.spring.parser.dao.Line;
import com.spring.parser.dao.Line.LineType;
import com.spring.parser.dao.UserInfo;

public class ParserEngine {
	
	public static final String APEX_LOG_REGEX = "apex.*.log";
	public static Set<String> unprocessedLineTypes = new HashSet<String>();
	public static Set<String> unrecognizedStrings = new HashSet<String>();
	
	public static void main(String args[]) throws Exception {
		String inputDir = "C:\\Users\\cshah100\\Downloads";
		String outputDir = "C:\\Users\\cshah100\\Downloads";
		processDirectory(inputDir, outputDir);
	}
	
	public static void processDirectory(String inputDir, String outputDir) throws Exception {
		processFiles( listFilesMatching( new File(inputDir) , APEX_LOG_REGEX ) , outputDir );
	}
	
	public static void processFiles(List<File> inputFiles, String outputDir) throws Exception {
		for(File inputFile : inputFiles ) {
			processFile(inputFile, outputDir);
		}
	}
	
	public static void processFile(File inputFile, String outputDir) throws Exception {
		System.out.println(" processing " + inputFile );
		List<String> lineStrings = new ArrayList<String>();
		List<Line> lines = new ArrayList<Line>();
		try (BufferedReader br = Files.newBufferedReader(Paths.get(inputFile.toURI()))) {
			lineStrings = br.lines().collect(Collectors.toList());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		processLines(lineStrings, lines);
		ExecutionSuite executionSuite = new  ExecutionSuite();
		buildExecutionSuite(lines, executionSuite, null, null, null);
		summarizeExecutionSuite(executionSuite);
		String fileName = outputDir + File.separator + inputFile.getName() + ".json";
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);		
		mapper.enable(SerializationFeature.INDENT_OUTPUT);			
		mapper.writeValue( new File(fileName), executionSuite);
		//printLines(lines, 1);
	}
	
	private static void summarizeExecutionSuite(ExecutionSuite executionSuite) {
		for(Execution execution : executionSuite.executions ) {
			for(CodeUnit codeUnit : execution.codeUnits ) {
				summarizeCodeUnit(codeUnit);
			}
		}
	}
	
	private static String newLine = "NEWLINE";
	
	private static void summarizeCodeUnit(CodeUnit codeUnit) {
		if( codeUnit != null ) {
			if( codeUnit.codeUnits != null && codeUnit.codeUnits.size() > 0  ) {
				for(CodeUnit subCodeUnit : codeUnit.codeUnits ) {
					summarizeCodeUnit( subCodeUnit );
				}
			} else {
				for(CodeUnitBlock block : codeUnit.blocks ) {
					summarizeCodeUnitBlock( block, 0 );
				}
				int validationRuleCount = 0;
				int workflowRuleCount = 0;
				int processFlowCount = 0;
				for(CodeUnitBlock block : codeUnit.blocks ) {
					if( block.blockType == CodeUnitBlock.BlockType.VALIDATION_RULE ) {
						validationRuleCount++;
					} else if( block.blockType == CodeUnitBlock.BlockType.WORKFLOW ) {
						workflowRuleCount++;
					} else if( block.blockType == CodeUnitBlock.BlockType.PROCESS_FLOW ) {
						processFlowCount++;
					}
				}
				
				codeUnit.summary = codeUnit.name;
				if( validationRuleCount > 0 ) {
					codeUnit.summary += newLine + "validation rules : " +  validationRuleCount;
				}
				if( workflowRuleCount > 0 ) {
					codeUnit.summary += newLine + "workflows : " +  workflowRuleCount;
				}
				if( processFlowCount > 0 ) {
					codeUnit.summary += newLine + "process flows : " +  processFlowCount;
				}
				codeUnit.summary += newLine + newLine ;
				for(CodeUnitBlock block : codeUnit.blocks ) {
					codeUnit.summary += block.summary + newLine;
				}
			}
		}
	}
	
	private static void summarizeCodeUnitBlock(CodeUnitBlock codeUnitBlock, int depth) {
		if( codeUnitBlock.blocks != null &&  codeUnitBlock.blocks.size() > 0 ) {
			for(CodeUnitBlock subcodeUnitBlock : codeUnitBlock.blocks ) {
				summarizeCodeUnitBlock(subcodeUnitBlock, depth+1);
			}
		}
		codeUnitBlock.summary = getSpace(depth) + CodeUnitBlock.friendlyBlockType( codeUnitBlock.blockType ) +  " : " + codeUnitBlock.name;
		if( codeUnitBlock.blocks != null &&  codeUnitBlock.blocks.size() > 0 ) {
			for(CodeUnitBlock subcodeUnitBlock : codeUnitBlock.blocks ) {
				codeUnitBlock.summary +=  newLine + getSpace(depth+1) +  subcodeUnitBlock.summary;
			}
		}
	}

	private static void validateCurrentCodeUnitBlockIsAlive(CodeUnitBlock currentCodeUnitBlock, String newLine) throws Exception {
		if( currentCodeUnitBlock == null && currentCodeUnitBlock.isCompleted() == true ) {
			throw new Exception(" No active Current Code Block Unit " + currentCodeUnitBlock.name + " newLine " + newLine );
		}
	}

	private static void validateCurrentCodeUnitBlockIsDead(CodeUnitBlock currentCodeUnitBlock, String newLine) throws Exception {
		if( currentCodeUnitBlock != null && currentCodeUnitBlock.isCompleted() == false ) {
			throw new Exception(" Current Code Block Unit is not yet completed " + currentCodeUnitBlock.blockType  + " " + currentCodeUnitBlock.name + " newLine " + newLine );
		}
	}
	
	private static void validateCurrentCodeUnitIsAlive(CodeUnit currentCodeUnit, String newLine) throws Exception {
		if( currentCodeUnit == null || currentCodeUnit.isCompleted() ) {
			throw new Exception(" No active code unit to work on " + " newLine " + newLine );
		}
	}

	private static void validateCurrentExecutionIsAlive(Execution execution, String newLine) throws Exception {
		if( execution == null || execution.isCompleted() ) {
			throw new Exception(" No active execution to work on " + " newLine " + newLine );
		}
	}

	private static void validateCurrentExecutionIsDead(Execution execution, String newLine) throws Exception {
		if( execution != null && !execution.isCompleted() ) {
			throw new Exception(" Current Execution is not yet completed. Execution Hiearchy is not yet supported. ");
		}
	}

	private static void buildExecutionSuite(List<Line> lines, ExecutionSuite executionSuite, Execution currentExecution, CodeUnit currentCodeUnit, CodeUnitBlock currentCodeUnitBlock) throws Exception {
		UserInfo currentUserInfo = null;
		
		for(Line line : lines ) {
			try {
				
				if( line.lineType == Line.LineType.USER_INFO ) {
					currentUserInfo = new UserInfo();
					currentUserInfo.id = line.attributes.get(3);
					currentUserInfo.userName = line.attributes.get(4);
				}
				
				else if( line.lineType == Line.LineType.EXECUTION_STARTED ) {
					validateCurrentExecutionIsDead(currentExecution, line.rawData);
					currentExecution = new Execution();
					executionSuite.executions.add( currentExecution );
					currentExecution.userInfo = currentUserInfo;
					currentExecution.startTime =  line.attributes.get(0);
				}
				
				else if( line.lineType == Line.LineType.EXECUTION_FINISHED ) {
					validateCurrentExecutionIsAlive(currentExecution, line.rawData);
					currentExecution.endTime =  line.attributes.get(0);
				}

				else if( line.lineType == Line.LineType.CODE_UNIT_STARTED ) {
					if( currentCodeUnit != null && currentCodeUnit.isCompleted() == false ) {
						CodeUnit parentCodeUnit = currentCodeUnit;
						currentCodeUnit = new CodeUnit();
						currentCodeUnit.parentCodeUnit = parentCodeUnit;
						parentCodeUnit.codeUnits.add( currentCodeUnit );
					} else {
						currentCodeUnit = new CodeUnit();
						currentExecution.codeUnits.add( currentCodeUnit );
					}
					currentCodeUnit.startTime =  line.attributes.get(0);
					currentCodeUnit.name = subStringAfter( line.rawData, "|" );
				}
				
				else if( line.lineType == Line.LineType.CODE_UNIT_FINISHED ) {
					validateCurrentCodeUnitIsAlive(currentCodeUnit, line.rawData);
					currentCodeUnit.endTime =  line.attributes.get(0);
					currentCodeUnit = currentCodeUnit.parentCodeUnit;
				}

				else if( line.lineType == Line.LineType.HEAP_ALLOCATE ) {
					validateCurrentCodeUnitIsAlive(currentCodeUnit, line.rawData);					
					currentCodeUnit.heap += Integer.parseInt(  subStringAfter( line.rawData, "Bytes:" )  );
					if( currentCodeUnitBlock != null && currentCodeUnitBlock.isCompleted() == false ) {
						currentCodeUnitBlock.heap += Integer.parseInt(  subStringAfter( line.rawData, "Bytes:" )  );
					}
				}

				else if( line.lineType == Line.LineType.LIMIT_USAGE_FOR_NS ) {
					validateCurrentCodeUnitIsAlive(currentCodeUnit, line.rawData);
					Limit limit = new Limit();
					currentCodeUnit.limits.add( limit );
					limit.limitNamespace = subStringAfter(line.rawData, "LIMIT_USAGE_FOR_NS|" );
				}

				else if( line.lineType == Line.LineType.METHOD_ENTRY || line.lineType == Line.LineType.SYSTEM_METHOD_ENTRY ) {
					validateCurrentCodeUnitIsAlive(currentCodeUnit, line.rawData);
					if( currentCodeUnitBlock != null && currentCodeUnitBlock.isCompleted() == false ) {
						CodeUnitBlock parentBlock = currentCodeUnitBlock;
						currentCodeUnitBlock = new CodeUnitBlock();
						currentCodeUnitBlock.parentBlock = parentBlock;
						parentBlock.blocks.add( currentCodeUnitBlock );
					} else {
						currentCodeUnitBlock = new CodeUnitBlock();
						currentCodeUnit.blocks.add( currentCodeUnitBlock );
					}
					currentCodeUnitBlock.blockType = CodeUnitBlock.BlockType.APEX_METHOD;
					currentCodeUnitBlock.startTime = line.attributes.get(0);
					if( line.lineType == Line.LineType.METHOD_ENTRY ) {
						currentCodeUnitBlock.id = line.attributes.get(3);
						currentCodeUnitBlock.name = line.attributes.get(4);
					} else {
						currentCodeUnitBlock.name = line.attributes.get(3);
					}
				}

				else if( line.lineType == Line.LineType.SOQL_EXECUTE_BEGIN ) {
					validateCurrentCodeUnitIsAlive(currentCodeUnit, line.rawData);
					validateCurrentCodeUnitIsAlive(currentCodeUnit, line.rawData);
					CodeUnitBlock parentBlock = currentCodeUnitBlock;
					currentCodeUnitBlock = new CodeUnitBlock();
					currentCodeUnitBlock.parentBlock = parentBlock;
					parentBlock.soqls.add( currentCodeUnitBlock );
					currentCodeUnitBlock.name = line.attributes.get(4);
					currentCodeUnitBlock.startTime = line.attributes.get(0);
				}
				
				else if( line.lineType == Line.LineType.VALIDATION_RULE ) {
					/** no hierarchy support - not needed */
					validateCurrentCodeUnitIsAlive(currentCodeUnit, line.rawData);
					currentCodeUnitBlock = new CodeUnitBlock();
					currentCodeUnit.blocks.add( currentCodeUnitBlock );
					currentCodeUnitBlock.blockType = CodeUnitBlock.BlockType.VALIDATION_RULE;
					currentCodeUnitBlock.startTime = line.attributes.get(0);
					currentCodeUnitBlock.id = line.attributes.get(2);
					currentCodeUnitBlock.name = line.attributes.get(3);
				}

				else if( line.lineType == Line.LineType.FLOW_START_INTERVIEW_BEGIN ) {
					/** no hierarchy support - not needed */
					validateCurrentCodeUnitIsAlive(currentCodeUnit, line.rawData);
					currentCodeUnitBlock = new CodeUnitBlock();
					currentCodeUnit.blocks.add( currentCodeUnitBlock );
					currentCodeUnitBlock.blockType = CodeUnitBlock.BlockType.PROCESS_FLOW;
					currentCodeUnitBlock.startTime = line.attributes.get(0);
					currentCodeUnitBlock.id = line.attributes.get(2);
					currentCodeUnitBlock.name = line.attributes.get(3);
				}
				
				else if( line.lineType == Line.LineType.WF_CRITERIA_BEGIN ) {
					/** no hierarchy support - not needed */
					validateCurrentCodeUnitIsAlive(currentCodeUnit, line.rawData);
					currentCodeUnitBlock = new CodeUnitBlock();
					currentCodeUnit.blocks.add( currentCodeUnitBlock );
					currentCodeUnitBlock.blockType = CodeUnitBlock.BlockType.WORKFLOW;
					currentCodeUnitBlock.startTime = line.attributes.get(0);
					currentCodeUnitBlock.id = line.attributes.get(4);
					currentCodeUnitBlock.name = line.attributes.get(3);
					currentCodeUnitBlock.workflowEvaluationCriteria = line.attributes.get(5);
				}

				else if( line.lineType == Line.LineType.SYSTEM_METHOD_EXIT || 
						line.lineType == Line.LineType.METHOD_EXIT || 
						line.lineType == Line.LineType.SOQL_EXECUTE_END || 
						line.lineType == Line.LineType.VALIDATION_PASS || 
						line.lineType == Line.LineType.VALIDATION_FAIL ||
						line.lineType == Line.LineType.FLOW_START_INTERVIEW_END ||
						line.lineType == Line.LineType.WF_RULE_NOT_EVALUATED ||
						line.lineType == Line.LineType.WF_CRITERIA_END
						) {
					validateCurrentCodeUnitIsAlive(currentCodeUnit, line.rawData);
					validateCurrentCodeUnitBlockIsAlive( currentCodeUnitBlock, line.rawData);
					currentCodeUnitBlock.endTime = line.attributes.get(0);
					currentCodeUnitBlock = currentCodeUnitBlock.parentBlock;
				}
				
				else {
					if( line.lineType != null ) {
						unprocessedLineTypes.add( line.lineType.toString() );
					} else {
						unrecognizedStrings.add( line.rawData );
					}
				}
				
				
			} catch(Exception e) {
				System.out.println(" error parsing line " + line.rawData );
				e.printStackTrace();
			}
		}
		System.out.println(" ---------------- " );
		System.out.println(" unprocessedLineTypes ");
		for(String lineType : unprocessedLineTypes ) {
			System.out.println(lineType);
		}
		System.out.println(" ---------------- " );
		System.out.println(" unrecognizedStrings ");
		for(String un : unrecognizedStrings ) {
			System.out.println(un);
		}
	}
	
	private static String subStringAfter(String input, String matchString) {
		if( input != null && input.contains(matchString) ) {
			return input.substring(  input.lastIndexOf(matchString) + matchString.length() );
		}
		return input;
	}
	
	private static void processLines(List<String> lineStrings, List<Line> lines) throws Exception {
		Line previousLine = null;
		while(lineStrings.size() > 0) {
			String lineString = popLine(lineStrings);
			Line line = parseLine(lineString);
			lines.add( line );
		}
	}
	
	private static String getSpace(int length) {
		String space = "  ";
		for(int i=0;i<length;i++) {
			space += "  ";
		}
		return space;
	}
	
	private static void printLines(List<Line> lines) {
		if( lines != null && lines.size() > 0 ) {
			for(Line line: lines) {
				if( line.lineType == null ) {
					System.out.println(  "EXCEPTION " + line.lineType + " " +  line.rawData );
				} else {
					System.out.println( line.lineType + " " +  line.rawData );
				}
			}
		}
	}
	
	private static Line parseLine(String lineString) throws Exception {
		Line line = new Line();
		line.rawData = lineString.trim();
		if( line.rawData.contains("|") ) {
			line.attributes = Arrays.asList( lineString.split("\\|") );
			line.lineType = Line.findLineType(line.attributes);
		}
		return line;
	}
	
	private static String popLine(List<String> lineStrings) throws Exception {
		if( lineStrings.size() > 0 ) {
			String lineString = lineStrings.get(0);
			lineStrings.remove(0);	/* pass by reference, so should be fine */
			return lineString;
		}
		return null;
	}

	/**
	 * @param root
	 * @param regex
	 * @return
	 */
	public static List<File> listFilesMatching(File root, String regex) {
	    if(!root.isDirectory()) {
	        throw new IllegalArgumentException(root+" is no directory.");
	    }
	    final Pattern p = Pattern.compile(regex); // careful: could also throw an exception!
	    File[] files = root.listFiles(new FileFilter(){
	        @Override
	        public boolean accept(File file) {
	            return p.matcher(file.getName()).matches();
	        }
	    });
	    return (List<File>) Arrays.asList(files);
	}	
	
}
