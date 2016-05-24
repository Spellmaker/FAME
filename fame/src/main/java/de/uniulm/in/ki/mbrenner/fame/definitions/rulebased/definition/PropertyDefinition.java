package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.definition;

import org.semanticweb.owlapi.model.OWLObject;

/**
 * Created by spellmaker on 24.05.2016.
 */
public class PropertyDefinition implements DefinitionFunction {
    @Override
    public OWLObject action(OWLObject object) {
        return object;
    }

    @Override
    public String toString(){
        return "X";
    }

    @Override
    public boolean equals(Object o){
        return o instanceof PropertyDefinition;
    }
}
