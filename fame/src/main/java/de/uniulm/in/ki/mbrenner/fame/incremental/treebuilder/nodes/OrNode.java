package de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes;

import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.folder.NodeFolder;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.List;

/**
 * Represents a node which triggers if one of the children is triggering
 *
 * Created by spellmaker on 18.03.2016.
 */
public class OrNode extends Node{
    /**
     * Constructs a new instance
     * @param symbol The symbol of this node
     * @param children The children of this node
     */
    public OrNode(OWLObject symbol, List<Node> children){
        super(symbol, children);
    }

    @Override
    public void fold(NodeFolder folder) {
        folder.fold(this);
    }

    @Override
    public String toString(){
        String r = symbol + " OR(";
        if(children != null){
            for(Node n : children){
                r += n + ", ";
            }
            if(children.size() > 0){
                r = r.substring(0, r.length()  - 2);
            }
        }
        return r + ")";
    }
}
