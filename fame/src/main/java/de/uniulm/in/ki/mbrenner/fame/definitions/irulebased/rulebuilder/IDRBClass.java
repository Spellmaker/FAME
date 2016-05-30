package de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rulebuilder;

import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule.IDRBRule;
import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule.IDRBRuleFactory;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Class Visitor for the IDRBRuleBuilder
 *
 * Created by Spellmaker on 13.05.2016.
 */
class IDRBClass extends OWLClassExpressionVisitorAdapter{
    private final IDRBRuleBuilder parent;

    public IDRBClass(IDRBRuleBuilder drbRuleBuilder) {
        this.parent = drbRuleBuilder;
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom expression){
        expression.getFiller().accept(this);
        Set<IDRBRule> rules = new HashSet<>();
        if(parent.botMode){
            rules.addAll(parent.ruleBuffer.pop());
            rules.add(IDRBRuleFactory.getInternalRule(parent.ruleSet, expression, expression.getProperty(), expression.getFiller()));
        }
        else{
            parent.ruleBuffer.pop();
            rules.add(IDRBRuleFactory.getInternalRule(parent.ruleSet, expression, expression.getProperty()));
        }
        parent.ruleBuffer.push(rules);
    }

    @Override
    public void visit(OWLObjectIntersectionOf expression){
        Set<OWLClassExpression> classes = new HashSet<>();
        for(OWLClassExpression expr : expression.getOperands()){
            expr.accept(this);
            if(parent.botMode){
                classes.add(expr);
            }
            else{
                parent.ruleBuffer.pop();
            }
        }

        if(classes.isEmpty()){
            parent.ruleBuffer.push(Collections.emptySet());
        }
        else{
            OWLClassExpression[] array = new OWLClassExpression[classes.size()];
            int pos = 0;
            for(OWLClassExpression e : classes){
                array[pos++] = e;
            }

            Set<IDRBRule> r = new HashSet<>();
            for(int i = 0; i < classes.size(); i++) r.addAll(parent.ruleBuffer.pop());
            r.add(IDRBRuleFactory.getInternalRule(parent.ruleSet, expression, array));

            parent.ruleBuffer.push(r);
        }
    }

    @Override
    public void visit(OWLClass expression){
        parent.ruleBuffer.push(Collections.emptySet());
        parent.botMode = !expression.isTopEntity();
    }
}
