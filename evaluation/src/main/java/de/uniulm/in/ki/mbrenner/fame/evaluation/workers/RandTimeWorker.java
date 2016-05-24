package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


import de.tudresden.inf.lat.jcel.coreontology.axiom.NormalizedIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.complex.ComplexIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.extension.IntegerOntologyObjectFactoryImpl;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerObjectProperty;
import de.tudresden.inf.lat.jcel.ontology.normalization.OntologyNormalizer;
import de.tudresden.inf.lat.jcel.owlapi.translator.Translator;
import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.timeworkers.*;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleBuilder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;

import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;

public class RandTimeWorker implements Callable<Long[]>{
	private File f;
	private ExecutorService pool;
	private int id;
	
	public static int iterations = 300;
	public static int entities = 100;
	public static int sigsize = 1;

	private Random r;
	private List<OWLEntity> allEntities;

	public Set<OWLEntity> getRandomSignature(){
		return getRandomSignature(1 + r.nextInt(allEntities.size()));
	}

	public Set<OWLEntity> getRandomSignature(int size){
		Set<OWLEntity> result = new HashSet<>();
		for(int i = 0; i < size; i++){
			result.add(allEntities.get(r.nextInt(allEntities.size())));
		}
		return result;
	}

	public RandTimeWorker(File f, ExecutorService pool, int id){
		this.f = f;
		this.pool = pool;
		this.id = id;
		this.r = new Random();
	}
	
