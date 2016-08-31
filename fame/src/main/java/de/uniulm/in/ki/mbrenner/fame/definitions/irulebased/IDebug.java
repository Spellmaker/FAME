package de.uniulm.in.ki.mbrenner.fame.definitions.irulebased;

import de.uniulm.in.ki.mbrenner.fame.incremental.OWLDictionary;
import de.uniulm.in.ki.mbrenner.owlprinter.OWLPrinter;

/**
 * Created by spellmaker on 31.05.2016.
 */
public class IDebug {
    public static OWLDictionary debugDictionary;
    public static String get(Integer i){
        if(debugDictionary != null)
            return OWLPrinter.getString(debugDictionary.getObject(i));
        else
            return "" + i;
    }
}
