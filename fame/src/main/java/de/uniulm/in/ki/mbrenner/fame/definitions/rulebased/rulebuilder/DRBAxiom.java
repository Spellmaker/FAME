package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder;

import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBDefinition;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

import java.util.Collections;

/**
 * Axiom visitor for the IDRBRuleBuilder
 *
 * Created by spellmaker on 25.05.2016.
 */
class DRBAxiom extends OWLAxiomVisitorAdapter{
    private final DRBRuleBuilder parent;

    public DRBAxiom(DRBRuleBuilder parent){
        this.parent = parent;
    }

    @Override
    public void visit(OWLSubClassOfAxiom axiom){
        axiom.getSubClass().accept(parent.classVisitor);
        parent.addRules();
        axiom.getSuperClass().accept(parent.classVisitor);
        if(parent.botMode){
            parent.ruleBuffer.pop();
            parent.ruleSet.addRule(DRBRuleFactory.getExternalRule(
                    axiom,
                    DefFinder.getDefinitions(axiom.getSubClass(), axiom.getSuperClass()),
                    axiom.getSubClass()
            ));
        }
        else{
            parent.addRules();
            parent.ruleSet.addRule(DRBRuleFactory.getExternalRule(axiom,
                    DefFinder.getDefinitions(axiom.getSubClass(), axiom.getSuperClass()),
                    axiom.getSubClass(), axiom.getSuperClass()));
        }
    }

    @Override
    public void visit(OWLSubObjectPropertyOfAxiom axiom){
        parent.ruleSet.addRule(DRBRuleFactory.getExternalRule(axiom,
                DefFinder.getDefinitions(axiom.getSubProperty(), axiom.getSuperProperty()),
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
                parent.ruleSet.addRule(DRBRuleFactory.getExternalRule(axiom, DefFinder.getDefinitions(right, left)));
            }
            else{
                parent.ruleSet.addRule(DRBRuleFactory.getExternalRule(axiom, DefFinder.getDefinitions(left, right)));
            }
        }
        else{
            parent.addRules();
            parent.addRules();
            parent.ruleSet.addRule(DRBRuleFactory.getExternalRule(axiom, DefFinder.getDefinitions(left, right), left));
            parent.ruleSet.addRule(DRBRuleFactory.getExternalRule(axiom, DefFinder.getDefinitions(right, left), right));
        }
    }

    @Override
    public void visit(OWLTransitiveObjectPropertyAxiom axiom){
        parent.ruleSet.addRule(DRBRuleFactory.getExternalRule(axiom, axiom.getProperty()));
    }
}
