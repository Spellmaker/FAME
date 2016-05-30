package de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule;

import de.uniulm.in.ki.mbrenner.fame.util.printer.OWLPrinter;
import org.semanticweb.owlapi.metrics.IntegerValuedMetric;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * Stores all knowledge about a specific definition
 *
 * Created by Spellmaker on 13.05.2016.
 */
public class IDRBDefinition {
    /**
     * The symbol which is defined by this definition
     */
    public final Integer definedSymbol;
    /**
     * The symbol which defines in this definition
     * Note that this is not necessarily the final value which the definedSymbol assumes.
     * There may be additional definitions which further change the value
     */
    public final Integer definingSymbol;

    /**
     * Constructs a new definition
     * @param definedSymbol The defined symbol
     * @param definingSymbol The symbol as which the defined symbol is defined as
     */
    public IDRBDefinition(Integer definedSymbol, Integer definingSymbol){
        this.definedSymbol = definedSymbol;
        //this.definition = definition;
        this.definingSymbol = definingSymbol;
    }

    @Override
    public String toString(){
        return "[" + definedSymbol + " -> " + definingSymbol + "]";
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof IDRBDefinition){
            IDRBDefinition other = (IDRBDefinition) o;
            return other.definedSymbol.equals(definedSymbol) &&
                    //other.definition.equals(definition) &&
                    other.definingSymbol.equals(definingSymbol);
        }
        return false;
    }
}

