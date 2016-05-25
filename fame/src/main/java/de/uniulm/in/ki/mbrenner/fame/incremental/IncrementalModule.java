package de.uniulm.in.ki.mbrenner.fame.incremental;

import de.uniulm.in.ki.mbrenner.fame.simple.rule.Rule;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Encapsulates all information about a module needed to incrementally extend it upon changes in the original ontology
 *
 * Created by spellmaker on 17.03.2016.
 */
public class IncrementalModule implements Iterable<Integer>{
    private List<Boolean> known;
    private List<Integer> ruleCounter;
    IncrementalExtractor extractor;
    private Set<Integer> module;
    private Integer baseEntity;

    private IncrementalModule(){

    }

    /**
     * Provides access to the internal module in integer representation
     * @return The module represented by this instance
     */
    public Set<Integer> getModule(){
        return module;
    }

    /**
     * Adds an axiom to the module
     * @param axiom An axiom
     */
    public void addAxiom(Integer axiom){
        module.add(axiom);
    }

    /**
     * Creates a new instance and initializes it to be a copy of this module
     * The base entity is replaced with the provided value
     * @param baseEntity A base entity for the copy
     * @return A copy of this instance with a replaced base entity
     */
    public IncrementalModule getCopy(Integer baseEntity){
        IncrementalModule copy = new IncrementalModule();
        copy.known = new ArrayList<>(known);
        copy.ruleCounter = new ArrayList<>(ruleCounter);
        copy.extractor = extractor;
        copy.module = new HashSet<>(module);
        copy.baseEntity = baseEntity;
        return copy;
    }

    /**
     * Determines if the object is unknown
     * Also extends the internal data structures if necessary
     * @param i The index of the object
     * @return True, if the value is set to unknown
     */
    public boolean known(Integer i){
        checkKnown(i);
        return known.get(i);
    }

    /**
     * Sets the unknown status of the provided object
     * Also extends the internal data structures if necessary
     * @param i The index of the element which is to be set unknown
     */
    public void setKnown(Integer i){
        checkKnown(i);
        known.set(i, true);
    }

    /**
     * Provides access to the rule counter of a rule
     * Also extends the internal data structures if necessary
     * @param i The index of the rule
     * @return The number of elements in the rules body which are still interpreted with bottom
     */
    public int ruleCounter(Integer i){
        checkRuleCounter(i);
        return ruleCounter.get(i);
    }

    /**
     * Decreases the counter of the provided rule by 1
     * @param i The index of the rule
     * @return The new value of the rule or 0, if the value was already 0
     */
    public int decCounter(Integer i){
        checkRuleCounter(i);
        int val = ruleCounter.get(i);
        if(val == 0) return 0;
        ruleCounter.set(i, --val);
        return val;
    }

    private void checkKnown(Integer i){
        while(i >= known.size()) known.add(false);
    }

    private void checkRuleCounter(Integer i){
        if(i < ruleCounter.size()) return;
        List<Rule> rl = extractor.getRuleList();
        Iterator<Rule> iter = rl.iterator();

        for(int j = 0; j < ruleCounter.size(); j++){
            iter.next();
        }

        while(i >= ruleCounter.size()){
            ruleCounter.add(iter.next().size());//extractor.getRule(ruleCounter.size()).size());
        }
    }

    /**
     * Default constructor
     * @param extractor An extractor which provides the necessary dictionary and rule sets
     * @param baseEntity The base entity for this module, which is the entity for which the module has been extracted
     */
    public IncrementalModule(IncrementalExtractor extractor, Integer baseEntity){
        this.extractor = extractor;
        known = new ArrayList<>(extractor.dictionarySize());
        for(int i = 0; i < extractor.dictionarySize(); i++) known.add(false);
        ruleCounter = new ArrayList<>(extractor.ruleCount());
        Iterator<Rule> iter = extractor.getRuleList().iterator();
        for(int i = 0; i < extractor.ruleCount(); i++) ruleCounter.add(iter.next().size());
        module = new HashSet<>();
        this.baseEntity = baseEntity;
    }

    /**
     * Provides the base entity of the module
     * @return The base entity of the module
     */
    public Integer getBaseEntity(){
        return baseEntity;
    }

    @Override
    public Iterator<Integer> iterator() {
        return module.iterator();
    }

    @Override
    public void forEach(Consumer<? super Integer> action) {
        module.forEach(action);
    }

    /**
     * Provides the module size
     * @return The size of the module
     */
    public int size(){
        return module.size();
    }

    /**
     * Transforms the internal integer representation of the module into the corresponding OWL objects
     * @return The module represented by this instance
     */
    public Set<OWLAxiom> getOWLModule(){
        return module.stream().map(x -> (OWLAxiom) extractor.getObject(x)).collect(Collectors.toSet());
    }

    /**
     * Applies a single rule to this module
     * This modifies the data structures for the known status and rule counters correspondingly
     * @param r A rule which is to be applied
     * @param rpos The index of the rule w.r.t. to the extractor
     * @return True, if the rule triggered
     */
    public boolean applySingleRule(Rule r, int rpos){
        if(r.size() <= 0){
            setKnown(r.getHeadOrAxiom());
            return true;
        }

        int cnt = 0;
        for(Integer o : r){
            if(known(o)){
                cnt++;
                decCounter(rpos);
            }
        }
        if(cnt >= r.size()){
            setKnown(r.getHeadOrAxiom());
            return true;
        }
        return false;
    }

}
