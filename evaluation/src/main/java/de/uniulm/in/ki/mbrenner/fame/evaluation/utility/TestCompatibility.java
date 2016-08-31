package de.uniulm.in.ki.mbrenner.fame.evaluation.utility;

import de.uniulm.in.ki.mbrenner.fame.definitions.evaluator.DefinitionEvaluator;
import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rulebuilder.IDRBRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder.DRBRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.OntologyWorker;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.SingleLevelWorkerFactory;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.WorkerResult;
import de.uniulm.in.ki.mbrenner.owlapiaddons.misc.ClassCounter;
import de.uniulm.in.ki.mbrenner.owlapiaddons.misc.ConstructorCounter;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.FindImplementedVisitors;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by spellmaker on 31.05.2016.
 */
public class TestCompatibility implements SingleLevelWorkerFactory<CountClassesResult> {
    private ClassCounter counter = new ClassCounter();


    @Override
    public Callable<CountClassesResult> getWorker(File file, List<String> options) {
        return new ClassCountWorker(file);
    }

    @Override
    public String getGreeting() {
        return "Counting constructors and axioms in ontologies";
    }

    @Override
    public void newResult(CountClassesResult result) {
        counter.addOnce(result.counter);
    }

    @Override
    public void finish() {
        EvaluationMain.out.println("Evaluation finished:");
        counter.forEach(this::println);
        EvaluationMain.out.println("Testing compatibility with predefined visitors:");
        Class<?>[] testClasses = {DefinitionEvaluator.class, IDRBRuleBuilder.class, DRBRuleBuilder.class};

        for(Class<?> c : testClasses){
            EvaluationMain.out.println(c.getName() + ":");
            FindImplementedVisitors.getMissing(c, counter, OWLAnnotationAxiom.class, OWLAnnotationProperty.class).
                    forEach(x -> EvaluationMain.out.println("\tmissing implementation for " + x.getName()));
        }
    }
}

class CountClassesResult extends WorkerResult{
    public ClassCounter counter = new ClassCounter();

    public CountClassesResult(OntologyWorker<?> worker) {
        super(worker);
    }

    @Override
    public String toString(){
        String res = super.toString();
        for(String s : counter){
            res += "\n" + s;
        }
        return res;
    }
}

class ClassCountWorker extends OntologyWorker<CountClassesResult> {
    public ClassCountWorker(File f){
        super(f);
    }

    @Override
    public CountClassesResult process(OWLOntology o) throws Exception {
        CountClassesResult result = new CountClassesResult(this);
        result.counter.addOnce(ConstructorCounter.count(o));
        return result;
    }
}