package de.uniulm.in.ki.mbrenner.fame.incremental.v3;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import de.uniulm.in.ki.mbrenner.fame.incremental.v2.OWLDictionary;
import de.uniulm.in.ki.mbrenner.fame.incremental.v2.RuleStorage;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.TreeBuilder;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.folder.IncrementalRuleFolder;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.folder.NormalRuleFolder;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.nodes.Node;
import de.uniulm.in.ki.mbrenner.fame.locality.SyntacticLocalityEvaluator;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.InvalidRule;
import de.uniulm.in.ki.mbrenner.fame.rule.Rule;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
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

    public boolean isInBaseSet(OWLAxiom a){
        return baseModule.contains(getId(a));
    }

    private IncrementalModule base;
    //rules
    //private List<Rule> rules;
    //private Set<Rule> ruleSet;
    private List<Rule> ruleList;
    private Map<Rule, Rule> ruleSetMap;
    //private List<Integer> ruleOccurences;
    //map of elements to rules affected by them
    private Map<Integer, List<Rule>> ruleMap;
    //rule heads
    //private List<Integer> ruleHeads;
    //marks rules which provide axioms
    //private List<Boolean> isAxiomRule;
    //maps axioms to their precomputed signatures
    private Map<Integer, List<Integer>> axiomSignatures;
    //dictionary
    private List<OWLObject> dictionary;
    private Map<OWLObject, Integer> invDictionary;
    private Map<Integer, List<Rule>> axiomRules;

    private int ruleCounter;

    private Map<Integer, IncrementalModule> moduleMap;
    private List<IncrementalModule> modules;

    public IncrementalExtractor(OWLOntology ontology){
        init(ontology.getAxiomCount());
        //TreeBuilder tb = new TreeBuilder();

        //List<Node> forest = tb.buildTree(ontology.getAxioms(Imports.INCLUDED));
        //NormalRuleFolder nrf = new NormalRuleFolder(this, this);
        //nrf.getRules(forest);
        BottomModeRuleBuilder bmrb = new BottomModeRuleBuilder();
        bmrb.buildRules(ontology, true, this, this);
        determineBaseModule();
    }

    public IncrementalExtractor(Set<OWLAxiom> ontology){
        init(ontology.size());
        //TreeBuilder tb = new TreeBuilder();

        //List<Node> forest = tb.buildTree(ontology);
        //NormalRuleFolder nrf = new NormalRuleFolder(this, this);
        //nrf.getRules(forest);

        BottomModeRuleBuilder bmrb = new BottomModeRuleBuilder();
        Set<OWLEntity> signature = new HashSet<>();
        ontology.forEach(x -> signature.addAll(x.getSignature()));
        bmrb.buildRules(ontology, signature, true, this, this);
        determineBaseModule();
    }

    private void init(int size){
        ruleCounter = 0;
        baseSignature = new HashSet<>(); baseModule = new HashSet<>();
        ruleSetMap = new HashMap<>();

        ruleMap = new HashMap<>(); axiomSignatures = new HashMap<>();
        invDictionary = new HashMap<>(); axiomRules = new HashMap<>(); moduleMap = new HashMap<>();

        //ruleHeads = new ArrayList<>(); isAxiomRule = new ArrayList<>();
        ruleList = new LinkedList<>();
        dictionary = new LinkedList<>(); //rules = new ArrayList<>(size); //ruleOccurences = new ArrayList<>();

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
    /*public Rule getRule(int r){
        return rules.get(r);
    }*/

    public void finalize(){

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

    public IncrementalModule getModule(OWLEntity e){
        if(e == null) return base;
        IncrementalModule res = moduleMap.get(getId(e));
        if(res != null) return res;
        return extractModule(e);
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
            result.setKnown(axiom);
            axiomSignatures.get(axiom).forEach(x -> addQueue(x, result, procQueue));
            result.addAxiom(axiom);
        }
    }

    public void determineBaseModule(){
        base = new IncrementalModule(this, null);
        Queue<Integer> procQueue = new LinkedList<>();
        baseModule.forEach(x -> addAxiomToModule(x, base, procQueue));
        //baseSignature.forEach(x -> addQueue(x, result, procQueue));
        baseSignature.forEach(x -> addQueue(x, base, procQueue));
        processQueue(procQueue, base);
        //baseModule.addAll(base.getModule());
        //baseModule.forEach(x -> baseSignature.addAll(axiomSignatures.get(x)));
    }

    public Set<Integer> getBaseModule(){
        return this.base.getModule();
    }

    public IncrementalModule extractModule(OWLEntity entity){
        if(entity == null) return extractModule((Integer) null);
        return extractModule(getId(entity));
    }

    private IncrementalModule extractModule(Integer entity){
        if(entity == null) return base;

        IncrementalModule result = base.getCopy(entity);
        Queue<Integer> procQueue = new LinkedList<>();
        moduleMap.put(entity, result);
        modules.add(result);
        addQueue(entity, result, procQueue);

        //baseModule.forEach(x -> addAxiomToModule(x, result, procQueue));
        //baseSignature.forEach(x -> addQueue(x, result, procQueue));
        processQueue(procQueue, result);

        return result;
    }

    /*public void addAxioms(Set<OWLAxiom> newAxioms){
        TreeBuilder tb = new TreeBuilder();
        List<Node> forest = tb.buildTree(newAxioms);
        IncrementalRuleFolder irf = new IncrementalRuleFolder(this, this, modules);
        irf.buildRules(forest);
        for(Map.Entry<Integer, List<Integer>> entry : irf.applyAxiomToModules.entrySet()){
            IncrementalModule current = modules.get(entry.getKey());
            Queue<Integer> procQueue = new LinkedList<>();
            for(Integer a : entry.getValue()){
                addAxiomToModule(a, current, procQueue);
                //axiomSignatures.get(a).forEach(x -> addQueue(x, current, procQueue));
                //current.module.add(a);
            }
            processQueue(procQueue, current);
        }
    }*/

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
            IncrementalModule im = moduleMap.get(i);
            modules.remove(im);
            extractModule(i);
        }
    }

    private int incrCases = 0;

    public int getIncrCases(){
        return incrCases;
    }

    public Set<IncrementalModule> modifyOntologyNaive(Set<OWLAxiom> addedAxioms, Set<OWLAxiom> removedAxioms){
        Set<Integer> affectedEntities = new HashSet<>();
        SyntacticLocalityEvaluator synt = new SyntacticLocalityEvaluator(LocalityClass.BOTTOM_BOTTOM);
        //transform to internal representation
        Set<Integer> iRemovedAxioms = removedAxioms.stream().map(x -> getId(x)).collect(Collectors.toSet());
        //determine first, if the base module is affected by a deletion. If that is the case, then all modules
        //need to be redetermined anyways.
        boolean baseModuleAffected = false;
        for(Integer i : iRemovedAxioms){
            if(base.getModule().contains(i)){
                baseModule.remove(i);
                baseModuleAffected = true;
            }
        }
        if(!baseModuleAffected){
            for(OWLAxiom a : addedAxioms){
                Set<OWLEntity> signature = new HashSet<>();
                base.getOWLModule().forEach(x -> signature.addAll(x.getSignature()));
                if (!synt.isLocal(a, signature)) {
                    baseModuleAffected = true;
                }
            }
        }
        //remove rules generated by the removed axioms
        removeRules(iRemovedAxioms);
        //prepare rules for the new axioms
        TreeBuilder tb = new TreeBuilder();
        List<Node> forest = tb.buildTree(addedAxioms);
        //create new rules without any fancy optimizations
        NormalRuleFolder nrf = new NormalRuleFolder(this, this);
        nrf.getRules(forest);

        if(baseModuleAffected){
            //redetermine base module and all other modules
            determineBaseModule();
            affectedEntities.addAll(moduleMap.keySet());
            Set<Integer> entities = new HashSet<>(moduleMap.keySet());
        }
        else{
            //get signature for all modules and check, if the entity is local w.r.t. that signature
            Set<IncrementalModule> recompute = new HashSet<>();

            for(OWLAxiom a : removedAxioms){
                Integer ia = getId(a);
                for(IncrementalModule im : modules){
                    if(recompute.contains(im)) continue;
                    if(im.getModule().contains(ia)){
                        recompute.add(im);
                        affectedEntities.add(im.getBaseEntity());
                    }
                }
            }

            for(OWLAxiom a : addedAxioms) {
                for (IncrementalModule im : modules) {
                    if(recompute.contains(im)) continue;
                    Set<OWLEntity> signature = new HashSet<>();
                    signature.add((OWLEntity) getObject(im.getBaseEntity()));
                    im.getOWLModule().forEach(x -> signature.addAll(x.getSignature()));
                    if(getObject(im.getBaseEntity()).toString().equals("<http://schema.org/Place>"))System.out.println("checking locality of " + a + " vs " + signature + ": " + synt.isLocal(a, signature));
                    if (!synt.isLocal(a, signature)){

                        recompute.add(im);
                        affectedEntities.add(im.getBaseEntity());
                    }
                }
            }
        }
        System.out.println("reextracting size: " + affectedEntities.size() + " base module affected: " + baseModuleAffected);
        reextractFromScratch(affectedEntities);
        return affectedEntities.stream().map(i -> moduleMap.get(i)).collect(Collectors.toSet());
    }

    public Set<IncrementalModule> modifyOntology(Set<OWLAxiom> addedAxioms, Set<OWLAxiom> removedAxioms){
        //transform to internal representation
        Set<Integer> iRemovedAxioms = removedAxioms.stream().map(x -> getId(x)).collect(Collectors.toSet());

        Set<IncrementalModule> allAffected = new HashSet<>();

        //determine first, if the base module is affected by a deletion. If that is the case, then all modules
        //need to be redetermined anyways.
        boolean baseModuleAffected = false;
        for(Integer i : iRemovedAxioms){
            if(base.getModule().contains(i)){
                baseModule.remove(i);
                baseModuleAffected = true;
            }
        }

        //remove rules generated by the removed axioms
        removeRules(iRemovedAxioms);
        //prepare rules for the new axioms
        TreeBuilder tb = new TreeBuilder();
        List<Node> forest = tb.buildTree(addedAxioms);

        if(baseModuleAffected){
            //create new rules without any fancy optimizations
            NormalRuleFolder nrf = new NormalRuleFolder(this, this);
            nrf.getRules(forest);
            //redetermine base module and all other modules
            determineBaseModule();
            Set<Integer> entities = new HashSet<>(moduleMap.keySet());
            reextractFromScratch(entities);
            allAffected.addAll(modules);
        }
        else{
            //find modules unaffected by the removed axioms
            List<IncrementalModule> reextract = new LinkedList<>();
            List<IncrementalModule> unaffected = new LinkedList<>();
            for(IncrementalModule im : modules){
                boolean found = false;
                for(Integer i : iRemovedAxioms){
                    if(im.getModule().contains(i)){
                        found = true;
                        break;
                    }
                }
                if(!found)  unaffected.add(im);
                else        reextract.add(im);
            }
            //base module is always unaffected
            unaffected.add(base);
            //build rules and determine modules affected by added axioms only
            IncrementalRuleFolder irf = new IncrementalRuleFolder(this, this, unaffected);
            irf.buildRules(forest);

            //incrementally extract modules affected by only additions
            for(Map.Entry<Integer, List<Integer>> entry : irf.applyAxiomToModules.entrySet()){
                incrCases++;
                IncrementalModule current = unaffected.get(entry.getKey());
                allAffected.add(current);
                Queue<Integer> procQueue = new LinkedList<>();
                for(Integer a : entry.getValue()){
                    current.addAxiom(a);
                    for(Integer e : axiomSignatures.get(a)){
                        addQueue(e, current, procQueue);
                    }
                }
                processQueue(procQueue, current);
            }

            //reextract modules affected by deletions
            allAffected.addAll(reextract);
            reextractFromScratch(reextract.stream().map(x -> x.getBaseEntity()).collect(Collectors.toSet()));
        }
        return allAffected;
    }
}
