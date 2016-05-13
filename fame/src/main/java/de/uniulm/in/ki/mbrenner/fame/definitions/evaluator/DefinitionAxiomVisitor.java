package de.uniulm.in.ki.mbrenner.fame.definitions.evaluator;

import de.uniulm.in.ki.mbrenner.fame.definitions.CombinedObjectProperty;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;

/**
 * Created by spellmaker on 27.04.2016.
 */
public class DefinitionAxiomVisitor extends DefinitionVisitor implements OWLAxiomVisitor{
    OWLAxiom currentAxiom;
    boolean locality;

    //currently not supported: ABox

    public DefinitionAxiomVisitor(DefinitionEvaluator parent) {
        super(parent);
    }

    @Override
    public void visit(@Nonnull OWLDeclarationAxiom owlDeclarationAxiom) {
        //add later
        locality = true;
        return;
    }

    @Override
    public void visit(@Nonnull OWLSubClassOfAxiom owlSubClassOfAxiom) {
        owlSubClassOfAxiom.getSubClass().accept(parent.classVisitor);
        OWLClassExpression subClass = parent.classVisitor.currentClass;
        owlSubClassOfAxiom.getSuperClass().accept(parent.classVisitor);
        OWLClassExpression supClass = parent.classVisitor.currentClass;

        if(subClass.isBottomEntity() || supClass.isTopEntity()){
            locality = true;
            return;
        }

        if(subClass.equals(supClass)){
            locality = true;
            return;
        }
        locality = false;
        return;
    }

    @Override
    public void visit(@Nonnull OWLSubObjectPropertyOfAxiom owlSubObjectPropertyOfAxiom) {
        owlSubObjectPropertyOfAxiom.getSubProperty().accept(parent.propertyVisitor);
        OWLPropertyExpression subProp = parent.propertyVisitor.currentProperty;
        owlSubObjectPropertyOfAxiom.getSuperProperty().accept(parent.propertyVisitor);
        OWLPropertyExpression supProp = parent.propertyVisitor.currentProperty;

        if(subProp.isBottomEntity() || supProp.isTopEntity()){
            locality = true;
            return;
        }

        if(subProp.equals(supProp)){
            locality = true;
            return;
        }

        if(supProp instanceof CombinedObjectProperty){
            locality = ((CombinedObjectProperty) supProp).containsProperty((OWLObjectPropertyExpression) subProp);
        }

        locality = false;
        return;
    }

    @Override
    public void visit(@Nonnull OWLEquivalentClassesAxiom owlEquivalentClassesAxiom) {
        OWLClassExpression first = owlEquivalentClassesAxiom.getClassExpressionsAsList().get(0);
        OWLClassExpression second = owlEquivalentClassesAxiom.getClassExpressionsAsList().get(1);
        first.accept(parent.classVisitor);
        first = parent.classVisitor.currentClass;
        second.accept(parent.classVisitor);
        second = parent.classVisitor.currentClass;

        locality = first.equals(second);
    }

    @Override
    public void visit(@Nonnull OWLTransitiveObjectPropertyAxiom owlTransitiveObjectPropertyAxiom) {
        if(parent.signature.contains(owlTransitiveObjectPropertyAxiom.getProperty())){
            locality = false;
            return;
        }
        //TODO: combined object properties should also be transitive? There are cases, but not a lot
        /*OWLObjectProperty def = (OWLObjectProperty) parent.definitions.get(owlTransitiveObjectPropertyAxiom.getProperty());
        if(def != null && !def.equals(parent.data.getOWLTopObjectProperty()) && !def.equals(parent.data.getOWLBottomObjectProperty())){
            locality = false;
            return;
        }*/

        owlTransitiveObjectPropertyAxiom.getProperty().accept(parent.propertyVisitor);

        locality = parent.propertyVisitor.currentProperty.equals(parent.data.getOWLBottomObjectProperty()) ||
                    parent.propertyVisitor.currentProperty.equals(parent.data.getOWLTopObjectProperty());
    }

