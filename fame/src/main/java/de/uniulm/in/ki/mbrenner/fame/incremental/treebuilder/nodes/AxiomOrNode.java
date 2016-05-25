package de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes;

import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.folder.NodeFolder;

import java.util.List;

/**
 * Node triggering and adding an axiom if one of the children triggers
 *
 * Created by spellmaker on 29.03.2016.
 */
public class AxiomOrNode extends Node {
    /**
     * Default constructor
     * @param children The children of this node
     */
    public AxiomOrNode(List<Node> children) {
        super(null, children);
    }

    @Override
    public void fold(NodeFolder folder) {
        folder.fold(this);
    }
}
