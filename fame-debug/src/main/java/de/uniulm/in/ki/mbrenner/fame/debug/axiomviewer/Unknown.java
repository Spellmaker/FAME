package de.uniulm.in.ki.mbrenner.fame.debug.axiomviewer;

import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectComplementOfImpl;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

/**
 * Created by spellmaker on 12.05.2016.
 */
public class Unknown implements OWLClass {
    @Nonnull
    @Override
    public ClassExpressionType getClassExpressionType() {
        return ClassExpressionType.OWL_CLASS;
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public boolean isClassExpressionLiteral() {
        return false;
    }

    @Nonnull
    @Override
    public EntityType<?> getEntityType() {
        return EntityType.CLASS;
    }

    @Override
    public boolean isType(@Nonnull EntityType<?> entityType) {
        return entityType.equals(getEntityType());
    }

    @Override
    public boolean isBuiltIn() {
        return false;
    }

    @Override
    public boolean isOWLClass() {
        return true;
    }

    @Nonnull
    @Override
    public OWLClass asOWLClass() {
        return this;
    }

    @Override
    public boolean isOWLObjectProperty() {
        return false;
    }

    @Nonnull
    @Override
    public OWLObjectProperty asOWLObjectProperty() {
        throw new OWLRuntimeException();
    }

    @Override
    public boolean isOWLDataProperty() {
        return false;
    }

    @Nonnull
    @Override
    public OWLDataProperty asOWLDataProperty() {
        throw new OWLRuntimeException();
    }

    @Override
    public boolean isOWLNamedIndividual() {
        return false;
    }

    @Nonnull
    @Override
    public OWLNamedIndividual asOWLNamedIndividual() {
        throw new OWLRuntimeException();
    }

    @Override
    public boolean isOWLDatatype() {
        return false;
    }

    @Nonnull
    @Override
    public OWLDatatype asOWLDatatype() {
        throw new OWLRuntimeException();
    }

    @Override
    public boolean isOWLAnnotationProperty() {
        return false;
    }

    @Nonnull
    @Override
    public OWLAnnotationProperty asOWLAnnotationProperty() {
        throw new OWLRuntimeException();
    }

    @Nonnull
    @Override
    public String toStringID() {
        return "?";
    }

    @Override
    public void accept(@Nonnull OWLEntityVisitor owlEntityVisitor) {
        owlEntityVisitor.visit(this);
    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLEntityVisitorEx<O> owlEntityVisitorEx) {
        return owlEntityVisitorEx.visit(this);
    }

    @Override
    public boolean isOWLThing() {
        return false;
    }

    @Override
    public boolean isOWLNothing() {
        return false;
    }

    @Nonnull
    @Override
    public OWLClassExpression getNNF() {
        return this;
    }

    @Nonnull
    @Override
    public OWLClassExpression getComplementNNF() {
        return new OWLObjectComplementOfImpl(this);
    }

    @Nonnull
    @Override
    public OWLClassExpression getObjectComplementOf() {
        return new OWLObjectComplementOfImpl(this);
    }

    @Nonnull
    @Override
    public Set<OWLClassExpression> asConjunctSet() {
        return Collections.singleton(this);
    }

    @Override
    public boolean containsConjunct(@Nonnull OWLClassExpression owlClassExpression) {
        return owlClassExpression.equals(this);
    }

    @Nonnull
    @Override
    public Set<OWLClassExpression> asDisjunctSet() {
        return Collections.singleton(this);
    }

    @Override
    public void accept(@Nonnull OWLClassExpressionVisitor owlClassExpressionVisitor) {
        owlClassExpressionVisitor.visit(this);
    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLClassExpressionVisitorEx<O> owlClassExpressionVisitorEx) {
        return owlClassExpressionVisitorEx.visit(this);
    }

    @Nonnull
    @Override
    public IRI getIRI() {
        return IRI.create("http://example.org/ex#", "?");
    }

    @Override
    public void accept(@Nonnull OWLNamedObjectVisitor owlNamedObjectVisitor) {
        owlNamedObjectVisitor.visit(this);
    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLNamedObjectVisitorEx<O> owlNamedObjectVisitorEx) {
        return owlNamedObjectVisitorEx.visit(this);
    }

    @Nonnull
    @Override
    public Set<OWLClassExpression> getNestedClassExpressions() {
        return Collections.singleton(this);
    }

    @Override
    public void accept(@Nonnull OWLObjectVisitor owlObjectVisitor) {
        owlObjectVisitor.visit(this);
    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLObjectVisitorEx<O> owlObjectVisitorEx) {
        return owlObjectVisitorEx.visit(this);
    }

    @Override
    public boolean isTopEntity() {
        return false;
    }

    @Override
    public boolean isBottomEntity() {
        return false;
    }

    @Override
    public int compareTo(OWLObject o) {
        return 0;
    }

    @Nonnull
    @Override
    public Set<OWLAnnotationProperty> getAnnotationPropertiesInSignature() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAnonymousIndividual> getAnonymousIndividuals() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLClass> getClassesInSignature() {
        return Collections.singleton(this);
    }

    @Override
    public boolean containsEntityInSignature(@Nonnull OWLEntity owlEntity) {
        return owlEntity.equals(this);
    }

    @Nonnull
    @Override
    public Set<OWLDataProperty> getDataPropertiesInSignature() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDatatype> getDatatypesInSignature() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLNamedIndividual> getIndividualsInSignature() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLObjectProperty> getObjectPropertiesInSignature() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLEntity> getSignature() {
        return Collections.singleton(this);
    }
}
