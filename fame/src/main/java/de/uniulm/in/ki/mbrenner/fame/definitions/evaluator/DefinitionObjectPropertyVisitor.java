package de.uniulm.in.ki.mbrenner.fame.definitions.evaluator;

import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;

/**
 * Property Visitor for the DefinitionEvaluator
 *
 * Created by spellmaker on 27.04.2016.
 */
class DefinitionObjectPropertyVisitor implements OWLPropertyExpressionVisitor{
    OWLPropertyExpression currentProperty;

    private final DefinitionEvaluator parent;

    public DefinitionObjectPropertyVisitor(DefinitionEvaluator parent) {
        this.parent = parent;
    }

    @Override
    public void visit(@Nonnull OWLObjectProperty owlObjectProperty) {
        OWLObjectPropertyExpression def = (OWLObjectPropertyExpression) parent.definitions.get(owlObjectProperty);
        if(def == null){
            if(!parent.isFinalSymbol(owlObjectProperty))
                currentProperty = parent.data.getOWLBottomObjectProperty();
            else
                currentProperty = owlObjectProperty;
        }
        else{
            def.accept(this);
            //currentProperty = def;
            //parent.usedDefinitions.add(owlObjectProperty);
        }
    }

    @Override
    public void visit(@Nonnull OWLObjectInverseOf owlObjectInverseOf) {
        //does not exist in EL
    }

    @Override
    public void visit(@Nonnull OWLDataProperty owlDataProperty) {
        //does not exist in EL
    }

    @Override
    public void visit(@Nonnull OWLAnnotationProperty owlAnnotationProperty) {
        //does not exist in EL
    }
}
