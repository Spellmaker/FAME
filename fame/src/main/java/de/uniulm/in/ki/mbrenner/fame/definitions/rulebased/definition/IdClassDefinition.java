package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.definition;

import de.uniulm.in.ki.mbrenner.fame.definitions.IndicatorClass;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * Created by Spellmaker on 13.05.2016.
 */
public class IdClassDefinition implements DefinitionFunction{
    private IndicatorClass id;

    public IdClassDefinition(IndicatorClass id){
        this.id = id;
    }

    @Override
    public OWLObject action(OWLObject object) {
        return id;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof IdClassDefinition){
            return ((IdClassDefinition) o).id.equals(id);
        }
        return false;
    }

    @Override
    public String toString(){
        return id.toString();
    }
}
