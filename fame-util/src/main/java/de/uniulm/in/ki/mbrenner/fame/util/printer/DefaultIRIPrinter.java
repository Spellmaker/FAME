package de.uniulm.in.ki.mbrenner.fame.util.printer;

import org.semanticweb.owlapi.model.IRI;

/**
 * Created by spellmaker on 20.05.2016.
 */
public class DefaultIRIPrinter implements IRIPrinter {
    @Override
    public String getString(IRI iri) {
        String s = iri.toString();
        s = s.substring(s.lastIndexOf("/") + 1);
        s = s.substring(s.lastIndexOf("#") + 1);
        return s;
    }
}
