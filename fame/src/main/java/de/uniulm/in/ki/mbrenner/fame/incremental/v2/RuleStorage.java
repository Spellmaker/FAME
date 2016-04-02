package de.uniulm.in.ki.mbrenner.fame.incremental.v2;

import de.uniulm.in.ki.mbrenner.fame.rule.Rule;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Created by spellmaker on 18.03.2016.
 */
public interface RuleStorage {
    public int addRule(Integer cause, Rule r);
    public int ruleCount();
    public int findRule(Rule r);
    public void finalize();
}
