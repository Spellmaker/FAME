package de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.folder;

import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalModule;
import de.uniulm.in.ki.mbrenner.fame.incremental.OWLDictionary;
import de.uniulm.in.ki.mbrenner.fame.incremental.RuleStorage;
import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes.*;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.Rule;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates Rules from a tree node structure while filtering out incremental modules affected by the new rules
 *
 * Created by spellmaker on 18.03.2016.
 */
public class IncrementalRuleFolder extends NodeFolder {
    private final Set<Rule> rules;
    private final OWLDictionary dictionary;
    private final RuleStorage storage;
    private final List<IncrementalModule> modules;
    private Set<Integer> moduleBuffer;
    /**
     * Stores for each module the list of axioms which need to be added to it, if there is at least one such axiom
     */
    public final Map<IncrementalModule, List<Integer>> applyAxiomToModules; //Module -> List<OWLAxiom>

    private Integer currentReason;

    private boolean known(int mod, OWLObject obj){
        return modules.get(mod).known(dictionary.getId(obj));
    }

    private void addToAxiomList(Integer ax, Integer mod){
        List<Integer> l = applyAxiomToModules.get(modules.get(mod));
        if(l == null){
            l = new LinkedList<>();
            applyAxiomToModules.put(modules.get(mod), l);
        }
        l.add(ax);
    }

    /**
     * Creates a new instance
     * @param dictionary A dictionary to manage conversion between indices and objects
     * @param storage A storage for the generated rules
     * @param modules A list of modules which shall be examined in the rule generation
     */
    public IncrementalRuleFolder(OWLDictionary dictionary, RuleStorage storage, List<IncrementalModule> modules){
        this.dictionary = dictionary;
        this.storage = storage;
        this.modules = modules;
        applyAxiomToModules = new HashMap<>();
        rules = new HashSet<>();
    }

    /**
     * Builds the new rules for the provided nodes
     * @param roots A collection of nodes for which rules need to be generated
     */
    public void buildRules(List<Node> roots){
        for(Node n : roots){
            rules.clear();
            currentReason = dictionary.getId(n.symbol);
            n.fold(this);
            for(Integer i : moduleBuffer){
                addToAxiomList(currentReason, i);
            }
        }
    }

    @Override
    public void fold(LeafNode node) {
        moduleBuffer = new HashSet<>();
        for(int i = 0; i < modules.size(); i++){
            if(known(i, node.symbol))
                moduleBuffer.add(i);
        }
    }

    @Override
    public void fold(AndNode node) {
        moduleBuffer = new HashSet<>();
        Set<Integer> all = new HashSet<>();
        for(Node n : node){
            n.fold(this);
            all.addAll(moduleBuffer);
        }
        moduleBuffer = makeAndApply(all, node.symbol, node.children.stream().map(x -> x.symbol).collect(Collectors.toList()));
    }

    @Override
    public void fold(OrNode node) {
        moduleBuffer = new HashSet<>();
        Set<Integer> all = new HashSet<>();
        for(Node n : node){
            n.fold(this);
            all.addAll(makeAndApply(moduleBuffer, node.symbol, Collections.singletonList(n.symbol)));
        }
        moduleBuffer = all;
    }

    @Override
    public void fold(AxiomOrNode node){
        moduleBuffer = new HashSet<>();
        Set<Integer> all = new HashSet<>();
        for(Node n : node){
            n.fold(this);
            all.addAll(moduleBuffer);
        }
        moduleBuffer = all;
    }

    @Override
    public void fold(FactNode node) {
        moduleBuffer = new HashSet<>();
        moduleBuffer = makeAndApply(node.symbol, null);
    }

    private Rule lastRule;

    /**
     * Create a rule and apply it to all modules
     * @param head The head of the rule, either an axiom or another entity
     * @param body The body of the rule
     * @return The set of modules on which the rule triggered
     */
    private Set<Integer> makeAndApply(OWLObject head, List<OWLObject> body){
        Set<Integer> res = new HashSet<>();
        int index = makeRule(head, body);
        for(int i = 0; i < modules.size(); i++){
            if(modules.get(i).applySingleRule(lastRule, index)){
                res.add(i);
            }
        }
        return res;
    }

    /**
     * Create a rule and apply it to a set of modules
     * @param m The set of modules the rule is applicable to
     * @param head The head of the rule, either an axiom or another entity
     * @param body The body of the rule
     * @return A subset of the provided modules, on which the rule triggered
     */
    private Set<Integer> makeAndApply(Set<Integer> m, OWLObject head, List<OWLObject> body){
        int index = makeRule(head, body);
        return m.stream().filter(i -> modules.get(i).applySingleRule(lastRule, index)).collect(Collectors.toSet());
    }

    private int makeRule(OWLObject head, List<OWLObject> body){
        if(body == null)
            return makeRuleInt(head, null);
        else
            return makeRuleInt(head, body.stream().map(dictionary::getId).collect(Collectors.toList()));
    }

    private int makeRuleInt(OWLObject head, List<Integer> body){
        if(head instanceof OWLAxiom)
            lastRule = new Rule(null, dictionary.getId(head), null, body);
        else
            lastRule = new Rule(dictionary.getId(head), null, null, body);

        if(rules.add(lastRule))
            return storage.addRule(currentReason, lastRule);
        else
            return storage.findRule(lastRule);
    }
}
