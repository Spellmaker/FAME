package de.uniulm.in.ki.mbrenner.fame.evaluation;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.RuleSizeWorker;

public class RuleSizeComparison implements EvaluationCase{	
	public static void makeOutput(Long[] res){
		EvaluationMain.out.println("Ontology size: " + res[0] + " axioms, " + res[1] + " bytes");
		EvaluationMain.out.println("el: " + res[2] + " rules, " + res[3] + " bytes");
		EvaluationMain.out.println("bm: " + res[4] + " rules, " + res[5] + " bytes");
		EvaluationMain.out.println("cr: " + res[6] + " rules, " + res[7] + " bytes");
	}

	@Override
	public void evaluate(List<File> files, List<String> options) throws Exception {
		ExecutorService mainPool = Executors.newFixedThreadPool(5);
		List<Future<Long[]>> futures = new ArrayList<>(files.size());
		Map<Future<Long[]>, RuleSizeWorker> workers = new HashMap<>();
		for(int i = 0; i < files.size(); i++){
			RuleSizeWorker worker = new RuleSizeWorker(files.get(i));
			Future<Long[]> future = mainPool.submit(worker);
			workers.put(future, worker);
			futures.add(future);
		}
		int finished = 0;
		boolean terminated = false;
		List<Exception> errors = new LinkedList<>();
		while(!terminated) {
			for (int i = 0; i < futures.size(); i++) {
				Future<Long[]> f = futures.get(i);
				if (f.isDone()) {
					finished++;
					futures.remove(i);
					i--;
					EvaluationMain.out.println("finished task (" + finished + "/" + files.size() + ")");

					try {
						Long[] retVal = f.get();
						makeOutput(f.get());
					} catch (ExecutionException exc) {
						EvaluationMain.out.println("Error in task '" + workers.get(f).file + "':");
						errors.add(exc);
					}
				}
			}
			if (futures.size() <= 0) {
				EvaluationMain.out.println("thread pool terminated");
				terminated = true;
			}
		}
		EvaluationMain.out.println("there were " + errors.size() + " errors");
		Map<Class<?>, Integer> count = new HashMap<>();
		for(Exception e : errors){
			Integer i = count.get(e.getClass());
			if(i == null) i = 0;
			count.put(e.getClass(), ++i);
		}
		for(Map.Entry<Class<?>, Integer> entry : count.entrySet()){
			EvaluationMain.out.println(entry.getValue() + " times " + entry.getKey().getName());
		}
		for(Exception e : errors){
			e.printStackTrace();
			EvaluationMain.out.println("-------------------------------------------");
		}
		mainPool.shutdown();
	}
}
