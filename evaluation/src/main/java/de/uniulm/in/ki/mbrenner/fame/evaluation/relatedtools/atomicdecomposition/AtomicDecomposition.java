package de.uniulm.in.ki.mbrenner.fame.evaluation.relatedtools.atomicdecomposition;

import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.*;

/**
 * Created by spellmaker on 14.03.2016.
 */
public class AtomicDecomposition {


    public void decompose(OWLOntology ontology){
        List<OWLAxiom> toDoAxioms = new LinkedList<>();

        RuleSet rs = (new BottomModeRuleBuilder()).buildRules(ontology);

        Set<OWLAxiom> topModule = (new RBMExtractorNoDef(false)).extractModule(rs, ontology.getSignature());
        Set<OWLAxiom> botModule = rs.getBaseModule();

        Map<OWLAxiom, Set<OWLAxiom>> modules = new HashMap<>();
        Map<OWLAxiom, Set<OWLAxiom>> atoms = new HashMap<>();

        Map<OWLAxiom, List<OWLAxiom>> dependencies = new HashMap<>();

        topModule.stream().filter(x -> !botModule.contains(x)).forEach(x -> toDoAxioms.add(x));

        Set<OWLAxiom> genAxioms = new HashSet<>();
        for(OWLAxiom a : toDoAxioms){
            Set<OWLAxiom> module = (new RBMExtractorNoDef(false)).extractModule(rs, a.getSignature());
            boolean newMod = true;
            for(OWLAxiom b : genAxioms){
                if(modules.get(b).equals(module)){
                    atoms.get(b).add(a);
                    newMod = false;
                }
            }
            if(newMod){
                Set<OWLAxiom> tmp = new HashSet<>();
                tmp.add(a);
                atoms.put(a, tmp);
                modules.put(a, module);
                genAxioms.add(a);
            }
        }
        for(OWLAxiom a : genAxioms){
            for(OWLAxiom b : genAxioms){
                if(modules.get(a).contains(b)){
                    addDependency(dependencies, b, a);
                }
            }
        }

        System.out.println("found " + genAxioms.size() + " generating axioms");
        for(OWLAxiom a : genAxioms){
            System.out.println(a);
        }

    }

    /**
     * Adds dependency a dependsOn b
     * @param deps Map of dependencies
     * @param a An axiom
     * @param b Another axiom
     */
    private void addDependency(Map<OWLAxiom, List<OWLAxiom>> deps, OWLAxiom a, OWLAxiom b){
        List<OWLAxiom> d = deps.get(a);
        if(d == null){
            d = new LinkedList<>();
            deps.put(a, d);
        }
        d.add(b);
    }
}

class DepNode{
    OWLAxiom axiom;
    List<OWLAxiom> prev;
    List<OWLAxiom> next;
}