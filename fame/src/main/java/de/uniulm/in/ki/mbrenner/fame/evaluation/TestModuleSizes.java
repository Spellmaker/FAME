package de.uniulm.in.ki.mbrenner.fame.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.ModuleSizeWorker;

public class TestModuleSizes implements EvaluationCase{
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
	
	public static void makeOutput(Double[] res){
		System.out.println("biggest module without optimization: " + res[0]);
		System.out.println("biggest module with optimization: " + res[1]);
		double percent = 100 - (res[1] * 100) / res[0];
		System.out.println("percent reduction: " + percent);
		System.out.println("avg module size without optimization: " + res[2]);
		System.out.println("avg module size with optimization: " + res[3]);
		percent = 100 - (res[3] * 100) / res[2];
		System.out.println("percent reduction: " + percent);
	}

	@Override
	public void evaluate(List<File> files, List<String> options) throws Exception {		
		//setup threads
		ExecutorService mainPool = Executors.newFixedThreadPool(1);
		ExecutorService extractorPool = Executors.newFixedThreadPool(5);
		List<Future<Double[]>> futures = new ArrayList<>(files.size());
		for(int i = 0; i < files.size(); i++){
			futures.add(mainPool.submit(new ModuleSizeWorker(files.get(i), extractorPool, i)));
		}
		int finished = 0;
		boolean terminated = false;
		while(!terminated){
			for(int i = 0; i < futures.size(); i++){
				Future<Double[]> f = futures.get(i);
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
