package de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.nodes;

import de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.folder.NodeFolder;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.List;

/**
 * Created by spellmaker on 18.03.2016.
 */
public class AndNode extends Node{
    public AndNode(OWLObject symbol, List<Node> children){
        super(symbol, children);
    }

    @Override
    public void fold(NodeFolder folder) {
        folder.fold(this);
    }

    @Override
    public String toString(){
        String r = symbol + " AND(";
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
