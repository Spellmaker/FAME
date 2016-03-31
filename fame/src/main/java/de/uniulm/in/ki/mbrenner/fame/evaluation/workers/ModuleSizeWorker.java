package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.ModuleSizeResult;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.ModuleSizeSingleResult;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import de.uniulm.in.ki.mbrenner.fame.rule.ELRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;

public class ModuleSizeWorker implements Callable<ModuleSizeResult> {
	public File file;
	private ExecutorService pool;
	private int id;
	private boolean useBMRB;

	public ModuleSizeWorker(File f, ExecutorService pool, int id, boolean useBMRB){
		this.file = f;
		this.pool = pool;
		this.id = id;
		this.useBMRB = useBMRB;
	}
	@Override
	public ModuleSizeResult call() throws Exception {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = m.loadOntologyFromOntologyDocument(file);
		EvaluationMain.out.println("[Task " + id + "] loaded ontology");
		EvaluationMain.out.println("[Task " + id + "]" + ontology.getAxiomCount() + " axioms in the ontology");
		if(ontology.getAxioms(AxiomType.getTypeForClass(OWLEquivalentClassesAxiom.class)).isEmpty()){
			EvaluationMain.out.println("[Task " + id + "] Skipped: No equivalent classes axioms in ontology");
			return new ModuleSizeResult();
		}
		RuleSet ruleSet = null;
		if(!useBMRB) {
			ELRuleBuilder ruleBuilder = new ELRuleBuilder();
			ruleBuilder.printUnknown = true;
			ruleSet = (ruleBuilder).buildRules(ontology);
		}
		else{
			BottomModeRuleBuilder bmrb = new BottomModeRuleBuilder();
			bmrb.printUnknown = true;
			ruleSet = bmrb.buildRules(ontology);
		}

		int biggest1 = -1;
		int biggest2 = -1;
		long sum1 = 0;
		long sum2 = 0;
		int lbiggest1 = -1;
		int lbiggest2 = -1;
		long lsum1 = 0;
		long lsum2 = 0;
		
		int count = 0;
		List<Future<ModuleSizeSingleResult>> futures = new LinkedList<>();
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
					ModuleSizeSingleResult res = futures.get(i).get();
					futures.remove(i--);
					if(res.doDef){
						if(res.size > biggest2) biggest2 = res.size;
						sum2 += res.size;
						if(res.logical_size > lbiggest2) lbiggest2 = res.logical_size;
						lsum2 += res.logical_size;
					}
					else {
						if(res.size > biggest1) biggest1 = res.size;
						sum1 += res.size;
						if(res.logical_size > lbiggest1) lbiggest1 = res.logical_size;
						lsum1 += res.logical_size;
					}
					int percent = getPercent(begin, futures.size());
					if(percent - old != 0 && percent % 5 == 0){
						EvaluationMain.out.println("[Task " + id + "] " + getPercent(begin, futures.size()) + "% processed");
						old = percent;
					}
				}
			}
		}
		
		double avg1 = sum1 / count;
		double avg2 = sum2 / count;
		double lavg1 = lsum1 / count;
		double lavg2 = lsum2 / count;

		ModuleSizeResult res = new ModuleSizeResult(biggest1, avg1, biggest2, avg2, lbiggest1, lavg1, lbiggest2, lavg2);
		return res;
	}
	
	private static int getPercent(int begin, int current){
		return 100 - (current * 100) / begin;
	}
}
