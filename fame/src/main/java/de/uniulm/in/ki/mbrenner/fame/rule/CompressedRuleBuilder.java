package de.uniulm.in.ki.mbrenner.fame.rule;

import java.util.*;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import javax.annotation.Nonnull;

public class CompressedRuleBuilder implements OWLAxiomVisitor, OWLClassExpressionVisitor, OWLPropertyExpressionVisitor{
	private List<OWLObject> unknownObjects;
	/*
	 Contains the different signature combinations, under which the current considered element is non-top/-bottom, meaning
	 for every Set<OWLEntity> s : signature: The current element is not bot/top.

	 Following conventions hold:
	  - signature == null indicates, that the current element is always top/bot
	  - signature == Collections.emptySet() indicates, that the current element is always non-top/non-bot
	  - do NOT modify the contents of signature, instead create a new set and add the elements of signature
	  - combine signatures ONLY via the andSignature and orSignature methods
	 */
	private Set<Set<OWLObject>> signature;
	private boolean botMode;

	public List<OWLObject> getUnknownObjects(){
		return Collections.unmodifiableList(unknownObjects);
	}

	public CompressedRuleSet buildRules(OWLOntology ontology){
		unknownObjects = new LinkedList<>();
		CompressedRuleSet ruleSet = new CompressedRuleSet();
		for(OWLAxiom a : ontology.getAxioms(Imports.INCLUDED)){
			signature = null;
			a.accept(this);
			if(signature != null){
				if(signature.isEmpty()){
					ruleSet.addBase(a);
				}
				for(Set<OWLObject> s : signature){
					ruleSet.addRule(new CompressedRule(a, s));
				}
			}
		}

		for(OWLEntity o : ontology.getSignature()){
			ruleSet.lookup(o);
		}
		for(OWLIndividual i : ontology.getIndividualsInSignature()){
			ruleSet.lookup(i);
		}
		
		ruleSet.finalize();
		return ruleSet;
	}
	
	/**
	 * Combine two signature sets into a single set by matching each signature from sign1 with each signature
	 * of sign2
	 * @param sign1 The first signature
	 * @param sign2 The second signature
	 * @return A merged signature set
	 */
	private Set<Set<OWLObject>> signatureAnd(Set<Set<OWLObject>> sign1, Set<Set<OWLObject>> sign2){
		if(sign1 == null || sign2 == null){
			return null;
		}

		if(sign1.isEmpty()){
			if(sign2.isEmpty()){
				return Collections.emptySet();
			}
			return new HashSet<>(sign2);
		}
		else if(sign2.isEmpty()){
			return new HashSet<>(sign1);
		}

		Set<Set<OWLObject>> result = new HashSet<>();
		for(Set<OWLObject> s1 : sign1){
			for(Set<OWLObject> s2 : sign2){
				Set<OWLObject> m1 = new HashSet<>();
				m1.addAll(s1);
				m1.addAll(s2);
				result.add(m1);
			}
		}
		return result;
	}

	private Set<Set<OWLObject>> signatureOr(Set<Set<OWLObject>> sign1, Set<Set<OWLObject>> sign2){
		if(sign1 == null && sign2 == null) return null;

		Set<Set<OWLObject>> result = new HashSet<>();
		if(sign1 != null) result.addAll(sign1);
		if(sign2 != null) result.addAll(sign2);
		return result;
	}

	private void makeSimpleSet(OWLObject ...entities){
		signature = new HashSet<>();
		Set<OWLObject> tmp = new HashSet<>();
		Collections.addAll(tmp, entities);
		signature.add(tmp);
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
		makeSimpleSet(axiom.getEntity());
	}

	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		Set<Set<OWLObject>> sleft;
		boolean leftbotMode;
		axiom.getSubClass().accept(this);
		sleft = signature;
		leftbotMode = botMode;
		axiom.getSuperClass().accept(this);
		
