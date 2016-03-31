package de.uniulm.in.ki.mbrenner.fame.incremental.rule;

import de.uniulm.in.ki.mbrenner.fame.rule.Rule;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by spellmaker on 16.03.2016.
 */
public class IncrementalAxiomVisitor extends OWLAxiomVisitorAdapter{
    private IncrementalRuleBuilder master;

    public IncrementalAxiomVisitor(IncrementalRuleBuilder master){
        this.master = master;
    }

    @Override
    public void visit(OWLDeclarationAxiom axiom){
        master.addRule(null, axiom, axiom.getEntity());
    }

    @Override
    public void visit(OWLSubClassOfAxiom axiom){
        axiom.getSubClass().accept(master.classExpressionVisitor);
        master.addRule(null, axiom, axiom.getSubClass());
    }

    @Override
    public void visit(OWLEquivalentClassesAxiom axiom){
        OWLClassExpression left = axiom.getClassExpressionsAsList().get(0);
        OWLClassExpression right = axiom.getClassExpressionsAsList().get(1);

        List<Rule> lRule = new LinkedList<>();
        left.accept(master.classExpressionVisitor);
        lRule.addAll(master.ruleBuffer);
        master.clearBuffer();
        right.accept(master.classExpressionVisitor);
        master.ruleBuffer.addAll(lRule);
        master.addRule(null, axiom, left);
        master.addRule(null, axiom, right);
    }

    @Override
    public void visit(OWLEquivalentObjectPropertiesAxiom axiom){
        List<Rule> rules = new LinkedList<>();

        for(OWLObjectPropertyExpression e : axiom.getProperties()){
            master.clearBuffer();
            e.accept(master.objectPropertyVisitor);
            master.addRule(null, axiom, e);
            rules.addAll(master.ruleBuffer);
        }

        master.ruleBuffer.addAll(rules);
    }

    @Override
    public void visit(OWLDifferentIndividualsAxiom axiom){
        master.addRule(null, axiom, (OWLObject[]) null);
    }

    @Override
    public void visit(OWLObjectPropertyAssertionAxiom axiom){
        master.addRule(null, axiom, (OWLObject[]) null);
    }

    @Override
    public void visit(OWLSubObjectPropertyOfAxiom axiom){
        axiom.getSubProperty().accept(master.objectPropertyVisitor);
        master.addRule(null, axiom, axiom.getSubProperty());
    }

    @Override
    public void visit(OWLClassAssertionAxiom axiom){
        master.addRule(null, axiom, (OWLObject[]) null);
    }

    @Override
    public void visit(OWLTransitiveObjectPropertyAxiom axiom){
        axiom.getProperty().accept(master.objectPropertyVisitor);
        master.addRule(null, axiom, axiom.getProperty());
    }

    @Override
    public void visit(OWLSameIndividualAxiom axiom){
        master.addRule(null, axiom, (OWLObject[]) null);
    }

    @Override
    public void visit(OWLSubPropertyChainOfAxiom axiom){
        OWLObject[] props = new OWLObject[axiom.getPropertyChain().size()];
        for(int i = 0; i < axiom.getPropertyChain().size(); i++){
            props[i] = axiom.getPropertyChain().get(i);
        }
        master.addRule(null, axiom, props);
    }
}
