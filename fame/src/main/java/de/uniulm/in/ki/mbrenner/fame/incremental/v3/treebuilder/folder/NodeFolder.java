package de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.folder;

import de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.nodes.*;

/**
 * Created by spellmaker on 18.03.2016.
 */
public abstract class NodeFolder {
    public void fold(Node node){

    }
    public abstract void fold(LeafNode node);
    public abstract void fold(AndNode node);
    public abstract void fold(OrNode node);
    public abstract void fold(FactNode node);
    public abstract void fold(AxiomOrNode node);
}
