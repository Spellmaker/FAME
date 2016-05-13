package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule;

import org.semanticweb.owlapi.model.OWLObject;

/**
 * Created by Spellmaker on 13.05.2016.
 */
public class OWLRendererProvider {
    public static OWLRenderer renderer = Object::toString;

    public static String render(OWLObject object){
        return renderer.action(object);
    }
}
