package de.uniulm.in.ki.mbrenner.fame.util.locality;

import com.clarkparsia.owlapi.modularity.locality.LocalityEvaluator;
import com.google.common.base.Equivalence;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.*;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Created by spellmaker on 10.03.2016.
 */
public class EquivalenceLocalityEvaluator implements LocalityEvaluator, OWLAxiomVisitor, OWLClassExpressionVisitor, OWLPropertyExpressionVisitor {
    private Map<OWLObject, OWLObject> definitions;
    private LocalityEvaluator local;
    private OWLAxiom replacedAxiom;
    private OWLClassExpression replacedClassExpression;
    private OWLObjectPropertyExpression replacedObjectPropertyExpression;

    private List<OWLObject> unknownElements;

    public EquivalenceLocalityEvaluator(LocalityEvaluator local){
        definitions = new HashMap<>();
        this.local = local;
        unknownElements = new LinkedList<>();
    }

    @Override
    public boolean isLocal(@Nonnull OWLAxiom owlAxiom, @Nonnull Set<? extends OWLEntity> signature) {
        unknownElements = new LinkedList<>();
        owlAxiom.accept(this);
        if(!unknownElements.isEmpty()){
            //System.out.println("warning: encountered an unknown axiom type " + owlAxiom.getClass());
            return local.isLocal(owlAxiom, signature);
        }
        return local.isLocal(replacedAxiom, signature);
    }

    public void addDefinition(OWLObject object, OWLObject definition){
        definitions.put(object, definition);
    }

    public boolean resolveDefinitions(){
        //make sure there are no cyclical definitions and replace occurrences of defined elements in
        //definitions with their definition
        boolean changed = true;
        boolean faulty = false;

        while(changed){
            List<OWLObject> keys = new LinkedList<>(definitions.keySet());
            while(changed){
                changed = false;

                for(OWLObject o : keys){
                    OWLClassExpression old = (OWLClassExpression) definitions.get(o);
                    if(old.getSignature().contains(o)){
                        return false;
                    }
                    old.accept(this);
                    if(!old.equals(replacedClassExpression)){
                        changed = true;
                        definitions.put(o, replacedClassExpression);
                    }
                }
            }
        }

        return true;
    }

    public List<OWLObject> getUnknownElements(){
        return Collections.unmodifiableList(unknownElements);
    }

    public OWLAxiom getReplacedAxiom(OWLAxiom axiom){
        axiom.accept(this);
        return replacedAxiom;
    }

    /*
     *
     * Start of OWLAxiomVisitor methods
     *
     */
    @Override
    public void visit(@Nonnull OWLDeclarationAxiom owlDeclarationAxiom) {
        replacedAxiom = owlDeclarationAxiom;
    }

    @Override
    public void visit(@Nonnull OWLDatatypeDefinitionAxiom axiom) {
        unknownElements.add(axiom);
    }

    @Override
    public void visit(@Nonnull OWLAnnotationAssertionAxiom axiom) {
        unknownElements.add(axiom);
    }

    @Override
    public void visit(@Nonnull OWLSubAnnotationPropertyOfAxiom axiom) {
        unknownElements.add(axiom);
    }

    @Override
    public void visit(@Nonnull OWLAnnotationPropertyDomainAxiom axiom) {
        unknownElements.add(axiom);
    }

    @Override
    public void visit(@Nonnull OWLAnnotationPropertyRangeAxiom axiom) {
        unknownElements.add(axiom);
    }

    @Override
    public void visit(@Nonnull OWLSubClassOfAxiom axiom) {
        axiom.getSubClass().accept(this);
        OWLClassExpression sub = replacedClassExpression;
        axiom.getSuperClass().accept(this);
        replacedAxiom = new OWLSubClassOfAxiomImpl(sub, replacedClassExpression, Collections.emptyList());
    }

