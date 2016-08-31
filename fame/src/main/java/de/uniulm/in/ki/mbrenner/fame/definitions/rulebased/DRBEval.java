package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased;

import de.uniulm.in.ki.mbrenner.fame.definitions.CombinedObjectProperty;
import de.uniulm.in.ki.mbrenner.fame.definitions.IndicatorClass;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.ClassVisitorAdapterEx;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 06.06.2016.
 */
public class DRBEval extends ClassVisitorAdapterEx<OWLClassExpression> {
    //private static OWLClassExpression bot = new OWLDataFactoryImpl().getOWLNothing();
    private Set<OWLObject> inSignature;
    private Set<OWLObject> notBot;
    private Map<OWLObject, OWLObject> isDefinedAs;

    private DRBEval(){}

    public static OWLObject resolve(OWLObject oce, Set<OWLObject> notBot, Set<OWLObject> inSignature, Map<OWLObject, OWLObject> isDefinedAs){
        if(oce instanceof OWLObjectProperty){
            OWLObjectProperty prop = (OWLObjectProperty) oce;
            //if(prop.isBottomEntity() || !notBot.contains(prop)) return bot;
            while(!prop.isTopEntity() && !inSignature.contains(prop) && !(prop instanceof CombinedObjectProperty)){
                prop = (OWLObjectProperty) isDefinedAs.get(prop);
            }
            return prop;
        }

        DRBEval drb = new DRBEval();
        drb.inSignature = inSignature;
        drb.notBot = notBot;
        drb.isDefinedAs = isDefinedAs;
        return ((OWLClassExpression) oce).accept(drb);
    }

    @Override
    public OWLClassExpression visit(OWLObjectIntersectionOf oce){
        Set<OWLClassExpression> intersec = oce.getOperands().stream().map(x -> x.accept(this)).collect(Collectors.toSet());
        if(intersec.size() == 1) return intersec.iterator().next();
        return new OWLObjectIntersectionOfImpl(intersec);
    }

    @Override
    public OWLClassExpression visit(OWLObjectSomeValuesFrom oce){
        OWLObjectPropertyExpression prop = oce.getProperty();
        while(!prop.isTopEntity() && !inSignature.contains(prop) && !(prop instanceof CombinedObjectProperty)){
            prop = (OWLObjectPropertyExpression) isDefinedAs.get(prop);
        }

        OWLClassExpression c = oce.getFiller().accept(this);
        if(c.isTopEntity() && prop.isTopEntity()) return c;

        if(prop instanceof CombinedObjectProperty){
            CombinedObjectProperty cop = (CombinedObjectProperty) prop;
            OWLClassExpression d = cop.getMapping(c);
            if(d != null) return d;
        }

        return new OWLObjectSomeValuesFromImpl(prop, c);
    }

    @Override
    public OWLClassExpression visit(OWLClass oce){
        if(oce.isTopEntity() || inSignature.contains(oce) || oce instanceof IndicatorClass) return oce;
        OWLObject o = isDefinedAs.get(oce);
        if(!(o instanceof OWLClassExpression)) System.out.println(o);
        return ((OWLClassExpression) isDefinedAs.get(oce)).accept(this);
    }
}
