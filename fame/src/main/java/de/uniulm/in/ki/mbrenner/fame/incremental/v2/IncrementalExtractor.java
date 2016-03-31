package de.uniulm.in.ki.mbrenner.fame.incremental.v2;

import de.uniulm.in.ki.mbrenner.fame.incremental.v2.rule.IncrementalRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.Rule;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;

/**
 * Created by spellmaker on 17.03.2016.
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

    private Map<Integer, IncrementalModule> moduleMap;
    private List<IncrementalModule> modules;

    private void init(){
        baseSignature = new HashSet<>(); baseModule = new HashSet<>();
        ruleMap = new HashMap<>(); axiomSignatures = new HashMap<>(); invDictionary = new HashMap<>(); axiomRules = new HashMap<>();
        ruleHeads = new ArrayList<>(); isAxiomRule = new ArrayList<>(); dictionary = new ArrayList<>(); rules = new ArrayList<>();
        moduleMap = new HashMap<>();
        modules = new LinkedList<>();

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

    public void determineBaseModule(){
        IncrementalModule base = extractModule(null);
        baseModule.addAll(base.module);
        baseModule.forEach(x -> baseSignature.addAll(axiomSignatures.get(x)));
    }

    public int addRule(OWLAxiom cause, Rule r){
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
            return rules.size() - 1;
        }
        else{
            return rules.indexOf(r);
        }
    }

    public IncrementalModule extractModule(OWLEntity entity){
        IncrementalModule result = new IncrementalModule(this);
        Queue<Integer> procQueue = new LinkedList<>();
        if(entity != null) {
            Integer entityId = getId(entity);
            moduleMap.put(entityId, result);
            modules.add(result);
            addQueue(entityId, result, procQueue);
        }

        baseModule.forEach(x -> result.module.add(x));
        baseSignature.forEach(x -> addQueue(x, result, procQueue));
        for (Integer front = procQueue.poll(); front != null; front = procQueue.poll()) {
            List<Integer> matchRules = ruleMap.get(front);
            if (matchRules == null) continue;

            for (Integer cRule : matchRules) {
                if (result.ruleCounter(cRule) <= 0) {
                    int head = ruleHeads.get(cRule);
                    if (isAxiomRule.get(cRule)) {
                        result.module.add(head);
                        axiomSignatures.get(head).forEach(x -> addQueue(x, result, procQueue));
                    } else {
                        addQueue(head, result, procQueue);
                    }
                }
            }
        }
        return result;
    }

    public List<IncrementalModule> getAllModules(){
        return new LinkedList<>(modules);
    }

    public void notifyChange(int module){
        //TODO implement
    }

    private void addQueue(Integer obj, IncrementalModule result, Queue<Integer> procQueue){
        if(obj != 0){
            if(!result.known(obj)){
                result.setKnown(obj);
                procQueue.add(obj);
            }
        }
    }
}
