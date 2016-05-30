package de.uniulm.in.ki.mbrenner.fame.evaluation.utility;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationCase;
import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.ModuleCorrectnessWorker;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestCorrectness implements EvaluationCase {
	@Override
	public String getParameter() {
		return "correctness";
	}

	@Override
	public String getHelpLine() {
		return null;
	}
	@Override
	public void evaluate(List<File> files, List<String> options) throws Exception {
		EvaluationMain.out.println("warning: correctness currently excludes declaration axioms");
		//setup threads
		Map<Future<Object[]>, ModuleCorrectnessWorker> map = new HashMap<>();
		ExecutorService genPool = Executors.newFixedThreadPool(5);
		List<Future<Object[]>> futures = new ArrayList<>(files.size());
		for(int i = 0; i < files.size(); i++){
			ModuleCorrectnessWorker w = new ModuleCorrectnessWorker(files.get(i));
			Future<Object[]> fut = genPool.submit(w);
			futures.add(fut);
			map.put(fut, w);
		}
		int finished = 0;
		boolean terminated = false;
		int fame = 0;
		int famenodef = 0;
		int famecompr = 0;
		List<File> unknownOntologies = new LinkedList<>();
		List<File> wrongOntologies = new LinkedList<>();
		Map<Class<?>, Integer> unknownEntities = new HashMap<>();

		while(!terminated){
			for(int i = 0; i < futures.size(); i++){
				Future<Object[]> f = futures.get(i);
				if(f.isDone()){
					finished++;
					futures.remove(i);
					i--;

					EvaluationMain.out.println("finished task (" + finished + "/" + files.size() + "): '" + map.get(f).f + "'");
					try {
						Object[] retVal = f.get();
						if(retVal[3] != null){
							Map<Class<?>, Integer> tmpResult = (Map<Class<?>, Integer>) retVal[3];
							for(Map.Entry<Class<?>, Integer> entry : tmpResult.entrySet()){
								Integer in = unknownEntities.get(entry.getKey());
								if(in == null) i = 0;
								unknownEntities.put(entry.getKey(), i + 1);
							}

							unknownOntologies.add(map.get(f).f);
							//System.out.println("ontology had unknown axioms");
							//System.out.println("=====================================================");
						}
						else {
							EvaluationMain.out.println("FAME correct: " + retVal[0] + " FAMENoDef correct: " + retVal[1] + " FAMECompr correct: " + retVal[2]);
							if (!(Boolean) retVal[0]) fame++;
							if (!(Boolean) retVal[1]) famenodef++;
							if (!(Boolean) retVal[2]) famecompr++;
							if(!(Boolean) retVal[0] || !(Boolean) retVal[1] || !(Boolean) retVal[2]){
								wrongOntologies.add(map.get(f).f);
							}
							EvaluationMain.out.println("=====================================================");
						}
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
		EvaluationMain.out.println("wrong answers: ");
		EvaluationMain.out.println("FAME: " + fame);
		EvaluationMain.out.println("FAMENoDef: " + famenodef);
		EvaluationMain.out.println("FAMECompr: " + famecompr);
		EvaluationMain.out.println(wrongOntologies.size() + " wrong ontologies:");
		for(File f : wrongOntologies){
			EvaluationMain.out.println(f);
		}
		EvaluationMain.out.println(unknownOntologies.size() + " unknown ontologies: ");
		for(File f : unknownOntologies){
			EvaluationMain.out.println(f);
		}
		EvaluationMain.out.println("Occurences of unknown objects in ontologies: ");
		for(Map.Entry<Class<?>, Integer> entry : unknownEntities.entrySet()){
			EvaluationMain.out.println(entry.getKey() + ": " + entry.getValue());
		}
	}
}
