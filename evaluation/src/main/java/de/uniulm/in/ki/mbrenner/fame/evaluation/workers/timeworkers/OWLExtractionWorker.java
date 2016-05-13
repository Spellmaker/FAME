package de.uniulm.in.ki.mbrenner.fame.evaluation.workers.timeworkers;

import java.util.Set;
import java.util.concurrent.Callable;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.RandTimeWorker;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class OWLExtractionWorker implements Callable<Long[]>{
	private OWLOntologyManager man;
	private OWLOntology ont;
	private Set<OWLEntity> sign;
	
	public OWLExtractionWorker(OWLOntologyManager man, OWLOntology ont, Set<OWLEntity> sign){
		this.man = man;
		this.ont = ont;
		this.sign = sign;
	}
	
	@Override
	public Long[] call() throws Exception {
		Set<OWLAxiom> module = null;
		long start = System.currentTimeMillis();
		for(int i = 0; i < RandTimeWorker.iterations; i++){
			SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(man, ont, ModuleType.BOT);
			module = extractor.extract(sign);
		}
		long end = System.currentTimeMillis();
		if(module == null){
			EvaluationMain.out.println("this should never happen. Just to keep the compiler from removing the instructions above");
		}
		Long[] res = new Long[2];
		res[0] = 0L;
		res[1] = end - start;
		return res;
	}

}
