package de.uniulm.in.ki.mbrenner.fame.genetic.RuleGenerator;

import de.uniulm.in.ki.mbrenner.fame.simple.rule.Rule;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 14.06.2016.
 */
public class Generator {
    AxiomGenerator axiomGenerator = new AxiomGenerator(this);
    ClassGenerator classGenerator = new ClassGenerator(this);
    PropertyGenerator propertyGenerator = new PropertyGenerator(this);

    boolean botMode = true;

    Map<OWLObject, Integer> objToInt;
    List<OWLObject> intToObj;

    Set<OWLAxiom> base;

    Map<OWLObject, Boolean> interpretedAsBot;

    Set<Rule> rules;
    Map<Integer, Set<Integer>> axiomSignatures;
    Map<Integer, Set<Rule>> objToRules;

    public Generator(){
        objToInt = new HashMap<>();
        intToObj = new ArrayList<>();
        base = new HashSet<>();
        rules = new HashSet<>();
        objToRules = new HashMap<>();
        axiomSignatures = new HashMap<>();
    }

    public int dictionarySize(){
        return intToObj.size();
    }

    public int get(OWLObject obj){
        Integer i = objToInt.get(obj);
        if(i == null){
            i = intToObj.size();
            objToInt.put(obj, i);
            intToObj.add(obj);
        }
        return i;
    }

    public OWLObject get(int i){
        return intToObj.get(i);
    }

    public void addRule(Rule r){
        if(!rules.contains(r)){
            rules.add(r);
            r.setId(rules.size() - 1);
            for(int o : r){
                Set<Rule> s = objToRules.get(o);
                if(s == null){
                    s = new HashSet<>();
                    objToRules.put(o, s);
                }
                s.add(r);
            }
            if(r.getAxiom() != null){
                axiomSignatures.put(r.getAxiom(), get(r.getAxiom()).getSignature().stream().map(this::get).collect(Collectors.toSet()));
            }
        }
    }

    public void buildRules(Set<OWLAxiom> axioms, Map<OWLObject, Boolean> interpretedAsBot){
        axioms.forEach(x -> x.getSignature().forEach(this::get));
        this.interpretedAsBot = interpretedAsBot;
        axioms.forEach(x -> x.accept(axiomGenerator).forEach(this::addRule));
    }

    public Set<OWLAxiom> getResult(Set<OWLEntity> signature){
        Set<OWLAxiom> module = new HashSet<>();
        boolean[] unknown = new boolean[intToObj.size()];
        int[] counter = new int[rules.size()];
        Queue<Integer> queue = new LinkedList<>();
        for(OWLEntity e : signature){
            int id = get(e);
            queue.add(id);
            unknown[id] = true;
        }

        for(OWLAxiom ax : base){
            ax.getSignature().stream().map(this::get).forEach(x ->{
                if(!unknown[x]){
                    unknown[x] = true;
                    queue.add(x);
                }
            });
            module.add(ax);
        }

        while(!queue.isEmpty()){
            int head = queue.poll();
            Set<Rule> affected = objToRules.get(head);
            if(affected == null) continue;

            for(Rule r : affected){
                if(++counter[r.getId()] == r.size()){
                    if(r.getHead() == null){
                        module.add((OWLAxiom) get(r.getAxiom()));
                        for(Integer i : axiomSignatures.get(r.getAxiom())){
                            if(!unknown[i]){
                                unknown[i] = true;
                                queue.add(i);
                            }
                        }
                    }
                    else if(!unknown[r.getHead()]){
                        unknown[r.getHead()] = true;
                        queue.add(r.getHead());
                    }
                }
            }
        }
        return module;
    }

    public Set<Rule> getRules(){
        return Collections.unmodifiableSet(rules);
    }


}

/*class Rule{
    final int[] body;
    final int head;
    final int axiom;
    int id;

    public Rule(int head, int axiom, int...body){
        this.body = Arrays.copyOf(body, body.length);
        this.head = head;
        this.axiom = axiom;
    }
}*/