package de.uniulm.in.ki.mbrenner.fame.util.printer;

import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;

/**
 * Created by spellmaker on 17.05.2016.
 */
public class DefaultEntityPrinter implements OWLEntityVisitor, OWLEntityPrinter{
    private String result;
    @Override
    public void visit(@Nonnull OWLClass owlClass) {
        result = OWLPrinter.getString(owlClass.getIRI());
    }

    @Override
    public void visit(@Nonnull OWLObjectProperty owlObjectProperty) {
        result = OWLPrinter.getString(owlObjectProperty.getIRI());
    }

    @Override
    public void visit(@Nonnull OWLDataProperty owlDataProperty) {
        result = OWLPrinter.getString(owlDataProperty.getIRI());
    }

    @Override
    public void visit(@Nonnull OWLNamedIndividual owlNamedIndividual) {
        result = OWLPrinter.getString(owlNamedIndividual.getIRI());
    }

    @Override
    public void visit(@Nonnull OWLDatatype owlDatatype) {
        result = OWLPrinter.getString(owlDatatype.getIRI());
    }

    @Override
    public void visit(@Nonnull OWLAnnotationProperty owlAnnotationProperty) {
        result = OWLPrinter.getString(owlAnnotationProperty.getIRI());
    }

    @Override
    public String getString(OWLEntity entity) {
        entity.accept(this);
        return result;
    }
}
