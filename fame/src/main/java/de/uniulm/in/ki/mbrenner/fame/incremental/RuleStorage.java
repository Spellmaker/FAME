package de.uniulm.in.ki.mbrenner.fame.incremental;

import de.uniulm.in.ki.mbrenner.fame.simple.rule.Rule;

/**
 * Created by spellmaker on 18.03.2016.
 */
public interface RuleStorage {
    public int addRule(Integer cause, Rule r);
    public int ruleCount();
    public int findRule(Rule r);
    public void finalizeSet();
}
