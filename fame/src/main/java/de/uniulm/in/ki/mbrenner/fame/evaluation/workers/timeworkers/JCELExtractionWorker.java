package de.uniulm.in.ki.mbrenner.fame.evaluation.workers.timeworkers;

import de.tu_dresden.inf.lat.hys.graph_tools.Node;
import de.tudresden.inf.lat.jcel.core.algorithm.module.ModuleExtractor;
import de.tudresden.inf.lat.jcel.coreontology.axiom.NormalizedIntegerAxiom;
import de.tudresden.inf.lat.jcel.owlapi.translator.Translator;
import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.RandTimeWorker;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by spellmaker on 15.03.2016.
 */
public class JCELExtractionWorker implements Callable<Long[]> {
    private Set<Integer> properties;
    private Set<Integer> classes;
    private Set<NormalizedIntegerAxiom> ontology;
    private Set<OWLEntity> signature;
    private int ind;

    public JCELExtractionWorker(Set<NormalizedIntegerAxiom> ontology, Set<Integer> classes, Set<Integer> properties, int ind){
        this.ontology = ontology;
        this.classes = classes;
        this.properties = properties;
        this.ind = ind;
    }

    @Override
    public Long[] call() throws Exception {
        ModuleExtractor extractor = new ModuleExtractor();
        Set<NormalizedIntegerAxiom> module = null;
        long start = System.currentTimeMillis();
        for(int i = 0; i < RandTimeWorker.iterations; i++){
            module = extractor.extractModule(ontology, classes, properties);
        }
        long end = System.currentTimeMillis();
        if(module == null){
            EvaluationMain.out.println("this should never happen. Just to keep the compiler from removing the instructions above");
        }
        Long[] res = new Long[2];
        res[0] = (long) ind;
        res[1] = end - start;
        return res;
    }
}
