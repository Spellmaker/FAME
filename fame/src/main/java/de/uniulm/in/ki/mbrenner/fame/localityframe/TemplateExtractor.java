package de.uniulm.in.ki.mbrenner.fame.localityframe;

import de.uniulm.in.ki.mbrenner.owlprinter.OWLPrinter;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by spellmaker on 13.06.2016.
 */
public class TemplateExtractor {
    public static Set<OWLAxiom> extract(OWLOntology ontology, Set<OWLEntity> signature, LocalityChecker check){
        return extract(ontology, signature, check, false);
    }

    public static Set<OWLAxiom> extract(OWLOntology ontology, Set<OWLEntity> signature, LocalityChecker check, boolean debug){
        return extract(ontology.getAxioms(), signature, check, debug);
    }


    public static Set<OWLAxiom> extract(Set<OWLAxiom> ontology, Set<OWLEntity> signature, LocalityChecker check, boolean debug){
        Set<OWLEntity> currentSignature = new HashSet<>(signature);
        Set<OWLAxiom> module = new HashSet<>();
        boolean changed = true;
        int run = 0;
        while(changed){
            if(debug) System.out.println("Iteration "+ run++);
            changed = false;
            for(OWLAxiom axiom : ontology){
                if(module.contains(axiom)) continue;

                if(!check.isLocal(axiom, currentSignature)){
                    changed = true;
                    module.add(axiom);
                    if(debug) System.out.println("\tAdding non-local axiom " + OWLPrinter.getString(axiom));
                    currentSignature.addAll(axiom.getSignature());
                }
            }
        }
        return module;
    }
}
