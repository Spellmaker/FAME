package de.uniulm.in.ki.mbrenner.fame.util;

import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.ClassVisitorAdapter;
import org.semanticweb.owlapi.model.*;

import java.util.*;

/**
 * Class Visitor for the IDRBRuleBuilder
 *
 * Created by Spellmaker on 13.05.2016.
 */
public class ScanSomeValuesFrom extends ClassVisitorAdapter {
    private Map<OWLObjectProperty, Set<OWLClassExpression>> map;

    private ScanSomeValuesFrom(){
        map = new HashMap<>();
    }

    public static Map<OWLObjectProperty, Set<OWLClassExpression>> getRelations(OWLClassExpression expression){
        ScanSomeValuesFrom scan = new ScanSomeValuesFrom();
        expression.accept(scan);
        return scan.map;
    }


    @Override
    public void visit(OWLObjectSomeValuesFrom expression){
        Set<OWLClassExpression> s = map.get((OWLObjectProperty) expression.getProperty());
        if(s == null){
            s = new HashSet<>();
            map.put((OWLObjectProperty) expression.getProperty(), s);
        }
        s.add(expression.getFiller());
        expression.getFiller().accept(this);
    }

    @Override
    public void visit(OWLObjectIntersectionOf expression){
        expression.getOperands().forEach(x -> x.accept(this));
    }

    @Override
    public void visit(OWLClass expression){
    }

    @Override
    public void visit(OWLObjectHasValue expression){
    }

    @Override
    public void visit(OWLObjectOneOf expression){
    }
}
