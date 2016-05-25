package de.uniulm.in.ki.mbrenner.fame.simple.rule;
import java.util.*;

import org.semanticweb.owlapi.model.*;

import org.semanticweb.owlapi.model.parameters.Imports;
import uk.ac.manchester.cs.owl.owlapi.OWLDeclarationAxiomImpl;

import javax.annotation.Nonnull;

/**
 * EL Rule Builder
 * Compiles an EL Ontology into a set of rules to extract modules from it
 * @author spellmaker
 */
public class ELRuleBuilder implements OWLAxiomVisitor, OWLClassExpressionVisitor, OWLPropertyExpressionVisitor{
	private RuleSet rs;
	private List<OWLObject> unknownObjects;
	/**
	 * If set to true the builder will output the unknown elements via standard io
	 */
	public boolean printUnknown = false;
	private boolean isTopEq = false;
	private boolean simulate = false;
	private boolean finalizeWithDef;

	private int cause;

	/**
	 * Default constructor
	 */
	public ELRuleBuilder(){
		finalizeWithDef = false;
	}

	/**
	 * Creates a new instance
	 * Allows to configure which extractor should be used for the finalization of the rule set
	 * @param finalizeWithDef If set to true, definitions will be used for the finalization
     */
	public ELRuleBuilder(boolean finalizeWithDef){
		this.finalizeWithDef = finalizeWithDef;
	}

	/**
	 * Compiles the provided ontology into a rule set
	 * @param ontology An OWL Ontology in the EL profile
	 * @return A set of rules for the ontology
     */
	public RuleSet buildRules(OWLOntology ontology){

		rs = new RuleSet();
		unknownObjects = new LinkedList<>();

		for(OWLAxiom a : ontology.getAxioms(Imports.INCLUDED)){
			cause = rs.getId(a);
			a.accept(this);
		}
		
		rs.finalize(!finalizeWithDef, finalizeWithDef);
		
		if(unknownObjects.size() > 0){
			//System.out.println("warning: could not generate rules for at least " + unknownObjects.size() + " constructors");
			if(printUnknown){
				Set<Class<?>> classes = new HashSet<>();
				unknownObjects.stream().filter(o -> !classes.contains(o.getClass())).forEach(o -> {
					classes.add(o.getClass());
					System.out.println("unknown constructor: " + o.getClass());
				});
			}
		}
		return rs;
	}

	@Override
	public void visit(@Nonnull OWLAnnotationAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLSubAnnotationPropertyOfAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
		
	}

	@Override
	public void visit(@Nonnull OWLAnnotationPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLAnnotationPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLClass ce) {
		if(!ce.isTopEntity()){
			if(!simulate) rs.addRule(cause, new Rule(null, rs.getId(new OWLDeclarationAxiomImpl(ce, Collections.emptyList())), null, rs.getId(ce)));
		}
		else{
			isTopEq = true;
		}
	}

	@Override
	public void visit(@Nonnull OWLObjectIntersectionOf ce) {
		Set<OWLClassExpression> ops = ce.getOperands();
		Integer[] arr = new Integer[ops.size()];


		int pos = 0;
		for (OWLClassExpression op : ops) {
			arr[pos++] = rs.getId(op);
		}

		boolean top = true;
		if(!simulate) rs.addRule(cause, new Rule(rs.getId(ce), null, null, arr));
		for(OWLClassExpression e : ops){
			e.accept(this);
			top = top && isTopEq;
		}
		isTopEq = top;
	}

	@Override
	public void visit(@Nonnull OWLObjectUnionOf ce) {
		unknownObjects().add(ce);
		/*boolean top = false;
		for(OWLClassExpression oce : ce.getOperands()){
			if(!simulate) rs.addRule(cause, new Rule(rs.getId(ce), null, null, rs.getId(oce)));
			oce.accept(this);
			top = top || isTopEq;
		}*/
	}

	@Override
	public void visit(@Nonnull OWLObjectComplementOf ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLObjectSomeValuesFrom ce) {
		if(!simulate) rs.addRule(cause, new Rule(null,
				rs.getId(new OWLDeclarationAxiomImpl((OWLEntity) ce.getProperty(), Collections.emptyList())), null,
				rs.getId(ce.getProperty())));
		if(!simulate) rs.addRule(cause, new Rule(rs.getId(ce), null, null, rs.getId(ce.getFiller()), rs.getId(ce.getProperty())));
		ce.getFiller().accept(this);
		isTopEq = false;
	}

