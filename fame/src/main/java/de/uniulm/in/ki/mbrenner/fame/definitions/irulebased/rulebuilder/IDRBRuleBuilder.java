package de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rulebuilder;

import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule.IDRBRule;
import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule.IDRBRuleSet;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

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
public class IDRBRuleBuilder {
    boolean botMode;
    Stack<Set<IDRBRule>> ruleBuffer;

    final IDRBClass classVisitor;
    private final IDRBAxiom axiomVisitor;

    IDRBRuleSet ruleSet;

    /**
     * Default constructor
     */
    public IDRBRuleBuilder(){
        this.classVisitor = new IDRBClass(this);
        this.axiomVisitor = new IDRBAxiom(this);
    }

    /**
     * Builds rules for the provided ontology
     * @param ontology An OWL Ontology which lies in the EL+ language
     * @return A IDRBRuleSet containing the rules and necessary data structures for fast access
     */
    public IDRBRuleSet buildRules(OWLOntology ontology){
        ruleSet = new IDRBRuleSet();
        ruleBuffer = new Stack<>();

        for(OWLAxiom axiom: ontology.getAxioms()){
            axiom.accept(axiomVisitor);
        }
        return ruleSet;
    }

    void addRules(){
        addRules(ruleBuffer.pop());
    }

    private void addRules(Set<IDRBRule> rules){
        rules.forEach(x -> ruleSet.addRule(x));
    }
}
