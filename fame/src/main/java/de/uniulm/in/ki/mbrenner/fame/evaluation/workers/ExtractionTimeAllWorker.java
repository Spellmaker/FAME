package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.ELRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class ExtractionTimeAllWorker implements Callable<Long[]> {
	private File file;
	private int id;
	
	public ExtractionTimeAllWorker(File f, int id){
		this.file = f;
		this.id = id;
	}
	@Override
	public Long[] call() throws Exception {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = m.loadOntologyFromOntologyDocument(file);
		System.out.println("[Task " + id + "] loaded ontology");
		System.out.println("[Task " + id + "]" + ontology.getAxiomCount() + " axioms in the ontology");
		
		
		RuleSet modeRules = (new BottomModeRuleBuilder()).buildRules(ontology.getAxioms());
		RuleSet elRules = (new ELRuleBuilder()).buildRules(ontology.getAxioms());
		SyntacticLocalityModuleExtractor owlapi = new SyntacticLocalityModuleExtractor(m, ontology, ModuleType.BOT);
		RBMExtractor rbme1 = new RBMExtractor(false, false);
		RBMExtractor rbme2 = new RBMExtractor(true, false);
		
		System.out.println("[Task " + id + "] finished setup phase");
		
		Long[] results = new Long[5];
		
		long start, end;
		Set<OWLEntity> sig = new HashSet<>();
		for(OWLEntity e : ontology.getSignature()){
			if(!(e instanceof OWLClass) &&!(e instanceof OWLObjectProperty)) continue;
			sig.add(e);
		}
		System.out.println("[Task " + id + "] retained " + sig.size() + " entities after filtering");

		Set<OWLEntity> signature;
		Set<OWLAxiom> module = null;
		start = System.currentTimeMillis();
		for(OWLEntity e : sig){
			signature = new HashSet<>();
			signature.add(e);
			module = owlapi.extract(signature);
		}
		end = System.currentTimeMillis();
		results[0] = end - start;

		start = System.currentTimeMillis();
		for(OWLEntity e : sig){
			signature = new HashSet<>();
			signature.add(e);
			module = rbme1.extractModule(elRules, signature);
		}
		end = System.currentTimeMillis();
		results[1] = end - start;

		start = System.currentTimeMillis();
		for(OWLEntity e : sig){
			signature = new HashSet<>();
			signature.add(e);
			module = rbme1.extractModule(modeRules, signature);
		}
		end = System.currentTimeMillis();
		results[2] = end - start;

		start = System.currentTimeMillis();
		for(OWLEntity e : sig){
			signature = new HashSet<>();
			signature.add(e);
			module = rbme2.extractModule(elRules, signature);
		}
		end = System.currentTimeMillis();
		results[3] = end - start;

		start = System.currentTimeMillis();
		for(OWLEntity e : sig){
			signature = new HashSet<>();
			signature.add(e);
			module = rbme2.extractModule(modeRules, signature);
		}
		end = System.currentTimeMillis();
		results[4] = end - start;
		
		if(module == null) System.exit(0);
		
		return results;
	}
}
