package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBDefinition;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder.DefFinder;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.*;
import de.uniulm.in.ki.mbrenner.fame.util.ScanSomeValuesFrom;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 02.06.2016.
 */
public class SearchCombinedCase implements SingleLevelWorkerFactory<SearchCombinedResult>{
    private long possibleCases = 0;

    @Override
    public Callable<SearchCombinedResult> getWorker(File file, List<String> options) {
        return new SearchCombinedWorker(file);
    }

    @Override
    public String getGreeting() {
        return "Starting to look for combined object property benefit cases";
    }

    @Override
    public void newResult(SearchCombinedResult result) {
        if(result.WorkingProperties() >= 1) possibleCases++;
    }

    @Override
    public void finish() {
        println("Finished evaluation. " + possibleCases + " ontologies had properties which might benefit from the optimization");
    }
}

class SearchCombinedResult extends WorkerResult{
    Map<OWLObjectProperty, Map<OWLClassExpression, Set<OWLClassExpression>>> map;

    @PrintField
    public long PropertyCount(){
        return map.size();
    }

    @PrintField
    public long TrivialProperties(){
        return map.entrySet().stream().filter(x -> x.getValue().entrySet().stream().filter(y -> y .getValue().size() > 1).count() <= 0).filter(x -> x.getValue().size() <= 1).count();
    }

    @PrintField
    public long WorkingProperties(){
        return map.size() - IllegalProperties() - TrivialProperties();
    }

    @PrintField
    public long WorkingAndTrivial() {return WorkingProperties() + TrivialProperties();}

    @PrintField
    public long IllegalProperties(){
        return map.entrySet().stream().
                filter(x -> x.getValue().entrySet().stream().
                        filter(y -> y.getValue().size() > 1).
                        count() > 0).
                count();
    }

    @PrintField
    public double WorkingPropertiesProp(){
        return ((double) WorkingProperties())/((double) PropertyCount());
    }

    @PrintField
    public double IllegalPropertiesProp(){
        return ((double) IllegalProperties())/((double) PropertyCount());
    }

    @PrintField
    public double TrivialPropertiesProp() { return ((double) TrivialProperties()) / ((double) PropertyCount());}

    long axiomCount;

    public SearchCombinedResult(OntologyWorker<?> worker) {
        super(worker);

        this.axiomCount = this.axioms;
    }
}

class SearchCombinedWorker extends OntologyWorker<SearchCombinedResult>{
    public SearchCombinedWorker(File file) {
        super(file);
    }

    @Override
    protected SearchCombinedResult process(OWLOntology ontology) throws Exception {
        SearchCombinedResult result = new SearchCombinedResult(this);

        result.map = new HashMap<>();

        for(OWLAxiom axiom : ontology.getAxioms()){
            if(axiom instanceof OWLSubClassOfAxiom){
                attemptCount(((OWLSubClassOfAxiom) axiom).getSubClass(), ((OWLSubClassOfAxiom) axiom).getSuperClass(), result.map);
            }
            else if(axiom instanceof OWLEquivalentClassesAxiom){
                Iterator<OWLClassExpression> iter = ((OWLEquivalentClassesAxiom) axiom).getClassExpressions().iterator();
                OWLClassExpression first = iter.next();
                OWLClassExpression second = iter.next();
                attemptCount(first, second, result.map);
                attemptCount(second, first, result.map);
            }
        }
        return result;
    }

    private void attemptCount(OWLClassExpression left, OWLClassExpression right, Map<OWLObjectProperty, Map<OWLClassExpression, Set<OWLClassExpression>>> map){
        if(!(left instanceof OWLClass)) return;
        Map<OWLObjectProperty, Set<OWLClassExpression>> scan = ScanSomeValuesFrom.getRelations(right);

        for(Map.Entry<OWLObjectProperty, Set<OWLClassExpression>> entry : scan.entrySet()){
            Map<OWLClassExpression, Set<OWLClassExpression>> tmp = map.get(entry.getKey());
            if(tmp == null){
                tmp = new HashMap<>();
                map.put(entry.getKey(), tmp);
            }

            for(OWLClassExpression oce : entry.getValue()){
                Set<OWLClassExpression> set = tmp.get(oce);
                if(set == null){
                    set = new HashSet<>();
                    tmp.put(oce, set);
                }

                set.add(left);
            }
        }
    }
}