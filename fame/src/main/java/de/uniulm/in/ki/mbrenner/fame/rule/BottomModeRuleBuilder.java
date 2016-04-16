package de.uniulm.in.ki.mbrenner.fame.rule;

import java.util.*;

import de.uniulm.in.ki.mbrenner.fame.incremental.v2.OWLDictionary;
import de.uniulm.in.ki.mbrenner.fame.incremental.v2.RuleStorage;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import javax.annotation.Nonnull;

enum Mode{
	BottomMode, TopMode
}

public class BottomModeRuleBuilder implements RuleBuilder, OWLClassExpressionVisitor, OWLPropertyExpressionVisitor, OWLAxiomVisitor {
	private Mode cMode;
	private List<Rule> ruleBuffer;
	private RuleStorage ruleSet;
	private OWLDictionary dictionary;
	private List<OWLObject> unknownObjects;
	public boolean printUnknown = false;

	
	public BottomModeRuleBuilder(){
	}
	@Override
	public void visit(OWLClass ce) {
		//ignore mode here
		//Declaration axiom rule is added elsewhere
		//if(!ce.isTopEntity()) ruleBuffer.add(new Rule(null, dictionary.getId(new OWLDeclarationAxiomImpl(ce, Collections.emptyList())), null, dictionary.getId(ce)));
		//mode is now bottom, as we interpret classes with bottom
		if(ce.isTopEntity()){
			cMode = Mode.TopMode;
		}
		else {
			cMode = Mode.BottomMode;
		}
	}

