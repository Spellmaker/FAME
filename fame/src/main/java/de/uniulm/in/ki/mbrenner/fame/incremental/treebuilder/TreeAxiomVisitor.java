package de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder;

import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes.AndNode;
import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes.AxiomOrNode;
import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes.LeafNode;
import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes.Node;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Axiom Visitor for the TreeBuilder *
 *
 * Created by spellmaker on 18.03.2016.
 */
class TreeAxiomVisitor extends OWLAxiomVisitorAdapter {
    private final TreeBuilder master;
    private final TreeEntityVisitor entityVisitor;

    /**
     * Creates a new instance
     * @param master The owner of this Visitor
     */
    public TreeAxiomVisitor(TreeBuilder master){
        this.master = master;
        entityVisitor = new TreeEntityVisitor(master);
    }

    @Override
    public void visit(OWLDeclarationAxiom axiom){
        axiom.getEntity().accept(entityVisitor);
        master.makeSingleton(axiom);
    }

    @Override
    public void visit(OWLSubClassOfAxiom axiom){
        axiom.getSubClass().accept(master.treeClassExpressionVisitor);
        boolean lmode = master.botMode;
        Node lnode = master.currentNode;
        if(lnode == null && lmode) return;

        axiom.getSuperClass().accept(master.treeClassExpressionVisitor);
        if(master.currentNode == null && !master.botMode) return;

        List<Node> nodes = new LinkedList<>();
        if(lmode){
            nodes.add(lnode);
        }
        if(!master.botMode){
            nodes.add(master.currentNode);
        }

        if(nodes.isEmpty()){
            master.makeFactNode(axiom);
        }
        else {
            master.makeAndNode(axiom, nodes);
        }
    }

    @Override
    public void visit(OWLEquivalentClassesAxiom axiom){
        OWLClassExpression left = axiom.getClassExpressionsAsList().get(0);
        OWLClassExpression right = axiom.getClassExpressionsAsList().get(1);

        left.accept(master.treeClassExpressionVisitor);
        boolean lmode = master.botMode;
        Node lnode = master.currentNode;

        right.accept(master.treeClassExpressionVisitor);
        if(lmode != master.botMode){
            master.makeFactNode(axiom);
        }
        else{
            List<Node> children = new LinkedList<>();
            if(lnode != null) children.add(lnode);
            if(master.currentNode != null) children.add(master.currentNode);

            if(!children.isEmpty()){
                master.makeOrNode(axiom, children);
            }
        }
    }

    @Override
    public void visit(OWLEquivalentObjectPropertiesAxiom axiom){
        List<Node> nodes = new LinkedList<>();

        for(OWLObjectPropertyExpression e : axiom.getProperties()){
            e.accept(master.treeObjectPropertyVisitor);
            if(master.currentNode != null){
                nodes.add(master.currentNode);
            }
        }

        if(!nodes.isEmpty()) master.makeOrNode(axiom, nodes);
    }

    @Override
    public void visit(OWLSubObjectPropertyOfAxiom axiom){
        axiom.getSubProperty().accept(master.treeObjectPropertyVisitor);
        master.makeSingleton(axiom);
    }

    @Override
    public void visit(OWLClassAssertionAxiom axiom){
        if(axiom.getClassExpression().isTopEntity())
            master.currentNode = null;
        else
            master.makeFactNode(axiom);
    }

    @Override
    public void visit(OWLTransitiveObjectPropertyAxiom axiom){
        axiom.getProperty().accept(master.treeObjectPropertyVisitor);
        master.makeSingleton(axiom);
    }

    @Override
    public void visit(OWLSubPropertyChainOfAxiom axiom){
        List<Node> nodes = new LinkedList<>();
        for(int i = 0; i < axiom.getPropertyChain().size(); i++){
            nodes.add(new LeafNode(axiom.getPropertyChain().get(i)));
        }
        master.makeAndNode(axiom, nodes);
    }

