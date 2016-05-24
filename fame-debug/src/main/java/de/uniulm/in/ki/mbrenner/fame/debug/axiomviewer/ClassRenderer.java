package de.uniulm.in.ki.mbrenner.fame.debug.axiomviewer;

import de.uniulm.in.ki.mbrenner.fame.util.printer.OWLChars;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

import javax.swing.*;
import java.util.Iterator;

/**
 * Created by spellmaker on 12.05.2016.
 */
public class ClassRenderer extends OWLClassExpressionVisitorAdapter{
    private AxiomRenderer parent;

    public ClassRenderer(AxiomRenderer parent){
        this.parent = parent;
    }

    @Override
    public void visit(OWLObjectIntersectionOf concept){
        JPanel panel = parent.getEmptyPanel();

        int cnt = concept.getOperands().size();
        Iterator<OWLClassExpression> iter = concept.getOperands().iterator();
        iter.next().accept(this);
        panel.add(parent.currentPanel);
        while(iter.hasNext()){
            iter.next().accept(this);
            panel.add(parent.getLabel("" + OWLChars.and));
            panel.add(parent.currentPanel);
        }
        parent.currentPanel = panel;
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom concept){
        JPanel panel = parent.getEmptyPanel();
        panel.add(parent.getLabel("" + OWLChars.exists));
        concept.getProperty().accept(parent.propertyRenderer);
        panel.add(parent.currentPanel);
        panel.add(parent.getLabel("."));
        concept.getFiller().accept(this);
        panel.add(parent.currentPanel);
        parent.currentPanel = panel;
    }

    @Override
    public void visit(OWLClass concept){
        parent.build(concept);
        /*if(parent.isInSignature(concept)){
asdasd
        }
        else {
            SwitchPanel panel = parent.getEmptyPanel();
            panel.add(parent.iriRenderer.render(concept.getIRI()));
            panel.switchContent();
            panel.add(parent.getAlt(concept));
            panel.switchContent();
            parent.currentPanel = panel;
        }*/
    }
}
