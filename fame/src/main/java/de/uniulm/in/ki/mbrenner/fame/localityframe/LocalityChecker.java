package de.uniulm.in.ki.mbrenner.fame.localityframe;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.Set;

/**
 * Created by spellmaker on 13.06.2016.
 */
public interface LocalityChecker {
    public boolean isLocal(OWLAxiom axiom, Set<OWLEntity> signature);
}
