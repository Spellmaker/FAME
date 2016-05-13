package de.uniulm.in.ki.mbrenner.fame.debug.incremental.customextractor;

import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalModule;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 11.04.2016.
 */
public class ModificationResult extends de.uniulm.in.ki.mbrenner.fame.incremental.ModificationResult{
    public Collection<OWLClass> additionAffected;
    public Collection<OWLClass> deletionAffected;

    public ModificationResult(Collection<OWLClass> additionAffected, Collection<OWLClass> deletionAffected){
        super(null, null);
        this.additionAffected = additionAffected;
        this.deletionAffected = deletionAffected;
    }

    @Override
    public Set<OWLClass> getAddAffected(){
        return new HashSet<>(additionAffected);
    }

    @Override
    public Set<OWLClass> getDelAffected(){
        return new HashSet<>(deletionAffected);
    }
}
