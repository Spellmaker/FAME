package de.uniulm.in.ki.mbrenner.fame.incremental;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by spellmaker on 16.03.2016.
 */
public class IncrementalModule implements Iterable<Integer>{
    List<Integer> axiomQueuePosition;
    //Map<Integer, Integer> axiomQueuePosition;
    IncrementalExtractor extractor;
    Set<Integer> module;

    DropList<List<Integer>> ruleCounter;    //to restore appropriate rule counters
    DropList<List<Boolean>> known;          //to restore appropriate known conditions
    DropList<Integer> axiomOrder;           //to remove axioms from the module
    DropList<List<Integer>> activeAxioms;   //to restore the queue

    public IncrementalModule(IncrementalExtractor extractor){
        this.extractor = extractor;


        module = new HashSet<>();
        ruleCounter = new DropList<>();
        known = new DropList<>();

        List<Integer> rc = new ArrayList<>(extractor.ruleCount());
        for(int i = 0; i < extractor.ruleCount(); i++) rc.add(0);
        List<Boolean> k = new ArrayList<>(extractor.dictionarySize());
        axiomQueuePosition = new ArrayList<>(extractor.dictionarySize());//new HashMap<>();
        for(int i = 0; i < extractor.dictionarySize(); i++){
            k.add(false);
            axiomQueuePosition.add(null);
        }

        axiomOrder = new DropList<>();
        activeAxioms = new DropList<>();
        activeAxioms.add(new LinkedList<>());
        ruleCounter.add(rc);
        known.add(k);
    }

    public int getRuleCounter(Integer i){
        return ruleCounter.getLast().get(i);
    }

    public List<Integer> getActiveAxioms(){
        return activeAxioms.getLast();
    }

    public void addActiveAxiom(Integer axiom){
        activeAxioms.getLast().add(axiom);
    }

    public void incRuleCounter(Integer i){
        int cval = ruleCounter.getLast().get(i);
        ruleCounter.getLast().set(i, cval+1);
    }

    public boolean isKnown(Integer o){
        return known.getLast().get(o);
    }

    public void setKnown(Integer o){
        known.getLast().set(o, true);
    }

    public boolean addAxiom(int axiom){
        if(module.contains(axiom)) return false;
        List<Integer> rc = new ArrayList<>(ruleCounter.getLast());
        List<Boolean> k = new ArrayList<>(known.getLast());
        axiomOrder.add(axiom);
        ruleCounter.add(rc);
        known.add(k);

        int pos = ruleCounter.size() - 1;
        axiomQueuePosition.set(axiom, pos);
        module.add(axiom);
        activeAxioms.add(new LinkedList<>(activeAxioms.getLast()));
        activeAxioms.getLast().remove((Object) axiom);
        return true;
    }

    public Set<OWLAxiom> getOWLModule(){
        Set<OWLAxiom> result = new HashSet<>();
        module.forEach(x -> result.add((OWLAxiom) extractor.getObject(x)));
        return result;
    }

    public boolean resetToAxiom(Integer axiom){
        Integer queuePos = axiomQueuePosition.get(axiom);
        if(queuePos == null) return false;

        ruleCounter.dropAfterPosition(queuePos);
        known.dropAfterPosition(queuePos);
        activeAxioms.dropAfterPosition(queuePos);
        if(activeAxioms.getLast() == null){
            activeAxioms.add(new LinkedList<>());
        }
        //TODO: Fix this line with multiple axiom deletions in mind, because then it won't work
        //activeAxioms.getLast().remove(axiom);

        DropList<Integer> tail = axiomOrder.dropAfter(axiom);
        if(tail != null) {
            for (Integer i : tail) {
                module.remove(i);
            }
        }
        return true;
    }

    public int size(){
        return module.size();
    }

    @Override
    public Iterator<Integer> iterator() {
        return module.iterator();
    }

    @Override
    public void forEach(Consumer<? super Integer> action) {
        module.forEach(action);
    }
}