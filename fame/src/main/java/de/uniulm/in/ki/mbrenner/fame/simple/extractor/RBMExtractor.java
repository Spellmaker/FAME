package de.uniulm.in.ki.mbrenner.fame.simple.extractor;
import java.util.*;
import java.util.stream.Collectors;

import de.uniulm.in.ki.mbrenner.fame.util.printer.OWLPrinter;
import org.semanticweb.owlapi.model.*;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.Rule;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * Extracts a local module from an ontology
 * @author spellmaker
 *
 */
public class RBMExtractor {
	private Set<Integer> module;
	private Set<OWLAxiom> finalModule;
	private boolean[] knownNotBottom;//Set<Integer> knownNotBottom;
	private Integer[] definitions;
	private Queue<Integer> queue;
	private Integer owlThing;
	private RuleSet rules;
	private boolean doDefinitions = false;
	private boolean debug = false;
	
	private Integer[] defDeclPos;
	private Integer[] ruleAxioms;

	/**
	 * Constructs a new instance
	 */
	public RBMExtractor(){
		this.doDefinitions = false;
		this.debug = false;
	}

	/**
	 * Constructs a new instance and sets some flags
	 * @param doDefinitions If definitions should be processed
	 * @param debug If additional debug output is desired
     */
	public RBMExtractor(boolean doDefinitions, boolean debug){
		this.doDefinitions = doDefinitions;
		this.debug = debug;
	}

	/**
	 * Provides the active definitions of the last extraction
	 * @return The active definitions of the last extraction
     */
	public Map<OWLObject, OWLAxiom> getActiveDefinitions(){
		Map<OWLObject, OWLAxiom> result = new HashMap<>();

		for(int i = 0; i < definitions.length; i++){
			if(definitions[i] == null || definitions[i] <= 0) continue;

			OWLAxiom ax = (OWLAxiom) rules.getObject(definitions[i]);
			OWLObject def = rules.getObject(i);

			result.put(def, ax);
		}

		return result;
	}

