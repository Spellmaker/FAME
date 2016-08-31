package de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule;

import de.uniulm.in.ki.mbrenner.fame.incremental.OWLDictionary;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents a set of DRBRules
 *
 * Internally manages data structures for fast access to the rules which are affected by the addition of a specific symbol
 *
 * Created by Spellmaker on 13.05.2016.
 */
public class IDRBRuleSet implements Iterable<IDRBRule>, OWLDictionary{
    private final Set<IDRBRule> rules;

    private final Map<Integer, Set<IDRBRule>> ruleMap;
    private final Set<IDRBRule> baseRules;

    private List<OWLObject> dictionary;
    private Map<OWLObject, Integer> invDictionary;

    private Map<Integer, Set<Integer>> signatures;

    private final Set<Integer> baseAxioms;
    private final Set<Integer> baseEntities;

    /**
     * Default constructor
     */
    public IDRBRuleSet(){
        this.rules = new HashSet<>();
        this.ruleMap = new HashMap<>();
        this.baseRules = new HashSet<>();
        this.dictionary = new ArrayList<>();
        this.invDictionary = new HashMap<>();
        this.signatures = new HashMap<>();
        this.baseAxioms = new HashSet<>();
        this.baseEntities = new HashSet<>();

        OWLDataFactory data = new OWLDataFactoryImpl();
        getId(data.getOWLThing());
        getId(data.getOWLTopObjectProperty());
    }

    /**
     * Adds a rule to the set
     *
     * No changes are done if the rule is already part of the set.
     * If the rule is new, it is also assign an id unique within this set.
     * Note that this id shouldn't be tampered with, although it could be done
     * @param rule A rule
     */
    public void addRule(@Nonnull IDRBRule rule){
        if(rule.body.isEmpty()){
            //base rule, always executed
            baseRules.add(rule);
            if(rule.head != null)
                baseEntities.add(rule.head);
            else
                baseAxioms.add(rule.axiom);
        }
        else if(rules.add(rule)){
            rule.id = rules.size() - 1;
            for(Integer o : rule){
                Set<IDRBRule> current = ruleMap.getOrDefault(o, new HashSet<>());
                current.add(rule);
                ruleMap.put(o, current);
            }
        }
    }

    public Set<Integer> getBaseAxioms(){
        return Collections.unmodifiableSet(baseAxioms);
    }

    public Set<Integer> getBaseEntities(){
        return Collections.unmodifiableSet(baseEntities);
    }

    /**
     * Finds all rules which have a specific object in their body
     * @param o The object in question
     * @return An iterator on a set of all rules in question
     */
    public Iterator<IDRBRule> rulesForObjects(@Nonnull Integer o){
        Set<IDRBRule> s = ruleMap.get(o);
        if(s == null) return Collections.emptyIterator();
        return s.iterator();
    }

    @Override
    public Iterator<IDRBRule> iterator() {
        return rules.iterator();
    }

    @Override
    public void forEach(Consumer<? super IDRBRule> action) {
        rules.forEach(action);
    }

    @Override
    public Spliterator<IDRBRule> spliterator() {
        return rules.spliterator();
    }

    /**
     * Returns the size of the set
     * @return The number of rules in this set
     */
    public int size(){
        return rules.size();
    }

    @Override
    public Integer getId(OWLObject o) {
        Integer id = invDictionary.get(o);
        if(id == null){
            id = dictionary.size();
            dictionary.add(o);
            invDictionary.put(o, id);
            if(o instanceof OWLAxiom){
                OWLAxiom ax = (OWLAxiom) o;
                Set<Integer> sig = ax.getSignature().stream().map(this::getId).collect(Collectors.toSet());
                signatures.put(id, sig);
            }
        }
        return id;
    }

    public Set<Integer> getSignature(Integer axiom){
        return signatures.get(axiom);
    }

    @Override
    public OWLObject getObject(Integer id) {
        return dictionary.get(id);
    }

    @Override
    public int dictionarySize() {
        return dictionary.size();
    }
}
