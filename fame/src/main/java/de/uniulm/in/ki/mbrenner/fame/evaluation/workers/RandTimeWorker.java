package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.ELRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;

public class RandTimeWorker implements Callable<Long[]>{
	private File f;
	private ExecutorService pool;
	private int id;
	
	public static int iterations = 300;
	public static int entities = 100;
	
	public RandTimeWorker(File f, ExecutorService pool, int id){
		this.f = f;
		this.pool = pool;
		this.id = id;
	}
	
	@Override
	public Long[] call() throws Exception {
		message("Loading ontology");
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = m.loadOntologyFromOntologyDocument(f);
		message("Generating rules");
		RuleSet rulesEL = (new ELRuleBuilder()).buildRules(ontology.getAxioms());
		RuleSet rulesMode = (new BottomModeRuleBuilder()).buildRules(ontology.getAxioms());
		message("Filtering signature");
		List<OWLEntity> entityList = new ArrayList<>(ontology.getSignature().size());
		for(OWLEntity e : ontology.getSignature()){
			if(!(e instanceof OWLClass) &&!(e instanceof OWLObjectProperty)) continue;
			
			entityList.add(e);
		}
		message("testing with " + entities + " random entities and " + iterations + " iterations");
		Random rand = new Random();
		List<Future<Long[]>> futures = new LinkedList<>();
		for(int i = 0; i < entities; i++){
			Set<OWLEntity> signature = new HashSet<>();
			signature.add(entityList.get(rand.nextInt(entityList.size())));
			
			futures.add(pool.submit(new OWLExtractionWorker(m, ontology, signature)));
			futures.add(pool.submit(new FAMEExtractionWorker(signature, false, rulesEL, 1)));
			futures.add(pool.submit(new FAMEExtractionWorker(signature, false, rulesMode, 2)));
			futures.add(pool.submit(new FAMEExtractionWorker(signature, true, rulesEL, 3)));
			futures.add(pool.submit(new FAMEExtractionWorker(signature, true, rulesMode, 4)));
			futures.add(pool.submit(new FAMENoDefExtractionWorker(signature, rulesEL, 5)));
			futures.add(pool.submit(new FAMENoDefExtractionWorker(signature, rulesMode, 6)));
		}
		
		Long[] result = new Long[7];
		for(int i = 0; i < result.length; i++) result[i] = 0L;

		int begin = futures.size();
		int old = 0;
		while(futures.size() > 0){
			for(int i = 0; i < futures.size(); i++){
				if(futures.get(i).isDone()){
					Long[] res = futures.get(i).get();
					result[res[0].intValue()] += res[1];
					futures.remove(i--);
					
					int percent = getPercent(begin, futures.size());
					if(percent - old != 0 && percent % 5 == 0){
						System.out.println("[Task " + id + "] " + getPercent(begin, futures.size()) + "% processed");
						old = percent;
					}
				}
			}
		}
		
		return result;
	}
	
	private void message(String s){
		System.out.println("[Task " + id + "] " + s);
	}
	

	private static int getPercent(int begin, int current){
		return 100 - (current * 100) / begin;
	}

}
