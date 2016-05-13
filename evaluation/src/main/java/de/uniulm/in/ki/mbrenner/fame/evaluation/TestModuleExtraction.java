package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.ModuleExtractionTimeWorker;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestModuleExtraction implements EvaluationCase{
	public static int sig_size;
	public static int element_count;
	public static boolean skip_jcel = false;

	private String makeOut(Long[] res){
		String s = res[0].toString();
		for(int i = 1; i < res.length; i++){
			s += ";" + res[i];
		}
		return s;
	}

	@Override
	public void evaluate(List<File> files, List<String> options) throws Exception {
		sig_size = Integer.parseInt(options.get(0));
		element_count = Integer.parseInt(options.get(1));

		Path outDir = null;
		if(options.size() >= 3){
			outDir = Paths.get(options.get(2));
		}
		if(options.size() >= 4){
			skip_jcel = options.get(3).equals("true");
		}

		EvaluationMain.out.println("Starting module extraction time evaluation for " + files.size() + " ontologies");
		//setup threads
		Map<Future<Long[]>, ModuleExtractionTimeWorker> map = new HashMap<>();
		ExecutorService genPool = Executors.newFixedThreadPool(5);
		List<String> lines = new LinkedList<>();
		int toobig = 0;
		try {
			List<Future<Long[]>> futures = new ArrayList<>(files.size());
			for (int i = 0; i < files.size(); i++) {
				ModuleExtractionTimeWorker worker = new ModuleExtractionTimeWorker(files.get(i), i);
				Future<Long[]> f = genPool.submit(worker);
				map.put(f, worker);
				futures.add(f);
			}
			int finished = 0;
			boolean terminated = false;
			String header = "file;axioms;logicalaxioms;basemod;ModeRules;ModeRules+Def;OWLApi;JCEL;Incr";
			lines.add(header);

			while (!terminated) {
				for (int i = 0; i < futures.size(); i++) {
					Future<Long[]> f = futures.get(i);
					ModuleExtractionTimeWorker rgw = map.get(f);
					if (f.isDone()) {
						EvaluationMain.out.println("Task finished, evaluating");
						finished++;
						futures.remove(i);
						i--;
						EvaluationMain.out.println("finished task (" + finished + "/" + files.size() + ")");
						try {
							Long[] res = f.get();
							if(res == null){
								toobig++;
								continue;
							}
							String line = map.get(f).f + ";" + makeOut(res);
							lines.add(line);
							EvaluationMain.out.println(header);
							EvaluationMain.out.println(line);
							if(outDir != null && !Files.exists(outDir.resolve(map.get(f).f.getName()))){
								Files.write(outDir.resolve(map.get(f).f.getName()), Collections.singleton(line));
							}
						}
						catch(Throwable e){
							EvaluationMain.out.println("uncaught exception in task:");
							e.printStackTrace();
						}
					}
				}
				if (futures.size() <= 0) {
					EvaluationMain.out.println("thread pool terminated");
					terminated = true;
				}
			}
		}
		catch(Throwable t){
			EvaluationMain.out.println("ERROR: Evaluation with TestModuleExtraction failed:");
			t.printStackTrace();
			genPool.shutdownNow();
		}
		if(!genPool.isShutdown()) genPool.shutdown();
		EvaluationMain.out.println("skipped " + toobig + " ontologies due to size reasons");
		for(String s : lines){
			EvaluationMain.out.println(s);
		}
	}
}
