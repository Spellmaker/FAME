package de.uniulm.in.ki.mbrenner.fame.evaluation.utility;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationCase;
import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.RuleGenerationWorker;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestRuleGenerationBackup implements EvaluationCase {
	@Override
	public void evaluate(List<File> files, List<String> options) throws Exception {
		int iterations = Integer.parseInt(options.get(0));
		int threadTimeout = Integer.parseInt(options.get(1));
		EvaluationMain.out.println("performing " + iterations  + " iterations of rule generation");
		RuleGenerationWorker.iterations = iterations;
		//setup threads
		Map<Future<Long[]>, RuleGenerationWorker> map = new HashMap<>();
		Map<Future<Long[]>, Long> timeouts = new HashMap<>();

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

			long lastOut = System.currentTimeMillis();
			while (!terminated) {
				List<Future<Long[]>> runningTasks = new LinkedList<>();

				for (int i = 0; i < futures.size(); i++) {
					boolean removed = false;
					Future<Long[]> f = futures.get(i);
					RuleGenerationWorker rgw = map.get(f);
					Long startTime = timeouts.get(f);
					if (f.isDone()) {
						EvaluationMain.out.println("Task finished, evaluating");
						finished++;
						futures.remove(i);
						i--;
						EvaluationMain.out.println("finished task (" + finished + "/" + files.size() + ")");
						String data = map.get(f).f + ";" + f.get()[0] + ";" + f.get()[1] + ";" + f.get()[2] + ";" + f.get()[3] + ";" + f.get()[4] + ";" + f.get()[5] + ";" + f.get()[6] + ";" + f.get()[7] + ";" + f.get()[8];
						String o = header + "\n" + data;
						EvaluationMain.out.println(o);
						lines.add(data);
						removed = true;
						EvaluationMain.out.println("Task evaluation finished");
					} else if (rgw.isRunning() && startTime == null) {
						timeouts.put(f, System.currentTimeMillis());
					} else if (rgw.isRunning() && (System.currentTimeMillis() - startTime) > threadTimeout) {
						f.cancel(true);
						EvaluationMain.out.println("cancelled thread for " + map.get(f).f);
						futures.remove(i);
						i--;
						removed = true;
					}

					if (!removed) {
						runningTasks.add(f);
					}
				}
				if (System.currentTimeMillis() - lastOut >= 1000) {
					EvaluationMain.out.println("remaining: " + futures.size());
					for(Future<Long[]> f : futures){
						System.out.println("done: " + f.isDone() + " running: " + map.get(f).isRunning() + " starttime: " + timeouts.get(f));
					}
					lastOut = System.currentTimeMillis();
					EvaluationMain.out.println("Running threads:");
					for (Future<Long[]> f : runningTasks) {
						if(f.isDone())
							EvaluationMain.out.println("task for file " + map.get(f) + " is marked as running, but is done");
						else if (timeouts.get(f) == null)
							EvaluationMain.out.println(map.get(f).f + ": null");
						else
							EvaluationMain.out.println(map.get(f).f + ": " + (System.currentTimeMillis() - timeouts.get(f)));
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
