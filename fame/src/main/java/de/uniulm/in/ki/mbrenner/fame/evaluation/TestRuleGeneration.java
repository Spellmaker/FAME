package de.uniulm.in.ki.mbrenner.fame.evaluation;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.RuleGenerationWorker;
import de.uniulm.in.ki.mbrenner.fame.util.DevNull;

public class TestRuleGeneration implements EvaluationCase{
	@Override
	public void evaluate(List<File> files, List<String> options) throws Exception {
		int iterations = Integer.parseInt(options.get(0));
		Path outDir = null;
		if(options.size() >= 2){
			outDir = Paths.get(options.get(1));
		}
		EvaluationMain.out.println("performing " + iterations  + " iterations of rule generation");
		RuleGenerationWorker.iterations = iterations;
		//setup threads
		Map<Future<Long[]>, RuleGenerationWorker> map = new HashMap<>();
		ExecutorService genPool = Executors.newFixedThreadPool(5);
		List<String> lines = new LinkedList<>();
		try {
			List<Future<Long[]>> futures = new ArrayList<>(files.size());
			for (int i = 0; i < files.size(); i++) {
				RuleGenerationWorker worker = new RuleGenerationWorker(files.get(i), i);
				Future<Long[]> f = genPool.submit(new RuleGenerationWorker(files.get(i), i));
				map.put(f, worker);
				futures.add(f);
			}
			int finished = 0;
			boolean terminated = false;
			String header = "file;axioms;logicalaxioms;ELRules;ModeRules;CompressedRules;HyS;OWLApi;JCEL;Incr";
			lines.add(header);

			while (!terminated) {
				for (int i = 0; i < futures.size(); i++) {
					Future<Long[]> f = futures.get(i);
					RuleGenerationWorker rgw = map.get(f);
					if (f.isDone()) {
						EvaluationMain.out.println("Task finished, evaluating");
						finished++;
						futures.remove(i);
						i--;
						EvaluationMain.out.println("finished task (" + finished + "/" + files.size() + ")");
						String data = map.get(f).f + ";" + f.get()[0] + ";" + f.get()[1] + ";" + f.get()[2] + ";" + f.get()[3] + ";" + f.get()[4] + ";" + f.get()[5] + ";" + f.get()[6] + ";" + f.get()[7] + ";" + f.get()[8];
						if(outDir != null && !Files.exists(outDir.resolve(map.get(f).f.getName()))){
							Files.write(outDir.resolve(map.get(f).f.getName()), Collections.singleton(data));
						}
						String o = header + "\n" + data;
						EvaluationMain.out.println(o);
						lines.add(data);
						EvaluationMain.out.println("Task evaluation finished");
					}
				}
				if (futures.size() <= 0) {
					EvaluationMain.out.println("thread pool terminated");
					terminated = true;
				}
			}
		}
		catch(Throwable t){
			EvaluationMain.out.println("ERROR: Evaluation with TestRuleGeneration failed:");
			t.printStackTrace();
			genPool.shutdownNow();
			while(true) EvaluationMain.out.println("lol fail############################################################");
		}
		if(!genPool.isShutdown()) genPool.shutdown();
		for(String s : lines){
			EvaluationMain.out.println(s);
		}
	}
}
