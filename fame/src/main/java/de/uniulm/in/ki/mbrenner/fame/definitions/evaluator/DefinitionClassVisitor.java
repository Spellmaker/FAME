package de.uniulm.in.ki.mbrenner.fame.definitions.evaluator;

import de.uniulm.in.ki.mbrenner.fame.definitions.CombinedObjectProperty;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.ClassVisitorAdapter;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectHasValueImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class Visitor for the DefinitionEvaluator
 *
 * Created by spellmaker on 27.04.2016.
 */
class DefinitionClassVisitor extends ClassVisitorAdapter{
    OWLClassExpression currentClass;
    private final DefinitionEvaluator parent;

    public DefinitionClassVisitor(DefinitionEvaluator parent) {
        this.parent = parent;
    }

    @Override
    public void visit(@Nonnull OWLClass owlClass) {
        if(owlClass.isTopEntity()){
            currentClass = owlClass;
            return;
        }
        OWLClassExpression def = (OWLClassExpression) parent.definitions.get(owlClass);

        if(def == null){
            if(parent.isFinalSymbol(owlClass))
                currentClass = owlClass;
            else
                currentClass = parent.data.getOWLNothing();
        }
        else{
            def.accept(this);
            //parent.usedDefinitions.add(owlClass);
        }
    }

    @Override
    public void visit(@Nonnull OWLObjectIntersectionOf owlObjectIntersectionOf) {
        //return definition if there is one
        OWLClassExpression def = (OWLClassExpression) parent.definitions.get(owlObjectIntersectionOf);
        if(def != null){
            currentClass = def;
            def.accept(this);
            //parent.usedDefinitions.add(owlObjectIntersectionOf);
            return;
        }
        boolean foundBot = false;
        Set<OWLClassExpression> values = new HashSet<>();
        //process operands
        for (OWLClassExpression oce : owlObjectIntersectionOf.getOperands()) {
            oce.accept(this);
            if (currentClass.isBottomEntity()) {
                foundBot = true;
                break;
            }
            values.add(currentClass);
        }
        //if one of the operands evaluates to bot, return bot
        if(foundBot){
            currentClass = parent.data.getOWLNothing();
            return;
        }

        //if all operands evaluate to top, return top
        values = values.stream().filter(x -> !x.isTopEntity()).collect(Collectors.toSet());
        if(values.isEmpty()){
            currentClass = parent.data.getOWLThing();
            return;
        }

        //if all operands not evaluating to top evaluate to the same thing, return that thing
        OWLClassExpression first = values.iterator().next();
        if(values.stream().filter(x -> !x.equals(first)).count() <= 0){
            currentClass = first;
            return;
        }

        //if none of the above is applicable, return the replaced expression
        currentClass = parent.data.getOWLObjectIntersectionOf(values);
    }

    @Override
    public void visit(@Nonnull OWLObjectSomeValuesFrom owlObjectSomeValuesFrom) {
        //if there is some definition, return that definition
        OWLClassExpression def = (OWLClassExpression) parent.definitions.get(owlObjectSomeValuesFrom);
        if(def != null){
            def.accept(this);
            return;
        }
        //evaluate operands
        owlObjectSomeValuesFrom.getProperty().accept(parent.propertyVisitor);
        OWLPropertyExpression property = parent.propertyVisitor.currentProperty;
        owlObjectSomeValuesFrom.getFiller().accept(this);

        //if either of the two evaluates to bottom, return bottom
        if(currentClass.isBottomEntity() || property.isBottomEntity()){
            currentClass = parent.data.getOWLNothing();
            return;
        }

        //if both evaluate to top, return top
        if(currentClass.isTopEntity() && property.isTopEntity()){
            currentClass = parent.data.getOWLThing();
            return;
        }

        //if the property is a combined/single property, try to see if there is some special
        //connection to the other class
        if(property instanceof CombinedObjectProperty){
            CombinedObjectProperty cop = (CombinedObjectProperty) property;
            OWLClassExpression target = cop.getMapping(currentClass);
            if(target == null){
                currentClass = new OWLObjectSomeValuesFromImpl((OWLObjectPropertyExpression) property, currentClass);// parent.data.getOWLNothing();
            }
            else{
                currentClass = target;
                if(!parent.isFinalSymbol(target)){
                    target.accept(this);
                }
            }
            return;
        }


        //otherwise return the replaced expression
        currentClass = parent.data.getOWLObjectSomeValuesFrom((OWLObjectPropertyExpression) property, currentClass);
    }

    @Override
    public void visit(OWLObjectHasValue ce){
        ce.getProperty().accept(parent.propertyVisitor);
        if(parent.propertyVisitor.currentProperty.isBottomEntity()) currentClass = parent.data.getOWLNothing();
        else if(parent.propertyVisitor.currentProperty.isTopEntity()) currentClass = parent.data.getOWLThing();
        else currentClass = new OWLObjectHasValueImpl((OWLObjectPropertyExpression) parent.propertyVisitor.currentProperty, ce.getFiller());
    }

    @Override
    public void visit(OWLObjectOneOf ce){
        if(ce.getIndividuals().isEmpty()) currentClass = parent.data.getOWLNothing();
        else currentClass = ce;
    }
}
