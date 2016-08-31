package de.uniulm.in.ki.mbrenner.fame.genetic;

import de.uniulm.in.ki.mbrenner.fame.genetic.AxiomEvaluator.Evaluator;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by spellmaker on 14.06.2016.
 */
public class ModuleExtractor {
    public static Set<OWLEntity> workingSignature;

    public static Set<OWLAxiom> extract(Set<OWLAxiom> axioms, Map<OWLObject, Boolean> interpretAsBot, Set<OWLEntity> signature){
        workingSignature = new HashSet<>(signature);
        Evaluator eval = new Evaluator();
        Set<OWLAxiom> module = new HashSet<>();

        boolean changed = true;
        while(changed){
            changed = false;
            for(OWLAxiom axiom : axioms){
                if(module.contains(axiom)) continue;
                if(!eval.isLocal(axiom, interpretAsBot, workingSignature)){
                    changed = true;
                    module.add(axiom);
                    workingSignature.addAll(axiom.getSignature());
                }
            }
        }

        return module;
    }
}
