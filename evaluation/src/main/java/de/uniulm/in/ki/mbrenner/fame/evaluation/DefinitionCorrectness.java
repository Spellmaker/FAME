package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.uniulm.in.ki.mbrenner.fame.definitions.TestCorrectness;
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
import de.uniulm.in.ki.mbrenner.owlprinter.OWLPrinter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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
}

class DefinitionCorrectnessResult extends WorkerResult{
    public List<OWLEntity> drbNotLocal = new LinkedList<>();
    @PrintField
    public long DRBNotLocal(){
        return drbNotLocal.size();
    }
    public List<OWLEntity> drbNotProperSubset = new LinkedList<>();
    @PrintField
    public long DRBNotProperSubset(){
        return drbNotProperSubset.size();
    }
    public List<OWLEntity> idrbNotLocal = new LinkedList<>();
    @PrintField
    public long IDRBNotLocal(){
        return idrbNotLocal.size();
    }
    public List<OWLEntity> idrbNotProperSubset = new LinkedList<>();
    @PrintField
    public long IDRBNotProperSubset(){
        return idrbNotProperSubset.size();
    }
    public List<OWLEntity> differing = new LinkedList<>();
    @PrintField
    public long Differing(){
        return differing.size();
    }
    public DefinitionCorrectnessResult(OntologyWorker<?> worker) {
        super(worker);
    }
}


class DefinitionCorrectnessWorker extends OntologyWorker<DefinitionCorrectnessResult> {
    public DefinitionCorrectnessWorker(File f){
        super(f);
    }

    @Override
    public DefinitionCorrectnessResult process(OWLOntology o) throws Exception {
        DRBRuleSet drb = new DRBRuleBuilder().buildRules(o);
        IDRBRuleSet idrb = new IDRBRuleBuilder().buildRules(o);
        DefinitionCorrectnessResult result = new DefinitionCorrectnessResult(this);
        int i = 0;
        for(OWLEntity e : o.getSignature()){
            DRBExtractor drbExtractor = new DRBExtractor();
            IDRBExtractor idrbExtractor = new IDRBExtractor();
            Set<OWLAxiom> module = drbExtractor.extractModule(drb, Collections.singleton(e));
            if (!TestCorrectness.isDefinitionLocalModule(o, module, drbExtractor.getDefinitions(), Collections.singleton(e)).isEmpty()) {
                result.drbNotLocal.add(e);
            }
            if (!TestCorrectness.isProperSubset(o, module, Collections.singleton(e)).isEmpty()) {
                result.drbNotProperSubset.add(e);
            }

            Set<OWLAxiom> imodule = idrbExtractor.extractModule(idrb, Collections.singleton(e)).stream().map(x -> (OWLAxiom) idrb.getObject(x)).collect(Collectors.toSet());
            module = module.stream().filter(x -> x instanceof OWLLogicalAxiom).collect(Collectors.toSet());
            if(!imodule.equals(module)){
                result.differing.add(e);
            }
            if (!TestCorrectness.isDefinitionLocalModule(o, imodule, idrbExtractor.getDefinitions(), Collections.singleton(e)).isEmpty()) {
                result.idrbNotLocal.add(e);
            }
            if (!TestCorrectness.isProperSubset(o, imodule, Collections.singleton(e)).isEmpty()) {
                result.idrbNotProperSubset.add(e);
            }

            //if(i++ > 2000) break;
        }
        EvaluationMain.out.println("completed def-locality");
        return result;
    }
}
