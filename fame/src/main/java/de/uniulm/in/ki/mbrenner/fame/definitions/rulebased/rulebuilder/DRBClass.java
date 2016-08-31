package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder;

import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRule;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleFactory;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.ClassVisitorAdapter;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.ClassVisitorAdapterEx;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Class Visitor for the IDRBRuleBuilder
 *
 * Created by Spellmaker on 13.05.2016.
 */
class DRBClass extends ClassVisitorAdapterEx<Set<DRBRule>> {
    private final DRBRuleBuilder parent;

    public DRBClass(DRBRuleBuilder drbRuleBuilder) {
        this.parent = drbRuleBuilder;
    }

    @Override
    public Set<DRBRule> visit(OWLObjectSomeValuesFrom expression){
        Set<DRBRule> result = new HashSet<>(expression.getFiller().accept(this));
        if(parent.botMode){
            result.add(DRBRuleFactory.getInternalRule(expression, expression.getProperty(), expression.getFiller()));
        }
        else{
            parent.botMode = true;
            result.clear();
            result.add(DRBRuleFactory.getInternalRule(expression, expression.getProperty()));
        }
        return result;
    }

    @Override
    public Set<DRBRule> visit(OWLObjectIntersectionOf expression){
        Set<DRBRule> result = new HashSet<>();
        Set<OWLClassExpression> classes = new HashSet<>();
        for(OWLClassExpression expr : expression.getOperands()){
            Set<DRBRule> tmp = expr.accept(this);
            if(parent.botMode){
                classes.add(expr);
                result.addAll(tmp);
            }
        }

        if(classes.isEmpty()){
            return result;
        }
        else{
            OWLClassExpression[] array = new OWLClassExpression[classes.size()];
            int pos = 0;
            for(OWLClassExpression e : classes){
                array[pos++] = e;
            }
            result.add(DRBRuleFactory.getInternalRule(expression, array));

            return result;
        }
    }

    @Override
    public Set<DRBRule> visit(OWLClass expression){
        parent.botMode = !expression.isTopEntity();
        return Collections.emptySet();
    }

    @Override
    public Set<DRBRule> visit(OWLObjectHasValue expression){
        parent.botMode = !expression.getProperty().isTopEntity();
        return Collections.singleton(DRBRuleFactory.getInternalRule(expression, expression.getProperty()));
    }

    @Override
    public Set<DRBRule> visit(OWLObjectOneOf expression){
        parent.botMode = true;
        return Collections.singleton(DRBRuleFactory.getInternalRule(expression));
    }
}
