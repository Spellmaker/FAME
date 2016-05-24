package de.uniulm.in.ki.mbrenner.fame.util.printer;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * Created by spellmaker on 17.05.2016.
 */
@FunctionalInterface
public interface OWLClassPrinter {
    public String getString(OWLClassExpression oce);
}
