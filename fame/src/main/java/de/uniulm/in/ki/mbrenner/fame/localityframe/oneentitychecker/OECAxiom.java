package de.uniulm.in.ki.mbrenner.fame.localityframe.oneentitychecker;

import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.AxiomVisitorAdapterEx;
import org.semanticweb.owlapi.model.*;

import java.util.Iterator;

/**
 * Created by spellmaker on 13.06.2016.
 */
public class OECAxiom extends AxiomVisitorAdapterEx<Boolean> {
    private OneEntityChecker parent;

    public OECAxiom(OneEntityChecker parent){
        this.parent = parent;
    }

    @Override
    public Boolean visit(OWLSubClassOfAxiom axiom){
        OECValue sub = axiom.getSubClass().accept(parent.classVisitor);
        if(sub.equals(OECValue.BOT)) return true;
        OECValue sup = axiom.getSuperClass().accept(parent.classVisitor);
        return sup.equals(OECValue.TOP) || (sub.equals(OECValue.X) && sup.equals(OECValue.X));
    }

    @Override
    public Boolean visit(OWLEquivalentClassesAxiom axiom){
        Iterator<OWLClassExpression> iter = axiom.getClassExpressions().iterator();
        OECValue val = iter.next().accept(parent.classVisitor);
        if(val.equals(OECValue.AT_LEAST_ONE) || val.equals(OECValue.UNKNOWN)) return false;
        while(iter.hasNext()){
            OECValue next = iter.next().accept(parent.classVisitor);
            if(next.equals(OECValue.AT_LEAST_ONE) || next.equals(OECValue.UNKNOWN) || !next.equals(val)) return false;
        }
        return true;
    }

    @Override
    public Boolean visit(OWLDeclarationAxiom axiom){
        return !parent.signature.contains(axiom.getEntity());
    }

    @Override
    public Boolean visit(OWLTransitiveObjectPropertyAxiom axiom){
        return axiom.getProperty().accept(parent.propertyVisitor).equals(OECValue.TOP);
    }

    @Override
    public Boolean visit(OWLSubObjectPropertyOfAxiom axiom){
        OECValue sub = axiom.getSubProperty().accept(parent.propertyVisitor);
        OECValue sup = axiom.getSuperProperty().accept(parent.propertyVisitor);
        return sup.equals(OECValue.TOP);
    }
}
