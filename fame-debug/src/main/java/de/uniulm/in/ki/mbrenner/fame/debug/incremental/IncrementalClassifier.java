package de.uniulm.in.ki.mbrenner.fame.debug.incremental;

import de.uniulm.in.ki.mbrenner.fame.debug.incremental.classifiers.HierarchyManager;
import de.uniulm.in.ki.mbrenner.fame.debug.incremental.providers.IncrementalOntologyManager;
import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.IncrTimeResult;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by spellmaker on 24.03.2016.
 */
public class IncrementalClassifier implements Callable<IncrTimeResult> {
    private Random r;
    private int id;
    private IncrementalOntologyManager ontologySource;
    private HierarchyManager hierarchyManager;

    private boolean check_results = false;

    private void message(String msg){
        EvaluationMain.out.println("[Task " + id + "] " + msg);
    }

    public IncrementalClassifier(IncrementalOntologyManager ontologySource, HierarchyManager manager, int id){
        this.ontologySource = ontologySource;
        r = new Random();
        this.id = id;
        this.hierarchyManager = manager;
    }

    @Override
    public IncrTimeResult call() throws Exception{
        message("Ontology loaded");
        //initialize managers
        OWLOntology workingOntology = ontologySource.getCurrent();
        ReasonerFactory rf = new ReasonerFactory();
        OWLReasoner reasoner = rf.createNonBufferingReasoner(workingOntology);
        Hierarchy hierarchy = HierarchyTools.initialHierarchy(workingOntology, reasoner);
        hierarchyManager.initManager(hierarchy, workingOntology);
        message("initial hierarchy determined");

        int step = 0;
        long start = System.currentTimeMillis();
        while(ontologySource.hasMore()){
            step++;
            workingOntology = ontologySource.next();
            hierarchy = hierarchyManager.determineHierarchy(workingOntology, hierarchy, ontologySource.getAdded(), ontologySource.getRemoved(), ontologySource.newClasses());

            //determine actual hierarchy
            if(check_results) {
                if (!HierarchyTools.checkHierarchy(workingOntology, rf.createNonBufferingReasoner(workingOntology), hierarchy)) {
                    EvaluationMain.out.println("error: wrong hierarchy after " + step + " step(s)");
                    System.exit(0);
                    return null;
                }
            }
        }
        long end = System.currentTimeMillis();
        return new IncrTimeResult(end - start);
    }
}
