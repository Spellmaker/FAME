package de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rulebuilder;

import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule.IDRBRuleFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

/**
 * Axiom visitor for the IDRBRuleBuilder
 *
 * Created by spellmaker on 25.05.2016.
 */
class IDRBAxiom extends OWLAxiomVisitorAdapter{
    private final IDRBRuleBuilder parent;

    public IDRBAxiom(IDRBRuleBuilder parent){
        this.parent = parent;
    }

    @Override
    public void visit(OWLSubClassOfAxiom axiom){
        axiom.getSubClass().accept(parent.classVisitor);
        parent.addRules();
        axiom.getSuperClass().accept(parent.classVisitor);
        if(parent.botMode){
            parent.ruleBuffer.pop();
            parent.ruleSet.addRule(IDRBRuleFactory.getExternalRule(parent.ruleSet,
                    axiom,
                    IDefFinder.getDefinitions(parent.ruleSet, axiom.getSubClass(), axiom.getSuperClass()),
                    axiom.getSubClass()
            ));
        }
        else{
            parent.addRules();
            parent.ruleSet.addRule(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom,
                    IDefFinder.getDefinitions(parent.ruleSet, axiom.getSubClass(), axiom.getSuperClass()),
                    axiom.getSubClass(), axiom.getSuperClass()));
        }
    }

    @Override
    public void visit(OWLSubObjectPropertyOfAxiom axiom){
        parent.ruleSet.addRule(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom,
                IDefFinder.getDefinitions(parent.ruleSet, axiom.getSubProperty(), axiom.getSuperProperty()),
                axiom.getSubProperty()));

    }

    @Override
    public void visit(OWLEquivalentClassesAxiom axiom){
        OWLClassExpression left = axiom.getClassExpressionsAsList().get(0);
        OWLClassExpression right = axiom.getClassExpressionsAsList().get(1);

        left.accept(parent.classVisitor);
        boolean state = parent.botMode;
        right.accept(parent.classVisitor);

        if(state != parent.botMode){
            parent.ruleBuffer.pop();
            parent.ruleBuffer.pop();
            if(!state){
                parent.ruleSet.addRule(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, IDefFinder.getDefinitions(parent.ruleSet, right, left)));
            }
            else{
                parent.ruleSet.addRule(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, IDefFinder.getDefinitions(parent.ruleSet, left, right)));
            }
        }
        else{
            parent.addRules();
            parent.addRules();
            parent.ruleSet.addRule(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, IDefFinder.getDefinitions(parent.ruleSet, left, right), left));
            parent.ruleSet.addRule(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, IDefFinder.getDefinitions(parent.ruleSet, right, left), right));
        }
    }

    @Override
    public void visit(OWLTransitiveObjectPropertyAxiom axiom){
        parent.ruleSet.addRule(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, axiom.getProperty()));
    }
}
