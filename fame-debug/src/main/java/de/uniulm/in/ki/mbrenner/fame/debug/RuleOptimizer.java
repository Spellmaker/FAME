package de.uniulm.in.ki.mbrenner.fame.debug;

import de.uniulm.in.ki.mbrenner.fame.simple.rule.Rule;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.*;

/**
 * Created by spellmaker on 02.05.2016.
 */
public class RuleOptimizer {
    public static RuleSet optimizeRules(RuleSet source){
        RuleSet result = new RuleSet();
        for(int i = 0; i < source.dictionarySize(); i++){
            result.getId(source.getObject(i));
        }
        for(OWLAxiom ax : source.getBaseModule()){
            result.addRule(-1, new Rule(null, source.getId(ax), null, (Integer[])null));
        }


        List<Rule> rules = new ArrayList<>();

        Map<Integer, Set<Rule>> headToRules = new HashMap<>();
        Map<Integer, Set<Rule>> bodyToRules = new HashMap<>();

        for(Rule r : source){
            rules.add(r);

            Integer head = r.getHeadOrAxiom();
            Set<Rule> tmp = headToRules.get(head);
            if(tmp == null){
                tmp = new HashSet<>();
                headToRules.put(head, tmp);
            }
            tmp.add(r);
            for(Integer b : r) {
                tmp = bodyToRules.get(b);
                if (tmp == null) {
                    tmp = new HashSet<>();
                    bodyToRules.put(b, tmp);
                }
                tmp.add(r);
            }

            //debug check: find if there is a null element in the rule
            for(Integer el : r){
                if(el == null){
                    System.out.println("found null rule: " + r);
                    System.out.println(r.toDebugString(source));
                    System.exit(0);
                }
            }
        }
        System.out.println("preparations done");

        boolean changed = true;
        while(changed){
            changed = false;

            for(int i = 0; i < rules.size(); i++){
                Rule current = rules.get(i);

                Set<Rule> sameHead = headToRules.get(current.getHeadOrAxiom());
                Set<Rule> inBody = bodyToRules.get(current.getHeadOrAxiom());

                if(sameHead != null && sameHead.size() >= 2) continue;
                if(inBody == null) continue;
                rules.remove(current);
                changed = true;

                //element won't exist afterwards anymore
                bodyToRules.remove(inBody);
                headToRules.remove(sameHead);

                for(Rule other : inBody){
                    rules.remove(other);
                    List<Integer> nBody = new LinkedList<>();

                    for(Integer el : other){
                        if(el.equals(current.getHeadOrAxiom())) continue;
                        nBody.add(el);
                    }
                    for(Integer el : current){
                        nBody.add(el);
                    }
                    for(Integer el : nBody){
                        if(el == null){
                            System.out.println("other body: " + other.size());
                            System.out.println("my body: " + current.size());
                            System.out.println("other: " + other);
                            System.out.println("me: " + current);
                            System.out.println("adding null");
                            System.exit(0);
                        }
                    }
                    Rule nRule = new Rule(other.getHead(), other.getAxiom(), other.getDefinition(), nBody);
                    rules.add(nRule);
                    for(Integer b : current){
                        Set<Rule> s = bodyToRules.get(b);
                        s.remove(current);
                        s.add(nRule);
                    }
                    Set<Rule> s = headToRules.get(other.getHeadOrAxiom());
                    s.remove(other);
                    s.add(nRule);
                }
            }
        }

        for(Rule r : rules){
            result.addRule(-1, r);
        }
        result.finalizeSet();
        return result;
    }
}
