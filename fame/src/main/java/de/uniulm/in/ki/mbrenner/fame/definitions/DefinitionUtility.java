package de.uniulm.in.ki.mbrenner.fame.definitions;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import de.uniulm.in.ki.mbrenner.fame.util.ClassPrinter;
import de.uniulm.in.ki.mbrenner.fame.util.locality.SyntacticLocalityEvaluator;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 29.04.2016.
 */
public class DefinitionUtility {
    public static void printDefinitions(Map<OWLObject, OWLObject> definitions){
        for(Map.Entry<OWLObject, OWLObject> e : definitions.entrySet()){
            System.out.println(ClassPrinter.printClass(e.getKey()) + " = " + ClassPrinter.printClass(e.getValue()));
        }
    }

    public static boolean isSubset(Set<OWLAxiom> superSet, Set<OWLAxiom> subSet){
        for(OWLAxiom ax : subSet){
            if(!superSet.contains(ax)) return false;
        }
        return true;
    }

    public static Set<OWLAxiom> getCheckNeeded(Set<OWLAxiom> axioms, Set<OWLAxiom> module, Map<OWLObject, OWLObject> definitions, Set<OWLEntity> signature){
        Set<OWLEntity> extendedSignature = new HashSet<>();
        module.forEach(x -> extendedSignature.addAll(x.getSignature()));
        extendedSignature.addAll(signature);
        definitions.keySet().forEach(x -> extendedSignature.addAll(x.getSignature()));
        System.out.println("signature: " + extendedSignature);
        return axioms.stream().filter(x -> !module.contains(x) && needsCheck(x, extendedSignature)).collect(Collectors.toSet());
    }

    private static boolean needsCheck(OWLAxiom axiom, Set<OWLEntity> signature){
        SyntacticLocalityEvaluator synt = new SyntacticLocalityEvaluator(LocalityClass.BOTTOM_BOTTOM);
        return !synt.isLocal(axiom, signature);
    }
}
