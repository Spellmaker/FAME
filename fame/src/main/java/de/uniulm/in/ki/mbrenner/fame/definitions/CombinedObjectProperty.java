package de.uniulm.in.ki.mbrenner.fame.definitions;

import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Created by spellmaker on 04.05.2016.
 */
public class CombinedObjectProperty implements OWLObjectProperty{
    private Map<IndicatorClass, OWLClassExpression> mapping;

    private CombinedObjectProperty(){
        this.mapping = new HashMap<>();
    }

    public CombinedObjectProperty(OWLClassExpression from, IndicatorClass to){
        this.mapping = new HashMap<>();
        this.mapping.put(to, from);
        this.mapping = Collections.unmodifiableMap(this.mapping);
    }

    public CombinedObjectProperty merge(CombinedObjectProperty other){
        CombinedObjectProperty res = new CombinedObjectProperty();
        res.mapping.putAll(this.mapping);
        res.mapping.putAll(other.mapping);
        res.mapping = Collections.unmodifiableMap(res.mapping);
        return res;
    }

    public boolean isCompatible(CombinedObjectProperty sop){
        return sop.mapping.keySet().stream().filter(x -> mapping.get(x) != null).count() == 0;
    }

    public OWLClassExpression getMapping(OWLClassExpression oce){
        return mapping.get(oce);
    }

    public boolean containsProperty(OWLObjectPropertyExpression s){
        if(s instanceof CombinedObjectProperty){
            CombinedObjectProperty other = (CombinedObjectProperty) s;
            for(Map.Entry<IndicatorClass, OWLClassExpression> entry : other.mapping.entrySet()){
                if(!entry.getValue().equals(mapping.get(entry.getKey()))){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public EntityType<?> getEntityType() {
        return EntityType.OBJECT_PROPERTY;
    }

    @Override
    public boolean isType(@Nonnull EntityType<?> entityType) {
        return entityType.equals(EntityType.OBJECT_PROPERTY);
    }

    @Override
    public boolean isBuiltIn() {
        return false;
    }

    @Override
    public boolean isOWLClass() {
        return false;
    }

    @Nonnull
    @Override
    public OWLClass asOWLClass() {
        return null;
    }

    @Override
    public boolean isOWLObjectProperty() {
        return false;
    }

    @Override
    public boolean isOWLDataProperty() {
        return false;
    }

    @Nonnull
    @Override
    public OWLDataProperty asOWLDataProperty() {
        return null;
    }

    @Override
    public boolean isOWLNamedIndividual() {
        return false;
    }

    @Nonnull
    @Override
    public OWLNamedIndividual asOWLNamedIndividual() {
        return null;
    }

    @Override
    public boolean isOWLDatatype() {
        return false;
    }

    @Nonnull
    @Override
    public OWLDatatype asOWLDatatype() {
        return null;
    }

    @Override
    public boolean isOWLAnnotationProperty() {
        return false;
    }

    @Nonnull
    @Override
    public OWLAnnotationProperty asOWLAnnotationProperty() {
        return null;
    }

    @Nonnull
    @Override
    public String toStringID() {
        return null;
    }

    @Override
    public void accept(@Nonnull OWLEntityVisitor owlEntityVisitor) {

    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLEntityVisitorEx<O> owlEntityVisitorEx) {
        return null;
    }

    @Nonnull
    @Override
    public IRI getIRI() {
        String iri = "";
        for(Map.Entry<IndicatorClass, OWLClassExpression> entry : mapping.entrySet()){
            iri += entry.getKey().getIRI().toString() + "_" + entry.getValue().toString();
        }

        return IRI.create(iri);
    }

    @Override
    public void accept(@Nonnull OWLNamedObjectVisitor owlNamedObjectVisitor) {

    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLNamedObjectVisitorEx<O> owlNamedObjectVisitorEx) {
        return null;
    }

    @Nonnull
    @Override
    public OWLObjectProperty asOWLObjectProperty() {
        return null;
    }

    @Nonnull
    @Override
    public OWLObjectPropertyExpression getInverseProperty() {
        return null;
    }

    @Nonnull
    @Override
    public OWLObjectPropertyExpression getSimplified() {
        return null;
    }

    @Nonnull
    @Override
    public OWLObjectProperty getNamedProperty() {
        return null;
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public void accept(@Nonnull OWLPropertyExpressionVisitor owlPropertyExpressionVisitor) {

    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLPropertyExpressionVisitorEx<O> owlPropertyExpressionVisitorEx) {
        return null;
    }

    @Override
    public boolean isDataPropertyExpression() {
        return false;
    }

    @Override
    public boolean isObjectPropertyExpression() {
        return false;
    }

    @Override
    public boolean isOWLTopObjectProperty() {
        return false;
    }

    @Override
    public boolean isOWLBottomObjectProperty() {
        return false;
    }

    @Override
    public boolean isOWLTopDataProperty() {
        return false;
    }

    @Override
    public boolean isOWLBottomDataProperty() {
        return false;
    }

    @Nonnull
    @Override
    public Set<OWLClassExpression> getNestedClassExpressions() {
        return null;
    }

    @Override
    public void accept(@Nonnull OWLObjectVisitor owlObjectVisitor) {

    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLObjectVisitorEx<O> owlObjectVisitorEx) {
        return null;
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
        return Collections.emptySet();
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
        return Collections.singleton(this);
    }

    @Nonnull
    @Override
    public Set<OWLEntity> getSignature() {
        return Collections.singleton(this);
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof CombinedObjectProperty){
            CombinedObjectProperty other = (CombinedObjectProperty) o;
            return other.mapping.equals(mapping);
        }
        return false;
    }

    @Override
    public String toString(){
        return mapping.toString();
    }
}