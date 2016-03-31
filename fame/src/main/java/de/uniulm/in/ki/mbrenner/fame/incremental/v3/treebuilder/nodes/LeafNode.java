package de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.nodes;

import de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.folder.NodeFolder;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * Created by spellmaker on 18.03.2016.
 */
public class LeafNode extends Node {
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
