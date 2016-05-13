package de.uniulm.in.ki.mbrenner.fame.rule;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.incremental.OWLDictionary;
import de.uniulm.in.ki.mbrenner.fame.incremental.RuleStorage;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractor;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class RuleSet implements Iterable<Rule>, OWLDictionary, RuleStorage {
	protected Set<Integer> baseSignature;
	protected Set<OWLAxiom> baseModule;

	protected Map<Integer, List<Integer>> ruleMap;

	protected Map<Integer, List<Integer>> axiomSignatures;
	protected List<OWLObject> dictionary;
	protected List<Boolean> isDeclRule;
	protected Map<OWLObject, Integer> invDictionary;
	protected OWLObject[] arrDictionary;
	protected Boolean[] arrisDeclRule;
	protected int newCounter = 0;


	protected Set<Rule> rules;
	protected Rule[] rulesArray;
	protected int pos;
	
	private int size = -1;
	
	public RuleSet(){
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
		getId(factory.getOWLThing());
	}

	@Override
	public OWLObject getObject(Integer id){
		return arrDictionary[id];
	}
	
	public OWLObject debugLookup(int i){
		return dictionary.get(i);
	}
	
	public List<Integer> getAxiomSignature(int i){
		return axiomSignatures.get(i);
	}
	
	public boolean isDeclRule(int i){
		return arrisDeclRule[i];
	}

	@Override
	public Integer getId(OWLObject o){
		Integer index = invDictionary.get(o);
		if(index == null){
			if(arrDictionary != null){
				System.out.println("WARNING: Generating ID for previously unknown element " + o + " after finalization");
				return newCounter++;
				//throw new UnsupportedOperationException("RuleSet has already been finalized, cannot add object '" + o + "'");
			}
			//object is not known
			index = dictionary.size();
			dictionary.add(o);
			invDictionary.put(o, index);
			
			if(o instanceof OWLAxiom){
				List<Integer> sign = new LinkedList<>();
				OWLAxiom ax = (OWLAxiom) o;
				for(OWLObject obj : ax.getSignature()){
					sign.add(getId(obj));
				}
				axiomSignatures.put(index, Collections.unmodifiableList(sign));
				//ax.accept(this);
			}
		}
		return index;
	}

	public void finalize(boolean useNoDefExtractor, boolean useDefinitions){
		//run module extraction once with the base signature to determine the correct
		//base module and -signature
		size = rules.size();
		ruleMap = Collections.unmodifiableMap(ruleMap);

		rulesArray = new Rule[rules.size()];
		int cnt = 0;
		for(Rule r : rules){
			rulesArray[cnt++] = r;
		}
		rules = null;//Collections.unmodifiableSet(rules);

		arrDictionary = dictionary.toArray(new OWLObject[1]);
		newCounter = arrDictionary.length;
		arrisDeclRule = isDeclRule.toArray(new Boolean[1]);
		dictionary = Collections.unmodifiableList(dictionary);

		//baseSignature = new LinkedHashSet<>();
		Set<OWLEntity> sig = new HashSet<>();
		for(OWLAxiom ax : baseModule){
			sig.addAll(ax.getSignature());
		}
		if(useNoDefExtractor){
			RBMExtractorNoDef rbme = new RBMExtractorNoDef(false);
			baseModule = rbme.extractModule(this, sig);
		}
		else {
			RBMExtractor rbme = new RBMExtractor(useDefinitions, false);
			//baseModule.forEach(x -> sig.addAll(x.getSignature()));
			baseModule = rbme.extractModule(this, sig);
		}
		baseModule.forEach(x -> x.getSignature().forEach(y -> baseSignature.add(getId(y))));

		baseSignature = Collections.unmodifiableSet(baseSignature);
		baseModule = Collections.unmodifiableSet(baseModule);
	}

	public void finalize(boolean useNoDefExtractor){finalize(useNoDefExtractor, false);}

	public void finalizeSet(){
		finalize(false, false);
	}
	
	public Rule getRule(int i){
		/*Iterator<Rule> it = rules.iterator();
		Rule c = it.next();
		for(int j = 0; j < i; j++){
			c = it.next();
		}
		return c;*/
		return rulesArray[i];
	}
	
	public int addRule(Integer cause, Rule r){
		if(rules == null){
			throw new UnsupportedOperationException("RuleSet has already been finalized, cannot add rule '" + r + "'");
		}
		if(r.size() > 0){
			if(this.rules.add(r)){
				if(r.getAxiom() != null){
					isDeclRule.add(dictionary.get(r.getAxiom()) instanceof OWLDeclarationAxiom);
				}
				else isDeclRule.add(false);
				
				for(Integer o : r){
					List<Integer> current = ruleMap.get(o);
					if(current == null) current = new LinkedList<>();
					current.add(pos);
					ruleMap.put(o, current);
				}
				pos++;
			}
		}
		else{
			//TODO: Examine for correctness
			if(r.getAxiom() != null) {
				this.baseModule.add((OWLAxiom) dictionary.get(r.getAxiom()));
			}
			else{
				this.baseSignature.add(r.getHead());
				dictionary.get(r.getHead()).getSignature().forEach(x -> this.baseSignature.add(getId(x)));
			}
		}
		return -1;
	}
	
	/*@Override
	public void visit(OWLClassAssertionAxiom ax){
		//baseSignature.addAll(ax.getClassExpression().getSignature());
		//baseModule.add(ax);
		ax.getClassExpression().getSignature().forEach(x -> baseSignature.add(putObject(x)));
		baseSignature.add(putObject(ax.getIndividual()));
		putObject(ax);
		/*for(OWLEntity ent : ax.getClassExpression().getSignature()){
			if(!(ent instanceof OWLClass)) continue;
			
			//OWLAxiom declAxiom = new OWLDeclarationAxiomImpl(ent, Collections.emptyList());
			//putObject(declAxiom);
			//baseModule.add(declAxiom);
		}* /
		baseModule.add(ax);
	}
	
	@Override
	public void visit(OWLObjectPropertyAssertionAxiom ax){
		OWLObjectPropertyExpression prop = ax.getProperty();
		prop.getSignature().forEach(x -> baseSignature.add(putObject(x)));
		ax.getIndividualsInSignature().forEach(x -> baseSignature.add(putObject(x)));
		putObject(ax);
		prop.getSignature().stream().filter(x -> x instanceof OWLObjectProperty).forEach(x -> baseModule.add(new OWLDeclarationAxiomImpl(x, Collections.emptyList())));
		baseModule.add(ax);
		//baseSignature.addAll(prop.getSignature());
		//baseModule.add(ax);
	}*/

	public int findRule(Rule r){
		throw new NotImplementedException();
	}
	
	public Set<OWLAxiom> getBaseModule(){
		return baseModule;
	}
	
	public Set<Integer> getBaseSignature(){
		return baseSignature;
	}
	
	public int ruleCount(){
		return size;
	}
	
	public int dictionarySize(){
		return arrDictionary.length;
	}
	
	public List<Integer> findRules(Integer o){
		return ruleMap.get(o);
	}

	@Override
	public Iterator<Rule> iterator() {
		return new ArrayIterator<Rule>(rulesArray);
	}
}
