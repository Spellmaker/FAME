package de.uniulm.in.ki.mbrenner.fame.incremental;

import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import org.semanticweb.owlapi.model.OWLDataFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * Extension of the normal RuleSet
 * As opposed to the normal RuleSet, which can only be finalized once and does not allow modifications
 * afterwards, the IncrementalRuleSet allows arbitrary many finalizations and modifications.
 * TODO: Finish implementation
 * Created by spellmaker on 11.03.2016.
 */
public class IncrementalRuleSet extends RuleSet {
    public IncrementalRuleSet(){
        this.ruleMap = new HashMap<>();
        this.rules = new LinkedHashSet<>();
        this.baseModule = new LinkedHashSet<>();
        this.baseSignature = new LinkedHashSet<>();
        this.isDeclRule = new LinkedList<>();
        this.pos = 0;
        dictionary = new LinkedList<>();
        invDictionary = new HashMap<>();
        axiomSignatures = new HashMap<>();
        arrDictionary = null;

        //the rule set always knows owl:thing
        OWLDataFactory factory = new OWLDataFactoryImpl();
        putObject(factory.getOWLThing());
    }

    @Override
    public void finalize(){

    }
}
