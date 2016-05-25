package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder;

import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRule;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleFactory;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleSet;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

import java.util.Set;
import java.util.Stack;

/**
 * Builds extraction rules for a given ontology
 *
 * These rules can be used to extract a definition locality or even a simple bot-locality module
 * from the given ontology
 *
 * Created by Spellmaker on 13.05.2016.
 */
public class DRBRuleBuilder extends OWLAxiomVisitorAdapter {
    boolean botMode;
    Stack<Set<DRBRule>> ruleBuffer;

    private final DRBClass classVisitor;

    DRBRuleSet ruleSet;

    /**
     * Default constructor
     */
    public DRBRuleBuilder(){
        this.classVisitor = new DRBClass(this);
    }

    /**
     * Builds rules for the provided ontology
     * @param ontology An OWL Ontology which lies in the EL+ language
     * @return A DRBRuleSet containing the rules and necessary data structures for fast access
     */
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
