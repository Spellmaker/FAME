package de.uniulm.in.ki.mbrenner.fame.simple.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uniulm.in.ki.mbrenner.fame.simple.extractor.CompressedExtractor;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;

import org.semanticweb.owlapi.model.OWLObject;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * Stores compressed rules and organizes them efficiently
 */
public class CompressedRuleSet implements Iterable<CompressedRule>{
	private final Set<CompressedRule> rules;						//all rules
	private final Map<Integer, List<Integer>> map;				//maps integer entities to rule indices
	private Set<OWLAxiom> base;								//base module axioms

	private CompressedRule[] ruleArray;						//rules in array form
	private final Map<OWLObject, Integer> dictionary;				//maps entities to integer entities
	private final List<OWLObject> invDictionary;					//maps integer entities to entities
	private Map<OWLAxiom, Set<Integer>> axiomSignatures;	//maps axioms to integer entity signatures
	private boolean finalized = false;

	/**
	 * Constructs a new set
	 */
	public CompressedRuleSet(){
		rules = new HashSet<>();
		map = new HashMap<>();
		base = new HashSet<>();
		dictionary = new HashMap<>();
		invDictionary = new ArrayList<>();
		OWLDataFactory factory = new OWLDataFactoryImpl();
		dictionary.put(factory.getOWLThing(), 0);
		invDictionary.add(factory.getOWLThing());
	}

	/**
	 * Adds a new rule
	 * @param cr A compressed rule
     */
	public void addRule(CompressedRule cr){
		rules.add(cr);
	}

	/**
	 * Adds an axiom to the base module
	 * Axioms in the base module will be part of each extracted module
	 * @param ax An axiom
     */
	public void addBase(OWLAxiom ax){
		base.add(ax);
	}

	/**
	 * Finalizes the set by determining the full base module
	 */
	public void finalizeSet(){
		//invDictionary = new ArrayList<>();
		//dictionary = new HashMap<>();

		ruleArray = new CompressedRule[rules.size()];
		axiomSignatures = new HashMap<>();
		int i = 0;
		for(OWLAxiom ax : base){
			ax.getSignature().forEach(this::lookup);
			ax.getIndividualsInSignature().forEach(this::lookup);
		}
		for(CompressedRule cr : rules){
			ruleArray[i] = cr;
			for(OWLObject e : cr.body){
				List<Integer> l = map.get(lookup(e));
				if(l == null){
					l = new LinkedList<>();
					map.put(lookup(e), l);
				}
				l.add(i);
			}

			Set<Integer> sign = new HashSet<>();
			cr.head.getSignature().forEach(x -> sign.add(lookup(x)));
			cr.head.getIndividualsInSignature().forEach(x -> sign.add(lookup(x)));
			axiomSignatures.put(cr.head, sign);
			i++;
		}
		//System.out.println("cFame base module contains " + base.size() + " axioms: ");
		//base.forEach(x -> System.out.println(x));

		finalized = true;

		CompressedExtractor ce = new CompressedExtractor();
		base = ce.extractModule(this, new HashSet<>());
	}

	/**
	 * Looks up an object in the internal object to integer conversion table
	 * @param e An object
	 * @return The index of the element or null, if there is no index for the element
     */
	public Integer lookup(OWLObject e){
		Integer i = dictionary.get(e);
		if(i == null){
			if(finalized){
				System.out.println("Error: Rule set is finalized, cannot introduce element " + e);
			}
			else {
				i = invDictionary.size();
				dictionary.put(e, i);
				invDictionary.add(e);
			}
		}
		return i;
	}

	/**
	 * Looks up an object in the internal integer to object conversion table
	 * @param i An index
	 * @return The object with the given index or null, if there is no such object
     */
	public OWLObject lookup(Integer i){
		return invDictionary.get(i);
	}

	/**
	 * Provides access to the stored rules
	 * May throw an ArrayIndexOutOfBoundsException if the index is illegal
	 * @param i The index of the rule
	 * @return The rule with the given index
     */
	public CompressedRule getRule(int i){
		return ruleArray[i];
	}

	/**
	 * Counts the elements in the dictionary
	 * @return The number of object to integer mappings
     */
	public int dictionarySize(){
		return dictionary.size();
	}

	/**
	 * Counts the number of rules in the set
	 * @return The size of the rule set
     */
	public int ruleCount(){
		return rules.size();
	}

	@Override
	public Iterator<CompressedRule> iterator() {
		return rules.iterator();
	}

	/**
	 * Provides the precomputed signatures of an axiom as internal integer indices
	 * @param ax An axiom
	 * @return The signature of the axiom as internal integer indices
     */
	public Set<Integer> getSignature(OWLAxiom ax){
		return axiomSignatures.get(ax);
	}

	/**
	 * Provides the base module of this rule set
	 * @return The base module
	 */
	public Set<OWLAxiom> getBase(){
		return base;
	}

	/**
	 * Provides fast access to rules with a certain element in their body
	 * @param i An element index
	 * @return The indices of all rules, which have the element in their body
     */
	public List<Integer> findMatches(Integer i){
		return map.get(i);
	}

	/**
	 * Provides the number of rules in the set
	 * @return The number of rules in the set
     */
	public int size(){
		return rules.size();
	}
}
