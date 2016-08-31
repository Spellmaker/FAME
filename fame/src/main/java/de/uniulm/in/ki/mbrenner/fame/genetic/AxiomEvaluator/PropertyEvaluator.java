package de.uniulm.in.ki.mbrenner.fame.genetic.AxiomEvaluator;

import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.PropertyVisitorAdapterEx;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import javax.annotation.Nonnull;

/**
 * Created by spellmaker on 14.06.2016.
 */
public class PropertyEvaluator extends PropertyVisitorAdapterEx<ExpressionState>{
    private Evaluator parent;

    public PropertyEvaluator(Evaluator parent){
        this.parent = parent;
    }

    @Override
    public @Nonnull ExpressionState visit(@Nonnull OWLObjectProperty property){
        if(parent.signature.contains(property)) return ExpressionState.UNKNOWN;
        else return (parent.interpretAsBot.get(property)) ? ExpressionState.BOT : ExpressionState.TOP;
    }
}
