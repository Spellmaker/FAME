package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.OntologyWorker;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.SingleLevelEvaluationCase;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.SingleLevelWorkerFactory;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.WorkerResult;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by spellmaker on 08.06.2016.
 */
public class InfoWorker implements SingleLevelWorkerFactory<InfoResult> {
    public InfoWorker() {
    }

    @Override
    public Callable<InfoResult> getWorker(File file, List<String> options) {
        return new InfoWorkerWorker(file);
    }

    @Override
    public String getGreeting() {
        return null;
    }
}

class InfoResult extends WorkerResult{
    long axioms;

    public InfoResult(OntologyWorker<?> worker) {
        super(worker);
    }

    @Override
    public String toString(){
        return "Ontology " + this.f + ": " + axioms + " logical axioms";
    }

    @Override
    public String getHeader(){
        return null;
    }
}

class InfoWorkerWorker extends OntologyWorker<InfoResult> {
    public InfoWorkerWorker(File file) {
        super(file);
    }

    @Override
    protected InfoResult process(OWLOntology ontology) throws Exception {
        InfoResult info = new InfoResult(this);
        info.axioms = ontology.getLogicalAxiomCount();
        return info;
    }
}