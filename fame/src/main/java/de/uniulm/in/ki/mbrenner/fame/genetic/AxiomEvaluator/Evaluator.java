package de.uniulm.in.ki.mbrenner.fame.genetic.AxiomEvaluator;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.Map;
import java.util.Set;

/**
 * Created by spellmaker on 14.06.2016.
 */
public class Evaluator {
    AxiomEvaluator axiomEvaluator = new AxiomEvaluator(this);
    ClassEvaluator classEvaluator = new ClassEvaluator(this);
    PropertyEvaluator propertyEvaluator = new PropertyEvaluator(this);

    Map<OWLObject, Boolean> interpretAsBot;
    Set<OWLEntity> signature;

    public boolean isLocal(OWLAxiom axiom, Map<OWLObject, Boolean> interpretAsBot, Set<OWLEntity> signature){
        this.interpretAsBot = interpretAsBot;
        this.signature = signature;

        return axiom.accept(axiomEvaluator);
    }
}

enum ExpressionState{
    BOT,
    TOP,
    UNKNOWN
}