package de.uniulm.in.ki.mbrenner.fame.incremental.v2;

import de.uniulm.in.ki.mbrenner.fame.rule.Rule;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 17.03.2016.
 */
public class IncrementalModule implements Iterable<Integer>{
    List<Boolean> known;
    List<Integer> ruleCounter;
    IncrementalExtractor extractor;
    Set<Integer> module;

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

    public void incCounter(Integer i){
        checkRuleCounter(i);
        ruleCounter.set(i, ruleCounter.get(i) + 1);
    }

    private void checkKnown(Integer i){
        if(i >= known.size()){
            for(int j = 0; j <= i - known.size(); j++){
                known.add(false);
            }
        }
    }

    private void checkRuleCounter(Integer i){
        if(i >= ruleCounter.size()){
            for(int j = 0; j <= i - ruleCounter.size(); j++){
                ruleCounter.add(0);
            }
        }
    }

    public IncrementalModule(IncrementalExtractor extractor){
        this.extractor = extractor;
        known = new ArrayList<>(extractor.dictionarySize());
        for(int i = 0; i < extractor.dictionarySize(); i++) known.add(false);
        ruleCounter = new ArrayList<>(extractor.ruleCount());
        for(int i = 0; i < extractor.dictionarySize(); i++) ruleCounter.add(0);
        module = new HashSet<>();
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
            known.set(r.getHeadOrAxiom(), true);
            return true;
        }

        int cnt = 0;
        for(Integer o : r){
            if(known.get(o)){
                cnt++;
            }
        }
        ruleCounter.set(rpos, cnt);
        if(cnt >= r.size()){
            known.set(r.getHeadOrAxiom(), true);
            return true;
        }
        return false;
    }

}
