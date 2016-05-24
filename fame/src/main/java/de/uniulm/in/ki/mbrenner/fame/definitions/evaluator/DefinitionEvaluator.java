package de.uniulm.in.ki.mbrenner.fame.definitions.evaluator;

import de.uniulm.in.ki.mbrenner.fame.definitions.CombinedObjectProperty;
import de.uniulm.in.ki.mbrenner.fame.definitions.IndicatorClass;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Evaluates the value of complex class and property expressions under a given set of definitions and determines the locality of axioms
 *
 * Created by spellmaker on 27.04.2016.
 */
public class DefinitionEvaluator{
    Map<OWLObject, OWLObject> definitions;
    Set<OWLObject> definingSymbols;
    Set<OWLEntity> signature;
    OWLDataFactory data;

    DefinitionAxiomVisitor axiomVisitor;
    DefinitionObjectPropertyVisitor propertyVisitor;
    DefinitionClassVisitor classVisitor;
    //OWLClass unknownClass;
    //OWLObjectProperty unknownProperty;

    /**
     * Default constructor
     */
    public DefinitionEvaluator(){
        this.axiomVisitor = new DefinitionAxiomVisitor(this);
        this.propertyVisitor = new DefinitionObjectPropertyVisitor(this);
        this.classVisitor = new DefinitionClassVisitor(this);
        data = new OWLDataFactoryImpl();

        //unknownClass = data.getOWLClass(IRI.create("?UNKNOWNCLASS?"));
        //unknownProperty = data.getOWLObjectProperty(IRI.create("?UNKNOWNPROPERTY?"));
    }

    private void init(Set<OWLEntity> signature, Map<OWLObject, OWLObject> definitions){
        this.signature = signature;
        this.definitions = definitions;
        this.definingSymbols = new HashSet<>(definitions.values());
    }

    /**
     * Obtains the value of the class expression under the provided definitions
     * @param c The class expression whose value is to be obtained
     * @param signature A set of symbols, which have an unknown interpretation
     * @param definitions A set of definitions, which are to be used
     * @return An evaluated class expression, in which all symbols have been replaced with their definition and which has been simplified where possible
     */
    public OWLClassExpression getDefined(OWLClassExpression c, Set<OWLEntity> signature, Map<OWLObject, OWLObject> definitions){
        init(signature, definitions);
        c.accept(classVisitor);
        return classVisitor.currentClass;
    }


    /**
     * Obtains the value of the property expression under the provided definitions
     * @param c The property expression whose value is to be obtained
     * @param signature A set of symbols, which have an unknown interpretation
     * @param definitions A set of definitions, which are to be used
     * @return An evaluated property expression, in which all symbols have been replaced with their definition and which has been simplified where possible
     */
    public OWLPropertyExpression getDefined(OWLPropertyExpression c, Set<OWLEntity> signature, Map<OWLObject, OWLObject> definitions){
        init(signature, definitions);
        c.accept(propertyVisitor);
        return propertyVisitor.currentProperty;
    }

    /**
     * Examines if the provided axiom is a tautology given a signature and a set of definitions, which are to be applied
     * @param axiom The axiom in question
     * @param signature A set of symbols which have an unknown interpretation
     * @param definitions A set of definitions which are to be used
     * @return True, if the axiom becomes a tautology given the definitions and the signature
     */
    public boolean isDefinitionLocal(OWLAxiom axiom, Set<OWLEntity> signature, Map<OWLObject, OWLObject> definitions) {
        init(signature, definitions);
        axiom.accept(axiomVisitor);

        return axiomVisitor.locality;
    }

    boolean isFinalSymbol(OWLObject o){
        if(!(o instanceof OWLEntity)) return false;
        return signature.contains(o) || o instanceof IndicatorClass || o instanceof CombinedObjectProperty;
    }
}
