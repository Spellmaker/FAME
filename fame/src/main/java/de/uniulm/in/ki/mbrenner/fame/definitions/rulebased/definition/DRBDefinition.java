package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.definition;

import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.OWLRendererProvider;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * Created by Spellmaker on 13.05.2016.
 */
public class DRBDefinition {
    public final OWLObject definedSymbol;
    public final DefinitionFunction definition;
    public final OWLObject definingSymbol;

    public DRBDefinition(OWLObject definedSymbol, OWLObject definingSymbol, DefinitionFunction definition){
        this.definedSymbol = definedSymbol;
        this.definition = definition;
        this.definingSymbol = definingSymbol;
    }

    @Override
    public String toString(){
        return "[" + OWLRendererProvider.render(definedSymbol) + " -> " + definition.toString() + "]";
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof DRBDefinition){
            DRBDefinition other = (DRBDefinition) o;
            return other.definedSymbol.equals(definedSymbol) &&
                    other.definition.equals(definition) &&
                    other.definingSymbol.equals(definingSymbol);
        }
        return false;
    }
}

@FunctionalInterface
interface DefinitionFunction{
    OWLObject action(OWLObject object);
}