    @Override
    public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        master.makeFactNode(axiom);
    }
    @Override
    public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        axiom.getProperty().accept(master.treeObjectPropertyVisitor);
        master.makeSingleton(axiom);
    }
    @Override
    public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
        master.makeFactNode(axiom);
    }

    @Override
    public void visit(OWLObjectPropertyDomainAxiom axiom) {
        List<Node> nodes = new LinkedList<>();
        axiom.getProperty().accept(master.treeObjectPropertyVisitor);
        nodes.add(master.currentNode);
        axiom.getDomain().accept(master.treeClassExpressionVisitor);
        if(!master.botMode){
            nodes.add(master.currentNode);
            master.makeAndNode(axiom, nodes);
        }
        else{
            master.makeOrNode(axiom, nodes);
        }
    }

    @Override
    public void visit(OWLDifferentIndividualsAxiom axiom) {
        List<Node> nodes = axiom.getIndividualsAsList().stream().map(LeafNode::new).collect(Collectors.toCollection(LinkedList::new));
        master.makeOrNode(axiom, nodes);
    }

    @Override
    public void visit(OWLObjectPropertyRangeAxiom axiom) {
        List<Node> nodes = new LinkedList<>();
        axiom.getProperty().accept(master.treeObjectPropertyVisitor);
        nodes.add(master.currentNode);
        axiom.getRange().accept(master.treeClassExpressionVisitor);
        if(master.botMode){
            master.makeOrNode(axiom, nodes);
        }
        else{
            nodes.add(master.currentNode);
            master.makeAndNode(axiom, nodes);
        }
    }

    @Override
    public void visit(OWLObjectPropertyAssertionAxiom axiom) {
        //TODO: Reintroduce after testing is completed, as it would be logically sound
        //if(!axiom.getProperty().isTopEntity())
        master.makeFactNode(axiom);
    }
    @Override
    public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        axiom.getProperty().accept(master.treeObjectPropertyVisitor);
        master.makeSingleton(axiom);
    }

    @Override
    public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
        axiom.getProperty().accept(master.treeObjectPropertyVisitor);
        master.makeSingleton(axiom);
    }

    @Override
    public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        axiom.getProperty().accept(master.treeObjectPropertyVisitor);
        master.makeSingleton(axiom);
    }

    @Override
    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        axiom.getProperty().accept(master.treeObjectPropertyVisitor);
        master.makeSingleton(axiom);
    }
    @Override
    public void visit(OWLSameIndividualAxiom axiom) {
        List<Node> nodes = axiom.getIndividualsAsList().stream().map(LeafNode::new).collect(Collectors.toCollection(LinkedList::new));
        master.makeOrNode(axiom, nodes);
    }

    @Override
    public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        List<Node> nodes = new LinkedList<>();
        axiom.getFirstProperty().accept(master.treeObjectPropertyVisitor);
        nodes.add(master.currentNode);
        axiom.getSecondProperty().accept(master.treeObjectPropertyVisitor);
        nodes.add(master.currentNode);
        master.makeOrNode(axiom, nodes);
    }


    @Override
    public void visit(OWLDisjointClassesAxiom axiom) {
        List<Node> nodes = new ArrayList<>(axiom.getClassExpressions().size());
        OWLClassExpression foundTop = null;
        for(OWLClassExpression oce : axiom.getClassExpressionsAsList()){
            oce.accept(master.treeClassExpressionVisitor);
            if(!master.botMode){
                if(foundTop != null){
                    master.makeFactNode(axiom);
                    return;
                }
                else{
                    foundTop = oce;
                }
            }
            else if(master.botMode){
                nodes.add(master.currentNode);
            }
        }

        if(foundTop != null){
            master.makeOrNode(axiom, nodes);
        }
        else{
            List<Node> rnodes = new LinkedList<>();
            for(int i = 0; i < nodes.size(); i++){
                for(int j = i + 1; j < nodes.size(); j++){
                    rnodes.add(new AndNode(axiom, Node.makeList(nodes.get(i), nodes.get(j))));
                }
            }
            master.currentNode = new AxiomOrNode(rnodes);
        }
    }

    @Override
    public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        List<Node> props = new LinkedList<>();
        //List<OWLObjectPropertyExpression> expr = new LinkedList<>();

        for(OWLObjectPropertyExpression oce : axiom.getProperties()){
            oce.accept(master.treeObjectPropertyVisitor);
            props.add(master.currentNode);
            //expr.add(oce);
        }

        List<Node> rnodes = new LinkedList<>();
        for(int i = 0; i < props.size(); i++){
            if(props.get(i) == null) continue;

            for(int j = i + 1; j < props.size(); j++){
                if(props.get(j) == null) continue;

                rnodes.add(new AndNode(axiom, Node.makeList(props.get(i), props.get(j))));
            }
        }
        master.currentNode = new AxiomOrNode(rnodes);
    }

    @Override
    public void visit(OWLDisjointUnionAxiom axiom) {
        List<Node> nodes = new LinkedList<>();
        axiom.getOWLClass().accept(master.treeClassExpressionVisitor);
        boolean lMode = master.botMode;
        nodes.add(master.currentNode);

        boolean foundTop = false;
        for(OWLClassExpression oce : axiom.getOWLDisjointClassesAxiom().getClassExpressions()){
            oce.accept(master.treeClassExpressionVisitor);
            if(!master.botMode){
                if(foundTop || lMode){
                    master.makeFactNode(axiom);
                    return;
                }
                foundTop = true;
            }
            nodes.add(master.currentNode);
        }

        if(!foundTop && !lMode){
            master.makeFactNode(axiom);
            return;
        }

        master.makeOrNode(axiom, nodes);
    }
}
