package de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes;

import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.folder.NodeFolder;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * Leaf node which fires if the symbol is present
 *
 * Created by spellmaker on 18.03.2016.
 */
public class LeafNode extends Node {
    /**
     * Default constructor
     * @param symbol The symbol of this node
     */
    public LeafNode(OWLObject symbol){
        super(symbol, null);
    }

    @Override
    public void fold(NodeFolder folder) {
        folder.fold(this);
    }

    @Override
    public String toString(){
        return "(" + symbol + ")";
    }
}
