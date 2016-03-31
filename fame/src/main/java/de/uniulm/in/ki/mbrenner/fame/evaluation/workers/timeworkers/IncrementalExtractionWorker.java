package de.uniulm.in.ki.mbrenner.fame.evaluation.workers.timeworkers;

import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.RandTimeWorker;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.IncrementalModule;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by spellmaker on 30.03.2016.
 */
public class IncrementalExtractionWorker implements Callable<Long[]> {
    private OWLEntity sign;
    private IncrementalExtractor rules;
    private int ind;

    public IncrementalExtractionWorker(Set<OWLEntity> sign, IncrementalExtractor ie, int ind){
        this.sign = sign.iterator().next();
        this.rules = ie;
        this.ind = ind;
    }

    @Override
    public Long[] call() throws Exception {
        IncrementalModule im = null;
        long start = System.currentTimeMillis();
        for(int i = 0; i < RandTimeWorker.iterations; i++){
            im = rules.extractModule(sign);
        }
        long end = System.currentTimeMillis();
        Long[] res = new Long[2];
        res[0] = (long) ind;
        res[1] = end - start;
        return res;
    }
}
