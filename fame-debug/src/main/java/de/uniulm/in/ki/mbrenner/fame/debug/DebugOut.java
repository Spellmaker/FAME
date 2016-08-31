package de.uniulm.in.ki.mbrenner.fame.debug;

import de.uniulm.in.ki.mbrenner.fame.definitions.TestCorrectness;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.DRBExtractor;
import de.uniulm.in.ki.mbrenner.owlprinter.OWLPrinter;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 01.06.2016.
 */
public class DebugOut {
    public static void printDebugDefModule(OWLOntology ontology, Set<OWLAxiom> module, Map<OWLObject, OWLObject> definitions, Set<OWLEntity> signature){
        Collection<OWLAxiom> fail = TestCorrectness.isDefinitionLocalModule(ontology, module, definitions, signature);

        System.out.println("Non-local axioms: " + fail.size());
        fail.forEach(System.out::println);
        System.out.println("Module size: " + module.size());
        Set<OWLEntity> modSig = module.stream().map(OWLAxiom::getSignature).flatMap(Collection::stream).collect(Collectors.toSet());
        System.out.println("Module signature: " + modSig);
        System.out.println("Definitions:");
        if(definitions.isEmpty()) System.out.println("none");
        OWLPrinter.println(definitions);
        System.out.println("module: " + module);
    }
}
