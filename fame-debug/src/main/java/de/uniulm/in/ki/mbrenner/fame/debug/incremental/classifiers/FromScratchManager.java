package de.uniulm.in.ki.mbrenner.fame.debug.incremental.classifiers;

import de.uniulm.in.ki.mbrenner.fame.debug.incremental.Hierarchy;
import de.uniulm.in.ki.mbrenner.fame.debug.incremental.HierarchyTools;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by spellmaker on 27.04.2016.
 */
public class FromScratchManager implements HierarchyManager {
    private OWLReasonerFactory factory;
    private Set<OWLClass> classes;
    private OWLClass top;
    private OWLClass bottom;

    public FromScratchManager(OWLReasonerFactory factory){
        OWLDataFactory fact = new OWLDataFactoryImpl();
        top = fact.getOWLThing();
        bottom = fact.getOWLNothing();
        this.factory = factory;
    }

    public FromScratchManager(OWLReasonerFactory factory, Set<OWLClass> classes){
        this.factory = factory;
        this.classes = classes;
    }

    @Override
    public void initManager(Hierarchy hierarchy, OWLOntology ontology) {
        //nothing to do, we do not reuse previous knowledge
    }

    @Override
    public Hierarchy determineHierarchy(OWLOntology ontology, Hierarchy previous, Collection<OWLAxiom> added, Collection<OWLAxiom> removed, Collection<OWLClass> newSymbols) {
        if(classes == null)
            return HierarchyTools.initialHierarchy(ontology, factory.createNonBufferingReasoner(ontology));
        else
            return HierarchyTools.initialHierarchy(factory.createNonBufferingReasoner(ontology), classes);
    }
}
