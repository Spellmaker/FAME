package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule;

import org.semanticweb.owlapi.model.OWLObject;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by Spellmaker on 13.05.2016.
 */
public class DRBRuleSet implements Iterable<DRBRule>{
    private Set<DRBRule> rules;

    private Map<OWLObject, Set<DRBRule>> ruleMap;
    private Set<DRBRule> baseRules;

    public DRBRuleSet(){
        this.rules = new HashSet<>();
        this.ruleMap = new HashMap<>();
        this.baseRules = new HashSet<>();
    }

    public void addRule(DRBRule rule){
        if(rule.body.isEmpty()){
            //base rule, always executed
            baseRules.add(rule);
        }
        else if(rules.add(rule)){
            for(OWLObject o : rule){
                Set<DRBRule> current = ruleMap.getOrDefault(o, new HashSet<>());
                current.add(rule);
                ruleMap.put(o, current);
            }
        }
    }

    @Override
    public Iterator<DRBRule> iterator() {
        return rules.iterator();
    }

    @Override
    public void forEach(Consumer<? super DRBRule> action) {
        rules.forEach(action);
    }

    @Override
    public Spliterator<DRBRule> spliterator() {
        return rules.spliterator();
    }
}
