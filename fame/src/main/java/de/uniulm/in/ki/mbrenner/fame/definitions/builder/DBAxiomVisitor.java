package de.uniulm.in.ki.mbrenner.fame.definitions.builder;

import org.openrdf.model.vocabulary.OWL;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Created by spellmaker on 28.04.2016.
 */
public class DBAxiomVisitor implements OWLAxiomVisitor{
    private DefinitionBuilder parent;

    public DBAxiomVisitor(DefinitionBuilder parent){
        this.parent = parent;
    }

    @Override
    public void visit(@Nonnull OWLEquivalentClassesAxiom owlEquivalentClassesAxiom) {
        OWLClassExpression first = owlEquivalentClassesAxiom.getClassExpressionsAsList().get(0);
        OWLClassExpression second = owlEquivalentClassesAxiom.getClassExpressionsAsList().get(1);
        //try to define in both directions
        DefinitionBuilder db = new DefinitionBuilder();
        Map<OWLObject, OWLObject> def = db.tryDefine(first, second, parent.definitions, parent.signature);
        if(def == null){
            def = db.tryDefine(second, first, parent.definitions, parent.signature);
        }

        if(def == null){
            parent.error = true;
        }
        else{
            parent.definitions = def;
            parent.dependent = db.dependent;
        }
    }

    @Override
    public void visit(@Nonnull OWLSubClassOfAxiom owlSubClassOfAxiom) {
        //try to define in both directions
        DefinitionBuilder db = new DefinitionBuilder();
        Map<OWLObject, OWLObject> def = db.tryDefine(owlSubClassOfAxiom.getSubClass(), owlSubClassOfAxiom.getSuperClass(), parent.definitions, parent.signature);
        if(def == null){
            def = db.tryDefine(owlSubClassOfAxiom.getSuperClass(), owlSubClassOfAxiom.getSubClass(), parent.definitions, parent.signature);
        }

        if(def == null){
            parent.error = true;
        }
        else{
            parent.definitions = def;
            parent.dependent = db.dependent;
        }
    }

    @Override
    public void visit(@Nonnull OWLSubObjectPropertyOfAxiom owlSubObjectPropertyOfAxiom) {
        //try to define in both directions
        DefinitionBuilder db = new DefinitionBuilder();
        Map<OWLObject, OWLObject> def = db.tryDefine(owlSubObjectPropertyOfAxiom.getSubProperty(), owlSubObjectPropertyOfAxiom.getSuperProperty(), parent.definitions, parent.signature);
        if(def == null){
            def = db.tryDefine(owlSubObjectPropertyOfAxiom.getSuperProperty(), owlSubObjectPropertyOfAxiom.getSubProperty(), parent.definitions, parent.signature);
        }

        if(def == null){
            parent.error = true;
        }
        else{
            parent.definitions = def;
            parent.dependent = db.dependent;
        }
    }

    @Override
    public void visit(@Nonnull OWLTransitiveObjectPropertyAxiom owlTransitiveObjectPropertyAxiom) {
        //axiom is local, if we can interpret it with top
        DefinitionBuilder db = new DefinitionBuilder();
        Map<OWLObject, OWLObject> def = db.tryDefine(owlTransitiveObjectPropertyAxiom.getProperty(), parent.data.getOWLTopObjectProperty(), parent.definitions, parent.signature);
        if (def == null){
            parent.error = true;
        }
        else{
            parent.definitions = def;
            parent.dependent = db.dependent;
        }
    }

    @Override
    public void visit(@Nonnull OWLClassAssertionAxiom owlClassAssertionAxiom) {
        parent.error = true;
    }

    @Override
    public void visit(@Nonnull OWLObjectPropertyAssertionAxiom owlObjectPropertyAssertionAxiom) {
        parent.error = true;
    }


    @Override
    public void visit(@Nonnull OWLDeclarationAxiom owlDeclarationAxiom) {

    }

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
    public void visit(@Nonnull OWLEquivalentObjectPropertiesAxiom owlEquivalentObjectPropertiesAxiom) {

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
    public void visit(@Nonnull OWLSubPropertyChainOfAxiom owlSubPropertyChainOfAxiom) {
        parent.error = true;
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
