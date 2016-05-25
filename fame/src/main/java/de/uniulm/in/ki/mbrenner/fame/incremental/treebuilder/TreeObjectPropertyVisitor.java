package de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder;

import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;

/**
 * Object property visitor for the TreeBuilder
 *
 * Created by spellmaker on 18.03.2016.
 */
class TreeObjectPropertyVisitor implements OWLPropertyExpressionVisitor{
    private final TreeBuilder master;

    public TreeObjectPropertyVisitor(TreeBuilder master){
        this.master = master;
    }
    @Override
    public void visit(@Nonnull OWLObjectProperty owlObjectProperty) {
        master.makeLeafNode(owlObjectProperty);
    }

    @Override
    public void visit(@Nonnull OWLObjectInverseOf owlObjectInverseOf) {
        owlObjectInverseOf.getInverse().accept(this);
        master.makeSingleton(owlObjectInverseOf);
    }

    @Override
    public void visit(@Nonnull OWLDataProperty owlDataProperty) {
        //not implemented
    }

    @Override
    public void visit(@Nonnull OWLAnnotationProperty owlAnnotationProperty) {
        //not implemented
    }
}
