package de.uniulm.in.ki.mbrenner.fame.util.printer;

import org.semanticweb.owlapi.model.OWLPropertyExpression;

/**
 * Created by spellmaker on 17.05.2016.
 */
@FunctionalInterface
public interface OWLPropertyPrinter {
    public String getString(OWLPropertyExpression property);
}
