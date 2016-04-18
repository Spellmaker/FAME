package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.concurrent.Callable;

import de.tu_dresden.inf.lat.hys.graph_tools.SCCAlgorithm;
import de.tudresden.inf.lat.jcel.core.algorithm.module.ModuleExtractor;
import de.tudresden.inf.lat.jcel.coreontology.axiom.NormalizedIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.complex.ComplexIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.extension.IntegerOntologyObjectFactoryImpl;
import de.tudresden.inf.lat.jcel.ontology.normalization.OntologyNormalizer;
import de.tudresden.inf.lat.jcel.owlapi.translator.Translator;
import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.TestRuleGeneration;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.related.HyS.HyS;
import de.uniulm.in.ki.mbrenner.fame.rule.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class RuleGenerationWorker implements Callable<Long[]>{
	public File f;
	private int id;
	
	public static int iterations;
	
	public RuleGenerationWorker(File f, int id){
		this.f = f;
		this.id = id;
	}

	private boolean isRunning = false;
	public boolean isRunning(){
		return isRunning;
	}
	
	
	@Override
	public Long[] call() throws Exception {
		isRunning = true;
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
		loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
		OWLOntology ontology = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f), loaderConfig);
		EvaluationMain.out.println("[Task " + id + "] loaded ontology");
		EvaluationMain.out.println("[Task " + id + "] " + ontology.getAxiomCount() + " axioms in the ontology");
		Set<OWLAxiom> axioms = ontology.getAxioms();
		
		Long[] results = new Long[10];
		results[0] = (long) ontology.getAxiomCount();

		results[1] = 0L;
		for(OWLAxiom a : axioms){
			if(a instanceof OWLLogicalAxiom) results[1]++;
		}

		if(ontology.getLogicalAxiomCount() > EvaluationMain.max_size){
			EvaluationMain.out.println("Skipping ontology " + f + " as it is too large");
			return null;
		}
		if(ontology.getLogicalAxiomCount() < EvaluationMain.min_size){
			EvaluationMain.out.println("Skipping ontology " + f + " as it is too small");
			return null;
		}

		results[2] = Long.valueOf((new BottomModeRuleBuilder().buildRules(ontology)).getBaseModule().size());


		//ELRuleBuilder elRules = new ELRuleBuilder();
		BottomModeRuleBuilder btmRules = new BottomModeRuleBuilder();
		//CompressedRuleBuilder compr = new CompressedRuleBuilder();
		//CompressedRuleSet crs = null;
		RuleSet rs = null;
		long start, end;
		/*try {
			start = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				rs = elRules.buildRules(ontology);
			}
			end = System.currentTimeMillis();
			results[3] = end - start;
			EvaluationMain.out.println("[Task " + id + "] Finished ELRuleBuilder");
		}
		catch(Throwable e){
			results[3] = -1L;
			EvaluationMain.out.println("[Task " + id + "] ELRuleBuilder had errors: " + e);
		}*/
		results[3]=-1L;

		try {
			start = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				rs = btmRules.buildRules(ontology);
			}
			end = System.currentTimeMillis();
			results[4] = end - start;
			EvaluationMain.out.println("[Task " + id + "] Finished BottomModeRuleBuilder");
		}
		catch(Throwable e){
			results[4] = -1L;
			EvaluationMain.out.println("[Task " + id + "] BottomModeRuleBuilder had errors: " + e);
		}

		/*try {
			start = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				crs = compr.buildRules(ontology);
			}
			end = System.currentTimeMillis();
			results[5] = end - start;

			EvaluationMain.out.println("[Task " + id + "] Finished CompressedRuleBuilder");
		}
		catch(Throwable e){
			results[5] = -1L;
			EvaluationMain.out.println("[Task " + id + "] CompressedRuleBuilder had errors: " + e);
		}*/
		results[5] = -1L;

		/*try {
			start = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				HyS h = new HyS(ontology, ModuleType.BOT);
				h.condense(SCCAlgorithm.TARJAN);
				h.condense(SCCAlgorithm.MREACHABILITY);
			}
			end = System.currentTimeMillis();
			results[6] = end - start;
			EvaluationMain.out.println("[Task " + id + "] Finished HyS");
		}
		catch(Throwable e){
			results[6] = -1L;
			EvaluationMain.out.println("[Task " + id + "] HyS had errors: " + e);
		}*/
		results[6] = -1L;

		try {
			start = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				SyntacticLocalityModuleExtractor synt = new SyntacticLocalityModuleExtractor(m, ontology, ModuleType.BOT);
			}
			end = System.currentTimeMillis();
			results[7] = end - start;
			EvaluationMain.out.println("[Task " + id + "] Finished OWLAPI");
		}
		catch(Throwable e){
			results[7] = -1L;
			EvaluationMain.out.println("[Task " + id + "] OWLAPI had errors: " + e);
		}

		try {
			if(!TestRuleGeneration.skip_jcel) {
				start = System.currentTimeMillis();
				for (int i = 0; i < iterations; i++) {
					Translator trans = new Translator(m.getOWLDataFactory(), new IntegerOntologyObjectFactoryImpl());
					trans.getTranslationRepository().addAxiomEntities(ontology);
					Set<ComplexIntegerAxiom> transOntology = trans.translateSA(ontology.getAxioms());
					Set<NormalizedIntegerAxiom> normOntology = (new OntologyNormalizer()).normalize(transOntology, trans.getOntologyObjectFactory());
					ModuleExtractor extr = new ModuleExtractor();
				}
				end = System.currentTimeMillis();
				results[8] = end - start;
				EvaluationMain.out.println("[Task " + id + "] Finished JCEL");
			}
			else{
				results[8] = -1L;
				EvaluationMain.out.println("[Task " + id + "] Skipped JCEL");
			}
		}
		catch(Throwable e){
			results[8] = -1L;
			EvaluationMain.out.println("[Task " + id + "] JCEL had errors: " + e);
		}

		try{
			start = System.currentTimeMillis();
			for(int i = 0; i < iterations; i++){
				IncrementalExtractor ie = new IncrementalExtractor(ontology);
			}
			end = System.currentTimeMillis();
			results[9] = end - start;
			EvaluationMain.out.println("[Task " + id + "] Finished Incremental");
		}
		catch(Throwable e){
			results[9] = -1L;
			EvaluationMain.out.println("[Task " + id + "] Incremental had errors: " + e);
		}

		return results;
	}

}
