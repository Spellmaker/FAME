package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.uniulm.in.ki.mbrenner.fame.evaluation.unused.RuleSizeComparison;
import de.uniulm.in.ki.mbrenner.fame.evaluation.utility.CurrentEvaluation;
import de.uniulm.in.ki.mbrenner.fame.evaluation.utility.ModulePreparation;
import de.uniulm.in.ki.mbrenner.fame.evaluation.utility.TestCorrectness;
import de.uniulm.in.ki.mbrenner.fame.evaluation.utility.TestIncrCorrectness;
import de.uniulm.in.ki.mbrenner.fame.util.DevNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Permission;
import java.util.LinkedList;
import java.util.List;

public class EvaluationMain {
	public static PrintStream out = System.out;
	public static int min_size = -1;
	public static int max_size = Integer.MAX_VALUE;

	private static class ExitTrappedException extends SecurityException { }

	private static void forbidSystemExitCall() {
		final SecurityManager securityManager = new SecurityManager() {
			public void checkPermission( Permission permission ) {
				if(permission.getName().startsWith("exitVM")) {
					throw new ExitTrappedException() ;
				}
			}
		} ;
		System.setSecurityManager( securityManager ) ;
	}

	private static void enableSystemExitCall() {
		System.setSecurityManager( null ) ;
	}

	private static boolean hasArg(String[] array, String arg){
		for(String s : array){
			if(arg.equals(s)) return true;
		}
		return false;
	}

	/**
	 * Main entry point
	 * @param args command line arguments
	 */
	public static void main(String[] args) throws Throwable{
		System.setOut(new DevNull());
		if(args.length <= 0){
			out.println("ERROR: No evaluation name provided");
			out.println("Usage: <programname> <test> (-f <list of ontologies> | -d <directory>) -o <list of options>");
			out.println("available tests: rule-size, module-size, rand-time, rule-gen");
			System.exit(0);
		}
		//needs to be done, as some third party algorithms use system.exit
		forbidSystemExitCall();
		List<EvaluationCase> ec = new LinkedList<>();

		if(hasArg(args, "rule-size")){
			ec.add(new RuleSizeComparison());
		}
		else if(hasArg(args, "module-size")){
			ec.add(new TestModuleSizes());
		}
		else if(hasArg(args, "rand-time")){
			ec.add(new TestRandTimes());
		}
		else if(hasArg(args, "rule-gen")){
			ec.add(new TestRuleGeneration());
		}
		else if(hasArg(args, "current")){
			ec.add(new CurrentEvaluation());
		}
		else if(hasArg(args, "correctness")){
			ec.add(new TestCorrectness());
		}
		else if(hasArg(args, "prepare")){
			ec.add(new ModulePreparation());
		}
		else if(hasArg(args, "incrcorrectness")){
			ec.add(new TestIncrCorrectness());
		}
		else if(hasArg(args, "incrtime")){
			ec.add(new TestIncrementalTime());
		}
		else if(hasArg(args, "memory")){
			ec.add(new MemoryTest());
		}
		else if(hasArg(args, "extraction")){
			ec.add(new TestModuleExtraction());
		}
		else if(hasArg(args, "extraction-hys")){
			ec.add(new HySModuleExtractionTest());
		}
		else if(hasArg(args, "extraction-incr")){
			ec.add(new SingleIncrModuleExtractionTest());
		}
		else if(hasArg(args, "rule-gen-hys")){
			ec.add(new HySRuleGenerationTest());
		}

		List<File> ontologies = new LinkedList<>();
		List<String> options = new LinkedList<>();
		String[] instr = null;
		try{
			instr = getInStream();
		}
		catch(Exception e){
			out.println("WARNING: Error accessing stdin");
		}

		int mode = 0;
		List<String> directory = new LinkedList<>();
		if (args.length > 1){
			for(int i = 1; i < args.length; i++){
				if(args[i].equals("-f")){
					mode = 1;
				}
				else if(args[i].equals("-o")){
					mode = 2;
				}
				else if(args[i].equals("-d")){
					mode = 3;
				}
				else if(args[i].equals("-m")){
					min_size = Integer.parseInt(args[i + 1]);
					max_size = Integer.parseInt(args[i + 2]);
					i += 2;
				}
				else if(mode == 1){
					ontologies.add(new File(args[i]));
				}
				else if(mode == 2){
					options.add(args[i]);
				}
				else if(mode == 3){
					directory.add(args[i]);
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
				else if(instr[i].equals("-d")){
					mode = 3;
				}
				else if(mode == 1){
					ontologies.add(new File(instr[i]));
				}
				else if(mode == 2){
					options.add(instr[i]);
				}
				else if(mode == 3){
					directory.add(instr[i]);
				}
			}
		}

		for(String d : directory){
			try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(d))){
				for(Path path : directoryStream){
					ontologies.add(path.toFile());
				}
			}
			catch(IOException e){
				out.println("ERROR: ERROR while accessing directory " + d);
			}
		}

		if(ontologies.size() == 0){
			out.println("WARNING: No ontologies provided");
		}

		for(EvaluationCase c : ec) {
			try {
				c.evaluate(ontologies, options);
			}
			catch(Throwable e){
				out.println("evaluation with " + c.getClass().getName() + " failed");
				e.printStackTrace();
			}
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
