package de.uniulm.in.ki.mbrenner.fame.debug.graphstream;

import de.uniulm.in.ki.mbrenner.fame.simple.rule.Rule;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import de.uniulm.in.ki.mbrenner.owlprinter.OWLPrinter;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Extracts a local module from an ontology
 * @author spellmaker
 *
 */
public class RBMExtractorNoDef {
	private Set<OWLAxiom> finalModule;
	private boolean[] knownNotBottom;//Set<Integer> knownNotBottom;
	private Queue<Integer> queue;
	private Integer owlThing;
	private boolean debug = false;
	//private Integer[] ruleAxioms;

	private Set<OWLAxiom> base;

	/**
	 * Constructs a new instance
	 */
	public RBMExtractorNoDef(){
		this.debug = false;
	}

	/**
	 * Constructs a new instance
	 * @param debug If additional debug output is desired
     */
	public RBMExtractorNoDef(boolean debug){
		this.debug = debug;
	}

	/**
	 * Sets the base module of this extractor
	 * The base module is added to each module regardless of the signature
	 * @param base The base module
     */
	public void setBase(Set<OWLAxiom> base){
		this.base = base;
	}

	/**
	 * Uses the rules provided by the rule set to extract a module using the given signature
	 * @param rules A set of rules constructed by a rule builder
	 * @param signature A set of OWL classes forming a signature
	 * @return A set of OWL axioms forming a module for the signature
	 */
	public Set<OWLAxiom> extractModule(RuleSet rules, Set<OWLEntity> signature){

		if(debug) System.out.println("> Extraction start");


		/*Graph graph = new SingleGraph("Module");
		graph.addAttribute("ui.stylesheet", "url('file:///C:\\Users\\spellmaker\\Desktop\\stylesheet.css')");
		signature.forEach(x -> graph.addNode(OWLPrinter.getString(x)).setAttribute("ui.label", OWLPrinter.getString(x)));
		Map<OWLEntity, Set<OWLAxiom>> entitySource = new HashMap<>();
		Map<OWLObject, String> idMap = new HashMap<>();
		int edgeid = 0;
		int nodeid = 0;*/

		//initialize the processing queue to the signature
		Set<Integer> module = new HashSet<>();
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
		//Integer[] ruleHeads = new Integer[rules.ruleCount()]; 				//rule heads of intermediary rules
		//ruleAxioms = new Integer[rules.ruleCount()]; 						//axioms of leaf rules

		//add base module and signature
		finalModule.addAll(rules.getBaseModule());
		rules.getBaseSignature().forEach(this::addQueue);
		//System.out.println("queue now contains " + queue.size() + " elements");
		//add predefined base if available
		if(base != null){
			finalModule.addAll(base);
			for(OWLAxiom a : base){
				a.getSignature().forEach(x -> addQueue(rules.getId(x)));
			}
		}

		int pos = 0;
		for(Rule rule : rules){
			ruleCounter[pos] = rule.size();
			//ruleHeads[pos] = rule.getHead();
			//ruleAxioms[pos] = rule.getAxiom();
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
					Rule crRule = rules.getRule(cRule);
					//if there is no head, then there must be an axiom
					if(crRule.getHead() == null){
						Integer currentAxiom = crRule.getAxiom();
						//skip, if the axiom is already in the module
						//in case the head is an axiom, add all new vocabulary from the axiom
						//into the processing queue
						rules.getAxiomSignature(currentAxiom).forEach(this::addQueue);

						/*if(!module.contains(currentAxiom)){
							OWLAxiom ax = (OWLAxiom) rules.getObject(currentAxiom);
							if(!(ax instanceof OWLDeclarationAxiom)) {
								Node n = graph.addNode("" + (nodeid++));
								n.setAttribute("ui.label", OWLPrinter.getString(ax));
								idMap.put(ax, "" + (nodeid-1));
								for (OWLEntity e : ax.getSignature()) {
									if (signature.contains(e)) {
										graph.addEdge("" + (edgeid++), OWLPrinter.getString(e), idMap.get(ax), true);
									}
									Set<OWLAxiom> source = entitySource.get(e);
									if (source == null) {
										source = new HashSet<>();
										source.add(ax);
										entitySource.put(e, source);
										continue;
									}
									for (OWLAxiom b : source) {
										try{
											graph.addEdge("" + (edgeid++), idMap.get(b), idMap.get(ax), true);
										}
										catch(EdgeRejectedException exc){
											//skip, there already is a connection
										}
									}
								}
							}
						}*/

						if(debug && !module.contains(currentAxiom) && !(rules.getObject(currentAxiom) instanceof OWLDeclarationAxiom)) System.out.println("added axiom " + OWLPrinter.getString(rules.getObject(currentAxiom)));//ClassPrinter.printAxiom((OWLAxiom) rules.lookup(currentAxiom)));
						module.add(currentAxiom);

						knownNotBottom[currentAxiom] = true;
					}
					else{
						/*if(head instanceof OWLAxiom){
							System.out.println("this is awkward...");
						}*/
						//in case of an intermediate rule, add the head
						addQueue(crRule.getHead());
					}
				}
			}
			//System.out.println("--------------------------------------");
		}

		module.forEach(x -> finalModule.add((OWLAxiom) rules.getObject(x)));
		//graph.display();
		return finalModule;
	}

	private boolean addQueue(Integer o){
		if(o.equals(owlThing)){
			//handle owl:thing as a special case
			return false;
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
}
