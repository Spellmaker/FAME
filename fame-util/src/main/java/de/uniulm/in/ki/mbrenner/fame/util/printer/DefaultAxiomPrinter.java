package de.uniulm.in.ki.mbrenner.fame.util.printer;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

import javax.annotation.Nonnull;

/**
 * Created by spellmaker on 17.05.2016.
 */
public class DefaultAxiomPrinter extends OWLAxiomVisitorAdapter implements OWLAxiomPrinter {
    private String result;
    @Override
    public String getString(OWLAxiom axiom) {
        axiom.accept(this);
        return result;
    }

    @Override
    public void visit(@Nonnull OWLDeclarationAxiom owlDeclarationAxiom) {
        result = "Decl(" + OWLPrinter.getString(owlDeclarationAxiom.getEntity()) + ")";
    }

    @Override
    public void visit(@Nonnull OWLSubClassOfAxiom owlSubClassOfAxiom) {
        result = OWLPrinter.getString(owlSubClassOfAxiom.getSubClass()) +
                OWLChars.subset +
                OWLPrinter.getString(owlSubClassOfAxiom.getSuperClass());
    }

    @Override
    public void visit(@Nonnull OWLSubObjectPropertyOfAxiom owlSubObjectPropertyOfAxiom) {
        result = OWLPrinter.getString(owlSubObjectPropertyOfAxiom.getSubProperty()) +
                OWLChars.subset +
                OWLPrinter.getString(owlSubObjectPropertyOfAxiom.getSuperProperty());
    }

    @Override
    public void visit(@Nonnull OWLClassAssertionAxiom owlClassAssertionAxiom) {
        result = OWLPrinter.getString(owlClassAssertionAxiom.getClassExpression()) + "(" +
                OWLPrinter.getString(owlClassAssertionAxiom.getIndividual()) + ")";
    }

    @Override
    public void visit(@Nonnull OWLEquivalentClassesAxiom owlEquivalentClassesAxiom) {
        result = OWLPrinter.getString(owlEquivalentClassesAxiom.getClassExpressionsAsList().get(0)) +
                OWLChars.equiv +
                OWLPrinter.getString(owlEquivalentClassesAxiom.getClassExpressionsAsList().get(1));
    }

    @Override
    public void visit(@Nonnull OWLTransitiveObjectPropertyAxiom owlTransitiveObjectPropertyAxiom) {
        result = "Trans(" + OWLPrinter.getString(owlTransitiveObjectPropertyAxiom.getProperty()) + ")";
    }
}
