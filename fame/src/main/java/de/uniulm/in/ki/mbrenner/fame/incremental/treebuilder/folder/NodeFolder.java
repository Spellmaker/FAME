package de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.folder;

import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes.*;

/**
 * Processes a tree structure and produces some sort of output
 *
 * Created by spellmaker on 18.03.2016.
 */
public abstract class NodeFolder {
    //public abstract void fold(Node node);

    /**
     * Visits a LeafNode
     * @param node A LeafNode
     */
    public abstract void fold(LeafNode node);

    /**
     * Visits an AndNode
     * @param node An AndNode
     */
    public abstract void fold(AndNode node);

    /**
     * Visits an OrNode
     * @param node An OrNode
     */
    public abstract void fold(OrNode node);

    /**
     * Visits a FactNode
     * @param node A FactNode
     */
    public abstract void fold(FactNode node);

    /**
     * Visits an AxiomOrNode
     * @param node An AxiomOrNode
     */
    public abstract void fold(AxiomOrNode node);
}