	@Override
	public Long[] call() throws Exception {
		message("Loading ontology");
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
		loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
		OWLOntology ontology = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f), loaderConfig);
		if(ontology.getLogicalAxiomCount() > EvaluationMain.max_size){
			EvaluationMain.out.println("Skipping ontology " + f + " as it is too large");
			return null;
		}
		if(ontology.getLogicalAxiomCount() < EvaluationMain.min_size){
			EvaluationMain.out.println("Skipping ontology " + f + " as it is too small");
			return null;
		}
		if(ontology.getSignature().size() <= 0){
			EvaluationMain.out.println("No elements in signature for ontology " + f + " skipping");
			return null;
		}
		message("Size is " + ontology.getAxiomCount());
		message("Generating rules");
		//preparation work
		//RuleSet rulesEL = (new ELRuleBuilder()).buildRules(ontology);

		RuleSet rulesMode = (new RuleBuilder()).buildRules(ontology);

		Translator trans = null;
		Set<ComplexIntegerAxiom> transOntology = null;
		Set<NormalizedIntegerAxiom> normOntology = null;
		try {
			trans = new Translator(m.getOWLDataFactory(), new IntegerOntologyObjectFactoryImpl());
			transOntology = trans.translateSA(ontology.getAxioms());
			normOntology = (new OntologyNormalizer()).normalize(transOntology, trans.getOntologyObjectFactory());
		}
		catch(Throwable t){
			trans = null;
			transOntology = null;
			normOntology = null;
		}
		allEntities = new ArrayList<>(ontology.getSignature().size());
		allEntities.addAll(ontology.getClassesInSignature());
		allEntities.addAll(ontology.getObjectPropertiesInSignature());

		/*HyS hys = null;
		try{
			hys = new HyS(ontology, ModuleType.BOT);
			hys.condense(SCCAlgorithm.TARJAN);
			hys.condense(SCCAlgorithm.MREACHABILITY);
		}
		catch(Throwable t){
			hys = null;
		}*/

		IncrementalExtractor ie = new IncrementalExtractor(ontology);

		message("testing with " + entities + " random entities, " + iterations + " iterations and " + sigsize + " signature size");
		//Random rand = new Random();
		List<Future<Long[]>> futures = new LinkedList<>();
		Map<Future<Long[]>, Integer> map = new HashMap<>();
		for(int i = 0; i < entities; i++){
			//OWLEntity e = allEntities.get(rand.nextInt(allEntities.size()));
			//Set<OWLEntity> signature = new HashSet<>();
			//signature.add(e);
			Set<OWLEntity> signature = getRandomSignature(sigsize);
			Set<Integer> intClasses = new HashSet<>();
			Set<Integer> intProperties = new HashSet<>();
			try {
				for (OWLEntity e : signature) {
					if (e instanceof OWLClass) intClasses.add(trans.translateC((OWLClass) e).getId());
					else intProperties.add(((IntegerObjectProperty) trans.translateOPE((OWLObjectProperty) e)).getId());
				}
			}
			catch(Exception e){
				EvaluationMain.out.println("Some error with jcel");
			}

			Future<Long[]> f = pool.submit(new OWLExtractionWorker(m, ontology, signature));
			futures.add(f); map.put(f, 0);
			/*f = pool.submit(new FAMEExtractionWorker(signature, false, rulesEL, 1));
			futures.add(f); map.put(f, 1);
			f = pool.submit(new FAMEExtractionWorker(signature, false, rulesMode, 2));
			futures.add(f); map.put(f, 2);
			f = pool.submit(new FAMEExtractionWorker(signature, true, rulesEL, 3));
			futures.add(f); map.put(f, 3);*/
			f = pool.submit(new FAMEExtractionWorker(signature, true, rulesMode, 4));
			futures.add(f); map.put(f, 4);
			/*f = pool.submit(new FAMENoDefExtractionWorker(signature, rulesEL, 5));
			futures.add(f); map.put(f, 5);*/
			f = pool.submit(new FAMENoDefExtractionWorker(signature, rulesMode, 6));
			futures.add(f); map.put(f, 6);
			/*if(hys != null) {
				f = pool.submit(new HySExtractionWorker(hys, signature, 7));
				futures.add(f);
				map.put(f, 7);
			}*/
			if(normOntology != null) {
				f = pool.submit(new JCELExtractionWorker(normOntology, intClasses, intProperties, 8));
				futures.add(f);
				map.put(f, 8);
			}
			if(sigsize == 1){
				f = pool.submit(new IncrementalExtractionWorker(signature, ie, 9));
				futures.add(f); map.put(f, 9);
			}
		}
		
		Long[] result = new Long[13];
		for(int i = 0; i < result.length; i++) result[i] = -1L;
		result[0] = (long) ontology.getAxioms().size();
		result[1] = 0L;
		for(OWLAxiom a : ontology.getAxioms()){
			if(a instanceof OWLLogicalAxiom) result[1]++;
		}
		result[2] = Long.valueOf((new RuleBuilder()).buildRules(ontology).getBaseModule().size());


		int begin = futures.size();
		int old = 0;
		while(futures.size() > 0){
			for(int i = 0; i < futures.size(); i++){
				if(futures.get(i).isDone()){
					Future<Long[]> current = futures.get(i);
					futures.remove(i--);
					try {
						Long[] res = current.get();
						result[res[0].intValue() + 3] += res[1];
					}
					catch(Throwable e){
						EvaluationMain.out.println("[Task " + id + "] Had errors for execution with " + getName(map.get(f)));
					}
					int percent = getPercent(begin, futures.size());
					if (percent - old != 0 && percent % 5 == 0) {
						EvaluationMain.out.println("[Task " + id + "] " + getPercent(begin, futures.size()) + "% processed");
						old = percent;
					}
				}
			}
		}
		
		return result;
	}

	private String getName(int id){
		switch(id) {
			case 0:
				return "OWLAPI";
			case 1:
				return "FAME+EL";
			case 2:
				return "FAME+BM";
			case 3:
				return "FAME+Def+EL";
			case 4:
				return "FAME+Def+BM";
			case 5:
				return "FAMENDef+EL";
			case 6:
				return "FAMENDef+BM";
			case 7:
				return "HyS";
			case 8:
				return "JCEL";
			case 9:
				return "INCR";
		}
		return "unknown";
	}
	
	private void message(String s){
		EvaluationMain.out.println("[Task " + id + "] " + s);
	}
	

	private static int getPercent(int begin, int current){
		return 100 - (current * 100) / begin;
	}

}
