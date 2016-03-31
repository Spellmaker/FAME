package de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder;

import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;

/**
 * Created by spellmaker on 18.03.2016.
 */
public class TreeObjectPropertyVisitor implements OWLPropertyExpressionVisitor{
    private TreeBuilder master;

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
