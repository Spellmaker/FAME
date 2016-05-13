package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule;

import org.semanticweb.owlapi.model.OWLObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Spellmaker on 13.05.2016.
 */
public class DRBRuleSet {
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
}
