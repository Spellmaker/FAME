package de.uniulm.in.ki.mbrenner.fame.incremental;

import org.semanticweb.owlapi.model.OWLClass;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Collects the result of an ontology modification in terms of affected modules
 *
 * Created by spellmaker on 11.04.2016.
 */
public class ModificationResult {
    /**
     * Modules affected by added axioms
     */
    public final Collection<IncrementalModule> additionAffected;
    /**
     * Modules affected by deleted axioms
     */
    public final Collection<IncrementalModule> deletionAffected;

    /**
     * Default constructor
     * @param additionAffected Modules affected by added axioms
     * @param deletionAffected Modules affected by deleted axioms
     */
    public ModificationResult(Collection<IncrementalModule> additionAffected, Collection<IncrementalModule> deletionAffected){
        this.additionAffected = additionAffected;
        this.deletionAffected = deletionAffected;
    }

    /**
     * Converts the modules to the base entity which creates the module
     * Note that this of cause only works if there is a single base entity
     * @return The base entities for modules which are affected by added axioms
     */
    public Set<OWLClass> getAddAffected(){
        return additionAffected.stream().map(x -> (OWLClass) x.extractor.getObject(x.getBaseEntity())).collect(Collectors.toSet());
    }

    /**
     * Converts the modules to the base entity which creates the module
     * Note that this of cause only works if there is a single base entity
     * @return The base entities for modules which are affected by deleted axioms
     */
    public Set<OWLClass> getDelAffected(){
        return deletionAffected.stream().map(x -> (OWLClass) x.extractor.getObject(x.getBaseEntity())).collect(Collectors.toSet());
    }
}
