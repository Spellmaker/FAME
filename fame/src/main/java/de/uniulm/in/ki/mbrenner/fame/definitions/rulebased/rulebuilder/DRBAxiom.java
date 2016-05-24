package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder;

import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRule;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleFactory;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleSet;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

import java.util.Set;
import java.util.Stack;

/**
 * Created by Spellmaker on 13.05.2016.
 */
public class DRBAxiom extends OWLAxiomVisitorAdapter {
    boolean botMode;
    Stack<Set<DRBRule>> ruleBuffer;

    DRBClass classVisitor;

    DRBRuleSet ruleSet;

    public DRBAxiom(){
        this.classVisitor = new DRBClass(this);
    }

    public DRBRuleSet buildRules(OWLOntology ontology){
        ruleSet = new DRBRuleSet();
        ruleBuffer = new Stack<>();

        for(OWLAxiom axiom: ontology.getAxioms()){
            axiom.accept(this);
        }
        return ruleSet;
    }

    private void addRules(){
        addRules(ruleBuffer.pop());
    }

    private void addRules(Set<DRBRule> rules){
        rules.forEach(x -> ruleSet.addRule(x));
    }

    @Override
    public void visit(OWLSubClassOfAxiom axiom){
        axiom.getSubClass().accept(classVisitor);
        addRules();
        axiom.getSuperClass().accept(classVisitor);
        if(botMode){
            ruleBuffer.pop();
            ruleSet.addRule(DRBRuleFactory.getExternalRule(
                    axiom,
                    DefFinder.getDefinitions(axiom.getSubClass(), axiom.getSuperClass()),
                    axiom.getSubClass()
            ));
        }
        else{
            addRules();
            ruleSet.addRule(DRBRuleFactory.getExternalRule(axiom,
                    DefFinder.getDefinitions(axiom.getSubClass(), axiom.getSuperClass()),
                    axiom.getSubClass(), axiom.getSuperClass()));
        }
    }

    @Override
    public void visit(OWLSubObjectPropertyOfAxiom axiom){
        ruleSet.addRule(DRBRuleFactory.getExternalRule(axiom,
                DefFinder.getDefinitions(axiom.getSubProperty(), axiom.getSuperProperty()),
                axiom.getSubProperty()));

    }

    @Override
    public void visit(OWLEquivalentClassesAxiom axiom){
        OWLClassExpression left = axiom.getClassExpressionsAsList().get(0);
        OWLClassExpression right = axiom.getClassExpressionsAsList().get(1);

        left.accept(classVisitor);
        boolean state = botMode;
        right.accept(classVisitor);

        if(state != botMode){
            ruleBuffer.pop();
            ruleBuffer.pop();
            if(!state){
                ruleSet.addRule(DRBRuleFactory.getExternalRule(axiom, DefFinder.getDefinitions(right, left)));
            }
            else{
                ruleSet.addRule(DRBRuleFactory.getExternalRule(axiom, DefFinder.getDefinitions(left, right)));
            }
        }
        else{
            addRules();
            addRules();
            ruleSet.addRule(DRBRuleFactory.getExternalRule(axiom, DefFinder.getDefinitions(left, right), left));
            ruleSet.addRule(DRBRuleFactory.getExternalRule(axiom, DefFinder.getDefinitions(right, left), right));
        }
    }

    @Override
    public void visit(OWLTransitiveObjectPropertyAxiom axiom){
        ruleSet.addRule(DRBRuleFactory.getExternalRule(axiom, axiom.getProperty()));
    }
}
