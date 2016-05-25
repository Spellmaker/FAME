package de.uniulm.in.ki.mbrenner.fame.simple.rule;

import java.util.*;

import de.uniulm.in.ki.mbrenner.fame.incremental.OWLDictionary;
import de.uniulm.in.ki.mbrenner.fame.incremental.RuleStorage;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import javax.annotation.Nonnull;

/**
 * Transforms an ontology into a set of rules for module extraction
 * These rules contain additional information, which allows to avoid adding equivalence axioms in some situations
 */
public class RuleBuilder implements OWLClassExpressionVisitor, OWLPropertyExpressionVisitor, OWLAxiomVisitor {
	private boolean botMode;
	private List<Rule> ruleBuffer;
	private RuleStorage ruleSet;
	private OWLDictionary dictionary;
	private List<OWLObject> unknownObjects;
	/**
	 * If set to true unknown elements will be printed after rule generation has been completed
	 */
	public boolean printUnknown = false;

	@Override
	public void visit(@Nonnull OWLClass ce) {
		//ignore mode here
		//Declaration axiom rule is added elsewhere
		//if(!ce.isTopEntity()) ruleBuffer.add(new Rule(null, dictionary.getId(new OWLDeclarationAxiomImpl(ce, Collections.emptyList())), null, dictionary.getId(ce)));
		//mode is now bottom, as we interpret classes with bottom
		botMode = !ce.isTopEntity();
	}

