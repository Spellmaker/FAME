package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.definition;

import de.uniulm.in.ki.mbrenner.fame.util.printer.OWLPrinter;
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
     * There may be additional definitions which further change the value and the definition
     * itself may only use the defining symbol in the construction of the actual value.
     */
    public final OWLObject definingSymbol;
    /**
     * A function which can be used to obtain the value of the defined symbol
     * This value can still be transformed by further definitions, but is also not necessarily equal to the defining symbol
     */
    //public final DefinitionFunction definition;

    /**
     * Constructs a new definition
     * @param definedSymbol The defined symbol
     * @param definingSymbol The symbol which is used to define the defined symbol
     * @ pa ram definition A function which derives the value of the defined symbol
     */
    public DRBDefinition(OWLObject definedSymbol, OWLObject definingSymbol/*, DefinitionFunction definition*/){
        this.definedSymbol = definedSymbol;
        //this.definition = definition;
        this.definingSymbol = definingSymbol;
    }

    @Override
    public String toString(){
        return "[" + OWLPrinter.getString(definedSymbol) + " -> " + definingSymbol + "]";//definition.toString() + "]";
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

