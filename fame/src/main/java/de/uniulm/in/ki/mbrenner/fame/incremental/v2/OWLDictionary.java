package de.uniulm.in.ki.mbrenner.fame.incremental.v2;

import org.semanticweb.owlapi.model.OWLObject;

/**
 * Created by spellmaker on 18.03.2016.
 */
public interface OWLDictionary {
    Integer getId(OWLObject o);
    OWLObject getObject(Integer id);
    int dictionarySize();
}
