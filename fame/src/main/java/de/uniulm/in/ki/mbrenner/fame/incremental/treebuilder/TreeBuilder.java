package de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder;

import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes.*;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.*;

/**
 * Transforms an ontology into a tree structure representing the interplay of symbols in the locality and non-locality of the axioms
 *
 * Created by spellmaker on 18.03.2016.
 */
public class TreeBuilder {
    boolean botMode;
    Node currentNode; //null means tautology, empty node means fact
    final TreeObjectPropertyVisitor treeObjectPropertyVisitor;
    final TreeClassExpressionVisitor treeClassExpressionVisitor;
    private final TreeAxiomVisitor treeAxiomVisitor;

    /**
     * Constructs a new instance
     */
    public TreeBuilder(){
        botMode = true;
        treeObjectPropertyVisitor = new TreeObjectPropertyVisitor(this);
        treeClassExpressionVisitor = new TreeClassExpressionVisitor(this);
        treeAxiomVisitor = new TreeAxiomVisitor(this);
    }

    void makeSingleton(OWLObject object){
        if(currentNode != null){
            currentNode = new AndNode(object, Collections.singletonList(currentNode));
        }
    }

    void makeAndNode(OWLObject object, List<Node> children){
        currentNode = new AndNode(object, children);
    }

    void makeOrNode(OWLObject object, List<Node> children){
        currentNode = new OrNode(object, children);
    }

    void makeFactNode(OWLObject object){
        currentNode = new FactNode(object);
    }

    void makeLeafNode(OWLObject object){
        currentNode = new LeafNode(object);
    }

    /**
     * Compiles the provided axioms into trees, which reflect the dependency of symbols for the locality of axioms
     * @param ax A collection of axioms
     * @return A list of nodes forming the root of trees
     */
    public List<Node> buildTree(Collection<OWLAxiom> ax){
        List<Node> nodes = new LinkedList<>();
        for(OWLAxiom a : ax){
            Node n = buildTree(a);
            if(n != null) nodes.add(n);
        }

        return nodes;
    }

    private Node buildTree(OWLAxiom ax){
        ax.accept(treeAxiomVisitor);
        return currentNode;
    }
}
