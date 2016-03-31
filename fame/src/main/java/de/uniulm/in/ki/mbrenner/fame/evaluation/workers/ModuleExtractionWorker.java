package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.ModuleSizeSingleResult;
import de.uniulm.in.ki.mbrenner.fame.util.Misc;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;

public class ModuleExtractionWorker implements Callable<ModuleSizeSingleResult> {
	private OWLEntity e;
	private RuleSet ruleSet;
	private boolean doDef;
	
	
	public ModuleExtractionWorker(OWLEntity e, RuleSet ruleSet, boolean doDef){
		this.e = e;
		this.ruleSet = ruleSet;
		this.doDef = doDef;
	}
	
	
	@Override
	public ModuleSizeSingleResult call() throws Exception {
		RBMExtractor extr = new RBMExtractor(doDef, false);
		Set<OWLAxiom> mod = extr.extractModule(ruleSet, Collections.singleton(e));
		return new ModuleSizeSingleResult(doDef, mod.size(), Misc.stripNonLogical(mod).size());
	}

}