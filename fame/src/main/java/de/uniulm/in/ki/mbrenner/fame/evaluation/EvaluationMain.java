package de.uniulm.in.ki.mbrenner.fame.evaluation;

import java.io.File;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class EvaluationMain {
	/**
	 * Main entry point
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		if(args.length <= 0){
			System.out.println("ERROR: No evaluation name provided");
			System.out.println("Usage: <programname> <test> -f <list of ontologies> -o <list of options>");
			System.out.println("available tests: rule-size, module-size, rand-time, rule-gen");
			System.exit(0);
		}
		EvaluationCase ec = null;
		if(args[0].equals("rule-size")){
			ec = new RuleSizeComparison();
		}
		else if(args[0].equals("module-size")){
			ec = new TestModuleSizes();
		}
		else if(args[0].equals("rand-time")){
			ec = new TestRandTimes();
		}
		else if(args[0].equals("rule-gen")){
			ec = new TestRuleGeneration();
		}
		else if(args[0].equals("current")){
			ec = new CurrentEvaluation();
		}
		
		List<File> ontologies = new LinkedList<>();
		List<String> options = new LinkedList<>();
		String[] instr = null;
		try{
			instr = getInStream();
		}
		catch(Exception e){
			System.out.println("WARNING: Error accessing stdin");
		}

		int mode = 0;
		if (args.length > 1){
			for(int i = 1; i < args.length; i++){
				if(args[i].equals("-f")){
					mode = 1;
				}
				else if(args[i].equals("-o")){
					mode = 2;
				}
				else if(mode == 1){
					ontologies.add(new File(args[i]));
				}
				else if(mode == 2){
					options.add(args[i]);
				}
			}
		}
		if(instr != null){
			for(int i = 0; i < instr.length; i++){
				if(instr[i].equals("")) continue;
				
				System.out.println("line " + i + " is " + instr[i]);
				if(instr[i].equals("-f")){
					mode = 1;
				}
				else if(instr[i].equals("-o")){
					mode = 2;
				}
				else if(mode == 1){
					ontologies.add(new File(instr[i]));
				}
				else if(mode == 2){
					options.add(instr[i]);
				}
			}
		}
		
		if(ontologies.size() == 0){
			System.out.println("WARNING: No ontologies provided");
		}
		
		try {
			ec.evaluate(ontologies, options);
		}
		catch(Exception e){
			System.out.println("evaluation failed");
			e.printStackTrace();
		}
	}

	private static String[] getInStream() throws Exception{
		InputStreamReader isr = new InputStreamReader(System.in);
		String s = "";
		char[] buffer = new char[100];
		if(isr.ready()){
			int read = isr.read(buffer);
			while(read > -1){
				for(int i = 0; i < read; i++){
					s += buffer[i];
				}
				read = isr.read(buffer);
			}
		}
		return s.split(" ");
	}
}
