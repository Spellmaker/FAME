package de.uniulm.in.ki.mbrenner.fame.definitions.evaluator;

import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;

/**
 * Created by spellmaker on 27.04.2016.
 */
public class DefinitionObjectPropertyVisitor extends DefinitionVisitor implements OWLPropertyExpressionVisitor{
    OWLPropertyExpression currentProperty;

    public DefinitionObjectPropertyVisitor(DefinitionEvaluator parent) {
        super(parent);
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
