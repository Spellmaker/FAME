package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.uniulm.in.ki.mbrenner.fame.rule.ELRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;

public class ModuleSizeWorker implements Callable<Double[]> {
	private File file;
	private ExecutorService pool;
	private int id;
	
	public ModuleSizeWorker(File f, ExecutorService pool, int id){
		this.file = f;
		this.pool = pool;
		this.id = id;
	}
	@Override
	public Double[] call() throws Exception {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = m.loadOntologyFromOntologyDocument(file);
		System.out.println("[Task " + id + "] loaded ontology");
		System.out.println("[Task " + id + "]" + ontology.getAxiomCount() + " axioms in the ontology");
		ELRuleBuilder ruleBuilder = new ELRuleBuilder();
		ruleBuilder.printUnknown = true;
		RuleSet ruleSet = (ruleBuilder).buildRules(ontology.getAxioms());

		int biggest1 = -1;
		int biggest2 = -1;
		long sum1 = 0;
		long sum2 = 0;
		
		int count = 0;
		List<Future<Pair<Boolean, Integer>>> futures = new LinkedList<>();
		for(OWLEntity e : ontology.getSignature()){
			if(!(e instanceof OWLClass) &&!(e instanceof OWLObjectProperty)) continue;
			
			futures.add(pool.submit(new ModuleExtractionWorker(e, ruleSet, true)));
			futures.add(pool.submit(new ModuleExtractionWorker(e, ruleSet, false)));
			count++;
		}
		
		int begin = futures.size();
		int old = 0;
		while(futures.size() > 0){
			for(int i = 0; i < futures.size(); i++){
				if(futures.get(i).isDone()){
					Pair<Boolean, Integer> res = futures.get(i).get();
					futures.remove(i--);
					if(res.v1){
						if(res.v2 > biggest2) biggest2 = res.v2;
						sum2 += res.v2;
					}
					else {
						if(res.v2 > biggest1) biggest1 = res.v2;
						sum1 += res.v2;
					}
					int percent = getPercent(begin, futures.size());
					if(percent - old != 0 && percent % 5 == 0){
						System.out.println("[Task " + id + "] " + getPercent(begin, futures.size()) + "% processed");
						old = percent;
					}
				}
			}
		}
		
		double avg1 = sum1 / count;
		double avg2 = sum2 / count;
		
		Double[] result = new Double[4];
		result[0] = (double) biggest1;
		result[1] = (double) biggest2;
		result[2] = avg1;
		result[3] = avg2;
		
		return result;
	}
	
	private static int getPercent(int begin, int current){
		return 100 - (current * 100) / begin;
	}
}
