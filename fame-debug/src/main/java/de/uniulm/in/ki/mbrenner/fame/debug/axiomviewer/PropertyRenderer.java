package de.uniulm.in.ki.mbrenner.fame.debug.axiomviewer;

import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import javax.swing.*;

/**
 * Created by spellmaker on 12.05.2016.
 */
public class PropertyRenderer implements OWLPropertyExpressionVisitor{
    private AxiomRenderer parent;

    public PropertyRenderer(AxiomRenderer parent){
        this.parent = parent;
    }

    @Override
    public void visit(@Nonnull OWLObjectProperty owlObjectProperty) {
        /*SwitchPanel panel = parent.getEmptyPanel();
        panel.add(parent.iriRenderer.render(owlObjectProperty.getIRI()));
        panel.switchContent();
        panel.add(parent.getAlt(owlObjectProperty));
        panel.switchContent();
        parent.currentPanel = panel;*/
        parent.build(owlObjectProperty);
    }

    @Override
    public void visit(@Nonnull OWLObjectInverseOf owlObjectInverseOf) {

    }

    @Override
    public void visit(@Nonnull OWLDataProperty owlDataProperty) {

    }

    @Override
    public void visit(@Nonnull OWLAnnotationProperty owlAnnotationProperty) {

    }
}