	/**
	 * Uses the rules provided by the rule set to extract a module using the given signature
	 * @param rules A set of rules constructed by a rule builder
	 * @param signature A set of OWL classes forming a signature
	 * @return A set of OWL axioms forming a module for the signature
	 */
	public Set<OWLAxiom> extractModule(RuleSet rules, Set<OWLEntity> signature){
		if(debug) System.out.println("> Starting extraction with signature " + OWLPrinter.getString(signature));
		this.rules = rules;
		//initialize the processing queue to the signature
		module = new HashSet<>();
		finalModule = new HashSet<>();
		knownNotBottom = new boolean[rules.dictionarySize()];
		//TODO: Make this safe against inclusions of owl top and unknown vocabulary
		signature.forEach(x -> knownNotBottom[rules.getId(x)] = true);
		
		//Note: this filter can be dropped, if we assume that signatures do not contain owl:thing
		signature = signature.stream().filter(x -> (!(x instanceof OWLClass)) || !((OWLClass)x).isOWLThing()).collect(Collectors.toSet());
		
		//TODO: Verify if this line is needed or not; it should not be needed, as all elements in the signature have been defined already
		//signature.forEach(x -> module.add(new OWLDeclarationAxiomImpl(x, Collections.emptyList())));
		queue = new LinkedList<>();
		signature.forEach(x -> queue.add(rules.getId(x)));
		//System.out.println("queue now contains " + queue.size() + " elements");
		
		//OWL Thing is always assumed to be not bottom
		OWLDataFactory factory = new OWLDataFactoryImpl();
		owlThing = rules.getId(factory.getOWLThing());
		//queue.add(owlThing);
		//System.out.println("queue now contains " + queue.size() + " elements");
		
		//knownNotBottom[owlThing] = true;//.add(owlThing);

		int[] ruleCounter = new int[rules.ruleCount()]; 						//counter for the number of elements in the rule body
		Integer[] ruleHeads = new Integer[rules.ruleCount()]; 				//rule heads of intermediary rules
		ruleAxioms = new Integer[rules.ruleCount()]; 						//axioms of leaf rules
		Integer[] ruleDefs = new Integer[rules.ruleCount()];					//symbol defined by the rule
		boolean[] definedAxioms = new boolean[rules.dictionarySize()];  //indicates if the axiom is used for a definition
		boolean[] isDeclAxRule = new boolean[rules.ruleCount()];				//indicates if the rule adds a declaration axiom
		defDeclPos = new Integer[rules.dictionarySize()];				//assigns to symbols the declarating axiom for use with definitions
		definitions = new Integer[rules.dictionarySize()];				//assings to symbols the defining axiom
		
		//add base module and signature
		finalModule.addAll(rules.getBaseModule());
		rules.getBaseSignature().forEach(this::addQueue);
		//System.out.println("queue now contains " + queue.size() + " elements");
		
		int pos = 0;
		for(Rule rule : rules){
			ruleCounter[pos] = rule.size();
			ruleHeads[pos] = rule.getHead();
			ruleAxioms[pos] = rule.getAxiom();
			isDeclAxRule[pos] = rules.isDeclRule(pos);
			if(doDefinitions) ruleDefs[pos] = rule.getDefinition();
			pos++;
		}
		
		/*if(debug){
			System.out.print("starting queue is ");
			queue.forEach(x -> System.out.print(rules.lookup(x) + " "));
			System.out.println();
			if(finalModule.size() > 0){
			System.out.println("starting with module ");
			finalModule.forEach(x -> System.out.println(ClassPrinter.printAxiom(x)));
			}
		}*/
		if(debug){
			System.out.println("> base module:");
			finalModule.forEach(System.out::println);
			System.out.println("> entering main loop");
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
				
				//if the axiom is an OWLDeclarationAxiom and there is a valid definition for the declared thing
				//do not progress rule evaluation
				if(doDefinitions && isDeclAxRule[cRule]){
					Integer symbol = rules.getRule(cRule).get(0);
					Integer def = definitions[symbol];
					if(def != null && def >= 0){
						defDeclPos[symbol] = cRule;
						continue;
					}
				}
				
				//check for rule completion, that is, if all body elements 
				//have been found to be possibly not bottom
				if(--ruleCounter[cRule] <= 0){
					Integer head = ruleHeads[cRule];
					
					//if there is no head, then there must be an axiom
					if(head == null){
						Integer currentAxiom = ruleAxioms[cRule];
						//skip, if the axiom is already in the module
						if(knownNotBottom[currentAxiom]) continue;
						//also skip if the axiom is used in a definition
						if(definedAxioms[currentAxiom]) continue;
						
						//find out if adding the axiom might be avoided by assuming a definition
						Integer definedSymbol = ruleDefs[cRule];
						if(doDefinitions && definedSymbol != null && !knownNotBottom[definedSymbol]){
							Integer oldDefAxiom = definitions[definedSymbol];
							
							//if there already is a definition for the symbol, roll back 
							if(oldDefAxiom != null){
								if(debug){
									System.out.println("def of " + OWLPrinter.getString(
												rules.getObject(definedSymbol)) + " collapsed (" +
												OWLPrinter.getString(rules.getObject(currentAxiom)) + ")");
									
								}
								if(oldDefAxiom != -1) collapseDefinition(oldDefAxiom);
								
								module.add(currentAxiom); 
								knownNotBottom[currentAxiom] = true;
								rules.getAxiomSignature(currentAxiom).forEach(this::addQueue);
							}
							else{
								//mark axiom as defined
								definedAxioms[currentAxiom] = true;
								//add definition
								definitions[definedSymbol] = currentAxiom;
								knownNotBottom[definedSymbol] = true;
								queue.add(definedSymbol);
								if(debug) System.out.println("def " +
										OWLPrinter.getString(rules.getObject(definedSymbol)) + " via " +
										OWLPrinter.getString(rules.getObject(currentAxiom)));
							}
						}
						else{
							//in case the head is an axiom, add all new vocabulary from the axiom
							//into the processing queue
							if(debug && !(rules.getObject(currentAxiom) instanceof OWLDeclarationAxiom)) System.out.println("+" + OWLPrinter.getString(rules.getObject(currentAxiom)));
							rules.getAxiomSignature(ruleAxioms[cRule]).forEach(this::addQueue);
							module.add(ruleAxioms[cRule]);
							knownNotBottom[ruleAxioms[cRule]] = true;
						}
					}
					else{
						/*if(head instanceof OWLAxiom){
							System.out.println("this is awkward...");
						}*/
						//in case of an intermediate rule, add the head
						if(debug) System.out.println(rules.getRule(cRule).toDebugString(rules));
						addQueue(head);
					}
				}
			}
			//System.out.println("--------------------------------------");
		}
		
		module.forEach(x -> finalModule.add((OWLAxiom) rules.getObject(x)));
		
		
		return finalModule;
	}

	/**
	 * Provides the axioms used for definitions
	 * @return A list of axioms
     */
	public List<Integer> getDefinedAxioms(){
		List<Integer> result = new LinkedList<>();
		for (Integer definition : definitions) {
			if (definition != null) {
				result.add(definition);
			}
		}
		return result;
	}
	
	private boolean addQueue(Integer o){
		if(o.equals(owlThing)){
			return false;
		}
		if(doDefinitions && definitions[o] != null && definitions[o] >= 0){
			if(debug) System.out.println("def of " + OWLPrinter.getString(rules.getObject(o)) + " collapsed (double adding, " + OWLPrinter.getString(rules.getObject(o)) + ")");
			collapseDefinition(o);
		}
		//add the entity to the list of those known to be possibly not bottom
		if(!knownNotBottom[o]){
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
	
	private void collapseDefinition(Integer definedSymbol){
		Integer defAxiom = definitions[definedSymbol];
		//mark definition as invalid
		definitions[definedSymbol] = -1;
		//add equivalence axiom to the module
		module.add(defAxiom);
		if(debug) System.out.println("+" + OWLPrinter.getString(rules.getObject(defAxiom)) + " (collapsing)");
		knownNotBottom[defAxiom] = true;
		//add signature
		rules.getAxiomSignature(defAxiom).forEach(this::addQueue);
		//add declaration, if the corresponding rule was ignored
		if(defDeclPos[definedSymbol] != null){
			module.add(ruleAxioms[defDeclPos[definedSymbol]]);
			knownNotBottom[ruleAxioms[defDeclPos[definedSymbol]]] = true;
		}
	}
}