    @Override
    public void visit(@Nonnull OWLNegativeObjectPropertyAssertionAxiom axiom) {
        axiom.getProperty().accept(this);
        replacedAxiom = new OWLNegativeObjectPropertyAssertionAxiomImpl(axiom.getSubject(), replacedObjectPropertyExpression, axiom.getObject(), Collections.emptySet());
    }

    @Override
    public void visit(@Nonnull OWLAsymmetricObjectPropertyAxiom axiom) {
        axiom.getProperty().accept(this);
        replacedAxiom = new OWLAsymmetricObjectPropertyAxiomImpl(replacedObjectPropertyExpression, Collections.emptyList());
    }

    @Override
    public void visit(@Nonnull OWLReflexiveObjectPropertyAxiom axiom) {
        axiom.getProperty().accept(this);
        replacedAxiom = new OWLReflexiveObjectPropertyAxiomImpl(replacedObjectPropertyExpression, Collections.emptyList());
    }

    @Override
    public void visit(@Nonnull OWLDisjointClassesAxiom axiom) {
        Set<OWLClassExpression> classes = new HashSet<>();
        for(OWLClassExpression oce : axiom.getClassExpressions()){
            oce.accept(this);
            classes.add(replacedClassExpression);
        }
        replacedAxiom = new OWLDisjointClassesAxiomImpl(classes, Collections.emptySet());
    }

    @Override
    public void visit(@Nonnull OWLDataPropertyDomainAxiom axiom) {
        unknownElements.add(axiom);
    }

    @Override
    public void visit(@Nonnull OWLObjectPropertyDomainAxiom axiom) {
        axiom.getDomain().accept(this);
        OWLClassExpression dom = replacedClassExpression;
        axiom.getProperty().accept(this);
        replacedAxiom = new OWLObjectPropertyDomainAxiomImpl(replacedObjectPropertyExpression, dom, Collections.emptySet());
    }

    @Override
    public void visit(@Nonnull OWLEquivalentObjectPropertiesAxiom axiom) {
        Set<OWLObjectPropertyExpression> props = new HashSet<>();
        for(OWLObjectPropertyExpression ope : axiom.getProperties()){
            ope.accept(this);
            props.add(replacedObjectPropertyExpression);
        }
        replacedAxiom = new OWLEquivalentObjectPropertiesAxiomImpl(props, Collections.emptySet());
    }

    @Override
    public void visit(@Nonnull OWLNegativeDataPropertyAssertionAxiom axiom) {
        unknownElements.add(axiom);
    }

    @Override
    public void visit(@Nonnull OWLDifferentIndividualsAxiom axiom) {
        replacedAxiom = axiom;
    }

    @Override
    public void visit(@Nonnull OWLDisjointDataPropertiesAxiom axiom) {
        unknownElements.add(axiom);
    }

    @Override
    public void visit(@Nonnull OWLDisjointObjectPropertiesAxiom axiom) {
        Set<OWLObjectPropertyExpression> props = new HashSet<>();
        for(OWLObjectPropertyExpression ope : axiom.getProperties()){
            ope.accept(this);
            props.add(replacedObjectPropertyExpression);
        }
        replacedAxiom = new OWLDisjointObjectPropertiesAxiomImpl(props, Collections.emptySet());
    }

    @Override
    public void visit(@Nonnull OWLObjectPropertyRangeAxiom axiom) {
        axiom.getRange().accept(this);
        OWLClassExpression range = replacedClassExpression;
        axiom.getProperty().accept(this);
        replacedAxiom = new OWLObjectPropertyRangeAxiomImpl(replacedObjectPropertyExpression, range, Collections.emptySet());
    }

    @Override
    public void visit(@Nonnull OWLObjectPropertyAssertionAxiom axiom) {
        axiom.getProperty().accept(this);
        replacedAxiom = new OWLObjectPropertyAssertionAxiomImpl(axiom.getSubject(), replacedObjectPropertyExpression, axiom.getObject(), Collections.emptySet());
    }

