package de.uniulm.in.ki.mbrenner.fame.rule;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;

public class CompressedRuleBuilder implements OWLAxiomVisitor, OWLClassExpressionVisitor, OWLPropertyExpressionVisitor{
	private CompressedRuleSet ruleSet;
	private List<OWLObject> unknownObjects;
	
	private Set<Set<OWLEntity>> signature;
	private boolean botMode;
	
	public CompressedRuleBuilder(){
		
	}
	
	public Set<CompressedRule> makeRule(OWLAxiom a){
		unknownObjects = new LinkedList<>();
		a.accept(this);
		Set<CompressedRule> res = new HashSet<>();
		signature.forEach(x -> res.add(new CompressedRule(a, x)));
		return res;
	}
	
	public CompressedRuleSet buildRules(Set<OWLAxiom> ontology){
		unknownObjects = new LinkedList<>();
		ruleSet = new CompressedRuleSet();
		for(OWLAxiom a : ontology){
			signature = null;
			a.accept(this);
			if(signature != null){
				for(Set<OWLEntity> s : signature){
					ruleSet.addRule(new CompressedRule(a, s));
				}
				if(signature.isEmpty()){
					ruleSet.addBase(a);
				}
			}
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
	private Set<Set<OWLEntity>> merge(Set<Set<OWLEntity>> sign1, Set<Set<OWLEntity>> sign2){
		Set<Set<OWLEntity>> result = new HashSet<>();
		for(Set<OWLEntity> s1 : sign1){
			for(Set<OWLEntity> s2 : sign2){
				Set<OWLEntity> m1 = new HashSet<>();
				m1.addAll(s1);
				m1.addAll(s2);
				result.add(m1);
			}
		}
		return result;
	}
	
	private void makeSimpleSet(OWLEntity ...entities){
		signature = new HashSet<>();
		Set<OWLEntity> tmp = new HashSet<>();
		for(OWLEntity e : entities) tmp.add(e);
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
		Set<Set<OWLEntity>> sleft;
		boolean left;
		axiom.getSubClass().accept(this);
		sleft = signature;
		left = botMode;
		axiom.getSuperClass().accept(this);
		
		if(left){
			if(botMode){
				signature = sleft;
			}
			else{
				merge(signature, sleft);
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
		axiom.getProperty().accept(this);
	}

	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		unknownObjects.add(axiom);
		System.out.println("todo: Implement disjoint classes");
	}

	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		unknownObjects.add(axiom);
		System.out.println("todo: Implement object property domains");
	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		unknownObjects.add(axiom);
		System.out.println("todo: Implement equivalent object properties");
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		signature = Collections.emptySet();
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		unknownObjects.add(axiom);
		System.out.println("todo: Implement disjoint object properties");
	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		unknownObjects.add(axiom);
		System.out.println("todo: Implement object property range");
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		signature = Collections.emptySet();
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		unknownObjects.add(axiom);
		System.out.println("todo: Implement functional object properties");
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		axiom.getSubProperty().accept(this);
	}

	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
		unknownObjects.add(axiom);
		System.out.println("todo: Implement disjoint union");
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		axiom.getProperty().accept(this);
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
		signature = Collections.emptySet();
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		axiom.getClassExpressionsAsList().get(0).accept(this);
		Set<Set<OWLEntity>> sleft = signature;
		boolean mleft = botMode;
		
		axiom.getClassExpressionsAsList().get(1).accept(this);
		
		if(mleft != botMode){
			signature = Collections.emptySet();
		}
		else{
			signature.addAll(sleft);
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
		unknownObjects.add(axiom);
		System.out.println("todo: Implement sub property chains");
	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		unknownObjects.add(axiom);
		System.out.println("todo: Implement inverse properties");
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
		botMode = true;
		makeSimpleSet(ce);
	}

	@Override
	public void visit(OWLObjectIntersectionOf ce) {
		Set<Set<OWLEntity>> csign = new HashSet<>();
		boolean allTop = true;
		for(OWLClassExpression c : ce.getOperands()){
			c.accept(this);
			if(!botMode && allTop){
				csign = merge(csign, signature);
			}
			else if(allTop){
				allTop = false;
				csign = signature;
			}
			else if(botMode){
				csign = merge(csign, signature);
			}
		}
		signature = csign;
	}

	@Override
	public void visit(OWLObjectUnionOf ce) {
		Set<Set<OWLEntity>> csign = new HashSet<>();
		boolean allBot = true;
		for(OWLClassExpression c : ce.getOperands()){
			c.accept(this);
			if(botMode && allBot){
				csign = merge(csign, signature);
			}
			else if(allBot){
				allBot = false;
				csign = new HashSet<>();
				csign = signature;
			}
			else if(!botMode){
				csign = merge(csign, signature);
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
		Set<Set<OWLEntity>> rsign = signature;
		
		ce.getFiller().accept(this);
		if(botMode){
			signature = merge(signature, rsign);
		}
		else{
			signature = rsign;
			botMode = true;
		}
	}

	@Override
	public void visit(OWLObjectAllValuesFrom ce) {
		ce.getProperty().accept(this);
		Set<Set<OWLEntity>> rsign = signature;
		
		ce.getFiller().accept(this);
		if(botMode){
			signature = rsign;
			botMode = false;
		}
		else{
			signature = merge(signature, rsign);
		}
	}

	@Override
	public void visit(OWLObjectHasValue ce) {
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectMinCardinality ce) {
		unknownObjects.add(ce);
		System.out.println("todo: Implement min cardinality");
	}

	@Override
	public void visit(OWLObjectExactCardinality ce) {
		unknownObjects.add(ce);
		System.out.println("todo: Implement exact cardinality");
	}

	@Override
	public void visit(OWLObjectMaxCardinality ce) {
		unknownObjects.add(ce);
		System.out.println("todo: Implement max cardinality");
	}

	@Override
	public void visit(OWLObjectHasSelf ce) {
		botMode = true;
		ce.getProperty().accept(this);
	}

	@Override
	public void visit(OWLObjectOneOf ce) {
		unknownObjects.add(ce);
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
		makeSimpleSet(property);
	}

	@Override
	public void visit(OWLObjectInverseOf property) {
		property.getInverse().accept(this);
	}

	@Override
	public void visit(OWLDataProperty property) {
		unknownObjects.add(property);
	}

	@Override
	public void visit(@Nonnull OWLAnnotationProperty owlAnnotationProperty) {

	}

	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		signature = Collections.emptySet();
	}
}
