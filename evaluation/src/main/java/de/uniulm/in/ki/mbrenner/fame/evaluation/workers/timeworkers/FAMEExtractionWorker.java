package de.uniulm.in.ki.mbrenner.fame.evaluation.workers.timeworkers;

import java.util.Set;
import java.util.concurrent.Callable;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.RandTimeWorker;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;

public class FAMEExtractionWorker implements Callable<Long[]>{
	private Set<OWLEntity> sign;
	private boolean def;
	private RuleSet rules;
	private int ind;
	
	public FAMEExtractionWorker(Set<OWLEntity> sign, boolean def, RuleSet rules, int ind){
		this.sign = sign;
		this.def = def;
		this.rules = rules;
		this.ind = ind;
	}
	
	@Override
	public Long[] call() throws Exception {
		Set<OWLAxiom> module = null;
		long start = System.currentTimeMillis();
		for(int i = 0; i < RandTimeWorker.iterations; i++){
			RBMExtractor rbme = new RBMExtractor(def, false);
			module = rbme.extractModule(rules, sign);
		}
		long end = System.currentTimeMillis();
		if(module == null){
			EvaluationMain.out.println("this should never happen. Just to keep the compiler from removing the instructions above");
		}
		Long[] res = new Long[2];
		res[0] = (long) ind;
		res[1] = end - start;
		return res;
	}

}