    @Override
    public void visit(@Nonnull OWLFunctionalObjectPropertyAxiom axiom) {
        axiom.getProperty().accept(this);
        replacedAxiom = new OWLFunctionalObjectPropertyAxiomImpl(replacedObjectPropertyExpression, Collections.emptyList());
    }

    @Override
    public void visit(@Nonnull OWLSubObjectPropertyOfAxiom axiom) {
        axiom.getSubProperty().accept(this);
        OWLObjectPropertyExpression sub = replacedObjectPropertyExpression;
        axiom.getSuperProperty().accept(this);
        replacedAxiom = new OWLSubObjectPropertyOfAxiomImpl(sub, replacedObjectPropertyExpression, Collections.emptySet());
    }

    @Override
    public void visit(@Nonnull OWLDisjointUnionAxiom axiom) {
        axiom.getOWLClass().accept(this);
        OWLClass subClass = (OWLClass) replacedClassExpression;
        Set<OWLClassExpression> union = new HashSet<>();
        for(OWLClassExpression oce : axiom.getClassExpressions()){
            oce.accept(this);
            union.add(replacedClassExpression);
        }
        replacedAxiom = new OWLDisjointUnionAxiomImpl(subClass, union, Collections.emptySet());
    }

    @Override
    public void visit(@Nonnull OWLSymmetricObjectPropertyAxiom axiom) {
        axiom.getProperty().accept(this);
        replacedAxiom = new OWLSymmetricObjectPropertyAxiomImpl(replacedObjectPropertyExpression, Collections.emptyList());
    }

    @Override
    public void visit(@Nonnull OWLDataPropertyRangeAxiom axiom) {
        unknownElements.add(axiom);
    }

    @Override
    public void visit(@Nonnull OWLFunctionalDataPropertyAxiom axiom) {
        unknownElements.add(axiom);
    }

    @Override
    public void visit(@Nonnull OWLEquivalentDataPropertiesAxiom axiom) {
        unknownElements.add(axiom);
    }

    @Override
    public void visit(@Nonnull OWLClassAssertionAxiom axiom) {
        axiom.getClassExpression().accept(this);
        replacedAxiom = new OWLClassAssertionAxiomImpl(axiom.getIndividual(), replacedClassExpression, Collections.emptySet());
    }

    @Override
    public void visit(@Nonnull OWLEquivalentClassesAxiom axiom) {
        Set<OWLClassExpression> eq = new HashSet<>();
        for(OWLClassExpression oce : axiom.getClassExpressions()){
            oce.accept(this);
            eq.add(replacedClassExpression);
        }
        replacedAxiom = new OWLEquivalentClassesAxiomImpl(eq, Collections.emptySet());
    }

    @Override
    public void visit(@Nonnull OWLDataPropertyAssertionAxiom axiom) {
        unknownElements.add(axiom);
    }

    @Override
    public void visit(@Nonnull OWLTransitiveObjectPropertyAxiom axiom) {
        axiom.getProperty().accept(this);
        replacedAxiom = new OWLTransitiveObjectPropertyAxiomImpl(replacedObjectPropertyExpression, Collections.emptyList());
    }

    @Override
    public void visit(@Nonnull OWLIrreflexiveObjectPropertyAxiom axiom) {
        axiom.getProperty().accept(this);
        replacedAxiom = new OWLIrreflexiveObjectPropertyAxiomImpl(replacedObjectPropertyExpression, Collections.emptyList());
    }

    @Override
    public void visit(@Nonnull OWLSubDataPropertyOfAxiom axiom) {
        unknownElements.add(axiom);
    }

    @Override
    public void visit(@Nonnull OWLInverseFunctionalObjectPropertyAxiom axiom) {
        axiom.getProperty().accept(this);
        replacedAxiom = new OWLInverseFunctionalObjectPropertyAxiomImpl(replacedObjectPropertyExpression, Collections.emptyList());
    }

