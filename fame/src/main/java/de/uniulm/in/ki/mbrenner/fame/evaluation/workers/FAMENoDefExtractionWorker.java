package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import java.util.Set;
import java.util.concurrent.Callable;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;

public class FAMENoDefExtractionWorker implements Callable<Long[]>{
	private Set<OWLEntity> sign;
	private RuleSet rules;
	private int ind;
	
	public FAMENoDefExtractionWorker(Set<OWLEntity> sign, RuleSet rules, int ind){
		this.sign = sign;
		this.rules = rules;
		this.ind = ind;
	}
	
	@Override
	public Long[] call() throws Exception {
		Set<OWLAxiom> module = null;
		long start = System.currentTimeMillis();
		for(int i = 0; i < RandTimeWorker.iterations; i++){
			RBMExtractorNoDef rbme = new RBMExtractorNoDef(false);
			module = rbme.extractModule(rules, sign);
		}
		long end = System.currentTimeMillis();
		if(module == null){
			System.out.println("this should never happen. Just to keep the compiler from removing the instructions above");
		}
		Long[] res = new Long[2];
		res[0] = (long) ind;
		res[1] = end - start;
		return res;
	}

}
