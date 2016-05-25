package de.uniulm.in.ki.mbrenner.fame.incremental;

import org.semanticweb.owlapi.model.OWLObject;

/**
 * Provides a mapping of OWLObjects to integer values and back
 *
 * Created by spellmaker on 18.03.2016.
 */
public interface OWLDictionary {
    /**
     * Converts the provided object into a unique index
     * @param o An object
     * @return An index for the object or possibly null if there is no index
     */
    Integer getId(OWLObject o);

    /**
     * Provides the object associated with the unique index
     * @param id The id of the object
     * @return The object for the provided id
     */
    OWLObject getObject(Integer id);

    /**
     * Provides the number of elements in the dictionary
     * @return The number of elements in the dictionary
     */
    int dictionarySize();
}