    @Override
    public void visit(@Nonnull OWLSameIndividualAxiom axiom) {
        replacedAxiom = axiom;
    }

    @Override
    public void visit(@Nonnull OWLSubPropertyChainOfAxiom axiom) {
        axiom.getSuperProperty().accept(this);
        OWLObjectPropertyExpression sup = replacedObjectPropertyExpression;
        List<OWLObjectPropertyExpression> chain = new LinkedList<>();
        for(OWLObjectPropertyExpression ope : axiom.getPropertyChain()){
            ope.accept(this);
            chain.add(replacedObjectPropertyExpression);
        }
        replacedAxiom = new OWLSubPropertyChainAxiomImpl(chain, sup, Collections.emptySet());
    }

    @Override
    public void visit(@Nonnull OWLInverseObjectPropertiesAxiom axiom) {
        axiom.getFirstProperty().accept(this);
        OWLObjectPropertyExpression first = replacedObjectPropertyExpression;
        axiom.getSecondProperty().accept(this);
        replacedAxiom = new OWLInverseObjectPropertiesAxiomImpl(first, replacedObjectPropertyExpression, Collections.emptySet());
    }

    @Override
    public void visit(@Nonnull OWLHasKeyAxiom axiom) {
        unknownElements.add(axiom);
    }

    @Override
    public void visit(@Nonnull SWRLRule swrlRule) {
        unknownElements.add(swrlRule);
    }
    /*
     *
     * Start of OWLClassExpressionVisitor methods
     *
     */

    @Override
    public void visit(@Nonnull OWLClass owlClass) {
        OWLClassExpression repl = (OWLClassExpression) definitions.get(owlClass);
        if(repl != null)
            replacedClassExpression = repl;
        else
            replacedClassExpression = owlClass;
    }

    @Override
    public void visit(@Nonnull OWLObjectIntersectionOf owlObjectIntersectionOf) {
        Set<OWLClassExpression> operands = new HashSet<>();
        for(OWLClassExpression oce : owlObjectIntersectionOf.getOperands()){
            oce.accept(this);
            operands.add(replacedClassExpression);
        }
        replacedClassExpression = new OWLObjectIntersectionOfImpl(operands);
    }

    @Override
    public void visit(@Nonnull OWLObjectUnionOf owlObjectUnionOf) {
        Set<OWLClassExpression> operands = new HashSet<>();
        for(OWLClassExpression oce : owlObjectUnionOf.getOperands()){
            oce.accept(this);
            operands.add(replacedClassExpression);
        }
        replacedClassExpression = new OWLObjectUnionOfImpl(operands);
    }

    @Override
    public void visit(@Nonnull OWLObjectComplementOf owlObjectComplementOf) {
        owlObjectComplementOf.getOperand().accept(this);
        replacedClassExpression = new OWLObjectComplementOfImpl(replacedClassExpression);
    }

    @Override
    public void visit(@Nonnull OWLObjectSomeValuesFrom owlObjectSomeValuesFrom) {
        owlObjectSomeValuesFrom.getFiller().accept(this);
        OWLClassExpression filler = replacedClassExpression;
        owlObjectSomeValuesFrom.getProperty().accept(this);
        replacedClassExpression = new OWLObjectSomeValuesFromImpl(replacedObjectPropertyExpression, filler);
    }

    @Override
    public void visit(@Nonnull OWLObjectAllValuesFrom owlObjectAllValuesFrom) {
        owlObjectAllValuesFrom.getFiller().accept(this);
        OWLClassExpression filler = replacedClassExpression;
        owlObjectAllValuesFrom.getProperty().accept(this);
        replacedClassExpression = new OWLObjectAllValuesFromImpl(replacedObjectPropertyExpression, filler);
    }

    @Override
    public void visit(@Nonnull OWLObjectHasValue owlObjectHasValue) {
        owlObjectHasValue.getProperty().accept(this);
        replacedClassExpression = new OWLObjectHasValueImpl(replacedObjectPropertyExpression, owlObjectHasValue.getFiller());
    }

