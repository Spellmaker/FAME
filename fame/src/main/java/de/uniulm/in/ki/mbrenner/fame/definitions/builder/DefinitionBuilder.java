package de.uniulm.in.ki.mbrenner.fame.definitions.builder;

import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Builds definitions for class and property expressions under which they assume a certain value
 *
 * Created by spellmaker on 28.04.2016.
 */
public class DefinitionBuilder {
    DBClassVisitor classVisitor;
    DBPropertyVisitor propertyVisitor;
    DBAxiomVisitor axiomVisitor;

    Map<OWLObject, OWLObject> definitions;
    Set<OWLEntity> signature;
    OWLObject target;
    OWLObject currentTarget;

    OWLDataFactory data;

    boolean error;

    Set<OWLEntity> dependent;

    /**
     * Default constructor
     */
    public DefinitionBuilder(){
        classVisitor = new DBClassVisitor(this);
        propertyVisitor = new DBPropertyVisitor(this);
        axiomVisitor = new DBAxiomVisitor(this);
        data = new OWLDataFactoryImpl();
    }

    private void init(Map<OWLObject, OWLObject> definitions, Set<OWLEntity> signature, OWLObject target){
        this.definitions = new HashMap<>(definitions);
        this.signature = signature;
        //fully evaluate target just in case
        //DefinitionEvaluator de = new DefinitionEvaluator();

        if(target instanceof OWLClassExpression)
            this.target = target;//de.getDefined((OWLClassExpression) target, signature, definitions);
        else
            this.target = target;//de.getDefined((OWLPropertyExpression) target, signature, definitions);

        this.currentTarget = this.target;
        this.error = false;
        this.dependent = new HashSet<>();
    }

    /**
     * Provides access to the entities used in the definition
     * @return A set of entities which have been used in the definition and are therefore relevant for its validity
     */
    public Set<OWLEntity> getDependent(){
        return dependent;
    }

    /**
     * Attempts to find values for the concept and property names in the provided class expression, such that the expression assumes the provided value.
     * Such a definition can only be found, if it doesn't contradict the already existing definitions or the signature
     * @param expression The expression which is supposed to assume a certain value
     * @param defineAs The value the expression is supposed to assume
     * @param definitions The currently valid definitions which cannot be contradicted
     * @param signature The symbols which have some unknown interpretation and can thus not be reinterpreted
     * @return An updated map of definitions, which will also contain the previously valid definitions or null, if the request cannot be satisfied
     */
    public Map<OWLObject, OWLObject> tryDefine(@Nonnull OWLClassExpression expression, @Nonnull OWLClassExpression defineAs, @Nonnull Map<OWLObject, OWLObject> definitions, @Nonnull Set<OWLEntity> signature){
        init(definitions, signature, defineAs);
        expression.accept(classVisitor);
        return (error) ? null : this.definitions;
    }

    /**
     * Attempts to find values for the concept and property names in the provided class expression, such that the expression assumes the provided value.
     * Such a definition can only be found, if it doesn't contradict the already existing definitions or the signature
     * @param expression The expression which is supposed to assume a certain value
     * @param defineAs The value the expression is supposed to assume
     * @param definitions The currently valid definitions which cannot be contradicted
     * @param signature The symbols which have some unknown interpretation and can thus not be reinterpreted
     * @return An updated map of definitions, which will also contain the previously valid definitions or null, if the request cannot be satisfied
     */
    public Map<OWLObject, OWLObject> tryDefine(@Nonnull OWLPropertyExpression expression, @Nonnull OWLPropertyExpression defineAs, @Nonnull Map<OWLObject, OWLObject> definitions, @Nonnull Set<OWLEntity> signature){
        init(definitions, signature, defineAs);
        expression.accept(propertyVisitor);
        return (error) ? null : this.definitions;
    }

    /**
     * Attempts to find definitions, such that the axiom becomes local under the provided signature and the existing definitions.
     * @param axiom The axiom which needs to become local
     * @param definitions The existing definitions, which cannot be contradicted
     * @param signature The signature under which the axiom is supposed to be local
     * @return An updated map of definitions or null, if the request cannot be satisfied
     */
    public Map<OWLObject, OWLObject> tryDefine(@Nonnull OWLAxiom axiom, @Nonnull Map<OWLObject, OWLObject> definitions, @Nonnull Set<OWLEntity> signature){
        this.definitions = new HashMap<>(definitions);
        this.signature = signature;
        this.error = false;
        this.dependent = new HashSet<>();

        axiom.accept(axiomVisitor);

        return (error) ? null : this.definitions;
    }
}
