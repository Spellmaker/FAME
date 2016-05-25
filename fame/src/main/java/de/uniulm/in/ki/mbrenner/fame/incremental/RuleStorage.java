package de.uniulm.in.ki.mbrenner.fame.incremental;

import de.uniulm.in.ki.mbrenner.fame.simple.rule.Rule;

/**
 * Stores rules
 *
 * Created by spellmaker on 18.03.2016.
 */
public interface RuleStorage {
    /**
     * Adds a rule to the storage
     * @param cause The axiom generating the rule
     * @param r The rule
     * @return The index assigned to the rule in the set
     */
    int addRule(Integer cause, Rule r);

    /**
     * Provides access to the number of rules in the set
     * @return The number of rules in the set
     */
    int ruleCount();

    /**
     * Finds the index of the given rule in the set
     * @param r A rule
     * @return The index of the rule
     */
    int findRule(Rule r);

    /**
     * Finalizes the set for further use
     * This may involve optimization of data structures or other operations
     * and should be performed after all rules have been added
     */
    void finalizeSet();
}