    @Override
    public void visit(@Nonnull OWLObjectMinCardinality owlObjectMinCardinality) {
        owlObjectMinCardinality.getFiller().accept(this);
        OWLClassExpression filler = replacedClassExpression;
        owlObjectMinCardinality.getProperty().accept(this);
        replacedClassExpression = new OWLObjectMinCardinalityImpl(replacedObjectPropertyExpression, owlObjectMinCardinality.getCardinality(), filler);

    }

    @Override
    public void visit(@Nonnull OWLObjectExactCardinality owlObjectExactCardinality) {
        owlObjectExactCardinality.getFiller().accept(this);
        OWLClassExpression filler = replacedClassExpression;
        owlObjectExactCardinality.getProperty().accept(this);
        replacedClassExpression = new OWLObjectExactCardinalityImpl(replacedObjectPropertyExpression, owlObjectExactCardinality.getCardinality(), filler);
    }

    @Override
    public void visit(@Nonnull OWLObjectMaxCardinality owlObjectMaxCardinality) {
        owlObjectMaxCardinality.getFiller().accept(this);
        OWLClassExpression filler = replacedClassExpression;
        owlObjectMaxCardinality.getProperty().accept(this);
        replacedClassExpression = new OWLObjectMaxCardinalityImpl(replacedObjectPropertyExpression, owlObjectMaxCardinality.getCardinality(), filler);
    }

    @Override
    public void visit(@Nonnull OWLObjectHasSelf owlObjectHasSelf) {
        owlObjectHasSelf.getProperty().accept(this);
        replacedClassExpression = new OWLObjectHasSelfImpl(replacedObjectPropertyExpression);
    }

    @Override
    public void visit(@Nonnull OWLObjectOneOf owlObjectOneOf) {
        replacedClassExpression = owlObjectOneOf;
    }

    @Override
    public void visit(@Nonnull OWLDataSomeValuesFrom owlDataSomeValuesFrom) {
        unknownElements.add(owlDataSomeValuesFrom);
    }

    @Override
    public void visit(@Nonnull OWLDataAllValuesFrom owlDataAllValuesFrom) {
        unknownElements.add(owlDataAllValuesFrom);
    }

    @Override
    public void visit(@Nonnull OWLDataHasValue owlDataHasValue) {
        unknownElements.add(owlDataHasValue);
    }

    @Override
    public void visit(@Nonnull OWLDataMinCardinality owlDataMinCardinality) {
        unknownElements.add(owlDataMinCardinality);
    }

    @Override
    public void visit(@Nonnull OWLDataExactCardinality owlDataExactCardinality) {
        unknownElements.add(owlDataExactCardinality);
    }

    @Override
    public void visit(@Nonnull OWLDataMaxCardinality owlDataMaxCardinality) {
        unknownElements.add(owlDataMaxCardinality);
    }


    /*
     *
     * Start of OWLPropertyExpressionVisitor methods
     *
     */

    @Override
    public void visit(@Nonnull OWLObjectProperty owlObjectProperty) {
        OWLObjectPropertyExpression repl = (OWLObjectPropertyExpression) definitions.get(owlObjectProperty);
        if(repl != null)
            replacedObjectPropertyExpression = repl;
        else
            replacedObjectPropertyExpression = owlObjectProperty;
    }

    @Override
    public void visit(@Nonnull OWLObjectInverseOf owlObjectInverseOf) {
        owlObjectInverseOf.getInverse().accept(this);
        replacedObjectPropertyExpression = new OWLObjectInverseOfImpl(replacedObjectPropertyExpression);
    }

    @Override
    public void visit(@Nonnull OWLDataProperty owlDataProperty) {
        unknownElements.add(owlDataProperty);
    }

    @Override
    public void visit(@Nonnull OWLAnnotationProperty owlAnnotationProperty) {
        unknownElements.add(owlAnnotationProperty);
    }
}
