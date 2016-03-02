package de.uniulm.in.ki.mbrenner.fame.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.RuleGenerationWorker;

public class TestRuleGeneration implements EvaluationCase{
	@Override
	public void evaluate(List<File> files, List<String> options) throws Exception {
		int iterations = Integer.parseInt(options.get(0));
		System.out.println("performing " + iterations  + " iterations of rule generation");
		RuleGenerationWorker.iterations = iterations;
		//setup threads
		ExecutorService genPool = Executors.newFixedThreadPool(5);
		List<Future<Long[]>> futures = new ArrayList<>(files.size());
		for(int i = 0; i < files.size(); i++){
			futures.add(genPool.submit(new RuleGenerationWorker(files.get(i), i)));
		}
		int finished = 0;
		boolean terminated = false;
		while(!terminated){
			for(int i = 0; i < futures.size(); i++){
				Future<Long[]> f = futures.get(i);
				if(f.isDone()){
					finished++;
					futures.remove(i);
					i--;
					System.out.println("finished task (" + finished + "/" + files.size() + ")");
					System.out.println("Rulegen Time ELRules: " + f.get()[0] + " Time ModeRules: " + f.get()[1]);
				}
			}
			if(futures.size() <= 0){
				System.out.println("thread pool terminated");
				terminated = true;
			}
		}
		genPool.shutdown();
	}
}
