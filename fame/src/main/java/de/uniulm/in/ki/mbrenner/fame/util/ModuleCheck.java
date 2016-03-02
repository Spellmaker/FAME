package de.uniulm.in.ki.mbrenner.fame.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import com.clarkparsia.owlapi.modularity.locality.LocalityEvaluator;
import com.clarkparsia.owlapi.modularity.locality.SyntacticLocalityEvaluator;

import de.uniulm.in.ki.mbrenner.fame.locality.SemanticLocalityEvaluator;

/**
 * Checks for the correctness of a module
 * @author spellmaker
 *
 */
public class ModuleCheck implements OWLAxiomVisitor{
	private OWLReasonerFactory factory = new Reasoner.ReasonerFactory();
	private SemanticLocalityEvaluator locality;
	private SyntacticLocalityEvaluator locality2 = new SyntacticLocalityEvaluator(LocalityClass.BOTTOM_BOTTOM);
	private OWLAxiom error;
	
	private LocalityEvaluator current = null;
	private Set<OWLEntity> signature = null;
	
	public ModuleCheck(OWLOntologyManager manager){
		//Configuration config = new Configuration();
		//config.existentialStrategyType = ExistentialStrategyType.INDIVIDUAL_REUSE;
		locality = new SemanticLocalityEvaluator(manager, factory); /*new OWLReasonerFactory() {
			
			@Override
			public String getReasonerName() {
				return factory.getReasonerName();
			}
			
			@Override
			public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config)
					throws IllegalConfigurationException {
				return factory.createReasoner(ontology, config);
			}
			
			@Override
			public OWLReasoner createReasoner(OWLOntology ontology) {
				return factory.createReasoner(ontology, config);
			}
			
			@Override
			public OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config)
					throws IllegalConfigurationException {
				return factory.createNonBufferingReasoner(ontology, config);
			}
			
			@Override
			public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
				return factory.createNonBufferingReasoner(ontology, config);
			}
		});*/
	}	
	
	/**
	 * Checks for syntactical locality of the module in the given ontology
	 * @param ontology An OWL ontology
	 * @param module A module
	 * @return null, if the module is correct or the axiom for which the module is not correct
	 */
	public OWLAxiom isSyntacticalLocalModule(OWLOntology ontology, Set<OWLAxiom> module){
		return check(ontology, module, locality2, Collections.emptySet());
	}
	
	public OWLAxiom isSyntacticalLocalModule(OWLOntology ontology, Set<OWLAxiom> module, Set<OWLAxiom> skip){
		return check(ontology, module, locality2, skip);
	}
	
	/**
	 * Checks for semantical locality of the module in the given ontology using a reasoner
	 * @param ontology An OWL ontology
	 * @param module A module
	 * @return null, if the module is correct or the axiom for which the module is not correct
	 */
	public OWLAxiom isSemanticalLocalModule(OWLOntology ontology, Set<OWLAxiom> module){
		return check(ontology, module, locality, Collections.emptySet());
	}
	
	public OWLAxiom isSemanticalLocalModule(OWLOntology ontology, Set<OWLAxiom> module, Set<OWLAxiom> skip){
		return check(ontology, module, locality, skip);
	}
	
	private OWLAxiom check(OWLOntology ontology, Set<OWLAxiom> module, LocalityEvaluator evaluator, Set<OWLAxiom> skip){
		signature = new HashSet<>();
		current = evaluator;
		module.forEach(x -> signature.addAll(x.getSignature()));
		//System.out.println("checking with signature");
		//for(OWLEntity e : signature){
		//	System.out.println(e);
		//}
		error = null;
		for(OWLAxiom axiom : ontology.getAxioms()){
			if(module.contains(axiom) || skip.contains(axiom)) continue;
			
			axiom.accept(this);
			if(error != null) return error;
		}
		return null;
	}

	@Override
	public void visit(OWLDeclarationAxiom axiom) {
		//skip
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		//skip
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		//skip
	}

	@Override
	public void visit(OWLAnnotationAssertionAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLHasKeyAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(OWLDatatypeDefinitionAxiom axiom) {
		if(!current.isLocal(axiom, signature)) error = axiom;
	}

	@Override
	public void visit(SWRLRule rule) {
		//skip
	}
}
