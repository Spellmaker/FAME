package de.uniulm.in.ki.mbrenner.fame.evaluation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.RandTimeWorker;
import de.uniulm.in.ki.mbrenner.fame.util.OutputFormatter;

/**
 * Tests the extraction time of various module extractors for a number of random signatures
 * options[0]: Number of different singleton signatures to check
 * options[1]: Number of extraction iterations
 */
public class TestRandTimes implements EvaluationCase{
	@Override
	public String getParameter() {
		return "rand-time";
	}

	@Override
	public String getHelpLine() {
		return null;
	}
	/*public static void makeOutput(Long[] res){
		Long largest = 0L;
		for(Long l : res){
			if(l > largest) largest = l;
		}
		Evaluation;Main
		EvaluationMain.out.println("OWLAPI:\t\t" + fill(res[0], largest));
		EvaluationMain.out.println("FAME EL ND:\t" + fill(res[1], largest));
		EvaluationMain.out.println("FAME M ND:\t" + fill(res[2], largest));
		EvaluationMain.out.println("FAME EL D:\t" + fill(res[3], largest));
		EvaluationMain.out.println("FAME M D:\t" + fill(res[4], largest));
		EvaluationMain.out.println("FAMEND EL:\t" + fill(res[5], largest));
		EvaluationMain.out.println("FAMEND M:\t" + fill(res[6], largest));
		EvaluationMain.out.println("HyS:\t" + fill(res[7], largest));
		EvaluationMain.out.println("JCEL:\t" + fill(res[8], largest));
		EvaluationMain.out.println("Incremental:\t" + fill(res[9], largest));
	}*/

	public static String fill(long number, long digits){
		String res = "" + number;
		String comp = "" + digits;
		while(res.length() < comp.length()){
			res = " " + res;
		}
		return res;
	}

	@Override
	public void evaluate(List<File> files, List<String> options) throws Exception {
		RandTimeWorker.entities = Integer.parseInt(options.get(0));
		RandTimeWorker.iterations = Integer.parseInt(options.get(1));
		RandTimeWorker.sigsize = Integer.parseInt(options.get(2));
		Path outDir = null;
		if(options.size() >= 4) {
			outDir = Paths.get(options.get(3));
		}
		int toobig = 0;
		//setup threads
		ExecutorService mainPool = Executors.newFixedThreadPool(1);
		ExecutorService extractorPool = Executors.newFixedThreadPool(5);
		List<Future<Long[]>> futures = new ArrayList<>(files.size());
		Map<Future<Long[]>, File> map = new HashMap<>();
		for(int i = 0; i < files.size(); i++){
			Future<Long[]> f = mainPool.submit(new RandTimeWorker(files.get(i), extractorPool, i));
			map.put(f, files.get(i));
			futures.add(f);
		}
		int finished = 0;
		boolean terminated = false;
		List<String> lines = new LinkedList<>();
		lines.add("file;axioms;logicalaxioms;basemod;OWLAPI;FAME EL ND;FAME M ND;FAME EL D;FAME M D;FAMEND EL;FAMEND M;HyS;JCEL;Incremental");
		while(!terminated){
			for(int i = 0; i < futures.size(); i++){
				Future<Long[]> f = futures.get(i);
				if (f.isDone()) {
					finished++;
					futures.remove(i);
					i--;
					EvaluationMain.out.println("finished task (" + finished + "/" + files.size() + ")");

					try {
						Long[] res = f.get();
						if(res == null){
							toobig++;
							continue;
						}
						//makeOutput(res);
						String s = OutputFormatter.formatCSV(map.get(f), res[0], res[1], res[2], res[3], res[4], res[5], res[6], res[7], res[8], res[9], res[10], res[11], res[12]);
						EvaluationMain.out.println(s);
						lines.add(s);
						if(outDir != null){
							Files.write(outDir.resolve(map.get(f).getName()), Collections.singleton(s));
						}
					}
					catch(Throwable e){
						EvaluationMain.out.println("task (" + finished + "/" + files.size() + ") had errors: " + e);
						e.printStackTrace();
					}
				}
			}
			if(futures.size() <= 0){
				EvaluationMain.out.println("thread pool terminated");
				terminated = true;
			}
		}
		mainPool.shutdown();
		extractorPool.shutdown();
		EvaluationMain.out.println("Skipped " + toobig + " ontologies for size reasons");
		for(String s : lines) EvaluationMain.out.println(s);
	}
}
