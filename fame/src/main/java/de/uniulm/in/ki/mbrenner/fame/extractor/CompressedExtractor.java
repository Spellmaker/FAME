package de.uniulm.in.ki.mbrenner.fame.extractor;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import de.uniulm.in.ki.mbrenner.fame.rule.CompressedRule;
import de.uniulm.in.ki.mbrenner.fame.rule.CompressedRuleSet;

public class CompressedExtractor {

	public Set<OWLAxiom> extractModule(CompressedRuleSet rules, Set<OWLEntity> signature){
		boolean[] known = new boolean[rules.dictionarySize()];
		
		int[] counters = new int[rules.ruleCount()];
		OWLAxiom[] heads = new OWLAxiom[rules.ruleCount()];
		
		
		Queue<Integer> queue = new LinkedList<>();
		
		Set<OWLAxiom> module = new HashSet<>();
		module.addAll(rules.getBase());
		signature.forEach(x -> addQueue(rules.lookup(x), queue, known));
		module.forEach(x -> x.getSignature().forEach(y -> addQueue(rules.lookup(y), queue, known)));
		
		int i = 0;
		for(CompressedRule cr : rules){
			counters[i] = cr.size();
			heads[i] = cr.getHead();
			i++;
		}

		addQueue(0, queue, known);
		while(!queue.isEmpty()){
			Integer e = queue.poll();
			
			List<Integer> matches = rules.findMatches(e);
			if(matches == null) continue;
			for(Integer m : matches){
				if(counters[m] == 0) continue;
				
				if(--counters[m] <= 0){
					module.add(heads[m]);
					for(Integer s : rules.getSignature(heads[m])){
						addQueue(s, queue, known);
					}
				}
			}
		}
		
		return module;
	}
	
	private void addQueue(Integer e, Queue<Integer> q, boolean[] known){
		if(!known[e]){
			q.add(e);
			known[e] = true;
		}
	}
}
