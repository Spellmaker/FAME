package de.uniulm.in.ki.mbrenner.fame.incremental.v3;

import java.util.Collection;

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
}
