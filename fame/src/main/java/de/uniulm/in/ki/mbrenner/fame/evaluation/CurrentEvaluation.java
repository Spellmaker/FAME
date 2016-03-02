package de.uniulm.in.ki.mbrenner.fame.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.uniulm.in.ki.mbrenner.fame.OntologiePaths;
import de.uniulm.in.ki.mbrenner.fame.extractor.CompressedExtractor;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.CompressedRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.CompressedRuleSet;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import de.uniulm.in.ki.mbrenner.fame.util.ClassPrinter;

public class CurrentEvaluation implements EvaluationCase {

	@Override
	public void evaluate(List<File> ontologies, List<String> options) throws Exception {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = m.loadOntologyFromOntologyDocument(new File(OntologiePaths.galen));
		System.out.println("loaded");
				
		RuleSet rs = (new BottomModeRuleBuilder()).buildRules(ontology.getAxioms());
		CompressedRuleSet crs = (new CompressedRuleBuilder()).buildRules(ontology.getAxioms());
		System.out.println("normal ruleset: " + rs.size() + " rules");
		System.out.println("compressed ruleset: " + crs.size() + " rules");
		
		List<OWLEntity> entityList = new ArrayList<>();
		for(OWLEntity e : ontology.getSignature()){
			if(!(e instanceof OWLClass)) continue;
			entityList.add(e);
		}
		
		Set<Set<OWLEntity>> signatures = new HashSet<>();
		Random r = new Random();
		for(int i = 0; i < 1000; i++){
			Set<OWLEntity> nsig = new HashSet<>();
			nsig.add(entityList.get(r.nextInt(entityList.size())));
			signatures.add(nsig);
		}
		
		//correctness
		for(Set<OWLEntity> e : signatures){					
			Set<OWLAxiom> m1 = (new RBMExtractorNoDef(false)).extractModule(rs, e);
			Set<OWLAxiom> m2 = (new CompressedExtractor()).extractModule(crs, e);
			
			for(OWLAxiom a : m2){
				if(!m1.contains(a)){
					System.out.println("compressed has additional axiom " + ClassPrinter.printAxiom(a));
				}
			}
			for(OWLAxiom a : m1){
				if(!m2.contains(a)){
					System.out.println("compressed is missing axiom " + ClassPrinter.printAxiom(a));
				}
			}
		}
		
		//time
		long start, end;
		RBMExtractorNoDef rbme = new RBMExtractorNoDef(false);
		start = System.currentTimeMillis();
		for(Set<OWLEntity> e : signatures){
			@SuppressWarnings("unused")
			Set<OWLAxiom> m1 = rbme.extractModule(rs, e);
		}
		end = System.currentTimeMillis();
		System.out.println("rbme: " + (end - start));
		
		CompressedExtractor compressed = new CompressedExtractor();
		start = System.currentTimeMillis();
		for(Set<OWLEntity> e : signatures){
			@SuppressWarnings("unused")
			Set<OWLAxiom> m1 = compressed.extractModule(crs, e);
		}
		end = System.currentTimeMillis();
		System.out.println("compressed: " + (end - start));
	}

}
