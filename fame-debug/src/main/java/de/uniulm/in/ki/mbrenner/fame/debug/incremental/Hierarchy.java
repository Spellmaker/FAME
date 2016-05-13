package de.uniulm.in.ki.mbrenner.fame.debug.incremental;

import org.semanticweb.owlapi.model.OWLClass;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by spellmaker on 26.04.2016.
 */
public class Hierarchy extends HashMap<OWLClass, Set<OWLClass>> {
    public Hierarchy(){
        super();
    }

    public Hierarchy(Hierarchy other){
        super(other);
    }
}
