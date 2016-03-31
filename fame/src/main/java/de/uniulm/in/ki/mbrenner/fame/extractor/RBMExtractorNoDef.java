package de.uniulm.in.ki.mbrenner.fame.extractor;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import de.uniulm.in.ki.mbrenner.fame.rule.Rule;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import de.uniulm.in.ki.mbrenner.fame.util.ClassPrinter;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * Extracts a local module from an ontology
 * @author spellmaker
 *
 */
public class RBMExtractorNoDef {
	private Set<Integer> module;
	private Set<OWLAxiom> finalModule;
	private boolean[] knownNotBottom;//Set<Integer> knownNotBottom;
	private Queue<Integer> queue;
	private Integer owlThing;
	private boolean debug = false;
	private Integer[] ruleAxioms;

	private Set<OWLAxiom> base;

	public RBMExtractorNoDef(boolean debug){
		this.debug = debug;
	}

	public void setBase(Set<OWLAxiom> base){
		this.base = base;
	}
	
	/**
	 * Uses the rules provided by the rule set to extract a module using the given signature
	 * @param rules A set of rules constructed by a RuleBuilder
	 * @param signature A set of OWL classes forming a signature
	 * @return A set of OWL axioms forming a module for the signature
	 */
	public Set<OWLAxiom> extractModule(RuleSet rules, Set<OWLEntity> signature){
		if(debug) System.out.println("> Extraction start");
		//initialize the processing queue to the signature
		module = new HashSet<>();
		finalModule = new HashSet<>();
		knownNotBottom = new boolean[rules.dictionarySize()];
		//TODO: Make this safe against inclusions of owl top and unknown vocabulary
		signature.forEach(x -> knownNotBottom[rules.putObject(x)] = true);
		
		//Note: this filter can be dropped, if we assume that signatures do not contain owl:thing
		signature = signature.stream().filter(x -> (!(x instanceof OWLClass)) || !((OWLClass)x).isOWLThing()).collect(Collectors.toSet());
		
		//TODO: Verify if this line is needed or not; it should not be needed, as all elements in the signature have been defined already
		//signature.forEach(x -> module.add(new OWLDeclarationAxiomImpl(x, Collections.emptyList())));
		queue = new LinkedList<>();
		signature.forEach(x -> queue.add(rules.putObject(x)));
		//System.out.println("queue now contains " + queue.size() + " elements");
		
		//OWL Thing is always assumed to be not bottom
		OWLDataFactory factory = new OWLDataFactoryImpl();
		owlThing = rules.putObject(factory.getOWLThing());
		//queue.add(owlThing);
		//System.out.println("queue now contains " + queue.size() + " elements");
		//knownNotBottom[owlThing] = true;//.add(owlThing);

		int[] ruleCounter = new int[rules.size()]; 						//counter for the number of elements in the rule body
		Integer[] ruleHeads = new Integer[rules.size()]; 				//rule heads of intermediary rules
		ruleAxioms = new Integer[rules.size()]; 						//axioms of leaf rules
		
		//add base module and signature
		finalModule.addAll(rules.getBaseModule());
		rules.getBaseSignature().forEach(x -> addQueue(x));
		//System.out.println("queue now contains " + queue.size() + " elements");
		//add predefined base if available
		if(base != null){
			finalModule.addAll(base);
			for(OWLAxiom a : base){
				a.getSignature().forEach(x -> addQueue(rules.putObject(x)));
			}
		}
		
		int pos = 0;
		for(Rule rule : rules){
			ruleCounter[pos] = rule.size();
			ruleHeads[pos] = rule.getHead();
			ruleAxioms[pos] = rule.getAxiom();
			pos++;
		}
		
		//main processing loop
		for(Integer front = queue.poll(); front != null; front = queue.poll()){
			//System.out.println("looking at item " + rules.lookup(front));
			//process all rules, which have the front element in their body
			List<Integer> matchRules = rules.findRules(front);
			//System.out.println("found " + ((matchRules != null) ? matchRules.size() : -1) + " matching rules");
			if(matchRules == null) continue;
			
			for(Integer cRule : matchRules){
				//System.out.println("processing rule " + cRule + " (" + rules.getRule(cRule) + ")");
				if(ruleCounter[cRule] <= 0) continue; //rule has already been processed
				
				//check for rule completion, that is, if all body elements 
				//have been found to be possibly not bottom
				if(--ruleCounter[cRule] <= 0){
					Integer head = ruleHeads[cRule];
					
					//if there is no head, then there must be an axiom
					if(head == null){
						Integer currentAxiom = ruleAxioms[cRule];
						//skip, if the axiom is already in the module
						//in case the head is an axiom, add all new vocabulary from the axiom
						//into the processing queue
						rules.getAxiomSignature(ruleAxioms[cRule]).forEach(x -> addQueue(x));
						module.add(ruleAxioms[cRule]);
						knownNotBottom[ruleAxioms[cRule]] = true;
						if(debug) System.out.println("added axiom " + rules.lookup(currentAxiom));//ClassPrinter.printAxiom((OWLAxiom) rules.lookup(currentAxiom)));
					}
					else{
						/*if(head instanceof OWLAxiom){
							System.out.println("this is awkward...");
						}*/
						//in case of an intermediate rule, add the head
						addQueue(head);
					}
				}
			}
			//System.out.println("--------------------------------------");
		}
		
		module.forEach(x -> finalModule.add((OWLAxiom) rules.lookup(x)));
		
		
		return finalModule;
	}
	
	private boolean addQueue(Integer o){
		if(o == owlThing){
			//handle owl:thing as a special case
			return false;
		}
		//add the entity to the list of those known to be possibly not bottom
		if(knownNotBottom[o] == false){
			knownNotBottom[o] = true;
			//check if the entity was previously considered defined. If so, add the appropriate axiom to the module
			/*if(definitions[o] != null){
				module.add(definitions[o]);
				knownNotBottom[definitions[o]] = true;
				rules.getAxiomSignature(definitions[o]).forEach(x -> addQueue(x));
				definitions[o] = null;
			}*/
			
			//add the entity to the processing queue
			//System.out.println("adding element " + rules.lookup(o));
			return queue.add(o);
		}
		return false;
	}
}
