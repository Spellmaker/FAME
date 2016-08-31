package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.SingleLevelEvaluationCase;
import de.uniulm.in.ki.mbrenner.fame.evaluation.unused.RuleSizeComparison;
import de.uniulm.in.ki.mbrenner.fame.evaluation.utility.*;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.SearchCombinedCase;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class EvaluationMain {
	public static PrintStream out = System.out;
	public static int min_size = -1;
	public static int max_size = Integer.MAX_VALUE;

	private static class ExitTrappedException extends SecurityException { }

	public static final Path outPath = Paths.get("fameout");

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
		List<EvaluationCase> availableCases = new LinkedList<>();
		availableCases.add(new RuleSizeComparison());
		availableCases.add(new TestModuleSizes());
		availableCases.add(new TestRandTimes());
		availableCases.add(new TestRuleGeneration());
		availableCases.add(new CurrentEvaluation());
		availableCases.add(new TestCorrectness());
		availableCases.add(new ModulePreparation());
		availableCases.add(new TestIncrCorrectness());
		availableCases.add(new TestIncrementalTime());
		availableCases.add(new MemoryTest());
		availableCases.add(new TestModuleExtraction());
		availableCases.add(new HySModuleExtractionTest());
		availableCases.add(new SingleIncrModuleExtractionTest());
		availableCases.add(new HySRuleGenerationTest());
		availableCases.add(new MergeResults());
		availableCases.add(new SingleLevelEvaluationCase<>(new OntoStatFactory(), "stats", true));
		availableCases.add(new DefinitionEvaluation());
		availableCases.add(new SingleLevelEvaluationCase<>(new DefinitionTimeComparison(), "def-time", true));
		availableCases.add(new SingleLevelEvaluationCase<>(new DefinitionSizeComparison(), "def-size", true));
		availableCases.add(new SingleLevelEvaluationCase<>(new DefinitionCorrectness(), "def-correctness", true));
		availableCases.add(new SingleLevelEvaluationCase<>(new TestCompatibility(), "count", false));
		availableCases.add(new SingleLevelEvaluationCase<>(new SearchCombinedCase(), "combinedsearch", true));
		availableCases.add(new SingleLevelEvaluationCase<>(new InfoWorker(), "info", false));
		availableCases.add(new GeneticEvaluation());

		System.setOut(new DevNull());
		if(args.length <= 0){
			out.println("ERROR: No evaluation name provided");
			out.println("Usage: <programname> <test> (-f <list of ontologies> | -d <directory>) -o <list of options>");
			Iterator<EvaluationCase> iter = availableCases.iterator();
			String available = "available: " + iter.next().getParameter();
			while(iter.hasNext()) available += ", " + iter.next().getParameter();
			out.println(available);
			out.println("Use <programname> help <test> for more information about a specific test case");
			System.exit(0);
		}

		if(args[0].equals("help")){
			if(args.length < 2){
				out.println("Usage: <programname> help <test>");
				System.exit(0);
			}
			Optional<EvaluationCase> opt = availableCases.stream().filter(x -> x.getParameter().equals(args[1])).findFirst();
			if(opt.isPresent()){
				out.println(opt.get().getHelpLine());
			}
			else{
				out.println(args[1] + ": No such evaluation case");
			}
			System.exit(0);
		}



		List<EvaluationCase> ec = new LinkedList<>();
		for(EvaluationCase c : availableCases){
			if(hasArg(args, c.getParameter())){
				ec.add(c);
			}
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

		if(ec.size() == 0){
			out.println("Could not recognize any known evaluation cases");
		}
		//needs to be done, as some third party algorithms use system.exit
		forbidSystemExitCall();
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
