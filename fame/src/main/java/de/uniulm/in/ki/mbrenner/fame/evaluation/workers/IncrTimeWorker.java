package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.IncrTimeBothResult;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.IncrTimeResult;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by spellmaker on 24.03.2016.
 */
public class IncrTimeWorker implements Callable<IncrTimeBothResult> {
    public File f;
    private int change;
    private int iterations;
    private Random r;
    private int id;
    private ExecutorService pool;

    private void message(String msg){
        EvaluationMain.out.println("[Task " + id + "]: " + msg);
    }

    public IncrTimeWorker(File f, int change, int iterations, int id, ExecutorService pool){
        this.f = f;
        this.change = change;
        this.iterations = iterations;
        r = new Random();
        this.id = id;
        this.pool = pool;
    }

    @Override
    public IncrTimeBothResult call() throws Exception {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
        loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
        OWLOntology o = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f), loaderConfig);
        if(o.getLogicalAxiomCount() > EvaluationMain.max_size){
            EvaluationMain.out.println("Skipping ontology " + f + " as it is too large");
            return null;
        }
        if(o.getLogicalAxiomCount() < EvaluationMain.min_size){
            EvaluationMain.out.println("Skipping ontology " + f + " as it is too small");
            return null;
        }

        //create removal orders
        Random r = new Random();
        List<OWLAxiom> removal = new LinkedList<>();
        List<Integer> removalCount = new LinkedList<>();
        List<OWLAxiom> current = new ArrayList<>(o.getLogicalAxioms());
        Set<OWLAxiom> removed = new HashSet<>();
        for(int i = 0; i < iterations; i++){
            Set<OWLAxiom> nremoved = new HashSet<>();
            for(int j = 0; j < change; j++){
                int pos = r.nextInt(current.size());
                nremoved.add(current.get(pos));
                removal.add(current.get(pos));
                current.remove(pos);
            }
            removalCount.add(change);
            current.addAll(removed);
            removed = nremoved;
        }

        message("Starting up");
        Future<IncrTimeResult> f1 = pool.submit(new IncrIncrementalWorker(o, removalCount.iterator(), removal.iterator(), id, false, false));
        Future<IncrTimeResult> f2 = pool.submit(new IncrIncrementalWorker(o, removalCount.iterator(), removal.iterator(), id, true, false));
        Future<IncrTimeResult> f3 = pool.submit(new IncrIncrementalWorker(o, removalCount.iterator(), removal.iterator(), id, true, true));
        return new IncrTimeBothResult(f1.get(), f2.get(), f3.get());
    }
}
