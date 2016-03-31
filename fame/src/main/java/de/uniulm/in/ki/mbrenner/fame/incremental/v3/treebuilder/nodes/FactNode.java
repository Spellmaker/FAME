package de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.nodes;

import de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.folder.NodeFolder;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.Collections;

/**
 * Created by spellmaker on 18.03.2016.
 */
public class FactNode extends Node {
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
