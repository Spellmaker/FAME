package de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule;

import de.uniulm.in.ki.mbrenner.fame.incremental.OWLDictionary;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

/**
 * Helper class which eases the creation of IDRBRule instances without adding too many constructors to the class
 *
 * Created by Spellmaker on 13.05.2016.
 */
public class IDRBRuleFactory {
    /**
     * Constructs an internal rule, which does not add an axiom but an intermediate symbol
     * @param head The head of the rule
     * @param body The body of the rule
     * @return A new rule with the provided parameters
     */
    public static IDRBRule getInternalRule(@Nonnull OWLDictionary provider, @Nonnull OWLObject head, @Nonnull OWLObject...body){
        return new IDRBRule(provider.getId(head), null, Collections.emptySet(), convert(body, provider));
    }

    /**
     * Constructs an external rule with definitions, which adds an axiom
     * @param axiom The axiom added by the rule
     * @param definitions The definitions associated with the rule
     * @param body The body of the rule
     * @return A new rule with the provided parameters
     */
    public static IDRBRule getExternalRule(@Nonnull OWLDictionary provider, @Nonnull OWLObject axiom, @Nonnull Set<IDRBDefinition> definitions, @Nonnull OWLObject...body){
        return new IDRBRule(null, provider.getId(axiom), definitions, convert(body, provider));
    }

    /**
     * Constructs an external rule which adds an axiom
     * @param axiom The axiom added by the rule
     * @param body The body of the rule
     * @return A new rule with the provided parameters
     */
    public static IDRBRule getExternalRule(@Nonnull OWLDictionary provider, @Nonnull OWLObject axiom, @Nonnull OWLObject...body){
        return new IDRBRule(null, provider.getId(axiom), Collections.emptySet(), convert(body, provider));
    }

    private static Integer[] convert(OWLObject[] body, OWLDictionary provider){
        Integer arr[] = new Integer[body.length];
        int pos = 0;
        for(OWLObject o : body) arr[pos++] = provider.getId(o);
        return arr;
    }
}
