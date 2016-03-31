package de.uniulm.in.ki.mbrenner.fame.incremental.rule;

import de.uniulm.in.ki.mbrenner.fame.rule.Rule;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by spellmaker on 16.03.2016.
 */
public class IncrementalClassExpressionVisitor extends OWLClassExpressionVisitorAdapter {
    private IncrementalRuleBuilder master;

    public IncrementalClassExpressionVisitor(IncrementalRuleBuilder master){
        this.master = master;
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom expression){
        master.clearBuffer();
        List<Rule> ceRules = new LinkedList<>();
        expression.getFiller().accept(this);
        ceRules.addAll(master.ruleBuffer);
        master.clearBuffer();
        expression.getProperty().accept(master.objectPropertyVisitor);
        master.ruleBuffer.addAll(ceRules);
        master.addRule(expression, null, expression.getFiller(), expression.getProperty());
    }

    @Override
    public void visit(OWLObjectIntersectionOf expression){
        List<Rule> rules = new LinkedList<>();
        OWLObject[] arr = new OWLObject[expression.getOperands().size()];

        int pos = 0;
        for(OWLClassExpression oce : expression.getOperands()){
            master.clearBuffer();
            oce.accept(this);
            rules.addAll(master.ruleBuffer);
            arr[pos++] = oce;
        }
        master.clearBuffer();
        master.ruleBuffer.addAll(rules);
        master.addRule(expression, null, arr);
    }

    @Override
    public void visit(OWLClass expression){
        //nothing to do
    }
}
