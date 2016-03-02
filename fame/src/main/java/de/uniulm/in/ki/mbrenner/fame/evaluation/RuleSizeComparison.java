package de.uniulm.in.ki.mbrenner.fame.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.RuleSizeWorker;

public class RuleSizeComparison implements EvaluationCase{	
	public static void makeOutput(Long[] res){
		System.out.println("Ontology size: " + res[0] + " axioms, " + res[1] + " bytes");
		System.out.println("el: " + res[2] + " rules, " + res[3] + " bytes");
		System.out.println("bm: " + res[4] + " rules, " + res[5] + " bytes");
		System.out.println("cr: " + res[6] + " rules, " + res[7] + " bytes");
	}

	@Override
	public void evaluate(List<File> files, List<String> options) throws Exception {
		ExecutorService mainPool = Executors.newFixedThreadPool(5);
		List<Future<Long[]>> futures = new ArrayList<>(files.size());
		for(int i = 0; i < files.size(); i++){
			futures.add(mainPool.submit(new RuleSizeWorker(files.get(i))));
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
					makeOutput(f.get());
				}
			}
			if(futures.size() <= 0){
				System.out.println("thread pool terminated");
				terminated = true;
			}
		}
		mainPool.shutdown();
	}
}
