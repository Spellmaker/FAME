package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.TestModuleSizes;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.ModuleSizeResult;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.ModuleSizeSingleResult;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;

import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;

public class ModuleSizeWorker implements Callable<ModuleSizeResult> {
	public File file;
	private ExecutorService pool;
	private int id;

	public ModuleSizeWorker(File f, ExecutorService pool, int id){
		this.file = f;
		this.pool = pool;
		this.id = id;
	}
	@Override
	public ModuleSizeResult call() throws Exception {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
		loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
		OWLOntology ontology = m.loadOntologyFromOntologyDocument(new FileDocumentSource(file), loaderConfig);

		if(ontology.getLogicalAxiomCount() > EvaluationMain.max_size){
			EvaluationMain.out.println("Skipping ontology " + file + " as it is too large");
			return null;
		}
		if(ontology.getLogicalAxiomCount() < EvaluationMain.min_size){
			EvaluationMain.out.println("Skipping ontology " + file + " as it is too small");
			return null;
		}

		EvaluationMain.out.println("[Task " + id + "] loaded ontology");
		EvaluationMain.out.println("[Task " + id + "]" + ontology.getAxiomCount() + " axioms in the ontology");
		if(TestModuleSizes.skipNonEq && ontology.getAxioms(AxiomType.getTypeForClass(OWLEquivalentClassesAxiom.class)).isEmpty()){
			EvaluationMain.out.println("[Task " + id + "] Skipped: No equivalent classes axioms in ontology");
			return new ModuleSizeResult();
		}
		RuleSet ruleSet = null;
		BottomModeRuleBuilder bmrb = new BottomModeRuleBuilder();
		bmrb.printUnknown = true;
		ruleSet = bmrb.buildRules(ontology);

		int biggest1 = -1;
		int biggest2 = -1;
		long sum1 = 0;
		long sum2 = 0;
		int lbiggest1 = -1;
		int lbiggest2 = -1;
		long lsum1 = 0;
		long lsum2 = 0;
		
		int count = 0;
		List<Future<ModuleSizeSingleResult>> futures = new LinkedList<>();
		for(OWLEntity e : ontology.getSignature()){
			if(!(e instanceof OWLClass) &&!(e instanceof OWLObjectProperty)) continue;
			
			futures.add(pool.submit(new ModuleExtractionWorker(e, ruleSet, true, ontology)));
			futures.add(pool.submit(new ModuleExtractionWorker(e, ruleSet, false, null)));
			count++;
		}
		
		int begin = futures.size();
		int old = 0;
		while(futures.size() > 0){
			for(int i = 0; i < futures.size(); i++){
				if(futures.get(i).isDone()){
					ModuleSizeSingleResult res = futures.get(i).get();
					futures.remove(i--);
					if(res.doDef){
						if(res.size > biggest2) biggest2 = res.size;
						sum2 += res.size;
						if(res.logical_size > lbiggest2) lbiggest2 = res.logical_size;
						lsum2 += res.logical_size;
					}
					else {
						if(res.size > biggest1) biggest1 = res.size;
						sum1 += res.size;
						if(res.logical_size > lbiggest1) lbiggest1 = res.logical_size;
						lsum1 += res.logical_size;
					}
					int percent = getPercent(begin, futures.size());
					if(percent - old != 0 && percent % 5 == 0){
						EvaluationMain.out.println("[Task " + id + "] " + getPercent(begin, futures.size()) + "% processed");
						old = percent;
					}
				}
			}
		}
		
		double avg1 = (double) sum1 / (double) count;
		double avg2 = (double) sum2 / (double) count;
		double lavg1 = (double) lsum1 / (double) count;
		double lavg2 = (double) lsum2 / (double) count;

		ModuleSizeResult res = new ModuleSizeResult(biggest1, avg1, biggest2, avg2, lbiggest1, lavg1, lbiggest2, lavg2);
		return res;
	}
	
	private static int getPercent(int begin, int current){
		return 100 - (current * 100) / begin;
	}
}
