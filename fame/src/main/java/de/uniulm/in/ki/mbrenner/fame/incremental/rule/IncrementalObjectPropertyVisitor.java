package de.uniulm.in.ki.mbrenner.fame.incremental.rule;

import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;

/**
 * Created by spellmaker on 16.03.2016.
 */
public class IncrementalObjectPropertyVisitor implements OWLPropertyExpressionVisitor{
    private IncrementalRuleBuilder master;

    public IncrementalObjectPropertyVisitor(IncrementalRuleBuilder master){
        this.master = master;
    }

    @Override
    public void visit(@Nonnull OWLObjectProperty owlObjectProperty) {
        //Nothing to do here
    }

    @Override
    public void visit(@Nonnull OWLObjectInverseOf owlObjectInverseOf) {
        master.clearBuffer();
        owlObjectInverseOf.getInverse().accept(this);
        master.addRule(owlObjectInverseOf, null, owlObjectInverseOf.getInverse());
    }

    @Override
    public void visit(@Nonnull OWLDataProperty owlDataProperty) {
        master.unknownObjects.add(owlDataProperty);
    }

    @Override
    public void visit(@Nonnull OWLAnnotationProperty owlAnnotationProperty) {
        master.unknownObjects.add(owlAnnotationProperty);
    }
}
