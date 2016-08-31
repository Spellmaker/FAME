package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule;

import de.uniulm.in.ki.mbrenner.owlprinter.OWLPrinter;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * Stores all knowledge about a specific definition
 *
 * Created by Spellmaker on 13.05.2016.
 */
public class DRBDefinition {
    /**
     * The symbol which is defined by this definition
     */
    public final OWLObject definedSymbol;
    /**
     * The symbol which defines in this definition
     * Note that this is not necessarily the final value which the definedSymbol assumes.
     * There may be additional definitions which further change the value
     */
    public final OWLObject definingSymbol;

    /**
     * Constructs a new definition
     * @param definedSymbol The defined symbol
     * @param definingSymbol The symbol as which the defined symbol is defined as
     */
    public DRBDefinition(OWLObject definedSymbol, OWLObject definingSymbol){
        this.definedSymbol = definedSymbol;
        //this.definition = definition;
        this.definingSymbol = definingSymbol;
    }

    @Override
    public String toString(){
        return "[" + OWLPrinter.getString(definedSymbol) + " -> " + OWLPrinter.getString(definingSymbol) + "]";//definition.toString() + "]";
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof DRBDefinition){
            DRBDefinition other = (DRBDefinition) o;
            return other.definedSymbol.equals(definedSymbol) &&
                    //other.definition.equals(definition) &&
                    other.definingSymbol.equals(definingSymbol);
        }
        return false;
    }
}

