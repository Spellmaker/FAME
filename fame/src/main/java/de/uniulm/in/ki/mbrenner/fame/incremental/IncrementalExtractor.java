package de.uniulm.in.ki.mbrenner.fame.incremental;

import de.uniulm.in.ki.mbrenner.fame.incremental.rule.IncrementalRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.Rule;
import de.uniulm.in.ki.mbrenner.fame.util.ClassPrinter;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by spellmaker on 16.03.2016.
 */
public class IncrementalExtractor {
    //base module signature and axioms
    private Set<Integer> baseSignature;
    private Set<Integer> baseModule;
    //rules
    private List<Rule> rules;
    //map of elements to rules affected by them
    private Map<Integer, List<Integer>> ruleMap;
    //rule heads
    private List<Integer> ruleHeads;
    //marks rules which provide axioms
    private List<Boolean> isAxiomRule;
    //maps axioms to their precomputed signatures
    private Map<Integer, List<Integer>> axiomSignatures;
    //dictionary
    private List<OWLObject> dictionary;
    private Map<OWLObject, Integer> invDictionary;
    private Map<Integer, List<Rule>> axiomRules;

    public IncrementalExtractor(OWLOntology ontology){
        init();

        IncrementalRuleBuilder.buildRules(this, ontology);
    }

    public int ruleCount(){
        return rules.size();
    }

    public int dictionarySize(){
        return dictionary.size();
    }


    private void init(){
        baseSignature = new HashSet<>(); baseModule = new HashSet<>();
        ruleMap = new HashMap<>(); axiomSignatures = new HashMap<>(); invDictionary = new HashMap<>(); axiomRules = new HashMap<>();
        ruleHeads = new ArrayList<>(); isAxiomRule = new ArrayList<>(); dictionary = new ArrayList<>(); rules = new ArrayList<>();

        OWLDataFactory factory = new OWLDataFactoryImpl();
        getId(factory.getOWLThing());
    }

    public Integer getId(OWLObject object){
        Integer res = invDictionary.get(object);
        if(res == null){
            res = dictionary.size();
            dictionary.add(object);
            invDictionary.put(object, res);
        }
        return res;
    }

    public OWLObject getObject(Integer id){
        return dictionary.get(id);
    }

    private void updateAxiomMap(int axiom, Rule r){
        List<Rule> l = axiomRules.get(axiom);
        if(l == null){
            l = new LinkedList<>();
            axiomRules.put(axiom, l);
        }
        l.add(r);
    }

    private void updateRuleMap(int key, int value){
        List<Integer> l = ruleMap.get(key);
        if(l == null){
            l = new LinkedList<>();
            ruleMap.put(key, l);
        }
        l.add(value);
    }

    public void addRule(OWLAxiom cause, Rule r){
        //skip if rule is already in the system
        if(!rules.contains(r)) {
            rules.add(r);
            updateAxiomMap(getId(cause), r);
            ruleHeads.add(r.getHeadOrAxiom());
            boolean axRule = r.getAxiom() != null;
            isAxiomRule.add(axRule);

            //update axiom signatures
            if(axRule){
                List<Integer> axiomSignature = new LinkedList<>();
                (getObject(r.getAxiom())).getSignature().forEach(x -> axiomSignature.add(getId(x)));
                axiomSignatures.put(r.getAxiom(), axiomSignature);
            }

            //extend base module if rule body is empty
            if (r.size() <= 0) {
                baseModule.add(r.getAxiom());
                ((OWLAxiom) getObject(r.getAxiom())).getSignature().forEach(x -> baseSignature.add(getId(x)));
            }
            else{
                //update rule map
                for(Integer i : r){
                    updateRuleMap(i, rules.size() - 1);
                }
            }
        }
    }

    public IncrementalModule extractModule(Set<OWLEntity> signature){

        IncrementalModule result = new IncrementalModule(this);

        Queue<Integer> procQueue = new LinkedList<>();
        signature.forEach(x -> addQueue(getId(x), result, procQueue));

        baseModule.forEach(x -> result.addActiveAxiom(x));
        //baseSignature.forEach(x -> addQueue(x, result, procQueue));
        boolean compute = true;
        while(compute) {
            for (Integer front = procQueue.poll(); front != null; front = procQueue.poll()) {
                List<Integer> matchRules = ruleMap.get(front);
                if (matchRules == null) continue;

                for (Integer cRule : matchRules) {
                    if (result.getRuleCounter(cRule) <= 0) {
                        int head = ruleHeads.get(cRule);
                        if (isAxiomRule.get(cRule)) {
                            result.addActiveAxiom(head);
                        } else {
                            addQueue(head, result, procQueue);
                        }
                    }
                }
            }
            compute = false;
            for(Integer a : result.getActiveAxioms()){
                if(result.addAxiom(a)){
                    axiomSignatures.get(a).forEach(x -> addQueue(x, result, procQueue));
                    compute = true;
                    break;
                }
            }
        }

        return result;
    }

    public IncrementalModule extractChangedModule(IncrementalModule old, OWLAxiom reset){
        //TODO: This is just a test implementation!
        old.resetToAxiom(getId(reset));

        Queue<Integer> procQueue = new LinkedList<>();
        old.getActiveAxioms().forEach(x -> axiomSignatures.get(x).forEach(y -> addQueue(y, old, procQueue)));

        boolean compute = true;
        while(compute) {
            for (Integer front = procQueue.poll(); front != null; front = procQueue.poll()) {
                List<Integer> matchRules = ruleMap.get(front);
                if (matchRules == null) continue;

                for (Integer cRule : matchRules) {
                    if (old.getRuleCounter(cRule) <= 0) {
                        int head = ruleHeads.get(cRule);
                        if (isAxiomRule.get(cRule)) {
                            old.addActiveAxiom(head);
                        } else {
                            addQueue(head, old, procQueue);
                        }
                    }
                }
            }
            compute = false;
            for(Integer a : old.getActiveAxioms()){
                if(old.addAxiom(a)){
                    axiomSignatures.get(a).forEach(x -> addQueue(x, old, procQueue));
                    compute = true;
                    break;
                }
            }
        }

        return old;
    }


    private void addQueue(Integer obj, IncrementalModule result, Queue<Integer> procQueue){
        if(obj != 0){
            if(!result.isKnown(obj)){
                result.setKnown(obj);
                procQueue.add(obj);
            }
        }
    }
}



