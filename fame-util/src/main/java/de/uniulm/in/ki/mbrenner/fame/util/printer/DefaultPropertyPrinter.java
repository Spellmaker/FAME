package de.uniulm.in.ki.mbrenner.fame.util.printer;

import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;

/**
 * Created by spellmaker on 17.05.2016.
 */
public class DefaultPropertyPrinter implements OWLPropertyPrinter, OWLPropertyExpressionVisitor{
    private String result;


    @Override
    public void visit(@Nonnull OWLObjectProperty owlObjectProperty) {
        result = OWLPrinter.getString(owlObjectProperty.getIRI());
    }

    @Override
    public void visit(@Nonnull OWLObjectInverseOf owlObjectInverseOf) {

    }

    @Override
    public void visit(@Nonnull OWLDataProperty owlDataProperty) {

    }

    @Override
    public void visit(@Nonnull OWLAnnotationProperty owlAnnotationProperty) {

    }

    @Override
    public String getString(OWLPropertyExpression property) {
        property.accept(this);
        return result;
    }
}
