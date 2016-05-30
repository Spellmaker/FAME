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

public class DefinitionSizeComparison implements SingleLevelWorkerFactory<DefinitionSizeResult>{
    private List<String> lines;

    public DefinitionSizeComparison(){
        lines = new LinkedList<>();
    }


    @Override
    public Callable<DefinitionSizeResult> getWorker(File file, List<String> options) {
        return new DefinitionSizeComparisonWorker(file);
    }

    @Override
    public String getGreeting() {
        return "Starting module size comparison for bottom locality and definition locality";
    }

    @Override
    public String newResult(DefinitionSizeResult result, int finishedTasks, int maxTasks) {
        EvaluationMain.out.println("Finished task " + finishedTasks + " of " + maxTasks);
        EvaluationMain.out.println("bot locality: " + result.botLocSize + " def locality: " + result.defLocSize);
        String s = result.botLocSize + ";" + result.defLocSize;
        lines.add(s);
        return s;
    }

    @Override
    public void printFinalResult() {
        EvaluationMain.out.println("bot locality;def locality");
        lines.forEach(EvaluationMain.out::println);
    }
}

class DefinitionSizeResult{
    public long botLocSize;
    public long defLocSize;
}


class DefinitionSizeComparisonWorker implements Callable<DefinitionSizeResult>{
    private File file;

    public DefinitionSizeComparisonWorker(File f){
        this.file = f;
    }

    @Override
    public DefinitionSizeResult call() throws Exception {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.loadOntologyFromOntologyDocument(file);

        IncrementalExtractor iExtractor = new IncrementalExtractor(o);
        DRBRuleSet drb = new DRBRuleBuilder().buildRules(o);

        DefinitionSizeResult result = new DefinitionSizeResult();

        int i = 0;
        for(OWLEntity e : o.getSignature()){
            IncrementalModule module = iExtractor.extractModuleStatic(Collections.singleton(e));
            result.botLocSize += module.size();
            if(i++ > 2000) break;
        }

        EvaluationMain.out.println("completed bot-locality");

        i = 0;
        for(OWLEntity e : o.getSignature()){
            Set<OWLAxiom> module = new DRBExtractor().extractModule(drb, Collections.singleton(e));
            result.defLocSize += module.size();
            if(i++ > 2000) break;
        }
        EvaluationMain.out.println("completed def-locality");
        return result;
    }
}