    @Override
    public void visit(@Nonnull OWLClassAssertionAxiom owlClassAssertionAxiom) {
        owlClassAssertionAxiom.getClassExpression().accept(parent.classVisitor);
        locality = parent.classVisitor.currentClass.equals(parent.data.getOWLThing());
    }

    @Override
    public void visit(@Nonnull OWLObjectPropertyAssertionAxiom owlObjectPropertyAssertionAxiom) {
        owlObjectPropertyAssertionAxiom.getProperty().accept(parent.propertyVisitor);
        locality = parent.propertyVisitor.currentProperty.equals(parent.data.getOWLTopObjectProperty());
    }

    @Override
    public void visit(@Nonnull OWLSubPropertyChainOfAxiom owlSubPropertyChainOfAxiom) {
        //a chain is a tautology if:
        // - a chain element is bot
        // - the head is top
        for(OWLObjectPropertyExpression o : owlSubPropertyChainOfAxiom.getPropertyChain()){
            o.accept(parent.propertyVisitor);
            if(parent.propertyVisitor.currentProperty.equals(parent.data.getOWLBottomObjectProperty())){
                locality = true;
                return;
            }
        }

        owlSubPropertyChainOfAxiom.getSuperProperty().accept(parent.propertyVisitor);
        locality = parent.propertyVisitor.currentProperty.equals(parent.data.getOWLTopObjectProperty());
    }

    @Override
    public void visit(@Nonnull OWLEquivalentObjectPropertiesAxiom owlEquivalentObjectPropertiesAxiom) {

    }



    //non EL+ or ABox axioms

    @Override
    public void visit(@Nonnull OWLDatatypeDefinitionAxiom owlDatatypeDefinitionAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLAnnotationAssertionAxiom owlAnnotationAssertionAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLSubAnnotationPropertyOfAxiom owlSubAnnotationPropertyOfAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLAnnotationPropertyDomainAxiom owlAnnotationPropertyDomainAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLAnnotationPropertyRangeAxiom owlAnnotationPropertyRangeAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLNegativeObjectPropertyAssertionAxiom owlNegativeObjectPropertyAssertionAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLAsymmetricObjectPropertyAxiom owlAsymmetricObjectPropertyAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLReflexiveObjectPropertyAxiom owlReflexiveObjectPropertyAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLDisjointClassesAxiom owlDisjointClassesAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLDataPropertyDomainAxiom owlDataPropertyDomainAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLObjectPropertyDomainAxiom owlObjectPropertyDomainAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLNegativeDataPropertyAssertionAxiom owlNegativeDataPropertyAssertionAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLDifferentIndividualsAxiom owlDifferentIndividualsAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLDisjointDataPropertiesAxiom owlDisjointDataPropertiesAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLDisjointObjectPropertiesAxiom owlDisjointObjectPropertiesAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLObjectPropertyRangeAxiom owlObjectPropertyRangeAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLFunctionalObjectPropertyAxiom owlFunctionalObjectPropertyAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLDisjointUnionAxiom owlDisjointUnionAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLSymmetricObjectPropertyAxiom owlSymmetricObjectPropertyAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLDataPropertyRangeAxiom owlDataPropertyRangeAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLFunctionalDataPropertyAxiom owlFunctionalDataPropertyAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLEquivalentDataPropertiesAxiom owlEquivalentDataPropertiesAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLDataPropertyAssertionAxiom owlDataPropertyAssertionAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLIrreflexiveObjectPropertyAxiom owlIrreflexiveObjectPropertyAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLSubDataPropertyOfAxiom owlSubDataPropertyOfAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLInverseFunctionalObjectPropertyAxiom owlInverseFunctionalObjectPropertyAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLSameIndividualAxiom owlSameIndividualAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLInverseObjectPropertiesAxiom owlInverseObjectPropertiesAxiom) {

    }

    @Override
    public void visit(@Nonnull OWLHasKeyAxiom owlHasKeyAxiom) {

    }

    @Override
    public void visit(@Nonnull SWRLRule swrlRule) {

    }
}