	@Override
	public void visit(OWLObjectIntersectionOf ce) {
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
			if(cMode == Mode.BottomMode){
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
			for(int i = 0; i < index; i++) shortArgs[i] = ruleArgs[i];
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, shortArgs));
			ruleBuffer.addAll(bottomRules);
			cMode = Mode.BottomMode;
		}
	}

	@Override
	public void visit(OWLObjectUnionOf ce) {
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
			if(cMode == Mode.TopMode){
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
			for(int i = 0; i < index; i++) shortArgs[i] = ruleArgs[i];
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, shortArgs));
			ruleBuffer.addAll(topRules);
			cMode = Mode.TopMode;
		}
	}

	@Override
	public void visit(OWLObjectComplementOf ce) {
		ce.getOperand().accept(this);
		if(cMode == Mode.BottomMode) cMode = Mode.TopMode;
		else if(cMode == Mode.TopMode) cMode = Mode.BottomMode;
		
		ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getOperand())));
	}

	@Override
	public void visit(OWLObjectSomeValuesFrom ce) {
		//process property, ignore mode, as there are no modes for properties
		List<Rule> propertyRules = new LinkedList<>();
		ce.getProperty().accept(this);
		propertyRules.addAll(ruleBuffer);
		ruleBuffer.clear();
		//process filler
		ce.getFiller().accept(this);
		if(cMode == Mode.BottomMode){
			//R, C -> ER.C
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getFiller()), dictionary.getId(ce.getProperty())));
		}
		else if(cMode == Mode.TopMode){
			cMode = Mode.BottomMode;
			ruleBuffer.clear();
			//R -> ER.C
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getProperty())));
		}
		ruleBuffer.addAll(propertyRules);
	}

	@Override
	public void visit(OWLObjectAllValuesFrom ce) {
		List<Rule> propertyRules = new LinkedList<>();
		ce.getProperty().accept(this);
		propertyRules.addAll(ruleBuffer);
		ruleBuffer.clear();
		ce.getFiller().accept(this);
		
		if(cMode == Mode.BottomMode){
			ruleBuffer.clear();
			//R -> VR.C
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getProperty())));
			cMode = Mode.TopMode;
		}
		else if(cMode == Mode.TopMode){
			//R, C -> VR.C
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getProperty()), dictionary.getId(ce.getFiller())));
		}
		ruleBuffer.addAll(propertyRules);
	}

	@Override
	public void visit(OWLObjectHasValue ce) {
		cMode = Mode.BottomMode;
		ce.getProperty().accept(this);
		ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getProperty())));
	}

	@Override
	public void visit(OWLObjectMinCardinality ce) {
		if(ce.getCardinality() <= 0){
			//tautology, can never become anything other than top
			cMode = Mode.TopMode;
		}
		else{
			//process property, ignore mode, as there are no modes for properties
			List<Rule> propertyRules = new LinkedList<>();
			ce.getProperty().accept(this);
			propertyRules.addAll(ruleBuffer);
			ruleBuffer.clear();
			//process filler
			ce.getFiller().accept(this);
			if(cMode == Mode.BottomMode){
				//R, C -> >=n R.C
				ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getFiller()), dictionary.getId(ce.getProperty())));
			}
			else if(cMode == Mode.TopMode){
				cMode = Mode.BottomMode;
				ruleBuffer.clear();
				//R -> >=n R.C
				ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getProperty())));
			}
			ruleBuffer.addAll(propertyRules);
		}
	}

	@Override
	public void visit(OWLObjectExactCardinality ce) {		
		//process property, ignore mode, as there are no modes for properties
		List<Rule> propertyRules = new LinkedList<>();
		ce.getProperty().accept(this);
		propertyRules.addAll(ruleBuffer);
		ruleBuffer.clear();
		ce.getFiller().accept(this);
		if(cMode == Mode.BottomMode){
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getFiller()), dictionary.getId(ce.getProperty())));
		}
		else{
			ruleBuffer.clear();
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getProperty())));
		}
		ruleBuffer.addAll(propertyRules);

		if(ce.getCardinality() == 0){
			cMode = Mode.TopMode;
		}
		else{
			cMode = Mode.BottomMode;
		}
	}

	@Override
	public void visit(OWLObjectMaxCardinality ce) {
		//process property, ignore mode, as there are no modes for properties
		List<Rule> propertyRules = new LinkedList<>();
		ce.getProperty().accept(this);
		propertyRules.addAll(ruleBuffer);
		ruleBuffer.clear();
		//process filler
		ce.getFiller().accept(this);
		if(cMode == Mode.BottomMode){
			cMode = Mode.TopMode;
			//R, C -> ER.C
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getFiller()), dictionary.getId(ce.getProperty())));
		}
		else if(cMode == Mode.TopMode){
			ruleBuffer.clear();
			//R -> ER.C
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getProperty())));
		}
		ruleBuffer.addAll(propertyRules);
	}

	@Override
	public void visit(OWLObjectHasSelf ce) {
		ruleBuffer.clear();
		ce.getProperty().accept(this);
		ruleBuffer.add(new Rule(dictionary.getId(ce), null, null, dictionary.getId(ce.getProperty())));
		cMode = Mode.BottomMode;
	}

	@Override
	public void visit(OWLObjectOneOf ce) {
		if(ce.getIndividuals().isEmpty()){
			//always bottom
			cMode = Mode.BottomMode;
		}
		else{
			ruleBuffer.add(new Rule(dictionary.getId(ce), null, null));
		}
	}

	@Override
	public void visit(OWLDataSomeValuesFrom ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataAllValuesFrom ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataHasValue ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataMinCardinality ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataExactCardinality ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataMaxCardinality ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectProperty property) {
		//if(!property.isTopEntity())
		//	ruleBuffer.add(new Rule(null, dictionary.getId(new OWLDeclarationAxiomImpl(property, Collections.emptyList())), null, dictionary.getId(property)));
		//Declaration axiom is added elsewhere
	}

	@Override
	public void visit(OWLObjectInverseOf property) {
		ruleBuffer.clear();
		property.getInverse().accept(this);
		ruleBuffer.add(new Rule(dictionary.getId(property), null, null, dictionary.getId(property.getInverse())));
	}

	@Override
	public void visit(OWLDataProperty property) {
		unknownObjects.add(property);
	}

	@Override
	public void visit(@Nonnull OWLAnnotationProperty owlAnnotationProperty) {
		unknownObjects.add(owlAnnotationProperty);
	}

	@Override
	public RuleSet buildRules(OWLOntology ontology){
		return buildRules(ontology, false);
	}

	public RuleSet buildRules(OWLOntology ontology, boolean useNoDefExtractor) {
		RuleSet rs = new RuleSet();
		buildRules(ontology, useNoDefExtractor, rs, rs);
		return rs;
	}

	public void buildRules(OWLOntology ontology, boolean useNoDefExtractor, RuleStorage rs, OWLDictionary dict){
		Set<OWLEntity> signature = new HashSet<>();
		//TODO: Check if necessary
		signature.addAll(ontology.getSignature());
		//signature.addAll(ontology.getIndividualsInSignature());
		buildRules(ontology.getAxioms(Imports.INCLUDED), signature, useNoDefExtractor, rs, dict);
	}

	public void buildRules(Set<OWLAxiom> ontology, Set<OWLEntity> signature, boolean useNoDefExtractor, RuleStorage rs, OWLDictionary dict){
		this.ruleSet = rs;
		this.dictionary = dict;
		//initialize rule generation data structures
		cMode = Mode.BottomMode;
		ruleBuffer = new LinkedList<>();
		unknownObjects = new LinkedList<>();
		//process each axiom
		ontology.forEach(x -> x.accept(this));
		//add each element in the ontology signature to the dictionary
		signature.forEach(x -> dictionary.getId(x));
		//ontology.getSignature().forEach(x -> dictionary.getId(x));
		//ontology.getIndividualsInSignature().forEach(x -> dictionary.getId(x));
		//finalize rule set
		ruleSet.finalize();
		if(!unknownObjects.isEmpty()){
			//System.out.println("warning: could not generate rules for at least " + unknownObjects.size() + " things");
			if(printUnknown){
				Set<Class<?>> classes = new HashSet<>();
				for(Object o : unknownObjects){
					if(!classes.contains(o.getClass())){
						classes.add(o.getClass());
						System.out.println("unknown constructor: " + o.getClass());
					}
				}
			}
		}
	}

	@Override
	public Collection<OWLObject> unknownObjects() {
		return unknownObjects;
	}
	
	@Override
	public void visit(OWLAnnotationAssertionAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLDeclarationAxiom axiom) {
		ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getEntity())));
	}
	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		ruleBuffer.clear();
		axiom.getSubClass().accept(this);
		Mode subClassMode = cMode;
		List<Rule> subClassRules = new LinkedList<>();
		subClassRules.addAll(ruleBuffer);
		ruleBuffer.clear();
		axiom.getSuperClass().accept(this);
		
		if(subClassMode == Mode.BottomMode){
			if(cMode == Mode.BottomMode){
				//A -> A c B
				ruleBuffer.clear();
				if(axiom.getSuperClass() instanceof OWLClass)
					ruleBuffer.add(new Rule(null, dictionary.getId(axiom), dictionary.getId(axiom.getSuperClass()), dictionary.getId(axiom.getSubClass())));
				else
					ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getSubClass())));

				ruleBuffer.addAll(subClassRules);
			}
			else if(cMode == Mode.TopMode){
				//A, B -> A c B
				ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getSubClass()), dictionary.getId(axiom.getSuperClass())));
				ruleBuffer.addAll(subClassRules);
			}
		}
		else if(subClassMode == Mode.TopMode){
			if(cMode == Mode.BottomMode){
				// -> A c B
				ruleBuffer.clear();
				ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null));
			}
			else if(cMode == Mode.TopMode){
				//B -> A c B
				ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getSuperClass())));
			}
		}
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null));
	}
	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		ruleBuffer.clear();
		
		axiom.getProperty().accept(this);
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty())));
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		//ruleBuffer.clear();
		//
		//axiom.getProperty().accept(this);
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, (Integer[]) null));
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		List<Rule> rules = new LinkedList<>();
		List<OWLClassExpression> elements = new ArrayList<>();
		OWLClassExpression foundTop = null;
		for(OWLClassExpression oce : axiom.getClassExpressionsAsList()){
			ruleBuffer.clear();
			oce.accept(this);
			if(cMode == Mode.TopMode){
				if(foundTop != null){
					ruleBuffer.clear();
					ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null, (Integer[]) null));
					return;
				}
				else{
					foundTop = oce;
				}
			}
			else if(cMode == Mode.BottomMode){
				elements.add(oce);
				rules.addAll(ruleBuffer);
			}
		}

		ruleBuffer.clear();
		ruleBuffer.addAll(rules);
		if(foundTop != null){
			for(int i = 0; i < elements.size(); i++){
				ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(elements.get(i))));
			}
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
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		ruleBuffer.clear();
		List<Rule> propRules = new LinkedList<>();
		axiom.getProperty().accept(this);
		propRules.addAll(ruleBuffer);
		
		ruleBuffer.clear();
		axiom.getDomain().accept(this);
		if(cMode == Mode.TopMode){
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
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		for(OWLObjectPropertyExpression p : axiom.getProperties()){
			ruleBuffer.clear();
			p.accept(this);
			ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(p)));
			ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
		}
	}
	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		for(OWLIndividual ind : axiom.getIndividualsAsList()){
			Rule r = new Rule(null, dictionary.getId(axiom), null, dictionary.getId(ind));
			ruleSet.addRule(dictionary.getId(axiom), r);
		}
	}
	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
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
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		ruleBuffer.clear();
		axiom.getProperty().accept(this);
		List<Rule> propRules = new LinkedList<>();
		propRules.addAll(ruleBuffer);
		
		ruleBuffer.clear();
		axiom.getRange().accept(this);
		
		if(cMode == Mode.BottomMode){
			ruleBuffer.clear();
			
			ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty())));
		}
		else if(cMode == Mode.TopMode){
			ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty()), dictionary.getId(axiom.getRange())));
		}
		ruleBuffer.addAll(propRules);

		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		//TODO: Reintroduce after testing is completed, as it would be logically sound
		//if(!axiom.getProperty().isTopEntity())
			ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null));
	}
	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		ruleBuffer.clear();
		axiom.getProperty().accept(this);
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty())));
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		ruleBuffer.clear();
		
		axiom.getSubProperty().accept(this);
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getSubProperty())));
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
		List<Rule> rules = new LinkedList<>();
		ruleBuffer.clear();
		axiom.getOWLClass().accept(this);
		Mode lMode = cMode;
		rules.addAll(ruleBuffer);

		boolean foundTop = false;
		for(OWLClassExpression oce : axiom.getOWLDisjointClassesAxiom().getClassExpressions()){
			ruleBuffer.clear();
			oce.accept(this);
			if(cMode == Mode.TopMode){
				if(foundTop || lMode == Mode.BottomMode){
					ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null, (Integer[]) null));
					return;
				}
				foundTop = true;
			}
			rules.addAll(ruleBuffer);
			rules.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(oce)));
		}

		if(!foundTop && lMode == Mode.TopMode){
			ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null, (Integer[]) null));
			return;
		}

		rules.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
		ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getOWLClass())));
	}
	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		ruleBuffer.clear();
		axiom.getProperty().accept(this);
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty())));
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		if(!axiom.getClassExpression().isTopEntity())
			ruleSet.addRule(dictionary.getId(axiom), new Rule(null, dictionary.getId(axiom), null));
	}
	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		//TODO: Implement for longer equivalence chains
		ruleBuffer.clear();

		OWLClassExpression left = axiom.getClassExpressionsAsList().get(0);
		OWLClassExpression right = axiom.getClassExpressionsAsList().get(1);
		if(axiom.getClassExpressionsAsList().size() > 2){
			System.out.println("warning: longer equivalent classes axiom then supported");
		}
		
		left.accept(this);
		Mode leftMode = cMode;
		List<Rule> leftrules = new LinkedList<>();
		leftrules.addAll(ruleBuffer);
		ruleBuffer.clear();
		
		right.accept(this);
		
		if(leftMode != cMode){
			ruleBuffer.clear();
			// -> A = B
			ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null));
		}
		else{
			//A -> A = B, B
			//B -> A = B, A
			ruleBuffer.addAll(leftrules);
			if(cMode == Mode.BottomMode) {
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
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		ruleBuffer.clear();
		
		axiom.getProperty().accept(this);
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty())));
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		ruleBuffer.clear();
		
		axiom.getProperty().accept(this);
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty())));
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		ruleBuffer.clear();
		
		axiom.getProperty().accept(this);
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, dictionary.getId(axiom.getProperty())));
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		for(OWLIndividual ind : axiom.getIndividualsAsList()){
			Rule r = new Rule(null, dictionary.getId(axiom), null, dictionary.getId(ind));
			ruleSet.addRule(dictionary.getId(axiom), r);
		}
	}
	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		Integer[] ruleArgs = new Integer[axiom.getPropertyChain().size()];
		List<Rule> proprules = new LinkedList<>();
		int index = 0;
		for(OWLObjectPropertyExpression p : axiom.getPropertyChain()){
			ruleBuffer.clear();
			p.accept(this);
			proprules.addAll(ruleBuffer);
			ruleArgs[index++] = dictionary.getId(p);
		}
		ruleBuffer.add(new Rule(null, dictionary.getId(axiom), null, ruleArgs));
		
		ruleBuffer.forEach(x -> ruleSet.addRule(dictionary.getId(axiom), x));
	}
	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		
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
	public void visit(OWLHasKeyAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(OWLDatatypeDefinitionAxiom axiom) {
		unknownObjects.add(axiom);
	}
	@Override
	public void visit(SWRLRule rule) {
		unknownObjects.add(rule);
	}

}
