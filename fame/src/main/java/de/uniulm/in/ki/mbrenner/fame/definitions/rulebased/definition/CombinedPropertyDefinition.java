package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.definition;

import de.uniulm.in.ki.mbrenner.fame.definitions.CombinedObjectProperty;
import de.uniulm.in.ki.mbrenner.fame.definitions.IndicatorClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * Created by Spellmaker on 13.05.2016.
 */
public class CombinedPropertyDefinition implements DefinitionFunction {
    private IndicatorClass idClass;

    public CombinedPropertyDefinition(IndicatorClass id){
        this.idClass = id;
    }

    @Override
    public OWLObject action(OWLObject object) {
        return new CombinedObjectProperty((OWLClassExpression) object, idClass);
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof CombinedPropertyDefinition){
            CombinedPropertyDefinition other = (CombinedPropertyDefinition) o;
            return other.idClass.equals(idClass);
        }
        return false;
    }

    @Override
    public String toString(){
        return "CProp:" + idClass + "->X";
    }
}
