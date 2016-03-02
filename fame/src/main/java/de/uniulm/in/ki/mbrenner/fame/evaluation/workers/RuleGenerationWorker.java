package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.ELRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;

public class RuleGenerationWorker implements Callable<Long[]>{
	private File f;
	private int id;
	
	public static int iterations;
	
	public RuleGenerationWorker(File f, int id){
		this.f = f;
		this.id = id;
	}
	
	
	@Override
	public Long[] call() throws Exception {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = m.loadOntologyFromOntologyDocument(f);
		System.out.println("[Task " + id + "] loaded ontology");
		System.out.println("[Task " + id + "]" + ontology.getAxiomCount() + " axioms in the ontology");
		Set<OWLAxiom> axioms = ontology.getAxioms();
		
		Long[] results = new Long[2];
		ELRuleBuilder elRules = new ELRuleBuilder();
		BottomModeRuleBuilder btmRules = new BottomModeRuleBuilder();
		RuleSet rs = null;		
		long start = System.currentTimeMillis();
		for(int i = 0; i < iterations; i++){
			rs = elRules.buildRules(axioms);
		}
		long end = System.currentTimeMillis();
		results[0] = end - start;
		start = System.currentTimeMillis();
		for(int i = 0; i < iterations; i++){
			rs = btmRules.buildRules(axioms);
		}
		end = System.currentTimeMillis();
		results[1] = end - start;
		if(rs == null){
			//hopefully stops rs from being optimized away
			System.exit(0);
		}
		return results;
	}

}
