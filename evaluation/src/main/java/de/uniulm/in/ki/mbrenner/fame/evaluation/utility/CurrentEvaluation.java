package de.uniulm.in.ki.mbrenner.fame.evaluation.utility;

import java.io.File;
import java.util.*;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationCase;
import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.rule.*;
import de.uniulm.in.ki.mbrenner.fame.util.ClassCounter;
import de.uniulm.in.ki.mbrenner.fame.util.locality.EqCorrectnessChecker;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import org.semanticweb.owlapi.model.parameters.Imports;

public class CurrentEvaluation implements EvaluationCase {
	private static boolean checkSkip(Collection<OWLObject> unknown){
		for(OWLObject o : unknown){
			if(o instanceof OWLAnnotationObject) continue;
			return true;
		}
		return false;
	}

	@Override
	public void evaluate(List<File> ontologies, List<String> options) throws Exception {

	}

	private void checkEqModules(List<File> ontologies) throws OWLOntologyCreationException{

		int cnt = 1;

		int max = 10000;
		int skipped = 0;
		int errors = 0;

		int cnt_eq = 0;
		int cnt_eq_ad = 0;
		EvaluationMain.out.println("starting computation");
		ClassCounter all = new ClassCounter();
		mainloop:
		for(File f : ontologies){
			ClassCounter current = new ClassCounter();
			OWLOntology o = null;
			try {
				OWLOntologyManager m = OWLManager.createOWLOntologyManager();
				o = m.loadOntologyFromOntologyDocument(f);
			}
			catch(Exception e){
				EvaluationMain.out.println("error loading ontology " + f + ": " + e.getMessage());
				errors++;
				cnt++;
				continue;
			}
			EvaluationMain.out.println("ontology loaded");
			Set<OWLEquivalentClassesAxiom> eqClasses = o.getAxioms(AxiomType.getTypeForClass(OWLEquivalentClassesAxiom.class));
			if(eqClasses.size() > 0) cnt_eq++;
			outer:
			for(OWLEquivalentClassesAxiom eq : eqClasses){
				for(OWLClassExpression oce : eq.getClassExpressions()){
					if(oce instanceof OWLClass){
						cnt_eq_ad++;
						break outer;
					}
				}
			}
			EvaluationMain.out.println("check completed");
			BottomModeRuleBuilder bmrb = new BottomModeRuleBuilder();
			RuleSet rs = bmrb.buildRules(o);
			if(checkSkip(bmrb.unknownObjects())){
				EvaluationMain.out.println("unknown objects in rule builder, skipping ontology " + f);
				bmrb.unknownObjects().forEach(x -> current.add(x));
				all.addOnce(current);
				skipped++;
				continue;
			}
			RBMExtractor fame = new RBMExtractor(true, false);

			Set<OWLEntity> seeds = new HashSet<>();
			seeds.addAll(o.getClassesInSignature(Imports.INCLUDED));
			seeds.addAll(o.getObjectPropertiesInSignature(Imports.INCLUDED));
			EvaluationMain.out.println("seeds: " + seeds.size());
			int i = 0;
			for(OWLEntity e : seeds){
				Set<OWLAxiom> mod = fame.extractModule(rs, Collections.singleton(e));
				OWLAxiom wrong = EqCorrectnessChecker.isCorrectEqModule(mod, fame.getActiveDefinitions(), o, new RBMExtractorNoDef(false).extractModule(rs, Collections.singleton(e)));

				if(wrong != null){
					EvaluationMain.out.println("error in ontology " + f);
					EvaluationMain.out.println("wrong is " + wrong);
					EvaluationMain.out.println("entity is " + e);
					break mainloop;
				}
				if(++i > max){
					EvaluationMain.out.println("aborting early for time reasons");
					break;
				}
				//System.out.println(i);
			}
			EvaluationMain.out.println("finished ontology " + cnt + " of " + ontologies.size() + " successfully (" + f + ")");
			cnt++;
		}
		EvaluationMain.out.println("Statistics: ");
		EvaluationMain.out.println("Total: " + ontologies.size() + " ontologies");
		EvaluationMain.out.println("Equivalence Axioms in " + cnt_eq + " ontologies");
		EvaluationMain.out.println("Applicable Equivalence Axioms in " + cnt_eq_ad + " ontologies");
		EvaluationMain.out.println("skipped: " + skipped);
		EvaluationMain.out.println("skipped due to loading errors: " + errors);
		EvaluationMain.out.println("unknown: ");
		all.forEach(x -> EvaluationMain.out.println(x));
	}

	private void findSuitableEq(List<File> ontologies) throws OWLOntologyCreationException{
		//current: filter set of ontologies by finding out, if there is an equivalent classes axiom or not
		int count_eq = 0;
		int count_eq_applicable = 0;

		int task = 1;
		for(File f : ontologies){
			try {
				OWLOntologyManager m = OWLManager.createOWLOntologyManager();
				OWLOntology o = m.loadOntologyFromOntologyDocument(f);
				Set<OWLEquivalentClassesAxiom> eqClasses = o.getAxioms(AxiomType.getTypeForClass(OWLEquivalentClassesAxiom.class));

				if (eqClasses.size() > 0) {
					count_eq++;

					outer:
					for (OWLEquivalentClassesAxiom a : eqClasses) {
						boolean found = false;
						for (OWLClassExpression oce : a.getClassExpressions()) {
							if (oce instanceof OWLClass) {
								count_eq_applicable++;
								found = true;
								break outer;
							}
						}
						if (found) {
							EvaluationMain.out.println("ERROR: Should have breaked, but didnt");
						}
					}
				}
			}
			catch(Exception e){
				EvaluationMain.out.println("ERROR: could not load ontology, skipped");
			}
			EvaluationMain.out.println("task " + task + "/" + ontologies.size() + " finished");
			task++;
		}

		EvaluationMain.out.println("Statistics: ");
		EvaluationMain.out.println("Total: " + ontologies.size() + " ontologies");
		EvaluationMain.out.println("Equivalence Axioms in " + count_eq + " ontologies");
		EvaluationMain.out.println("Applicable Equivalence Axioms in " + count_eq_applicable + " ontologies");
	}
}
