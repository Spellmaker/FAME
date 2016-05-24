package de.uniulm.in.ki.mbrenner.fame.debug.incremental.customextractor;

import de.uniulm.in.ki.mbrenner.fame.incremental.OWLDictionary;
import de.uniulm.in.ki.mbrenner.fame.incremental.RuleStorage;
import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.TreeBuilder;
import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.folder.IncrementalRuleFolder;
import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes.Node;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.Rule;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 18.03.2016.
 */
public class IncrementalExtractor implements RuleStorage, OWLDictionary {
    //base module signature and axioms
    private Set<Integer> baseSignature;
    private Set<Integer> baseModule;
    private IncrementalModule base;

    //rule management
    private List<Rule> ruleList;                            //list of all rules
    private Map<Rule, Rule> ruleSetMap;                     //TODO: Find out
    private Map<Integer, List<Rule>> ruleMap;               //map elements to rules affected by them
    private Map<Integer, List<Integer>> axiomSignatures;    //map axioms to their signatures
    private Map<Integer, List<Rule>> axiomRules;            //maps axioms to rules created by them
    private int ruleCounter;                                //number of rules
    //dictionary
    private List<OWLObject> dictionary;                     //maps Integers to OWLObjects
    private Map<OWLObject, Integer> invDictionary;          //maps OWLObjects to Integers

    //incremental management
    private Set<Integer> deletedAxioms;
    private boolean deletedFound;

    public IncrementalExtractor(OWLOntology ontology){
        init(ontology.getAxiomCount());
        //initialize rules
        BottomModeRuleBuilder bmrb = new BottomModeRuleBuilder();
        bmrb.buildRules(ontology, true, this, this);
        determineBaseModule();
    }

    public IncrementalExtractor(Set<OWLAxiom> ontology){
        init(ontology.size());
        BottomModeRuleBuilder bmrb = new BottomModeRuleBuilder();
        Set<OWLEntity> signature = new HashSet<>();
        ontology.forEach(x -> signature.addAll(x.getSignature()));
        bmrb.buildRules(ontology, signature, true, this, this);
        determineBaseModule();
    }

