package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Helper class which eases the creation of IDRBRule instances without adding too many constructors to the class
 *
 * Created by Spellmaker on 13.05.2016.
 */
public class DRBRuleFactory {
    /**
     * Constructs an internal rule, which does not add an axiom but an intermediate symbol
     * @param head The head of the rule
     * @param body The body of the rule
     * @return A new rule with the provided parameters
     */
    public static DRBRule getInternalRule(@Nonnull OWLObject head, @Nonnull OWLObject...body){
        return new DRBRule(head, null, Collections.emptySet(), body);
    }

    /**
     * Constructs an external rule with definitions, which adds an axiom
     * @param axiom The axiom added by the rule
     * @param definitions The definitions associated with the rule
     * @param body The body of the rule
     * @return A new rule with the provided parameters
     */
    public static DRBRule getExternalRule(@Nonnull OWLAxiom axiom, @Nonnull Set<DRBDefinition> definitions, @Nonnull OWLObject...body){
        return new DRBRule(null, axiom, definitions, body);
    }

    /**
     * Constructs an external rule which adds an axiom
     * @param axiom The axiom added by the rule
     * @param body The body of the rule
     * @return A new rule with the provided parameters
     */
    public static DRBRule getExternalRule(@Nonnull OWLAxiom axiom, @Nonnull OWLObject...body){
        return new DRBRule(null, axiom, Collections.emptySet(), body);
    }
}
