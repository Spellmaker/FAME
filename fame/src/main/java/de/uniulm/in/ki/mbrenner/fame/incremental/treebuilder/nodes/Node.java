package de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes;

import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.folder.NodeFolder;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base node class
 *
 * Nodes are used to abstract away from the structure of the axioms of an ontology
 * to a node structure, which provides information about the interplay of different
 * symbols in terms of the locality or non-locality of an axiom
 *
 * Created by spellmaker on 18.03.2016.
 */
public abstract class Node implements Iterable<Node>{
    /**
     * The list of children of this node
     */
    public final List<Node> children;
    /**
     * The symbol assigned to this node
     */
    public final OWLObject symbol;

    Node(OWLObject symbol, List<Node> children){
        this.children = children;
        this.symbol = symbol;
    }

    /**
     * Accepts a NodeFolder to process the tree
     * @param folder A NodeFolder
     */
    public abstract void fold(NodeFolder folder);

    @Override
    public Iterator<Node> iterator() {
        return children.iterator();
    }

    @Override
    public void forEach(Consumer<? super Node> action) {
        children.forEach(action);
    }

    /**
     * Provides the number of children of this node
     * @return The number of child nodes
     */
    public int size(){return children.size();}

    /**
     * Helper method creating a list of nodes from a set of nodes
     * @param nodes Nodes which are to be part of the list
     * @return A list containing all provided nodes
     */
    public static List<Node> makeList(Node...nodes){
        List<Node> res = new LinkedList<>();
        Collections.addAll(res, nodes);
        return res;
    }
}
