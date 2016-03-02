package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.semanticweb.owlapi.model.OWLEntity;

import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;

public class ModuleExtractionWorker implements Callable<Pair<Boolean, Integer>> {
	private OWLEntity e;
	private RuleSet ruleSet;
	private boolean doDef;
	
	
	public ModuleExtractionWorker(OWLEntity e, RuleSet ruleSet, boolean doDef){
		this.e = e;
		this.ruleSet = ruleSet;
		this.doDef = doDef;
	}
	
	
	@Override
	public Pair<Boolean, Integer> call() throws Exception {
		Set<OWLEntity> signature = new HashSet<>();
		signature.add(e);
		RBMExtractor extr = new RBMExtractor(doDef, false);
		return new Pair<>(doDef, extr.extractModule(ruleSet, signature).size());
	}

}

class Pair <K, V>{
	K v1;
	V v2;
	public Pair(K v1, V v2){
		this.v1 = v1;
		this.v2 = v2;
	}
}
