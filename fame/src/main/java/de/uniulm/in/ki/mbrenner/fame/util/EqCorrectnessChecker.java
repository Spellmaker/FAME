package de.uniulm.in.ki.mbrenner.fame.util;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.locality.EquivalenceLocalityEvaluator;
import de.uniulm.in.ki.mbrenner.fame.locality.SyntacticLocalityEvaluator;
import org.semanticweb.owlapi.model.*;

import java.util.*;

/**
 * Created by spellmaker on 11.03.2016.
 */
public class EqCorrectnessChecker {
    public static OWLAxiom isCorrectEqModule(Set<OWLAxiom> module, RBMExtractor extractor, OWLOntology o, Set<OWLAxiom> normalModule){
        Set<OWLEntity> mSig = new HashSet<>();
        for(OWLAxiom a : module) mSig.addAll(a.getSignature());

        Map<OWLObject, OWLAxiom> definitions = extractor.getActiveDefinitions();
        EquivalenceLocalityEvaluator eq = new EquivalenceLocalityEvaluator(new SyntacticLocalityEvaluator(LocalityClass.BOTTOM_BOTTOM));
        List<OWLAxiom> defAxioms = new LinkedList<>();
        int i = 0;
        for(Map.Entry<OWLObject, OWLAxiom> entry : definitions.entrySet()){
            if(entry.getValue() instanceof OWLEquivalentClassesAxiom) {
                for (OWLClassExpression oce : ((OWLEquivalentClassesAxiom) entry.getValue()).getClassExpressions()) {
                    if (!oce.equals(entry.getKey())) {
                        eq.addDefinition(entry.getKey(), oce);
                        break;
                    }
                }
            }
            else if(entry.getValue() instanceof OWLSubClassOfAxiom){
                eq.addDefinition(entry.getKey(), ((OWLSubClassOfAxiom) entry.getValue()).getSubClass());
            }
            defAxioms.add(entry.getValue());
        }
        if(!eq.resolveDefinitions()){
            return module.iterator().next();
        }

        for(OWLAxiom a : o.getAxioms()){
            if(!normalModule.contains(a)) continue;
            if(defAxioms.contains(a)) continue;
            if(module.contains(a)) continue;

            if(a == null) System.out.println("a is null");
            if(!eq.isLocal(a, mSig)) return a;
        }
        return null;
    }
}
