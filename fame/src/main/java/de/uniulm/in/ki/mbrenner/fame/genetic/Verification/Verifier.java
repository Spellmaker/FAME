package de.uniulm.in.ki.mbrenner.fame.genetic.Verification;

import de.uniulm.in.ki.mbrenner.fame.genetic.AxiomEvaluator.AxiomEvaluator;
import de.uniulm.in.ki.mbrenner.fame.genetic.AxiomEvaluator.Evaluator;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 15.06.2016.
 */
public class Verifier {
    public static Set<OWLAxiom> verifyModule(Set<OWLAxiom> axioms, Set<OWLAxiom> module, Map<OWLObject, Boolean> interpretedAsBot){
        Set<OWLEntity> signature = module.stream().map(OWLAxiom::getSignature).flatMap(Collection::stream).collect(Collectors.toSet());

        Evaluator eval = new Evaluator();
        return module.stream().filter(x -> !module.contains(x)).filter(x -> !eval.isLocal(x, interpretedAsBot, signature)).collect(Collectors.toSet());
    }
}

