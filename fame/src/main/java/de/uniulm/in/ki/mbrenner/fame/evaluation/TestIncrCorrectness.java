package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.IncrCorrectnessWorker;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.ModuleCorrectnessWorker;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.IncrCorrectnessResult;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestIncrCorrectness implements EvaluationCase{
	@Override
	public void evaluate(List<File> files, List<String> options) throws Exception {
		EvaluationMain.out.println("warning: correctness currently excludes declaration axioms");
		int change = 1;
		int iterations = 50;
		//setup threads
		Map<Future<IncrCorrectnessResult>, IncrCorrectnessWorker> map = new HashMap<>();
		ExecutorService genPool = Executors.newFixedThreadPool(5);
		List<Future<IncrCorrectnessResult>> futures = new ArrayList<>(files.size());
		for(int i = 0; i < files.size(); i++){
			IncrCorrectnessWorker w = new IncrCorrectnessWorker(files.get(i), change, iterations, i);
			Future<IncrCorrectnessResult> fut = genPool.submit(w);
			futures.add(fut);
			map.put(fut, w);
		}
		int finished = 0;
		boolean terminated = false;
		while(!terminated){
			for(int i = 0; i < futures.size(); i++){
				Future<IncrCorrectnessResult> f = futures.get(i);
				if(f.isDone()){
					finished++;
					futures.remove(i);
					i--;

					EvaluationMain.out.println("finished task (" + finished + "/" + files.size() + "): '" + map.get(f).f + "'");
					try {
						IncrCorrectnessResult retVal = f.get();
						EvaluationMain.out.println(retVal);
					}
					catch(ExecutionException exc){
						EvaluationMain.out.println("execution failed:");
						exc.printStackTrace();
					}
				}
			}
			if(futures.size() <= 0){
				EvaluationMain.out.println("thread pool terminated");
				terminated = true;
			}
		}
		genPool.shutdown();
	}
}
