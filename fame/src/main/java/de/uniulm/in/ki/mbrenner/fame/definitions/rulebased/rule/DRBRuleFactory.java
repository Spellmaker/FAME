package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule;

import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.definition.DRBDefinition;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Created by Spellmaker on 13.05.2016.
 */
public class DRBRuleFactory {
    public static DRBRule getRule(OWLObject head, OWLAxiom axiom, @Nonnull Set<DRBDefinition> definitions, @Nonnull OWLObject...body){
        return new DRBRule(head, axiom, definitions, body);
    }

    public static DRBRule getInternalRule(@Nonnull OWLObject head, @Nonnull OWLObject...body){
        return new DRBRule(head, null, Collections.emptySet(), body);
    }

    public static DRBRule getExternalRule(@Nonnull OWLAxiom axiom, @Nonnull Set<DRBDefinition> definitions, @Nonnull OWLObject...body){
        return new DRBRule(null, axiom, definitions, body);
    }

    public static DRBRule getExternalRule(@Nonnull OWLAxiom axiom, @Nonnull OWLObject...body){
        return new DRBRule(null, axiom, Collections.emptySet(), body);
    }
}
