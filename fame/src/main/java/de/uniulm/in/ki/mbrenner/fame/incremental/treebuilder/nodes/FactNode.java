package de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes;

import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.folder.NodeFolder;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.Collections;

/**
 * Fact node which always triggers
 *
 * Created by spellmaker on 18.03.2016.
 */
public class FactNode extends Node {
    /**
     * Default constructor
     * @param symbol The symbol of this node
     */
    public FactNode(OWLObject symbol) {
        super(symbol, Collections.emptyList());
    }

    @Override
    public void fold(NodeFolder folder) {
        folder.fold(this);
    }

    @Override
    public String toString(){
        return "(Fact " + symbol + ")";
    }
}
