package de.uniulm.in.ki.mbrenner.fame.util.printer;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Created by spellmaker on 17.05.2016.
 */
@FunctionalInterface
public interface OWLAxiomPrinter {
    public String getString(OWLAxiom axiom);
}
