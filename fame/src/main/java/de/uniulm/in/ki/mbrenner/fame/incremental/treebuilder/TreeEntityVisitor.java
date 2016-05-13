package de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder;

import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes.LeafNode;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;

/**
 * Created by spellmaker on 18.03.2016.
 */
public class TreeEntityVisitor implements OWLEntityVisitor{
    private TreeBuilder master;

    public TreeEntityVisitor(TreeBuilder master){
        this.master = master;
    }

    @Override
    public void visit(@Nonnull OWLClass owlClass) {
        master.currentNode = new LeafNode(owlClass);
    }

    @Override
    public void visit(@Nonnull OWLNamedIndividual owlNamedIndividual) {
        master.currentNode = new LeafNode(owlNamedIndividual);
    }

    @Override
    public void visit(@Nonnull OWLObjectProperty owlObjectProperty) {
        master.currentNode = new LeafNode(owlObjectProperty);
    }

    @Override
    public void visit(@Nonnull OWLDataProperty owlDataProperty) {

    }

    @Override
    public void visit(@Nonnull OWLDatatype owlDatatype) {

    }

    @Override
    public void visit(@Nonnull OWLAnnotationProperty owlAnnotationProperty) {

    }
}
