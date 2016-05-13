package de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes;

import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.folder.NodeFolder;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by spellmaker on 18.03.2016.
 */
public abstract class Node implements Iterable<Node>{
    public final List<Node> children;
    public final OWLObject symbol;

    public Node(OWLObject symbol, List<Node> children){
        this.children = children;
        this.symbol = symbol;
    }

    public abstract void fold(NodeFolder folder);

    @Override
    public Iterator<Node> iterator() {
        return children.iterator();
    }

    @Override
    public void forEach(Consumer<? super Node> action) {
        children.forEach(action);
    }

    public int size(){return children.size();}

    public static List<Node> makeList(Node...nodes){
        List<Node> res = new LinkedList<>();
        for(Node n : nodes){
            res.add(n);
        }
        return res;
    }
}
