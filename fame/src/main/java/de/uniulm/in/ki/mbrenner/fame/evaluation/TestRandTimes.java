package de.uniulm.in.ki.mbrenner.fame.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.RandTimeWorker;

public class TestRandTimes implements EvaluationCase{	
	public static void makeOutput(Long[] res){
		Long largest = 0L;
		for(Long l : res){
			if(l > largest) largest = l;
		}
		
		System.out.println("OWLAPI:\t\t" + fill(res[0], largest));
		System.out.println("FAME EL ND:\t" + fill(res[1], largest));
		System.out.println("FAME M ND:\t" + fill(res[2], largest));
		System.out.println("FAME EL D:\t" + fill(res[3], largest));
		System.out.println("FAME M D:\t" + fill(res[4], largest));
		System.out.println("FAMEND EL:\t" + fill(res[5], largest));
		System.out.println("FAMEND M:\t" + fill(res[6], largest));
	}

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
		//setup threads
		ExecutorService mainPool = Executors.newFixedThreadPool(1);
		ExecutorService extractorPool = Executors.newFixedThreadPool(5);
		List<Future<Long[]>> futures = new ArrayList<>(files.size());
		for(int i = 0; i < files.size(); i++){
			futures.add(mainPool.submit(new RandTimeWorker(files.get(i), extractorPool, i)));
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
		extractorPool.shutdown();
	}
}
