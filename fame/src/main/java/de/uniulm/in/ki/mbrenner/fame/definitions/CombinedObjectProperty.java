package de.uniulm.in.ki.mbrenner.fame.definitions;

import de.uniulm.in.ki.mbrenner.owlprinter.OWLPrinter;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Represents a special property which maps IndicatorClass objects to other classes
 *
 * Created by spellmaker on 04.05.2016.
 */
public class CombinedObjectProperty implements OWLObjectProperty{
    private Map<IndicatorClass, OWLClassExpression> mapping;

    private CombinedObjectProperty(){
        this.mapping = new HashMap<>();
    }

    /**
     * Builds a new combined object property
     * @param from The value the indicator class is mapped to
     * @param to The indicator class
     */
    public CombinedObjectProperty(OWLClassExpression from, IndicatorClass to){
        this.mapping = new HashMap<>();
        this.mapping.put(to, from);
        this.mapping = Collections.unmodifiableMap(this.mapping);
    }

    @Override
    public int hashCode(){
        int val = mapping.hashCode();
        return val + val;
    }

    /**
     * Creates a new combined object property as the result of merging another with this instance
     * The result may be invalid, as there are no sanity checks made. In particular, mappings from
     * the other property may override mappings of this property.
     * If the operations succeeds, the property satisfies all mappings from both properties
     * @param other A combined object property
     * @return A merged combined object property
     */
    public CombinedObjectProperty merge(CombinedObjectProperty other){
        CombinedObjectProperty res = new CombinedObjectProperty();
        res.mapping.putAll(this.mapping);
        res.mapping.putAll(other.mapping);
        res.mapping = Collections.unmodifiableMap(res.mapping);
        return res;
    }

    /**
     * Determines if this property is compatible to another property
     * A property is compatible, if there are no conflicting mappings
     * @param sop A property which shall be tested for compatibility
     * @return True, if the two properties are compatible
     */
    public boolean isCompatible(CombinedObjectProperty sop){
        return sop.mapping.keySet().stream().filter(x -> mapping.get(x) != null).count() == 0;
    }

    /**
     * Tries to find a mapping for the provided class expression
     * @param oce A class expression, which needs to be an indicator class to obtain any value different from null
     * @return The mapping for oce or null, if there is no mapping
     */
    public OWLClassExpression getMapping(OWLClassExpression oce){
        if(!(oce instanceof IndicatorClass)) return null;
        return mapping.get(oce);
    }

    public boolean hasMappingReasoner(OWLReasoner reasoner, OWLClassExpression oce){
        for(Map.Entry<IndicatorClass, OWLClassExpression> e : mapping.entrySet()){
            if(reasoner.isEntailed(new OWLSubClassOfAxiomImpl(e.getKey().getSourceClass(), oce, Collections.emptySet()))){
                return true;
            }
            Set<OWLClassExpression> eq = new HashSet<>();
            eq.add(e.getKey().getSourceClass());
            eq.add(oce);
            if(reasoner.isEntailed(new OWLEquivalentClassesAxiomImpl(eq, Collections.emptySet()))){
                return true;
            }
        }
        return false;
    }

    /**
     * Tests if the provided property expression is a sub property of this property.
     * This can only be guaranteed, if the other property is also a combined object property,
     * in which case the sub-properties can be determined by testing, if all mappings of the
     * other property are contained in this property
     * @param s A property expression
     * @return True, if this property contains the other property
     */
    public boolean containsProperty(OWLObjectPropertyExpression s){
        if(s instanceof CombinedObjectProperty){
            CombinedObjectProperty other = (CombinedObjectProperty) s;
            return other.mapping.equals(mapping);
            /*for(Map.Entry<IndicatorClass, OWLClassExpression> entry : other.mapping.entrySet()){
                if(!entry.getValue().equals(mapping.get(entry.getKey()))){
                    return false;
                }
            }
            return true;*/
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
        throw new OWLRuntimeException();
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
        return getIRI().toString();
    }

    @Override
    public void accept(@Nonnull OWLEntityVisitor owlEntityVisitor) {

    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLEntityVisitorEx<O> owlEntityVisitorEx) {
        return owlEntityVisitorEx.visit(this);
    }

    @Nonnull
    @Override
    public IRI getIRI() {
        String iri = "";
        for(Map.Entry<IndicatorClass, OWLClassExpression> entry : mapping.entrySet()){
            iri += "COP{" + OWLPrinter.getString(entry.getKey().getIRI()) + "}_{" + OWLPrinter.getString(entry.getValue()) + "}";
        }

        return IRI.create(iri);
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
    public OWLObjectProperty asOWLObjectProperty() {
        throw new OWLRuntimeException();
    }

    @Nonnull
    @Override
    public OWLObjectPropertyExpression getInverseProperty() {
        throw new OWLRuntimeException();
    }

    @Deprecated
    @Nonnull
    @Override
    public OWLObjectPropertyExpression getSimplified() {
        throw new OWLRuntimeException();
    }

    @Nonnull
    @Override
    public OWLObjectProperty getNamedProperty() {
        throw new OWLRuntimeException();
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public void accept(@Nonnull OWLPropertyExpressionVisitor owlPropertyExpressionVisitor) {
        owlPropertyExpressionVisitor.visit(this);
    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLPropertyExpressionVisitorEx<O> owlPropertyExpressionVisitorEx) {
        return owlPropertyExpressionVisitorEx.visit(this);
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
        throw new OWLRuntimeException();
    }

    @Override
    public void accept(@Nonnull OWLObjectVisitor owlObjectVisitor) {

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
    public int compareTo(@Nonnull OWLObject o) {
        return o.equals(this) ? 0 : -1;
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
    public @Nonnull String toString(){
        return mapping.toString();
    }
}
