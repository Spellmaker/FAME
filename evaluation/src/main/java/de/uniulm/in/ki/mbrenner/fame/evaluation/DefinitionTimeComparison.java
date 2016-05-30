package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.DRBExtractor;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleSet;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder.DRBRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.SingleLevelWorkerFactory;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalModule;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
        return new DefinitionTimeComparisonWorker(file);
    }

    @Override
    public String getGreeting() {
        return "Starting extraction time comparison for bottom locality and definition locality";
    }

    @Override
    public String newResult(DefinitionTimeResult result, int finishedTasks, int maxTasks) {
        EvaluationMain.out.println("Finished task " + finishedTasks + " of " + maxTasks);
        EvaluationMain.out.println("bot locality: " + result.botLocTime + " def locality: " + result.defLocTime);
        String s = result.botLocTime + ";" + result.defLocTime;
        lines.add(s);
        return s;
    }

    @Override
    public void printFinalResult() {
        EvaluationMain.out.println("bot locality;def locality");
        lines.forEach(EvaluationMain.out::println);
    }
}

class DefinitionTimeResult{
    public long botLocTime;
    public long defLocTime;
}


class DefinitionTimeComparisonWorker implements Callable<DefinitionTimeResult>{
    private File file;

    public DefinitionTimeComparisonWorker(File f){
        this.file = f;
    }

    @Override
    public DefinitionTimeResult call() throws Exception {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.loadOntologyFromOntologyDocument(file);

        IncrementalExtractor iExtractor = new IncrementalExtractor(o);
        DRBRuleSet drb = new DRBRuleBuilder().buildRules(o);

        DefinitionTimeResult result = new DefinitionTimeResult();

        long start, end;

        int i = 0;
        start = System.currentTimeMillis();
        for(OWLEntity e : o.getSignature()){
            Set<OWLAxiom> module = iExtractor.extractModuleStatic(Collections.singleton(e)).getOWLModule();
            //EvaluationMain.out.println(i + "/" + o.getSignature().size());
            //EvaluationMain.out.println(e);
            if(i++ > 2000) break;
        }
        end = System.currentTimeMillis();
        result.botLocTime = end - start;

        EvaluationMain.out.println("completed bot-locality");

        i = 0;
        start = System.currentTimeMillis();
        for(OWLEntity e : o.getSignature()){
            Set<OWLAxiom> module = new DRBExtractor().extractModule(drb, Collections.singleton(e));
            //EvaluationMain.out.println(i + "/" + o.getSignature().size());
            if(i++ > 2000) break;
        }
        end = System.currentTimeMillis();
        result.defLocTime = end - start;
        EvaluationMain.out.println("completed def-locality");
        return result;
    }
}