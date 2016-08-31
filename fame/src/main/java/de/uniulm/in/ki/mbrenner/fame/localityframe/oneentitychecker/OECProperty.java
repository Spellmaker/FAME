package de.uniulm.in.ki.mbrenner.fame.localityframe.oneentitychecker;

import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.PropertyVisitorAdapterEx;
import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * Created by spellmaker on 13.06.2016.
 */
public class OECProperty extends PropertyVisitorAdapterEx<OECValue> {
    private OneEntityChecker parent;

    public OECProperty(OneEntityChecker parent){
        this.parent = parent;
    }

    @Override
    public OECValue visit(OWLObjectProperty property){
        if(parent.signature.contains(property)){
            return OECValue.UNKNOWN;
        }
        else{
            return OECValue.TOP;
        }
    }
}
