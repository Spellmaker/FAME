package de.uniulm.in.ki.mbrenner.fame.incremental.rule;

import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.rule.Rule;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.*;
/**
 * Created by spellmaker on 11.03.2016.
 */
public class IncrementalRuleBuilder {
    //package visibility for use with visitors
    boolean botMode;
    IncrementalAxiomVisitor axiomVisitor;
    IncrementalObjectPropertyVisitor objectPropertyVisitor;
    IncrementalClassExpressionVisitor classExpressionVisitor;
    List<Rule> ruleBuffer;
    List<OWLObject> unknownObjects;

    private IncrementalExtractor extractor;

    private IncrementalRuleBuilder(){
    }

    void clearBuffer(){
        ruleBuffer.clear();
    }

    void addRule(Integer head, Integer axiom, Integer...body){
        ruleBuffer.add(new Rule(head, axiom, null, body));
    }

    void addRule(OWLObject head, OWLAxiom axiom, OWLObject...body){
        Integer h = (head != null) ? extractor.getId(head) : null;
        Integer a = (axiom != null) ? extractor.getId(axiom) : null;
        if(body != null) {
            Integer[] b = new Integer[body.length];
            for (int i = 0; i < body.length; i++) b[i] = extractor.getId(body[i]);
            addRule(h, a, b);
        }
        else{
            addRule(h, a, (Integer[]) null);
        }
    }

    private void init(IncrementalExtractor extractor){
        this.extractor = extractor;
        botMode = false;
        axiomVisitor = new IncrementalAxiomVisitor(this);
        objectPropertyVisitor = new IncrementalObjectPropertyVisitor(this);
        classExpressionVisitor = new IncrementalClassExpressionVisitor(this);
        ruleBuffer = new LinkedList<>();
        unknownObjects = new LinkedList<>();
    }

    public static void buildRules(IncrementalExtractor extractor, OWLOntology ontology){
        (new IncrementalRuleBuilder()).build(extractor, ontology);
    }

    private void build(IncrementalExtractor extractor, OWLOntology ontology){
        init(extractor);

        //visit axioms to build rules
        for(OWLAxiom ax : ontology.getAxioms(Imports.INCLUDED)){
            ruleBuffer.clear();
            ax.accept(axiomVisitor);
            for(Rule r : ruleBuffer){
                extractor.addRule(ax, r);
            }
        }
        //complete ontology conversion
        ontology.getSignature().forEach(x -> extractor.getId(x));
        ontology.getIndividualsInSignature().forEach(x -> extractor.getId(x));
    }

}
