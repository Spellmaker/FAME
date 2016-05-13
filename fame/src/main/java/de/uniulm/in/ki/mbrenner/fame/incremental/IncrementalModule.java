package de.uniulm.in.ki.mbrenner.fame.incremental;

/**
 * Created by spellmaker on 18.03.2016.
 */

import de.uniulm.in.ki.mbrenner.fame.rule.Rule;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 17.03.2016.
 */
public class IncrementalModule implements Iterable<Integer>{
    List<Boolean> known;
    public List<Integer> ruleCounter;
    IncrementalExtractor extractor;
    private Set<Integer> module;
    private Integer baseEntity;

    private IncrementalModule(){

    }

    public Set<Integer> getModule(){
        return module;
    }

    public boolean addAxiom(Integer axiom){
        return module.add(axiom);
    }

    public IncrementalModule getCopy(Integer baseEntity){
        IncrementalModule copy = new IncrementalModule();
        copy.known = new ArrayList<>(known);
        copy.ruleCounter = new ArrayList<>(ruleCounter);
        copy.extractor = extractor;
        copy.module = new HashSet<>(module);
        copy.baseEntity = baseEntity;
        return copy;
    }

    public void replaceWith(IncrementalModule other){
        this.known = other.known;
        this.ruleCounter = other.ruleCounter;
        this.module = other.module;
        this.extractor = other.extractor;
    }

    //convenience methods
    public boolean known(Integer i){
        checkKnown(i);
        return known.get(i);
    }

    public void setKnown(Integer i){
        checkKnown(i);
        known.set(i, true);
    }

    public int ruleCounter(Integer i){
        checkRuleCounter(i);
        return ruleCounter.get(i);
    }

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

    public int size(){
        return module.size();
    }

    public Set<OWLAxiom> getOWLModule(){
        return module.stream().map(x -> (OWLAxiom) extractor.getObject(x)).collect(Collectors.toSet());
    }

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
