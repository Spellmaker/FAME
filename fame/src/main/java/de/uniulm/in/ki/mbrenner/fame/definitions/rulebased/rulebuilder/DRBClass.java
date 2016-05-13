package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder;

import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRule;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleFactory;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Spellmaker on 13.05.2016.
 */
public class DRBClass extends OWLClassExpressionVisitorAdapter{
    private DRBAxiom parent;

    public DRBClass(DRBAxiom drbAxiom) {
        this.parent = drbAxiom;
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom expression){
        expression.getFiller().accept(this);
        Set<DRBRule> rules = new HashSet<>();
        if(parent.botMode){
            rules.addAll(parent.ruleBuffer.pop());
            rules.add(DRBRuleFactory.getInternalRule(expression, expression.getProperty(), expression.getFiller()));
        }
        else{
            parent.ruleBuffer.pop();
            rules.add(DRBRuleFactory.getInternalRule(expression, expression.getProperty()));
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
            return;
        }
        else{
            OWLClassExpression[] array = new OWLClassExpression[classes.size()];
            int pos = 0;
            for(OWLClassExpression e : classes){
                array[pos++] = e;
            }

            Set<DRBRule> r = new HashSet<>();
            for(int i = 0; i < classes.size(); i++) r.addAll(parent.ruleBuffer.pop());
            r.add(DRBRuleFactory.getInternalRule(expression, array));

            parent.ruleBuffer.push(r);
        }
    }

    @Override
    public void visit(OWLClass expression){
        parent.ruleBuffer.push(Collections.emptySet());
        if(expression.isTopEntity())
            parent.botMode = false;
        else
            parent.botMode = true;
    }
}