	@Override
	public void visit(@Nonnull OWLObjectAllValuesFrom ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLObjectHasValue ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLObjectMinCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLObjectExactCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLObjectMaxCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLObjectHasSelf ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLObjectOneOf ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLDataSomeValuesFrom ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLDataAllValuesFrom ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLDataHasValue ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLDataMinCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLDataExactCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLDataMaxCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(@Nonnull OWLDeclarationAxiom axiom) {
		// TODO Auto-generated method stub
		if(!simulate) rs.addRule(cause, new Rule(null, rs.getId(axiom), null, rs.getId(axiom.getEntity())));
	}

	@Override
	public void visit(@Nonnull OWLSubClassOfAxiom axiom) {
		OWLClassExpression expr = axiom.getSubClass();
		//TODO: This is quite nasty, as it adds a lot of unecessary rules
		simulate = true;
		axiom.getSuperClass().accept(this);
		simulate = false;

		if(!isTopEq) {
			rs.addRule(cause, new Rule(null, rs.getId(axiom), null, rs.getId(expr)));
			expr.accept(this);
			if (isTopEq) {
				rs.addRule(cause, new Rule(null, rs.getId(axiom), null, (Integer[]) null));
			}
		}
	}

	@Override
	public void visit(@Nonnull OWLNegativeObjectPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLAsymmetricObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLReflexiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLDisjointClassesAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLDataPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLObjectPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLEquivalentObjectPropertiesAxiom axiom) {
		for(OWLObjectPropertyExpression e : axiom.getProperties()){
			rs.addRule(cause, new Rule(null, rs.getId(axiom), null, rs.getId(e)));
			e.accept(this);
		}
	}

	@Override
	public void visit(@Nonnull OWLNegativeDataPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLDifferentIndividualsAxiom axiom) {
		rs.addRule(cause, new Rule(null, rs.getId(axiom), null));
	}

	@Override
	public void visit(@Nonnull OWLDisjointDataPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLDisjointObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLObjectPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLObjectPropertyAssertionAxiom axiom) {
		//TODO: Reintroduce after testing is finished, this is actually correct
		//if(!axiom.getProperty().isTopEntity())
			rs.addRule(cause, new Rule(null, rs.getId(axiom), null));
	}

	@Override
	public void visit(@Nonnull OWLFunctionalObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLSubObjectPropertyOfAxiom axiom) {
		OWLObjectPropertyExpression expr = axiom.getSubProperty();
		rs.addRule(cause, new Rule(null, rs.getId(axiom), null, rs.getId(expr)));
		expr.accept(this);
		//axiom.getSuperProperty().accept(this);
	}

	@Override
	public void visit(@Nonnull OWLDisjointUnionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLSymmetricObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLDataPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLFunctionalDataPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLEquivalentDataPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLClassAssertionAxiom axiom) {
		if(!axiom.getClassExpression().isTopEntity())
			rs.addRule(cause, new Rule(null, rs.getId(axiom), null));
	}

	@Override
	public void visit(@Nonnull OWLEquivalentClassesAxiom axiom) {
		OWLClassExpression left = axiom.getClassExpressionsAsList().get(0);
		OWLClassExpression right = axiom.getClassExpressionsAsList().get(1);
		
		rs.addRule(cause, new Rule(null, rs.getId(axiom), ((left instanceof OWLClass) ? rs.getId(left) : null), rs.getId(right)));
		left.accept(this);
		rs.addRule(cause, new Rule(null, rs.getId(axiom), ((right instanceof OWLClass) ? rs.getId(right) : null), rs.getId(left)));
		right.accept(this);
	}

	@Override
	public void visit(@Nonnull OWLDataPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLTransitiveObjectPropertyAxiom axiom) {
		rs.addRule(cause, new Rule(null, rs.getId(axiom), null, rs.getId(axiom.getProperty())));
	}

	@Override
	public void visit(@Nonnull OWLIrreflexiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLSubDataPropertyOfAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLInverseFunctionalObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLSameIndividualAxiom axiom) {
		rs.addRule(cause, new Rule(null, rs.getId(axiom), null));
	}

	@Override
	public void visit(@Nonnull OWLSubPropertyChainOfAxiom axiom) {
		Integer[] props = new Integer[axiom.getPropertyChain().size()];
		for(int i = 0; i < axiom.getPropertyChain().size(); i++){
			props[i] = rs.getId(axiom.getPropertyChain().get(i));
		}
		rs.addRule(cause, new Rule(null, rs.getId(axiom), null, props));
	}

	@Override
	public void visit(@Nonnull OWLInverseObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLHasKeyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull OWLDatatypeDefinitionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(@Nonnull SWRLRule rule) {
		// TODO Auto-generated method stub
		unknownObjects.add(rule);
	}

	/**
	 * Lists objects which could not be processed in the last rule generation
	 * Check this method to find out if the rules generated are sound and complete
	 * @return A collection of unprocessable objects of the last rule generation
     */
	public Collection<OWLObject> unknownObjects() {
		return unknownObjects;
	}

	@Override
	public void visit(@Nonnull OWLObjectProperty property) {
		if(!property.isTopEntity())
			if(!simulate) rs.addRule(cause, new Rule(null, rs.getId(new OWLDeclarationAxiomImpl(property, Collections.emptyList())), null, rs.getId(property)));
	}

	@Override
	public void visit(@Nonnull OWLObjectInverseOf property) {
		if(!simulate) rs.addRule(cause, new Rule(rs.getId(property), null, null, rs.getId(property.getInverse())));
		property.getInverse().accept(this);
	}

	@Override
	public void visit(@Nonnull OWLDataProperty property) {
		unknownObjects().add(property);
	}

	@Override
	public void visit(@Nonnull OWLAnnotationProperty owlAnnotationProperty) {
		unknownObjects().add(owlAnnotationProperty);
	}
}
