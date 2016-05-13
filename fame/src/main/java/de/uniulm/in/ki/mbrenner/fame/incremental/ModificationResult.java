package de.uniulm.in.ki.mbrenner.fame.incremental;

import org.semanticweb.owlapi.model.OWLClass;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 11.04.2016.
 */
public class ModificationResult {
    public Collection<IncrementalModule> additionAffected;
    public Collection<IncrementalModule> deletionAffected;

    public ModificationResult(Collection<IncrementalModule> additionAffected, Collection<IncrementalModule> deletionAffected){
        this.additionAffected = additionAffected;
        this.deletionAffected = deletionAffected;
    }

    public Set<OWLClass> getAddAffected(){
        return additionAffected.stream().map(x -> (OWLClass) x.extractor.getObject(x.getBaseEntity())).collect(Collectors.toSet());
    }
    public Set<OWLClass> getDelAffected(){
        return deletionAffected.stream().map(x -> (OWLClass) x.extractor.getObject(x.getBaseEntity())).collect(Collectors.toSet());
    }
}
