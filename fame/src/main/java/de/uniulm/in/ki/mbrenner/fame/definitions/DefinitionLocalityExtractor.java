package de.uniulm.in.ki.mbrenner.fame.definitions;

import de.uniulm.in.ki.mbrenner.fame.definitions.builder.DefinitionBuilder;
import de.uniulm.in.ki.mbrenner.fame.definitions.evaluator.DefinitionEvaluator;
import de.uniulm.in.ki.mbrenner.fame.extractor.DirectLocalityExtractor;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 28.04.2016.
 */
public class DefinitionLocalityExtractor {
    public DefinitionLocalityExtractor(){

    }

    //data structures to track definitions
    private Map<OWLAxiom, Set<OWLObject>> axiomToDefinitions = new HashMap<>(); //maps axioms to definitions for later deletion
    private Map<OWLEntity, Set<OWLAxiom>> entityToAxioms = new HashMap<>();    //maps entities to dependent axioms
    public Map<OWLObject, OWLObject> finalDefinitions;
    public Set<OWLEntity> finalSignature;
    public Set<OWLEntity> finalExtSignature;

    public Set<OWLAxiom> getDefinitionLocalityModule(Collection<OWLAxiom> axiomCollection, Set<OWLEntity> sign){
        Set<OWLAxiom> axioms = new HashSet<>(axiomCollection);
        Set<OWLEntity> signature = new HashSet<>(sign);
        Map<OWLObject, OWLObject> definitions = new HashMap<>();
        Set<OWLAxiom> module = new HashSet<>();
        Set<OWLAxiom> remaining = new HashSet<>(axioms);

        //get a rule set for the axioms, this will be used to restrict the range of axioms, which need to be checked
        //for locality
        RuleSet rs = new RuleSet();
        BottomModeRuleBuilder bmrb = new BottomModeRuleBuilder();
        bmrb.buildRules(axioms, axioms.stream().map(x -> x.getSignature()).flatMap(Collection::stream).collect(Collectors.toSet()), true, rs, rs);
        DirectLocalityExtractor direct = new DirectLocalityExtractor(false);

        //the extended signature is the normal signature extended by the elements, for which there are definitions
        Set<OWLEntity> extSignature = new HashSet<>();
        extSignature.addAll(signature);

        DefinitionBuilder db = new DefinitionBuilder();
        DefinitionEvaluator de = new DefinitionEvaluator();

        int prev_size = remaining.size();
        int step = 0;
        while(true){
            OWLAxiom nonLocal = null;
            for(OWLAxiom ax : direct.extractModule(rs, extSignature)){
                if(!remaining.contains(ax)) continue;
                if(!de.isDefinitionLocal(ax, signature, definitions)){
                    nonLocal = ax;
                    break;
                }
            }
            if(nonLocal == null) break;
            //System.out.println("non-local axiom is " + nonLocal);

            //try to fix the non-local axiom
            //TODO: Can a higher order definition hide other definitions?
            Map<OWLObject, OWLObject> ndef = db.tryDefine(nonLocal, definitions, signature);
            if(ndef == null){
                //System.out.println("rolling back definitions");
                //System.out.println(remaining.size());
                Set<OWLAxiom> change = resolveConflict(nonLocal, signature, definitions);
                module.addAll(change);
                remaining.removeAll(change);
                //System.out.println(remaining.size());
                //System.out.println(prev_size);
            }
            else{
                //System.out.println("axiom is local now");
                Set<OWLObject> axiomDefinitions = new HashSet<>();
                for(Map.Entry<OWLObject, OWLObject> entry : ndef.entrySet()){
                    if(definitions.get(entry.getKey()) != null) continue;

                    //System.out.println("new definition: " + entry);

                    //for each new definition
                    //1. add it to the set of definitions
                    definitions.put(entry.getKey(), entry.getValue());
                    //2. add them to the set of things defined by the axiom
                    axiomDefinitions.add(entry.getKey());
                    //3. add the defined symbols to the extended Signature
                    extSignature.addAll(entry.getKey().getSignature());
                }
                axiomToDefinitions.put(nonLocal, axiomDefinitions);
                for(OWLEntity e : db.getDependent()){
                    Set<OWLAxiom> depAxs = entityToAxioms.get(e);
                    if(depAxs == null){
                        depAxs = new HashSet<>();
                        entityToAxioms.put(e, depAxs);
                    }
                    depAxs.add(nonLocal);
                }
            }
            //debug check to determine correctness: Either the found axiom is local after the change or the
            //size has decreased
            //System.out.println("prev: " + prev_size + " rem: " + remaining.size());
            if(!de.isDefinitionLocal(nonLocal, signature, definitions) && prev_size <= remaining.size()){
                //if(remaining.contains(nonLocal)) System.out.println("nonlocal is in remaining");
                throw new IllegalArgumentException("error in implementation (step is " + step + ")");
            }
            prev_size = remaining.size();
            step++;
        }

        finalDefinitions = Collections.unmodifiableMap(definitions);
        finalSignature = Collections.unmodifiableSet(signature);
        finalExtSignature = Collections.unmodifiableSet(extSignature);

        return module;
    }

    private Set<OWLAxiom> resolveConflict(OWLAxiom root, Set<OWLEntity> signature, Map<OWLObject, OWLObject> definitions){
        Set<OWLAxiom> modAdd = new HashSet<>();
        modAdd.add(root);
        signature.addAll(root.getSignature());
        for(OWLEntity e : root.getSignature()){
            Set<OWLAxiom> s = entityToAxioms.get(e);
            entityToAxioms.remove(e);
            if(s == null) continue;
            for(OWLAxiom ax : s){
                if(axiomToDefinitions.get(ax) == null){
                    continue;
                }
                for(OWLObject o : axiomToDefinitions.get(ax)){
                    definitions.remove(o);
                }
                axiomToDefinitions.remove(ax);
                //modAdd.add(ax);
                //signature.addAll(ax.getSignature());
                modAdd.addAll(resolveConflict(ax, signature, definitions));
            }
        }
        return modAdd;
    }
}
