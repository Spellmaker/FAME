package de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes;

import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.folder.NodeFolder;

import java.util.List;

/**
 * Created by spellmaker on 29.03.2016.
 */
public class AxiomOrNode extends Node {
    public AxiomOrNode(List<Node> children) {
        super(null, children);
    }

    @Override
    public void fold(NodeFolder folder) {
        folder.fold(this);
    }
}
