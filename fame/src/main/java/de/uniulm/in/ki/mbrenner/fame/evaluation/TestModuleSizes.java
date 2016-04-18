package de.uniulm.in.ki.mbrenner.fame.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.ModuleSizeWorker;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.ModuleSizeResult;
import de.uniulm.in.ki.mbrenner.fame.util.DevNull;

public class TestModuleSizes implements EvaluationCase{
	public static boolean skipNonEq = true;

	public static List<Integer> readGenerating(String file) throws Exception{
		List<Integer> generating = new LinkedList<>();
		if(Files.exists(Paths.get(file))){
			BufferedReader br = new BufferedReader(new FileReader(file));
			String s = "";
			while((s = br.readLine()) != null){
				if(!s.equals("")){
					generating.add(Integer.parseInt(s));
				}
			}
			br.close();
		}
		return generating;
	}
	
	public static void makeOutput(ModuleSizeResult res){
		EvaluationMain.out.println("biggest module without optimization: " + res.size_ndef_max);
		EvaluationMain.out.println("biggest module with optimization: " + res.size_def_max);
		EvaluationMain.out.println("percent reduction: " + res.getMaxPercent());
		EvaluationMain.out.println("avg module size without optimization: " + res.size_ndef_avg);
		EvaluationMain.out.println("avg module size with optimization: " + res.size_def_avg);
		EvaluationMain.out.println("percent reduction: " + res.getAvgPercent());

		EvaluationMain.out.println("biggest logical module without optimization: " + res.size_ndef_max_logical);
		EvaluationMain.out.println("biggest logical module with optimization: " + res.size_def_max_logical);
		EvaluationMain.out.println("percent reduction: " + res.getMaxPercentLogical());
		EvaluationMain.out.println("avg logical module size without optimization: " + res.size_ndef_avg_logical);
		EvaluationMain.out.println("avg logical module size with optimization: " + res.size_def_avg_logical);
		EvaluationMain.out.println("percent reduction: " + res.getAvgPercentLogical());
	}

	@Override
	public void evaluate(List<File> files, List<String> options) throws Exception {
		Path oDir = null;
		if(options.size() >= 1){
			oDir = Paths.get(options.get(0));
			EvaluationMain.out.println("writing files to " + oDir);
		}
		if(options.size() >= 2){
			skipNonEq = options.get(1).equals("true");
		}

		//setup threads
		ExecutorService mainPool = Executors.newFixedThreadPool(1);
		ExecutorService extractorPool = Executors.newFixedThreadPool(5);
		List<Future<ModuleSizeResult>> futures = new ArrayList<>(files.size());
		Map<Future<ModuleSizeResult>, ModuleSizeWorker> workerMap = new HashMap<>();
		for(int i = 0; i < files.size(); i++){
			ModuleSizeWorker w = new ModuleSizeWorker(files.get(i), extractorPool, i);
			Future<ModuleSizeResult> f = mainPool.submit(w);
			futures.add(f);
			workerMap.put(f, w);
		}
		int finished = 0;
		boolean terminated = false;

		List<String> global = new LinkedList<>();
		int reduced = 0;
		int meaningless = 0;
		int skipped = 0;
		while(!terminated){
			for(int i = 0; i < futures.size(); i++){
				Future<ModuleSizeResult> f = futures.get(i);
				if(f.isDone()){
					finished++;
					futures.remove(i);
					i--;
					Path putPath = null;
					if(oDir != null) putPath = oDir.resolve(workerMap.get(f).file.getName());
					try {
						EvaluationMain.out.println("finished task (" + finished + "/" + files.size() + ")");
						ModuleSizeResult d = f.get();
						if(d == null) continue;
						if(d.hasEq) {
							if(d.getMaxPercent() > 0 || d.getAvgPercent() > 0){
								reduced++;
							}
							if(d.size_ndef_max == 0 || d.size_ndef_avg == 0) meaningless++;
							makeOutput(d);
							if (putPath != null && Files.exists(putPath)) Files.delete(putPath);
							List<String> lines = new LinkedList<>();
							lines.add(workerMap.get(f).file + ";" + d.size_ndef_max + ";" + d.size_def_max + ";" + d.getMaxPercent() +
									";" + d.size_ndef_avg + ";" + d.size_def_avg + ";" + d.getAvgPercent() +
									";" + d.size_ndef_max_logical + ";" + d.size_def_max_logical + ";" + d.getMaxPercentLogical() +
									";" + d.size_ndef_avg_logical + ";" + d.size_def_avg_logical + ";" + d.getAvgPercentLogical());
							global.addAll(lines);

							if(putPath != null){
								Files.write(putPath, lines);
							}
						}
						else{
							skipped++;
						}
					}
					catch(Exception e){
						EvaluationMain.out.println("evaluation for file " + workerMap.get(f).file.getName() + " failed: " + e);
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

		EvaluationMain.out.println("Task Number;Biggest WO Opt;Biggest W Opt;Reduction Perc;Avg WO Opt;Avg W Opt;Reduction perc; Biggest Logical WO Opt;Biggest Logical W Opt;Reduction Perc;Avg Logical WO Opt;Avg Logical W Opt;Reduction perc");
		int pos = 0;
		for(String s : global) EvaluationMain.out.println(s);

		EvaluationMain.out.println("Skipped " + skipped + " ontologies of " + files.size() + " because they had no equivalence axioms");
		EvaluationMain.out.println("Achieved a reduction in " + reduced + " ontologies");
		EvaluationMain.out.println(meaningless + " ontologies had meaningless results, as avg or maximum module size was 0");

	}
}
