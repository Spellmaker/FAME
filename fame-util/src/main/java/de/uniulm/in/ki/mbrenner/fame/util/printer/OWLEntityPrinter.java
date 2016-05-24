package de.uniulm.in.ki.mbrenner.fame.util.printer;

import org.semanticweb.owlapi.model.OWLEntity;

/**
 * Created by spellmaker on 17.05.2016.
 */
@FunctionalInterface
public interface OWLEntityPrinter {
    public String getString(OWLEntity entity);
}
