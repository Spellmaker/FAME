package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.OntologyWorker;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.PrintField;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.SingleLevelWorkerFactory;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.WorkerResult;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleBuilder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by spellmaker on 14.04.2016.
 */

public class OntoStatFactory implements SingleLevelWorkerFactory<OntoStatResult>{
    @Override
    public Callable<OntoStatResult> getWorker(File file, List<String> options) {
        return new OntoStatWorker(file);
    }

    @Override
    public String getGreeting() {
        return "Determining stats for ontologies";
    }
}

class OntoStatResult extends WorkerResult{
    @PrintField
    int baseModuleSize;

    public OntoStatResult(OntologyWorker<?> worker) {
        super(worker);
    }
}

class OntoStatWorker extends OntologyWorker<OntoStatResult> {
    public OntoStatWorker(File file) {
        super(file);
    }

    @Override
    protected OntoStatResult process(OWLOntology ontology) throws Exception {
        OntoStatResult res = new OntoStatResult(this);
        res.baseModuleSize = new RuleBuilder().buildRules(ontology).getBaseModule().size();
        return res;
    }
}
