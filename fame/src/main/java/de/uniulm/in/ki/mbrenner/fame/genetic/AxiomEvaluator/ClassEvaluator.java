package de.uniulm.in.ki.mbrenner.fame.genetic.AxiomEvaluator;

import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.ClassVisitorAdapterEx;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import javax.annotation.Nonnull;

/**
 * Created by spellmaker on 14.06.2016.
 */
public class ClassEvaluator extends ClassVisitorAdapterEx<ExpressionState>{
    private Evaluator parent;

    public ClassEvaluator(Evaluator parent){
        this.parent = parent;
    }

    @Override
    public @Nonnull ExpressionState visit(@Nonnull OWLObjectSomeValuesFrom expr){
        switch(expr.getProperty().accept(parent.propertyEvaluator)){
            case BOT: return ExpressionState.BOT;
            case TOP:
                switch(expr.getFiller().accept(parent.classEvaluator)){
                    case BOT: return ExpressionState.BOT;
                    case UNKNOWN: return ExpressionState.UNKNOWN;
                    case TOP: return ExpressionState.TOP;
                }
            case UNKNOWN:
                switch(expr.getFiller().accept(parent.classEvaluator)){
                    case BOT: return ExpressionState.BOT;
                    case UNKNOWN: return ExpressionState.UNKNOWN;
                    case TOP: return ExpressionState.UNKNOWN;
                }
        }
        return ExpressionState.UNKNOWN;
    }

    @Override
    public @Nonnull ExpressionState visit(@Nonnull OWLObjectIntersectionOf expr){
        ExpressionState res = ExpressionState.TOP;
        for(OWLClassExpression oce : expr.getOperands()){
            ExpressionState c = oce.accept(parent.classEvaluator);
            if(c == ExpressionState.BOT) return c;
            if(c == ExpressionState.UNKNOWN) res = c;
        }
        return res;
    }

    @Override
    public @Nonnull ExpressionState visit(@Nonnull OWLClass expr){
        if(expr.isBottomEntity()) return ExpressionState.BOT;
        if(expr.isTopEntity()) return ExpressionState.TOP;

        if(parent.signature.contains(expr)) return ExpressionState.UNKNOWN;
        else return (parent.interpretAsBot.get(expr)) ? ExpressionState.BOT : ExpressionState.TOP;
    }
}
