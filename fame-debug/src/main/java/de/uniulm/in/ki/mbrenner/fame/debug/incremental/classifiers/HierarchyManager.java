package de.uniulm.in.ki.mbrenner.fame.debug.incremental.classifiers;

import de.uniulm.in.ki.mbrenner.fame.debug.incremental.Hierarchy;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Collection;

/**
 * Created by spellmaker on 26.04.2016.
 */

public interface HierarchyManager {
    public void initManager(Hierarchy hierarchy, OWLOntology ontology);
    public Hierarchy determineHierarchy(OWLOntology ontology, Hierarchy previous, Collection<OWLAxiom> added, Collection<OWLAxiom> removed, Collection<OWLClass> newSymbols);
}
