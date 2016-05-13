package de.uniulm.in.ki.mbrenner.fame.debug;

import de.uniulm.in.ki.mbrenner.fame.OntologiePaths;
import de.uniulm.in.ki.mbrenner.fame.incremental.OWLDictionary;
import de.uniulm.in.ki.mbrenner.fame.incremental.RuleStorage;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.Rule;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 22.04.2016.
 */
public class CandidateFinder implements OWLDictionary, RuleStorage{
    int exact_depth = 2;
    private List<OWLObject> intToObj;
    private Map<OWLObject, Integer> objToInt;
    private Set<Rule> rules;


    private Set<Integer> identifyCandidatesApproximated(Integer symbol, Set<Rule> rules){
        Set<Integer> result = new HashSet<>();
        for(Rule r : rules){
            if(r.getHead() == null && ((OWLAxiom) intToObj.get(r.getAxiom())).getSignature().contains(intToObj.get(symbol))){
                result.add(r.getAxiom());
            }
            else if(r.getHead() != null && r.getHead().equals(symbol)){
                result.add(r.getHead());
            }
        }
        return result;
    }

    private boolean isInRuleAxiom(Rule r, Integer symbol){
        if(r.getAxiom() == null) return false;
        OWLAxiom ax = (OWLAxiom) getObject(r.getAxiom());
        OWLObject sym = getObject(symbol);
        return ax.getSignature().contains(sym);
    }

    private Set<Integer> identifyCandidatesExact(Set<Rule> rules, Integer symbol, int current_depth){
        if(current_depth > exact_depth){
            return identifyCandidatesApproximated(symbol, rules);
        }

        Set<Integer> candidates = new HashSet<>();
        System.out.println("iterate");
        for(Rule r : rules){
            if(r.getHeadOrAxiom().equals(symbol) || isInRuleAxiom(r, symbol)){
                List<Set<Integer>> candidateList = new LinkedList<>();
                for(Integer i : r){
                    candidateList.add(identifyCandidatesExact(rules, i, current_depth + 1));
                }

                for(Set<Integer> s : candidateList){
                    for(Integer i : s){
                        boolean missing = false;
                        for(Set<Integer> o : candidateList){
                            if(!o.contains(i)){
                                missing = true;
                                break;
                            }
                        }
                        if(!missing){
                            candidates.add(i);
                        }
                    }
                }
            }
        }
        return candidates;
    }

    public List<Integer> findCandidates(OWLOntology ontology, OWLAxiom axiom) throws OWLOntologyCreationException {
        /*BottomModeRuleBuilder ruleBuilder = new BottomModeRuleBuilder();
        intToObj = new ArrayList<>();
        objToInt = new HashMap<>();
        rules = new HashSet<>();
        /*Set<OWLAxiom> axioms = new HashSet<>(ontology.getAxioms().stream().filter(x -> !x.equals(axiom)).collect(Collectors.toSet()));
        ruleBuilder.buildRules(axioms, ontology.getSignature(), true, this, this);
        //at this point, we have all rules except those for the new axiom
        * /
        ruleBuilder.buildRules(ontology.getAxioms(), ontology.getSignature(), true, this, this);
        Set<Integer> pred = new HashSet<>();

        addPredecessors(rules, getId(axiom), pred, 0);
        System.out.println("found " + pred.size() + " exact predecessors");
        Set<Integer> p = new HashSet<>();
        for(Integer i : pred) {
            p.addAll(getPredecessors(i, rules));
        }
        */
        return null;//new ArrayList<>(p);
    }

    @Override
    public Integer getId(OWLObject o) {
        Integer i = objToInt.get(o);
        if(i == null){
            i = intToObj.size();
            intToObj.add(o);
            objToInt.put(o, i);
        }
        return i;
    }

    @Override
    public OWLObject getObject(Integer id) {
        return intToObj.get(id);
    }

    @Override
    public int dictionarySize() {
        return intToObj.size();
    }

    @Override
    public int addRule(Integer cause, Rule r) {
        rules.add(r);
        return rules.size();
    }

    @Override
    public int ruleCount() {
        return rules.size();
    }

    @Override
    public int findRule(Rule r) {
        return 0;
    }

    @Override
    public void finalizeSet(){

    }
}
