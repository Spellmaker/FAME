package de.uniulm.in.ki.mbrenner.fame.definitions.builder;

import de.uniulm.in.ki.mbrenner.fame.definitions.evaluator.DefinitionEvaluator;
import de.uniulm.in.ki.mbrenner.fame.definitions.CombinedObjectProperty;
import de.uniulm.in.ki.mbrenner.fame.definitions.IndicatorClass;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;

/**
 * Created by spellmaker on 28.04.2016.
 */
public class DBClassVisitor implements OWLClassExpressionVisitor {
    OWLClassExpression currentClass;
    DefinitionBuilder parent;

    public DBClassVisitor(DefinitionBuilder parent){
        this.parent = parent;
    }


    @Override
    public void visit(@Nonnull OWLClass owlClass) {
        //only possible if
        //1. class is not part of the signature
        //2. there is no definition for class
        //3. class is bottom or top and therefore not definable
        if(parent.signature.contains(owlClass)){
            if(parent.currentTarget.equals(owlClass)){
                return;
            }
            parent.error = true;
            return;
        }
        if((owlClass.isTopEntity() && !parent.currentTarget.isTopEntity()) ||
                (owlClass.isBottomEntity() && !parent.currentTarget.isBottomEntity())){
            parent.error = true;
            return;
        }


        OWLObject def = parent.definitions.get(owlClass);
        if(def != null && !def.equals(owlClass)){
            parent.error = true;
            return;
        }
        currentClass = (OWLClassExpression) parent.currentTarget;
        parent.definitions.put(owlClass, parent.currentTarget);
        parent.dependent.add(owlClass);
    }

    @Override
    public void visit(@Nonnull OWLObjectIntersectionOf owlObjectIntersectionOf) {
        //TODO: Again, when the target expression is an object intersection aswell it might be an idea to try and bind single parts to it
        OWLObject def = parent.definitions.get(owlObjectIntersectionOf);
        if(def != null){
            if(def.equals(parent.currentTarget)){
                return;
            }
            else{
                parent.error = true;
                return;
            }
        }
        //if there is no definition, try to resolve the problem by attempting to declare
        //each operand as the current target
        for(OWLClassExpression oce : owlObjectIntersectionOf.getOperands()){
            oce.accept(this);
        }
        parent.definitions.put(owlObjectIntersectionOf, parent.currentTarget);
    }

    @Override
    public void visit(@Nonnull OWLObjectSomeValuesFrom owlObjectSomeValuesFrom) {
        //check if there is already a definition for this expression
        OWLObject def = parent.definitions.get(owlObjectSomeValuesFrom);
        if(def != null){
            //if the definition agrees with the current target, there is nothing to do
            if(def.equals(parent.currentTarget)) {
                return;
            }
            //otherwise we have two definitions which do not agree and there is nothing left to do
            //TODO: There can be a case where one of the definitions is simply not cleaned up - what to do?
            else{
                parent.error = true;
                return;
            }
        }

        //if there is no definition try to define the property as top and the class expression
        //as the desired target
        //TODO: what if the desired target is also an existential restriction? there is another possibility
        OWLObject ctarget = parent.currentTarget;

        //filler needs to be interpreted as the indicator class
        IndicatorClass ind = new IndicatorClass(owlObjectSomeValuesFrom.getFiller());
        parent.currentTarget = ind;
        owlObjectSomeValuesFrom.getFiller().accept(this);
        parent.definitions.put(owlObjectSomeValuesFrom, ctarget);
        parent.currentTarget = new CombinedObjectProperty((OWLClassExpression) ctarget, ind);
        owlObjectSomeValuesFrom.getProperty().accept(parent.propertyVisitor);
        parent.currentTarget = ctarget;
    }

    //below is not allowed


    @Override
    public void visit(@Nonnull OWLObjectUnionOf owlObjectUnionOf) {

    }

    @Override
    public void visit(@Nonnull OWLObjectComplementOf owlObjectComplementOf) {

    }

    @Override
    public void visit(@Nonnull OWLObjectAllValuesFrom owlObjectAllValuesFrom) {

    }

    @Override
    public void visit(@Nonnull OWLObjectHasValue owlObjectHasValue) {

    }

    @Override
    public void visit(@Nonnull OWLObjectMinCardinality owlObjectMinCardinality) {

    }

    @Override
    public void visit(@Nonnull OWLObjectExactCardinality owlObjectExactCardinality) {

    }

    @Override
    public void visit(@Nonnull OWLObjectMaxCardinality owlObjectMaxCardinality) {

    }

    @Override
    public void visit(@Nonnull OWLObjectHasSelf owlObjectHasSelf) {

    }

    @Override
    public void visit(@Nonnull OWLObjectOneOf owlObjectOneOf) {

    }

    @Override
    public void visit(@Nonnull OWLDataSomeValuesFrom owlDataSomeValuesFrom) {

    }

    @Override
    public void visit(@Nonnull OWLDataAllValuesFrom owlDataAllValuesFrom) {

    }

    @Override
    public void visit(@Nonnull OWLDataHasValue owlDataHasValue) {

    }

    @Override
    public void visit(@Nonnull OWLDataMinCardinality owlDataMinCardinality) {

    }

    @Override
    public void visit(@Nonnull OWLDataExactCardinality owlDataExactCardinality) {

    }

    @Override
    public void visit(@Nonnull OWLDataMaxCardinality owlDataMaxCardinality) {

    }
}
