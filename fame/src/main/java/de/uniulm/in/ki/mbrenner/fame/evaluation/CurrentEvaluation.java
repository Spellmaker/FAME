package de.uniulm.in.ki.mbrenner.fame.evaluation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import de.tu_dresden.inf.lat.hys.graph_tools.SCCAlgorithm;
import de.tudresden.inf.lat.jcel.coreontology.axiom.NormalizedIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.complex.ComplexIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.extension.IntegerOntologyObjectFactoryImpl;
import de.tudresden.inf.lat.jcel.ontology.normalization.OntologyNormalizer;
import de.tudresden.inf.lat.jcel.owlapi.translator.Translator;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.related.HyS.HyS;
import de.uniulm.in.ki.mbrenner.fame.rule.*;
import de.uniulm.in.ki.mbrenner.fame.util.ClassCounter;
import de.uniulm.in.ki.mbrenner.fame.util.EqCorrectnessChecker;
import de.uniulm.in.ki.mbrenner.fame.util.OntologyFilter;
import de.uniulm.in.ki.mbrenner.oremanager.OREManager;
import de.uniulm.in.ki.mbrenner.oremanager.filters.ORENoFilter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;

import de.uniulm.in.ki.mbrenner.fame.OntologiePaths;
import de.uniulm.in.ki.mbrenner.fame.extractor.CompressedExtractor;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.util.ClassPrinter;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWL2ProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

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
		//OREManager manager = new OREManager();
		//manager.load(Paths.get(options.get(0)), "el/classification", "el/consistency", "el/instantiation");
		//ontologies = manager.filterOntologies(new ORENoFilter());
		EvaluationMain.out.println("ORE found " + ontologies.size() + " ontologies");

		ClassCounter root = new ClassCounter();
		Path goal = Paths.get(options.get(0));
		int skipped = 0;
		int error = 0;
		int profileskipped = 0;
		int allOk = 0;
		int hyscount = 0;
		int jcelcount = 0;
		int c = 0;
		for(File f : ontologies){
			EvaluationMain.out.println("Ontology " + ++c + " of " + ontologies.size());
			try{
				if(Files.exists(goal.resolve(f.getName()))) continue;

				OWLOntologyManager m = OWLManager.createOWLOntologyManager();
				OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
				loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
				OWLOntology o = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f), loaderConfig);

				OWL2ELProfile profile = new OWL2ELProfile();
				OWLProfileReport report = profile.checkOntology(o);
				if(!report.isInProfile()){
					EvaluationMain.out.println("Skipped " + f + ": ontology is not in EL profile");
					profileskipped++;
					continue;
				}

				BottomModeRuleBuilder el = new BottomModeRuleBuilder();
				el.buildRules(o);
				if(!el.unknownObjects().isEmpty()){
					EvaluationMain.out.println("Skipped " + f + ": unknown objects in ontology");
					ClassCounter tmp = new ClassCounter();
					tmp.addAll(el.unknownObjects());
					root.addOnce(tmp);
					skipped++;
				}
				else{
					if(!Files.exists(goal.resolve(f.getName()))) Files.copy(f.toPath(), goal.resolve(f.getName()));

					//check jcel
					boolean bothOk = true;
					try{
						Translator trans = new Translator(m.getOWLDataFactory(), new IntegerOntologyObjectFactoryImpl());
						Set<ComplexIntegerAxiom> transOntology = trans.translateSA(o.getAxioms());
						Set<NormalizedIntegerAxiom> normOntology = (new OntologyNormalizer()).normalize(transOntology, trans.getOntologyObjectFactory());
						jcelcount++;
					}
					catch(Throwable e){
						EvaluationMain.out.println("Skipped: failed jcel");
						bothOk = false;
					}
					//check hys
					try{
						HyS hys = new HyS(o, ModuleType.BOT);
						hys.condense(SCCAlgorithm.TARJAN);
						hys.condense(SCCAlgorithm.MREACHABILITY);
						hyscount++;
					}
					catch(Throwable e){
						EvaluationMain.out.println("Skipped: failed hys");
						bothOk = false;
					}
					if(bothOk) {
						allOk++;
					}
				}
			}
			catch(Exception e){
				EvaluationMain.out.println("Skipped " + f + ": " + e);
				error++;
			}
		}
		EvaluationMain.out.println("Result:");
		EvaluationMain.out.println("Skipped " + (error + skipped) + " ontologies of " + ontologies.size() + ", " + error + " of them due to errors");
		EvaluationMain.out.println("Profile would have skipped " + profileskipped + " ontologies, whereas ELBuilder skipped " + skipped);
		EvaluationMain.out.println("Ontologies working for HyS: " + hyscount + " for JCEL: " + jcelcount + " for both: " + allOk);
		EvaluationMain.out.println("Unknown objects statistics: ");
		root.forEach(x -> EvaluationMain.out.println(x));

		//OntologyFilter.filterOntologies(ontologies, options.get(0));
		//findSuitableEq(ontologies);
		return;

		//OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		//OWLOntology o = m.loadOntologyFromOntologyDocument(new File("EL-GALEN.owl"));
		//RuleSet rs = (new BottomModeRuleBuilder()).buildRules(o);
		//EvaluationMain.out.println("GALEN loaded and prepared");

		/*int max = 0;
		OWLEntity maxEntity = null;
		outer:
		for(OWLEntity e : o.getSignature()){
			if(!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)) continue;

			Set<OWLAxiom> def = (new RBMExtractor(true, false)).extractModule(rs, Collections.singleton(e));
			Set<OWLAxiom> ndef = (new RBMExtractor(false, false)).extractModule(rs, Collections.singleton(e));

			for(OWLAxiom a : ndef) {
				if (def.contains(a)) continue;
				if(a instanceof OWLDeclarationAxiom) continue;
				if (!(a instanceof OWLEquivalentClassesAxiom)) {
					EvaluationMain.out.println("it is not an equivalent classes axiom - this should not happen!");
					EvaluationMain.out.println("entity is " + e);
					EvaluationMain.out.println("axiom is " + a);
					break outer;
				}

				OWLEquivalentClassesAxiom eq = (OWLEquivalentClassesAxiom) a;
				for (OWLClassExpression oce : eq.getClassExpressions()) {
					if (!(oce instanceof OWLClass)) continue;
					int cnt = 0;
					for (OWLAxiom ax : o.getAxioms()) {
						if (ax.getSignature().contains(oce)) {
							cnt++;
						}
					}
					if (cnt > max) {
						cnt = max;
						maxEntity = e;
					}
				}
			}
		}

		EvaluationMain.out.println("max entity is " + maxEntity);
		EvaluationMain.out.println("max diff is " + max);*/
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
				OWLAxiom wrong = EqCorrectnessChecker.isCorrectEqModule(mod, fame, o);

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
