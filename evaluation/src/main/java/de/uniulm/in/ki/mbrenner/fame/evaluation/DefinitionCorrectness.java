package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.uniulm.in.ki.mbrenner.fame.definitions.TestCorrectness;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.DRBExtractor;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleSet;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder.DRBRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.SingleLevelWorkerFactory;
import de.uniulm.in.ki.mbrenner.fame.util.printer.OWLPrinter;
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
 * Created by spellmaker on 30.05.2016.
 */
public class DefinitionCorrectness implements SingleLevelWorkerFactory<DefinitionCorrectnessResult> {
    private List<String> lines;

    public DefinitionCorrectness(){
        lines = new LinkedList<>();
    }


    @Override
    public Callable<DefinitionCorrectnessResult> getWorker(File file, List<String> options) {
        return new DefinitionCorrectnessWorker(file);
    }

    @Override
    public String getGreeting() {
        return "Starting module correctness check for definition locality";
    }

    @Override
    public String newResult(DefinitionCorrectnessResult result, int finishedTasks, int maxTasks) {
        EvaluationMain.out.println("Finished task " + finishedTasks + " of " + maxTasks);
        EvaluationMain.out.println("Problematic modules total " + (result.notProperSubset.size() + result.notLocal.size()));
        EvaluationMain.out.println("Not local: " + result.notLocal.size());
        result.notLocal.forEach(x -> EvaluationMain.out.println(OWLPrinter.getString(x)));
        EvaluationMain.out.println("Not proper subsets: " + result.notProperSubset.size());
        result.notProperSubset.forEach(x -> EvaluationMain.out.println(OWLPrinter.getString(x)));

        String s = result.notLocal.size() + ";" + result.notProperSubset.size();
        lines.add(s);
        return s;
    }

    @Override
    public void printFinalResult() {
        EvaluationMain.out.println("not local;not proper subsets");
        lines.forEach(EvaluationMain.out::println);
    }
}

class DefinitionCorrectnessResult{
    public List<OWLEntity> notLocal = new LinkedList<>();
    public List<OWLEntity> notProperSubset = new LinkedList<>();
}


class DefinitionCorrectnessWorker implements Callable<DefinitionCorrectnessResult>{
    private File file;

    public DefinitionCorrectnessWorker(File f){
        this.file = f;
    }

    @Override
    public DefinitionCorrectnessResult call() throws Exception {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.loadOntologyFromOntologyDocument(file);

        DRBRuleSet drb = new DRBRuleBuilder().buildRules(o);
        DefinitionCorrectnessResult result = new DefinitionCorrectnessResult();

        int i = 0;
        for(OWLEntity e : o.getSignature()){
            DRBExtractor drbExtractor = new DRBExtractor();
            Set<OWLAxiom> module = drbExtractor.extractModule(drb, Collections.singleton(e));
            if(!TestCorrectness.isDefinitionLocalModule(o, module, drbExtractor.getDefinitions(), Collections.singleton(e)).isEmpty()){
                result.notLocal.add(e);
            }
            if(!TestCorrectness.isProperSubset(o, module, Collections.singleton(e)).isEmpty()){
                result.notProperSubset.add(e);
            }
            if(i++ > 2000) break;
        }
        EvaluationMain.out.println("completed def-locality");
        return result;
    }
}
