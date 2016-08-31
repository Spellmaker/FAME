package de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rulebuilder;

import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule.IDRBRule;
import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule.IDRBRuleFactory;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder.DRBRuleBuilder;
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
class IDRBClass extends ClassVisitorAdapterEx<Set<IDRBRule>> {
    private final IDRBRuleBuilder parent;

    public IDRBClass(IDRBRuleBuilder drbRuleBuilder) {
        this.parent = drbRuleBuilder;
    }

    @Override
    public Set<IDRBRule> visit(OWLObjectSomeValuesFrom expression){
        Set<IDRBRule> result = new HashSet<>(expression.getFiller().accept(this));
        if(parent.botMode){
            result.add(IDRBRuleFactory.getInternalRule(parent.ruleSet, expression, expression.getProperty(), expression.getFiller()));
        }
        else{
            parent.botMode = true;
            result.clear();
            result.add(IDRBRuleFactory.getInternalRule(parent.ruleSet, expression, expression.getProperty()));
        }
        return result;
    }

    @Override
    public Set<IDRBRule> visit(OWLObjectIntersectionOf expression){
        Set<IDRBRule> result = new HashSet<>();
        Set<OWLClassExpression> classes = new HashSet<>();
        for(OWLClassExpression expr : expression.getOperands()){
            Set<IDRBRule> tmp = expr.accept(this);
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
            result.add(IDRBRuleFactory.getInternalRule(parent.ruleSet, expression, array));

            return result;
        }
    }

    @Override
    public Set<IDRBRule> visit(OWLClass expression){
        parent.botMode = !expression.isTopEntity();
        return Collections.emptySet();
    }

    @Override
    public Set<IDRBRule> visit(OWLObjectHasValue expression){
        parent.botMode = !expression.getProperty().isTopEntity();
        return Collections.singleton(IDRBRuleFactory.getInternalRule(parent.ruleSet, expression, expression.getProperty()));
    }

    @Override
    public Set<IDRBRule> visit(OWLObjectOneOf expression){
        parent.botMode = true;
        return Collections.singleton(IDRBRuleFactory.getInternalRule(parent.ruleSet, expression));
    }
}
