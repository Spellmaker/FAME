package de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder;

import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes.Node;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

import java.util.LinkedList;
import java.util.List;

/**
 * Class Expression Visitor for the TreeBuilder
 *
 * Created by spellmaker on 18.03.2016.
 */
class TreeClassExpressionVisitor extends OWLClassExpressionVisitorAdapter {
    private final TreeBuilder master;

    public TreeClassExpressionVisitor(TreeBuilder master){
        this.master = master;
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom expression){
        List<Node> nodes = new LinkedList<>();
        expression.getProperty().accept(master.treeObjectPropertyVisitor);
        if(master.currentNode != null) nodes.add(master.currentNode);
        else return;

        expression.getFiller().accept(this);
        if(master.botMode){
            if(master.currentNode != null){
                nodes.add(master.currentNode);
                master.makeAndNode(expression, nodes);
            }
        }
        else{
            master.makeAndNode(expression, nodes);
            master.botMode = true;
        }
    }

    @Override
    public void visit(OWLObjectAllValuesFrom expression) {
        List<Node> nodes = new LinkedList<>();
        expression.getProperty().accept(master.treeObjectPropertyVisitor);
        if(master.currentNode != null) nodes.add(master.currentNode);
        else return;

        expression.getFiller().accept(this);
        if(!master.botMode){
            if(master.currentNode != null){
                nodes.add(master.currentNode);
                master.makeAndNode(expression, nodes);
            }
        }
        else{
            master.makeAndNode(expression, nodes);
            master.botMode = true;
        }
    }

    @Override
    public void visit(OWLObjectIntersectionOf expression){
        List<Node> nodes = new LinkedList<>();
        boolean allTop = true;
        for(OWLClassExpression oce : expression.getOperands()){
            oce.accept(this);

            if(master.botMode){
                if(allTop){
                    allTop = false;
                    nodes.clear();
                }
                if(master.currentNode == null){
                    //one bot tautology found, therefore the intersection
                    //always evaluates to bot
                    master.botMode = true;
                    master.currentNode = null;
                    return;
                }
                nodes.add(master.currentNode);
            }
            else if(allTop){
                if(master.currentNode != null) nodes.add(master.currentNode);
            }
        }

        if(!nodes.isEmpty()){
            if(allTop){
                master.botMode = false;
                master.makeOrNode(expression, nodes);
            }
            else{
                master.botMode = true;
                master.makeAndNode(expression, nodes);
            }
        }
        else{
            //all nodes are top tautologies
            master.botMode = false;
            master.currentNode = null;
        }
    }

    @Override
    public void visit(OWLObjectUnionOf expression) {
        List<Node> nodes = new LinkedList<>();
        boolean allBot = true;
        for(OWLClassExpression oce : expression.getOperands()){
            oce.accept(this);

            if(!master.botMode){
                if(allBot){
                    allBot = false;
                    nodes.clear();
                }
                if(master.currentNode == null){
                    //one top tautology found, therefore the intersection
                    //always evaluates to bot
                    master.botMode = false;
                    master.currentNode = null;
                    return;
                }
                nodes.add(master.currentNode);
            }
            else if(allBot){
                if(master.currentNode != null) nodes.add(master.currentNode);
            }
        }

        if(!nodes.isEmpty()){
            if(allBot){
                master.botMode = true;
                master.makeOrNode(expression, nodes);
            }
            else{
                master.botMode = false;
                master.makeAndNode(expression, nodes);
            }
        }
        else{
            //all nodes are bot tautologies
            master.botMode = true;
            master.currentNode = null;
        }
    }

    @Override
    public void visit(OWLClass expression){
        master.makeLeafNode(expression);
        master.botMode = !expression.isOWLThing();
    }


    @Override
    public void visit(OWLObjectComplementOf ce) {
        ce.getOperand().accept(this);
        master.botMode = !master.botMode;
        master.makeSingleton(ce);
    }

    @Override
    public void visit(OWLObjectHasValue ce) {
        ce.getProperty().accept(master.treeObjectPropertyVisitor);
        master.makeSingleton(ce);
        master.botMode = true;
    }

    @Override
    public void visit(OWLObjectMinCardinality ce) {
        if(ce.getCardinality() <= 0){
            //tautology, can never become anything other than top
            master.botMode = false;
            master.currentNode = null;
        }
        else{
            //process property, ignore mode, as there are no modes for properties
            List<Node> nodes = new LinkedList<>();
            ce.getProperty().accept(master.treeObjectPropertyVisitor);
            nodes.add(master.currentNode);
            //process filler
            ce.getFiller().accept(this);
            if(master.botMode){
                //R, C -> >=n R.C
                nodes.add(master.currentNode);
                master.makeAndNode(ce, nodes);
            }
            else{
                master.botMode = true;
                master.makeOrNode(ce, nodes);
            }
        }
    }

    @Override
    public void visit(OWLObjectExactCardinality ce) {
        //process property, ignore mode, as there are no modes for properties
        List<Node> nodes = new LinkedList<>();
        ce.getProperty().accept(master.treeObjectPropertyVisitor);
        nodes.add(master.currentNode);
        ce.getFiller().accept(this);
        if(master.botMode){
            nodes.add(master.currentNode);
            master.makeAndNode(ce, nodes);
        }
        else{
            master.makeOrNode(ce, nodes);
        }

        master.botMode = ce.getCardinality() != 0;
    }

    @Override
    public void visit(OWLObjectMaxCardinality ce) {
        //process property, ignore mode, as there are no modes for properties
        List<Node> nodes = new LinkedList<>();
        ce.getProperty().accept(master.treeObjectPropertyVisitor);
        nodes.add(master.currentNode);
        //process filler
        ce.getFiller().accept(this);
        if(master.botMode){
            master.botMode = false;
            //R, C -> ER.C
            nodes.add(master.currentNode);
            master.makeAndNode(ce, nodes);
        }
        else{
            //R -> ER.C
            master.makeOrNode(ce, nodes);
        }
    }

    @Override
    public void visit(OWLObjectHasSelf ce) {
        ce.getProperty().accept(master.treeObjectPropertyVisitor);
        master.makeSingleton(ce);
        master.botMode = true;
    }

    @Override
    public void visit(OWLObjectOneOf ce) {
        if(ce.getIndividuals().isEmpty()){
            //always bottom
            master.botMode = true;
            master.currentNode = null;
        }
        else{
            master.makeFactNode(ce);
            master.botMode = true;
            //TODO: This has some repercussions, check!
        }
    }
}