	@Override
	public void visit(@Nonnull OWLObjectIntersectionOf ce) {
		//process branches
		Set<OWLClassExpression> ops = ce.getOperands();
		Integer[] ruleArgs = new Integer[ops.size()];
		int index = 0;
		
		List<Rule> bottomRules = new LinkedList<>();
		List<Rule> topRules = new LinkedList<>();
		
		boolean allTop = true;
		for(OWLClassExpression o : ops){
			//evaluate child
			o.accept(this);
			//if the child is either bottom or unknown
			if(botMode){
				//if it is the first bottom mode child, remove
				//all top mode childs from the operands
				if(allTop){
					index = 0;
					allTop = false;
				}
				bottomRules.addAll(ruleBuffer);
			}
			//if the child is either top or unkown _and_ there has been no bottom mode child
			else if(allTop){
				topRules.addAll(ruleBuffer);
			}
			else{
				//drop the generated rules, as top rules are not needed
				ruleBuffer.clear();
				continue;
			}
			//add generated rules at a new position
			ruleBuffer.clear();
			ruleArgs[index++] = dictionary.getId(o);
		}
		if(allTop){
			//mode is now top mode. As soon as one element is known to be possibly different from top
			//the whole conjunction is known to be possibly different from top
			for(int i = 0; i < index; i++){
				ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, ruleArgs[i]));
			}
			ruleBuffer.addAll(topRules);
			//mode is still set from the last element
		}
		else{
			//mode is now bot mode. As long as one element is known to be bot, the whole conjunction is still bot
			Integer[] shortArgs = new Integer[index];
			System.arraycopy(ruleArgs, 0, shortArgs, 0, index);
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, shortArgs));
			ruleBuffer.addAll(bottomRules);
			botMode = true;
		}
	}

	@Override
	public void visit(@Nonnull OWLObjectUnionOf ce) {
		//process branches
		Set<OWLClassExpression> ops = ce.getOperands();
		Integer[] ruleArgs = new Integer[ops.size()];
		int index = 0;
		
		List<Rule> bottomRules = new LinkedList<>();
		List<Rule> topRules = new LinkedList<>();
		
		boolean allBottom = true;
		for(OWLClassExpression o : ops){
			//evaluate child
			o.accept(this);
			//if the child is either top or unknown
			if(!botMode){
				//if it is the first top mode child, remove
				//all bottom mode childs from the operands
				if(allBottom){
					index = 0;
					allBottom = false;
				}
				topRules.addAll(ruleBuffer);
			}
			//if the child is either top or unkown _and_ there has been no bottom mode child
			else if(allBottom){
				bottomRules.addAll(ruleBuffer);
			}
			else{
				ruleBuffer.clear();
				continue;
			}
			ruleBuffer.clear();
			
			ruleArgs[index++] = dictionary.getId(o);
		}
		
		if(allBottom){
			//mode is now bottom mode. As soon as one element is known to be possibly different from bottom
			//the whole conjunction is known to be possibly different from bottom
			for(int i = 0; i < index; i++){
				ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, ruleArgs[i]));
			}
			ruleBuffer.addAll(bottomRules);
			//mode is still set from the last element
		}
		else{
			//mode is now top mode. As long as one element is known to be top, the whole conjunction is still top
			Integer[] shortArgs = new Integer[index];
			System.arraycopy(ruleArgs, 0, shortArgs, 0, index);
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, shortArgs));
			ruleBuffer.addAll(topRules);
			botMode = false;
		}
	}

	@Override
	public void visit(@Nonnull OWLObjectComplementOf ce) {
		ce.getOperand().accept(this);
		botMode = !botMode;
		ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getOperand())));
	}

	@Override
	public void visit(@Nonnull OWLObjectSomeValuesFrom ce) {
		//process property, ignore mode, as there are no modes for properties
		List<Rule> propertyRules = new LinkedList<>();
		ce.getProperty().accept(this);
		propertyRules.addAll(ruleBuffer);
		ruleBuffer.clear();
		//process filler
		ce.getFiller().accept(this);
		if(botMode){
			//R, C -> ER.C
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getFiller()), dictionary.getId(ce.getProperty())));
		}
		else{
			botMode = true;
			ruleBuffer.clear();
			//R -> ER.C
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getProperty())));
		}
		ruleBuffer.addAll(propertyRules);
	}

	@Override
	public void visit(@Nonnull OWLObjectAllValuesFrom ce) {
		List<Rule> propertyRules = new LinkedList<>();
		ce.getProperty().accept(this);
		propertyRules.addAll(ruleBuffer);
		ruleBuffer.clear();
		ce.getFiller().accept(this);

		//R, C -> VR.C
		if(botMode){
			ruleBuffer.clear();
			//R -> VR.C
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getProperty())));
			botMode = false;
		}
		else
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getProperty()), dictionary.getId(ce.getFiller())));
		ruleBuffer.addAll(propertyRules);
	}

	@Override
	public void visit(@Nonnull OWLObjectHasValue ce) {
		botMode = true;
		ce.getProperty().accept(this);
		ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getProperty())));
	}

	@Override
	public void visit(@Nonnull OWLObjectMinCardinality ce) {
		if(ce.getCardinality() <= 0){
			//tautology, can never become anything other than top
			botMode = false;
		}
		else{
			//process property, ignore mode, as there are no modes for properties
			List<Rule> propertyRules = new LinkedList<>();
			ce.getProperty().accept(this);
			propertyRules.addAll(ruleBuffer);
			ruleBuffer.clear();
			//process filler
			ce.getFiller().accept(this);
			if(botMode){
				//R, C -> >=n R.C
				ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getFiller()), dictionary.getId(ce.getProperty())));
			}
			else{
				botMode = true;
				ruleBuffer.clear();
				//R -> >=n R.C
				ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getProperty())));
			}
			ruleBuffer.addAll(propertyRules);
		}
	}

	@Override
	public void visit(@Nonnull OWLObjectExactCardinality ce) {
		//process property, ignore mode, as there are no modes for properties
		List<Rule> propertyRules = new LinkedList<>();
		ce.getProperty().accept(this);
		propertyRules.addAll(ruleBuffer);
		ruleBuffer.clear();
		ce.getFiller().accept(this);
		if(botMode){
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getFiller()), dictionary.getId(ce.getProperty())));
		}
		else{
			ruleBuffer.clear();
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getProperty())));
		}
		ruleBuffer.addAll(propertyRules);

		botMode = ce.getCardinality() != 0;
	}

	@Override
	public void visit(@Nonnull OWLObjectMaxCardinality ce) {
		//process property, ignore mode, as there are no modes for properties
		List<Rule> propertyRules = new LinkedList<>();
		ce.getProperty().accept(this);
		propertyRules.addAll(ruleBuffer);
		ruleBuffer.clear();
		//process filler
		ce.getFiller().accept(this);
		if(botMode){
			botMode = false;
			//R, C -> ER.C
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getFiller()), dictionary.getId(ce.getProperty())));
		}
		else{
			ruleBuffer.clear();
			//R -> ER.C
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getProperty())));
		}
		ruleBuffer.addAll(propertyRules);
	}

	@Override
	public void visit(@Nonnull OWLObjectHasSelf ce) {
		ruleBuffer.clear();
		ce.getProperty().accept(this);
		ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getProperty())));
		botMode = true;
	}

	@Override
	public void visit(@Nonnull OWLObjectOneOf ce) {
		if(ce.getIndividuals().isEmpty()){
			//always bottom
			botMode = true;
		}
		else{
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null));
		}
	}

	@Override
	public void visit(@Nonnull OWLDataSomeValuesFrom ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLDataAllValuesFrom ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLDataHasValue ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLDataMinCardinality ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLDataExactCardinality ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLDataMaxCardinality ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLObjectProperty property) {
		//if(!property.isTopEntity())
		//	ruleBuffer.add(new Rule(null, dictionary.getId(new OWLDeclarationAxiomImpl(property, Collections.emptyList())), null, dictionary.getId(property)));
		//Declaration axiom is added elsewhere
	}

	@Override
	public void visit(@Nonnull OWLObjectInverseOf property) {
		ruleBuffer.clear();
		property.getInverse().accept(this);
		ruleBuffer.add(new Rule(dictionary.getId(property), null, null, dictionary.getId(property.getInverse())));
	}

	@Override
	public void visit(@Nonnull OWLDataProperty property) {
		unknownObjects.add(property);
	}

	@Override
	public void visit(@Nonnull OWLAnnotationProperty owlAnnotationProperty) {
		unknownObjects.add(owlAnnotationProperty);
	}

	/**
	 * Builds the module extraction rules for the provided ontology
	 * @param ontology An OWL ontology
	 * @return A set of rules usable for module extraction
     */
	public RuleSet buildRules(OWLOntology ontology){
		return buildRules(ontology, false);
	}

	/**
	 * Builds the module extraction rules for the provided ontology
	 * @param ontology An OWL ontology
	 * @param useNoDefExtractor If set to true, the noDef extractor will be used to finalize the rule set
     * @return A set of rules usable for module extraction
     */
	public RuleSet buildRules(OWLOntology ontology, boolean useNoDefExtractor) {
		RuleSet rs = new RuleSet();
		buildRules(ontology, useNoDefExtractor, rs, rs);
		return rs;
	}

	/**
	 * Builds the module extraction rules for the provided ontology
	 * @param ontology An OWL ontology
	 * @param useNoDefExtractor If set to true, the noDef extractor will be used to finalize the rule set
	 * @param rs A rule storage to be used for the rule generation
     * @param dict A dictionary to be used for the rule generation
     */
	public void buildRules(OWLOntology ontology, boolean useNoDefExtractor, RuleStorage rs, OWLDictionary dict){
		Set<OWLEntity> signature = new HashSet<>();
		//TODO: Check if necessary
		signature.addAll(ontology.getSignature());
		//signature.addAll(ontology.getIndividualsInSignature());
		buildRules(ontology.getAxioms(Imports.INCLUDED), signature, useNoDefExtractor, rs, dict);
	}

	/**
	 * Builds the module extraction rules for the provided axioms
	 * @param ontology A set of axioms
	 * @param signature The combined signature of the provided axioms
	 * @param useNoDefExtractor If set to true, the noDef extractor will be used to finalize the rule set
	 * @param rs A rule storage to be used for the rule generation
     * @param dict A dictionary to be used for the rule generation
     */
	public void buildRules(Set<OWLAxiom> ontology, Set<OWLEntity> signature, boolean useNoDefExtractor, RuleStorage rs, OWLDictionary dict){
		this.ruleSet = rs;
		this.dictionary = dict;
		//initialize rule generation data structures
		botMode = true;
		ruleBuffer = new LinkedList<>();
		unknownObjects = new LinkedList<>();
		//process each axiom
		ontology.forEach(x -> x.accept(this));
		//add each element in the ontology signature to the dictionary
		signature.forEach(x -> dictionary.getId(x));
		//ontology.getSignature().forEach(x -> dictionary.getId(x));
		//ontology.getIndividualsInSignature().forEach(x -> dictionary.getId(x));
		//finalizeSet rule set
		ruleSet.finalizeSet();
		if(!unknownObjects.isEmpty()){
			//System.out.println("warning: could not generate rules for at least " + unknownObjects.size() + " things");
			if(printUnknown){
				Set<Class<?>> classes = new HashSet<>();
				unknownObjects.stream().filter(o -> !classes.contains(o.getClass())).forEach(o -> {
					classes.add(o.getClass());
					System.out.println("unknown constructor: " + o.getClass());
				});
			}
		}
	}

	/**
	 * Provides the objects which could not be processed in the last rule generation
	 * @return The unprocessed objects of the last rule generation
     */
	public Collection<OWLObject> unknownObjects() {
		return unknownObjects;
	}
	
	@Override
	public void visit(@Nonnull OWLAnnotationAssertionAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(@Nonnull OWLSubAnnotationPropertyOfAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(@Nonnull OWLAnnotationPropertyDomainAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(@Nonnull OWLAnnotationPropertyRangeAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(@Nonnull OWLDeclarationAxiom axiom) {
		ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getEntity())));
	}
	@Override
	public void visit(@Nonnull OWLSubClassOfAxiom axiom) {
		ruleBuffer.clear();
		axiom.getSubClass().accept(this);
		boolean subClassMode = botMode;
		List<Rule> subClassRules = new LinkedList<>();
		subClassRules.addAll(ruleBuffer);
		ruleBuffer.clear();
		axiom.getSuperClass().accept(this);
		
		if(subClassMode){
			if(botMode){
				//A -> A c B
				ruleBuffer.clear();
				if(axiom.getSuperClass() instanceof OWLClass)
					ruleBuffer.add(new Rule(null, dictionary.getId(axiom), dictionary.getId(axiom.getSuperClass()), dictionary.getId(axiom.getSubClass())));
				else
					ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getSubClass())));

				ruleBuffer.addAll(subClassRules);
			}
			else{
				//A, B -> A c B
				ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getSubClass()), dictionary.getId(axiom.getSuperClass())));
				ruleBuffer.addAll(subClassRules);
			}
		}
		else{
			if(botMode){
				// -> A c B
				ruleBuffer.clear();
				ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null));
			}
			else{
				//B -> A c B
				ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getSuperClass())));
			}
		}
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(@Nonnull OWLNegativeObjectPropertyAssertionAxiom axiom) {
		ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null));
	}
	@Override
	public void visit(@Nonnull OWLAsymmetricObjectPropertyAxiom axiom) {
		ruleBuffer.clear();
		
		axiom.getProperty().accept(this);
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty())));
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(@Nonnull OWLReflexiveObjectPropertyAxiom axiom) {
		//ruleBuffer.clear();
		//
		//axiom.getProperty().accept(this);
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, (Integer[]) null));
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(@Nonnull OWLDisjointClassesAxiom axiom) {
		List<Rule> rules = new LinkedList<>();
		List<OWLClassExpression> elements = new ArrayList<>();
		OWLClassExpression foundTop = null;
		for(OWLClassExpression oce : axiom.getClassExpressionsAsList()){
			ruleBuffer.clear();
			oce.accept(this);
			if(!botMode){
				if(foundTop != null){
					ruleBuffer.clear();
					ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null, (Integer[]) null));
					return;
				}
				else{
					foundTop = oce;
				}
			}
			else{
				elements.add(oce);
				rules.addAll(ruleBuffer);
			}
		}

		ruleBuffer.clear();
		ruleBuffer.addAll(rules);
		if(foundTop != null){
			Integer axiomId = dictionary.getId(axiom);
			elements.forEach(x -> ruleBuffer.add(new Rule(null, axiomId, null, dictionary.getId(x))));
		}
		else{
			for(int i = 0; i < elements.size(); i++){
				for(int j = i + 1; j < elements.size(); j++){
					ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(elements.get(i)), dictionary.getId(elements.get(j))));
				}
			}
		}
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(@Nonnull OWLDataPropertyDomainAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(@Nonnull OWLObjectPropertyDomainAxiom axiom) {
		ruleBuffer.clear();
		List<Rule> propRules = new LinkedList<>();
		axiom.getProperty().accept(this);
		propRules.addAll(ruleBuffer);
		
		ruleBuffer.clear();
		axiom.getDomain().accept(this);
		if(!botMode){
			ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty()), dictionary.getId(axiom.getDomain())));
		}
		else{
			ruleBuffer.clear();
			ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty())));
		}
		ruleBuffer.addAll(propRules);
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(@Nonnull OWLEquivalentObjectPropertiesAxiom axiom) {
		for(OWLObjectPropertyExpression p : axiom.getProperties()){
			ruleBuffer.clear();
			p.accept(this);
			ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(p)));
			ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
		}
	}
	@Override
	public void visit(@Nonnull OWLNegativeDataPropertyAssertionAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(@Nonnull OWLDifferentIndividualsAxiom axiom) {
		for(OWLIndividual ind : axiom.getIndividualsAsList()){
			Rule r = new Rule(null, dictionary.getId(axiom), null, dictionary.getId(ind));
			ruleSet.addRule(dictionary.getId(axiom), r);
		}
	}
	@Override
	public void visit(@Nonnull OWLDisjointDataPropertiesAxiom axiom) {
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLDisjointObjectPropertiesAxiom axiom) {
		List<List<Rule>> props = new LinkedList<>();
		List<OWLObjectPropertyExpression> expr = new LinkedList<>();
		
		for(OWLObjectPropertyExpression oce : axiom.getProperties()){
			ruleBuffer.clear();
			oce.accept(this);
			
			List<Rule> r = new LinkedList<>();
			r.addAll(ruleBuffer);
			expr.add(oce);
			props.add(r);
		}
		
		ruleBuffer.clear();
		for(int i = 0; i < props.size(); i++){
			if(props.get(i) == null) continue;
			ruleBuffer.addAll(props.get(i));
			for(int j = i + 1; j < props.size(); j++){
				if(props.get(j) == null) continue;
				
				ruleBuffer.add(new Rule(dictionary.getId(axiom), null, null, dictionary.getId(expr.get(i)), dictionary.getId(expr.get(j))));
			}
		}
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(@Nonnull OWLObjectPropertyRangeAxiom axiom) {
		ruleBuffer.clear();
		axiom.getProperty().accept(this);
		List<Rule> propRules = new LinkedList<>();
		propRules.addAll(ruleBuffer);
		
		ruleBuffer.clear();
		axiom.getRange().accept(this);
		
		if(botMode){
			ruleBuffer.clear();
			
			ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty())));
		}
		else{
			ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty()), dictionary.getId(axiom.getRange())));
		}
		ruleBuffer.addAll(propRules);

		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(@Nonnull OWLObjectPropertyAssertionAxiom axiom) {
		//TODO: Reintroduce after testing is completed, as it would be logically sound
		//if(!axiom.getProperty().isTopEntity())
			ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null));
	}
	@Override
	public void visit(@Nonnull OWLFunctionalObjectPropertyAxiom axiom) {
		ruleBuffer.clear();
		axiom.getProperty().accept(this);
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty())));
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(@Nonnull OWLSubObjectPropertyOfAxiom axiom) {
		ruleBuffer.clear();
		
		axiom.getSubProperty().accept(this);
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getSubProperty())));
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(@Nonnull OWLDisjointUnionAxiom axiom) {
		List<Rule> rules = new LinkedList<>();
		ruleBuffer.clear();
		axiom.getOWLClass().accept(this);
		boolean lMode = botMode;
		rules.addAll(ruleBuffer);

		boolean foundTop = false;
		for(OWLClassExpression oce : axiom.getOWLDisjointClassesAxiom().getClassExpressions()){
			ruleBuffer.clear();
			oce.accept(this);
			if(!botMode){
				if(foundTop || lMode){
					ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null, (Integer[]) null));
					return;
				}
				foundTop = true;
			}
			rules.addAll(ruleBuffer);
			rules.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(oce)));
		}

		if(!foundTop && !lMode){
			ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null, (Integer[]) null));
			return;
		}

		rules.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
		ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getOWLClass())));
	}
	@Override
	public void visit(@Nonnull OWLSymmetricObjectPropertyAxiom axiom) {
		ruleBuffer.clear();
		axiom.getProperty().accept(this);
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty())));
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(@Nonnull OWLDataPropertyRangeAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(@Nonnull OWLFunctionalDataPropertyAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(@Nonnull OWLEquivalentDataPropertiesAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(@Nonnull OWLClassAssertionAxiom axiom) {
		if(!axiom.getClassExpression().isTopEntity())
			ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null));
	}
	@Override
	public void visit(@Nonnull OWLEquivalentClassesAxiom axiom) {
		//TODO: Implement for longer equivalence chains
		ruleBuffer.clear();

		OWLClassExpression left = axiom.getClassExpressionsAsList().get(0);
		OWLClassExpression right = axiom.getClassExpressionsAsList().get(1);
		if(axiom.getClassExpressionsAsList().size() > 2){
			System.out.println("warning: longer equivalent classes axiom then supported");
		}
		
		left.accept(this);
		boolean leftMode = botMode;
		List<Rule> leftrules = new LinkedList<>();
		leftrules.addAll(ruleBuffer);
		ruleBuffer.clear();
		
		right.accept(this);
		
		if(leftMode != botMode){
			ruleBuffer.clear();
			// -> A = B
			ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null));
		}
		else{
			//A -> A = B, B
			//B -> A = B, A
			ruleBuffer.addAll(leftrules);
			if(botMode) {
				ruleBuffer.add(new Rule(null, dictionary.getId(axiom), ((left instanceof OWLClass) ? dictionary.getId(left) : null), dictionary.getId(right)));
				ruleBuffer.add(new Rule(null, dictionary.getId(axiom), ((right instanceof OWLClass) ? dictionary.getId(right) : null), dictionary.getId(left)));
			}
			else {
				ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(right)));
				ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(left)));
			}
		}
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(@Nonnull OWLDataPropertyAssertionAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(@Nonnull OWLTransitiveObjectPropertyAxiom axiom) {
		ruleBuffer.clear();
		
		axiom.getProperty().accept(this);
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty())));
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(@Nonnull OWLIrreflexiveObjectPropertyAxiom axiom) {
		ruleBuffer.clear();
		
		axiom.getProperty().accept(this);
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty())));
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(@Nonnull OWLSubDataPropertyOfAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(@Nonnull OWLInverseFunctionalObjectPropertyAxiom axiom) {
		ruleBuffer.clear();
		
		axiom.getProperty().accept(this);
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty())));
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(@Nonnull OWLSameIndividualAxiom axiom) {
		for(OWLIndividual ind : axiom.getIndividualsAsList()){
			Rule r = new Rule(null, dictionary.getId(axiom), null, dictionary.getId(ind));
			ruleSet.addRule(dictionary.getId(axiom), r);
		}
	}
	@Override
	public void visit(@Nonnull OWLSubPropertyChainOfAxiom axiom) {
		Integer[] ruleArgs = new Integer[axiom.getPropertyChain().size()];
		List<Rule> proprules = new LinkedList<>();
		int index = 0;
		for(OWLObjectPropertyExpression p : axiom.getPropertyChain()){
			ruleBuffer.clear();
			p.accept(this);
			proprules.addAll(ruleBuffer);
			ruleArgs[index++] = dictionary.getId(p);
		}
		ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null, ruleArgs));
		proprules.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(@Nonnull OWLInverseObjectPropertiesAxiom axiom) {
		
		ruleBuffer.clear();
		axiom.getFirstProperty().accept(this);
		List<Rule> rules = new LinkedList<>();
		rules.addAll(ruleBuffer);
		axiom.getSecondProperty().accept(this);
		ruleBuffer.addAll(rules);
		
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getFirstProperty())));
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getSecondProperty())));
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(@Nonnull OWLHasKeyAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(@Nonnull OWLDatatypeDefinitionAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(@Nonnull SWRLRule rule) {
		unknownObjects.add(rule);
	}

}
