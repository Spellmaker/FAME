package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.definition;

import org.semanticweb.owlapi.model.OWLObject;

/**
 * Created by spellmaker on 20.05.2016.
 */
@FunctionalInterface
public interface DefinitionFunction{
    public OWLObject action(OWLObject object);
}
