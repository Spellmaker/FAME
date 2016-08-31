package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.IDRBExtractor;
import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule.IDRBRuleSet;
import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rulebuilder.IDRBRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.DRBExtractor;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleSet;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder.DRBRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.OntologyWorker;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.PrintField;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.SingleLevelWorkerFactory;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.WorkerResult;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalModule;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by spellmaker on 25.05.2016.
 */

public class DefinitionTimeComparison implements SingleLevelWorkerFactory<DefinitionTimeResult>{
    private List<String> lines;

    public DefinitionTimeComparison(){
        lines = new LinkedList<>();
    }


    @Override
    public Callable<DefinitionTimeResult> getWorker(File file, List<String> options) {
        return new DefinitionTimeComparisonWorker(file, Integer.parseInt(options.get(0)), Integer.parseInt(options.get(1)));
    }

    @Override
    public String getGreeting() {
        return "Starting extraction time comparison for bottom locality and definition locality";
    }

    @Override
    public String getHelp(){
        return "Evaluates runtimes of IncrementalExtractor, DRBExtractor and IDRBExtractor.\nParameters: -o <signature count> <signature size>";
    }
}

class DefinitionTimeResult extends WorkerResult{
    @PrintField
    public long botLocTime;
    @PrintField
    public long defLocTime;
    @PrintField
    public long idefLocTime;

    public DefinitionTimeResult(OntologyWorker<?> worker) {
        super(worker);
    }
}


class DefinitionTimeComparisonWorker extends OntologyWorker<DefinitionTimeResult> {
    private int tests;
    private int size;

    public DefinitionTimeComparisonWorker(File f, int tests, int size){
        super(f);
        this.tests = tests;
        this.size = size;
    }

    private Set<OWLEntity> getRandSignature(List<OWLEntity> all, Random rand, int size){
        int max = all.size();
        Set<OWLEntity> sign = new HashSet<>();
        while(sign.size() < size)
            sign.add(all.get(rand.nextInt(max)));
        return sign;
    }

    @Override
    public DefinitionTimeResult process(OWLOntology o) throws Exception {
        EvaluationMain.out.println("Extracting " + tests + " signatures of size " + size);
        IncrementalExtractor iExtractor = new IncrementalExtractor(o);
        DRBRuleSet drb = new DRBRuleBuilder().buildRules(o);
        IDRBRuleSet idrb = new IDRBRuleBuilder().buildRules(o);

        DefinitionTimeResult result = new DefinitionTimeResult(this);

        List<OWLEntity> allEntities = new ArrayList<>(o.getSignature());
        if(allEntities.size() <= 0) return result;
        Random rand = new Random();
        int max = allEntities.size();
        List<Set<OWLEntity>> signatures = new LinkedList<>();
        for(int i = 0; i < tests; i++) signatures.add(getRandSignature(allEntities, rand, size));

        long start, end;

        EvaluationMain.out.println("warning: incremental extractor and idef extractor only deliver integer modules!");
        start = System.currentTimeMillis();
        for(Set<OWLEntity> sig : signatures){
            IncrementalModule module = iExtractor.extractModuleStatic(sig);
        }
        end = System.currentTimeMillis();
        result.botLocTime = end - start;

        EvaluationMain.out.println("completed bot-locality");

        start = System.currentTimeMillis();
        for(Set<OWLEntity> sig : signatures){
            Set<OWLAxiom> module = new DRBExtractor().extractModule(drb, sig);
        }
        end = System.currentTimeMillis();
        result.defLocTime = end - start;
        EvaluationMain.out.println("completed def-locality");

        start = System.currentTimeMillis();
        for(Set<OWLEntity> sig : signatures){
            Set<Integer> module = new IDRBExtractor().extractModule(idrb, sig);
        }
        end = System.currentTimeMillis();
        result.idefLocTime = end - start;
        EvaluationMain.out.println("completed idef-locality");
        return result;
    }
}