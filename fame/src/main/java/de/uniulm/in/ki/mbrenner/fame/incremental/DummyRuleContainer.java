package de.uniulm.in.ki.mbrenner.fame.incremental;

import de.uniulm.in.ki.mbrenner.fame.incremental.OWLDictionary;
import de.uniulm.in.ki.mbrenner.fame.incremental.RuleStorage;
import de.uniulm.in.ki.mbrenner.fame.rule.Rule;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by spellmaker on 11.04.2016.
 */
public class DummyRuleContainer implements OWLDictionary, RuleStorage {
    private Map<OWLObject, Integer> objects;
    private List<OWLObject> reverseObjects;

    public DummyRuleContainer(){
        objects = new HashMap<>();
        reverseObjects = new ArrayList<>();
    }

    @Override
    public Integer getId(OWLObject o) {
        Integer i = objects.get(o);
        if(i == null){
            reverseObjects.add(o);
            i = reverseObjects.size() - 1;
            objects.put(o, i);
        }
        return i;
    }

    @Override
    public OWLObject getObject(Integer id) {
        return reverseObjects.get(id);
    }

    @Override
    public int dictionarySize() {
        return reverseObjects.size();
    }

    @Override
    public int addRule(Integer cause, Rule r) {
        return 0;
    }

    @Override
    public int ruleCount() {
        return 0;
    }

    @Override
    public int findRule(Rule r) {
        return 0;
    }

    public void finalizeSet(){

    }
}
