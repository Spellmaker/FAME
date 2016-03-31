package de.uniulm.in.ki.mbrenner.fame.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uniulm.in.ki.mbrenner.fame.extractor.CompressedExtractor;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;

import org.semanticweb.owlapi.model.OWLObject;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class CompressedRuleSet implements Iterable<CompressedRule>{
	private Set<CompressedRule> rules;						//all rules
	private Map<Integer, List<Integer>> map;				//maps integer entities to rule indices
	private Set<OWLAxiom> base;								//base module axioms
	
	private CompressedRule[] ruleArray;						//rules in array form
	private Map<OWLObject, Integer> dictionary;				//maps entities to integer entities
	private List<OWLObject> invDictionary;					//maps integer entities to entities
	private Map<OWLAxiom, Set<Integer>> axiomSignatures;	//maps axioms to integer entity signatures
	private boolean finalized = false;
	
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
	
	public void addRule(CompressedRule cr){
		rules.add(cr);
	}
	
	public void addBase(OWLAxiom ax){
		base.add(ax);
	}

	public void finalize(){
		//invDictionary = new ArrayList<>();
		//dictionary = new HashMap<>();
		
		ruleArray = new CompressedRule[rules.size()];
		axiomSignatures = new HashMap<>();
		int i = 0;
		for(OWLAxiom ax : base){
			for(OWLEntity e : ax.getSignature()){
				lookup(e);
			}
			for(OWLObject e : ax.getIndividualsInSignature()){
				lookup(e);
			}
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
		//System.out.println("cfame base module contains " + base.size() + " axioms: ");
		//base.forEach(x -> System.out.println(x));

		finalized = true;

		CompressedExtractor ce = new CompressedExtractor();
		base = ce.extractModule(this, new HashSet<>());
	}
	
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
	
	public OWLObject lookup(Integer i){
		return invDictionary.get(i);
	}
	
	public CompressedRule getRule(int i){
		int pos = 0;
		for(CompressedRule cr : rules){
			if(pos == i) return cr;
			pos++;
		}
		return null;
	}
	
	public int dictionarySize(){
		return dictionary.size();
	}

	public int ruleCount(){
		return rules.size();
	}

	@Override
	public Iterator<CompressedRule> iterator() {
		return rules.iterator();
	}

	public Set<Integer> getSignature(OWLAxiom ax){
		return axiomSignatures.get(ax);
	}

	public Set<OWLAxiom> getBase(){
		return base;
	}
	
	public List<Integer> findMatches(Integer i){
		return map.get(i);
	}
	
	public int size(){
		return rules.size();
	}
}
