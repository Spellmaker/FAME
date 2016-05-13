package de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder;

import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes.*;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.*;

/**
 * Created by spellmaker on 18.03.2016.
 */
public class TreeBuilder {
    boolean botMode;
    Node currentNode; //null means tautology, empty node means fact
    TreeObjectPropertyVisitor treeObjectPropertyVisitor;
    TreeClassExpressionVisitor treeClassExpressionVisitor;
    TreeAxiomVisitor treeAxiomVisitor;

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

    public List<Node> buildTree(Collection<OWLAxiom> ax){
        List<Node> nodes = new LinkedList<>();
        for(OWLAxiom a : ax){
            Node n = buildTree(a);
            if(n != null) nodes.add(n);
        }

        return nodes;
    }

    public Node buildTree(OWLAxiom ax){
        ax.accept(treeAxiomVisitor);
        return currentNode;
    }
}
