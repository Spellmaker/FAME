package de.uniulm.in.ki.mbrenner.fame.genetic.AxiomEvaluator;

import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.AxiomVisitorAdapterEx;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * Created by spellmaker on 14.06.2016.
 */
public class AxiomEvaluator extends AxiomVisitorAdapterEx<Boolean>{
    private Evaluator parent;

    public AxiomEvaluator(Evaluator parent){
        this.parent = parent;
    }

    @Override
    public @Nonnull Boolean visit(@Nonnull OWLSubClassOfAxiom axiom) {
        return axiom.getSubClass().accept(parent.classEvaluator) == ExpressionState.BOT ||
                axiom.getSuperClass().accept(parent.classEvaluator) == ExpressionState.TOP;
    }

    @Override
    public @Nonnull Boolean visit(@Nonnull OWLEquivalentClassesAxiom axiom){
        Iterator<OWLClassExpression> iter = axiom.getClassExpressions().iterator();
        ExpressionState initial = iter.next().accept(parent.classEvaluator);
        if(initial == ExpressionState.UNKNOWN) return false;
        while(iter.hasNext()){
            if(iter.next().accept(parent.classEvaluator) != initial) return false;
        }
        return true;
    }

    @Override
    public @Nonnull Boolean visit(@Nonnull OWLDeclarationAxiom axiom){
        return !parent.signature.contains(axiom.getEntity());
    }

    @Override
    public @Nonnull Boolean visit(@Nonnull OWLTransitiveObjectPropertyAxiom axiom){
        return axiom.getProperty().accept(parent.propertyEvaluator) != ExpressionState.UNKNOWN;
    }

    @Override
    public @Nonnull Boolean visit(@Nonnull OWLSubObjectPropertyOfAxiom axiom){
        return axiom.getSubProperty().accept(parent.propertyEvaluator) == ExpressionState.BOT ||
                axiom.getSuperProperty().accept(parent.propertyEvaluator) == ExpressionState.TOP;
    }
}
