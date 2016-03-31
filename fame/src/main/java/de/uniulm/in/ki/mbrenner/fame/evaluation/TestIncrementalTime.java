package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.IncrTimeWorker;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.ModuleSizeWorker;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.IncrTimeBothResult;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.ModuleSizeResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestIncrementalTime implements EvaluationCase{
	@Override
	public void evaluate(List<File> files, List<String> options) throws Exception {
		//setup threads
		ExecutorService mainPool = Executors.newFixedThreadPool(1);
		ExecutorService extractorPool = Executors.newFixedThreadPool(5);

		if(options.size() < 2){
			EvaluationMain.out.println("ERROR: Need at least two options");
			return;
		}

		int change = Integer.parseInt(options.get(0));
		int iterations = Integer.parseInt(options.get(1));

		List<Future<IncrTimeBothResult>> futures = new ArrayList<>(files.size());
		Map<Future<IncrTimeBothResult>, IncrTimeWorker> workerMap = new HashMap<>();
		for(int i = 0; i < files.size(); i++){
			IncrTimeWorker w = new IncrTimeWorker(files.get(i), change, iterations, i, extractorPool);
			Future<IncrTimeBothResult> f = mainPool.submit(w);
			futures.add(f);
			workerMap.put(f, w);
		}
		int finished = 0;
		boolean terminated = false;

		List<String> global = new LinkedList<>();
		while(!terminated){
			for(int i = 0; i < futures.size(); i++){
				Future<IncrTimeBothResult> f = futures.get(i);
				if(f.isDone()){
					finished++;
					futures.remove(i);
					i--;
					try {
						IncrTimeBothResult itbr = f.get();
						EvaluationMain.out.println("Finished " + workerMap.get(f).f + ": Normal time: " + itbr.normal + " Incr time: " + itbr.incremental);
					}
					catch(Exception e){
						EvaluationMain.out.println("evaluation for file " + workerMap.get(f).f + " failed: " + e);
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
	}
}
