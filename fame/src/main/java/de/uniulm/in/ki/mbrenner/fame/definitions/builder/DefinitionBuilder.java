package de.uniulm.in.ki.mbrenner.fame.definitions.builder;

import de.uniulm.in.ki.mbrenner.fame.definitions.evaluator.DefinitionEvaluator;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
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
        DefinitionEvaluator de = new DefinitionEvaluator();

        if(target instanceof OWLClassExpression)
            this.target = de.getDefined((OWLClassExpression) target, signature, definitions);
        else
            this.target = de.getDefined((OWLPropertyExpression) target, signature, definitions);

        this.currentTarget = this.target;
        this.error = false;
        this.dependent = new HashSet<>();
    }

    public Set<OWLEntity> getDependent(){
        return dependent;
    }

    public Map<OWLObject, OWLObject> tryDefine(OWLClassExpression expression, OWLClassExpression defineAs, Map<OWLObject, OWLObject> definitions, Set<OWLEntity> signature){
        init(definitions, signature, defineAs);
        expression.accept(classVisitor);
        return (error) ? null : this.definitions;
    }

    public Map<OWLObject, OWLObject> tryDefine(OWLPropertyExpression expression, OWLPropertyExpression defineAs, Map<OWLObject, OWLObject> definitions, Set<OWLEntity> signature){
        init(definitions, signature, defineAs);
        expression.accept(propertyVisitor);
        return (error) ? null : this.definitions;
    }

    public Map<OWLObject, OWLObject> tryDefine(OWLAxiom axiom, Map<OWLObject, OWLObject> definitions, Set<OWLEntity> signature){
        this.definitions = new HashMap<>(definitions);
        this.signature = signature;
        this.error = false;
        this.dependent = new HashSet<>();

        axiom.accept(axiomVisitor);

        return (error) ? null : this.definitions;
    }
}
