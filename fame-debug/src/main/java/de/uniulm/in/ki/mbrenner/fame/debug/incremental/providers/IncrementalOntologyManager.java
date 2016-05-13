package de.uniulm.in.ki.mbrenner.fame.debug.incremental.providers;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.Collection;
import java.util.Set;

/**
 * Created by spellmaker on 26.04.2016.
 */
public interface IncrementalOntologyManager {
    public OWLOntology getCurrent();
    public OWLOntology next();
    public Collection<OWLAxiom> getRemoved();
    public Collection<OWLAxiom> getAdded();
    public boolean hasMore();
    public OWLReasoner getReasoner();
    public Set<OWLClass> previousClassesInSignature();
    public Set<OWLClass> newClasses();
}
