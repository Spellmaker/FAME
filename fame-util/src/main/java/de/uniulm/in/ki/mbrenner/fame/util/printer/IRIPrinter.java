package de.uniulm.in.ki.mbrenner.fame.util.printer;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * Created by spellmaker on 17.05.2016.
 */
@FunctionalInterface
public interface IRIPrinter {
    public String getString(IRI iri);
}