		if(leftbotMode){
			if(botMode){
				signature = sleft;
			}
			else{
				signature = signatureAnd(signature, sleft);
			}
		}
		else{
			if(botMode){
				signature = Collections.emptySet();
			}
			else{
				//signature = signature;
			}
		}
	}

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		signature = Collections.emptySet();
	}

	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		axiom.getProperty().accept(this);
	}

	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		signature = Collections.emptySet();
	}

	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		List<Set<Set<OWLObject>>> signatures = new LinkedList<>();
		boolean foundTop = false;
		for(OWLClassExpression oce : axiom.getClassExpressionsAsList()){
			oce.accept(this);

			if(!botMode){
				if(foundTop){
					signature = Collections.emptySet();
					return;
				}
				else{
					foundTop = true;
				}
			}
			else{
				signatures.add(signature);
			}
		}

		signature = new HashSet<>();
		if(foundTop){
			signatures.forEach(x -> signature = signatureOr(signature, x));
		}
		else{
			for(int i = 0; i < signatures.size(); i++){
				for(int j = i + 1; j < signatures.size(); j++){
					signature = signatureOr(signature, signatureAnd(signatures.get(i), signatures.get(j)));
				}
			}
		}
	}

	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		Set<Set<OWLObject>> propSign = null;
		axiom.getProperty().accept(this);
		propSign = signature;

		axiom.getDomain().accept(this);
		if(!botMode){
			signature = signatureAnd(propSign, signature);
		}
		else{
			signature = propSign;
		}
	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		Set<Set<OWLObject>> propSign = null;
		axiom.getProperty().accept(this);
		propSign = signature;

		axiom.getDomain().accept(this);
		if(!botMode){
			signature = signatureAnd(propSign, signature);
		}
		else{
			signature = propSign;
		}
	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		Set<Set<OWLObject>> sign = new HashSet<>();
		for(OWLObjectPropertyExpression p : axiom.getProperties()){
			p.accept(this);
			sign = signatureOr(sign, signature);
		}
		signature = sign;
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		Set<Set<OWLObject>> res = new HashSet<>();
		for(OWLIndividual ind : axiom.getIndividuals()){
			Set<OWLObject> s = new HashSet<>();
			s.add(ind);
			res.add(s);
		}
		signature = res;
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		List<Set<Set<OWLObject>>> signatures = new ArrayList<>();
		for(OWLObjectPropertyExpression expr : axiom.getProperties()){
			expr.accept(this);
			signatures.add(signature);
		}

		Set<Set<OWLObject>> sign = new HashSet<>();
		for(int i = 0; i < signatures.size(); i++){
			for(int j = i + 1; j < signatures.size(); j++){
				sign = signatureOr(sign, signatureAnd(signatures.get(i), signatures.get(j)));
			}
		}
		signature = sign;
	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		Set<Set<OWLObject>> propSig = null;
		axiom.getProperty().accept(this);
		propSig = signature;

		axiom.getRange().accept(this);

		if(botMode){
			signature = propSig;
		}
		else{
			signature = signatureAnd(signature, propSig);
		}
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		//Remark: The commented code is actually logically correct and should be reintroduced after testing
		//TODO: Reintroduce after testing is finished
		/*if(axiom.getProperty().isTopEntity())
			signature = null;
		else*/
			signature = Collections.emptySet();
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		axiom.getProperty().accept(this);
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		axiom.getSubProperty().accept(this);
	}

	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
		Set<Set<OWLObject>> signatures = new HashSet<>();
		axiom.getOWLClass().accept(this);
		Set<Set<OWLObject>> leftSide = signature;
		boolean leftMode = botMode;

		boolean foundTop = false;

		for(OWLClassExpression oce : axiom.getOWLDisjointClassesAxiom().getClassExpressions()){
			oce.accept(this);
			if(!botMode){
				if(foundTop || leftMode){
					signature = Collections.emptySet();
					return;
				}
				foundTop = true;
			}
			signatures = signatureOr(signatures, signature);
		}

		if(!foundTop && !leftMode){
			signature = Collections.emptySet();
			return;
		}

		signature = signatureOr(signatures, leftSide);
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		axiom.getProperty().accept(this);
	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		Set<Set<OWLObject>> propSig = null;
		axiom.getProperty().accept(this);
		propSig = signature;
	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom){
		axiom.getProperty().accept(this);
	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		if(axiom.getClassExpression().isTopEntity())
			signature = null;
		else
			signature = Collections.emptySet();
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		axiom.getClassExpressionsAsList().get(0).accept(this);
		Set<Set<OWLObject>> sleft = signature;
		boolean mleft = botMode;
		
		axiom.getClassExpressionsAsList().get(1).accept(this);

		if(mleft != botMode){
			signature = Collections.emptySet();
		}
		else{
			signature = signatureOr(signature, sleft);
		}
		
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		axiom.getProperty().accept(this);
	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		axiom.getProperty().accept(this);
	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		axiom.getProperty().accept(this);
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		Set<Set<OWLObject>> sign = new HashSet<>();
		for(OWLObjectPropertyExpression p : axiom.getPropertyChain()){
			p.accept(this);
			sign = signatureAnd(sign, signature);
		}

		signature = sign;
	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		Set<Set<OWLObject>> sign = null;
		axiom.getFirstProperty().accept(this);
		sign = signature;
		axiom.getSecondProperty().accept(this);
		sign = signatureOr(sign, signature);
		signature = sign;
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

	@Override
	public void visit(OWLClass ce) {
		if(ce.isTopEntity()){
			botMode = false;
			signature = null;
		}
		else {
			botMode = true;
			makeSimpleSet(ce);
		}
	}

	@Override
	public void visit(OWLObjectIntersectionOf ce) {
		Set<Set<OWLObject>> csign = new HashSet<>();
		boolean allTop = true;
		for(OWLClassExpression c : ce.getOperands()){
			c.accept(this);
			if(!botMode && allTop){
				csign = signatureOr(csign, signature);
			}
			else if(allTop){
				allTop = false;
				csign = signature;
			}
			else if(botMode){
				csign = signatureAnd(csign, signature);
			}
		}
		if(allTop) botMode = false;
		else botMode = true;
		signature = csign;
	}

	@Override
	public void visit(OWLObjectUnionOf ce) {
		Set<Set<OWLObject>> csign = new HashSet<>();
		boolean allBot = true;
		for(OWLClassExpression c : ce.getOperands()){
			c.accept(this);
			if(botMode && allBot){
				csign = signatureOr(csign, signature);
			}
			else if(allBot){
				allBot = false;
				csign = signature;
			}
			else if(!botMode){
				csign = signatureAnd(csign, signature);
			}
		}
		signature = csign;
	}

	@Override
	public void visit(OWLObjectComplementOf ce) {
		ce.getOperand().accept(this);
		botMode = !botMode;
	}

	@Override
	public void visit(OWLObjectSomeValuesFrom ce) {
		ce.getProperty().accept(this);
		Set<Set<OWLObject>> rsign = signature;
		
		ce.getFiller().accept(this);
		if(botMode){
			signature = signatureAnd(signature, rsign);
		}
		else{
			signature = rsign;
			botMode = true;
		}
	}

	@Override
	public void visit(OWLObjectAllValuesFrom ce) {
		ce.getProperty().accept(this);
		Set<Set<OWLObject>> rsign = signature;
		
		ce.getFiller().accept(this);
		if(botMode){
			signature = rsign;
			botMode = false;
		}
		else{
			signature = signatureAnd(signature, rsign);
		}
	}

	@Override
	public void visit(OWLObjectHasValue ce) {
		ce.getProperty().accept(this);
	}

	@Override
	public void visit(OWLObjectMinCardinality ce) {
		if(ce.getCardinality() <= 0){
			signature = null;
			botMode = false;
		}
		else{
			ce.getProperty().accept(this);
			Set<Set<OWLObject>> propSig = signature;
			ce.getFiller().accept(this);
			if(botMode){
				signature = signatureAnd(signature, propSig);
			}
			else{
				signature = propSig;
				botMode = true;
			}
		}
	}

	@Override
	public void visit(OWLObjectExactCardinality ce) {
		ce.getProperty().accept(this);
		Set<Set<OWLObject>> propSig = signature;
		ce.getFiller().accept(this);
		if(botMode){
			signature = signatureAnd(signature, propSig);
		}
		else{
			signature = propSig;
		}
		botMode = (ce.getCardinality() != 0);
	}

	@Override
	public void visit(OWLObjectMaxCardinality ce) {
		Set<Set<OWLObject>> propSig = null;
		ce.getProperty().accept(this);
		propSig = signature;
		ce.getFiller().accept(this);

		if(botMode){
			botMode = false;
			signature = signatureAnd(signature, propSig);
		}
		else{
			signature = propSig;
		}
	}

	@Override
	public void visit(OWLObjectHasSelf ce) {
		botMode = true;
		ce.getProperty().accept(this);
	}

	@Override
	public void visit(OWLObjectOneOf ce) {
		botMode = true;
		if(ce.getIndividuals().isEmpty()){
			signature = null;
		}
		else{
			signature = Collections.emptySet();
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
	public void visit(OWLDataMaxCardinality ce) { unknownObjects.add(ce);}

	@Override
	public void visit(OWLObjectProperty property) {
		makeSimpleSet(property);
	}

	@Override
	public void visit(OWLObjectInverseOf property) {
		property.getInverse().accept(this);
	}

	@Override
	public void visit(OWLDataProperty property) {
		makeSimpleSet(property);
	}

	@Override
	public void visit(@Nonnull OWLAnnotationProperty owlAnnotationProperty) {

	}

	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		Set<Set<OWLObject>> res = new HashSet<>();
		for(OWLIndividual ind : axiom.getIndividuals()){
			Set<OWLObject> s = new HashSet<>();
			s.add(ind);
			res.add(s);
		}
		signature = res;
	}
}
