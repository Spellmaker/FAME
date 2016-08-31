package de.uniulm.in.ki.mbrenner.fame.localityframe.oneentitychecker;

import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.ClassVisitorAdapterEx;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;

/**
 * Created by spellmaker on 13.06.2016.
 */
public class OECClass extends ClassVisitorAdapterEx<OECValue> {
    private OneEntityChecker parent;

    public OECClass(OneEntityChecker parent){
        this.parent = parent;
    }

    @Override
    public @Nonnull OECValue visit(@Nonnull OWLObjectSomeValuesFrom oce){
        OECValue prop = oce.getProperty().accept(parent.propertyVisitor);
        OECValue fill = oce.getFiller().accept(parent.classVisitor);

        switch(prop) {
            case TOP:
                switch (fill) {
                    case BOT:
                        return OECValue.BOT;
                    case UNKNOWN:
                        return OECValue.UNKNOWN;
                    default:
                        return OECValue.TOP;
                }

            case UNKNOWN:
                switch (fill) {
                    case BOT:
                        return OECValue.BOT;
                    default:
                        if(parent.isBot((OWLObjectProperty) oce.getProperty(), oce.getFiller()))
                            return OECValue.BOT;
                        return OECValue.UNKNOWN;
                }
            default:
                throw new IllegalArgumentException("This case should never occur");
        }
    }

    @Override
    public @Nonnull OECValue visit(@Nonnull OWLObjectIntersectionOf oce){
        OECValue current = OECValue.TOP;
        for(OWLClassExpression c : oce.getOperands()){
            OECValue next = c.accept(parent.classVisitor);
            switch(next){
                case BOT:
                    return OECValue.BOT;
                case TOP:
                    break;
                case X:
                    break;
                case AT_LEAST_ONE:
                    if(current.equals(OECValue.TOP)){
                        current = OECValue.AT_LEAST_ONE;
                    }
                    else if(current.equals(OECValue.X)){
                        current = OECValue.UNKNOWN;
                    }
                    break;
                case UNKNOWN:
                    current = OECValue.UNKNOWN;
                    break;
            }
        }
        return current;
    }

    @Override
    public OECValue visit(@Nonnull OWLClass c){
        if(parent.signature.contains(c)) return OECValue.AT_LEAST_ONE;
        else return OECValue.X;
    }
}
