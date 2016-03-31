package de.uniulm.in.ki.mbrenner.fame.evaluation.workers.timeworkers;

import de.tu_dresden.inf.lat.hys.graph_tools.Node;
import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.RandTimeWorker;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.related.HyS.HyS;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by spellmaker on 15.03.2016.
 */
public class HySExtractionWorker implements Callable<Long[]> {
    private HyS hys;
    private Set<OWLEntity> signature;
    private int ind;

    public HySExtractionWorker(HyS hys, Set<OWLEntity> signature, int ind){
        this.hys = hys;
        this.signature = signature;
        this.ind = ind;
    }

    @Override
    public Long[] call() throws Exception {
        Set<OWLAxiom> module = null;
        long start = System.currentTimeMillis();
        for(int i = 0; i < RandTimeWorker.iterations; i++){
            Set<Node> allNodes = hys.getConnectedComponent(signature);
            module = hys.getAxioms(allNodes);
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
