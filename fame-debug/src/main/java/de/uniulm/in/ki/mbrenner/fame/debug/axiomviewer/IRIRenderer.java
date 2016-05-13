package de.uniulm.in.ki.mbrenner.fame.debug.axiomviewer;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObject;

import javax.swing.*;

/**
 * Created by spellmaker on 12.05.2016.
 */
public class IRIRenderer {
    private AxiomRenderer parent;



    public IRIRenderer(AxiomRenderer parent){
        this.parent = parent;
    }

    public JLabel render(IRI iri){
        //adopt the foll
        String text = iri.toString();
        text = text.substring(text.lastIndexOf('#') + 1);
        JLabel label = parent.getLabel(text);
        return label;
    }
}
