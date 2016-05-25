package de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.folder;

import de.uniulm.in.ki.mbrenner.fame.incremental.OWLDictionary;
import de.uniulm.in.ki.mbrenner.fame.incremental.RuleStorage;
import de.uniulm.in.ki.mbrenner.fame.incremental.treebuilder.nodes.*;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.Rule;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.*;

/**
 * Folds a tree structure into module extraction rules
 * Currently unused, as using the normal RuleBuilder prooved sufficient
 *
 * Created by spellmaker on 18.03.2016.
 */
@Deprecated
public class NormalRuleFolder extends NodeFolder {
    private List<Rule> rules;
    private final OWLDictionary dictionary;
    private final RuleStorage storage;
    private OWLAxiom currentReason;
    /**
     * The last rule
     */
    public Rule last;

    /**
     * Constructs a new instance
     * @param dictionary A dictionary to manage the conversion of indices to objects and vice versa
     * @param storage A storage for the generated rules
     */
    public NormalRuleFolder(OWLDictionary dictionary, RuleStorage storage){
        this.dictionary = dictionary;
        this.storage = storage;
    }

    /**
     * Builds rules from the provided trees
     * @param roots A set of trees
     */
    public void buildRules(List<Node> roots){
        rules = new LinkedList<>();
        for(Node n : roots){
            Integer reason = dictionary.getId(n.symbol);
            rules.clear();
            n.fold(this);
            for(Rule r : rules){
                storage.addRule(reason, r);
            }
        }
    }

    @Override
    public void fold(LeafNode node) {
        //nothing to do
    }

    @Override
    public void fold(AndNode node) {
        node.children.forEach(x -> x.fold(this));
        Integer[] body = new Integer[node.children.size()];
        Iterator<Node> iter = node.iterator();
        for(int i = 0; i < node.children.size(); i++){
            body[i] = dictionary.getId(iter.next().symbol);
        }

        makeRuleInt(node.symbol, body);
    }

    @Override
    public void fold(OrNode node) {
        for(Node n : node.children){
            n.fold(this);
            makeRuleInt(node.symbol, dictionary.getId(n.symbol));
        }
    }

    @Override
    public void fold(AxiomOrNode node){
        for(Node n : node.children){
            n.fold(this);
        }
    }

    @Override
    public void fold(FactNode node) {
        makeRuleInt(node.symbol, (Integer[]) null);
    }

    /*private void makeRule(OWLObject head, OWLObject...body){
        makeRuleInt(head, null);
        //makeRuleInt(head, body.stream().map(x -> dictionary.getId(x)).collect(Collectors.toList()));
    }*/

    private void makeRuleInt(OWLObject head, Integer...body){
        if(head instanceof OWLAxiom)
            rules.add(new Rule(null, dictionary.getId(head), null, body));
        else
            rules.add(new Rule(dictionary.getId(head), null, null, body));
    }
}