    private void init(int size){
        ruleCounter = 0;
        baseSignature = new HashSet<>(); baseModule = new HashSet<>(); deletedAxioms = new HashSet<>();
        ruleSetMap = new HashMap<>();
        ruleMap = new HashMap<>(); axiomSignatures = new HashMap<>();
        invDictionary = new HashMap<>(); axiomRules = new HashMap<>();
        ruleList = new LinkedList<>();
        dictionary = new LinkedList<>();
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

    private void updateRuleMap(int key, Rule value){
        List<Rule> l = ruleMap.get(key);
        if(l == null){
            l = new LinkedList<>();
            ruleMap.put(key, l);
        }
        l.add(value);
    }

    public int ruleCount(){
        return ruleCounter;
    }

    public int findRule(Rule r){
        return ruleSetMap.get(r).getId();
    }

    public int dictionarySize(){
        return dictionary.size();
    }

    public List<Rule> getRuleList(){
        return ruleList;
    }

    public void finalizeSet(){

    }

    public int addRule(Integer cause, Rule r){
        //skip if rule is already in the system
        if(!ruleSetMap.containsKey(r)) {
            ruleSetMap.put(r, r);
            ruleList.add(r);
            ruleCounter++;
            //rules.add(r);
            r.setId(ruleCounter - 1);
            r.occurences = 1;
            //ruleOccurences.add(1);

            updateAxiomMap(cause, r);
            //ruleHeads.add(r.getHeadOrAxiom());
            boolean axRule = r.getAxiom() != null;
            //isAxiomRule.add(axRule);

            //update axiom signatures
            if(axRule){
                List<Integer> axiomSignature = new LinkedList<>();
                (getObject(r.getAxiom())).getSignature().forEach(x -> axiomSignature.add(getId(x)));
                axiomSignatures.put(r.getAxiom(), axiomSignature);
            }

            //extend base module if rule body is empty
            if (r.size() <= 0) {
                if(r.getAxiom() != null) {
                    baseModule.add(r.getAxiom());
                    //((OWLAxiom) getObject(r.getAxiom())).getSignature().forEach(x -> baseSignature.add(getId(x)));
                }
                else{
                    baseSignature.add(r.getHead());
                }
            }
            else{
                //update rule map
                for(Integer i : r){
                    updateRuleMap(i, r);
                }
            }
            return ruleCounter - 1;
        }
        else{
            //increment counter
            Rule canonical = ruleSetMap.get(r);
            canonical.occurences++;
            return canonical.getId();
        }
    }

    private void processQueue(Queue<Integer> procQueue, IncrementalModule result){
        boolean found = false;
        for (Integer front = procQueue.poll(); front != null; front = procQueue.poll()) {
            List<Rule> matchRules = ruleMap.get(front);
            if (matchRules == null) continue;

            for (Rule cRule : matchRules) {
                if (result.ruleCounter(cRule.getId()) <= 0) continue;
                if (result.decCounter(cRule.getId()) <= 0) {
                    Integer head = cRule.getHead();
                    if(head != null){
                        addQueue(head, result, procQueue);
                    }
                    else{
                        addAxiomToModule(cRule.getAxiom(), result, procQueue);
                    }
                }
            }
        }
    }

    private void addQueue(Integer obj, IncrementalModule result, Queue<Integer> procQueue){
        if(obj != 0){
            if(!result.known(obj)){
                result.setKnown(obj);
                procQueue.add(obj);
            }
        }
    }

    private void addAxiomToModule(Integer axiom, IncrementalModule result, Queue<Integer> procQueue){
        if(!result.known(axiom)){
            if(!deletedFound && !deletedAxioms.contains(axiom)){
                result.setKnown(axiom);
                axiomSignatures.get(axiom).forEach(x -> addQueue(x, result, procQueue));
                result.addAxiom(axiom);
            }
            else{
                deletedFound = true;
            }
        }
    }

    public void determineBaseModule(){
        base = new IncrementalModule(this, null);
        Queue<Integer> procQueue = new LinkedList<>();
        baseModule.forEach(x -> addAxiomToModule(x, base, procQueue));
        baseSignature.forEach(x -> addQueue(x, base, procQueue));
        processQueue(procQueue, base);
    }

    public Set<Integer> getBaseModule(){
        return this.base.getModule();
    }

    public IncrementalModule extractModule(OWLEntity entity){
        if(entity == null) return extractModule((Integer) null);
        return extractModule(getId(entity));
    }

    public IncrementalModule extractModuleStatic(Set<OWLEntity> entity){
        if(entity == null) return extractModule((Integer) null);
        Integer[] e = new Integer[entity.size()];
        int pos = 0;
        for(OWLEntity x : entity) e[pos++] = getId(x);
        return extractModule(e);
    }

    private IncrementalModule extractModule(Integer...entity){
        if(entity == null) return base;

        IncrementalModule result = base.getCopy(entity[0]);
        Queue<Integer> procQueue = new LinkedList<>();
        for(Integer i : entity) {
            addQueue(i, result, procQueue);
        }
        processQueue(procQueue, result);

        return result;
    }

    private void removeRules(Set<Integer> removedAxioms){
        for(Integer i : removedAxioms){
            List<Rule> axRules = axiomRules.get(i);
            if(axRules == null) continue; //actually this should never happen

            for(Rule rule : axRules){
                rule.occurences -= 1;
                if(rule.occurences > 0) continue;
                ruleSetMap.remove(rule);
                //rules.set(rule, new InvalidRule());
                for(Integer b : rule){
                    ruleMap.get(b).remove(rule);
                }
            }
            axiomSignatures.remove(i);
            axiomRules.remove(i);
        }
    }

    private void reextractFromScratch(Set<Integer> entities){
        for(Integer i : entities){
            extractModule(i);
        }
    }

    public ModificationResult modifyOntology(Collection<OWLAxiom> addedAxioms, Collection<OWLAxiom> removedAxioms, Collection<OWLClass> classesInSignature){
        Set<OWLClass> delAffected = new HashSet<>();
        Set<OWLClass> addAffected = new HashSet<>();

        //add deleted axioms to no-go list
        deletedAxioms = removedAxioms.stream().map(x -> getId(x)).collect(Collectors.toSet());
        //make new rules
        TreeBuilder tb = new TreeBuilder();
        List<Node> forest = tb.buildTree(addedAxioms);
        IncrementalRuleFolder nrf = new IncrementalRuleFolder(this, this, Collections.emptyList());
        nrf.buildRules(forest);
        //reextract all modules
        for(OWLClass c : classesInSignature){
            deletedFound = false;
            IncrementalModule im = extractModule(getId(c));
            if(deletedFound){
                delAffected.add(c);
            }
            boolean found = false;
            for(OWLAxiom ax : addedAxioms){
                if(im.getModule().contains(getId(ax))){
                    found = true;
                    break;
                }
            }
            if(found) addAffected.add(c);
        }
        //clean up rules
        removeRules(removedAxioms.stream().map(x -> getId(x)).collect(Collectors.toSet()));

        return new ModificationResult(addAffected, delAffected);
    }
}
