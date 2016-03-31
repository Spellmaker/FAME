package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.IncrCorrectnessResult;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.IncrTimeBothResult;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.IncrTimeResult;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.IncrementalModule;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import de.uniulm.in.ki.mbrenner.fame.util.ModuleDiff;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;

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
        message("Starting up");
        Future<IncrTimeResult> f1 = pool.submit(new IncrIncrementalWorker(f, change, iterations, id));
        Future<IncrTimeResult> f2 = pool.submit(new RBMEIncrementalWorker(f, change, iterations, id));
        return new IncrTimeBothResult(f1.get(), f2.get());
    }
}
