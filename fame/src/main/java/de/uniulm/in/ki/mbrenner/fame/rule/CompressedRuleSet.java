package de.uniulm.in.ki.mbrenner.fame.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class CompressedRuleSet implements Iterable<CompressedRule>{
	private Set<CompressedRule> rules;
	private Map<Integer, List<Integer>> map;
	private Set<OWLAxiom> base;
	
	private CompressedRule[] ruleArray;
	private Map<OWLEntity, Integer> dictionary;
	private List<OWLEntity> invDictionary;
	private Map<OWLAxiom, Set<Integer>> axiomSignatures;
	
	public CompressedRuleSet(){
		rules = new HashSet<>();
		map = new HashMap<>();
		base = new HashSet<>();
	}
	
	public void addRule(CompressedRule cr){
		rules.add(cr);
	}
	
	public void addBase(OWLAxiom ax){
		base.add(ax);
	}
	
	public void finalize(){
		invDictionary = new ArrayList<>();
		dictionary = new HashMap<>();
		OWLDataFactory factory = new OWLDataFactoryImpl();
		dictionary.put(factory.getOWLThing(), 0);
		invDictionary.add(factory.getOWLThing());
		
		ruleArray = new CompressedRule[rules.size()];
		axiomSignatures = new HashMap<>();
		int i = 0;
		for(CompressedRule cr : rules){
			ruleArray[i] = cr;
			
			
			for(OWLEntity e : cr.body){
				List<Integer> l = map.get(lookup(e));
				if(l == null){
					l = new LinkedList<>();
					map.put(lookup(e), l);
				}
				l.add(i);
			}

			Set<Integer> sign = new HashSet<>();
			cr.head.getSignature().forEach(x -> sign.add(lookup(x)));
			axiomSignatures.put(cr.head, sign);
			i++;
		}
	}
	
	public Integer lookup(OWLEntity e){
		Integer i = dictionary.get(e);
		if(i == null){
			i = invDictionary.size();
			dictionary.put(e, i);
			invDictionary.add(e);
		}
		return i;
	}
	
	public OWLEntity lookup(Integer i){
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
		return invDictionary.size();
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
