package de.uniulm.in.ki.mbrenner.fame.rule;
import java.util.*;

import org.semanticweb.owlapi.model.*;

import org.semanticweb.owlapi.model.parameters.Imports;
import uk.ac.manchester.cs.owl.owlapi.OWLDeclarationAxiomImpl;

import javax.annotation.Nonnull;

/**
 * EL implementation of the RuleBuilder interface.
 * Compiles an EL Ontology into a set of rules to extract modules from it
 * @author spellmaker
 */
public class ELRuleBuilder implements RuleBuilder, OWLAxiomVisitor, OWLClassExpressionVisitor, OWLPropertyExpressionVisitor{
	private RuleSet rs;
	private List<OWLObject> unknownObjects;
	public boolean printUnknown = false;
	private boolean isTopEq = false;
	private boolean simulate = false;
	private boolean finalizeWithDef;

	public ELRuleBuilder(){
		finalizeWithDef = false;
	}

	public ELRuleBuilder(boolean finalizeWithDef){
		this.finalizeWithDef = finalizeWithDef;
	}

	@Override
	public RuleSet buildRules(OWLOntology ontology){

		rs = new RuleSet();
		unknownObjects = new LinkedList<>();

		ontology.getAxioms(Imports.INCLUDED).forEach(x -> x.accept(this));
		
		rs.finalize(!finalizeWithDef, finalizeWithDef);
		
		if(unknownObjects.size() > 0){
			//System.out.println("warning: could not generate rules for at least " + unknownObjects.size() + " constructors");
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
		return rs;
	}

	@Override
	public void visit(OWLAnnotationAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
		
	}

	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLClass ce) {
		if(!ce.isTopEntity()){
			if(!simulate) rs.add(new Rule(null, rs.putObject(new OWLDeclarationAxiomImpl(ce, Collections.emptyList())), null, rs.putObject(ce)));
		}
		else{
			isTopEq = true;
		}
	}

	@Override
	public void visit(OWLObjectIntersectionOf ce) {
		Set<OWLClassExpression> ops = ce.getOperands();
		Integer[] arr = new Integer[ops.size()];


		int pos = 0;
		for(Iterator<OWLClassExpression> iter = ops.iterator(); iter.hasNext();){
			arr[pos++] = rs.putObject(iter.next()); 
		}

		boolean top = true;
		if(!simulate) rs.add(new Rule(rs.putObject(ce), null, null, arr));
		for(OWLClassExpression e : ops){
			e.accept(this);
			top = top && isTopEq;
		}
		isTopEq = top;
	}

	@Override
	public void visit(OWLObjectUnionOf ce) {
		unknownObjects().add(ce);
		/*boolean top = false;
		for(OWLClassExpression oce : ce.getOperands()){
			if(!simulate) rs.add(new Rule(rs.putObject(ce), null, null, rs.putObject(oce)));
			oce.accept(this);
			top = top || isTopEq;
		}*/
	}

	@Override
	public void visit(OWLObjectComplementOf ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectSomeValuesFrom ce) {
		if(!simulate) rs.add(new Rule(null,
				rs.putObject(new OWLDeclarationAxiomImpl((OWLEntity) ce.getProperty(), Collections.emptyList())), null,
				rs.putObject(ce.getProperty())));
		if(!simulate) rs.add(new Rule(rs.putObject(ce), null, null, rs.putObject(ce.getFiller()), rs.putObject(ce.getProperty())));
		ce.getFiller().accept(this);
		isTopEq = false;
	}

	@Override
	public void visit(OWLObjectAllValuesFrom ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectHasValue ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectMinCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectExactCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectMaxCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectHasSelf ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLObjectOneOf ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataSomeValuesFrom ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataAllValuesFrom ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataHasValue ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataMinCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataExactCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDataMaxCardinality ce) {
		// TODO Auto-generated method stub
		unknownObjects.add(ce);
	}

	@Override
	public void visit(OWLDeclarationAxiom axiom) {
		// TODO Auto-generated method stub
		if(!simulate) rs.add(new Rule(null, rs.putObject(axiom), null, rs.putObject(axiom.getEntity())));
	}

	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		OWLClassExpression expr = axiom.getSubClass();
		//TODO: This is quite nasty, as it adds a lot of unecessary rules
		simulate = true;
		axiom.getSuperClass().accept(this);
		simulate = false;

		if(!isTopEq) {
			rs.add(new Rule(null, rs.putObject(axiom), null, rs.putObject(expr)));
			expr.accept(this);
			if (isTopEq) {
				rs.add(new Rule(null, rs.putObject(axiom), null, (Integer[]) null));
			}
		}
	}

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		for(OWLObjectPropertyExpression e : axiom.getProperties()){
			rs.add(new Rule(null, rs.putObject(axiom), null, rs.putObject(e)));
			e.accept(this);
		}
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		rs.add(new Rule(null, rs.putObject(axiom), null));
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		//TODO: Reintroduce after testing is finished, this is actually correct
		//if(!axiom.getProperty().isTopEntity())
			rs.add(new Rule(null, rs.putObject(axiom), null));
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		OWLObjectPropertyExpression expr = axiom.getSubProperty();
		rs.add(new Rule(null, rs.putObject(axiom), null, rs.putObject(expr)));
		expr.accept(this);
		//axiom.getSuperProperty().accept(this);
	}

	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		if(!axiom.getClassExpression().isTopEntity())
			rs.add(new Rule(null, rs.putObject(axiom), null));
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		OWLClassExpression left = axiom.getClassExpressionsAsList().get(0);
		OWLClassExpression right = axiom.getClassExpressionsAsList().get(1);
		
		rs.add(new Rule(null, rs.putObject(axiom), ((left instanceof OWLClass) ? rs.putObject(left) : null), rs.putObject(right)));
		left.accept(this);
		rs.add(new Rule(null, rs.putObject(axiom), ((right instanceof OWLClass) ? rs.putObject(right) : null), rs.putObject(left)));
		right.accept(this);
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		rs.add(new Rule(null, rs.putObject(axiom), null, rs.putObject(axiom.getProperty())));
	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		rs.add(new Rule(null, rs.putObject(axiom), null));
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		Integer[] props = new Integer[axiom.getPropertyChain().size()];
		for(int i = 0; i < axiom.getPropertyChain().size(); i++){
			props[i] = rs.putObject(axiom.getPropertyChain().get(i));
		}
		rs.add(new Rule(null, rs.putObject(axiom), null, props));
	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLHasKeyAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(OWLDatatypeDefinitionAxiom axiom) {
		// TODO Auto-generated method stub
		unknownObjects.add(axiom);
	}

	@Override
	public void visit(SWRLRule rule) {
		// TODO Auto-generated method stub
		unknownObjects.add(rule);
	}

	@Override
	public Collection<OWLObject> unknownObjects() {
		return unknownObjects;
	}

	@Override
	public void visit(OWLObjectProperty property) {
		if(!property.isTopEntity())
			if(!simulate) rs.add(new Rule(null, rs.putObject(new OWLDeclarationAxiomImpl(property, Collections.emptyList())), null, rs.putObject(property)));
	}

	@Override
	public void visit(OWLObjectInverseOf property) {
		if(!simulate) rs.add(new Rule(rs.putObject(property), null, null, rs.putObject(property.getInverse())));
		property.getInverse().accept(this);
	}

	@Override
	public void visit(OWLDataProperty property) {
		unknownObjects().add(property);
	}

	@Override
	public void visit(@Nonnull OWLAnnotationProperty owlAnnotationProperty) {
		unknownObjects().add(owlAnnotationProperty);
	}
}
