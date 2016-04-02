package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import java.io.File;
import java.util.concurrent.Callable;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.CompressedRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.CompressedRuleSet;
import de.uniulm.in.ki.mbrenner.fame.rule.ELRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import objectexplorer.MemoryMeasurer;

public class RuleSizeWorker implements Callable<Long[]>{
	public File file;
	
	public RuleSizeWorker(File file){
		this.file = file;
	}

	@Override
	public Long[] call() throws Exception {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = m.loadOntologyFromOntologyDocument(file);
		
		RuleSet r1 = (new ELRuleBuilder()).buildRules(ontology);
		RuleSet r2 = (new BottomModeRuleBuilder()).buildRules(ontology);
		CompressedRuleSet r3 = (new CompressedRuleBuilder()).buildRules(ontology);
		
		Long[] res = new Long[8];
		res[0] = (long) ontology.getAxioms().size();
		res[1] = MemoryMeasurer.measureBytes(ontology);
		res[2] = (long) r1.ruleCount();
		res[3] = MemoryMeasurer.measureBytes(r1);
		res[4] = (long) r2.ruleCount();
		res[5] = MemoryMeasurer.measureBytes(r2);
		res[6] = (long) r3.size();
		res[7] = MemoryMeasurer.measureBytes(r3);
		return res;
	}

}
