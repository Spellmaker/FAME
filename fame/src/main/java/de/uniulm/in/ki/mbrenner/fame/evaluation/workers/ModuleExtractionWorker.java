package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.ModuleSizeSingleResult;
import de.uniulm.in.ki.mbrenner.fame.util.EqCorrectnessChecker;
import de.uniulm.in.ki.mbrenner.fame.util.Misc;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import org.semanticweb.owlapi.model.OWLOntology;

public class ModuleExtractionWorker implements Callable<ModuleSizeSingleResult> {
	private OWLEntity e;
	private RuleSet ruleSet;
	private boolean doDef;
	private OWLOntology ontology;
	
	public ModuleExtractionWorker(OWLEntity e, RuleSet ruleSet, boolean doDef, OWLOntology ontology){
		this.e = e;
		this.ruleSet = ruleSet;
		this.doDef = doDef;
		this.ontology = ontology;
	}
	
	
	@Override
	public ModuleSizeSingleResult call() throws Exception {
		RBMExtractor extr = new RBMExtractor(doDef, false);
		Set<OWLAxiom> mod = extr.extractModule(ruleSet, Collections.singleton(e));

		if(doDef){
			RBMExtractor extr2 = new RBMExtractor(false, false);
			Set<OWLAxiom> ref = extr2.extractModule(ruleSet, Collections.singleton(e));
			//check modules
			if(EqCorrectnessChecker.isCorrectEqModule(mod, extr, ontology, ref) != null){
				EvaluationMain.out.println("produced incorrect eq module for entity " + e);
				mod = ref;
			}
		}

		return new ModuleSizeSingleResult(doDef, mod.size(), Misc.stripNonLogical(mod).size());
	}

}