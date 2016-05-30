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
public class DRBRuleBuilder{
    boolean botMode;
    Stack<Set<DRBRule>> ruleBuffer;

    final DRBClass classVisitor;
    private final DRBAxiom axiomVisitor;

    DRBRuleSet ruleSet;

    public static OWLOntology onto;

    /**
     * Default constructor
     */
    public DRBRuleBuilder(){
        this.classVisitor = new DRBClass(this);
        this.axiomVisitor = new DRBAxiom(this);
    }

    /**
     * Builds rules for the provided ontology
     * @param ontology An OWL Ontology which lies in the EL+ language
     * @return A IDRBRuleSet containing the rules and necessary data structures for fast access
     */
    public DRBRuleSet buildRules(OWLOntology ontology){
        onto = ontology;
        ruleSet = new DRBRuleSet();
        ruleBuffer = new Stack<>();

        for(OWLAxiom axiom: ontology.getAxioms()){
            axiom.accept(axiomVisitor);
        }
        return ruleSet;
    }

    void addRules(){
        addRules(ruleBuffer.pop());
    }

    private void addRules(Set<DRBRule> rules){
        rules.forEach(x -> ruleSet.addRule(x));
    }
}
