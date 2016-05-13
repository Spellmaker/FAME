package de.uniulm.in.ki.mbrenner.fame.debug.incremental.classifiers;

import de.uniulm.in.ki.mbrenner.fame.debug.incremental.Hierarchy;
import de.uniulm.in.ki.mbrenner.fame.debug.incremental.customextractor.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.debug.incremental.customextractor.ModificationResult;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

import java.util.*;

/**
 * Created by spellmaker on 27.04.2016.
 */
public class ModuleNoStorageManager implements HierarchyManager {
    private IncrementalExtractor extractor;
    private OWLReasonerFactory factory;
    private OWLClass top;
    private OWLClass bottom;

    public ModuleNoStorageManager(OWLReasonerFactory factory) {
        this.factory = factory;
    }

    @Override
    public void initManager(Hierarchy hierarchy, OWLOntology ontology) {
        extractor = new IncrementalExtractor(ontology);
        OWLDataFactory dFact = new OWLDataFactoryImpl();
        top = dFact.getOWLThing();
        bottom = dFact.getOWLNothing();
    }

    @Override
    public Hierarchy determineHierarchy(OWLOntology ontology, Hierarchy previous, Collection<OWLAxiom> added, Collection<OWLAxiom> removed, Collection<OWLClass> newSymbols) {
        Hierarchy result = new Hierarchy();
        OWLReasoner reasoner = factory.createNonBufferingReasoner(ontology);

        //process new symbols
        Hierarchy pCopy = new Hierarchy(previous);
        for(OWLClass c : newSymbols){
            Set<OWLClass> sups = new HashSet<>();
            sups.addAll(previous.get(top));
            pCopy.put(c, sups);
            //subclasses of bot
            for(Map.Entry<OWLClass, Set<OWLClass>> entry : previous.entrySet()){
                if(entry.getValue().contains(bottom)){
                    entry.getValue().add(c);
                }
            }
        }

        ModificationResult modRes = extractor.modifyOntology(added, removed, ontology.getClassesInSignature());
        for(OWLClass c : ontology.getClassesInSignature()){
            result.put(c, getSupClasses(pCopy.get(c), c, modRes.getAddAffected().contains(c), modRes.getDelAffected().contains(c), reasoner, ontology));
        }

        return result;
    }

    private Set<OWLClass> getSupClasses(Set<OWLClass> previous, OWLClass c, boolean addAffected, boolean delAffected, OWLReasoner reasoner, OWLOntology ontology){
        if(!addAffected && !delAffected){
            return previous;
        }
        Set<OWLClass> supClasses = new HashSet<>();
        if(!addAffected){
            for(OWLClass b : previous){
                OWLAxiom ax = new OWLSubClassOfAxiomImpl(c, b, Collections.emptySet());
                if(reasoner.isEntailed(ax)) supClasses.add(b);
            }
            return supClasses;
        }

        Set<OWLClass> all = new HashSet<>(ontology.getClassesInSignature());
        all.add(bottom);
        all.add(top);
        for(OWLClass b : all){
            if(b.equals(c)) continue;
            if(previous.contains(b) && !delAffected){
                supClasses.add(b);
                continue;
            }
            OWLAxiom ax = new OWLSubClassOfAxiomImpl(c, b, Collections.emptySet());
            if(reasoner.isEntailed(ax)){
                supClasses.add(b);
            }
        }
        return supClasses;
    }
}
