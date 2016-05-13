package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule;

import org.semanticweb.owlapi.model.OWLObject;

/**
 * Created by Spellmaker on 13.05.2016.
 */
@FunctionalInterface
public interface OWLRenderer {
    public String action(OWLObject object);
}
