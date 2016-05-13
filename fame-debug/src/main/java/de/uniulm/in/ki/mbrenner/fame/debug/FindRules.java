package de.uniulm.in.ki.mbrenner.fame.debug;

import de.uniulm.in.ki.mbrenner.fame.rule.CompressedRule;
import de.uniulm.in.ki.mbrenner.fame.rule.CompressedRuleSet;
import de.uniulm.in.ki.mbrenner.fame.rule.Rule;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by spellmaker on 08.03.2016.
 */
public class FindRules {
    public static Set<Rule> findRules(String axiom, RuleSet rs){
        Set<Rule> result = new HashSet<>();
        for(Rule r : rs){
            if(r.getAxiom() != null && r.getAxiom().toString().contains(axiom)){
                result.add(r);
            }
        }
        return result;
    }

    public static Set<CompressedRule> findRules(String axiom, CompressedRuleSet rs){
        Set<CompressedRule> result = new HashSet<>();
        for(CompressedRule r : rs){
            if(r.getHead() != null && r.getHead().toString().contains(axiom)){
                result.add(r);
            }
        }
        return result;
    }
}
