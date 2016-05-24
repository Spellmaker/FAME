package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule;

import org.semanticweb.owlapi.model.OWLObject;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a set of DRBRules
 *
 * Internally manages data structures for fast access to the rules which are affected by the addition of a specific symbol
 *
 * Created by Spellmaker on 13.05.2016.
 */
public class DRBRuleSet implements Iterable<DRBRule>{
    private Set<DRBRule> rules;

    private Map<OWLObject, Set<DRBRule>> ruleMap;
    private Set<DRBRule> baseRules;

    /**
     * Default constructor
     */
    public DRBRuleSet(){
        this.rules = new HashSet<>();
        this.ruleMap = new HashMap<>();
        this.baseRules = new HashSet<>();
    }

    /**
     * Adds a rule to the set
     *
     * No changes are done if the rule is already part of the set.
     * If the rule is new, it is also assign an id unique within this set.
     * Note that this id shouldn't be tampered with, although it could be done
     * @param rule A rule
     */
    public void addRule(@Nonnull DRBRule rule){
        if(rule.body.isEmpty()){
            //base rule, always executed
            baseRules.add(rule);
        }
        else if(rules.add(rule)){
            rule.id = rules.size() - 1;
            for(OWLObject o : rule){
                Set<DRBRule> current = ruleMap.getOrDefault(o, new HashSet<>());
                current.add(rule);
                ruleMap.put(o, current);
            }
        }
    }

    /**
     * Finds all rules which have a specific object in their body
     * @param o The object in question
     * @return An iterator on a set of all rules in question
     */
    public Iterator<DRBRule> rulesForObjects(@Nonnull OWLObject o){
        Set<DRBRule> s = ruleMap.get(o);
        if(s == null) return Collections.emptyIterator();
        return s.iterator();
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

    /**
     * Returns the size of the set
     * @return The number of rules in this set
     */
    public int size(){
        return rules.size();
    }
}
